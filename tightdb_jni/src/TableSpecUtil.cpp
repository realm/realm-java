#include "TableSpecUtil.h"
#include "columntypeutil.h"

using namespace tightdb;

jlong Java_com_tightdb_TableSpec_getColumnCount(JNIEnv* env, jobject jTableSpec)
{
	static jmethodID jGetColumnCountMethodId = NULL;
	if (jGetColumnCountMethodId == NULL) {
		jclass jTableSpecClass = env->GetObjectClass(jTableSpec);
		jGetColumnCountMethodId = env->GetMethodID(jTableSpecClass, "getColumnCount", "()J");
		if (jGetColumnCountMethodId == NULL) {
			jclass jNoSuchMethodClass = env->FindClass("java/lang/NoSuchMethodException");
			env->ThrowNew(jNoSuchMethodClass, "Method 'getColumnCount' could not be located in class com.tightdb.TableSpec");
			return -1;
		}
	}
	return env->CallLongMethod(jTableSpec, jGetColumnCountMethodId);
}

jobject Java_com_tightdb_TableSpec_getColumnType(JNIEnv* env, jobject jTableSpec, jlong columnIndex)
{
	static jmethodID jGetColumnTypeMethodId = NULL;
	if (jGetColumnTypeMethodId == NULL) {
		jclass jTableSpecClass = env->GetObjectClass(jTableSpec);
		jGetColumnTypeMethodId = env->GetMethodID(jTableSpecClass, "getColumnType", "(J)Lcom/tightdb/ColumnType;");
		if (jGetColumnTypeMethodId == NULL) {
			jclass jNoSuchMethodClass = env->FindClass("java/lang/NoSuchMethodException");
			env->ThrowNew(jNoSuchMethodClass, "Method 'getColumnType' could not be located in class com.tightdb.TableSpec");
			return NULL;
		}
	}
	return env->CallObjectMethod(jTableSpec, jGetColumnTypeMethodId, columnIndex);
}

jstring Java_com_tightdb_TableSpec_getColumnName(JNIEnv* env, jobject jTableSpec, jlong columnIndex)
{
	static jmethodID jGetColumnNameMethodId = NULL;
	if (jGetColumnNameMethodId == NULL) {
		jclass jTableSpecClass = env->GetObjectClass(jTableSpec);
		jGetColumnNameMethodId = env->GetMethodID(jTableSpecClass, "getColumnName", "(J)Ljava/lang/String;");
		if (jGetColumnNameMethodId == NULL) {
			jclass jNoSuchMethodClass = env->FindClass("java/lang/NoSuchMethodException");
			env->ThrowNew(jNoSuchMethodClass, "Method 'getColumnName' could not be located in class com.tightdb.TableSpec");
			return NULL; 
		}
	}
	return (jstring)env->CallObjectMethod(jTableSpec, jGetColumnNameMethodId, columnIndex);
}

jobject Java_com_tightdb_TableSpec_getTableSpec(JNIEnv* env, jobject jTableSpec, jlong columnIndex)
{
	static jmethodID jGetTableSpecMethodId = NULL;
	if (jGetTableSpecMethodId == NULL) {
		jclass jTableSpecClass = env->GetObjectClass(jTableSpec);
		jGetTableSpecMethodId = env->GetMethodID(jTableSpecClass, "getSubtableSpec", "(J)Lcom/tightdb/TableSpec;");
		if (jGetTableSpecMethodId == NULL) {
			jclass jNoSuchMethodClass = env->FindClass("java/lang/NoSuchMethodException");
			env->ThrowNew(jNoSuchMethodClass, "Method 'getSubtableSpec' could not be located in class com.tightdb.TableSpec");
			return NULL;
		}
	}
	return env->CallObjectMethod(jTableSpec, jGetTableSpecMethodId, columnIndex);
}

