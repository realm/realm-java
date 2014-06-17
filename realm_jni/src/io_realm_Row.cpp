#include "io_realm_Row.h"
#include "util.hpp"
#include "mixedutil.hpp"
#include "tablebase_tpl.hpp"

using namespace tightdb;

JNIEXPORT jlong JNICALL Java_io_realm_Row_nativeGetColumnCount
  (JNIEnv *, jobject, jlong nativeRowPtr)
{
    if (!ROW(nativeRowPtr)->is_attached())
        return 0;
    return ROW(nativeRowPtr)->get_column_count(); // noexcept
}

JNIEXPORT jstring JNICALL Java_io_realm_Row_nativeGetColumnName
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex))
        return NULL;
    try {
        return to_jstring(env, ROW(nativeRowPtr)->get_column_name( S(columnIndex)));
    } CATCH_STD();
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_Row_nativeGetColumnIndex
  (JNIEnv* env, jobject, jlong nativeRowPtr, jstring columnName)
{
    if (!ROW(nativeRowPtr)->is_attached())
        return 0;
    try {
        JStringAccessor columnName2(env, columnName); // throws
        return to_jlong_or_not_found( ROW(nativeRowPtr)->get_column_index(columnName2) ); // noexcept
    } CATCH_STD()
    return 0;
}

JNIEXPORT jint JNICALL Java_io_realm_Row_nativeGetColumnType
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex))
        return 0;

    return static_cast<jint>( ROW(nativeRowPtr)->get_column_type( S(columnIndex)) ); // noexcept
}

JNIEXPORT jlong JNICALL Java_io_realm_Row_nativeGetIndex
  (JNIEnv* env, jobject, jlong nativeRowPtr)
{
    if (!TABLE_VALID(env, ROW(nativeRowPtr)->get_table()))
        return 0;

    return ROW(nativeRowPtr)->get_index();
}

JNIEXPORT jlong JNICALL Java_io_realm_Row_nativeGetLong
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Int))
        return 0;

    return ROW(nativeRowPtr)->get_int( S(columnIndex) );
}

JNIEXPORT jboolean JNICALL Java_io_realm_Row_nativeGetBoolean
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Bool))
        return 0;

    return ROW(nativeRowPtr)->get_bool( S(columnIndex) );
}

JNIEXPORT jfloat JNICALL Java_io_realm_Row_nativeGetFloat
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Float))
        return 0;

    return ROW(nativeRowPtr)->get_float( S(columnIndex) );
}

JNIEXPORT jdouble JNICALL Java_io_realm_Row_nativeGetDouble
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Double))
        return 0;

    return ROW(nativeRowPtr)->get_double( S(columnIndex) );
}

JNIEXPORT jlong JNICALL Java_io_realm_Row_nativeGetDateTime
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_DateTime))
        return 0;

    return ROW(nativeRowPtr)->get_datetime( S(columnIndex) ).get_datetime();
}

JNIEXPORT jstring JNICALL Java_io_realm_Row_nativeGetString
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_String))
        return 0;

    try {
        return to_jstring(env, ROW(nativeRowPtr)->get_string( S(columnIndex) ));
    } CATCH_STD()
    return NULL;
}

JNIEXPORT jbyteArray JNICALL Java_io_realm_Row_nativeGetByteArray
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Binary))
        return 0;

    BinaryData bin = ROW(nativeRowPtr)->get_binary( S(columnIndex) );
    if (bin.size() <= MAX_JSIZE) {
        jbyteArray jresult = env->NewByteArray(static_cast<jsize>(bin.size()));
        if (jresult)
            env->SetByteArrayRegion(jresult, 0, static_cast<jsize>(bin.size()), reinterpret_cast<const jbyte*>(bin.data()));  // throws
        return jresult;
    }
    else {
        ThrowException(env, IllegalArgument, "Length of ByteArray is larger than an Int.");
        return NULL;
    }
}

