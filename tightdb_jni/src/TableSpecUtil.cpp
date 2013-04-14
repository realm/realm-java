#include "util.h"
#include "TableSpecUtil.h"
#include "columntypeutil.h"

using namespace tightdb;

jclass GetClassTableSpec(JNIEnv* env) 
{
	static jclass myClass = GetClass(env, "com/tightdb/TableSpec");
	return myClass;
}

jmethodID GetTableSpecMethodID(JNIEnv* env, const char* methodStr, const char* typeStr)
{
    jclass myClass = GetClassTableSpec(env);
    if (myClass == NULL) {
        return NULL;
    }
    jmethodID myMethod = env->GetMethodID(myClass, methodStr, typeStr);
    if (myMethod == NULL) {
        ThrowException(env, NoSuchMethod, "TableSpec", methodStr);
        return NULL;
    }
    return myMethod;
}

jlong Java_com_tightdb_TableSpec_getColumnCount(JNIEnv* env, jobject jTableSpec)
{
	static jmethodID jGetColumnCountMethodId = GetTableSpecMethodID(env, "getColumnCount", "()J");
	if (jGetColumnCountMethodId)
	    return env->CallLongMethod(jTableSpec, jGetColumnCountMethodId);
    return 0;
}

jobject Java_com_tightdb_TableSpec_getColumnType(JNIEnv* env, jobject jTableSpec, jlong columnIndex)
{
	static jmethodID jGetColumnTypeMethodId = GetTableSpecMethodID(env, "getColumnType", "(J)Lcom/tightdb/ColumnType;");
	if (jGetColumnTypeMethodId)
        return env->CallObjectMethod(jTableSpec, jGetColumnTypeMethodId, columnIndex);
    return NULL;
}

jstring Java_com_tightdb_TableSpec_getColumnName(JNIEnv* env, jobject jTableSpec, jlong columnIndex)
{
	static jmethodID jGetColumnNameMethodId = GetTableSpecMethodID(env, "getColumnName", "(J)Ljava/lang/String;");
	if (jGetColumnNameMethodId)
    	return (jstring)env->CallObjectMethod(jTableSpec, jGetColumnNameMethodId, columnIndex);
	return NULL; 
}

jobject Java_com_tightdb_TableSpec_getTableSpec(JNIEnv* env, jobject jTableSpec, jlong columnIndex)
{
	static jmethodID jGetTableSpecMethodId = GetTableSpecMethodID(env, "getSubtableSpec", "(J)Lcom/tightdb/TableSpec;");
	if (jGetTableSpecMethodId)
        return env->CallObjectMethod(jTableSpec, jGetTableSpecMethodId, columnIndex);
    return NULL;
}

jlong Java_com_tightdb_TableSpec_getColumnIndex(JNIEnv* env, jobject jTableSpec, jstring columnName)
{
	static jmethodID jGetColumnIndexMethodId = GetTableSpecMethodID(env, "getColumnIndex", "(Ljava/lang/String;)J");
	if (jGetColumnIndexMethodId)
	    return env->CallLongMethod(jTableSpec, jGetColumnIndexMethodId, columnName);
    return 0;
}

void updateSpecFromJSpec(JNIEnv* env, Spec& spec, jobject jTableSpec)
{
    jlong columnCount = Java_com_tightdb_TableSpec_getColumnCount(env, jTableSpec);
    for (jlong i = 0; i < columnCount; ++i) {
        jstring jColumnName = Java_com_tightdb_TableSpec_getColumnName(env, jTableSpec, i);
        JStringAccessor columnName(env, jColumnName);
        if (!columnName) 
            return;

        jobject jColumnType   = Java_com_tightdb_TableSpec_getColumnType(env, jTableSpec, i);
        DataType columnType = GetColumnTypeFromJColumnType(env, jColumnType);
        if (columnType != type_Table) {
            spec.add_column(columnType, columnName);
        }
        else {
            Spec nextColumnTableSpec = spec.add_subtable_column(columnName);
            jobject jNextColumnTableSpec = Java_com_tightdb_TableSpec_getTableSpec(env, jTableSpec, i);
            updateSpecFromJSpec(env, nextColumnTableSpec, jNextColumnTableSpec);
        }
    }
}

void UpdateJTableSpecFromSpec(JNIEnv* env, const Spec& spec, jobject jTableSpec)
{
	static jmethodID jAddColumnMethodId = GetTableSpecMethodID(env, "addColumn", "(ILjava/lang/String;)V");
	static jmethodID jAddSubtableColumnMethodId = GetTableSpecMethodID(env, "addSubtableColumn", "(Ljava/lang/String;)Lcom/tightdb/TableSpec;");
	
    if (jAddColumnMethodId == NULL || jAddSubtableColumnMethodId == NULL) {
		return;
	}

	size_t columnCount = spec.get_column_count();
	for(size_t i = 0; i < columnCount; ++i) {
		DataType colType = spec.get_column_type(i);
		StringData colName = spec.get_column_name(i);
		if (colType == type_Table) {
			jobject jSubTableSpec = env->CallObjectMethod(jTableSpec, jAddSubtableColumnMethodId, to_jstring(env, colName));
			const Spec& subTableSpec = spec.get_subtable_spec(i);
			UpdateJTableSpecFromSpec(env, subTableSpec, jSubTableSpec);
		} else {
			env->CallVoidMethod(jTableSpec, jAddColumnMethodId, static_cast<jint>(colType), to_jstring(env, colName));
		}
	}
}
