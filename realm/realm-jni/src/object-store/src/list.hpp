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

#ifndef REALM_LIST_HPP
#define REALM_LIST_HPP

#include <realm/link_view.hpp>

#include <memory>

namespace realm {
template<typename T> class BasicRowExpr;
using RowExpr = BasicRowExpr<Table>;

class ObjectSchema;
class Realm;
class Results;
struct SortOrder;

class List {
public:
    List() noexcept;
    List(std::shared_ptr<Realm> r, const ObjectSchema& s, LinkViewRef l) noexcept;
    ~List();

    const std::shared_ptr<Realm>& get_realm() const { return m_realm; }
    Query get_query() const;
    const ObjectSchema& get_object_schema() const { return *m_object_schema; }

    bool is_valid() const;
    void verify_attached() const;
    void verify_in_transaction() const;

    size_t size() const;
    RowExpr get(size_t row_ndx) const;
    size_t find(ConstRow const& row) const;

    void add(size_t target_row_ndx);
    void insert(size_t list_ndx, size_t target_row_ndx);
    void move(size_t source_ndx, size_t dest_ndx);
    void remove(size_t list_ndx);
    void remove_all();
    void set(size_t row_ndx, size_t target_row_ndx);
    void swap(size_t ndx1, size_t ndx2);

    void delete_all();

    Results sort(SortOrder order);

    bool operator==(List const& rgt) const noexcept;

    // These are implemented in object_accessor.hpp
    template <typename ValueType, typename ContextType>
    void add(ContextType ctx, ValueType value);

    template <typename ValueType, typename ContextType>
    void insert(ContextType ctx, ValueType value, size_t list_ndx);

    template <typename ValueType, typename ContextType>
    void set(ContextType ctx, ValueType value, size_t list_ndx);

private:
    std::shared_ptr<Realm> m_realm;
    const ObjectSchema* m_object_schema;
    LinkViewRef m_link_view;

    void verify_valid_row(size_t row_ndx, bool insertion = false) const;

    friend struct std::hash<List>;
};
} // namespace realm

namespace std {
template<> struct hash<realm::List> {
    size_t operator()(realm::List const&) const;
};
}

#endif /* REALM_LIST_HPP */
