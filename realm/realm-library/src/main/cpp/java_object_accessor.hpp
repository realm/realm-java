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

#include <algorithm>
#include <cstddef>
#include <type_traits>

#include "java_accessor.hpp"
#include "java_class_global_def.hpp"
#include <realm/object-store/object_accessor.hpp>
#include <realm/object-store/property.hpp>

#include <realm/decimal128.hpp>
#include <realm/object_id.hpp>
#include <realm/util/any.hpp>

using namespace realm::_impl;

#define REALM_FOR_EACH_JAVA_VALUE_TYPE(X) \
    X(Integer) \
    X(String) \
    X(Boolean) \
    X(Float) \
    X(Double) \
    X(Date) \
    X(ObjectId) \
    X(UUID) \
    X(ObjectLink) \
    X(Decimal) \
    X(Binary) \
    X(Object) \
    X(List) \
    X(PropertyList) \
    X(Dictionary) \

namespace realm {

struct JavaValue;

enum class JavaValueType {
    Empty,
#define REALM_DEFINE_JAVA_VALUE_TYPE(x) x,
    REALM_FOR_EACH_JAVA_VALUE_TYPE(REALM_DEFINE_JAVA_VALUE_TYPE)
#undef REALM_DEFINE_JAVA_VALUE_TYPE
    NumValueTypes
};

// Ugly work-around for initializer lists having problems on GCC 4.9
template <class T> constexpr T realm_max(T a) {
    return a;
}

template <class T, class... Rest> constexpr T realm_max(T a, T b, Rest... rest) {
    return a > realm_max(b, rest...) ? a : realm_max(b, rest...);
}

template <JavaValueType> struct JavaValueTypeRepr;
template <> struct JavaValueTypeRepr<JavaValueType::Integer>       { using Type = jlong; };
template <> struct JavaValueTypeRepr<JavaValueType::String>        { using Type = std::string; };
template <> struct JavaValueTypeRepr<JavaValueType::Boolean>       { using Type = jboolean; };
template <> struct JavaValueTypeRepr<JavaValueType::Float>         { using Type = jfloat; };
template <> struct JavaValueTypeRepr<JavaValueType::Double>        { using Type = jdouble; };
template <> struct JavaValueTypeRepr<JavaValueType::Date>          { using Type = Timestamp; };
template <> struct JavaValueTypeRepr<JavaValueType::ObjectId>      { using Type = ObjectId; };
template <> struct JavaValueTypeRepr<JavaValueType::Decimal>       { using Type = Decimal128; };
template <> struct JavaValueTypeRepr<JavaValueType::UUID>          { using Type = UUID; };
template <> struct JavaValueTypeRepr<JavaValueType::ObjectLink>    { using Type = ObjLink; };
template <> struct JavaValueTypeRepr<JavaValueType::Binary>        { using Type = OwnedBinaryData; };
template <> struct JavaValueTypeRepr<JavaValueType::Object>        { using Type = Obj*; };
template <> struct JavaValueTypeRepr<JavaValueType::List>          { using Type = std::vector<JavaValue>; };
template <> struct JavaValueTypeRepr<JavaValueType::PropertyList>  { using Type = std::map<ColKey, JavaValue>; };
template <> struct JavaValueTypeRepr<JavaValueType::Dictionary>    { using Type = std::map<std::string, JavaValue>; };

// Tagged union class representing all the values Java can send to Object Store
struct JavaValue {
    using Storage = std::aligned_storage_t<realm_max(
#define REALM_GET_SIZE_OF_JAVA_VALUE_TYPE_REPR(x) \
        sizeof(JavaValueTypeRepr<JavaValueType::x>::Type),
        REALM_FOR_EACH_JAVA_VALUE_TYPE(REALM_GET_SIZE_OF_JAVA_VALUE_TYPE_REPR)
        size_t(0)
#undef REALM_GET_SIZE_OF_JAVA_VALUE_TYPE_REPR
        ), realm_max(
#define REALM_GET_ALIGN_OF_JAVA_VALUE_TYPE_REPR(x) \
        alignof(JavaValueTypeRepr<JavaValueType::x>::Type),
        REALM_FOR_EACH_JAVA_VALUE_TYPE(REALM_GET_ALIGN_OF_JAVA_VALUE_TYPE_REPR)
        size_t(0)
#undef REALM_GET_ALIGN_OF_JAVA_VALUE_TYPE_REPR
        )>;

    Storage m_storage;
    JavaValueType m_type;

