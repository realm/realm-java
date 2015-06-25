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

#ifndef REALM_JAVA_UTIL_HPP
#define REALM_JAVA_UTIL_HPP

#include <string>
#include <sstream>
#include <memory>

#include <jni.h>

// Used by logging
#define __STDC_FORMAT_MACROS
#include <inttypes.h>

#include <realm.hpp>
#include <realm/util/meta.hpp>
#include <realm/util/safe_int_ops.hpp>
#include <realm/lang_bind_helper.hpp>

#include "io_realm_internal_Util.h"


#define TRACE               1       // disable for performance
#define CHECK_PARAMETERS    1       // Check all parameters in API and throw exceptions in java if invalid

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved);

#ifdef __cplusplus
}
#endif

// Use this macro when logging a pointer using '%p'
#define VOID_PTR(ptr) reinterpret_cast<void*>(ptr)

#define STRINGIZE_DETAIL(x) #x
#define STRINGIZE(x) STRINGIZE_DETAIL(x)

// Exception handling

#define CATCH_FILE(fileName) \
    catch (InvalidDatabase&) { \
        ThrowException(env, IllegalArgument, "Invalid format of Realm file."); \
    } \
    catch (util::File::PermissionDenied& e) { \
        ThrowException(env, IOFailed, string(fileName), string("Permission denied. ") + e.what()); \
    } \
    catch (util::File::NotFound&) { \
        ThrowException(env, FileNotFound, string(fileName).data());    \
    } \
    catch (util::File::AccessError& e) { \
        ThrowException(env, FileAccessError, string(fileName), e.what()); \
    }

#define CATCH_STD() \
    catch (...) { \
        ConvertException(env, __FILE__, __LINE__); \
    }

template <typename T>
std::string num_to_string(T pNumber)
{
 std::ostringstream oOStrStream;
 oOStrStream << pNumber;
 return oOStrStream.str();
}


#define MAX_JLONG  0x7FFFFFFFFFFFFFFFLL
#define MIN_JLONG -0x8000000000000000LL
#define MAX_JINT   0x7FFFFFFFL
#define MAX_JSIZE  MAX_JINT

// Helper macros for better readability
// Use S64() when logging
#define S(x)    static_cast<size_t>(x)
#define B(x)    static_cast<bool>(x)
#define S64(x)  static_cast<int64_t>(x)
#define TBL(x)  reinterpret_cast<realm::Table*>(x)
#define TV(x)   reinterpret_cast<realm::TableView*>(x)
#define LV(x)   reinterpret_cast<realm::LinkView*>(x)
#define Q(x)    reinterpret_cast<realm::Query*>(x)
#define G(x)    reinterpret_cast<realm::Group*>(x)
#define ROW(x)  reinterpret_cast<realm::Row*>(x)
#define SG(ptr) reinterpret_cast<SharedGroup*>(ptr)
#define CH(ptr) reinterpret_cast<realm::ClientHistory*>(ptr)

// Exception handling

enum ExceptionKind {
    ClassNotFound = 0,
    NoSuchField = 1,
    NoSuchMethod = 2,

    IllegalArgument = 3,
    IOFailed = 4,
    FileNotFound = 5,
    FileAccessError = 6,
    IndexOutOfBounds = 7,
    TableInvalid = 8,
    UnsupportedOperation = 9,
    OutOfMemory = 10,
    Unspecified = 11,
    RuntimeError = 12,
    RowInvalid = 13,
    UnreachableVersion = 14
};

void ConvertException(JNIEnv* env, const char *file, int line);
void ThrowException(JNIEnv* env, ExceptionKind exception, const std::string& classStr, const std::string& itemStr="");
void ThrowException(JNIEnv* env, ExceptionKind exception, const char *classStr);

jclass GetClass(JNIEnv* env, const char* classStr);


// Debug trace
extern int trace_level;
extern const char *log_tag;

