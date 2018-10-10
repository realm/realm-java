////////////////////////////////////////////////////////////////////////////
//
// Copyright 2018 Realm Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////

#ifndef REALM_JAVA_OBJECT_ACCESSOR
#define REALM_JAVA_OBJECT_ACCESSOR

#include <cstddef>

#include "java_accessor.hpp"
#include "java_class_global_def.hpp"
#include "object_accessor.hpp"

#include <realm/util/any.hpp>

using namespace realm::_impl;

namespace realm {
using AnyDict = std::map<std::string, util::Any>;
using AnyVector = std::vector<util::Any>;

struct RequiredFieldValueNotProvidedException : public std::logic_error {
    const std::string object_type;
    RequiredFieldValueNotProvidedException(const std::string& object_type)
            : std::logic_error("This field is required. A non-null '" + object_type + "' type value is expected.")
    {
    }
};

// This is the Java implementation of the `CppContext` class found in `object_accessor_impl.hpp`
// It is an object accessor context which can be used to create and access objects.
// It will map between JNI types and Cores data types.
class JavaContext {
public:
    JavaContext(JNIEnv* env, std::shared_ptr<Realm> realm, const ObjectSchema& os)
    : m_env(env),
      realm(std::move(realm)),
      object_schema(&os) { }

    // This constructor is the only one used by the object accessor code, and is
    // used when recurring into a link or array property during object creation
    // (i.e. prop.type will always be Object or Array).
    JavaContext(JavaContext& c, Property const& prop)
            : m_env(c.m_env),
              realm(c.realm)
            , object_schema(prop.type == PropertyType::Object ? &*realm->schema().find(prop.object_type) : c.object_schema)
    { }

    // The use of util::Optional for the following two functions is not a hard
    // requirement; only that it be some type which can be evaluated in a
    // boolean context to determine if it contains a value, and if it does
    // contain a value it must be dereferencable to obtain that value.

    // Get the value for a property in an input object, or `util::none` if no
    // value present. The property is identified both by the name of the
    // property and its index within the ObjectScehma's persisted_properties
    // array.
    util::Optional<util::Any> value_for_property(util::Any& dict,
                                                 std::string const& prop_name,
                                                 size_t /* property_index */) const
    {
        auto const& v = any_cast<AnyDict&>(dict);
        auto it = v.find(prop_name);
        return it == v.end() ? util::none : util::make_optional(it->second);
    }

    // Get the default value for the given property in the given object schema,
    // or `util::none` if there is none (which is distinct from the default
    // being `null`).
    //
    // This implementation does not support default values; see the default
    // value tests for an example of one which does.
    util::Optional<util::Any>
    default_value_for_property(ObjectSchema const&, std::string const&) const
    {
        return util::none;
    }

    // Invoke `fn` with each of the values from an enumerable type
    template<typename Func>
    void enumerate_list(util::Any& value, Func&& fn) {
        for (auto&& v : any_cast<AnyVector&>(value))
            fn(v);
    }

    // Determine if `value` boxes the same List as `list`
    bool is_same_list(List const& list, util::Any const& value)
    {
        if (auto list2 = any_cast<List>(&value))
            return list == *list2;
        return false;
    }

    // Convert from core types to the boxed type
    // These are currently not used as Proxy objects read directly from the Row objects
    // This implementation is thus entirely untested.
    util::Any box(BinaryData v) const { return reinterpret_cast<jobject>(JavaClassGlobalDef::new_byte_array(m_env, v)); }
    util::Any box(List /*v*/) const { REALM_TERMINATE("'List' not implemented"); }
    util::Any box(Object /*v*/) const { REALM_TERMINATE("'Object' not implemented"); }
    util::Any box(Results /*v*/) const { REALM_TERMINATE("'Results' not implemented"); }
    util::Any box(StringData v) const { return reinterpret_cast<jobject>(to_jstring(m_env, v)); }
    util::Any box(Timestamp v) const { return JavaClassGlobalDef::new_date(m_env, v); }
    util::Any box(bool v) const { return _impl::JavaClassGlobalDef::new_boolean(m_env, v); }
    util::Any box(double v) const { return _impl::JavaClassGlobalDef::new_double(m_env, v); }
    util::Any box(float v) const { return _impl::JavaClassGlobalDef::new_float(m_env, v); }
    util::Any box(int64_t v) const { return _impl::JavaClassGlobalDef::new_long(m_env, v); }
    util::Any box(util::Optional<bool> v) const { return v ? _impl::JavaClassGlobalDef::new_boolean(m_env, v.value()) : nullptr; }
    util::Any box(util::Optional<double> v) const { return v ? _impl::JavaClassGlobalDef::new_double(m_env, v.value()) : nullptr; }
    util::Any box(util::Optional<float> v) const { return v ? _impl::JavaClassGlobalDef::new_float(m_env, v.value()) : nullptr; }
    util::Any box(util::Optional<int64_t> v) const { return v ? _impl::JavaClassGlobalDef::new_long(m_env, v.value()) : nullptr; }
    util::Any box(RowExpr) const { REALM_TERMINATE("'RowExpr' not implemented"); }

    // Mixed type is only supported by the Cocoa binding to enable reading
    // old Realm files that may have used them. All other bindings can ignore it.
    util::Any box(Mixed) const { REALM_TERMINATE("'Mixed' not supported"); }

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
    template<typename T>
    T unbox(util::Any& /*v*/, bool /*create*/= false, bool /*update*/= false, bool /*diff_on_update*/= false) const {
        throw std::logic_error("Missing template specialization"); // All types should have specialized templates
    }

