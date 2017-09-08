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

#ifndef REALM_JNI_IMPL_JAVA_ACCESSOR_HPP
#define REALM_JNI_IMPL_JAVA_ACCESSOR_HPP

#include <jni.h>

#include <vector>
#include <memory>

#include <realm/binary_data.hpp>

#include <util/format.hpp>

#include "java_exception_def.hpp"
#include "jni_util/java_exception_thrower.hpp"

// Utility classes for accessing Java objects from JNI
namespace realm {
namespace _impl {

template <typename, typename>
class JPrimitiveArrayAccessor;
typedef JPrimitiveArrayAccessor<jbyteArray, jbyte> JByteArrayAccessor;
typedef JPrimitiveArrayAccessor<jbooleanArray, jboolean> JBooleanArrayAccessor;
typedef JPrimitiveArrayAccessor<jlongArray, jlong> JLongArrayAccessor;

// JPrimitiveArrayAccessor and JObjectArrayAccessor are not supposed to be used across JNI borders. They won't acquire
// references of the original Java object. Thus, you have to ensure the original java object is available during the
// life cycle of those accessors. Moreover, some returned object like BinaryData and StringData, they don't own the
// memory they use. So the accessor has to be available during the life cycle of those returned objects.

// Accessor for Java primitive arrays
template <typename ArrayType, typename ElementType>
class JPrimitiveArrayAccessor {
public:
    JPrimitiveArrayAccessor(JNIEnv* env, ArrayType jarray)
        : m_size(jarray ? env->GetArrayLength(jarray) : 0)
        , m_elements_holder(std::make_shared<ElementsHolder>(env, jarray))
    {
        check_init(env);
    }
    ~JPrimitiveArrayAccessor() = default;

    JPrimitiveArrayAccessor(JPrimitiveArrayAccessor&&) = default;
    JPrimitiveArrayAccessor& operator=(JPrimitiveArrayAccessor&&) = default;
    JPrimitiveArrayAccessor(const JPrimitiveArrayAccessor&) = default;
    JPrimitiveArrayAccessor& operator=(const JPrimitiveArrayAccessor&) = default;

    inline bool is_null() {
        return !m_elements_holder->m_jarray;
    }

    inline jsize size() const noexcept
    {
        return m_size;
    }

    inline ElementType* data() const noexcept
    {
        return m_elements_holder->m_data_ptr;
    }

    inline const ElementType& operator[](const int index) const noexcept
    {
        return m_elements_holder->m_data_ptr[index];
    }

    // Converts the Java array into an instance of T. The returned value's life cycle may still rely on this accessor.
    // (e.g.: BinaryData/StringData)
    template <typename T>
    T transform();

private:
    // Holding the data returned by GetXxxArrayElements call.
    struct ElementsHolder {
        ElementsHolder(JNIEnv*, ArrayType);
        ~ElementsHolder();

        JNIEnv* m_env;
        const ArrayType m_jarray;
        ElementType* m_data_ptr;
        const jint m_release_mode = JNI_ABORT;
    };

    jsize m_size;
    // For enabling copy/move constructors. ReleaseXxxArrayElements should only be called once.
    std::shared_ptr<ElementsHolder> m_elements_holder;

    inline void check_init(JNIEnv* env)
    {
        if (m_elements_holder->m_jarray != nullptr && m_elements_holder->m_data_ptr == nullptr) {
            THROW_JAVA_EXCEPTION(env, JavaExceptionDef::IllegalArgument,
                                 util::format("GetXxxArrayElements failed on %1.",
                                              reinterpret_cast<int64_t>(m_elements_holder->m_jarray)));
        }
    }
};

// Accessor for Java object arrays
template <typename AccessorType, typename ObjectType>
class JObjectArrayAccessor {
public:
    JObjectArrayAccessor(JNIEnv* env, jobjectArray jobject_array)
        : m_env(env)
        , m_jobject_array(jobject_array)
        , m_size(jobject_array ? env->GetArrayLength(jobject_array) : 0)
    {
    }
    ~JObjectArrayAccessor()
    {
    }

    // Not implemented
    JObjectArrayAccessor(JObjectArrayAccessor&&) = delete;
    JObjectArrayAccessor& operator=(JObjectArrayAccessor&&) = delete;
    JObjectArrayAccessor(const JObjectArrayAccessor&) = delete;
    JObjectArrayAccessor& operator=(const JObjectArrayAccessor&) = delete;

    inline jsize size() const noexcept
    {
        return m_size;
    }

    inline AccessorType operator[](const int index) const noexcept
    {
        return AccessorType(m_env, static_cast<ObjectType>(m_env->GetObjectArrayElement(m_jobject_array, index)));
    }

private:
    JNIEnv* m_env;
    jobjectArray m_jobject_array;
    jsize m_size;
};

// Accessor for jbyteArray
template <>
inline JPrimitiveArrayAccessor<jbyteArray, jbyte>::ElementsHolder::ElementsHolder(JNIEnv* env, jbyteArray jarray)
    : m_env(env)
    , m_jarray(jarray)
    , m_data_ptr(jarray ? env->GetByteArrayElements(jarray, nullptr) : nullptr)
{
}

template <>
inline JPrimitiveArrayAccessor<jbyteArray, jbyte>::ElementsHolder::~ElementsHolder()
{
    if (m_jarray) {
        m_env->ReleaseByteArrayElements(m_jarray, m_data_ptr, m_release_mode);
    }
}

template <>
template <>
inline BinaryData JPrimitiveArrayAccessor<jbyteArray, jbyte>::transform<BinaryData>()
{
    return is_null() ? realm::BinaryData()
                     : realm::BinaryData(reinterpret_cast<const char*>(m_elements_holder->m_data_ptr), m_size);
}

template <>
template <>
inline std::vector<char> JPrimitiveArrayAccessor<jbyteArray, jbyte>::transform<std::vector<char>>()
{
    if (is_null()) {
        return {};
    }

    std::vector<char> v(m_size);
    std::copy_n(m_elements_holder->m_data_ptr, v.size(), v.begin());
    return v;
}

// Accessor for jbooleanArray
template <>
inline JPrimitiveArrayAccessor<jbooleanArray, jboolean>::ElementsHolder::ElementsHolder(JNIEnv* env, jbooleanArray jarray)
    : m_env(env)
    , m_jarray(jarray)
    , m_data_ptr(jarray ? env->GetBooleanArrayElements(jarray, nullptr) : nullptr)
{
}

template <>
inline JPrimitiveArrayAccessor<jbooleanArray, jboolean>::ElementsHolder::~ElementsHolder()
{
    if (m_jarray) {
        m_env->ReleaseBooleanArrayElements(m_jarray, m_data_ptr, m_release_mode);
    }
}

// Accessor for jlongArray
template <>
inline JPrimitiveArrayAccessor<jlongArray, jlong>::ElementsHolder::ElementsHolder(JNIEnv* env, jlongArray jarray)
    : m_env(env)
    , m_jarray(jarray)
    , m_data_ptr(jarray ? env->GetLongArrayElements(jarray, nullptr) : nullptr)
{
}

template <>
inline JPrimitiveArrayAccessor<jlongArray, jlong>::ElementsHolder::~ElementsHolder()
{
    if (m_jarray) {
        m_env->ReleaseLongArrayElements(m_jarray, m_data_ptr, m_release_mode);
    }
}

} // namespace realm
} // namespace _impl

#endif // REALM_JNI_IMPL_JAVA_ACCESSOR_HPP
