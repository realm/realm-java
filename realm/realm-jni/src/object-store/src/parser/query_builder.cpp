////////////////////////////////////////////////////////////////////////////
//
// Copyright 2015 Realm Inc.
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

#include "query_builder.hpp"
#include "parser.hpp"

#include <realm.hpp>
#include "object_store.hpp"
#include "schema.hpp"

#include <assert.h>

namespace realm {
namespace query_builder {
using namespace parser;

template<typename T>
T stot(const std::string s) {
    std::istringstream iss(s);
    T value;
    iss >> value;
    if (iss.fail()) {
        throw std::invalid_argument("Cannot convert string '" + s + "'");
    }
    return value;
}

// check a precondition and throw an exception if it is not met
// this should be used iff the condition being false indicates a bug in the caller
// of the function checking its preconditions
#define precondition(condition, message) if (!__builtin_expect(condition, 1)) {  throw std::runtime_error(message); }

// FIXME: TrueExpression and FalseExpression should be supported by core in some way
struct TrueExpression : realm::Expression {
    size_t find_first(size_t start, size_t end) const override
    {
        if (start != end)
            return start;

        return realm::not_found;
    }
    void set_base_table(const Table*) override {}
    const Table* get_base_table() const override { return nullptr; }
    std::unique_ptr<Expression> clone(QueryNodeHandoverPatches*) const override
    {
        return std::unique_ptr<Expression>(new TrueExpression(*this));
    }
};

struct FalseExpression : realm::Expression {
    size_t find_first(size_t, size_t) const override { return realm::not_found; }
    void set_base_table(const Table*) override {}
    const Table* get_base_table() const override { return nullptr; }
    std::unique_ptr<Expression> clone(QueryNodeHandoverPatches*) const override
    {
        return std::unique_ptr<Expression>(new FalseExpression(*this));
    }
};

using KeyPath = std::vector<std::string>;
KeyPath key_path_from_string(const std::string &s) {
    std::stringstream ss(s);
    std::string item;
    KeyPath key_path;
    while (std::getline(ss, item, '.')) {
        key_path.push_back(item);
    }
    return key_path;
}

struct PropertyExpression
{
    const Property *prop = nullptr;
    std::vector<size_t> indexes;
    std::function<Table *()> table_getter;

