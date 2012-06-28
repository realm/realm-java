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
#ifndef TIGHTDB_TABLE_HPP
#define TIGHTDB_TABLE_HPP

#include "column_fwd.hpp"
#include "table_ref.hpp"
#include "spec.hpp"
#include "mixed.hpp"

namespace tightdb {

using std::size_t;
using std::time_t;

class TableView;
class ConstTableView;


/**
 * The Table class is non-polymorphic, that is, it has no virtual
 * functions. This is important because it ensures that there is no
 * run-time distinction between a Table instance and an instance of
 * any variation of BasicTable<T>, and this, in turn, makes it valid
 * to cast a pointer from Table to BasicTable<T> even when the
 * instance is constructed as a Table. Of couse, this also assumes
 * that BasicTable<> is non-polymorphic, has no destructor, and adds
 * no extra data members.
 */
class Table {
public:
    // Construct a new top-level table with an independent schema.
    Table(Allocator& alloc = GetDefaultAllocator());
    ~Table();

    // Schema handling (see also Spec.hpp)
    Spec&       get_spec();
    const Spec& get_spec() const;
    void        update_from_spec(); // Must not be called for a table with shared schema
                // Add a column dynamically
    size_t      add_column(ColumnType type, const char* name);

    // Table size and deletion
    bool        is_empty() const {return m_size == 0;}
    size_t      size() const {return m_size;}
    void        clear();

    // Column information
    size_t      get_column_count() const;
    const char* get_column_name(size_t column_ndx) const;
    size_t      get_column_index(const char* name) const;
    ColumnType  get_column_type(size_t column_ndx) const;

    // Row handling
    size_t      add_empty_row(size_t num_of_rows = 1);
    void        insert_empty_row(size_t row_ndx, size_t num_of_rows = 1);
    void        remove(size_t row_ndx);
    void        remove_last() {if (!is_empty()) remove(m_size-1);}

    // Insert row
    // NOTE: You have to insert values in ALL columns followed by insert_done().
    void insert_int(size_t column_ndx, size_t row_ndx, int64_t value);
    void insert_bool(size_t column_ndx, size_t row_ndx, bool value);
    void insert_date(size_t column_ndx, size_t row_ndx, time_t value);
    template<class E> void insert_enum(size_t column_ndx, size_t row_ndx, E value);
    void insert_string(size_t column_ndx, size_t row_ndx, const char* value);
    void insert_mixed(size_t column_ndx, size_t row_ndx, Mixed value);
    void insert_binary(size_t column_ndx, size_t row_ndx, const char* value, size_t len);
    void insert_done();

    // Get cell values
    int64_t     get_int(size_t column_ndx, size_t row_ndx) const;
    bool        get_bool(size_t column_ndx, size_t row_ndx) const;
    time_t      get_date(size_t column_ndx, size_t row_ndx) const;
    const char* get_string(size_t column_ndx, size_t row_ndx) const;
    BinaryData  get_binary(size_t column_ndx, size_t row_ndx) const;
    Mixed       get_mixed(size_t column_ndx, size_t row_ndx) const;
    ColumnType  get_mixed_type(size_t column_ndx, size_t row_ndx) const;

    // Set cell values
    void set_int(size_t column_ndx, size_t row_ndx, int64_t value);
    void set_bool(size_t column_ndx, size_t row_ndx, bool value);
    void set_date(size_t column_ndx, size_t row_ndx, time_t value);
    template<class E> void set_enum(size_t column_ndx, size_t row_ndx, E value);
    void set_string(size_t column_ndx, size_t row_ndx, const char* value);
    void set_binary(size_t column_ndx, size_t row_ndx, const char* value, size_t len);
    void set_mixed(size_t column_ndx, size_t row_ndx, Mixed value);
    void add_int(size_t column_ndx, int64_t value);

