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
#include "object-store/src/property.hpp"

#include <realm/row.hpp>
#include <realm/util/any.hpp>

using namespace realm::_impl;

namespace realm {
    
// Tagged union class representing all the values Java can send to Object Store
struct JavaValueType {
    enum { Empty, Integer, String, Boolean, Float, Double, Date, Binary, Object, List } type;
    union Value {
        bool empty;
        jlong val_int;
        JStringAccessor val_string;
        jboolean val_bool;
        jfloat val_float;
        jdouble val_double;
        Timestamp val_timestamp;
        OwnedBinaryData val_binary;
        Row* val_object;
        std::vector<JavaValueType> val_list; // Replace with each type of list?
//        Value() : empty(true) {};
//        Value(jlong val) : val_int(val) {};
//        Value(JStringAccessor val) : val_string(std::move(val)) {};
//        Value(jboolean val) : val_bool(val) {};
//        Value(jfloat val) : val_float(val) {};
//        Value(jdouble val) : val_double(val) {};
//        Value(Timestamp val) : val_timestamp(val) {};
//        Value(OwnedBinaryData val) : val_binary(std::move(val)) {};
//        Value(Row* val) : val_object(val) {};
//        Value(std::vector<JavaValueType> val) : val_list(std::move(val)) {};
//        ~Value() {
//            // Deleted due to non-trivial members. No idea what to do here?
//        }
    } value;

    // Initializer constructors
    JavaValueType() : type(Empty), value() {};
    JavaValueType(jlong val) : type(Integer), value(val) {};
    JavaValueType(JStringAccessor val) : type(String), value(val) {};
    JavaValueType(jboolean val) : type(Boolean), value(val) {};
    JavaValueType(jfloat val) : type(Float), value(val) {};
    JavaValueType(jdouble val) : type(Double), value(val) {};
    JavaValueType(Timestamp val) : type(Date), value(val) {};
    JavaValueType(OwnedBinaryData val) : type(Binary), value(val) {};
    JavaValueType(Row* val) : type(Object), value(val) {};
    JavaValueType(std::vector<JavaValueType> val) : type(List), value(val) {};

    // Copy constructors
    JavaValueType(const realm::JavaValueType& jvt) : type(jvt.type) {
        switch(jvt.type) {
            case Empty: value.empty = true; break;
            case Integer:break;
            case String:break;
            case Boolean:break;
            case Float:break;
            case Double:break;
            case Date:break;
            case Binary:break;
            case Object:break;
            case List:break;
        }
    };

    // Move constructor
    JavaValueType(JavaValueType&& jvt) : type(jvt.type), value(jvt.value) {
        jvt.value = nullptr;
        jvt.type = nullptr;
    }

//    JavaValueType& operator=(JavaValueType const& rhs)
//    {
//        // FIXME: This is probably not correct, no idea why it is needed in the first place.
//        type = rhs.type;
//        value = rhs.value;
//        return *this;
//    }

    bool has_value() const {
        return type != Empty;
    }

    jlong get_int() const { return value.val_int; };
    JStringAccessor get_string() const { return value.val_string; };
    std::vector<JavaValueType> get_list() const { return value.val_list; };

    ~JavaValueType() {
        // Do stuff
    }

    // Returns a string representation of the value contained in this object.
    std::string to_string() {
        switch(type) {
            case Empty: return "null";
            case Integer: return "not implemented";
            case String: return std::string(value.val_string);
            case Boolean: return "not implemented";
            case Float: return "not implemented";
            case Double: return "not implemented";
            case Date: return "not implemented";
            case Binary: return "not implemented";
            case Object: return "not implemented";
            case List: return "not implemented";
        }
    }
};

using AnyDict = std::vector<JavaValueType>; // Use column indexes as lookup keys
using AnyVector = std::vector<JavaValueType>;

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
    util::Optional<JavaValueType> value_for_property(JavaValueType& dict,
                                                 Property const& prop,
                                                 size_t /*property_index*/) const
    {
        std::vector<JavaValueType> list = dict.get_list();
        auto property_value = list.at(prop.table_column);
        return util::make_optional(property_value);
    }

    // Get the default value for the given property in the given object schema,
    // or `util::none` if there is none (which is distinct from the default
    // being `null`).
    //
    // This implementation does not support default values; see the default
    // value tests for an example of one which does.
    util::Optional<JavaValueType>
    default_value_for_property(ObjectSchema const&, Property const&) const
    {
        return util::none;
    }

