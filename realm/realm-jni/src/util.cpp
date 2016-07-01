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
jclass sync_manager;
jmethodID sync_manager_notify_handler;

void ConvertException(JNIEnv* env, const char *file, int line)
{
    ostringstream ss;
    try {
        throw;
    }
    catch (bad_alloc& e) {
        ss << e.what() << " in " << file << " line " << line;
        ThrowException(env, OutOfMemory, ss.str());
    }
    catch (CrossTableLinkTarget& e) {
        ss << e.what() << " in " << file << " line " << line;
        ThrowException(env, CrossTableLink, ss.str());
    }
    catch (SharedGroup::BadVersion& e) {
        ss << e.what() << " in " << file << " line " << line;
        ThrowException(env, BadVersion, ss.str());
    }
    catch (invalid_argument& e) {
        ss << e.what() << " in " << file << " line " << line;
        ThrowException(env, IllegalArgument, ss.str());
    }
    catch (File::AccessError& e) {
        ss << e.what() << " path: " << e.get_path() << " in " << file << " line " << line;
        ThrowException(env, IllegalArgument, ss.str());
    }
    catch (exception& e) {
        ss << e.what() << " in " << file << " line " << line;
        ThrowException(env, FatalError, ss.str());
    }
    /* catch (...) is not needed if we only throw exceptions derived from std::exception */
}

void ThrowException(JNIEnv* env, ExceptionKind exception, const char *classStr)
{
    ThrowException(env, exception, classStr, "");
}

