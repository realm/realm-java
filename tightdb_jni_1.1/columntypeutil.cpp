#include "columntypeutil.h"

ColumnType GetColumnTypeFromJColumnType(JNIEnv* env, jobject jColumnType){
	jclass jColumnTypeClass = env->GetObjectClass(jColumnType);
	jfieldID jIndexFieldId = env->GetFieldID(jColumnTypeClass, "index", "I");
	jint columnType = env->GetIntField(jColumnType, jIndexFieldId);
	return static_cast<ColumnType>(columnType);
}


jobject GetJColumnTypeFromColumnType(JNIEnv* env, ColumnType columnType){
	return NULL;
}