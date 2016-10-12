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
#include <realm/unicode.hpp>
#include "utf8.hpp"

#include "util.hpp"
#include "io_realm_internal_Util.h"
#include "io_realm_internal_SharedRealm.h"
#include "shared_realm.hpp"

using namespace std;
using namespace realm;
using namespace realm::util;

// Caching classes and constructors for boxed types.
JavaVM* g_vm;
jclass java_lang_long;
jmethodID java_lang_long_init;
jclass java_lang_float;
jmethodID java_lang_float_init;
jclass java_lang_double;
jmethodID java_lang_double_init;
jclass session_class_ref;
jmethodID session_error_handler;

void ThrowRealmFileException(JNIEnv* env, const std::string& message, realm::RealmFileException::Kind kind);

jthrowable JThrowableNew(JNIEnv* env, const char* class_name, const char* message) {
    jclass j_exception_class = env->FindClass(class_name);
    if (j_exception_class == nullptr) {
        return nullptr;
    }
    jmethodID constructor = env->GetMethodID(j_exception_class, "<init>", "(Ljava/lang/String;)V");
    if (constructor == nullptr) {
        env->DeleteLocalRef(j_exception_class);
        return nullptr;
    }
    jstring j_str = env->NewStringUTF(message);
    jobject exception = env->NewObject(j_exception_class, constructor, j_str);
    env->DeleteLocalRef(j_exception_class);
    return reinterpret_cast<jthrowable>(exception);
}

jthrowable JThrowableNew(JNIEnv* env, const char* class_name, const char* message, jbyte kind) {
    jclass j_exception_class = env->FindClass(class_name);
    if (j_exception_class == nullptr) {
        return nullptr;
    }
    jmethodID constructor = env->GetMethodID(j_exception_class, "<init>", "(BLjava/lang/String;)V");
    if (constructor == nullptr) {
        env->DeleteLocalRef(j_exception_class);
        return nullptr;
    }
    jstring j_str = env->NewStringUTF(message);
    jobject exception = env->NewObject(j_exception_class, constructor, kind, j_str);
    env->DeleteLocalRef(j_exception_class);
    return reinterpret_cast<jthrowable>(exception);
}

void ConvertException(JNIEnv* env) {
    const char out_of_memory_error[]             = "io/realm/internal/OutOfMemoryError";
    const char illegal_state_exception[]         = "java/lang/IllegalStateException";
    const char illegal_argument_exception[]      = "java/lang/IllegalArgumentException";
    const char realm_error[]                     = "io/realm/exceptions/RealmError";
    const char bad_version[]                     = "io/realm/internal/async/BadVersionException";
    const char realm_file_exception[]            = "io/realm/exceptions/RealmFileException";
    const char index_out_of_bound_exception[]    = "java/lang/ArrayIndexOutOfBoundsException";
    const char unsupported_operation_exception[] = "java/lang/UnsupportedOperationException";
    const char class_not_found_exception[]       = "java/lang/ClassNotFoundException";
    const char runtime_exception[]               = "java/lang/RuntimeException";

    jthrowable j_exception;

    // we are going to "throw" a Java exception so it is safest to clear
    // already occurred exceptions
    env->ExceptionClear();

    try {
        throw;
    }
    catch (bad_alloc& e) {
        j_exception = JThrowableNew(env, out_of_memory_error, e.what());
    }
    catch (CrossTableLinkTarget& e) {
        j_exception = JThrowableNew(env, illegal_state_exception, e.what());
    }
    catch (SharedGroup::BadVersion& e) {
        j_exception = JThrowableNew(env, bad_version, ""); // never comes with an error message
    }
    catch (invalid_argument& e) {
        ostringstream ss;
        ss << "Illegal Argument: " << e.what();
        j_exception = JThrowableNew(env, illegal_argument_exception, ss.str().c_str());
    }
    catch (JavaIllegalState& e) {
        j_exception = JThrowableNew(env, illegal_state_exception, e.what());
    }
    catch (InvalidTransactionException& e) {
        j_exception = JThrowableNew(env, illegal_state_exception, e.what());
    }
    catch (range_error& e) {
        j_exception = JThrowableNew(env, index_out_of_bound_exception, e.what());
    }
    catch (InvalidEncryptionKeyException& e) {
        j_exception = JThrowableNew(env, illegal_argument_exception, e.what());
    }
    catch (JavaUnsupportedOperation& e) {
        j_exception = JThrowableNew(env, unsupported_operation_exception, e.what());
    }
    catch (JavaFatalError& e) {
        ostringstream ss;
        ss << "Unrecoverable error. " << e.what();
        j_exception = JThrowableNew(env, realm_error, ss.str().c_str());
    }
    catch (JavaClassNotFound& e) {
        ostringstream ss;
        ss << "Class '" << e.what() << "' could not be located.";
        j_exception = JThrowableNew(env, class_not_found_exception, ss.str().c_str());
    }
    catch (RealmFileException& e) {
        // File exceptions are handled a bit different - we wish to send back information about
        // what the actual cause was. For that, we have the exception
        // io.realm.exceptions.RealmFileException which constructor takes two parameters:
        //   - kind of exception (kind_code)
        //   - message (jstr)

        jbyte kind_code;
        switch (e.kind()) {
            case realm::RealmFileException::Kind::AccessError:
                kind_code = io_realm_internal_SharedRealm_FILE_EXCEPTION_KIND_ACCESS_ERROR;
                break;
            case realm::RealmFileException::Kind::PermissionDenied:
                kind_code = io_realm_internal_SharedRealm_FILE_EXCEPTION_KIND_PERMISSION_DENIED;
                break;
            case realm::RealmFileException::Kind::Exists:
                kind_code = io_realm_internal_SharedRealm_FILE_EXCEPTION_KIND_EXISTS;
                break;
            case realm::RealmFileException::Kind::NotFound:
                kind_code = io_realm_internal_SharedRealm_FILE_EXCEPTION_KIND_NOT_FOUND;
                break;
            case realm::RealmFileException::Kind::IncompatibleLockFile:
                kind_code = io_realm_internal_SharedRealm_FILE_EXCEPTION_KIND_IMCOMPATIBLE_LOCK_FILE;
                break;
            case realm::RealmFileException::Kind::FormatUpgradeRequired:
                kind_code = io_realm_internal_SharedRealm_FILE_EXCEPTION_KIND_FORMAT_UPGRADE_REQUIRED;
                break;
        }
        j_exception = JThrowableNew(env, realm_file_exception, e.what(), kind_code);
    }
    catch (JavaNullValue& e) {
        j_exception = JThrowableNew(env, illegal_argument_exception, e.what());
    }
    catch (runtime_error& e) {
        j_exception = JThrowableNew(env, runtime_exception, e.what());
    }
    catch (exception& e) {
        std::ostringstream ss;
        ss << "Unrecoverable error: " << e.what();
        j_exception = JThrowableNew(env, realm_error, ss.str().c_str());
    }

    /* Catch (...) is not needed if we only throw exceptions derived from std::exception */

    if (j_exception != nullptr) {
        env->Throw(j_exception);
        TR_ERR_NO_VA_ARG(env, "Exception has been throw")
    }
    else {
        TR_ERR_NO_VA_ARG(env, "ERROR: Couldn't throw exception.")
        throw runtime_error("Couldn't throw Java exception.");
    }
    env->DeleteLocalRef(j_exception);
}

