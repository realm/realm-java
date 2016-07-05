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

#include "parser.hpp"

#include <iostream>

#include <pegtl.hh>
#include <pegtl/analyze.hh>
#include <pegtl/trace.hh>

// String tokens can't be followed by [A-z0-9_].
#define string_token_t(s) seq< pegtl_istring_t(s), not_at< identifier_other > >

using namespace pegtl;

namespace realm {
namespace parser {

// strings
struct unicode : list< seq< one< 'u' >, rep< 4, must< xdigit > > >, one< '\\' > > {};
struct escaped_char : one< '"', '\'', '\\', '/', 'b', 'f', 'n', 'r', 't', '0' > {};
struct escaped : sor< escaped_char, unicode > {};
struct unescaped : utf8::range< 0x20, 0x10FFFF > {};
struct chars : if_then_else< one< '\\' >, must< escaped >, unescaped > {};
struct dq_string_content : until< at< one< '"' > >, must< chars > > {};
struct dq_string : seq< one< '"' >, must< dq_string_content >, any > {};

struct sq_string_content : until< at< one< '\'' > >, must< chars > > {};
struct sq_string : seq< one< '\'' >, must< sq_string_content >, any > {};

// numbers
struct minus : opt< one< '-' > > {};
struct dot : one< '.' > {};

struct float_num : sor<
    seq< plus< digit >, dot, star< digit > >,
    seq< star< digit >, dot, plus< digit > >
> {};
struct hex_num : seq< one< '0' >, one< 'x', 'X' >, plus< xdigit > > {};
struct int_num : plus< digit > {};

struct number : seq< minus, sor< float_num, hex_num, int_num > > {};

struct true_value : string_token_t("true") {};
struct false_value : string_token_t("false") {};
struct null_value : string_token_t("null") {};

// key paths
struct key_path : list< seq< sor< alpha, one< '_' > >, star< sor< alnum, one< '_', '-' > > > >, one< '.' > > {};

// argument
struct argument_index : plus< digit > {};
struct argument : seq< one< '$' >, must< argument_index > > {};

// expressions and operators
struct expr : sor< dq_string, sq_string, number, argument, true_value, false_value, null_value, key_path > {};
struct case_insensitive : pegtl_istring_t("[c]") {};

struct eq : seq< sor< two< '=' >, one< '=' > >, star< blank >, opt< case_insensitive > >{};
struct noteq : pegtl::string< '!', '=' > {};
struct lteq : pegtl::string< '<', '=' > {};
struct lt : one< '<' > {};
struct gteq : pegtl::string< '>', '=' > {};
struct gt : one< '>' > {};
struct contains : string_token_t("contains") {};
struct begins : string_token_t("beginswith") {};
struct ends : string_token_t("endswith") {};

struct string_oper : seq< sor< contains, begins, ends>, star< blank >, opt< case_insensitive > > {};
struct symbolic_oper : sor< eq, noteq, lteq, lt, gteq, gt > {};

// predicates
struct comparison_pred : seq< expr, pad< sor< string_oper, symbolic_oper >, blank >, expr > {};

struct pred;
struct group_pred : if_must< one< '(' >, pad< pred, blank >, one< ')' > > {};
struct true_pred : string_token_t("truepredicate") {};
struct false_pred : string_token_t("falsepredicate") {};

struct not_pre : seq< sor< one< '!' >, string_token_t("not") > > {};
struct atom_pred : seq< opt< not_pre >, pad< sor< group_pred, true_pred, false_pred, comparison_pred >, blank > > {};

struct and_op : pad< sor< two< '&' >, string_token_t("and") >, blank > {};
struct or_op : pad< sor< two< '|' >, string_token_t("or") >, blank > {};

struct or_ext : if_must< or_op, pred > {};
struct and_ext : if_must< and_op, pred > {};
struct and_pred : seq< atom_pred, star< and_ext > > {};

struct pred : seq< and_pred, star< or_ext > > {};

// state
struct ParserState
{
    std::vector<Predicate *> group_stack;

