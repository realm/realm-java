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
#include <inttypes.h>

#include <realm.hpp>
#include <realm/lang_bind_helper.hpp>
#include <realm/timestamp.hpp>
#include <realm/util/safe_int_ops.hpp>

#include <util/format.hpp>

#include "io_realm_internal_Util.h"

#include "jni_util/log.hpp"

#define CHECK_PARAMETERS 1 // Check all parameters in API and throw exceptions in java if invalid

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved);

#ifdef __cplusplus
}
#endif

#define STRINGIZE_DETAIL(x) #x
#define STRINGIZE(x) STRINGIZE_DETAIL(x)

// Exception handling
#define CATCH_STD()                                                                                                  \
    catch (...)                                                                                                      \
    {                                                                                                                \
        ConvertException(env, __FILE__, __LINE__);                                                                   \
    }

template <typename T>
std::string num_to_string(T pNumber)
{
    std::ostringstream oOStrStream;
    oOStrStream << pNumber;
    return oOStrStream.str();
}


#define MAX_JINT 0x7FFFFFFFL
#define MAX_JSIZE MAX_JINT

// TODO: Clean up those marcos. Casting with marcos reduces the readability, and it is actually breaking the C++ type
// conversion. e.g.: You cannot cast a pointer with S64 below.
// Helper macros for better readability
#define S(x) static_cast<size_t>(x)
#define B(x) static_cast<bool>(x)
#define S64(x) static_cast<int64_t>(x)
#define TBL(x) reinterpret_cast<realm::Table*>(x)
#define TV(x) reinterpret_cast<realm::TableView*>(x)
#define LV(x) reinterpret_cast<realm::LinkViewRef*>(x)
#define Q(x) reinterpret_cast<realm::Query*>(x)
#define ROW(x) reinterpret_cast<realm::Row*>(x)
#define HO(T, ptr) reinterpret_cast<realm::SharedGroup::Handover<T>*>(ptr)

// Exception handling
enum ExceptionKind {
    // FIXME: This is not something should be exposed to java, ClassNotFound is something we should
    // crash hard in native code and fix it.
    ClassNotFound = 0,
    IllegalArgument,
    IndexOutOfBounds,
    UnsupportedOperation,
    OutOfMemory,
    FatalError,
    RuntimeError,
    BadVersion,
    IllegalState,
    RealmFileError,
    // NOTE!!!!: Please also add test cases to io_realm_internal_TestUtil when introducing a
    // new exception kind.
    ExceptionKindMax // Always keep this as the last one!
};

void ConvertException(JNIEnv* env, const char* file, int line);
void ThrowException(JNIEnv* env, ExceptionKind exception, const std::string& classStr,
                    const std::string& itemStr = "");
void ThrowException(JNIEnv* env, ExceptionKind exception, const char* classStr);
void ThrowNullValueException(JNIEnv* env, realm::Table* table, size_t col_ndx);

jclass GetClass(JNIEnv* env, const char* classStr);


// Check parameters

#define TABLE_VALID(env, ptr) TableIsValid(env, ptr)
#define ROW_VALID(env, ptr) RowIsValid(env, ptr)
#define QUERY_VALID(env, ptr) QueryIsValid(env, ptr)

#if CHECK_PARAMETERS

