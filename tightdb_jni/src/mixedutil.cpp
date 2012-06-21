#include "util.h"
#include "mixedutil.h"
#include "columntypeutil.h"

using namespace tightdb;

jclass GetClassMixed(JNIEnv* env) 
{
	static jclass jMixedClass = GetClass(env, "com/tightdb/Mixed");
	return jMixedClass;
}

jmethodID GetMixedMethodID(JNIEnv* env, char* methodStr, char* typeStr)
{
    jclass myClass = GetClassMixed(env);
    if (myClass == NULL)
        return NULL;

    jmethodID myMethod = env->GetMethodID(myClass, methodStr, typeStr);
    if (myMethod == NULL) {
        ThrowException(env, NoSuchMethod, "mixed", methodStr);
        return NULL;
    }
    return myMethod;
}

ColumnType GetMixedObjectType(JNIEnv* env, jobject jMixed)
{
    // Call Java "Mixed.getType"
	static jmethodID jGetTypeMethodId = GetMixedMethodID(env, "getType", "()Lcom/tightdb/ColumnType;");
    if (jGetTypeMethodId == NULL)
       	return static_cast<ColumnType>(-1);
    
    // ???TODO optimize
	jobject jColumnType = env->CallObjectMethod(jMixed, jGetTypeMethodId);
    return static_cast<ColumnType>(GetColumnTypeFromJColumnType(env, jColumnType));
}

jobject CreateJMixedFromMixed(JNIEnv* env, Mixed& mixed) 
{
	jclass jMixedClass = GetClassMixed(env);
    if (jMixedClass == NULL)
        return NULL;

    TR("CreateJMixedFromMixed(type %d)", mixed.get_type());
	switch (mixed.get_type()) {
	case COLUMN_TYPE_INT:
	{
		jmethodID consId = GetMixedMethodID(env, "<init>", "(J)V");
        if (consId)
		    return env->NewObject(jMixedClass, consId, mixed.get_int());
	}
	case COLUMN_TYPE_STRING:
	{
		jmethodID consId = GetMixedMethodID(env, "<init>", "(Ljava/lang/String;)V");
		if (consId)
		    return env->NewObject(jMixedClass, consId, env->NewStringUTF(mixed.get_string()));
	}
	case COLUMN_TYPE_BOOL:
	{
        jmethodID consId = GetMixedMethodID(env, "<init>", "(Z)V");
		if (consId)
			return env->NewObject(jMixedClass, consId, mixed.get_bool());
	}
	case COLUMN_TYPE_DATE:
		{
			time_t timeValue = mixed.get_date();
			jclass jDateClass = env->FindClass("java/util/Date");
			if (jDateClass == NULL) {
                ThrowException(env, ClassNotFound, "Date");
				return NULL;
			}
			jmethodID jDateConsId = env->GetMethodID(jDateClass, "<init>", "(J)V");
			if (jDateConsId == NULL) {
                ThrowException(env, NoSuchMethod, "Date", "<init>");
				return NULL;
			}
			jobject jDate = env->NewObject(jDateClass, jDateConsId, static_cast<jlong>(timeValue));
			jmethodID consId = GetMixedMethodID(env, "<init>", "(Ljava/util/Date;)V");
			if (consId)
			    return env->NewObject(jMixedClass, consId, jDate);
		}
	case COLUMN_TYPE_BINARY:
		{
			BinaryData binaryData = mixed.get_binary();
			jmethodID consId = GetMixedMethodID(env, "<init>", "(Ljava/nio/ByteBuffer;)V");
			if (consId) {
				jobject jByteBuffer = env->NewDirectByteBuffer((void*)binaryData.pointer, binaryData.len);
				return env->NewObject(jMixedClass, consId, jByteBuffer);
            }
		}
    case COLUMN_TYPE_TABLE:
        {
            TR("--Mixed(COLUMN_TYPE_TABLE)\n");
            jmethodID consId = GetMixedMethodID(env, "<init>", "(Lcom/tightdb/ColumnType;)V");

            jobject jColumnType = NULL; // GetJColumnTypeFromColumnType(env, COLUMN_TYPE_TABLE);
            if (consId) 
			    return env->NewObject(jMixedClass, consId, jColumnType);
        }
	default:
		{
	        ThrowException(env, IllegalArgument, "Invalid Mixed type.");
            TR("Mixed type is not supported: %d", mixed.get_type());
		}
	}
    
	return NULL;
}

jlong GetMixedIntValue(JNIEnv* env, jobject jMixed) 
{
	static jmethodID jGetLongValueMethodId = GetMixedMethodID(env, "getLongValue", "()J");

    if (jGetLongValueMethodId)
	    return env->CallLongMethod(jMixed, jGetLongValueMethodId);
	return 0;
}

jstring GetMixedStringValue(JNIEnv* env, jobject jMixed)
{
	static jmethodID jGetStringValueMethodId = GetMixedMethodID(env, "getStringValue", "()Ljava/lang/String;");;

	if (jGetStringValueMethodId) 
		return (jstring)(env->CallObjectMethod(jMixed, jGetStringValueMethodId));
	return 0;
}

jboolean GetMixedBooleanValue(JNIEnv* env, jobject jMixed)
{
	static jmethodID jGetBoolValueMethodId = GetMixedMethodID(env, "getBooleanValue", "()Z");
    
    if (jGetBoolValueMethodId)
        return env->CallBooleanMethod(jMixed, jGetBoolValueMethodId);
	return 0;
}

jbyteArray GetMixedByteArrayValue(JNIEnv* env, jobject jMixed)
{
	static jmethodID jGetBinaryDataMethodId = GetMixedMethodID(env, "getBinaryByteArray", "()[B");

	if (jGetBinaryDataMethodId)
		return reinterpret_cast<jbyteArray>(env->CallObjectMethod(jMixed, jGetBinaryDataMethodId));
	return 0;
}

jlong GetMixedDateTimeValue(JNIEnv* env, jobject jMixed)
{
	static jmethodID jGetDateTimeMethodId = GetMixedMethodID(env, "getDateTimeValue", "()J");

	if (jGetDateTimeMethodId)
	    return env->CallLongMethod(jMixed, jGetDateTimeMethodId);
    return 0;
}

jobject GetMixedByteBufferValue(JNIEnv* env, jobject jMixed)
{
	static jmethodID jGetBinaryValueMethodId = GetMixedMethodID(env, "getBinaryValue", "()Ljava/nio/ByteBuffer;");

	if (jGetBinaryValueMethodId)
		return env->CallObjectMethod(jMixed, jGetBinaryValueMethodId);
    return 0;
}

jint GetMixedBinaryType(JNIEnv* env, jobject jMixed) 
{
	static jmethodID jGetBinaryTypeMethodId = GetMixedMethodID(env, "getBinaryType", "()I");

	if (jGetBinaryTypeMethodId)
	    return env->CallIntMethod(jMixed, jGetBinaryTypeMethodId);
    return 0;
}