    // Sub-tables (works on columns whose type is either 'subtable' or 'mixed', for a value in a mixed column that is not a subtable, get_subtable() returns null, get_subtable_size() returns zero, and clear_subtable() does nothing.)
    TableRef        get_subtable(size_t column_ndx, size_t row_ndx);
    ConstTableRef   get_subtable(size_t column_ndx, size_t row_ndx) const;
    size_t          get_subtable_size(size_t column_ndx, size_t row_ndx) const;
    void            clear_subtable(size_t column_ndx, size_t row_ndx);
    void            insert_subtable(size_t column_ndx, size_t row_ndx); // Insert empty table

    // Indexing
    bool has_index(size_t column_ndx) const;
    void set_index(size_t column_ndx);

    // Aggregate functions
    int64_t sum(size_t column_ndx) const;
    int64_t maximum(size_t column_ndx) const;
    int64_t minimum(size_t column_ndx) const;

    // Searching
    size_t         find_first_int(size_t column_ndx, int64_t value) const;
    size_t         find_first_bool(size_t column_ndx, bool value) const;
    size_t         find_first_date(size_t column_ndx, time_t value) const;
    size_t         find_first_string(size_t column_ndx, const char* value) const;
    // FIXME: Need: size_t find_first_binary(size_t column_ndx, const char* value, size_t len) const;

    TableView      find_all_int(size_t column_ndx, int64_t value);
    ConstTableView find_all_int(size_t column_ndx, int64_t value) const;
    TableView      find_all_bool(size_t column_ndx, bool value);
    ConstTableView find_all_bool(size_t column_ndx, bool value) const;
    TableView      find_all_date(size_t column_ndx, time_t value);
    ConstTableView find_all_date(size_t column_ndx, time_t value) const;
    TableView      find_all_string(size_t column_ndx, const char* value);
    ConstTableView find_all_string(size_t column_ndx, const char* value) const;
    // FIXME: Need: TableView find_all_binary(size_t column_ndx, const char* value, size_t len);
    // FIXME: Need: ConstTableView find_all_binary(size_t column_ndx, const char* value, size_t len) const;

    TableView      get_sorted_view(size_t column_ndx, bool ascending=true);
    ConstTableView get_sorted_view(size_t column_ndx, bool ascending=true) const;

    // Optimizing
    void optimize();

    // Conversion
    void to_json(std::ostream& out);

    // Get a reference to this table
    TableRef get_table_ref() { return TableRef(this); }
    ConstTableRef get_table_ref() const { return ConstTableRef(this); }

    // Debug
#ifdef _DEBUG
    bool compare(const Table& c) const;
    void Verify() const; // Must be upper case to avoid conflict with macro in ObjC
    void to_dot(std::ostream& out, const char* title=NULL) const;
    void print() const;
    MemStats stats() const;
#endif //_DEBUG

    // todo, note, these three functions have been protected
    const ColumnBase& GetColumnBase(size_t column_ndx) const;
    ColumnType GetRealColumnType(size_t column_ndx) const;

    class Parent;

protected:
    size_t find_pos_int(size_t column_ndx, int64_t value) const;

    // FIXME: Most of the things that are protected here, could instead be private
    // Direct Column access
    Column& GetColumn(size_t column_ndx);
    const Column& GetColumn(size_t column_ndx) const;
    AdaptiveStringColumn& GetColumnString(size_t column_ndx);
    const AdaptiveStringColumn& GetColumnString(size_t column_ndx) const;
    ColumnBinary& GetColumnBinary(size_t column_ndx);
    const ColumnBinary& GetColumnBinary(size_t column_ndx) const;
    ColumnStringEnum& GetColumnStringEnum(size_t column_ndx);
    const ColumnStringEnum& GetColumnStringEnum(size_t column_ndx) const;
    ColumnTable& GetColumnTable(size_t column_ndx);
    const ColumnTable& GetColumnTable(size_t column_ndx) const;
    ColumnMixed& GetColumnMixed(size_t column_ndx);
    const ColumnMixed& GetColumnMixed(size_t column_ndx) const;


    /**
     * Construct a top-level table with independent schema from ref.
     */
    Table(Allocator& alloc, size_t top_ref, Parent* parent, size_t ndx_in_parent);