    // Initializer constructors
    JavaValue() : m_type(JavaValueType::Empty) {}

#define REALM_DEFINE_JAVA_VALUE_TYPE_CONSTRUCTOR(x) \
    explicit JavaValue(JavaValueTypeRepr<JavaValueType::x>::Type value) : m_type(JavaValueType::x) \
    { \
        new(&m_storage) JavaValueTypeRepr<JavaValueType::x>::Type{std::move(value)}; \
    }
    REALM_FOR_EACH_JAVA_VALUE_TYPE(REALM_DEFINE_JAVA_VALUE_TYPE_CONSTRUCTOR)
#undef REALM_DEFINE_JAVA_VALUE_TYPE_CONSTRUCTOR

    // Copy constructors
    JavaValue(const JavaValue& jvt) : m_type(JavaValueType::Empty) {
        *this = jvt;
    }

    // Move constructor
    JavaValue(JavaValue&& jvt) : m_type(JavaValueType::Empty) {
        *this = std::move(jvt);
    }

    ~JavaValue()
    {
        clear();
    }

    JavaValue& operator=(const JavaValue& rhs)
    {
       clear();
       switch (rhs.m_type) {
#define REALM_DEFINE_JAVA_VALUE_COPY_ASSIGNMENT(x) \
           case JavaValueType::x: { \
               using T = JavaValueTypeRepr<JavaValueType::x>::Type; \
               new(&m_storage) T{*reinterpret_cast<const T*>(&rhs.m_storage)}; \
               break; \
           }
        REALM_FOR_EACH_JAVA_VALUE_TYPE(REALM_DEFINE_JAVA_VALUE_COPY_ASSIGNMENT)
#undef REALM_DEFINE_JAVA_VALUE_COPY_ASSIGNMENT
           default: REALM_ASSERT(rhs.m_type == JavaValueType::Empty);
        }
        m_type = rhs.m_type;
        return *this;
    }

    JavaValue& operator=(JavaValue&& rhs)
    {
       clear();
       switch (rhs.m_type) {
           case JavaValueType::Empty: break; // Do nothing
#define REALM_DEFINE_JAVA_VALUE_COPY_ASSIGNMENT(x) \
           case JavaValueType::x: { \
               using T = JavaValueTypeRepr<JavaValueType::x>::Type; \
               new(&m_storage) T{std::move(*reinterpret_cast<T*>(&rhs.m_storage))}; \
               break; \
           }
        REALM_FOR_EACH_JAVA_VALUE_TYPE(REALM_DEFINE_JAVA_VALUE_COPY_ASSIGNMENT)
#undef REALM_DEFINE_JAVA_VALUE_COPY_ASSIGNMENT
           default: REALM_TERMINATE("Invalid type");
        }
        m_type = rhs.m_type;
        return *this;
    }

    bool has_value() const noexcept
    {
        return m_type != JavaValueType::Empty;
    }

    JavaValueType get_type() const noexcept
    {
        return m_type;
    }

    template <JavaValueType type>
    const typename JavaValueTypeRepr<type>::Type& get_as() const noexcept
    {
        REALM_ASSERT(m_type == type);
        return *reinterpret_cast<const typename JavaValueTypeRepr<type>::Type*>(&m_storage);
    }

    auto& get_int() const noexcept
    {
        return get_as<JavaValueType::Integer>();
    }

    auto& get_boolean() const noexcept
    {
        return get_as<JavaValueType::Boolean>();
    }

    auto& get_string() const noexcept
    {
        return get_as<JavaValueType::String>();
    }

    auto& get_float() const noexcept
    {
        return get_as<JavaValueType::Float>();
    }

    auto& get_double() const noexcept
    {
        return get_as<JavaValueType::Double>();
    }

    auto& get_list() const noexcept
    {
        return get_as<JavaValueType::List>();
    }

    auto& get_dictionary() const noexcept
    {
        return get_as<JavaValueType::Dictionary>();
    }

    auto& get_property_list() const noexcept
    {
        return get_as<JavaValueType::PropertyList>();
    }


    auto& get_date() const noexcept
    {
        return get_as<JavaValueType::Date>();
    }

    auto& get_object_id() const noexcept
    {
        return get_as<JavaValueType::ObjectId>();
    }

    auto& get_uuid() const noexcept
    {
        return get_as<JavaValueType::UUID>();
    }

    auto& get_object_link() const noexcept
    {
        return get_as<JavaValueType::ObjectLink>();
    }

    auto& get_decimal128() const noexcept
    {
        return get_as<JavaValueType::Decimal>();
    }

    auto& get_binary() const noexcept
    {
        return get_as<JavaValueType::Binary>();
    }