    // Invoke `fn` with each of the values from an enumerable type
    template<typename Func>
    void enumerate_list(JavaValueType& value, Func&& fn) {
        if (value.type == JavaValueType::List) {
            for (auto&& v : value.get_list()) {
                fn(v);
            }
        } else {
            throw std::logic_error("Type is not a list");
        }
    }

    // Determine if `value` boxes the same List as `list`
    bool is_same_list(List const& /*list*/, JavaValueType const& /*value*/)
    {
//        if (auto list2 = any_cast<List>(&value))
//            return list == *list2;
        return false;
    }

    // Convert from core types to the boxed type
    // These are currently not used as Proxy objects read directly from the Row objects
    // This implementation is thus entirely untested.
//    JavaValueType box(BinaryData v) const { return reinterpret_cast<jobject>(JavaClassGlobalDef::new_byte_array(m_env, v)); }
//    JavaValueType box(List /*v*/) const { REALM_TERMINATE("'List' not implemented"); }
//    JavaValueType box(Object /*v*/) const { REALM_TERMINATE("'Object' not implemented"); }
//    JavaValueType box(Results /*v*/) const { REALM_TERMINATE("'Results' not implemented"); }
//    JavaValueType box(StringData v) const { return reinterpret_cast<jobject>(to_jstring(m_env, v)); }
//    JavaValueType box(Timestamp v) const { return JavaClassGlobalDef::new_date(m_env, v); }
//    JavaValueType box(bool v) const { return _impl::JavaClassGlobalDef::new_boolean(m_env, v); }
//    JavaValueType box(double v) const { return _impl::JavaClassGlobalDef::new_double(m_env, v); }
//    JavaValueType box(float v) const { return _impl::JavaClassGlobalDef::new_float(m_env, v); }
//    JavaValueType box(int64_t v) const { return _impl::JavaClassGlobalDef::new_long(m_env, v); }
//    JavaValueType box(util::Optional<bool> v) const { return v ? _impl::JavaClassGlobalDef::new_boolean(m_env, v.value()) : nullptr; }
//    JavaValueType box(util::Optional<double> v) const { return v ? _impl::JavaClassGlobalDef::new_double(m_env, v.value()) : nullptr; }
//    JavaValueType box(util::Optional<float> v) const { return v ? _impl::JavaClassGlobalDef::new_float(m_env, v.value()) : nullptr; }
//    JavaValueType box(util::Optional<int64_t> v) const { return v ? _impl::JavaClassGlobalDef::new_long(m_env, v.value()) : nullptr; }
//    JavaValueType box(RowExpr) const { REALM_TERMINATE("'RowExpr' not implemented"); }

    // Mixed type is only supported by the Cocoa binding to enable reading
    // old Realm files that may have used them. All other bindings can ignore it.
//    JavaValueType box(Mixed) const { REALM_TERMINATE("'Mixed' not supported"); }

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
    T unbox(JavaValueType& /*v*/, bool /*create*/= false, bool /*update*/= false, bool /*diff_on_update*/= false, size_t /*current_row*/ = realm::npos) const {
        throw std::logic_error("Missing template specialization"); // All types should have specialized templates
    }

    bool is_null(JavaValueType const& v) const noexcept { return !v.has_value(); }
    JavaValueType null_value() const noexcept { return {}; }
    util::Optional<JavaValueType> no_value() const noexcept { return {}; }

    // KVO hooks which will be called before and after modying a property from
    // within Object::create().
    void will_change(Object const&, Property const&) {}
    void did_change() {}

    // Get a string representation of the given value for use in error messages.
    // This method should only be used when printing warnings about primary keys
    // which means the input should only be valid types for primary keys:
    // StringData, int64_t and Optional<int64_t>
    std::string print(JavaValueType const& /*val*/) const {
        return "null";

// FIXME: Figure out why this doesn't work on some architectures (CI)
// One example: https://ci.realm.io/blue/organizations/jenkins/realm%2Frealm-java/detail/PR-6224/26/pipeline
//        if (!val.has_value() ||  val.type() == typeid(void)) {
//            return "null";
//        }
//
//        if (val.type() == typeid(StringData)) {
//            auto str = any_cast<StringData>(val);
//            if (str.is_null()) {
//                return "null";
//            } else {
//                return std::string(str);
//            }
//        } else if (val.type() == typeid(JStringAccessor)) {
//            auto str = any_cast<JStringAccessor>(val);
//            if (str.is_null()) {
//                return "null";
//            } else {
//                return std::string(str);
//            }
//        } else if (val.type() == typeid(std::string)) {
//            return any_cast<std::string>(val);
//        } else if (val.type() == typeid(util::Optional<int64_t>)) {
//            auto opt = any_cast<util::Optional<int64_t>>(val);
//            if (!opt) {
//                return "null";
//            } else {
//                std::ostringstream o;
//                o << opt.value();
//                return o.str();
//            }
//        } else if (val.type() == typeid(int64_t)) {
//            auto number = any_cast<int64_t>(val);
//            std::ostringstream o;
//            o << number;
//            return o.str();
//        } else {
//            auto str = std::string(val.type().name());
//            throw std::logic_error(util::format("Unexpected type: %s", str));
//        }
    }