    /**
     * Used when constructing subtables, that is, tables whose
     * lifetime is managed by reference counting, and not by the
     * application.
     */
    class SubtableTag {};

    /**
     * Construct a subtable with independent schema from ref.
     */
    Table(SubtableTag, Allocator& alloc, size_t top_ref,
          Parent* parent, size_t ndx_in_parent);

    /**
     * Construct a subtable with shared schema from ref.
     *
     * It is possible to construct a 'null' table by passing zero for
     * columns_ref, in this case the columns will be created on
     * demand.
     */
    Table(SubtableTag, Allocator& alloc, size_t schema_ref, size_t columns_ref,
          Parent* parent, size_t ndx_in_parent);

    void Create(size_t ref_specSet, size_t ref_columns,
                ArrayParent* parent, size_t ndx_in_parent);
    void CreateColumns();
    void CacheColumns();
    void ClearCachedColumns();

    // Specification
    size_t GetColumnRefPos(size_t column_ndx) const;
    void UpdateColumnRefs(size_t column_ndx, int diff);
    void UpdateFromParent();


#ifdef _DEBUG
    void ToDotInternal(std::ostream& out) const;
#endif //_DEBUG

    // Member variables
    size_t m_size;

    // On-disk format
    Array m_top;
    Array m_columns;
    Spec m_spec_set;

    // Cached columns
    Array m_cols;

    /**
     * Get the subtable at the specified column and row index.
     *
     * The returned table pointer must always end up being wrapped in
     * a TableRef.
     */
    Table *get_subtable_ptr(size_t col_idx, size_t row_idx);

    /**
     * Get the subtable at the specified column and row index.
     *
     * The returned table pointer must always end up being wrapped in
     * a ConstTableRef.
     */
    const Table *get_subtable_ptr(size_t col_idx, size_t row_idx) const;

private:
    Table(Table const &); // Disable copy construction
    Table &operator=(Table const &); // Disable copying assignment

    mutable size_t m_ref_count;
    void bind_ref() const { ++m_ref_count; }
    void unbind_ref() const { if (--m_ref_count == 0) delete this; }

    ColumnBase& GetColumnBase(size_t column_ndx);
    void InstantiateBeforeChange();

    /**
     * Construct a table with independent schema and return just the
     * reference to the underlying memory.
     */
    static size_t create_table(Allocator&);

    // Experimental
    TableView find_all_hamming(size_t column_ndx, uint64_t value, size_t max);
    ConstTableView find_all_hamming(size_t column_ndx, uint64_t value, size_t max) const;

    friend class Group;
    friend class Query;
    friend class ColumnMixed;
    template<class> friend class bind_ptr;
    friend class ColumnSubtableParent;
    friend class LangBindHelper;
};



class Table::Parent: public ArrayParent {
protected:
    friend class Table;

    /**
     * Must be called whenever a child Table is destroyed.
     */
    virtual void child_destroyed(size_t child_ndx) = 0;
};




// Implementation:

inline void Table::insert_bool(size_t column_ndx, size_t row_ndx, bool value)
{
    insert_int(column_ndx, row_ndx, value);
}

inline void Table::insert_date(size_t column_ndx, size_t row_ndx, time_t value)
{
    insert_int(column_ndx, row_ndx, value);
}

template<class E> inline void Table::insert_enum(size_t column_ndx, size_t row_ndx, E value)
{
    insert_int(column_ndx, row_ndx, value);
}

template<class E> inline void Table::set_enum(size_t column_ndx, size_t row_ndx, E value)
{
    set_int(column_ndx, row_ndx, value);
}

inline TableRef Table::get_subtable(size_t column_ndx, size_t row_ndx)
{
    return TableRef(get_subtable_ptr(column_ndx, row_ndx));
}

inline ConstTableRef Table::get_subtable(size_t column_ndx, size_t row_ndx) const
{
    return ConstTableRef(get_subtable_ptr(column_ndx, row_ndx));
}


} // namespace tightdb

#endif // TIGHTDB_TABLE_HPP
