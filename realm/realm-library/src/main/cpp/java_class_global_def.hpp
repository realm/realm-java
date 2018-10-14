/*
 * Copyright 2017 Realm Inc.
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

#ifndef REALM_JNI_IMPL_CLASS_GLOBAL_DEF_HPP
#define REALM_JNI_IMPL_CLASS_GLOBAL_DEF_HPP

#include "util.hpp"
#include "jni_util/java_class.hpp"
#include "jni_util/java_method.hpp"

#include <memory>

#include <realm/util/assert.hpp>

namespace realm {

class BinaryData;

namespace _impl {

// Manage a global static jclass pool which will be initialized when JNI_OnLoad() called.
// FindClass is a relatively slow operation, loading all the needed classes when start is not good since usually user
// will call Realm.init() when the app starts.
// Instead, we only load necessary classes including:
// 1. Common types which might be used everywhere. (Boxed types, String, etc.)
// 2. Classes which might be initialized in the native thread.
//
// FindClass will fail if it is called from a native thread (e.g.: the sync client thread.). But usually it is not a
// problem if the FindClass is called from an JNI method. So keeping a static JavaClass var locally is still preferred
// if it is possible.
class JavaClassGlobalDef {
private:
    JavaClassGlobalDef(JNIEnv* env)
        : m_java_lang_long(env, "java/lang/Long", false)
        , m_java_lang_float(env, "java/lang/Float", false)
        , m_java_lang_double(env, "java/lang/Double", false)
        , m_java_util_date(env, "java/util/Date", false)
        , m_java_lang_string(env, "java/lang/String", false)
        , m_java_lang_boolean(env, "java/lang/Boolean", false)
        , m_shared_realm_schema_change_callback(env, "io/realm/internal/OsSharedRealm$SchemaChangedCallback", false)
        , m_realm_notifier(env, "io/realm/internal/RealmNotifier", false)
    {
    }

    jni_util::JavaClass m_java_lang_long;
    jni_util::JavaClass m_java_lang_float;
    jni_util::JavaClass m_java_lang_double;
    jni_util::JavaClass m_java_util_date;
    jni_util::JavaClass m_java_lang_string;
    jni_util::JavaClass m_java_lang_boolean;

    jni_util::JavaClass m_shared_realm_schema_change_callback;
    jni_util::JavaClass m_realm_notifier;

    inline static std::unique_ptr<JavaClassGlobalDef>& instance()
    {
        static std::unique_ptr<JavaClassGlobalDef> instance;
        return instance;
    };

public:
    // Called in JNI_OnLoad
    static void initialize(JNIEnv* env)
    {
        REALM_ASSERT(!instance());
        instance().reset(new JavaClassGlobalDef(env));
    }
    // Called in JNI_OnUnload
    static void release()
    {
        REALM_ASSERT(instance());
        instance().release();
    }

    // java.lang.Long
    inline static jobject new_long(JNIEnv* env, int64_t value)
    {
        static jni_util::JavaMethod init(env, instance()->m_java_lang_long, "<init>", "(J)V");
        return env->NewObject(instance()->m_java_lang_long, init, value);
    }
    inline static const jni_util::JavaClass& java_lang_long()
    {
        return instance()->m_java_lang_long;
    }

    // java.lang.Float
    inline static jobject new_float(JNIEnv* env, float value)
    {
        static jni_util::JavaMethod init(env, instance()->m_java_lang_float, "<init>", "(F)V");
        return env->NewObject(instance()->m_java_lang_float, init, value);
    }
    inline static const jni_util::JavaClass& java_lang_float()
    {
        return instance()->m_java_lang_float;
    }

    // java.lang.Double
    inline static jobject new_double(JNIEnv* env, double value)
    {
        static jni_util::JavaMethod init(env, instance()->m_java_lang_double, "<init>", "(D)V");
        return env->NewObject(instance()->m_java_lang_double, init, value);
    }
    inline static const jni_util::JavaClass& java_lang_double()
    {
        return instance()->m_java_lang_double;
    }

    // java.lang.Boolean
    inline static jobject new_boolean(JNIEnv* env, bool value)
    {
        static jni_util::JavaMethod init(env, instance()->m_java_lang_boolean, "<init>", "(Z)V");
        return env->NewObject(instance()->m_java_lang_boolean, init, value ? JNI_TRUE : JNI_FALSE);
    }
    inline static const jni_util::JavaClass& java_lang_boolean()
    {
        return instance()->m_java_lang_boolean;
    }

    // java.util.Date
    // return nullptr if ts is null
    inline static jobject new_date(JNIEnv* env, const realm::Timestamp& ts)
    {
        if (ts.is_null()) {
            return nullptr;
        }
        static jni_util::JavaMethod init(env, instance()->m_java_util_date, "<init>", "(J)V");
        return env->NewObject(instance()->m_java_util_date, init, to_milliseconds(ts));
    }
    inline static const jni_util::JavaClass& java_util_date()
    {
        return instance()->m_java_util_date;
    }

    // java.util.String
    inline static const jni_util::JavaClass& java_lang_string()
    {
        return instance()->m_java_lang_string;
    }

    // byte[]
    // return nullptr if binary_data is null
    static jbyteArray new_byte_array(JNIEnv* env, const BinaryData& binary_data);

    // io.realm.internal.OsSharedRealm.SchemaChangedCallback
    inline static const jni_util::JavaClass& shared_realm_schema_change_callback()
    {
        return instance()->m_shared_realm_schema_change_callback;
    }

    // io.realm.internal.RealmNotifier
    inline static const jni_util::JavaClass& realm_notifier()
    {
        return instance()->m_realm_notifier;
    }
};

} // namespace realm
} // namespace jni_impl


#endif // REALM_JNI_IMPL_CLASS_GLOBAL_DEF_HPP