#if TRACE
  #if defined(ANDROID)
    #include <android/log.h>
    #define LOG_DEBUG ANDROID_LOG_DEBUG
    #define TR_ENTER() if (trace_level >= 1) { __android_log_print(ANDROID_LOG_DEBUG, log_tag, " --> %s", __FUNCTION__); } else {}
    #define TR_ENTER_PTR(ptr) if (trace_level >= 1) { __android_log_print(ANDROID_LOG_DEBUG, log_tag, " --> %s %" PRId64, __FUNCTION__, static_cast<int64_t>(ptr)); } else {}
    #define TR(...) if (trace_level >= 2) { __android_log_print(ANDROID_LOG_DEBUG, log_tag, __VA_ARGS__); } else {}
    #define TR_ERR(...) if (trace_level >= 0) { __android_log_print(ANDROID_LOG_DEBUG, log_tag, __VA_ARGS__); } else {}
    #define TR_LEAVE() if (trace_level >= 3) { __android_log_print(ANDROID_LOG_DEBUG, log_tag, " <-- %s", __FUNCTION__); } else {}
  #else // ANDROID
    #define TR_ENTER()
    #define TR_ENTER_PTR(ptr)
    #define TR(...)
    #define TR_ERR(...)
    #define TR_LEAVE()
  #endif
#else // TRACE - these macros must be empty
  #define TR_ENTER()
  #define TR_ENTER_PTR(ptr)
  #define TR(...)
  #define TR_ERR(...)
  #define TR_LEAVE()
#endif


// Check parameters

#define TABLE_VALID(env,ptr)    TableIsValid(env, ptr)
#define ROW_VALID(env,ptr)      RowIsValid(env, ptr)

#if CHECK_PARAMETERS

#define ROW_INDEXES_VALID(env,ptr,start,end, range)             RowIndexesValid(env, ptr, start, end, range)
#define ROW_INDEX_VALID(env,ptr,row)                            RowIndexValid(env, ptr, row)
#define ROW_INDEX_VALID_OFFSET(env,ptr,row)                     RowIndexValid(env, ptr, row, true)
#define TBL_AND_ROW_INDEX_VALID(env,ptr,row)                    TblRowIndexValid(env, ptr, row)
#define TBL_AND_ROW_INDEX_VALID_OFFSET(env,ptr,row, offset)     TblRowIndexValid(env, ptr, row, offset)
#define COL_INDEX_VALID(env,ptr,col)                            ColIndexValid(env, ptr, col)
#define TBL_AND_COL_INDEX_VALID(env,ptr,col)                    TblColIndexValid(env, ptr, col)
#define COL_INDEX_AND_TYPE_VALID(env,ptr,col,type)              ColIndexAndTypeValid(env, ptr, col, type)
#define TBL_AND_COL_INDEX_AND_TYPE_VALID(env,ptr,col, type)     TblColIndexAndTypeValid(env, ptr, col, type)
#define TBL_AND_COL_INDEX_AND_LINK_OR_LINKLIST(env,ptr,col)     TblColIndexAndLinkOrLinkList(env, ptr, col)
#define INDEX_VALID(env,ptr,col,row)                            IndexValid(env, ptr, col, row)
#define TBL_AND_INDEX_VALID(env,ptr,col,row)                    TblIndexValid(env, ptr, col, row)
#define TBL_AND_INDEX_INSERT_VALID(env,ptr,col,row)             TblIndexInsertValid(env, ptr, col, row)
#define INDEX_AND_TYPE_VALID(env,ptr,col,row,type)              IndexAndTypeValid(env, ptr, col, row, type, false)
#define TBL_AND_INDEX_AND_TYPE_VALID(env,ptr,col,row,type)      TblIndexAndTypeValid(env, ptr, col, row, type, false)
#define INDEX_AND_TYPE_VALID_MIXED(env,ptr,col,row,type)        IndexAndTypeValid(env, ptr, col, row, type, true)
#define TBL_AND_INDEX_AND_TYPE_VALID_MIXED(env,ptr,col,row,type) TblIndexAndTypeValid(env, ptr, col, row, type, true)
#define TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env,ptr,col,row,type) TblIndexAndTypeInsertValid(env, ptr, col, row, type)

#define ROW_AND_COL_INDEX_AND_TYPE_VALID(env,ptr,col, type)     RowColIndexAndTypeValid(env, ptr, col, type)
#define ROW_AND_COL_INDEX_VALID(env,ptr,col)                    RowColIndexValid(env, ptr, col)

#else

