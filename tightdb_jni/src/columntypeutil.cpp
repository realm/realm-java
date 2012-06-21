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
    TR("jni: Enter GetJColumnTypeFromColumnType(%d)\n", columnType);
	static jclass jColumnTypeClass = GetClass(env, "com/tightdb/ColumnType");

	if (jColumnTypeClass == NULL) {
	    TR("--class is NULL\n");
        return NULL;
    }
    TR("---2\n");
    
    // Couldn't figure out how to create a new enum on Java side and return as object...
    // A workaround in java to not check for the correct ColumnTypeTable works.
    /*
	jmethodID jColumnTypeConsId2 = env->GetMethodID(jColumnTypeClass, "<init>", "()V");
    if (jColumnTypeConsId2) {
        TR("-GOT INIT\n");
        return NULL;
    }
    */

   /*
    jfieldID subtable_id = env->GetStaticFieldID(jColumnTypeClass, "ColumnTypeTable", "LColumnType;");
	if (!subtable_id) {
        TR("--subtable_id is NULL\n");
        return NULL;
    }
    
    jobject jColumnTypeConsId = env->GetStaticObjectField(jColumnTypeClass, subtable_id);
    if (jColumnTypeConsId == NULL) {
        TR("---2.5");
        ThrowException(env, NoSuchMethod, "ColumnType", "<init>");
    	return NULL;
	}
    return jColumnTypeConsId; 
    */
    TR("---3\n");
    return NULL;
	//jobject jColumnType = env->NewObject(jColumnTypeClass, jColumnTypeConsId, 
                                       //  static_cast<jint>(columnType));
    //jobject jColumnType = env->NewObject(jColumnTypeClass, jColumnTypeConsId);
                                         
    //TR("jni: New ColumnType %d.\n", columnType);
	//return jColumnType;
    
}
