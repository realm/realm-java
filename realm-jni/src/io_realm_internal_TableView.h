/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class io_realm_internal_TableView */

#ifndef _Included_io_realm_internal_TableView
#define _Included_io_realm_internal_TableView
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeClose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeClose
  (JNIEnv *, jclass, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSize
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeSize
  (JNIEnv *, jobject, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetSourceRowIndex
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetSourceRowIndex
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetColumnCount
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetColumnCount
  (JNIEnv *, jobject, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetColumnName
 * Signature: (JJ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeGetColumnName
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetColumnIndex
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetColumnIndex
  (JNIEnv *, jobject, jlong, jstring);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetColumnType
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_io_realm_internal_TableView_nativeGetColumnType
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetLong
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetLong
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetBoolean
 * Signature: (JJJ)Z
 */
JNIEXPORT jboolean JNICALL Java_io_realm_internal_TableView_nativeGetBoolean
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetFloat
 * Signature: (JJJ)F
 */
JNIEXPORT jfloat JNICALL Java_io_realm_internal_TableView_nativeGetFloat
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetDouble
 * Signature: (JJJ)D
 */
JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeGetDouble
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetDateTimeValue
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetDateTimeValue
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetString
 * Signature: (JJJ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeGetString
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetByteArray
 * Signature: (JJJ)[B
 */
JNIEXPORT jbyteArray JNICALL Java_io_realm_internal_TableView_nativeGetByteArray
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetLink
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetLink
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetSubtable
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetSubtable
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeGetSubtableSize
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeGetSubtableSize
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeClearSubtable
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeClearSubtable
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSetLong
 * Signature: (JJJJ)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetLong
  (JNIEnv *, jobject, jlong, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSetBoolean
 * Signature: (JJJZ)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetBoolean
  (JNIEnv *, jobject, jlong, jlong, jlong, jboolean);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSetFloat
 * Signature: (JJJF)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetFloat
  (JNIEnv *, jobject, jlong, jlong, jlong, jfloat);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSetDouble
 * Signature: (JJJD)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetDouble
  (JNIEnv *, jobject, jlong, jlong, jlong, jdouble);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSetDateTimeValue
 * Signature: (JJJJ)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetDateTimeValue
  (JNIEnv *, jobject, jlong, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSetString
 * Signature: (JJJLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetString
  (JNIEnv *, jobject, jlong, jlong, jlong, jstring);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSetByteArray
 * Signature: (JJJ[B)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetByteArray
  (JNIEnv *, jobject, jlong, jlong, jlong, jbyteArray);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSetLink
 * Signature: (JJJJ)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSetLink
  (JNIEnv *, jobject, jlong, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeIsNullLink
 * Signature: (JJJ)Z
 */
JNIEXPORT jboolean JNICALL Java_io_realm_internal_TableView_nativeIsNullLink
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeNullifyLink
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeNullifyLink
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeAddInt
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeAddInt
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeClear
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeClear
  (JNIEnv *, jobject, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeRemoveRow
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeRemoveRow
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeFindFirstInt
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstInt
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeFindFirstBool
 * Signature: (JJZ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstBool
  (JNIEnv *, jobject, jlong, jlong, jboolean);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeFindFirstFloat
 * Signature: (JJF)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstFloat
  (JNIEnv *, jobject, jlong, jlong, jfloat);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeFindFirstDouble
 * Signature: (JJD)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstDouble
  (JNIEnv *, jobject, jlong, jlong, jdouble);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeFindFirstDate
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstDate
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeFindFirstString
 * Signature: (JJLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindFirstString
  (JNIEnv *, jobject, jlong, jlong, jstring);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeFindAllInt
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllInt
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeFindAllBool
 * Signature: (JJZ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllBool
  (JNIEnv *, jobject, jlong, jlong, jboolean);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeFindAllFloat
 * Signature: (JJF)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllFloat
  (JNIEnv *, jobject, jlong, jlong, jfloat);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeFindAllDouble
 * Signature: (JJD)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllDouble
  (JNIEnv *, jobject, jlong, jlong, jdouble);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeFindAllDate
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllDate
  (JNIEnv *, jobject, jlong, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeFindAllString
 * Signature: (JJLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeFindAllString
  (JNIEnv *, jobject, jlong, jlong, jstring);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSumInt
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeSumInt
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeMaximumInt
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeMaximumInt
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeMinimumInt
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeMinimumInt
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeAverageInt
 * Signature: (JJ)D
 */
JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeAverageInt
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSumFloat
 * Signature: (JJ)D
 */
JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeSumFloat
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeMaximumFloat
 * Signature: (JJ)F
 */
JNIEXPORT jfloat JNICALL Java_io_realm_internal_TableView_nativeMaximumFloat
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeMinimumFloat
 * Signature: (JJ)F
 */
JNIEXPORT jfloat JNICALL Java_io_realm_internal_TableView_nativeMinimumFloat
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeAverageFloat
 * Signature: (JJ)D
 */
JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeAverageFloat
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSumDouble
 * Signature: (JJ)D
 */
JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeSumDouble
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeMaximumDouble
 * Signature: (JJ)D
 */
JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeMaximumDouble
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeMinimumDouble
 * Signature: (JJ)D
 */
JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeMinimumDouble
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeAverageDouble
 * Signature: (JJ)D
 */
JNIEXPORT jdouble JNICALL Java_io_realm_internal_TableView_nativeAverageDouble
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeMaximumDate
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeMaximumDate
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeMinimumDate
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeMinimumDate
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSort
 * Signature: (JJZ)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSort
  (JNIEnv *, jobject, jlong, jlong, jboolean);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSortMulti
 * Signature: (J[J[Z)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativeSortMulti
  (JNIEnv *, jobject, jlong, jlongArray, jbooleanArray);

/*
 * Class:     io_realm_internal_TableView
 * Method:    createNativeTableView
 * Signature: (Lio/realm/internal/Table;J)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_createNativeTableView
  (JNIEnv *, jobject, jobject, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeToJson
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeToJson
  (JNIEnv *, jobject, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeToString
 * Signature: (JJ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeToString
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeRowToString
 * Signature: (JJ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_io_realm_internal_TableView_nativeRowToString
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeWhere
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeWhere
  (JNIEnv *, jobject, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativePivot
 * Signature: (JJJIJ)V
 */
JNIEXPORT void JNICALL Java_io_realm_internal_TableView_nativePivot
  (JNIEnv *, jobject, jlong, jlong, jlong, jint, jlong);

/*
 * Class:     io_realm_internal_TableView
 * Method:    nativeSync
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_io_realm_internal_TableView_nativeSync
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
