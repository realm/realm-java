/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "util.hpp"
#include "mixedutil.hpp"
#include "columntypeutil.hpp"

using namespace realm;

jclass GetClassMixed(JNIEnv* env)
{
    static jclass jMixedClass = GetClass(env, "io/realm/internal/Mixed");
    return jMixedClass;
}

jmethodID GetMixedMethodID(JNIEnv* env, const char* methodStr, const char* typeStr)
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

DataType GetMixedObjectType(JNIEnv* env, jobject jMixed)
{
    // Call Java "Mixed.getType"
    static jmethodID jGetTypeMethodId = GetMixedMethodID(env, "getType", "()Lio/realm/RealmFieldType;");
    if (jGetTypeMethodId == NULL)
        return DataType(0);

    // ???TODO optimize
    jobject jColumnType = env->CallObjectMethod(jMixed, jGetTypeMethodId);
    return static_cast<DataType>(GetColumnTypeFromJColumnType(env, jColumnType));
}

jobject CreateJMixedFromMixed(JNIEnv* env, Mixed& mixed)
{
    jclass jMixedClass = GetClassMixed(env);
    if (jMixedClass == NULL)
        return NULL;

    TR("CreateJMixedFromMixed(type %d)", mixed.get_type())
    switch (mixed.get_type()) {
    case type_Int:
    {
        jmethodID consId = GetMixedMethodID(env, "<init>", "(J)V");
        if (consId)
            return env->NewObject(jMixedClass, consId, mixed.get_int());
    }
    case type_Float:
    {
        jmethodID consId = GetMixedMethodID(env, "<init>", "(F)V");
        if (consId)
            return env->NewObject(jMixedClass, consId, mixed.get_float());
    }
    case type_Double:
    {
        jmethodID consId = GetMixedMethodID(env, "<init>", "(D)V");
        if (consId)
            return env->NewObject(jMixedClass, consId, mixed.get_double());
    }
    case type_String:
    {
        jmethodID consId = GetMixedMethodID(env, "<init>", "(Ljava/lang/String;)V");
        if (consId)
            return env->NewObject(jMixedClass, consId, to_jstring(env, mixed.get_string()));
    }
    case type_Bool:
    {
        jmethodID consId = GetMixedMethodID(env, "<init>", "(Z)V");
        if (consId)
            return env->NewObject(jMixedClass, consId, mixed.get_bool());
    }
    case type_DateTime:
        {
            time_t timeValue = mixed.get_datetime().get_datetime();
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
    case type_Binary:
        {
            BinaryData binaryData = mixed.get_binary();
            jmethodID consId = GetMixedMethodID(env, "<init>", "(Ljava/nio/ByteBuffer;)V");
            if (consId) {
                jobject jByteBuffer = env->NewDirectByteBuffer(const_cast<char*>(binaryData.data()), binaryData.size());
                return env->NewObject(jMixedClass, consId, jByteBuffer);
            }
        }
    case type_Table:
        {
            // param input: Table* t.
            TR("   --Mixed(type_Table)")
            jmethodID consId = GetMixedMethodID(env, "<init>", "(Lio/realm/RealmFieldType;)V");

            jobject jColumnType = NULL; // GetJColumnTypeFromColumnType(env, type_Table);
            if (consId)
                return env->NewObject(jMixedClass, consId, jColumnType);
        }
    case type_Mixed:
        break;
    case type_Link:
        break;
    case type_LinkList:
        break;
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

jfloat GetMixedFloatValue(JNIEnv* env, jobject jMixed)
{
    static jmethodID jGetFloatValueMethodId = GetMixedMethodID(env, "getFloatValue", "()F");

    if (jGetFloatValueMethodId)
        return env->CallFloatMethod(jMixed, jGetFloatValueMethodId);
    return 0;
}

jdouble GetMixedDoubleValue(JNIEnv* env, jobject jMixed)
{
    static jmethodID jGetDoubleValueMethodId = GetMixedMethodID(env, "getDoubleValue", "()D");

    if (jGetDoubleValueMethodId)
        return env->CallDoubleMethod(jMixed, jGetDoubleValueMethodId);
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