    bool is_null(util::Any const& v) const noexcept { return !v.has_value(); }
    util::Any null_value() const noexcept { return {}; }
    util::Optional<util::Any> no_value() const noexcept { return {}; }

    // KVO hooks which will be called before and after modying a property from
    // within Object::create().
    void will_change(Object const&, Property const&) {}
    void did_change() {}

    // Get a string representation of the given value for use in error messages.
    // This method should only be used when printing warnings about primary keys
    // which means the input should only be valid types for primary keys:
    // StringData, int64_t and Optional<int64_t>
    std::string print(util::Any const& val) const {
        if (!val.has_value() ||  val.type() == typeid(void)) {
            return "null";
        }

        if (val.type() == typeid(StringData)) {
            auto str = any_cast<StringData>(val);
            if (str.is_null()) {
                return "null";
            } else {
                return std::string(str);
            }
        } else if (val.type() == typeid(JStringAccessor)) {
            auto str = any_cast<JStringAccessor>(val);
            if (str.is_null()) {
                return "null";
            } else {
                return std::string(str);
            }
        } else if (val.type() == typeid(std::string)) {
            return any_cast<std::string>(val);
        } else if (val.type() == typeid(util::Optional<int64_t>)) {
            auto opt = any_cast<util::Optional<int64_t>>(val);
            if (!opt) {
                return "null";
            } else {
                std::ostringstream o;
                o << opt.value();
                return o.str();
            }
        } else if (val.type() == typeid(int64_t)) {
            auto number = any_cast<int64_t>(val);
            std::ostringstream o;
            o << number;
            return o.str();
        } else {
            auto str = std::string(val.type().name());
            throw std::logic_error(util::format("Unexpected type: %s", str));
        }
    }

    // Cocoa allows supplying fewer values than there are properties when
    // creating objects using an array of values. Other bindings should not
    // mimick this behavior so just return false here.
    bool allow_missing(util::Any const&) const { return false; }

private:
    JNIEnv* m_env;
    std::shared_ptr<Realm> realm;
    const ObjectSchema* object_schema = nullptr;

    inline void check_value_not_null(util::Any& v, const char* expected_type) const
    {
        if (!v.has_value()) {
            throw RequiredFieldValueNotProvidedException(std::string(expected_type));
        }
    }
};

template <>
inline bool JavaContext::unbox(util::Any& v, bool, bool, bool) const
{
    check_value_not_null(v, "Boolean");
    return any_cast<jboolean>(v) == JNI_TRUE;
}

template <>
inline int64_t JavaContext::unbox(util::Any& v, bool, bool, bool) const
{
    check_value_not_null(v, "Long");
    return static_cast<int64_t>(any_cast<jlong>(v));
}

template <>
inline double JavaContext::unbox(util::Any& v, bool, bool, bool) const
{
    check_value_not_null(v, "Double");
    return static_cast<double>(any_cast<jdouble>(v));
}

template <>
inline float JavaContext::unbox(util::Any& v, bool, bool, bool) const
{
    check_value_not_null(v, "Float");
    return static_cast<float>(any_cast<jfloat>(v));
}

template <>
inline StringData JavaContext::unbox(util::Any& v, bool, bool, bool) const
{
    if (!v.has_value()) {
        return StringData();
    }
    auto& value = any_cast<JStringAccessor&>(v);
    StringData str = StringData(value);
    return str;
}

template <>
inline BinaryData JavaContext::unbox(util::Any& v, bool, bool, bool) const
{
    if (!v.has_value())
        return BinaryData();

    auto& value = any_cast<OwnedBinaryData&>(v);
    const BinaryData& data = value.get();
    return data;
}

template <>
inline Timestamp JavaContext::unbox(util::Any& v, bool, bool, bool) const
{
    return v.has_value() ? from_milliseconds(any_cast<jlong>(v)) : Timestamp();
}

template <>
inline RowExpr JavaContext::unbox(util::Any& v, bool create, bool update, bool diff_on_update) const
{
    if (auto row = any_cast<Row>(&v))
        return *row;
    if (auto object = any_cast<Object>(&v))
        return object->row();
    if (auto row = any_cast<RowExpr>(&v))
        return *row;
    if (!create)
        return RowExpr();

    REALM_ASSERT(object_schema);
    return Object::create(const_cast<JavaContext&>(*this), realm, *object_schema, v, update, diff_on_update).row();
}

template <>
inline util::Optional<bool> JavaContext::unbox(util::Any& v, bool, bool, bool) const
{
    return v.has_value() ? util::make_optional(any_cast<jboolean>(v) == JNI_TRUE) : util::none;
}

template <>
inline util::Optional<int64_t> JavaContext::unbox(util::Any& v, bool, bool, bool) const
{
    return v.has_value() ? util::make_optional(static_cast<int64_t>(any_cast<jlong>(v))) : util::none;
}

template <>
inline util::Optional<double> JavaContext::unbox(util::Any& v, bool, bool, bool) const
{
    return v.has_value() ? util::make_optional(any_cast<jdouble>(v)) : util::none;
}

template <>
inline util::Optional<float> JavaContext::unbox(util::Any& v, bool, bool, bool) const
{
    return v.has_value() ? util::make_optional(any_cast<jfloat>(v)) : util::none;
}

template <>
inline Mixed JavaContext::unbox(util::Any&, bool, bool, bool) const
{
    REALM_TERMINATE("'Mixed' not supported");
}

}

#endif // REALM_JAVA_OBJECT_ACCESSOR_HPP
