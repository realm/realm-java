#ifndef TIGHTDB_JAVA_UTIL_HPP
#define TIGHTDB_JAVA_UTIL_HPP

#include <string>

#include <jni.h>

#include <tightdb.hpp>
#include <tightdb/meta.hpp>
#include <tightdb/unique_ptr.hpp>
#include <tightdb/safe_int_ops.hpp>
#include <tightdb/lang_bind_helper.hpp>

#include "com_tightdb_internal_util.hpp"

//using namespace tightdb;

#define TRACE               1       // disable for performance
#define CHECK_PARAMETERS    1       // Check all parameters in API and throw exceptions in java if invalid

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved);

#ifdef __cplusplus
}
#endif

#define MAX_JLONG  9223372036854775807
#define MIN_JLONG -9223372036854775808
#define MAX_JINT   2147483647
#define MAX_JSIZE  MAX_JINT

// Helper macros for better readability
#define S(x) static_cast<size_t>(x)
#define TBL(x) reinterpret_cast<tightdb::Table*>(x)
#define TV(x) reinterpret_cast<tightdb::TableView*>(x)
#define Q(x) reinterpret_cast<tightdb::Query*>(x)
#define G(x) reinterpret_cast<tightdb::Group*>(x)

// Exception handling

enum ExceptionKind {
    ClassNotFound,
    NoSuchField,
    NoSuchMethod,
    IllegalArgument,
    IOFailed,
    IndexOutOfBounds,
    TableInvalid,
    UnsupportedOperation
};

extern void ThrowException(JNIEnv* env, ExceptionKind exception, std::string classStr, std::string itemStr = "");

extern jclass GetClass(JNIEnv* env, const char* classStr);


// Debug trace

extern int trace_level;

#if TRACE
#define TR(args) if (trace_level >= 2) { jprintf args; } else {}
#define TR_ERR(args) if (trace_level >= 1) { jprintf args; } else {}
#else
#define TR(args)
#define TR_ERR(args)
#endif

extern void jprintf(JNIEnv *env, const char *fmt, ...);

extern void jprint(JNIEnv *env, char *txt);


// Check parameters

#define TABLE_VALID(env,ptr)                        TableIsValid(env, ptr)

#if CHECK_PARAMETERS

#define ROW_INDEXES_VALID(env,ptr,start,end, range) RowIndexesValid(env, ptr, start, end, range)
#define ROW_INDEX_VALID(env,ptr,row)                RowIndexValid(env, ptr, row)
#define ROW_INDEX_VALID_OFFSET(env,ptr,row, offset) RowIndexValid(env, ptr, row, offset)
#define COL_INDEX_VALID(env,ptr,col)                ColIndexValid(env, ptr, col)
#define INDEX_VALID(env,ptr,col,row)                IndexValid(env, ptr, col, row)
#define INDEX_INSERT_VALID(env,ptr,col,row)         IndexInsertValid(env, ptr, col, row)
#define INDEX_AND_TYPE_VALID(env,ptr,col,row,type)  IndexAndTypeValid(env, ptr, col, row, type)
#define INDEX_AND_TYPE_INSERT_VALID(env,ptr,col,row,type)  IndexAndTypeInsertValid(env, ptr, col, row, type)

#else

#define ROW_INDEXES_VALID(env,ptr,row) (true)
#define ROW_INDEX_VALID(env,ptr,row) (true)
#define COL_INDEX_VALID(env,ptr,col) (true)
#define INDEX_VALID(env,ptr,col,row) (true)
#define INDEX_INSERT_VALID(env,ptr,col,row) (true)
#define INDEX_AND_TYPE_VALID(env,ptr,col,row,type) (true)
#define INDEX_AND_TYPE_INSERT_VALID(env,ptr,col,row,type) (true)

#endif

inline bool TableIsValid(JNIEnv* env, tightdb::Table* pTable)
{
    bool valid = (pTable != NULL);
    if (valid)
        valid = pTable->is_valid();
    if (!valid) {
        TR_ERR((env, "Table %x is invalid!", pTable));
        ThrowException(env, IllegalArgument, "Table is invalid.");
    }
    return valid;
}

template <class T>
bool RowIndexesValid(JNIEnv* env, T* pTable, jlong startIndex, jlong endIndex, jlong range)
{
    // Check if Table is valid - but only if T is a 'Table' type
    if (tightdb::SameType<tightdb::Table*, T>::value)
        if (!TableIsValid(env, TBL(pTable)))
            return false;

    size_t maxIndex = pTable->size();
    if (endIndex == -1)
        endIndex = maxIndex;
    if (startIndex < 0) {
        TR_ERR((env, "startIndex %lld < 0 - invalid!", S(startIndex), 0));
        ThrowException(env, IndexOutOfBounds, "startIndex < 0.");
        return false;
    }
    if (tightdb::int_greater_than(startIndex, maxIndex)) {
        TR_ERR((env, "startIndex %lld > %lld - invalid!", S(startIndex), maxIndex));
        ThrowException(env, IndexOutOfBounds, "startIndex > available rows.");
        return false;
    }

    if (tightdb::int_greater_than(endIndex, maxIndex)) {
        TR_ERR((env, "endIndex %lld > %lld - invalid!", S(endIndex), maxIndex));
        ThrowException(env, IndexOutOfBounds, "endIndex > available rows.");
        return false;
    }
    if (startIndex > endIndex) {
        TR_ERR((env, "startIndex %lld > endIndex %lld- invalid!", S(startIndex), S(endIndex)));
        ThrowException(env, IndexOutOfBounds, "startIndex > endIndex.");
        return false;
    }

    if (range != -1 && range < 0) {
        TR_ERR((env, "range %lld < 0 - invalid!", range));
        ThrowException(env, IndexOutOfBounds, "range < 0.");
        return false;
    }

    return true;
}

