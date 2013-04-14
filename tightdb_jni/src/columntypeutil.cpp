#include "util.hpp"
#include "columntypeutil.hpp"


static jfieldID GetFieldIDColumnType(JNIEnv* env, const char* methodStr, const char* typeStr)
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

DataType GetColumnTypeFromJColumnType(JNIEnv* env, jobject jColumnType)
{
    static jfieldID jValueFieldId = GetFieldIDColumnType(env, "nativeValue", "I");
    if (jValueFieldId == NULL)
        return DataType(0);

    jint columnType = env->GetIntField(jColumnType, jValueFieldId);
    return static_cast<DataType>(columnType);
}

jobject GetJColumnTypeFromColumnType(JNIEnv* env, DataType columnType)
{
    TR((env, "jni: Enter GetJColumnTypeFromColumnType(%d)\n", columnType));
    static jclass jColumnTypeClass = GetClass(env, "com/tightdb/ColumnType");

    if (jColumnTypeClass == NULL) {
        TR((env, "--class is NULL\n"));
        return NULL;
    }
    TR((env, "---2\n"));

    // Couldn't figure out how to create a new enum on Java side and return as object...
    // A workaround in java to not check for the correct ColumnTypeTable works.
    /*
    jmethodID jColumnTypeConsId2 = env->GetMethodID(jColumnTypeClass, "<init>", "()V");
    if (jColumnTypeConsId2) {
        TR((env, "-GOT INIT\n"));
        return NULL;
    }
    */

   /*
    jfieldID subtable_id = env->GetStaticFieldID(jColumnTypeClass, "ColumnTypeTable", "LColumnType;");
    if (!subtable_id) {
        TR((env, "--subtable_id is NULL\n"));
        return NULL;
    }

    jobject jColumnTypeConsId = env->GetStaticObjectField(jColumnTypeClass, subtable_id);
    if (jColumnTypeConsId == NULL) {
        TR((env, "---2.5"));
        ThrowException(env, NoSuchMethod, "ColumnType", "<init>");
        return NULL;
    }
    return jColumnTypeConsId;
    */
    TR((env, "---3\n"));
    return NULL;
    //jobject jColumnType = env->NewObject(jColumnTypeClass, jColumnTypeConsId,
                                       //  static_cast<jint>(columnType));
    //jobject jColumnType = env->NewObject(jColumnTypeClass, jColumnTypeConsId);

    //TR((env, "jni: New ColumnType %d.\n", columnType));
    //return jColumnType;

}