#define ROW_INDEXES_VALID(env,ptr,start,end, range)             (true)
#define ROW_INDEX_VALID(env,ptr,row)                            (true)
#define ROW_INDEX_VALID_OFFSET(env,ptr,row)                     (true)
#define TBL_AND_ROW_INDEX_VALID(env,ptr,row)                    (true)
#define TBL_AND_ROW_INDEX_VALID_OFFSET(env,ptr,row, offset)     (true)
#define COL_INDEX_VALID(env,ptr,col)                            (true)
#define TBL_AND_COL_INDEX_VALID(env,ptr,col)                    (true)
#define COL_INDEX_AND_TYPE_VALID(env,ptr,col,type)              (true)
#define TBL_AND_COL_INDEX_AND_TYPE_VALID(env,ptr,col, type)     (true)
#define TBL_AND_COL_INDEX_AND_LINK_OR_LINKLIST(env,ptr,col)     (true)
#define INDEX_VALID(env,ptr,col,row)                            (true)
#define TBL_AND_INDEX_VALID(env,ptr,col,row)                    (true)
#define TBL_AND_INDEX_INSERT_VALID(env,ptr,col,row)             (true)
#define INDEX_AND_TYPE_VALID(env,ptr,col,row,type)              (true)
#define TBL_AND_INDEX_AND_TYPE_VALID(env,ptr,col,row,type)      (true)
#define INDEX_AND_TYPE_VALID_MIXED(env,ptr,col,row,type)        (true)
#define TBL_AND_INDEX_AND_TYPE_VALID_MIXED(env,ptr,col,row,type) (true)
#define TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env,ptr,col,row,type) (true)

#define ROW_AND_COL_INDEX_AND_TYPE_VALID(env,ptr,col, type)     (true)
#define ROW_AND_COL_INDEX_VALID(env,ptr,col)                    (true)

#endif


inline jlong to_jlong_or_not_found(size_t res) {
    return (res == realm::not_found) ? jlong(-1) : jlong(res);
}

template <class T>
inline bool TableIsValid(JNIEnv* env, T* objPtr)
{
    bool valid = (objPtr != NULL);
    if (valid) {
        // Check if Table is valid
        if (std::is_same<realm::Table, T>::value) {
            valid = TBL(objPtr)->is_attached();
        }
        // TODO: Add check for TableView

    }
    if (!valid) {
        TR_ERR("Table %p is no longer attached!", VOID_PTR(objPtr))
        ThrowException(env, TableInvalid, "Table is no longer valid to operate on.");
    }
    return valid;
}

inline bool RowIsValid(JNIEnv* env, Row* rowPtr)
{
    bool valid = (rowPtr != NULL && rowPtr->is_attached());
    if (!valid) {
        TR_ERR("Row %p is no longer attached!", VOID_PTR(rowPtr))
        ThrowException(env, RowInvalid, "Row/Object is no longer valid to operate on. Was it deleted?");
    }
    return valid;
}

// Requires an attached Table
template <class T>
bool RowIndexesValid(JNIEnv* env, T* pTable, jlong startIndex, jlong endIndex, jlong range)
{
    size_t maxIndex = pTable->size();
    if (endIndex == -1)
        endIndex = maxIndex;
    if (startIndex < 0) {
        TR_ERR("startIndex %" PRId64 " < 0 - invalid!", S64(startIndex))
        ThrowException(env, IndexOutOfBounds, "startIndex < 0.");
        return false;
    }
    if (realm::util::int_greater_than(startIndex, maxIndex)) {
        TR_ERR("startIndex %" PRId64 " > %" PRId64 " - invalid!", S64(startIndex), S64(maxIndex))
        ThrowException(env, IndexOutOfBounds, "startIndex > available rows.");
        return false;
    }

    if (realm::util::int_greater_than(endIndex, maxIndex)) {
        TR_ERR("endIndex %" PRId64 " > %" PRId64 " - invalid!", S64(endIndex), S64(maxIndex))
        ThrowException(env, IndexOutOfBounds, "endIndex > available rows.");
        return false;
    }
    if (startIndex > endIndex) {
        TR_ERR("startIndex %" PRId64 " > endIndex %" PRId64 " - invalid!", S64(startIndex), S64(endIndex))
        ThrowException(env, IndexOutOfBounds, "startIndex > endIndex.");
        return false;
    }

    if (range != -1 && range < 0) {
        TR_ERR("range %" PRId64 " < 0 - invalid!", S64(range))
        ThrowException(env, IndexOutOfBounds, "range < 0.");
        return false;
    }

    return true;
}