template <class T>
inline bool RowIndexValid(JNIEnv* env, T* pTable, jlong rowIndex, jlong offset=0)
{
    // Check if Table is valid - but only if T is a 'Table' type
    if (tightdb::SameType<tightdb::Table*, T>::value)
        if (!TableIsValid(env, TBL(pTable)))
            return false;
    size_t size = pTable->size();
    if (size > 0)
        size += offset;
    bool rowErr = tightdb::int_greater_than_or_equal(rowIndex, size);
    if (rowErr) {
        TR_ERR((env, "rowIndex %lld > %lld - invalid!", S(rowIndex), size));
        ThrowException(env, IndexOutOfBounds, "rowIndex > available rows.");
    }
    return !rowErr;
}

template <class T>
inline bool ColIndexValid(JNIEnv* env, T* pTable, jlong columnIndex)
{
    // Check if Table is valid - but only if T is a 'Table' type
    if (tightdb::SameType<tightdb::Table*, T>::value)
        if (!TableIsValid(env, TBL(pTable)))
            return false;

    bool colErr = tightdb::int_greater_than_or_equal(columnIndex, pTable->get_column_count());
    if (colErr) {
        TR_ERR((env, "columnIndex %lld > %lld - invalid!", S(columnIndex), pTable->get_column_count()));
        ThrowException(env, IndexOutOfBounds, "columnIndex > available columns.");
    }
    return !colErr;
}

template <class T>
inline bool IndexValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex)
{
    return ColIndexValid(env, pTable, columnIndex) && RowIndexValid(env, pTable, rowIndex);
}

template <class T>
inline bool IndexInsertValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex)
{
    if (!ColIndexValid(env, pTable, columnIndex))
        return false;
    // FIXME: REMOVE const size_t colSize = pTable->GetColumn(columnIndex).Size();
    bool rowErr = tightdb::int_greater_than(rowIndex, pTable->size()+1);
    if (rowErr) {
        TR_ERR((env, "rowIndex %lld > %lld - invalid!", rowIndex, pTable->size()));
        ThrowException(env, IndexOutOfBounds, "rowIndex > available rows.");
    }
    return !rowErr;
}

template <class T>
inline bool TypeValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex, int expectColType)
{
    size_t col = static_cast<size_t>(columnIndex);
    size_t row = static_cast<size_t>(rowIndex);
    int colType = pTable->get_column_type(col);
    if (colType == tightdb::type_Mixed)
        colType = pTable->get_mixed_type(col, row);

    if (colType != expectColType) {
        TR_ERR((env, "Expected columnType %d, but got %d.", expectColType, pTable->get_column_type(col)));
        ThrowException(env, IllegalArgument, "column type != ColumnTypeTable.");
        return false;
    }
    return true;
}

template <class T>
inline bool IndexAndTypeValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex, int expectColType)
{
    if (!IndexValid(env, pTable, columnIndex, rowIndex))
        return false;
    if (!TypeValid(env, pTable, columnIndex, rowIndex, expectColType))
        return false;
    return true;
}

template <class T>
inline bool IndexAndTypeInsertValid(JNIEnv* env, T* pTable, jlong columnIndex, jlong rowIndex, int expectColType)
{
    if (!IndexInsertValid(env, pTable, columnIndex, rowIndex))
        return false;
    if (!TypeValid(env, pTable, columnIndex, rowIndex, expectColType))
        return false;
    return true;
}

bool GetBinaryData(JNIEnv* env, jobject jByteBuffer, tightdb::BinaryData& data);


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

jstring to_jstring(JNIEnv*, tightdb::StringData);

class JStringAccessor {
public:
    JStringAccessor(JNIEnv*, jstring);

    operator tightdb::StringData() const TIGHTDB_NOEXCEPT
    {
        return tightdb::StringData(m_data.get(), m_size);
    }

    // Part of the "safe bool" idiom
    typedef tightdb::UniquePtr<char[]> (JStringAccessor::*unspecified_bool_type);
    operator unspecified_bool_type() const TIGHTDB_NOEXCEPT
    {
        return m_data ? &JStringAccessor::m_data : 0;
    }

private:
    tightdb::UniquePtr<char[]> m_data;
    std::size_t m_size;
};

#endif // TIGHTDB_JAVA_UTIL_HPP
