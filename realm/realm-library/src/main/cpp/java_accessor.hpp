
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
#include <realm/table.hpp>
#include <realm/util/any.hpp>
#include <realm/util/to_string.hpp>

#include <object_accessor.hpp>
#include <realm/util/any.hpp>

#include "java_class_global_def.hpp"
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

    inline bool is_null()
    {
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

// An object accessor context which can be used to create and access objects
// using util::Any as the type-erased value type. In addition, this serves as
// the reference implementation of an accessor context that must be implemented
// by each binding.
class JavaAccessorContext {
public:
    JavaAccessorContext(JNIEnv* env)
        : m_env(env){}

    // Convert from core types to the boxed type
    util::Any box(BinaryData v) const
    {
        return reinterpret_cast<jobject>(JavaClassGlobalDef::new_byte_array(m_env, v));
    }
    util::Any box(List /*v*/) const
    {
        REALM_TERMINATE("not supported");
    }
    util::Any box(Object /*v*/) const
    {
        REALM_TERMINATE("not supported");
    }
    util::Any box(Results /*v*/) const
    {
        REALM_TERMINATE("not supported");
    }
    util::Any box(StringData v) const
    {
        return reinterpret_cast<jobject>(to_jstring(m_env, v));
    }
    util::Any box(Timestamp v) const
    {
        return JavaClassGlobalDef::new_date(m_env, v);
    }
    util::Any box(bool v) const
    {
        return _impl::JavaClassGlobalDef::new_boolean(m_env, v);
    }
    util::Any box(double v) const
    {
        return _impl::JavaClassGlobalDef::new_double(m_env, v);
    }
    util::Any box(float v) const
    {
        return _impl::JavaClassGlobalDef::new_float(m_env, v);
    }
    util::Any box(int64_t v) const
    {
        return _impl::JavaClassGlobalDef::new_long(m_env, v);
    }
    util::Any box(util::Optional<bool> v) const
    {
        return v ? _impl::JavaClassGlobalDef::new_boolean(m_env, v.value()) : nullptr;
    }
    util::Any box(util::Optional<double> v) const
    {
        return v ? _impl::JavaClassGlobalDef::new_double(m_env, v.value()) : nullptr;
    }
    util::Any box(util::Optional<float> v) const
    {
        return v ? _impl::JavaClassGlobalDef::new_float(m_env, v.value()) : nullptr;
    }
    util::Any box(util::Optional<int64_t> v) const
    {
        return v ? _impl::JavaClassGlobalDef::new_long(m_env, v.value()) : nullptr;
    }
    util::Any box(RowExpr) const
    {
        REALM_TERMINATE("not supported");
    }

    // Any properties are only supported by the Cocoa binding to enable reading
    // old Realm files that may have used them. Other bindings can safely not
    // implement this.
    util::Any box(Mixed) const
    {
        REALM_TERMINATE("not supported");
    }

    // Convert from the boxed type to core types. This needs to be implemented
    // for all of the types which `box()` can take, plus `RowExpr` and optional
    // versions of the numeric types, minus `List` and `Results`.
    //
    // `create` and `update` are only applicable to `unbox<RowExpr>`. If
    // `create` is false then when given something which is not a managed Realm
    // object `unbox()` should simply return a detached row expr, while if it's
    // true then `unbox()` should create a new object in the context's Realm
    // using the provided value. If `update` is true then upsert semantics
    // should be used for this.
    template <typename T>
    T unbox(util::Any& v, bool /*create*/ = false, bool /*update*/ = false) const
    {
        return any_cast<T>(v);
    }

private:
    JNIEnv* m_env;

    inline void check_value_not_null(util::Any& v, const char* expected_type) const
    {
        if (!v.has_value()) {
            THROW_JAVA_EXCEPTION(
                m_env, JavaExceptionDef::IllegalArgument,
                util::format("This field is required. A non-null '%1' type value is expected.", expected_type));
        }
    }
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
    // To solve the link issue by directly using Table::max_binary_size
    static constexpr size_t max_binary_size = Table::max_binary_size;

    if (static_cast<size_t>(m_size) > max_binary_size) {
        THROW_JAVA_EXCEPTION(m_elements_holder->m_env, JavaExceptionDef::IllegalArgument,
                             util::format("The length of 'byte[]' value is %1 which exceeds the max binary size %2.",
                                 m_size, max_binary_size));
    }
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
inline JPrimitiveArrayAccessor<jbooleanArray, jboolean>::ElementsHolder::ElementsHolder(JNIEnv* env,
                                                                                        jbooleanArray jarray)
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

template <>
inline bool JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    check_value_not_null(v, "Boolean");
    return any_cast<jboolean>(v) == JNI_TRUE;
}

template <>
inline int64_t JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    check_value_not_null(v, "Long");
    return static_cast<int64_t>(any_cast<jlong>(v));
}

template <>
inline double JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    check_value_not_null(v, "Double");
    return static_cast<double>(any_cast<jdouble>(v));
}

template <>
inline float JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    check_value_not_null(v, "Float");
    return static_cast<float>(any_cast<jfloat>(v));
}

template <>
inline StringData JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    if (!v.has_value()) {
        return StringData();
    }
    auto& value = any_cast<JStringAccessor&>(v);
    return value;
}

template <>
inline BinaryData JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    if (!v.has_value())
        return BinaryData();
    auto& value = any_cast<JByteArrayAccessor&>(v);
    return value.transform<BinaryData>();
}

template <>
inline Timestamp JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    return v.has_value() ? from_milliseconds(any_cast<jlong>(v)) : Timestamp();
}

template <>
inline RowExpr JavaAccessorContext::unbox(util::Any&, bool, bool) const
{
    REALM_TERMINATE("not supported");
}

template <>
inline util::Optional<bool> JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    return v.has_value() ? util::make_optional(any_cast<jboolean>(v) == JNI_TRUE) : util::none;
}

template <>
inline util::Optional<int64_t> JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    return v.has_value() ? util::make_optional(static_cast<int64_t>(any_cast<jlong>(v))) : util::none;
}

template <>
inline util::Optional<double> JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    return v.has_value() ? util::make_optional(any_cast<jdouble>(v)) : util::none;
}

template <>
inline util::Optional<float> JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    return v.has_value() ? util::make_optional(any_cast<jfloat>(v)) : util::none;
}

template <>
inline Mixed JavaAccessorContext::unbox(util::Any&, bool, bool) const
{
    REALM_TERMINATE("not supported");
}

} // namespace realm
} // namespace _impl

#endif // REALM_JNI_IMPL_JAVA_ACCESSOR_HPP