void ThrowException(JNIEnv* env, ExceptionKind exception, const std::string& classStr, const std::string& itemStr)
{
    string message;
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

        case FatalError:
            jExceptionClass = env->FindClass("io/realm/exceptions/RealmError");
            message = "Unrecoverable error. " + classStr;
            break;

        case RuntimeError:
            jExceptionClass = env->FindClass("java/lang/RuntimeException");
            message = classStr;
            break;

        case RowInvalid:
            jExceptionClass = env->FindClass("java/lang/IllegalStateException");
            message = "Illegal State: " + classStr;
            break;

        case CrossTableLink:
            jExceptionClass = env->FindClass("java/lang/IllegalStateException");
            message = "This class is referenced by other classes. Remove those fields first before removing this class.";
            break;

        case BadVersion:
            jExceptionClass = env->FindClass("io/realm/internal/async/BadVersionException");
            message = classStr;
            break;

        case LockFileError:
            jExceptionClass = env->FindClass("io/realm/exceptions/IncompatibleLockFileException");
            message = classStr;
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

void ThrowNullValueException(JNIEnv* env, Table* table, size_t col_ndx) {
    std::ostringstream ss;
    ss << "Trying to set a non-nullable field '"
       << table->get_column_name(col_ndx)
       << "' in '"
       << table->get_name()
       << "' to null.";
    ThrowException(env, IllegalArgument, ss.str());
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

// The string_compare_callback_func is a duplication of the code found in core.
// But the collation_order is different since we need to use the
// pre-1.1.2 sorting order.
bool string_compare_callback_func(const char* string1, const char* string2)
{
    static const uint32_t collation_order[] = { 0, 2, 3, 4, 5, 6, 7,
        8, 9, 33, 34, 35, 36, 37, 10, 11, 12, 13, 14, 15, 16, 17, 18,
        19, 20, 21, 22, 23, 24, 25, 26, 27, 31, 38, 39, 40, 41, 42,
        43, 29, 44, 45, 46, 76, 47, 30, 48, 49, 128, 132, 134, 137,
        139, 140, 143, 144, 145, 146, 50, 51, 77, 78, 79, 52, 53, 148,
        182, 191, 208, 229, 263, 267, 285, 295, 325, 333, 341, 360,
        363, 385, 429, 433, 439, 454, 473, 491, 527, 531, 537, 539,
        557, 54, 55, 56, 57, 58, 59, 147, 181, 190, 207 , 228, 262,
        266, 284, 294, 324, 332, 340, 359, 362, 384, 428, 432, 438,
        453, 472, 490, 526, 530, 536, 538, 556, 60, 61, 62, 63, 28,
        96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108,
        109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120,
        121, 122, 123, 124, 125, 126, 127, 32, 64, 72, 73, 74, 75, 65,
        88, 66, 89, 149, 81, 90, 1, 91, 67, 92, 80, 136, 138, 68, 93,
        94, 95, 69, 133, 386, 82, 129, 130, 131, 70, 153, 151, 157,
        165, 575, 588, 570, 201, 233 , 231, 237, 239, 300, 298, 303,
        305, 217, 371, 390, 388, 394, 402, 584, 83, 582, 495, 493,
        497, 555, 541, 487, 470, 152, 150, 156, 164, 574, 587, 569,
        200, 232, 230, 236, 238, 299, 297, 302, 304, 216, 370, 389,
        387, 393, 401, 583, 84, 581, 494, 492, 496, 554, 540, 486,
        544, 163, 162, 161, 160, 167, 166, 193, 192, 197, 196, 195,
        194, 199, 198, 210, 209, 212, 211, 245, 244, 243, 242, 235,
        234, 247, 246, 241, 240, 273, 272, 277, 276, 271, 270, 279,
        278, 287, 286, 291, 290, 313, 312, 311, 310, 309 , 308, 315,
        314, 301, 296, 323, 322, 328, 327, 337, 336, 434, 343, 342,
        349, 348, 347, 346, 345, 344, 353, 352, 365, 364, 373, 372,
        369, 368, 375, 383, 382, 400, 399, 398, 397, 586, 585, 425,
        424, 442, 441, 446, 445, 444, 443, 456, 455, 458, 457, 462,
        461, 460, 459, 477, 476, 475, 474, 489, 488, 505, 504, 503,
        502, 501, 500, 507, 506, 549, 548, 509, 508, 533, 532, 543,
        542, 545, 559, 558, 561, 560, 563, 562, 471, 183, 185, 187,
        186, 189, 188, 206, 205, 204, 226, 215, 214, 213, 218, 257,
        258, 259 , 265, 264, 282, 283, 292, 321, 316, 339, 338, 350,
        354, 361, 374, 376, 405, 421, 420, 423, 422, 431, 430, 440,
        468, 467, 466, 469, 480, 479, 478, 481, 524, 523, 525, 528,
        553, 552, 565, 564, 571, 579, 578, 580, 135, 142, 141, 589,
        534, 85, 86, 87, 71, 225, 224, 223, 357, 356, 355, 380, 379,
        378, 159, 158, 307, 306, 396, 395, 499, 498, 518, 517, 512,
        511, 516, 515, 514, 513, 256, 174, 173, 170, 169, 573, 572,
        281, 280, 275, 274, 335, 334, 404, 403, 415, 414, 577, 576,
        329, 222, 221, 220, 269 , 268, 293, 535, 367, 366, 172, 171,
        180, 179, 411, 410, 176, 175, 178, 177, 253, 252, 255, 254,
        318, 317, 320, 319, 417, 416, 419, 418, 450, 449, 452, 451,
        520, 519, 522, 521, 464, 463, 483, 482, 261, 260, 289, 288,
        377, 227, 427, 426, 567, 566, 155, 154, 249, 248, 409, 408,
        413, 412, 392, 391, 407, 406, 547, 546, 358, 381, 485, 326,
        219, 437, 168, 203, 202, 351, 484, 465, 568, 591, 590, 184,
        510, 529, 251, 250, 331, 330, 436, 435, 448, 447, 551, 550 };

    uint32_t char1;
    uint32_t char2;
    const char* s1 = string1;
    const char* s2 = string2;
    
    do {
        size_t remaining1 = strlen(string1) - (s1 - string1);
        size_t remaining2 = strlen(string2) - (s2 - string2);

        if ((remaining1 == 0) != (remaining2 == 0)) {
            // exactly one of the strings have ended (not both or none; xor)
            return (remaining1 == 0);
        }
        else if (remaining2 == 0 && remaining1 == 0) {
            // strings are identical
            return false;
        }

        // invalid utf8
        if (remaining1 < sequence_length(s1[0]) || remaining2 < sequence_length(s2[0]))
            return false;

        char1 = utf8value(s1);
        char2 = utf8value(s2);

        if (char1 == char2) {
            // Go to next characters for both strings
            s1 += sequence_length(s1[0]);
            s2 += sequence_length(s2[0]);
        }
        else {
            // Test if above Latin Extended B
            size_t collators = sizeof(collation_order) / sizeof(collation_order[0]);
            if (char1 >= collators || char2 >= collators)
                return char1 < char2;

            uint32_t value1 = collation_order[char1];
            uint32_t value2 = collation_order[char2];

            return value1 < value2;
        }

    } while (true);
}