#define ROW_INDEXES_VALID(env, ptr, start, end, range) RowIndexesValid(env, ptr, start, end, range)
#define ROW_INDEX_VALID(env, ptr, row) RowIndexValid(env, ptr, row)
#define ROW_INDEX_VALID_OFFSET(env, ptr, row) RowIndexValid(env, ptr, row, true)
#define TBL_AND_ROW_INDEX_VALID(env, ptr, row) TblRowIndexValid(env, ptr, row)
#define TBL_AND_ROW_INDEX_VALID_OFFSET(env, ptr, row, offset) TblRowIndexValid(env, ptr, row, offset)
#define COL_INDEX_VALID(env, ptr, col) ColIndexValid(env, ptr, col)
#define TBL_AND_COL_INDEX_VALID(env, ptr, col) TblColIndexValid(env, ptr, col)
#define COL_INDEX_AND_TYPE_VALID(env, ptr, col, type) ColIndexAndTypeValid(env, ptr, col, type)
#define TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ptr, col, type) TblColIndexAndTypeValid(env, ptr, col, type)
#define TBL_AND_COL_INDEX_AND_LINK_OR_LINKLIST(env, ptr, col) TblColIndexAndLinkOrLinkList(env, ptr, col)
#define TBL_AND_COL_NULLABLE(env, ptr, col) TblColIndexAndNullable(env, ptr, col)
#define INDEX_VALID(env, ptr, col, row) IndexValid(env, ptr, col, row)
#define TBL_AND_INDEX_VALID(env, ptr, col, row) TblIndexValid(env, ptr, col, row)
#define TBL_AND_INDEX_INSERT_VALID(env, ptr, col, row) TblIndexInsertValid(env, ptr, col, row)
#define INDEX_AND_TYPE_VALID(env, ptr, col, row, type) IndexAndTypeValid(env, ptr, col, row, type)
#define TBL_AND_INDEX_AND_TYPE_VALID(env, ptr, col, row, type) TblIndexAndTypeValid(env, ptr, col, row, type)
#define TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, ptr, col, row, type)                                                \
    TblIndexAndTypeInsertValid(env, ptr, col, row, type)

#define ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ptr, col, type) RowColIndexAndTypeValid(env, ptr, col, type)
#define ROW_AND_COL_INDEX_VALID(env, ptr, col) RowColIndexValid(env, ptr, col)

#else

#define ROW_INDEXES_VALID(env, ptr, start, end, range) (true)
#define ROW_INDEX_VALID(env, ptr, row) (true)
#define ROW_INDEX_VALID_OFFSET(env, ptr, row) (true)
#define TBL_AND_ROW_INDEX_VALID(env, ptr, row) (true)
#define TBL_AND_ROW_INDEX_VALID_OFFSET(env, ptr, row, offset) (true)
#define COL_INDEX_VALID(env, ptr, col) (true)
#define TBL_AND_COL_INDEX_VALID(env, ptr, col) (true)
#define COL_INDEX_AND_TYPE_VALID(env, ptr, col, type) (true)
#define TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ptr, col, type) (true)
#define TBL_AND_COL_INDEX_AND_LINK_OR_LINKLIST(env, ptr, col) (true)
#define TBL_AND_COL_NULLABLE(env, ptr, col) (true)
#define INDEX_VALID(env, ptr, col, row) (true)
#define TBL_AND_INDEX_VALID(env, ptr, col, row) (true)
#define TBL_AND_INDEX_INSERT_VALID(env, ptr, col, row) (true)
#define INDEX_AND_TYPE_VALID(env, ptr, col, row, type) (true)
#define TBL_AND_INDEX_AND_TYPE_VALID(env, ptr, col, row, type) (true)
#define TBL_AND_INDEX_AND_TYPE_INSERT_VALID(env, ptr, col, row, type) (true)

#define ROW_AND_COL_INDEX_AND_TYPE_VALID(env, ptr, col, type) (true)
#define ROW_AND_COL_INDEX_VALID(env, ptr, col) (true)

#endif


inline jlong to_jlong_or_not_found(size_t res)
{
    return (res == realm::not_found) ? jlong(-1) : jlong(res);
}

template <class T>
inline bool TableIsValid(JNIEnv* env, T* objPtr)
{
    bool valid = (objPtr != nullptr);
    if (valid) {
        // Check if Table is valid
        if (std::is_same<realm::Table, T>::value) {
            valid = TBL(objPtr)->is_attached();
        }
        // TODO: Add check for TableView
    }
    if (!valid) {
        realm::jni_util::Log::e("Table %1 is no longer attached!", reinterpret_cast<int64_t>(objPtr));
        ThrowException(env, IllegalState, "Table is no longer valid to operate on.");
    }
    return valid;
}

inline bool RowIsValid(JNIEnv* env, realm::Row* rowPtr)
{
    bool valid = (rowPtr != NULL && rowPtr->is_attached());
    if (!valid) {
        realm::jni_util::Log::e("Row %1 is no longer attached!", reinterpret_cast<int64_t>(rowPtr));
        ThrowException(env, IllegalState,
                       "Object is no longer valid to operate on. Was it deleted by another thread?");
    }
    return valid;
}