JNIEXPORT jint JNICALL Java_io_realm_Row_nativeGetMixedType
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Mixed))
        return 0;

    DataType mixedType = ROW(nativeRowPtr)->get_mixed_type( S(columnIndex) );  // noexcept
    return static_cast<jint>(mixedType);
}

JNIEXPORT jobject JNICALL Java_io_realm_Row_nativeGetMixed
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Mixed))
        return NULL;

    Mixed value = ROW(nativeRowPtr)->get_mixed( S(columnIndex) );  // noexcept
    try {
        return CreateJMixedFromMixed(env, value);
    } CATCH_STD();
    return NULL;
}

JNIEXPORT jlong JNICALL Java_io_realm_Row_nativeGetLink
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Link))
        return 0;

    return ROW(nativeRowPtr)->get_link( S(columnIndex) );
}

JNIEXPORT jboolean JNICALL Java_io_realm_Row_nativeIsNullLink
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Link))
        return 0;

    return ROW(nativeRowPtr)->is_null_link( S(columnIndex) );
}

JNIEXPORT jlong JNICALL Java_io_realm_Row_nativeGetLinkView
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_LinkList))
        return 0;

    LinkView* link_view_ptr = LangBindHelper::get_linklist_ptr( *ROW( nativeRowPtr ), S( columnIndex) );
    return reinterpret_cast<jlong>(link_view_ptr);
}

JNIEXPORT void JNICALL Java_io_realm_Row_nativeSetLong
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Int))
        return;

    try {
        ROW(nativeRowPtr)->set_int( S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_Row_nativeSetBoolean
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jboolean value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Bool))
        return;

    try {
        ROW(nativeRowPtr)->set_bool( S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_Row_nativeSetFloat
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jfloat value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Float))
        return;

    try {
        ROW(nativeRowPtr)->set_float( S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_Row_nativeSetDouble
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jdouble value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Double))
        return;

    try {
        ROW(nativeRowPtr)->set_double( S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_Row_nativeSetDate
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    if(!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_DateTime))
        return;

    try {
        ROW(nativeRowPtr)->set_datetime( S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_Row_nativeSetString
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jstring value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_String))
        return;

    try {
        JStringAccessor value2(env, value); // throws
        ROW(nativeRowPtr)->set_string( S(columnIndex), value2);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_Row_nativeSetByteArray
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jbyteArray value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Binary))
        return;

    jbyte* bytePtr = env->GetByteArrayElements(value, NULL);
    if (!bytePtr) {
        ThrowException(env, IllegalArgument, "doByteArray");
        return;
    }
    size_t dataLen = S(env->GetArrayLength(value));
    ROW(nativeRowPtr)->set_binary( S(columnIndex), BinaryData(reinterpret_cast<char*>(bytePtr), dataLen));
    env->ReleaseByteArrayElements(value, bytePtr, 0);
}

JNIEXPORT void JNICALL Java_io_realm_Row_nativeSetMixed
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jobject jMixedValue)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Mixed))
        return;
    try {
        row_nativeSetMixed(ROW(nativeRowPtr), env, columnIndex, jMixedValue);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_Row_nativeSetLink
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex, jlong value)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Link))
        return;

    try {
        ROW(nativeRowPtr)->set_link( S(columnIndex), value);
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_Row_nativeNullifyLink
  (JNIEnv* env, jobject, jlong nativeRowPtr, jlong columnIndex)
{
    if (!TBL_AND_COL_INDEX_AND_TYPE_VALID(env, ROW(nativeRowPtr)->get_table(), columnIndex, type_Link))
        return;

    try {
        ROW(nativeRowPtr)->nullify_link( S(columnIndex) );
    } CATCH_STD()
}

JNIEXPORT void JNICALL Java_io_realm_Row_nativeClose
  (JNIEnv *, jclass, jlong nativeRowPtr)
{
    delete ROW(nativeRowPtr);
}


