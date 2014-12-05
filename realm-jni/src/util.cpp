/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <algorithm>
#include <stdexcept>

#include <tightdb/util/assert.hpp>
#include <tightdb/util/utf8.hpp>
#include <tightdb/util/buffer.hpp>

#include "util.hpp"
#include "io_realm_internal_Util.h"

using namespace std;
using namespace tightdb;
using namespace tightdb::util;


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
            message = "Field '" + itemStr + "' could not be located in class io.realm." + classStr;
            break;

        case NoSuchMethod:
            jExceptionClass = env->FindClass("java/lang/NoSuchMethodException");
            message = "Method '" + itemStr + "' could not be located in class io.realm." + classStr;
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
            jExceptionClass = env->FindClass("io/realm/exceptions/RealmIOException");
            message = "Failed to open " + classStr + ". " + itemStr;
            break;

        case FileNotFound:
            jExceptionClass = env->FindClass("io/realm/exceptions/RealmIOException");
            message = "File not found: " + classStr + ".";
            break;

        case FileAccessError:
            jExceptionClass = env->FindClass("io/realm/exceptions/RealmIOException");
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
            jExceptionClass = env->FindClass("io/realm/internal/OutOfMemoryError");
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

        case RowInvalid:
            jExceptionClass = env->FindClass("java/lang/IllegalStateException");
            message = "Illegal State: " + classStr;
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
    static jclass myClass = GetClass(env, "io/realm/internal/Util");
    static jmethodID myMethod = env->GetStaticMethodID(myClass, "javaPrint", "(Ljava/lang/String;)V");
    if (myMethod)
        env->CallStaticVoidMethod(myClass, myMethod, to_jstring(env, txt));
    else
        ThrowException(env, NoSuchMethod, "Util", "javaPrint");
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


//*********************************************************************
// String handling
//*********************************************************************

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

string string_to_hex(const string& message, StringData& str) {
    ostringstream ret;

    const char *s = str.data();
    ret << message;
    for (string::size_type i = 0; i < str.size(); ++i)
        ret << " 0x" << std::hex << std::setfill('0') << std::setw(2) << (int)s[i];
    return ret.str();
}

string string_to_hex(const string& message, const jchar *str, size_t size) {
    ostringstream ret;

    ret << message;
    for (size_t i = 0; i < size; ++i)
        ret << " 0x" << std::hex << std::setfill('0') << std::setw(2) << (int)str[i];
    return ret.str();
}


jstring to_jstring(JNIEnv* env, StringData str)
{
    // Input is UTF-8 and output is UTF-16. Invalid UTF-8 input is
    // silently converted to Unicode replacement characters.

    // We use a small fixed size stack-allocated output buffer to avoid the cost
    // of dynamic allocation for short input strings. If this buffer turns out
    // to be too small, we proceed by calulating an estimate for the actual
    // required output buffer size, and then allocate the buffer dynamically.

    const size_t stack_buf_size = 48;
    jchar stack_buf[stack_buf_size];
    Buffer<jchar> dyn_buf;

    const char* in = str.data();
    const char* in_end = in + str.size();
    jchar* out = stack_buf;
    jchar* out_begin = out;
    jchar* out_end = out_begin + stack_buf_size;

    for (;;) {
        typedef Utf8x16<jchar, JcharTraits> Xcode;
        Xcode::to_utf16(in, in_end, out, out_end);
        bool end_of_input = in == in_end;
        if (end_of_input)
            break;
        bool bad_input = out != out_end;
        if (bad_input) {
            // Discard one or more invalid bytes from the input. We shall follow
            // the stardard way of doing this, namely by first discarding the
            // leading invalid byte, which must either be a sequence lead byte
            // (11xxxxxx) or a stray continuation byte (10xxxxxx), and then
            // discard any additional continuation bytes following leading
            // invalid byte.
            for (;;) {
                ++in;
                end_of_input = in == in_end;
                if (end_of_input)
                    break;
                bool next_byte_is_continuation = (unsigned(*in) & 0xC0 == 0x80);
                if (!next_byte_is_continuation)
                    break;
            }
        }
        size_t used_size = out - out_begin; // What we already have
        size_t min_capacity = used_size;
        min_capacity += 1; // Make space for a replacement character
        const char* in_2 = in; // Avoid clobbering `in`
        if (int_add_with_overflow_detect(min_capacity, Xcode::find_utf16_buf_size(in_2, in_end)))
            throw runtime_error("Buffer size overflow");
        bool copy_stack_buf = dyn_buf.size() == 0;
        size_t used_dyn_buf_size = copy_stack_buf ? 0 : used_size;
        dyn_buf.reserve(used_dyn_buf_size, min_capacity);
        if (copy_stack_buf)
            copy(out_begin, out, dyn_buf.data());
        out_begin = dyn_buf.data();
        out_end = dyn_buf.data() + dyn_buf.size();
        out = out_begin + used_size;
        if (bad_input)
            *out++ = JcharTraits::to_char_type(0xFFFD); // Unicode replacement character
    }

    jsize out_size;
    if (int_cast_with_overflow_detect(out - out_begin, out_size))
        throw runtime_error("String size overflow");

    return env->NewString(out_begin, out_size);
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
    m_data.reset(new char[buf_size]);  // throws
    {
        const jchar* in_begin = chars.data();
        const jchar* in_end   = in_begin + chars.size();
        char* out_begin = m_data.get();
        char* out_end   = m_data.get() + buf_size;
        if (!Xcode::to_utf8(in_begin, in_end, out_begin, out_end)) {
            throw runtime_error(string_to_hex("Failure when converting to UTF-8", chars.data(), chars.size()));
        }
        TIGHTDB_ASSERT(in_begin == in_end);
        m_size = out_begin - m_data.get();
    }
}
