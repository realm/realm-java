#include "util.h"
#include "mixedutil.h"

using namespace tightdb;

jclass GetMixedClass(JNIEnv* env) 
{
	static jobject jMixedClass = NULL;

	if (jMixedClass == NULL) {
		jclass jMixedClassLocal = env->FindClass("com/tightdb/Mixed");
		if (jMixedClassLocal == NULL) {
			jclass jClassNotFoundExceptionClass = env->FindClass("java/lang/ClassNotFoundException");
			env->ThrowNew(jClassNotFoundExceptionClass, "could not load class 'com.tightdb.Mixed'");
			return NULL;
		}
		jMixedClass = env->NewGlobalRef(jMixedClassLocal);
	}
	return (jclass)jMixedClass;
}

jmethodID GetMixedMethodID(JNIEnv* env, char* methodStr, char* typeStr)
{
    jclass myClass = GetMixedClass(env);
    if (myClass == NULL) {
    
        return NULL;
    }
    jmethodID myMethod = env->GetMethodID(myClass, methodStr, typeStr);
    if (myMethod == NULL) {
        ThrowException(env, NoSuchMethod, methodStr, "mixed");
        return NULL;
    }
    return myMethod;
}

// ??? TODO: Use above to reduce code below:


ColumnType GetMixedObjectType(JNIEnv* env, jobject jMixed)
{
	static jmethodID jGetTypeMethodId = NULL;
	
    if (jGetTypeMethodId == NULL) {
        jGetTypeMethodId = GetMixedMethodID(env, "getType", "()Lcom/tightdb/ColumnType;");
		if (jGetTypeMethodId == NULL) {
			return static_cast<ColumnType>(-1);
		}
	}
	jobject jColumnType = env->CallObjectMethod(jMixed, jGetTypeMethodId);

	static jfieldID columnTypeIndexFieldId = NULL;
	if (columnTypeIndexFieldId == NULL) {
		jclass jColumnTypeClass = env->FindClass("com/tightdb/ColumnType");
		columnTypeIndexFieldId = env->GetFieldID(jColumnTypeClass, "index", "I");
		if (columnTypeIndexFieldId == NULL) {
			jclass jNoSuchMethodExcetionClass = env->FindClass("com/tightdb/NoSuchFieldException");
			env->ThrowNew(jNoSuchMethodExcetionClass, "could not locate field 'index' in class 'com.tightdb.mixed'");
			return static_cast<ColumnType>(-1);
		}
	}
	jint index = env->GetIntField(jColumnType, columnTypeIndexFieldId);
	return static_cast<ColumnType>(index);
}