inline bool QueryIsValid(JNIEnv* env, realm::Query* query)
{
    return TableIsValid(env, query->get_table().get());
}


// Requires an attached Table
template <class T>
bool RowIndexesValid(JNIEnv* env, T* pTable, jlong startIndex, jlong endIndex, jlong range)
{
    size_t maxIndex = pTable->size();
    if (endIndex == -1) {
        endIndex = maxIndex;
    }
    if (startIndex < 0) {
        realm::jni_util::Log::e("startIndex %1 < 0 - invalid!", S64(startIndex));
        ThrowException(env, IndexOutOfBounds, "startIndex < 0.");
        return false;
    }
    if (realm::util::int_greater_than(startIndex, maxIndex)) {
        realm::jni_util::Log::e("startIndex %1 > %2 - invalid!", S64(startIndex), S64(maxIndex));
        ThrowException(env, IndexOutOfBounds, "startIndex > available rows.");
        return false;
    }

    if (realm::util::int_greater_than(endIndex, maxIndex)) {
        realm::jni_util::Log::e("endIndex %1 > %2 - invalid!", S64(endIndex), S64(maxIndex));
        ThrowException(env, IndexOutOfBounds, "endIndex > available rows.");
        return false;
    }
    if (startIndex > endIndex) {
        realm::jni_util::Log::e("startIndex %1 > endIndex %2 - invalid!", S64(startIndex), S64(endIndex));
        ThrowException(env, IndexOutOfBounds, "startIndex > endIndex.");
        return false;
    }

    if (range != -1 && range < 0) {
        realm::jni_util::Log::e("range %1 < 0 - invalid!", S64(range));
        ThrowException(env, IndexOutOfBounds, "range < 0.");
        return false;
    }

    return true;
}

template <class T>
inline bool RowIndexValid(JNIEnv* env, T pTable, jlong rowIndex, bool offset = false)
{
    if (rowIndex < 0) {
        ThrowException(env, IndexOutOfBounds, "rowIndex is less than 0.");
        return false;
    }
    size_t size = pTable->size();
    if (size > 0 && offset) {
        size -= 1;
    }
    bool rowErr = realm::util::int_greater_than_or_equal(rowIndex, size);
    if (rowErr) {
        realm::jni_util::Log::e("rowIndex %1 > %2 - invalid!", S64(rowIndex), S64(size));
        ThrowException(env, IndexOutOfBounds,
                       "rowIndex > available rows: " + num_to_string(rowIndex) + " > " + num_to_string(size));
    }
    return !rowErr;
}

template <class T>
inline bool TblRowIndexValid(JNIEnv* env, T* pTable, jlong rowIndex, bool offset = false)
{
    if (std::is_same<realm::Table, T>::value) {
        if (!TableIsValid(env, TBL(pTable))) {
            return false;
        }
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
        realm::jni_util::Log::e("columnIndex %1 > %2 - invalid!", S64(columnIndex), S64(pTable->get_column_count()));
        ThrowException(env, IndexOutOfBounds, "columnIndex > available columns.");
    }
    return !colErr;
}

template <class T>
inline bool TblColIndexValid(JNIEnv* env, T* pTable, jlong columnIndex)
{
    if (std::is_same<realm::Table, T>::value) {
        if (!TableIsValid(env, TBL(pTable))) {
            return false;
        }
    }
    return ColIndexValid(env, pTable, columnIndex);
}

inline bool RowColIndexValid(JNIEnv* env, realm::Row* pRow, jlong columnIndex)
{
    return RowIsValid(env, pRow) && ColIndexValid(env, pRow->get_table(), columnIndex);
}

template <class T>
inline bool IndexValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex)
{
    return ColIndexValid(env, pTable, columnIndex) && RowIndexValid(env, pTable, rowIndex);
}

template <class T>
inline bool TblIndexValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex)
{
    return TableIsValid(env, pTable) && IndexValid(env, pTable, columnIndex, rowIndex);
}

template <class T>
inline bool TblIndexInsertValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex)
{
    if (!TblColIndexValid(env, pTable, columnIndex)) {
        return false;
    }
    bool rowErr = realm::util::int_greater_than(rowIndex, pTable->size() + 1);
    if (rowErr) {
        realm::jni_util::Log::e("rowIndex %1 > %2 - invalid!", S64(rowIndex), S64(pTable->size()));
        ThrowException(env, IndexOutOfBounds, "rowIndex " + num_to_string(rowIndex) + " > available rows " +
                                                  num_to_string(pTable->size()) + ".");
    }
    return !rowErr;
}

