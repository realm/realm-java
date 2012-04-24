#include "util.h"

jlong GetNativePtrValue(JNIEnv* env, jobject jobj){
	jfieldID nativePtrFieldId = env->GetFieldID(env->GetObjectClass(jobj), "nativePtr", "J");
	if(nativePtrFieldId == NULL){
		//printf("\nError fetching native ptr field: %s", __FUNCTION__);
		return 0;
	}
	return env->GetLongField(jobj, nativePtrFieldId);
}

void SetNativePtrValue(JNIEnv* env, jobject jobj, jlong value){
	jfieldID nativePtrFieldId = env->GetFieldID(env->GetObjectClass(jobj), "nativePtr", "J");
	if(nativePtrFieldId == NULL){
		//printf("\nError fetching native ptr field: %s", __FUNCTION__);
		return;
	}
	env->SetLongField(jobj, nativePtrFieldId, value);
}