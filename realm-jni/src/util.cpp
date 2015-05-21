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

#include <realm/util/assert.hpp>
#include "utf8.hpp"

#include "util.hpp"
#include "io_realm_internal_Util.h"

using namespace std;
using namespace realm;
using namespace realm::util;

void ConvertException(JNIEnv* env, const char *file, int line)
{
    std::ostringstream ss;
    try {
        throw;
    }
    catch (std::bad_alloc& e) {
        ss << e.what() << " in " << file << " line " << line;
        ThrowException(env, OutOfMemory, ss.str());
    }
    catch (std::exception& e) {
        ss << e.what() << " in " << file << " line " << line;
        ThrowException(env, Unspecified, ss.str());
    }
    catch (...) { \
        REALM_ASSERT(false);
        ss << "Exception in " << file << " line " << line;
        ThrowException(env, RuntimeError, ss.str());
    }
    /* above (...) is not needed if we only throw exceptions derived from std::exception */
}

void ThrowException(JNIEnv* env, ExceptionKind exception, const char *classStr)
{
    ThrowException(env, exception, classStr, "");
}

void ThrowException(JNIEnv* env, ExceptionKind exception, const std::string& classStr, const std::string& itemStr)
{
    std::string message;
    jclass jExceptionClass = NULL;

    TR_ERR("jni: ThrowingException %d, %s, %s.", exception, classStr.c_str(), itemStr.c_str())

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
    if (jExceptionClass != NULL) {
        env->ThrowNew(jExceptionClass, message.c_str());
        TR_ERR("Exception has been throw: %s", message.c_str())
    }
    else {
        TR_ERR("ERROR: Couldn't throw exception.")
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

void ThrowNullValueException(JNIEnv* env, Table* table, size_t col_ndx) {
    std::ostringstream ss;
    ss << "Trying to set a non-nullable field '"
       << table->get_column_name(col_ndx)
       << "' in '"
       << table->get_name()
       << "' to null.";
    ThrowException(env, IllegalArgument, ss.str());
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

bool GetBinaryData(JNIEnv* env, jobject jByteBuffer, realm::BinaryData& bin)
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
    static jchar to_int_type(jchar c)  REALM_NOEXCEPT { return c; }
    static jchar to_char_type(jchar i) REALM_NOEXCEPT { return i; }
};

struct JStringCharsAccessor {
    JStringCharsAccessor(JNIEnv* e, jstring s):
        m_env(e), m_string(s), m_data(e->GetStringChars(s,0)), m_size(get_size(e,s)) {}
    ~JStringCharsAccessor()
    {
        m_env->ReleaseStringChars(m_string, m_data);
    }
    const jchar* data() const REALM_NOEXCEPT { return m_data; }
    size_t size() const REALM_NOEXCEPT { return m_size; }

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

string string_to_hex(const string& message, StringData& str, const char* in_begin, const char* in_end,
                     jchar* out_curr, jchar* out_end, size_t retcode, size_t error_code) {
    ostringstream ret;

    const char *s = str.data();
    ret << message << " ";
    ret << "error_code = " << error_code << "; ";
    ret << "retcode = " << retcode << "; ";
    ret << "StringData.size = " << str.size() << "; ";
    ret << "StringData.data = " << str.data() << "; ";
    ret << "StringData as hex = ";
    for (string::size_type i = 0; i < str.size(); ++i)
        ret << " 0x" << std::hex << std::setfill('0') << std::setw(2) << (int)s[i];
    ret << "; ";
    ret << "in_begin = " << in_begin << "; ";
    ret << "in_end = " << in_end << "; ";
    ret << "out_curr = " << out_curr << "; ";
    ret << "out_end = " << out_end << ";";
    return ret.str();
}

string string_to_hex(const string& message, const jchar *str, size_t size, size_t error_code) {
    ostringstream ret;

    ret << message << "; ";
    ret << "error_code = " << error_code << "; ";
    for (size_t i = 0; i < size; ++i)
        ret << " 0x" << std::hex << std::setfill('0') << std::setw(4) << (int)str[i];
    return ret.str();
}

string concat_stringdata(const char *message, StringData strData)
{
    return std::string(message) + (strData != NULL ? strData.data() : "");
}

jstring to_jstring(JNIEnv* env, StringData str)
{
    if (str.is_null()) {
        return NULL;
    }
    
    // For efficiency, if the incoming UTF-8 string is sufficiently
    // small, we will attempt to store the UTF-16 output into a stack
    // allocated buffer of static size. Otherwise we will have to
    // dynamically allocate the output buffer after calculating its
    // size.

    const size_t stack_buf_size = 48;
    jchar stack_buf[stack_buf_size];
    std::unique_ptr<jchar[]> dyn_buf;

    const char* in_begin = str.data();
    const char* in_end   = str.data() + str.size();
    jchar* out_begin = stack_buf;
    jchar* out_curr  = stack_buf;
    jchar* out_end   = stack_buf + stack_buf_size;

    typedef Utf8x16<jchar, JcharTraits> Xcode;

    if (str.size() <= stack_buf_size) {
        size_t retcode = Xcode::to_utf16(in_begin, in_end, out_curr, out_end);
        if (retcode != 0)
            throw runtime_error(string_to_hex("Failure when converting short string to UTF-16",  str, in_begin, in_end, out_curr, out_end, size_t(0), retcode));
        if (in_begin == in_end)
            goto transcode_complete;
    }

    {
        const char* in_begin2 = in_begin;
        size_t error_code;
        size_t size = Xcode::find_utf16_buf_size(in_begin2, in_end, error_code);
        if (in_begin2 != in_end) 
            throw runtime_error(string_to_hex("Failure when computing UTF-16 size", str, in_begin, in_end, out_curr, out_end, size, error_code));
        if (int_add_with_overflow_detect(size, stack_buf_size))
            throw runtime_error("String size overflow");
        dyn_buf.reset(new jchar[size]);
        out_curr = copy(out_begin, out_curr, dyn_buf.get());
        out_begin = dyn_buf.get();
        out_end   = dyn_buf.get() + size;
        size_t retcode = Xcode::to_utf16(in_begin, in_end, out_curr, out_end);
        if (retcode != 0) 
            throw runtime_error(string_to_hex("Failure when converting long string to UTF-16", str, in_begin, in_end, out_curr, out_end, size_t(0), retcode));
        REALM_ASSERT(in_begin == in_end);
    }

  transcode_complete:
    {
        jsize out_size;
        if (int_cast_with_overflow_detect(out_curr - out_begin, out_size))
            throw runtime_error("String size overflow");

        return env->NewString(out_begin, out_size);
    }
}


JStringAccessor::JStringAccessor(JNIEnv* env, jstring str)
{
    // For efficiency, if the incoming UTF-16 string is sufficiently
    // small, we will choose an UTF-8 output buffer whose size (in
    // bytes) is simply 4 times the number of 16-bit elements in the
    // input. This is guaranteed to be enough. However, to avoid
    // excessive over allocation, this is not done for larger input
    // strings.

    if (str == NULL) {
        m_is_null = true;
        return;
    }
    m_is_null = false;

    JStringCharsAccessor chars(env, str);

    typedef Utf8x16<jchar, JcharTraits> Xcode;
    size_t max_project_size = 48;
    REALM_ASSERT(max_project_size <= numeric_limits<size_t>::max()/4);
    size_t buf_size;
    if (chars.size() <= max_project_size) {
        buf_size = chars.size() * 4;
    }
    else {
        const jchar* begin = chars.data();
        const jchar* end   = begin + chars.size();
        size_t error_code;
        buf_size = Xcode::find_utf8_buf_size(begin, end, error_code);
    }
    m_data.reset(new char[buf_size]);  // throws
    {
        const jchar* in_begin = chars.data();
        const jchar* in_end   = in_begin + chars.size();
        char* out_begin = m_data.get();
        char* out_end   = m_data.get() + buf_size;
        size_t error_code;
        if (!Xcode::to_utf8(in_begin, in_end, out_begin, out_end, error_code)) {
            throw runtime_error(string_to_hex("Failure when converting to UTF-8", chars.data(), chars.size(), error_code));
        }
        if (in_begin != in_end) {
            throw runtime_error(string_to_hex("in_begin != in_end when converting to UTF-8", chars.data(), chars.size(), error_code));
        }
        m_size = out_begin - m_data.get();
    }
}