    auto& get_object() const noexcept
    {
        return get_as<JavaValueType::Object>();
    }

    void clear() noexcept
    {
        switch (m_type) {
            case JavaValueType::Empty: break; // Do nothing
#define REALM_DEFINE_JAVA_VALUE_DESTROY(x) \
            case JavaValueType::x: { \
                using T = JavaValueTypeRepr<JavaValueType::x>::Type; \
                reinterpret_cast<T*>(&m_storage)->~T(); \
                break; \
            }
            REALM_FOR_EACH_JAVA_VALUE_TYPE(REALM_DEFINE_JAVA_VALUE_DESTROY)
#undef REALM_DEFINE_JAVA_VALUE_DESTROY
            default: REALM_TERMINATE("Invalid type.");
        }
        m_type = JavaValueType::Empty;
    }

    // Returns a string representation of the value contained in this object.
    std::string to_string() const {
        std::ostringstream ss;
        switch(m_type) {
            case JavaValueType::Empty:
                return "null";
            case JavaValueType::Integer:
                ss << static_cast<int64_t>(get_int());
                return std::string(ss.str());
            case JavaValueType::String:
                return get_string();
            case JavaValueType::Boolean:
                return (get_boolean() == JNI_TRUE) ? "true" : "false";
            case JavaValueType::Float:
                ss << static_cast<float>(get_float());
                return std::string(ss.str());
            case JavaValueType::Double:
                ss << static_cast<double>(get_double());
                return std::string(ss.str());
            case JavaValueType::Date:
                ss << get_date();
                return std::string(ss.str());
            case JavaValueType::ObjectId:
                return get_object_id().to_string();
            case JavaValueType::UUID:
                return get_uuid().to_string();
            case JavaValueType::Decimal:
                return get_decimal128().to_string();
            case JavaValueType::Binary:
                ss << "Blob[";
                ss << get_binary().size();
                ss << "]";
                return std::string(ss.str());
            case JavaValueType::Object:
                ss << "Object[Type: ";
                ss << get_object()->get_table()->get_name();
                ss << ", colKey: ";
                ss << get_object()->get_key().value;
                ss << "]";
                return std::string(ss.str());
            case JavaValueType::ObjectLink:
                ss << "ObjectLink[tableKey: ";
                ss << get_object_link().get_table_key().value;
                ss << ", colKey: ";
                ss << get_object_link().get_obj_key().value;
                ss << "]";
                return std::string(ss.str());
            case JavaValueType::List:
                ss << "List[size: ";
                ss << get_list().size();
                ss << "]";
            case JavaValueType::PropertyList:
                ss << "PropertyList ";
                return std::string(ss.str());
            default: REALM_TERMINATE("Invalid type.");
        }
    }

    realm::Mixed to_mixed(){
        switch (this->get_type()) {
            case JavaValueType::Integer:
                return Mixed(this->get_int());
            case JavaValueType::String:
                return Mixed(StringData(this->get_string()));
            case JavaValueType::Boolean:
                return Mixed(B(this->get_boolean()));
            case JavaValueType::Float:
                return Mixed(this->get_float());
            case JavaValueType::Double:
                return Mixed(this->get_double());
            case JavaValueType::Date:
                return Mixed(this->get_date());
            case JavaValueType::ObjectId:
                return Mixed(this->get_object_id());
            case JavaValueType::UUID:
                return Mixed(this->get_uuid());
            case JavaValueType::Decimal:
                return Mixed(this->get_decimal128());
            case JavaValueType::Binary:
                return Mixed(this->get_binary().get());
            case JavaValueType::ObjectLink:
                return Mixed(this->get_object_link());
            case JavaValueType::Object:
            case JavaValueType::List:
            case JavaValueType::PropertyList:
            case JavaValueType::Dictionary:
            case JavaValueType::NumValueTypes:
            case JavaValueType::Empty:
                return Mixed();
        }
    }
};


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
    JavaContext(JavaContext& c, Obj parent, Property const& prop)
            : m_env(c.m_env)
            , realm(c.realm)
            , m_parent(std::move(parent))
            , m_property(&prop)
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
    util::Optional<JavaValue> value_for_property(JavaValue& dict,
                                                 Property const& prop,
                                                 size_t /*property_index*/) const
    {
        const std::map<ColKey, JavaValue>& map = dict.get_property_list();
        auto it = map.find(prop.column_key);
        return it == map.end() ? util::none : util::make_optional(it->second);
    }

    // Get the default value for the given property in the given object schema,
    // or `util::none` if there is none (which is distinct from the default
    // being `null`).
    //
    // This implementation does not support default values; see the default
    // value tests for an example of one which does.
    util::Optional<JavaValue>
    default_value_for_property(ObjectSchema const&, Property const&) const
    {
        return util::none;
    }

