#include <algorithm>
#include <stdexcept>

#include <tightdb/util/assert.hpp>
#include <tightdb/util/utf8.hpp>

#include "util.hpp"
#include "com_tightdb_internal_Util.h"


using namespace std;
using namespace tightdb;
using namespace tightdb::util;

namespace {

// This assumes that 'jchar' is an integral type with at least 16
// non-sign value bits, that is, an unsigned 16-bit integer, or any
// signed or unsigned integer with more than 16 bits.
struct JcharTraits {
    static jchar to_int_type(jchar c)  TIGHTDB_NOEXCEPT { return c; }
    static jchar to_char_type(jchar i) TIGHTDB_NOEXCEPT { return i; }
};

struct JStringCharsAccessor {
    JStringCharsAccessor(JNIEnv* e, jstring s):
        m_env(e), m_string(s), m_data(e->GetStringChars(s,0)), m_size(get_size(e,s)) {}
    ~JStringCharsAccessor()
    {
        m_env->ReleaseStringChars(m_string, m_data);
    }
    const jchar* data() const TIGHTDB_NOEXCEPT { return m_data; }
    size_t size() const TIGHTDB_NOEXCEPT { return m_size; }

private:
    JNIEnv* const m_env;
    const jstring m_string;
    const jchar* const m_data;
    const size_t m_size;

    static size_t get_size(JNIEnv* e, jstring s)
    {
        size_t size;
        if (int_cast_with_overflow_detect(e->GetStringLength(s), size))
            throw runtime_error("String size overflow");
        return size;
    }
};

} // anonymous namespace



void ThrowException(JNIEnv* env, ExceptionKind exception, std::string classStr, std::string itemStr)
{
    std::string message;
    jclass jExceptionClass = NULL;

    TR_ERR((env, "\njni: ThrowingException %d, %s, %s.\n", exception, classStr.c_str(), itemStr.c_str()));

    switch (exception) {
        case ClassNotFound:
            jExceptionClass = env->FindClass("java/lang/ClassNotFoundException");
            message = "Class '" + classStr + "' could not be located.";
            break;

        case NoSuchField:
            jExceptionClass = env->FindClass("java/lang/NoSuchFieldException");
            message = "Field '" + itemStr + "' could not be located in class com.tightdb." + classStr;
            break;

        case NoSuchMethod:
            jExceptionClass = env->FindClass("java/lang/NoSuchMethodException");
            message = "Method '" + itemStr + "' could not be located in class com.tightdb." + classStr;
            break;

        case IllegalArgument:
            jExceptionClass = env->FindClass("java/lang/IllegalArgumentException");
            message = "Illegal Argument: " + classStr;
            break;

        case TableInvalid:
            jExceptionClass = env->FindClass("java/lang/IllegalStateException");
            message = "Illegal State: " + classStr;
            break;

        case IOFailed:
            jExceptionClass = env->FindClass("com/tightdb/IOException");
            message = "Failed to open " + classStr + ". " + itemStr;
            break;

        case FileNotFound:
            jExceptionClass = env->FindClass("com/tightdb/IOException");
            message = "File not found: " + classStr + ".";
            break;

        case FileAccessError:
            jExceptionClass = env->FindClass("com/tightdb/IOException");
            message = "Failed to access: " + classStr + ". " + itemStr;
            break;

        case IndexOutOfBounds:
            jExceptionClass = env->FindClass("java/lang/ArrayIndexOutOfBoundsException");
            message = classStr;
            break;

        case UnsupportedOperation:
            jExceptionClass = env->FindClass("java/lang/UnsupportedOperationException");
            message = classStr;
            break;

        case OutOfMemory:
            jExceptionClass = env->FindClass("com/tightdb/OutOfMemoryError");
            message = classStr + " " + itemStr;
            break;

        case Unspecified:
            jExceptionClass = env->FindClass("java/lang/RuntimeException");
            message = "Unspecified exception. " + classStr;
            break;

        case RuntimeError:
            jExceptionClass = env->FindClass("java/lang/RuntimeException");
            message = classStr;
            break;
    }
    if (jExceptionClass != NULL)
        env->ThrowNew(jExceptionClass, message.c_str());
    else {
        TR_ERR((env, "\nERROR: Couldn't throw exception.\n"));
    }

    env->DeleteLocalRef(jExceptionClass);
}

jclass GetClass(JNIEnv* env, const char* classStr)
{
    jclass localRefClass = env->FindClass(classStr);
    if (localRefClass == NULL) {
        ThrowException(env, ClassNotFound, classStr);
        return NULL;
    }

    jclass myClass = reinterpret_cast<jclass>( env->NewGlobalRef(localRefClass) );
    env->DeleteLocalRef(localRefClass);
    return myClass;
}