template <class T>
inline bool TypeValid(JNIEnv* env, T* pTable, jlong columnIndex, int expectColType)
{
    size_t col = static_cast<size_t>(columnIndex);
    int colType = pTable->get_column_type(col);
    if (colType != expectColType) {
        realm::jni_util::Log::e("Expected columnType %1, but got %2.", expectColType, pTable->get_column_type(col));
        ThrowException(env, IllegalArgument, "ColumnType of '" + std::string(pTable->get_column_name(col)) + "' is invalid.");
        return false;
    }
    return true;
}

template <class T>
inline bool TypeIsLinkLike(JNIEnv* env, T* pTable, jlong columnIndex)
{
    size_t col = static_cast<size_t>(columnIndex);
    int colType = pTable->get_column_type(col);
    if (colType == realm::type_Link || colType == realm::type_LinkList) {
        return true;
    }

    realm::jni_util::Log::e("Expected columnType %1 or %2, but got %3", realm::type_Link, realm::type_LinkList,
                            colType);
    ThrowException(env, IllegalArgument, "ColumnType of '" + std::string(pTable->get_column_name(col)) + "' is invalid:"
                                         " expected type_Link or type_LinkList");
    return false;
}

template <class T>
inline bool ColIsNullable(JNIEnv* env, T* pTable, jlong columnIndex)
{
    size_t col = static_cast<size_t>(columnIndex);
    int colType = pTable->get_column_type(col);
    if (colType == realm::type_Link) {
        return true;
    }

    if (colType == realm::type_LinkList) {
        ThrowException(env, IllegalArgument, "RealmList(" + std::string(pTable->get_column_name(col)) + ") is not nullable.");
        return false;
    }

    if (pTable->is_nullable(col)) {
        return true;
    }

    realm::jni_util::Log::e("Expected nullable column type");
    ThrowException(env, IllegalArgument, "This field(" + std::string(pTable->get_column_name(col)) + ") is not nullable.");
    return false;
}

template <class T>
inline bool ColIndexAndTypeValid(JNIEnv* env, T* pTable, jlong columnIndex, int expectColType)
{
    return ColIndexValid(env, pTable, columnIndex) && TypeValid(env, pTable, columnIndex, expectColType);
}
template <class T>
inline bool TblColIndexAndTypeValid(JNIEnv* env, T* pTable, jlong columnIndex, int expectColType)
{
    return TableIsValid(env, pTable) && ColIndexAndTypeValid(env, pTable, columnIndex, expectColType);
}

template <class T>
inline bool TblColIndexAndLinkOrLinkList(JNIEnv* env, T* pTable, jlong columnIndex)
{
    return TableIsValid(env, pTable) && TypeIsLinkLike(env, pTable, columnIndex);
}

// FIXME Usually this is called after TBL_AND_INDEX_AND_TYPE_VALID which will validate Table as well.
// Try to avoid duplicated checks to improve performance.
template <class T>
inline bool TblColIndexAndNullable(JNIEnv* env, T* pTable, jlong columnIndex)
{
    return TableIsValid(env, pTable) && ColIsNullable(env, pTable, columnIndex);
}

inline bool RowColIndexAndTypeValid(JNIEnv* env, realm::Row* pRow, jlong columnIndex, int expectColType)
{
    return RowIsValid(env, pRow) && ColIndexAndTypeValid(env, pRow->get_table(), columnIndex, expectColType);
}

template <class T>
inline bool IndexAndTypeValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex, int expectColType)
{
    return IndexValid(env, pTable, columnIndex, rowIndex) && TypeValid(env, pTable, columnIndex, expectColType);
}
template <class T>
inline bool TblIndexAndTypeValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex, int expectColType)
{
    return TableIsValid(env, pTable) && IndexAndTypeValid(env, pTable, columnIndex, rowIndex, expectColType);
}

