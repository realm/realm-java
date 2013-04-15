#include <algorithm>
#include <stdexcept>

#include <tightdb/unique_ptr.hpp>
#include <tightdb/safe_int_ops.hpp>
#include <tightdb/assert.hpp>
#include <tightdb/utf8.hpp>

#include "util.hpp"
#include "com_tightdb_internal_util.hpp"


using namespace std;
using namespace tightdb;

namespace {

struct JcharTraits {
    static jchar to_int_type(jchar c)  TIGHTDB_NOEXCEPT { return c; }
    static jchar to_char_type(jchar i) TIGHTDB_NOEXCEPT { return i; }
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
        case TableInvalid:
            jExceptionClass = env->FindClass("java/lang/IllegalArgumentException");
            message = "Illegal Argument: " + classStr;
            break;

        case IOFailed:
            jExceptionClass = env->FindClass("java/lang/IOException");
            message = "Failed to open " + classStr;
            break;

        case IndexOutOfBounds:
            jExceptionClass = env->FindClass("java/lang/ArrayIndexOutOfBoundsException");
            message = classStr;
            break;

        case UnsupportedOperation:
            jExceptionClass = env->FindClass("java/lang/UnsupportedOperationException");
            message = classStr;
            break;

        default:
            TIGHTDB_ASSERT(false);
            return;
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
    if (size < 1) {
        ThrowException(env, IllegalArgument, "Can't get BufferCapacity.");
        return false;
    }
    bin = BinaryData(data, S(size));
    return true;
}


jstring to_jstring(JNIEnv* env, StringData str)
{
    // Note: JNI offers methods to convert between modified UTF-8 and
    // UTF-16. Unfortunately these methods are not appropriate in this
    // context. The reason is that they use a modified version of
    // UTF-8 where U+0000 is stored as 0xC0 0x80 instead of 0x00 and
    // where a character in the range U+10000 to U+10FFFF is stored as
    // two consecutive UTF-8 encodings of the corresponding UTF-16
    // surrogate pair. Because Tightdb uses proper UTF-8, we need to
    // do the transcoding ourselves.
    //
    // See also http://en.wikipedia.org/wiki/UTF-8#Modified_UTF-8
    //
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
        size_t size = calc_buf_size_utf8_to_utf16(in_begin2, in_end);
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
