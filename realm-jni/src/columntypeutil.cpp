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
#include "columntypeutil.hpp"


static jfieldID GetFieldIDColumnType(JNIEnv* env, const char* methodStr, const char* typeStr)
{
    static jclass myClass = GetClass(env, "io/realm/internal/ColumnType");
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
    TR((LOG_DEBUG, log_tag, "enter GetJColumnTypeFromColumnType(%d)", columnType));
    static jclass jColumnTypeClass = GetClass(env, "io/realm/internal/ColumnType");

    if (jColumnTypeClass == NULL) {
        TR((LOG_DEBUG, log_tag, "--class is NULL"));
        return NULL;
    }
    TR((LOG_DEBUG, log_tag, "---2"));

    // Couldn't figure out how to create a new enum on Java side and return as object...
    // A workaround in java to not check for the correct ColumnTypeTable works.
    /*
    jmethodID jColumnTypeConsId2 = env->GetMethodID(jColumnTypeClass, "<init>", "()V");
    if (jColumnTypeConsId2) {
        TR((env, "-GOT INIT"));
        return NULL;
    }
    */

   /*
    jfieldID subtable_id = env->GetStaticFieldID(jColumnTypeClass, "ColumnTypeTable", "LColumnType;");
    if (!subtable_id) {
        TR((env, "--subtable_id is NULL"));
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
    TR((LOG_DEBUG, log_tag, "---3"));
    return NULL;
    //jobject jColumnType = env->NewObject(jColumnTypeClass, jColumnTypeConsId,
                                       //  static_cast<jint>(columnType));
    //jobject jColumnType = env->NewObject(jColumnTypeClass, jColumnTypeConsId);

    //TR((env, "jni: New ColumnType %d.", columnType));
    //return jColumnType;

}