template <class T>
inline bool TblIndexAndTypeInsertValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex, int expectColType)
{
    return TblIndexInsertValid(env, pTable, columnIndex, rowIndex) &&
           TypeValid(env, pTable, columnIndex, expectColType);
}

bool GetBinaryData(JNIEnv* env, jobject jByteBuffer, realm::BinaryData& data);


// Utility function for appending StringData, which is returned
// by a lot of core functions, and might potentially be NULL.
std::string concat_stringdata(const char* message, realm::StringData data);

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
    JStringAccessor(JNIEnv*, jstring); // throws

    operator realm::StringData() const noexcept
    {
        if (m_is_null) {
            return realm::StringData(NULL);
        }
        else {
            return realm::StringData(m_data.get(), m_size);
        }
    }

    operator std::string() const noexcept
    {
        if (m_is_null) {
            return std::string();
        }
        return std::string(m_data.get(), m_size);
    }

private:
    bool m_is_null;
    std::unique_ptr<char[]> m_data;
    std::size_t m_size;
};

class JniLongArray {
public:
    JniLongArray(JNIEnv* env, jlongArray javaArray)
        : m_env(env)
        , m_javaArray(javaArray)
        , m_arrayLength(javaArray == NULL ? 0 : env->GetArrayLength(javaArray))
        , m_array(javaArray == NULL ? NULL : env->GetLongArrayElements(javaArray, NULL))
        , m_releaseMode(JNI_ABORT)
    {
    }

    JniLongArray(JniLongArray& other) = delete;

    JniLongArray(JniLongArray&& other)
        : m_env(other.m_env)
        , m_javaArray(other.m_javaArray)
        , m_arrayLength(other.m_arrayLength)
        , m_array(other.m_array)
        , m_releaseMode(other.m_releaseMode)
    {
        other.m_env = nullptr;
        other.m_javaArray = nullptr;
        other.m_arrayLength = 0;
        other.m_array = nullptr;
    }

    ~JniLongArray()
    {
        if (m_array) {
            m_env->ReleaseLongArrayElements(m_javaArray, m_array, m_releaseMode);
        }
    }

    inline jsize len() const noexcept
    {
        return m_arrayLength;
    }

    inline jlong* ptr() const noexcept
    {
        return m_array;
    }

    inline jlong& operator[](const int index) noexcept
    {
        return m_array[index];
    }

    inline void updateOnRelease() noexcept
    {
        m_releaseMode = 0;
    }

private:
    JNIEnv* m_env;
    jlongArray m_javaArray;
    jsize m_arrayLength;
    jlong* m_array;
    jint m_releaseMode;
};

template <typename T, typename J>
class JniArrayOfArrays {
public:
    JniArrayOfArrays(JNIEnv* env, jobjectArray javaArray)
        : m_env(env)
        , m_javaArray(javaArray)
        , m_arrayLength(javaArray == nullptr ? 0 : env->GetArrayLength(javaArray))
    {
        for (int i = 0; i < m_arrayLength; ++i) {
            // No type checking. Internal use only.
            J j_array = static_cast<J>(env->GetObjectArrayElement(m_javaArray, i));
            m_array.push_back(T(env, j_array));
        }
    }

    ~JniArrayOfArrays()
    {
    }

    inline jsize len() const noexcept
    {
        return m_arrayLength;
    }

    inline T& operator[](const int index) noexcept
    {
        return m_array[index];
    }

private:
    JNIEnv* const m_env;
    jobjectArray const m_javaArray;
    jsize const m_arrayLength;
    std::vector<T> m_array;
};

class JniByteArray {
public:
    JniByteArray(JNIEnv* env, jbyteArray javaArray)
        : m_env(env)
        , m_javaArray(javaArray)
        , m_arrayLength(javaArray == NULL ? 0 : env->GetArrayLength(javaArray))
        , m_array(javaArray == NULL ? NULL : env->GetByteArrayElements(javaArray, NULL))
        , m_releaseMode(JNI_ABORT)
    {
        if (m_javaArray != nullptr && m_array == nullptr) {
            // javaArray is not null but GetByteArrayElements returns null, something is really wrong.
            throw std::runtime_error(
                realm::util::format("GetByteArrayElements failed on byte array %x", m_javaArray));
        }
    }

    ~JniByteArray()
    {
        if (m_array) {
            m_env->ReleaseByteArrayElements(m_javaArray, m_array, m_releaseMode);
        }
    }