    // Convert from core types to the boxed type. These are currently not used as Proxy objects read
    // directly from the Row objects. This implementation is thus only here as a reminder of which
    // method signatures to add if needed.
    // JavaValueType box(BinaryData v) const { return reinterpret_cast<jobject>(JavaClassGlobalDef::new_byte_array(m_env, v)); }
    // JavaValueType box(List /*v*/) const { REALM_TERMINATE("'List' not implemented"); }
    // JavaValueType box(Object /*v*/) const { REALM_TERMINATE("'Object' not implemented"); }
    // JavaValueType box(Results /*v*/) const { REALM_TERMINATE("'Results' not implemented"); }
    // JavaValueType box(StringData v) const { return reinterpret_cast<jobject>(to_jstring(m_env, v)); }
    // JavaValueType box(Timestamp v) const { return JavaClassGlobalDef::new_date(m_env, v); }
    // JavaValueType box(bool v) const { return _impl::JavaClassGlobalDef::new_boolean(m_env, v); }
    // JavaValueType box(double v) const { return _impl::JavaClassGlobalDef::new_double(m_env, v); }
    // JavaValueType box(float v) const { return _impl::JavaClassGlobalDef::new_float(m_env, v); }
    // JavaValueType box(int64_t v) const { return _impl::JavaClassGlobalDef::new_long(m_env, v); }
    // JavaValueType box(util::Optional<bool> v) const { return v ? _impl::JavaClassGlobalDef::new_boolean(m_env, v.value()) : nullptr; }
    // JavaValueType box(util::Optional<double> v) const { return v ? _impl::JavaClassGlobalDef::new_double(m_env, v.value()) : nullptr; }
    // JavaValueType box(util::Optional<float> v) const { return v ? _impl::JavaClassGlobalDef::new_float(m_env, v.value()) : nullptr; }
    // JavaValueType box(util::Optional<int64_t> v) const { return v ? _impl::JavaClassGlobalDef::new_long(m_env, v.value()) : nullptr; }
    // JavaValueType box(RowExpr) const { REALM_TERMINATE("'RowExpr' not implemented"); }

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
    T unbox(JavaValue const& /*v*/, CreatePolicy = CreatePolicy::Skip, ObjKey /*current_row*/ = ObjKey()) const
    {
        throw std::logic_error("Missing template specialization"); // All types should have specialized templates
    }

    Obj unbox_embedded(JavaValue const& v, CreatePolicy policy, Obj& parent, ColKey col, size_t ndx) const;

    bool is_null(JavaValue const& v) const noexcept { return !v.has_value(); }
    JavaValue null_value() const noexcept { return {}; }
    util::Optional<JavaValue> no_value() const noexcept { return {}; }

    // Hooks which will be called before and after modifying a property from
    // within Object::create(). These are not currently used.
    void will_change(Object const&, Property const&) {}
    void did_change() {}

    // Get a string representation of the given value for use in error messages.
    // This method is currently only used when printing warnings about primary keys
    // which means the output only need to be valid for the primary key types:
    // StringData, int64_t and Optional<int64_t>
    std::string print(JavaValue const& val) const {
        return val.to_string();
    }

    // Cocoa allows supplying fewer values than there are properties when
    // creating objects using an array of values. Other bindings should not
    // mimick this behavior so just return false here.
    bool allow_missing(JavaValue const&) const { return false; }

    Obj create_embedded_object();

    // Determine if `value` boxes the same List as `list`
    bool is_same_list(List const& /*list*/, JavaValue const& /*value*/)
    {
        // Lists from Java are currently never the same as the ones found in Object Store.
        return false;
    }

    bool is_same_dictionary(const object_store::Dictionary&, JavaValue const& /*value*/){
        //TODO: Implement with sets
        return false;
    }

    bool is_same_set(object_store::Set const&, JavaValue const& /*value*/){
        //TODO: Implement with sets
        return false;
    }

    template<typename Func>
    void enumerate_collection(JavaValue& value, Func&& fn) {
        if (value.get_type() == JavaValueType::List) {
            for (const auto& v : value.get_list()) {
                fn(v);
            }
        } else {
            throw std::logic_error("Type is not a list");
        }
    }

    template<typename Func>
    void enumerate_dictionary(JavaValue& value, Func&& fn) {
        if (value.get_type() == JavaValueType::Dictionary) {
            for (const auto& v : value.get_dictionary()) {
                fn(v.first, v.second);
            }
        } else {
            throw std::logic_error("Type is not a dictionary");
        }
    }

