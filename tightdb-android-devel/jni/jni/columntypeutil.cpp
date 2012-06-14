#include "util.h"
#include "columntypeutil.h"


static jfieldID GetFieldIDColumnType(JNIEnv* env, char* methodStr, char* typeStr)
{
    static jclass myClass = GetClass(env, "com/tightdb/ColumnType");
    if (myClass == NULL)
        return NULL;

    jfieldID myField = env->GetFieldID(myClass, methodStr, typeStr);
    if (myField== NULL) {
        ThrowException(env, NoSuchField, "ColumnType", methodStr);
        return NULL;
    }
    return myField;
}

ColumnType GetColumnTypeFromJColumnType(JNIEnv* env, jobject jColumnType)
{
	static jfieldID jIndexFieldId = GetFieldIDColumnType(env, "index", "I");
	if (jIndexFieldId == NULL)
        return static_cast<ColumnType>(-1);

    jint columnType = env->GetIntField(jColumnType, jIndexFieldId);
	return static_cast<ColumnType>(columnType);
}

jobject GetJColumnTypeFromColumnType(JNIEnv* env, ColumnType columnType)
{
	assert(false);
    return NULL;
}