jclass GetClass(JNIEnv* env, const char* classStr)
{
    jclass localRefClass = env->FindClass(classStr);
    if (localRefClass == NULL) {
        throw JavaClassNotFound(classStr);
    }

    jclass myClass = reinterpret_cast<jclass>(env->NewGlobalRef(localRefClass));
    env->DeleteLocalRef(localRefClass);
    return myClass;
}

//*********************************************************************
// String handling
//*********************************************************************

namespace {

// This assumes that 'jchar' is an integral type with at least 16
// non-sign value bits, that is, an unsigned 16-bit integer, or any
// signed or unsigned integer with more than 16 bits.
struct JcharTraits {
    static jchar to_int_type(jchar c)  noexcept { return c; }
    static jchar to_char_type(jchar i) noexcept { return i; }
};

struct JStringCharsAccessor {
    JStringCharsAccessor(JNIEnv* e, jstring s):
        m_env(e), m_string(s), m_data(e->GetStringChars(s,0)), m_size(get_size(e,s)) {}
    ~JStringCharsAccessor()
    {
        m_env->ReleaseStringChars(m_string, m_data);
    }
    const jchar* data() const noexcept { return m_data; }
    size_t size() const noexcept { return m_size; }

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

static string string_to_hex(const string& message, StringData& str, const char* in_begin, const char* in_end,
                     jchar* out_curr, jchar* out_end, size_t retcode, size_t error_code) {
    ostringstream ret;

    const char *s = str.data();
    ret << message << " ";
    ret << "error_code = " << error_code << "; ";
    ret << "retcode = " << retcode << "; ";
    ret << "StringData.size = " << str.size() << "; ";
    ret << "StringData.data = " << str << "; ";
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

static string string_to_hex(const string& message, const jchar *str, size_t size, size_t error_code) {
    ostringstream ret;

    ret << message << "; ";
    ret << "error_code = " << error_code << "; ";
    for (size_t i = 0; i < size; ++i)
        ret << " 0x" << std::hex << std::setfill('0') << std::setw(4) << (int)str[i];
    return ret.str();
}

string concat_stringdata(const char *message, StringData strData)
{
    if (strData.is_null()) {
        return std::string(message);
    }
    return std::string(message) + std::string(strData.data(), strData.size());
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
            throw invalid_argument(string_to_hex("Failure when converting to UTF-8", chars.data(), chars.size(), error_code));
        }
        if (in_begin != in_end) {
            throw invalid_argument(string_to_hex("in_begin != in_end when converting to UTF-8", chars.data(), chars.size(), error_code));
        }
        m_size = out_begin - m_data.get();
    }
}