template <class T>
inline bool RowIndexValid(JNIEnv* env, T* pTable, jlong rowIndex, bool offset=false)
{
    if (rowIndex < 0) {
        ThrowException(env, IndexOutOfBounds, "rowIndex is less than 0.");
        return false;
    }
    size_t size = pTable->size();
    if (size > 0 && offset)
        size -= 1;
    bool rowErr = realm::util::int_greater_than_or_equal(rowIndex, size);
    if (rowErr) {
        TR_ERR("rowIndex %" PRId64 " > %" PRId64 " - invalid!", S64(rowIndex), S64(size))
        ThrowException(env, IndexOutOfBounds,
            "rowIndex > available rows: " +
            num_to_string(rowIndex) + " > " + num_to_string(size));
    }
    return !rowErr;
}

template <class T>
inline bool TblRowIndexValid(JNIEnv* env, T* pTable, jlong rowIndex, bool offset=false)
{
    if (std::is_same<realm::Table, T>::value) {
        if (!TableIsValid(env, TBL(pTable)))
            return false;
    }
    return RowIndexValid(env, pTable, rowIndex, offset);
}

template <class T>
inline bool ColIndexValid(JNIEnv* env, T* pTable, jlong columnIndex)
{
    if (columnIndex < 0) {
        ThrowException(env, IndexOutOfBounds, "columnIndex is less than 0.");
        return false;
    }
    bool colErr = realm::util::int_greater_than_or_equal(columnIndex, pTable->get_column_count());
    if (colErr) {
        TR_ERR("columnIndex %" PRId64 " > %" PRId64 " - invalid!", S64(columnIndex), S64(pTable->get_column_count()))
        ThrowException(env, IndexOutOfBounds, "columnIndex > available columns.");
    }
    return !colErr;
}

template <class T>
inline bool TblColIndexValid(JNIEnv* env, T* pTable, jlong columnIndex)
{
    if (std::is_same<realm::Table, T>::value) {
        if (!TableIsValid(env, TBL(pTable)))
            return false;
    }
    return ColIndexValid(env, pTable, columnIndex);
}

inline bool RowColIndexValid(JNIEnv* env, Row* pRow, jlong columnIndex)
{
    return RowIsValid(env, pRow) && ColIndexValid(env, pRow->get_table(), columnIndex);
}

template <class T>
inline bool IndexValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex)
{
    return ColIndexValid(env, pTable, columnIndex)
        && RowIndexValid(env, pTable, rowIndex);
}

template <class T>
inline bool TblIndexValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex)
{
    return TableIsValid(env, pTable)
        && IndexValid(env, pTable, columnIndex, rowIndex);
}

template <class T>
inline bool TblIndexInsertValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex)
{
    if (!TblColIndexValid(env, pTable, columnIndex))
        return false;
    bool rowErr = realm::util::int_greater_than(rowIndex, pTable->size()+1);
    if (rowErr) {
        TR_ERR("rowIndex %" PRId64 " > %" PRId64 " - invalid!", S64(rowIndex), S64(pTable->size()))
        ThrowException(env, IndexOutOfBounds,
            "rowIndex " + num_to_string(rowIndex) +
            " > available rows " + num_to_string(pTable->size()) + ".");
    }
    return !rowErr;
}

template <class T>
inline bool TypeValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex, int expectColType, bool allowMixed)
{
    size_t col = static_cast<size_t>(columnIndex);
    int colType = pTable->get_column_type(col);
    if (allowMixed) {
        if (colType == realm::type_Mixed) {
            size_t row = static_cast<size_t>(rowIndex);
            colType = pTable->get_mixed_type(col, row);
        }
    }
    if (colType != expectColType) {
        TR_ERR("Expected columnType %d, but got %d.", expectColType, pTable->get_column_type(col))
        ThrowException(env, IllegalArgument, "ColumnType invalid.");
        return false;
    }
    return true;
}

template <class T>
inline bool TypeIsLinkLike(JNIEnv* env, T* pTable, jlong columnIndex)
{
    size_t col = static_cast<size_t>(columnIndex);
    int colType = pTable->get_column_type(col);
    if (colType == type_Link || colType == type_LinkList) {
        return true;
    }

    TR_ERR("Expected columnType %d or %d, but got %d", type_Link, type_LinkList, colType)
    ThrowException(env, IllegalArgument, "ColumnType invalid: expected type_Link or type_LinkList");
    return false;
}