void jprint(JNIEnv *env, char *txt)
{
#if 1
    static_cast<void>(env);
    fprintf(stderr, " -- JNI: %s", txt);  fflush(stderr);
#else
    static jclass myClass = GetClass(env, "com/tightdb/util");
    static jmethodID myMethod = env->GetStaticMethodID(myClass, "javaPrint", "(Ljava/lang/String;)V");
    if (myMethod)
        env->CallStaticVoidMethod(myClass, myMethod, env->NewStringUTF(txt));
#endif
}

void jprintf(JNIEnv *env, const char *format, ...)
{
    va_list argptr;
    char buf[200];
    va_start(argptr, format);
    //vfprintf(stderr, format, argptr);
    vsnprintf(buf, 200, format, argptr);
    jprint(env, buf);
    va_end(argptr);
}

bool GetBinaryData(JNIEnv* env, jobject jByteBuffer, tightdb::BinaryData& bin)
{
    const char* data = static_cast<char*>(env->GetDirectBufferAddress(jByteBuffer));
    if (!data) {
        ThrowException(env, IllegalArgument, "ByteBuffer is invalid");
        return false;
    }
    jlong size = env->GetDirectBufferCapacity(jByteBuffer);
    if (size < 0) {
        ThrowException(env, IllegalArgument, "Can't get BufferCapacity.");
        return false;
    }
    bin = BinaryData(data, S(size));
    return true;
}


jstring to_jstring(JNIEnv* env, StringData str)
{
    // For efficiency, if the incoming UTF-8 string is sufficiently
    // small, we will attempt to store the UTF-16 output into a stack
    // allocated buffer of static size. Otherwise we will have to
    // dynamically allocate the output buffer after calculating its
    // size.

    const size_t stack_buf_size = 48;
    jchar stack_buf[stack_buf_size];
    UniquePtr<jchar[]> dyn_buf;

    const char* in_begin = str.data();
    const char* in_end   = str.data() + str.size();
    jchar* out_begin = stack_buf;
    jchar* out_curr  = stack_buf;
    jchar* out_end   = stack_buf + stack_buf_size;

    typedef Utf8x16<jchar, JcharTraits> Xcode;

    if (str.size() <= stack_buf_size) {
        if (!Xcode::to_utf16(in_begin, in_end, out_curr, out_end)) goto bad_utf8;
        if (in_begin == in_end) goto transcode_complete;
    }

    {
        const char* in_begin2 = in_begin;
        size_t size = Xcode::find_utf16_buf_size(in_begin2, in_end);
        if (in_begin2 != in_end) goto bad_utf8;
        if (int_add_with_overflow_detect(size, stack_buf_size))
            throw runtime_error("String size overflow");
        dyn_buf.reset(new jchar[size]);
        out_curr = copy(out_begin, out_curr, dyn_buf.get());
        out_begin = dyn_buf.get();
        out_end   = dyn_buf.get() + size;
        if (!Xcode::to_utf16(in_begin, in_end, out_curr, out_end)) goto bad_utf8;
        TIGHTDB_ASSERT(in_begin == in_end);
    }

  transcode_complete:
    {
        jsize out_size;
        if (int_cast_with_overflow_detect(out_curr - out_begin, out_size))
            throw runtime_error("String size overflow");

        return env->NewString(out_begin, out_size);
    }

  bad_utf8:
    throw runtime_error("Bad UTF-8 encoding");
}


JStringAccessor::JStringAccessor(JNIEnv* env, jstring str)
{
    // For efficiency, if the incoming UTF-16 string is sufficiently
    // small, we will choose an UTF-8 output buffer whose size (in
    // bytes) is simply 4 times the number of 16-bit elements in the
    // input. This is guaranteed to be enough. However, to avoid
    // excessive over allocation, this is not done for larger input
    // strings.

    JStringCharsAccessor chars(env, str);

    typedef Utf8x16<jchar, JcharTraits> Xcode;
    size_t max_project_size = 48;
    TIGHTDB_ASSERT(max_project_size <= numeric_limits<size_t>::max()/4);
    size_t buf_size;
    if (chars.size() <= max_project_size) {
        buf_size = chars.size() * 4;
    }
    else {
        const jchar* begin = chars.data();
        const jchar* end   = begin + chars.size();
        buf_size = Xcode::find_utf8_buf_size(begin, end);
    }
    m_data.reset(new char[buf_size]);   // throws
    {
        const jchar* in_begin = chars.data();
        const jchar* in_end   = in_begin + chars.size();
        char* out_begin = m_data.get();
        char* out_end   = m_data.get() + buf_size;
        if (!Xcode::to_utf8(in_begin, in_end, out_begin, out_end))
            throw runtime_error("Bad UTF-16 encoding");
        TIGHTDB_ASSERT(in_begin == in_end);
        m_size = out_begin - m_data.get();
    }
}


// native testcases
