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

#ifndef REALM_QUERY_BUILDER_HPP
#define REALM_QUERY_BUILDER_HPP

#include "parser.hpp"
#include "object_accessor.hpp"

#include <realm/util/to_string.hpp>

namespace realm {
class Query;
class Schema;

namespace query_builder {
class Arguments;

void apply_predicate(Query &query, const parser::Predicate &predicate, Arguments &arguments,
                     const Schema &schema, const std::string &objectType);

class Arguments {
  public:
    virtual bool bool_for_argument(size_t argument_index) = 0;
    virtual long long long_for_argument(size_t argument_index) = 0;
    virtual float float_for_argument(size_t argument_index) = 0;
    virtual double double_for_argument(size_t argument_index) = 0;
    virtual std::string string_for_argument(size_t argument_index) = 0;
    virtual std::string binary_for_argument(size_t argument_index) = 0;
    virtual Timestamp timestamp_for_argument(size_t argument_index) = 0;
    virtual size_t object_index_for_argument(size_t argument_index) = 0;
    virtual bool is_argument_null(size_t argument_index) = 0;
};

template<typename ValueType, typename ContextType>
class ArgumentConverter : public Arguments {
  public:
    ArgumentConverter(ContextType context, std::vector<ValueType> arguments) : m_arguments(arguments), m_ctx(context) {}

    using Accessor = realm::NativeAccessor<ValueType, ContextType>;
    virtual bool bool_for_argument(size_t argument_index) { return Accessor::to_bool(m_ctx, argument_at(argument_index)); }
    virtual long long long_for_argument(size_t argument_index) { return Accessor::to_long(m_ctx, argument_at(argument_index)); }
    virtual float float_for_argument(size_t argument_index) { return Accessor::to_float(m_ctx, argument_at(argument_index)); }
    virtual double double_for_argument(size_t argument_index) { return Accessor::to_double(m_ctx, argument_at(argument_index)); }
    virtual std::string string_for_argument(size_t argument_index) { return Accessor::to_string(m_ctx, argument_at(argument_index)); }
    virtual std::string binary_for_argument(size_t argument_index) { return Accessor::to_binary(m_ctx, argument_at(argument_index)); }
    virtual Timestamp timestamp_for_argument(size_t argument_index) { return Accessor::to_timestamp(m_ctx, argument_at(argument_index)); }
    virtual size_t object_index_for_argument(size_t argument_index) { return Accessor::to_existing_object_index(m_ctx, argument_at(argument_index)); }
    virtual bool is_argument_null(size_t argument_index) { return Accessor::is_null(m_ctx, argument_at(argument_index)); }

  private:
    std::vector<ValueType> m_arguments;
    ContextType m_ctx;

    ValueType &argument_at(size_t index) {
        if (index >= m_arguments.size()) {
            throw std::out_of_range((std::string)"Argument index " + util::to_string(index) + " out of range 0.." + util::to_string(m_arguments.size()-1));
        }
        return m_arguments[index];
    }
};
}
}

#endif // REALM_QUERY_BUILDER_HPP
