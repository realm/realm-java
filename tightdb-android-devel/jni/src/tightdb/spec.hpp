/*************************************************************************
 *
 * TIGHTDB CONFIDENTIAL
 * __________________
 *
 *  [2011] - [2012] TightDB Inc
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of TightDB Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to TightDB Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from TightDB Incorporated.
 *
 **************************************************************************/
#ifndef TIGHTDB_SPEC_HPP
#define TIGHTDB_SPEC_HPP

#include "array.hpp"
#include "array_string.hpp"
#include "column_type.hpp"

namespace tightdb {
using std::size_t;

class Spec {
public:
    Spec(Allocator& alloc);
    Spec(Allocator& alloc, ArrayParent* parent, size_t pndx);
    Spec(Allocator& alloc, size_t ref, ArrayParent *parent, size_t pndx);
    Spec(const Spec& s);

    void add_column(ColumnType type, const char* name);
    Spec add_subtable_column(const char* name);

    Spec get_subspec(size_t column_ndx);
    const Spec get_subspec(size_t column_ndx) const;
    size_t get_subspec_ref(size_t column_ndx) const;

    // Direct access to type and attribute list
    size_t get_type_attr_count() const;
    ColumnType get_type_attr(size_t column_ndx) const;

    // Column info
    size_t get_column_count() const;
    ColumnType get_column_type(size_t column_ndx) const;
    ColumnType get_real_column_type(size_t column_ndx) const;
    void set_column_type(size_t column_ndx, ColumnType type);
    const char* get_column_name(size_t column_ndx) const;
    size_t get_column_index(const char* name) const;

    // Column Attributes
    ColumnType get_column_attr(size_t column_ndx) const;
    void set_column_attr(size_t column_ndx, ColumnType attr);

#ifdef _DEBUG
    bool compare(const Spec& spec) const;
    void Verify() const; // Must be upper case to avoid conflict with macro in ObjC
    void to_dot(std::ostream& out, const char* title=NULL) const;
#endif //_DEBUG

private:
    friend class Table;

    void create(size_t ref, ArrayParent *parent, size_t pndx);
    void destroy();

    size_t get_ref() const;
    void update_ref(size_t ref, ArrayParent* parent=NULL, size_t pndx=0);

    bool update_from_parent();
    void set_parent(ArrayParent* parent, size_t pndx);

    // Serialization
    template<class S> size_t write(S& out, size_t& pos) const;

    Array m_specSet;
    Array m_spec;
    ArrayString m_names;
    Array m_subSpecs;
};


} // namespace tightdb

#endif // TIGHTDB_SPEC_HPP