    inline jsize len() const noexcept
    {
        return m_arrayLength;
    }

    inline jbyte* ptr() const noexcept
    {
        return m_array;
    }

    inline jbyte& operator[](const int index) noexcept
    {
        return m_array[index];
    }

    inline operator realm::BinaryData() const noexcept
    {
        return realm::BinaryData(reinterpret_cast<const char*>(m_array), m_arrayLength);
    }

    inline operator std::vector<char>() const noexcept
    {
        if (m_array == nullptr) {
            return {};
        }

        std::vector<char> v(m_arrayLength);
        std::copy_n(m_array, v.size(), v.begin());
        return v;
    }

    inline void updateOnRelease() noexcept
    {
        m_releaseMode = 0;
    }

private:
    JNIEnv* const m_env;
    jbyteArray const m_javaArray;
    jsize const m_arrayLength;
    jbyte* const m_array;
    jint m_releaseMode;
};

class JniBooleanArray {
public:
    JniBooleanArray(JNIEnv* env, jbooleanArray javaArray)
        : m_env(env)
        , m_javaArray(javaArray)
        , m_arrayLength(javaArray == NULL ? 0 : env->GetArrayLength(javaArray))
        , m_array(javaArray == NULL ? NULL : env->GetBooleanArrayElements(javaArray, NULL))
        , m_releaseMode(JNI_ABORT)
    {
    }

    ~JniBooleanArray()
    {
        if (m_array) {
            m_env->ReleaseBooleanArrayElements(m_javaArray, m_array, m_releaseMode);
        }
    }

    inline jsize len() const noexcept
    {
        return m_arrayLength;
    }

    inline jboolean* ptr() const noexcept
    {
        return m_array;
    }

    inline jboolean& operator[](const int index) noexcept
    {
        return m_array[index];
    }

    inline void updateOnRelease() noexcept
    {
        m_releaseMode = 0;
    }

private:
    JNIEnv* const m_env;
    jbooleanArray const m_javaArray;
    jsize const m_arrayLength;
    jboolean* const m_array;
    jint m_releaseMode;
};

extern jclass java_lang_long;
extern jmethodID java_lang_long_init;
extern jclass java_lang_float;
extern jmethodID java_lang_float_init;
extern jclass java_lang_double;
extern jclass java_lang_string;
extern jmethodID java_lang_double_init;
extern jclass java_util_date;
extern jmethodID java_util_date_init;
#if REALM_ENABLE_SYNC
extern jclass java_syncmanager_class;
extern jmethodID java_notify_progress_listener;
#endif

inline jobject NewLong(JNIEnv* env, int64_t value)
{
    return env->NewObject(java_lang_long, java_lang_long_init, value);
}

inline jobject NewDouble(JNIEnv* env, double value)
{
    return env->NewObject(java_lang_double, java_lang_double_init, value);
}

inline jobject NewFloat(JNIEnv* env, float value)
{
    return env->NewObject(java_lang_float, java_lang_float_init, value);
}

inline jlong to_milliseconds(const realm::Timestamp& ts)
{
    // From core's reference implementation aka unit test
    // FIXME: check for overflow/underflow
    const int64_t seconds = ts.get_seconds();
    const int32_t nanoseconds = ts.get_nanoseconds();
    const int64_t milliseconds = seconds * 1000 + nanoseconds / 1000000; // This may overflow
    return milliseconds;
}

inline realm::Timestamp from_milliseconds(jlong milliseconds)
{
    // From core's reference implementation aka unit test
    int64_t seconds = milliseconds / 1000;
    int32_t nanoseconds = (milliseconds % 1000) * 1000000;
    return realm::Timestamp(seconds, nanoseconds);
}

inline jobject NewDate(JNIEnv* env, const realm::Timestamp& ts)
{
    return env->NewObject(java_util_date, java_util_date_init, to_milliseconds(ts));
}

extern const std::string TABLE_PREFIX;

static inline bool to_bool(jboolean b)
{
    return b == JNI_TRUE;
}

static inline jboolean to_jbool(bool b)
{
    return b ? JNI_TRUE : JNI_FALSE;
}

#endif // REALM_JAVA_UTIL_HPP