jlong Java_com_tightdb_TableSpec_getColumnIndex(JNIEnv* env, jobject jTableSpec, jstring columnName)
{
	static jmethodID jGetColumnIndexMethodId = NULL;
	if (jGetColumnIndexMethodId == NULL) {
		jclass jTableSpecClass = env->GetObjectClass(jTableSpec);
		jGetColumnIndexMethodId = env->GetMethodID(jTableSpecClass, "getColumnIndex", "(Ljava/lang/String;)J");
		if (jGetColumnIndexMethodId == NULL) {
			jclass jNoSuchMethodClass = env->FindClass("java/lang/NoSuchMethodException");
			env->ThrowNew(jNoSuchMethodClass, "Method 'getColumnIndex' could not be located in class com.tightdb.TableSpec");
			return -1;
		}

	}
	return env->CallLongMethod(jTableSpec, jGetColumnIndexMethodId, columnName);
}

void updateSpecFromJSpec(JNIEnv* env, Spec& spec, jobject jTableSpec)
{
	jlong columnCount = Java_com_tightdb_TableSpec_getColumnCount(env, jTableSpec);
	for(jlong i=0; i<columnCount; i++) {
		jstring jColumnName = Java_com_tightdb_TableSpec_getColumnName(env, jTableSpec, i);
		const char* columnNameCharPtr = env->GetStringUTFChars(jColumnName, NULL);
		jobject jColumnType = Java_com_tightdb_TableSpec_getColumnType(env, jTableSpec, i);
		ColumnType columnType = GetColumnTypeFromJColumnType(env, jColumnType);
		if (columnType != COLUMN_TYPE_TABLE) {
			spec.add_column(columnType, columnNameCharPtr);
		}else{
			Spec nextColumnTableSpec = spec.add_subtable_column(columnNameCharPtr);
			jobject jNextColumnTableSpec = Java_com_tightdb_TableSpec_getTableSpec(env, jTableSpec, i);
			updateSpecFromJSpec(env, nextColumnTableSpec, jNextColumnTableSpec);
		}
		env->ReleaseStringUTFChars(jColumnName, columnNameCharPtr);
	}
}

void UpdateJTableSpecFromSpec(JNIEnv* env, const Spec& spec, jobject jTableSpec)
{
	size_t columnCount = spec.get_column_count();
	static jmethodID jAddColumnMethodId = NULL;
	static jmethodID jAddSubtableColumnMethodId = NULL;
	if (jAddColumnMethodId == NULL || jAddSubtableColumnMethodId == NULL) {
		jclass jTableSpecClass = env->FindClass("com/tightdb/TableSpec");
		if (jTableSpecClass == NULL) {
			jclass jClassNotFoundException = env->FindClass("java/lang/ClassNotFoundException");
			env->ThrowNew(jClassNotFoundException, "com.tightdb.TableSpec class not found");
			return;
		}
		jAddColumnMethodId = env->GetMethodID(jTableSpecClass, "addColumn", "(ILjava/lang/String;)V");
		if (jAddColumnMethodId == NULL) {
			jclass jNoSuchMethodClass = env->FindClass("java/lang/NoSuchMethodException");
			env->ThrowNew(jNoSuchMethodClass, "Method 'addColumn' could not be located in class com.tightdb.TableSpec");
			return;
		}
		jAddSubtableColumnMethodId = env->GetMethodID(jTableSpecClass, "addSubtableColumn", "(Ljava/lang/String;)Lcom/tightdb/TableSpec;");
		if (jAddSubtableColumnMethodId == NULL) {
			jclass jNoSuchMethodClass = env->FindClass("java/lang/NoSuchMethodException");
			env->ThrowNew(jNoSuchMethodClass, "Method 'addSubtableColumn' could not be located in class com.tightdb.TableSpec");
			return;
		}
	}
	for(size_t i=0; i<columnCount; i++) {
		ColumnType colType = spec.get_column_type(i);
		const char* colName = spec.get_column_name(i);
		if (colType == COLUMN_TYPE_TABLE) {
			jobject jSubTableSpec = env->CallObjectMethod(jTableSpec, jAddSubtableColumnMethodId, env->NewStringUTF(colName));
			const Spec& subTableSpec = spec.get_subspec(i);
			UpdateJTableSpecFromSpec(env, subTableSpec, jSubTableSpec);
		}else{
			env->CallVoidMethod(jTableSpec, jAddColumnMethodId, static_cast<jint>(colType), env->NewStringUTF(colName));
		}
	}
}