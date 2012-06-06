#include "util.h"
#include "columntypeutil.h"


ColumnType GetColumnTypeFromJColumnType(JNIEnv* env, jobject jColumnType)
{
	static jfieldID jIndexFieldId = NULL;
	if (jIndexFieldId == NULL) {
		jclass jColumnTypeClass = env->GetObjectClass(jColumnType);
		jIndexFieldId = env->GetFieldID(jColumnTypeClass, "index", "I");
		if (jIndexFieldId == NULL) {
            ThrowException(env, NoSuchField, "index", "ColumnType");
			return static_cast<ColumnType>(-1);
		}
	}
	jint columnType = env->GetIntField(jColumnType, jIndexFieldId);
	return static_cast<ColumnType>(columnType);
}

jobject GetJColumnTypeFromColumnType(JNIEnv* env, ColumnType columnType)
{
	return NULL;
}