jobject CreateJMixedFromMixed(JNIEnv* env, Mixed& mixed) 
{
	jclass jMixedClass = GetMixedClass(env);
	jmethodID consId = NULL;
	switch (mixed.get_type()) {
	case COLUMN_TYPE_INT:
	{
		consId = env->GetMethodID(jMixedClass, "<init>", "(J)V");
		if (consId == NULL) {
			jclass jNoSuchMethodExcetionClass = env->FindClass("com/tightdb/NoSuchMethodException");
			env->ThrowNew(jNoSuchMethodExcetionClass, "could not locate constructor Mixed(long) in class 'com.tightdb.mixed'");
			return NULL;
		}
		jlong value = mixed.get_int();
		return env->NewObject(jMixedClass, consId, mixed.get_int());
	}
	case COLUMN_TYPE_STRING:
	{
		consId = env->GetMethodID(jMixedClass, "<init>", "(Ljava/lang/String;)V");
		if (consId == NULL) {
			jclass jNoSuchMethodExcetionClass = env->FindClass("com/tightdb/NoSuchMethodException");
			env->ThrowNew(jNoSuchMethodExcetionClass, "could not locate constructor Mixed(String) in class 'com.tightdb.mixed'");
			return NULL;
		}
		const char* valueCharPtr = mixed.get_string();
		return env->NewObject(jMixedClass, consId, env->NewStringUTF(valueCharPtr));
	}
	case COLUMN_TYPE_BOOL:
	{
		consId = env->GetMethodID(jMixedClass, "<init>", "(Z)V");
		if (consId == NULL) {
			jclass jNoSuchMethodExcetionClass = env->FindClass("com/tightdb/NoSuchMethodException");
			env->ThrowNew(jNoSuchMethodExcetionClass, "could not locate constructor Mixed(boolean) in class 'com.tightdb.mixed'");
			return NULL;
		}
		return env->NewObject(jMixedClass, consId, mixed.get_bool());
	}
	case COLUMN_TYPE_DATE:
		{
			time_t timeValue = mixed.get_date();
			jclass jDateClass = env->FindClass("java/util/Date");
			if (jDateClass == NULL) {
				jclass jClassNotFoundException = env->FindClass("java/lang/ClassNotFoundException");
				env->ThrowNew(jClassNotFoundException, "Could not locate class 'java/util/Date'");
				return NULL;
			}
			jmethodID jDateConsId = env->GetMethodID(jDateClass, "<init>", "(J)V");
			if (jDateConsId == NULL) {
				jclass jNoSuchMethodException = env->FindClass("java/lang/NoSuchMethodException");
				env->ThrowNew(jNoSuchMethodException, "Could not locate constructor Date(long) in class java.util.Date'");
				return NULL;
			}
			jobject jDate = env->NewObject(jDateClass, jDateConsId, static_cast<jlong>(timeValue));
			consId = env->GetMethodID(jMixedClass, "<init>", "(Ljava/util/Date;)V");
			if (consId == NULL) {
				jclass jNoSuchMethodExcetionClass = env->FindClass("com/tightdb/NoSuchMethodException");
				env->ThrowNew(jNoSuchMethodExcetionClass, "could not locate constructor Mixed(java.util.Date) in class 'com.tightdb.Mixed'");
				return NULL;
			}
			return env->NewObject(jMixedClass, consId, jDate);
		}
	case COLUMN_TYPE_BINARY:
		{
			BinaryData binaryData = mixed.get_binary();
			consId = env->GetMethodID(jMixedClass, "<init>", "(Ljava/nio/ByteBuffer;)V");
			if (consId == NULL) {
				jclass jNoSuchMethodExcetionClass = env->FindClass("com/tightdb/NoSuchMethodException");
				env->ThrowNew(jNoSuchMethodExcetionClass, "could not locate constructor Mixed(byte[]) in class 'com.tightdb.Mixed'");
				return NULL;
			}
			jobject jByteBuffer = env->NewDirectByteBuffer((void*)binaryData.pointer, binaryData.len);
			
			return env->NewObject(jMixedClass, consId, jByteBuffer);
		}
	default:
		{
			printf("\nMixed type is not supported here");
			return NULL;
		}
	}
	return NULL;
}

jlong GetMixedIntValue(JNIEnv* env, jobject jMixed) 
{
	static jmethodID jGetLongValueMethodId = NULL;

	if (jGetLongValueMethodId == NULL) {
		jclass jMixedClass = GetMixedClass(env);
		jGetLongValueMethodId = env->GetMethodID(jMixedClass, "getLongValue", "()J");
		if (jGetLongValueMethodId == NULL) {
			jclass jNoSuchMethodExceptionClass = env->FindClass("java/lang/NoSuchMethodException");
			env->ThrowNew(jNoSuchMethodExceptionClass, "Could not locate method 'getLongValue' in class 'com.tightdb.Mixed'");
			return -1;
		}
	}
	jlong retValue = env->CallLongMethod(jMixed, jGetLongValueMethodId);
	return retValue;
}

