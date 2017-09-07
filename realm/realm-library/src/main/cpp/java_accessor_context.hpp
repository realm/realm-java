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

#ifndef JAVA_ACCESSOR_CONTEXT_HPP
#define JAVA_ACCESSOR_CONTEXT_HPP

#include <jni.h>

#include <object_accessor.hpp>
#include <util/any.hpp>

#include "java_class_global_def.hpp"

namespace realm {
class JavaAccessorContext {
public:
    JavaAccessorContext(JNIEnv* env)
        : m_env(env){};

    // Convert from core types to the boxed type
    util::Any box(BinaryData v) const
    {
        return std::string(v);
    }
    util::Any box(List v) const
    {
        return v;
    }
    util::Any box(Object v) const
    {
        return v;
    }
    util::Any box(Results v) const
    {
        return v;
    }
    util::Any box(StringData v) const
    {
        return to_jstring(m_env, v);
    }
    util::Any box(Timestamp v) const
    {
        return !v.is_null() ? _impl::JavaClassGlobalDef::new_date(m_env, v) : nullptr;
    }
    util::Any box(bool v) const
    {
        return v;
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
    };

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
};

template <>
inline StringData JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    if (!v.has_value()) {
        return StringData();
    }
    //auto& value = any_cast<JStringAccessor&>(v);
    //return value;
        return StringData();
}

template <>
inline BinaryData JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    if (!v.has_value())
        return BinaryData();
    auto& value = any_cast<std::string&>(v);
    return BinaryData(value.c_str(), value.size());
}

template <>
inline Timestamp JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    return v.has_value() ? from_milliseconds(unbox<jlong>(v)) : Timestamp();
}

template <>
inline RowExpr JavaAccessorContext::unbox(util::Any&, bool, bool) const
{
    REALM_TERMINATE("not supported");
}

template <>
inline util::Optional<bool> JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    return v.has_value() ? util::make_optional(unbox<jboolean>(v) == JNI_TRUE) : util::none;
}

template <>
inline util::Optional<int64_t> JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    return v.has_value() ? util::make_optional(unbox<jlong>(v)) : util::none;
}

template <>
inline util::Optional<double> JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    return v.has_value() ? util::make_optional(unbox<jdouble>(v)) : util::none;
}

template <>
inline util::Optional<float> JavaAccessorContext::unbox(util::Any& v, bool, bool) const
{
    return v.has_value() ? util::make_optional(unbox<jfloat>(v)) : util::none;
}

template <>
inline Mixed JavaAccessorContext::unbox(util::Any&, bool, bool) const
{
    throw std::logic_error("'Any' type is unsupported");
}
} // namespace realm

#endif // JAVA_ACCESSOR_CONTEXT_HPP
