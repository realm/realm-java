#include "mixedutil.h"

ColumnType GetMixedObjectType(JNIEnv* env, jobject jMixed){
	jclass jMixedClass = env->GetObjectClass(jMixed);
	jmethodID jGetTypeMethodId = env->GetMethodID(jMixedClass, "getType", "()Lcom/tightdb/ColumnType;");
	jobject jColumnType = env->CallObjectMethod(jMixed, jGetTypeMethodId);
	jclass jColumnTypeClass = env->GetObjectClass(jColumnType);
	jfieldID columnTypeIndexFieldId = env->GetFieldID(jColumnTypeClass, "index", "I");
	jint index = env->GetIntField(jColumnType, columnTypeIndexFieldId);
	return static_cast<ColumnType>(index);
}

jlong GetMixedIntValue(JNIEnv* env, jobject jMixed){
	jclass jMixedClass = env->GetObjectClass(jMixed);
	jmethodID jGetLongValueMethodId = env->GetMethodID(jMixedClass, "getLongValue", "()J");
	jlong retValue = env->CallLongMethod(jMixed, jGetLongValueMethodId);
	return retValue;
}

jstring GetMixedStringValue(JNIEnv* env, jobject jMixed){
	jclass jMixedClass = env->GetObjectClass(jMixed);
	jmethodID jGetStringValueMethodId = env->GetMethodID(jMixedClass, "getStringValue", "()Ljava/lang/String;");
	jstring retValue = (jstring)(env->CallObjectMethod(jMixed, jGetStringValueMethodId));
	return retValue;
}

jboolean GetMixedBooleanValue(JNIEnv* env, jobject jMixed){
	jclass jMixedClass = env->GetObjectClass(jMixed);
	jmethodID jGetBoolValueMethodId = env->GetMethodID(jMixedClass, "getBooleanValue", "()Z");
	jboolean retValue = env->CallBooleanMethod(jMixed, jGetBoolValueMethodId);
	return retValue;
}