    PropertyExpression(Query &query, const Schema &schema, Schema::const_iterator desc, const std::string &key_path_string)
    {
        KeyPath key_path = key_path_from_string(key_path_string);
        for (size_t index = 0; index < key_path.size(); index++) {
            if (prop) {
                precondition(prop->type == PropertyTypeObject || prop->type == PropertyTypeArray,
                             (std::string)"Property '" + key_path[index] + "' is not a link in object of type '" + desc->name + "'");
                indexes.push_back(prop->table_column);

            }
            prop = desc->property_for_name(key_path[index]);
            precondition(prop != nullptr, "No property '" + key_path[index] + "' on object of type '" + desc->name + "'");

            if (prop->object_type.size()) {
                desc = schema.find(prop->object_type);
            }
        }

        table_getter = [&] {
            TableRef& tbl = query.get_table();
            for (size_t col : indexes) {
                tbl->link(col); // mutates m_link_chain on table
            }
            return tbl.get();
        };
    }
};


// add a clause for numeric constraints based on operator type
template <typename A, typename B>
void add_numeric_constraint_to_query(Query& query,
                                     Predicate::Operator operatorType,
                                     A lhs,
                                     B rhs)
{
    switch (operatorType) {
        case Predicate::Operator::LessThan:
            query.and_query(lhs < rhs);
            break;
        case Predicate::Operator::LessThanOrEqual:
            query.and_query(lhs <= rhs);
            break;
        case Predicate::Operator::GreaterThan:
            query.and_query(lhs > rhs);
            break;
        case Predicate::Operator::GreaterThanOrEqual:
            query.and_query(lhs >= rhs);
            break;
        case Predicate::Operator::Equal:
            query.and_query(lhs == rhs);
            break;
        case Predicate::Operator::NotEqual:
            query.and_query(lhs != rhs);
            break;
        default:
            throw std::runtime_error("Unsupported operator for numeric queries.");
    }
}

template <typename A, typename B>
void add_bool_constraint_to_query(Query &query, Predicate::Operator operatorType, A lhs, B rhs) {
    switch (operatorType) {
        case Predicate::Operator::Equal:
            query.and_query(lhs == rhs);
            break;
        case Predicate::Operator::NotEqual:
            query.and_query(lhs != rhs);
            break;
        default:
            throw std::runtime_error("Unsupported operator for numeric queries.");
    }
}

void add_string_constraint_to_query(Query &query,
                                    Predicate::Operator op,
                                    Columns<String> &&column,
                                    std::string &&value) {
    bool case_sensitive = true;
    switch (op) {
        case Predicate::Operator::BeginsWith:
            query.and_query(column.begins_with(value, case_sensitive));
            break;
        case Predicate::Operator::EndsWith:
            query.and_query(column.ends_with(value, case_sensitive));
            break;
        case Predicate::Operator::Contains:
            query.and_query(column.contains(value, case_sensitive));
            break;
        case Predicate::Operator::Equal:
            query.and_query(column.equal(value, case_sensitive));
            break;
        case Predicate::Operator::NotEqual:
            query.and_query(column.not_equal(value, case_sensitive));
            break;
        default:
            throw std::runtime_error("Unsupported operator for string queries.");
    }
}

void add_string_constraint_to_query(realm::Query &query,
                                    Predicate::Operator op,
                                    std::string &&value,
                                    Columns<String> &&column) {
    bool case_sensitive = true;
    switch (op) {
        case Predicate::Operator::Equal:
            query.and_query(column.equal(value, case_sensitive));
            break;
        case Predicate::Operator::NotEqual:
            query.and_query(column.not_equal(value, case_sensitive));
            break;
        default:
            throw std::runtime_error("Substring comparison not supported for keypath substrings.");
    }
}

void add_binary_constraint_to_query(Query &query,
                                    Predicate::Operator op,
                                    Columns<Binary> &&column,
                                    std::string &&value) {
    switch (op) {
        case Predicate::Operator::BeginsWith:
            query.begins_with(column.m_column, BinaryData(value));
            break;
        case Predicate::Operator::EndsWith:
            query.ends_with(column.m_column, BinaryData(value));
            break;
        case Predicate::Operator::Contains:
            query.contains(column.m_column, BinaryData(value));
            break;
        case Predicate::Operator::Equal:
            query.equal(column.m_column, BinaryData(value));
            break;
        case Predicate::Operator::NotEqual:
            query.not_equal(column.m_column, BinaryData(value));
            break;
        default:
            throw std::runtime_error("Unsupported operator for binary queries.");
    }
}

void add_binary_constraint_to_query(realm::Query &query,
                                    Predicate::Operator op,
                                    std::string value,
                                    Columns<Binary> &&column) {
    switch (op) {
        case Predicate::Operator::Equal:
            query.equal(column.m_column, BinaryData(value));
            break;
        case Predicate::Operator::NotEqual:
            query.not_equal(column.m_column, BinaryData(value));
            break;
        default:
            throw std::runtime_error("Substring comparison not supported for keypath substrings.");
    }
}

void add_link_constraint_to_query(realm::Query &query,
                                  Predicate::Operator op,
                                  const PropertyExpression &prop_expr,
                                  size_t row_index) {
    precondition(prop_expr.indexes.empty(), "KeyPath queries not supported for object comparisons.");
    switch (op) {
        case Predicate::Operator::NotEqual:
            query.Not();
        case Predicate::Operator::Equal: {
            size_t col = prop_expr.prop->table_column;
            query.links_to(col, query.get_table()->get_link_target(col)->get(row_index));
            break;
        }
        default:
            throw std::runtime_error("Only 'equal' and 'not equal' operators supported for object comparison.");
    }
}

void add_link_constraint_to_query(realm::Query &query,
                                  Predicate::Operator op,
                                  PropertyExpression &prop_expr,
                                  realm::null) {
    precondition(prop_expr.indexes.empty(), "KeyPath queries not supported for object comparisons.");
    switch (op) {
        case Predicate::Operator::NotEqual:
            query.Not();
        case Predicate::Operator::Equal:
            query.and_query(query.get_table()->column<Link>(prop_expr.prop->table_column).is_null());
            break;
        default:
            throw std::runtime_error("Only 'equal' and 'not equal' operators supported for object comparison.");
    }
}

auto link_argument(const PropertyExpression &propExpr, const parser::Expression &argExpr, Arguments &args)
{
    return args.object_index_for_argument(stot<int>(argExpr.s));
}

auto link_argument(const parser::Expression &argExpr, const PropertyExpression &propExpr, Arguments &args)
{
    return args.object_index_for_argument(stot<int>(argExpr.s));
}


template <typename RetType, typename TableGetter>
struct ColumnGetter {
    static Columns<RetType> convert(TableGetter&& table, const PropertyExpression & expr, Arguments &args)
    {
        return table()->template column<RetType>(expr.prop->table_column);
    }
};

template <typename RequestedType, typename TableGetter>
struct ValueGetter;

template <typename TableGetter>
struct ValueGetter<DateTime, TableGetter> {
    static Int convert(TableGetter&&, const parser::Expression & value, Arguments &args)
    {
        if (value.type != parser::Expression::Type::Argument) {
            throw std::runtime_error("You must pass in a date argument to compare");
        }
        DateTime dt = args.datetime_for_argument(stot<int>(value.s));
        return dt.get_datetime();
    }
};

template <typename TableGetter>
struct ValueGetter<bool, TableGetter> {
    static bool convert(TableGetter&&, const parser::Expression & value, Arguments &args)
    {
        if (value.type == parser::Expression::Type::Argument) {
            return args.bool_for_argument(stot<int>(value.s));
        }
        if (value.type != parser::Expression::Type::True && value.type != parser::Expression::Type::False) {
            throw std::runtime_error("Attempting to compare bool property to a non-bool value");
        }
        return value.type == parser::Expression::Type::True;
    }
};

template <typename TableGetter>
struct ValueGetter<Double, TableGetter> {
    static Double convert(TableGetter&&, const parser::Expression & value, Arguments &args)
    {
        if (value.type == parser::Expression::Type::Argument) {
            return args.double_for_argument(stot<int>(value.s));
        }
        return stot<double>(value.s);
    }
};

template <typename TableGetter>
struct ValueGetter<Float, TableGetter> {
    static Float convert(TableGetter&&, const parser::Expression & value, Arguments &args)
    {
        if (value.type == parser::Expression::Type::Argument) {
            return args.float_for_argument(stot<int>(value.s));
        }
        return stot<float>(value.s);
    }
};

template <typename TableGetter>
struct ValueGetter<Int, TableGetter> {
    static Int convert(TableGetter&&, const parser::Expression & value, Arguments &args)
    {
        if (value.type == parser::Expression::Type::Argument) {
            return args.long_for_argument(stot<int>(value.s));
        }
        return stot<long long>(value.s);
    }
};

template <typename TableGetter>
struct ValueGetter<String, TableGetter> {
    static std::string convert(TableGetter&&, const parser::Expression & value, Arguments &args)
    {
        if (value.type == parser::Expression::Type::Argument) {
            return args.string_for_argument(stot<int>(value.s));
        }
        if (value.type != parser::Expression::Type::String) {
            throw std::runtime_error("Attempting to compare String property to a non-String value");
        }
        return value.s;
    }
};

template <typename TableGetter>
struct ValueGetter<Binary, TableGetter> {
    static std::string convert(TableGetter&&, const parser::Expression & value, Arguments &args)
    {
        if (value.type == parser::Expression::Type::Argument) {
            return args.binary_for_argument(stot<int>(value.s));
        }
        throw std::runtime_error("Binary properties must be compared against a binary argument.");
    }
};

template <typename RetType, typename Value, typename TableGetter>
auto value_of_type_for_query(TableGetter&& tables, Value&& value, Arguments &args)
{
    const bool isColumn = std::is_same<PropertyExpression, typename std::remove_reference<Value>::type>::value;
    using helper = std::conditional_t<isColumn, ColumnGetter<RetType, TableGetter>, ValueGetter<RetType, TableGetter>>;
    return helper::convert(tables, value, args);
}

template <typename A, typename B>
void do_add_comparison_to_query(Query &query, const Schema &schema, const ObjectSchema &object_schema, Predicate::Operator op,
                                const PropertyExpression &expr, A &lhs, B &rhs, Arguments &args)
{
    auto type = expr.prop->type;
    switch (type) {
        case PropertyTypeBool:
            add_bool_constraint_to_query(query, op, value_of_type_for_query<bool>(expr.table_getter, lhs, args),
                                                    value_of_type_for_query<bool>(expr.table_getter, rhs, args));
            break;
        case PropertyTypeDate:
            add_numeric_constraint_to_query(query, op, value_of_type_for_query<DateTime>(expr.table_getter, lhs, args),
                                                       value_of_type_for_query<DateTime>(expr.table_getter, rhs, args));
            break;
        case PropertyTypeDouble:
            add_numeric_constraint_to_query(query, op, value_of_type_for_query<Double>(expr.table_getter, lhs, args),
                                                       value_of_type_for_query<Double>(expr.table_getter, rhs, args));
            break;
        case PropertyTypeFloat:
            add_numeric_constraint_to_query(query, op, value_of_type_for_query<Float>(expr.table_getter, lhs, args),
                                                       value_of_type_for_query<Float>(expr.table_getter, rhs, args));
            break;
        case PropertyTypeInt:
            add_numeric_constraint_to_query(query, op, value_of_type_for_query<Int>(expr.table_getter, lhs, args),
                                                       value_of_type_for_query<Int>(expr.table_getter, rhs, args));
            break;
        case PropertyTypeString:
            add_string_constraint_to_query(query, op, value_of_type_for_query<String>(expr.table_getter, lhs, args),
                                                      value_of_type_for_query<String>(expr.table_getter, rhs, args));
            break;
        case PropertyTypeData:
            add_binary_constraint_to_query(query, op, value_of_type_for_query<Binary>(expr.table_getter, lhs, args),
                                                      value_of_type_for_query<Binary>(expr.table_getter, rhs, args));
            break;
        case PropertyTypeObject:
        case PropertyTypeArray:
            add_link_constraint_to_query(query, op, expr, link_argument(lhs, rhs, args));
            break;
        default: {
            throw std::runtime_error((std::string)"Object type " + string_for_property_type(type) + " not supported");
        }
    }
}

void add_comparison_to_query(Query &query, const Predicate &pred, Arguments &args, const Schema &schema, const std::string &type)
{
    const Predicate::Comparison &cmpr = pred.cmpr;
    auto t0 = cmpr.expr[0].type, t1 = cmpr.expr[1].type;
    auto object_schema = schema.find(type);
    if (t0 == parser::Expression::Type::KeyPath && t1 != parser::Expression::Type::KeyPath) {
        PropertyExpression expr(query, schema, object_schema, cmpr.expr[0].s);
        do_add_comparison_to_query(query, schema, *object_schema, cmpr.op, expr, expr, cmpr.expr[1], args);
    }
    else if (t0 != parser::Expression::Type::KeyPath && t1 == parser::Expression::Type::KeyPath) {
        PropertyExpression expr(query, schema, object_schema, cmpr.expr[1].s);
        do_add_comparison_to_query(query, schema, *object_schema, cmpr.op, expr, cmpr.expr[0], expr, args);
    }
    else {
        throw std::runtime_error("Predicate expressions must compare a keypath and another keypath or a constant value");
    }
}

void update_query_with_predicate(Query &query, const Predicate &pred, Arguments &arguments, const Schema &schema, const std::string &type)
{
    if (pred.negate) {
        query.Not();
    }

    switch (pred.type) {
        case Predicate::Type::And:
            query.group();
            for (auto &sub : pred.cpnd.sub_predicates) {
                update_query_with_predicate(query, sub, arguments, schema, type);
            }
            if (!pred.cpnd.sub_predicates.size()) {
                query.and_query(std::unique_ptr<realm::Expression>(new TrueExpression));
            }
            query.end_group();
            break;

        case Predicate::Type::Or:
            query.group();
            for (auto &sub : pred.cpnd.sub_predicates) {
                query.Or();
                update_query_with_predicate(query, sub, arguments, schema, type);
            }
            if (!pred.cpnd.sub_predicates.size()) {
                query.and_query(std::unique_ptr<realm::Expression>(new FalseExpression));
            }
            query.end_group();
            break;

        case Predicate::Type::Comparison: {
            add_comparison_to_query(query, pred, arguments, schema, type);
            break;
        }
        case Predicate::Type::True:
            query.and_query(std::unique_ptr<realm::Expression>(new TrueExpression));
            break;

        case Predicate::Type::False:
            query.and_query(std::unique_ptr<realm::Expression>(new FalseExpression));
            break;

        default:
            throw std::runtime_error("Invalid predicate type");
    }
}

void apply_predicate(Query &query, const Predicate &predicate, Arguments &arguments, const Schema &schema, const std::string &objectType)
{
    update_query_with_predicate(query, predicate, arguments, schema, objectType);

    // Test the constructed query in core
    std::string validateMessage = query.validate();
    precondition(validateMessage.empty(), validateMessage.c_str());
}

}}