    Predicate *current_group()
    {
        return group_stack.back();
    }

    Predicate *last_predicate()
    {
        Predicate *pred = current_group();
        while (pred->type != Predicate::Type::Comparison && pred->cpnd.sub_predicates.size()) {
            pred = &pred->cpnd.sub_predicates.back();
        }
        return pred;
    }

    void add_predicate_to_current_group(Predicate::Type type)
    {
        current_group()->cpnd.sub_predicates.emplace_back(type, negate_next);
        negate_next = false;

        if (current_group()->cpnd.sub_predicates.size() > 1) {
            if (next_type == Predicate::Type::Or) {
                apply_or();
            }
            else {
                apply_and();
            }
        }
    }

    bool negate_next = false;
    Predicate::Type next_type = Predicate::Type::And;

    void add_expression(Expression && exp)
    {
        Predicate *current = last_predicate();
        if (current->type == Predicate::Type::Comparison && current->cmpr.expr[1].type == parser::Expression::Type::None) {
            current->cmpr.expr[1] = std::move(exp);
        }
        else {
            add_predicate_to_current_group(Predicate::Type::Comparison);
            last_predicate()->cmpr.expr[0] = std::move(exp);
        }
    }

    void apply_or()
    {
        Predicate *group = current_group();
        if (group->type == Predicate::Type::Or) {
            return;
        }

        // convert to OR
        group->type = Predicate::Type::Or;
        if (group->cpnd.sub_predicates.size() > 2) {
            // split the current group into an AND group ORed with the last subpredicate
            Predicate new_sub(Predicate::Type::And);
            new_sub.cpnd.sub_predicates = std::move(group->cpnd.sub_predicates);

            group->cpnd.sub_predicates = { new_sub, std::move(new_sub.cpnd.sub_predicates.back()) };
            group->cpnd.sub_predicates[0].cpnd.sub_predicates.pop_back();
        }
    }