jstring GetMixedStringValue(JNIEnv* env, jobject jMixed)
{
	static jmethodID jGetStringValueMethodId = NULL;

	if (jGetStringValueMethodId == NULL) {
		jclass jMixedClass = GetMixedClass(env);
		jGetStringValueMethodId = env->GetMethodID(jMixedClass, "getStringValue", "()Ljava/lang/String;");
		if (jGetStringValueMethodId == NULL) {
			jclass jNoSuchMethodExceptionClass = env->FindClass("java/lang/NoSuchMethodException");
			env->ThrowNew(jNoSuchMethodExceptionClass, "Could not locate method 'getStringValue' in class 'com.tightdb.Mixed'");
			return NULL;
		}
	}
	jstring retValue = (jstring)(env->CallObjectMethod(jMixed, jGetStringValueMethodId));
	return retValue;
}

jboolean GetMixedBooleanValue(JNIEnv* env, jobject jMixed)
{
	static jmethodID jGetBoolValueMethodId = NULL;

	if (jGetBoolValueMethodId == NULL) {
		jclass jMixedClass = GetMixedClass(env);
		jGetBoolValueMethodId = env->GetMethodID(jMixedClass, "getBooleanValue", "()Z");
		if (jGetBoolValueMethodId == NULL) {
            ThrowException(env, NoSuchMethod, "getBooleanValue", "Mixed");
            return NULL;
		}
	}
	jboolean retValue = env->CallBooleanMethod(jMixed, jGetBoolValueMethodId);
	return retValue;
}

jbyteArray GetMixedByteArrayValue(JNIEnv* env, jobject jMixed)
{
	static jmethodID jGetBinaryDataMethodId = NULL;

	if (jGetBinaryDataMethodId == NULL) {
		jclass jMixedClass = GetMixedClass(env);
		jGetBinaryDataMethodId = env->GetMethodID(jMixedClass, "getByteArray", "()[B");
		if (jGetBinaryDataMethodId == NULL) {
            ThrowException(env, NoSuchMethod, "getByteArray", "Mixed");
			return NULL;
		}
	}
	jbyteArray retValue = reinterpret_cast<jbyteArray>(env->CallObjectMethod(jMixed, jGetBinaryDataMethodId));
	return retValue;
}


jlong GetMixedDateTimeValue(JNIEnv* env, jobject jMixed)
{
	static jmethodID jGetDateTimeMethodId = NULL;

	if (jGetDateTimeMethodId == NULL) {
		jclass jMixedClass = GetMixedClass(env);
		jGetDateTimeMethodId = env->GetMethodID(jMixedClass, "getDateTimeValue", "()J");
		if (jGetDateTimeMethodId == NULL) {
			ThrowException(env, NoSuchMethod, "getDateTimeValue", "Mixed");
            return NULL;
		}
	}
	return env->CallLongMethod(jMixed, jGetDateTimeMethodId);
}

jobject GetMixedByteBufferValue(JNIEnv* env, jobject jMixed)
{
	static jmethodID jGetBinaryValueMethodId = NULL;

	if (jGetBinaryValueMethodId == NULL) {
		jclass jMixedClass = GetMixedClass(env);
		jGetBinaryValueMethodId = env->GetMethodID(jMixedClass, "getBinaryValue", "()Ljava/nio/ByteBuffer;");
		if (jGetBinaryValueMethodId == NULL) {
            ThrowException(env, NoSuchMethod, "getBinaryValue", "Mixed");
			return NULL;
		}
	}
	return env->CallObjectMethod(jMixed, jGetBinaryValueMethodId);
}

jint GetMixedBinaryType(JNIEnv* env, jobject jMixed) 
{
	static jmethodID jGetBinaryTypeMethodId = NULL;

	if (jGetBinaryTypeMethodId == NULL) {
		jclass jMixedClass = GetMixedClass(env);
		jGetBinaryTypeMethodId = env->GetMethodID(jMixedClass, "getBinaryType", "()I");
		if (jGetBinaryTypeMethodId == NULL) {
            ThrowException(env, NoSuchMethod, "getBinaryType", "Mixed");
			return NULL;
		}
	}
	return env->CallIntMethod(jMixed, jGetBinaryTypeMethodId); 
}