template <class T>
inline bool ColIndexAndTypeValid(JNIEnv* env, T* pTable, jlong columnIndex, int expectColType)
{
    return ColIndexValid(env, pTable, columnIndex)
        && TypeValid(env, pTable, columnIndex, 0, expectColType, false);
}
template <class T>
inline bool TblColIndexAndTypeValid(JNIEnv* env, T* pTable, jlong columnIndex, int expectColType)
{
    return TableIsValid(env, pTable)
        && ColIndexAndTypeValid(env, pTable, columnIndex, expectColType);
}

template <class T>
inline bool TblColIndexAndLinkOrLinkList(JNIEnv* env, T* pTable, jlong columnIndex) {
    return TableIsValid(env, pTable)
        && TypeIsLinkLike(env, pTable, columnIndex);
}

inline bool RowColIndexAndTypeValid(JNIEnv* env, Row* pRow, jlong columnIndex, int expectColType)
{
    return RowIsValid(env, pRow)
        && ColIndexAndTypeValid(env, pRow->get_table(), columnIndex, expectColType);
}

template <class T>
inline bool IndexAndTypeValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex, int expectColType, bool allowMixed)
{
    return IndexValid(env, pTable, columnIndex, rowIndex)
        && TypeValid(env, pTable, columnIndex, rowIndex, expectColType, allowMixed);
}
template <class T>
inline bool TblIndexAndTypeValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex, int expectColType, bool allowMixed)
{
    return TableIsValid(env, pTable) && IndexAndTypeValid(env, pTable, columnIndex, rowIndex, expectColType, allowMixed);
}

template <class T>
inline bool TblIndexAndTypeInsertValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex, int expectColType)
{
    return TblIndexInsertValid(env, pTable, columnIndex, rowIndex)
        && TypeValid(env, pTable, columnIndex, rowIndex, expectColType, false);
}

bool GetBinaryData(JNIEnv* env, jobject jByteBuffer, realm::BinaryData& data);


// Utility function for appending StringData, which is returned
// by a lot of core functions, and might potentially be NULL.
std::string concat_stringdata(const char *message, StringData data);

// Note: JNI offers methods to convert between modified UTF-8 and
// UTF-16. Unfortunately these methods are not appropriate in this
// context. The reason is that they use a modified version of
// UTF-8 where U+0000 is stored as 0xC0 0x80 instead of 0x00 and
// where a character in the range U+10000 to U+10FFFF is stored as
// two consecutive UTF-8 encodings of the corresponding UTF-16
// surrogate pair. Because Realm uses proper UTF-8, we need to
// do the transcoding ourselves.
//
// See also http://en.wikipedia.org/wiki/UTF-8#Modified_UTF-8

jstring to_jstring(JNIEnv*, realm::StringData);

class JStringAccessor {
public:
    JStringAccessor(JNIEnv*, jstring);  // throws

    operator realm::StringData() const REALM_NOEXCEPT
    {
        return realm::StringData(m_data.get(), m_size);
    }

private:
    std::unique_ptr<char[]> m_data;
    std::size_t m_size;
};

class KeyBuffer {
public:
    KeyBuffer(JNIEnv* env, jbyteArray arr)
    : m_env(env)
    , m_array(arr)
    , m_ptr(0)
    {
#ifdef REALM_ENABLE_ENCRYPTION
        if (arr) {
            if (env->GetArrayLength(m_array) != 64)
                ThrowException(env, UnsupportedOperation, "Encryption key must be exactly 64 bytes.");
            m_ptr = env->GetByteArrayElements(m_array, NULL);
        }
#else
        if (arr)
            ThrowException(env, UnsupportedOperation,
                           "Encryption was disabled in the native library at compile time.");
#endif
    }

    const char *data() const {
        return reinterpret_cast<const char *>(m_ptr);
    }

    ~KeyBuffer() {
        if (m_ptr)
            m_env->ReleaseByteArrayElements(m_array, m_ptr, JNI_ABORT);
    }

private:
    JNIEnv* m_env;
    jbyteArray m_array;
    jbyte* m_ptr;
};


#endif // REALM_JAVA_UTIL_HPP