    // Cocoa allows supplying fewer values than there are properties when
    // creating objects using an array of values. Other bindings should not
    // mimick this behavior so just return false here.
    bool allow_missing(JavaValueType const&) const { return false; }

private:
    JNIEnv* m_env;
    std::shared_ptr<Realm> realm;
    const ObjectSchema* object_schema = nullptr;

    inline void check_value_not_null(JavaValueType& v, const char* expected_type) const
    {
        if (!v.has_value()) {
            throw RequiredFieldValueNotProvidedException(std::string(expected_type));
        }
    }
};

template <>
inline bool JavaContext::unbox(JavaValueType& v, bool, bool, bool, size_t) const
{
    check_value_not_null(v, "Boolean");
    return v.value.val_bool == JNI_TRUE;
}

template <>
inline int64_t JavaContext::unbox(JavaValueType& v, bool, bool, bool, size_t) const
{
    check_value_not_null(v, "Long");
    return static_cast<int64_t>(v.value.val_int);
}

template <>
inline double JavaContext::unbox(JavaValueType& v, bool, bool, bool, size_t) const
{
    check_value_not_null(v, "Double");
    return static_cast<double>(v.value.val_double);
}

template <>
inline float JavaContext::unbox(JavaValueType& v, bool, bool, bool, size_t) const
{
    check_value_not_null(v, "Float");
    return static_cast<float>(v.value.val_float);
}

template <>
inline StringData JavaContext::unbox(JavaValueType& v, bool, bool, bool, size_t) const
{
    if (!v.has_value()) {
        return StringData();
    }

    return StringData(v.value.val_string);
}

template <>
inline BinaryData JavaContext::unbox(JavaValueType& v, bool, bool, bool, size_t) const
{
    if (!v.has_value()) {
        return BinaryData();
    } else {
        return v.value.val_binary.get();
    }
}

template <>
inline Timestamp JavaContext::unbox(JavaValueType& v, bool, bool, bool, size_t) const
{
    return v.has_value() ? v.value.val_timestamp : Timestamp();
}

template <>
inline RowExpr JavaContext::unbox(JavaValueType& v, bool create, bool update, bool diff_on_update, size_t current_row) const
{
// FIXME
    if (v.type == JavaValueType::Object) {
        return *v.value.val_object;
    } else if (!create) {
        return RowExpr();
    }
//    if (auto row = any_cast<Row>(&v))
//        return *row;
//    if (auto object = any_cast<Object>(&v))
//        return object->row();
//    if (auto row = any_cast<RowExpr>(&v))
//        return *row;
//    if (!create)
//        return RowExpr();

    REALM_ASSERT(object_schema);
    return Object::create(const_cast<JavaContext&>(*this), realm, *object_schema, v, update, diff_on_update, current_row).row();
}

template <>
inline util::Optional<bool> JavaContext::unbox(JavaValueType& v, bool, bool, bool, size_t) const
{
    return v.has_value() ? util::make_optional(v.value.val_bool == JNI_TRUE) : util::none;
}

template <>
inline util::Optional<int64_t> JavaContext::unbox(JavaValueType& v, bool, bool, bool, size_t) const
{
    return v.has_value() ? util::make_optional(static_cast<int64_t>(v.value.val_int)) : util::none;
}

template <>
inline util::Optional<double> JavaContext::unbox(JavaValueType& v, bool, bool, bool, size_t) const
{
    return v.has_value() ? util::make_optional(v.value.val_double) : util::none;
}

template <>
inline util::Optional<float> JavaContext::unbox(JavaValueType& v, bool, bool, bool, size_t) const
{
    return v.has_value() ? util::make_optional(v.value.val_float) : util::none;
}

template <>
inline Mixed JavaContext::unbox(JavaValueType&, bool, bool, bool, size_t) const
{
    REALM_TERMINATE("'Mixed' not supported");
}

}

#endif // REALM_JAVA_OBJECT_ACCESSOR_HPP