    void apply_and()
    {
        if (current_group()->type == Predicate::Type::And) {
            return;
        }

        auto &sub_preds = current_group()->cpnd.sub_predicates;
        auto second_last = sub_preds.end() - 2;
        if (second_last->type == Predicate::Type::And && !second_last->negate) {
            // make a new and group populated with the last two predicates
            second_last->cpnd.sub_predicates.push_back(std::move(sub_preds.back()));
            sub_preds.pop_back();
        }
        else {
            // otherwise combine last two into a new AND group
            Predicate pred(Predicate::Type::And);
            pred.cpnd.sub_predicates.insert(pred.cpnd.sub_predicates.begin(), second_last, sub_preds.end());
            sub_preds.erase(second_last, sub_preds.end());
            sub_preds.emplace_back(std::move(pred));
        }
    }
};

// rules
template< typename Rule >
struct action : nothing< Rule > {};

#ifdef REALM_PARSER_PRINT_TOKENS
    #define DEBUG_PRINT_TOKEN(string) std::cout << string << std::endl
#else
    #define DEBUG_PRINT_TOKEN(string)
#endif

template<> struct action< and_op >
{
    static void apply( const input & in, ParserState & state )
    {
        DEBUG_PRINT_TOKEN("<and>");
        state.next_type = Predicate::Type::And;
    }
};

template<> struct action< or_op >
{
    static void apply( const input & in, ParserState & state )
    {
        DEBUG_PRINT_TOKEN("<or>");
        state.next_type = Predicate::Type::Or;
    }
};


#define EXPRESSION_ACTION(rule, type)                               \
template<> struct action< rule > {                                  \
    static void apply( const input & in, ParserState & state ) {    \
        DEBUG_PRINT_TOKEN(in.string());                             \
        state.add_expression(Expression(type, in.string())); }};

EXPRESSION_ACTION(dq_string_content, Expression::Type::String)
EXPRESSION_ACTION(sq_string_content, Expression::Type::String)
EXPRESSION_ACTION(key_path, Expression::Type::KeyPath)
EXPRESSION_ACTION(number, Expression::Type::Number)
EXPRESSION_ACTION(true_value, Expression::Type::True)
EXPRESSION_ACTION(false_value, Expression::Type::False)
EXPRESSION_ACTION(null_value, Expression::Type::Null)
EXPRESSION_ACTION(argument_index, Expression::Type::Argument)
    
template<> struct action< true_pred >
{
    static void apply( const input & in, ParserState & state )
    {
        DEBUG_PRINT_TOKEN(in.string());
        state.current_group()->cpnd.sub_predicates.emplace_back(Predicate::Type::True);
    }
};

template<> struct action< false_pred >
{
    static void apply( const input & in, ParserState & state )
    {
        DEBUG_PRINT_TOKEN(in.string());
        state.current_group()->cpnd.sub_predicates.emplace_back(Predicate::Type::False);
    }
};

#define OPERATOR_ACTION(rule, oper)                                 \
template<> struct action< rule > {                                  \
    static void apply( const input & in, ParserState & state ) {    \
        DEBUG_PRINT_TOKEN(in.string());                             \
        state.last_predicate()->cmpr.op = oper; }};

OPERATOR_ACTION(eq, Predicate::Operator::Equal)
OPERATOR_ACTION(noteq, Predicate::Operator::NotEqual)
OPERATOR_ACTION(gteq, Predicate::Operator::GreaterThanOrEqual)
OPERATOR_ACTION(gt, Predicate::Operator::GreaterThan)
OPERATOR_ACTION(lteq, Predicate::Operator::LessThanOrEqual)
OPERATOR_ACTION(lt, Predicate::Operator::LessThan)
OPERATOR_ACTION(begins, Predicate::Operator::BeginsWith)
OPERATOR_ACTION(ends, Predicate::Operator::EndsWith)
OPERATOR_ACTION(contains, Predicate::Operator::Contains)
    
template<> struct action< case_insensitive >
{
    static void apply( const input & in, ParserState & state )
    {
        DEBUG_PRINT_TOKEN(in.string());
        state.last_predicate()->cmpr.option = Predicate::OperatorOption::CaseInsensitive;
    }
};
    
template<> struct action< one< '(' > >
{
    static void apply( const input & in, ParserState & state )
    {
        DEBUG_PRINT_TOKEN("<begin_group>");
        state.add_predicate_to_current_group(Predicate::Type::And);
        state.group_stack.push_back(state.last_predicate());
    }
};

template<> struct action< group_pred >
{
    static void apply( const input & in, ParserState & state )
    {
        DEBUG_PRINT_TOKEN("<end_group>");
        state.group_stack.pop_back();
    }
};

template<> struct action< not_pre >
{
    static void apply( const input & in, ParserState & state )
    {
        DEBUG_PRINT_TOKEN("<not>");
        state.negate_next = true;
    }
};

template< typename Rule >
struct error_message_control : pegtl::normal< Rule >
{
    static const std::string error_message;

    template< typename Input, typename ... States >
    static void raise( const Input & in, States && ... )
    {
        throw pegtl::parse_error( error_message, in );
    }
};

template<>
const std::string error_message_control< chars >::error_message = "Invalid characters in string constant.";

template< typename Rule>
const std::string error_message_control< Rule >::error_message = "Invalid predicate.";

Predicate parse(const std::string &query)
{
    DEBUG_PRINT_TOKEN(query);

    Predicate out_predicate(Predicate::Type::And);

    ParserState state;
    state.group_stack.push_back(&out_predicate);

    pegtl::parse< must< pred, eof >, action, error_message_control >(query, query, state);
    if (out_predicate.type == Predicate::Type::And && out_predicate.cpnd.sub_predicates.size() == 1) {
        return std::move(out_predicate.cpnd.sub_predicates.back());
    }
    return out_predicate;
}

void analyze_grammar()
{
    analyze<pred>();
}

}}