    private:
    JNIEnv* m_env;
    std::shared_ptr<Realm> realm;
    Obj m_parent;
    const Property* m_property = nullptr;
    const ObjectSchema* object_schema = nullptr;

    inline void check_value_not_null(JavaValue const& v, const char* expected_type) const
    {
        if (!v.has_value()) {
            throw RequiredFieldValueNotProvidedException(std::string(expected_type));
        }
    }
};

template <>
inline bool JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    check_value_not_null(v, "Boolean");
    return v.get_boolean() == JNI_TRUE;
}

template <>
inline int64_t JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    check_value_not_null(v, "Long");
    return static_cast<int64_t>(v.get_int());
}

template <>
inline double JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    check_value_not_null(v, "Double");
    return static_cast<double>(v.get_double());
}

template <>
inline float JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    check_value_not_null(v, "Float");
    return static_cast<float>(v.get_float());
}

template <>
inline StringData JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    if (!v.has_value()) {
        return StringData();
    }
    return StringData(v.get_string());
}

template <>
inline BinaryData JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    if (!v.has_value()) {
        return BinaryData();
    } else {
        return v.get_binary().get();
    }
}

template <>
inline Timestamp JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    return v.has_value() ? v.get_date() : Timestamp();
}

template <>
inline Decimal128 JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    return v.has_value() ? v.get_decimal128() : Decimal128();
}

template <>
inline ObjectId JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    return v.has_value() ? v.get_object_id() : ObjectId();
}

template <>
inline UUID JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    return v.has_value() ? v.get_uuid() : UUID();
}

template <>
inline Obj JavaContext::unbox(JavaValue const& v, CreatePolicy policy, ObjKey current_row) const
{
    if (v.get_type() == JavaValueType::Object) {
        return *v.get_object();
    } else if (!policy.create) {
        return Obj();
    }
    REALM_ASSERT(object_schema);
    return Object::create(const_cast<JavaContext&>(*this), realm, *object_schema, v, policy, current_row).obj();
}

template <>
inline util::Optional<bool> JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    return v.has_value() ? util::make_optional(v.get_boolean() == JNI_TRUE) : util::none;
}

template <>
inline util::Optional<int64_t> JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    return v.has_value() ? util::make_optional(static_cast<int64_t>(v.get_int())) : util::none;
}

template <>
inline util::Optional<double> JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    return v.has_value() ? util::make_optional(v.get_double()) : util::none;
}

template <>
inline util::Optional<float> JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    return v.has_value() ? util::make_optional(v.get_float()) : util::none;
}

template <>
inline util::Optional<ObjectId> JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    return v.has_value() ? util::make_optional(v.get_object_id()) : util::none;
}

template <>
inline util::Optional<UUID> JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    return v.has_value() ? util::make_optional(v.get_uuid()) : util::none;
}

template <>
inline util::Optional<Decimal> JavaContext::unbox(JavaValue const& v, CreatePolicy, ObjKey) const
{
    return v.has_value() ? util::make_optional(v.get_decimal128()) : util::none;
}

inline Obj JavaContext::create_embedded_object() {
    return m_parent.create_and_set_linked_object(m_property->column_key);
}

inline JavaValue from_mixed(realm::Mixed mixed_value){
    if (mixed_value.is_null()) {
        return JavaValue();
    } else {
        switch (mixed_value.get_type()) {
            case type_Int:
                return JavaValue(mixed_value.get<int64_t>());
            case type_Bool:
                return JavaValue(static_cast<jboolean>(mixed_value.get<bool>()));
            case type_String:
                return JavaValue(std::string(mixed_value.get<StringData>()));
            case type_Binary:
                return JavaValue(OwnedBinaryData(mixed_value.get<BinaryData>()));
            case type_Timestamp:
                return JavaValue(mixed_value.get<Timestamp>());
            case type_Float:
                return JavaValue(mixed_value.get<float>());
            case type_Double:
                return JavaValue(mixed_value.get<double>());
            case type_Decimal:
                return JavaValue(mixed_value.get<Decimal128>());
            case type_ObjectId:
                return JavaValue(mixed_value.get<ObjectId>());
            case type_UUID:
                return JavaValue(mixed_value.get<UUID>());
            case type_TypedLink:
                return JavaValue(mixed_value.get<ObjLink>());
            case type_Mixed:
            case type_Link:
            case type_LinkList:
                return JavaValue();
        }
    }
}

}

#endif // REALM_JAVA_OBJECT_ACCESSOR_HPP
