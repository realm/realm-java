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

#include <tightdb/column_fwd.hpp>
#include <tightdb/table_ref.hpp>
#include <tightdb/spec.hpp>
#include <tightdb/mixed.hpp>

#ifdef TIGHTDB_ENABLE_REPLICATION
#include <tightdb/replication.hpp>
#endif

namespace tightdb {

using std::size_t;
using std::time_t;

class TableView;
class ConstTableView;


/// The Table class is non-polymorphic, that is, it has no virtual
/// functions. This is important because it ensures that there is no
/// run-time distinction between a Table instance and an instance of
/// any variation of BasicTable<T>, and this, in turn, makes it valid
/// to cast a pointer from Table to BasicTable<T> even when the
/// instance is constructed as a Table. Of couse, this also assumes
/// that BasicTable<> is non-polymorphic, has no destructor, and adds
/// no extra data members.
///
/// FIXME: Table copying (from any group to any group) could be made
/// aliasing safe as follows: Start by cloning source table into
/// target allocator. On success, assign, and then deallocate any
/// previous structure at the target.
///
/// FIXME: It might be desirable to have a 'table move' feature
/// between two places inside the same group (say from a subtable or a
/// mixed column to group level). This could be done in a very
/// efficient manner.
///
/// FIXME: When compiling in debug mode, all table methods should
/// should TIGHTDB_ASSERT(is_valid()).
class Table {
public:
    /// Construct a new freestanding top-level table with static
    /// lifetime.
    ///
    /// This constructor should be used only when placing table
    /// variables on the stack, and it is then the responsibility of
    /// the application that there are no objects of type TableRef or
    /// ConstTableRef that refer to it, or to any of its subtables,
    /// when it goes out of scope. To create a top-level table with
    /// dynamic lifetime, use Table::create() instead.
    Table(Allocator& alloc = GetDefaultAllocator());

    ~Table();

    /// Construct a new freestanding top-level table with dynamic
    /// lifetime.
    ///
    /// \return A reference to the new table, or null if allocation
    /// fails.
    static TableRef create(Allocator& alloc = GetDefaultAllocator());

    /// An invalid table must not be accessed in any way except by
    /// calling is_valid(). A table that is obtained from a Group
    /// becomes invalid if its group is destroyed. This is also true
    /// for any subtable that is obtained indirectly from a group. A
    /// subtable will generally become invalid if its parent table is
    /// modified. Calling a const member function on a parent table,
    /// will never invalidate its subtables. A free standing table
    /// will never become invalid. A subtable of a freestanding table
    /// may become invalid.
    ///
    /// FIXME: High level language bindings will probably want to be
    /// able to explicitely invalidate a group and all tables of that
    /// group if any modifying operation fails (e.g. memory allocation
    /// failure) (and something similar for freestanding tables) since
    /// that leaves the group in state where any further access is
    /// disallowed. This way they will be able to reliably intercept
    /// any attempt at accessing such a failed group.
    ///
    /// FIXME: The C++ documentation must state that if any modifying
    /// operation on a group (incl. tables, subtables, and specs), or
    /// on a free standing table (incl. subtables and specs), then any
    /// further access to that group (except ~Group()) or freestanding
    /// table (except ~Table()) has undefined behaviour and is
    /// considered an error on behalf of the application. Note that
    /// even Table::is_valid() is disallowed in this case.
    ///
    /// FIXME: When Spec changes become possible for non-empty tables,
    /// such changes would generally have to invalidate subtables
    /// (except add_column()).
    bool is_valid() const { return m_columns.HasParent(); }

    // Schema handling (see also <tightdb/spec.hpp>)
    Spec&       get_spec();
    const Spec& get_spec() const;
    void        update_from_spec(); // Must not be called for a table with shared spec
    size_t      add_column(ColumnType type, const char* name); // Add a column dynamically

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
    size_t      add_empty_row(size_t num_rows = 1);
    void        insert_empty_row(size_t row_ndx, size_t num_rows = 1);
    void        remove(size_t row_ndx);
    void        remove_last() {if (!is_empty()) remove(m_size-1);}

    // Insert row
    // NOTE: You have to insert values in ALL columns followed by insert_done().
    void insert_int(size_t column_ndx, size_t row_ndx, int64_t value);
    void insert_bool(size_t column_ndx, size_t row_ndx, bool value);
    void insert_date(size_t column_ndx, size_t row_ndx, time_t value);
    template<class E> void insert_enum(size_t column_ndx, size_t row_ndx, E value);
    void insert_string(size_t column_ndx, size_t row_ndx, const char* value);
    void insert_binary(size_t column_ndx, size_t row_ndx, const char* value, size_t len);
    void insert_subtable(size_t column_ndx, size_t row_ndx); // Insert empty table
    void insert_mixed(size_t column_ndx, size_t row_ndx, Mixed value);
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

    // Sub-tables (works on columns whose type is either 'subtable' or
    // 'mixed', for a value in a mixed column that is not a subtable,
    // get_subtable() returns null, get_subtable_size() returns zero,
    // and clear_subtable() replaces the value with an empty table.)
    TableRef        get_subtable(size_t column_ndx, size_t row_ndx);
    ConstTableRef   get_subtable(size_t column_ndx, size_t row_ndx) const;
    size_t          get_subtable_size(size_t column_ndx, size_t row_ndx) const;
    void            clear_subtable(size_t column_ndx, size_t row_ndx);

    // Indexing
    bool has_index(size_t column_ndx) const;
    void set_index(size_t column_ndx, bool update_spec=true);

    // Aggregate functions
    size_t  count(size_t column_ndx, int64_t target) const;
    size_t  count_string(size_t column_ndx, const char* target) const;
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

    /// Compare two tables for equality. Two tables are equal if, and
    /// only if, they contain the same columns and rows in the same
    /// order, that is, for each value V of type T at column index C
    /// and row index R in one of the tables, there is a value of type
    /// T at column index C and row index R in the other table that
    /// is equal to V.
    bool operator==(const Table&) const;

    /// Compare two tables for inequality. See operator==().
    bool operator!=(const Table& t) const;

    // Debug
#ifdef TIGHTDB_DEBUG
    void Verify() const; // Must be upper case to avoid conflict with macro in ObjC
    void to_dot(std::ostream& out, const char* title=NULL) const;
    void print() const;
    MemStats stats() const;
#endif // TIGHTDB_DEBUG

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


    /// Used when the lifetime of a table is managed by reference
    /// counting. The lifetime of free-standing tables allocated on
    /// the stack by the application is not managed by reference
    /// counting, so that is a case where this tag must not be
    /// specified.
    class RefCountTag {};

    /// Construct a wrapper for a table with independent spec, and
    /// whose lifetime is managed by reference counting.
    Table(RefCountTag, Allocator& alloc, size_t top_ref,
          Parent* parent, size_t ndx_in_parent);

    /// Construct a wrapper for a table with shared spec, and whose
    /// lifetime is managed by reference counting.
    ///
    /// It is possible to construct a 'null' table by passing zero for
    /// \a columns_ref, in this case the columns will be created on
    /// demand.
    Table(RefCountTag, Allocator& alloc, size_t spec_ref, size_t columns_ref,
          Parent* parent, size_t ndx_in_parent);

    void init_from_ref(size_t top_ref, ArrayParent* parent, size_t ndx_in_parent);
    void init_from_ref(size_t spec_ref, size_t columns_ref,
                       ArrayParent* parent, size_t ndx_in_parent);
    void CreateColumns();
    void CacheColumns();
    void ClearCachedColumns();

    // Specification
    size_t GetColumnRefPos(size_t column_ndx) const;
    void UpdateColumnRefs(size_t column_ndx, int diff);
    void UpdateFromParent();


#ifdef TIGHTDB_DEBUG
    void ToDotInternal(std::ostream& out) const;
#endif // TIGHTDB_DEBUG

    // Member variables
    size_t m_size;

    // On-disk format
    Array m_top;
    Array m_columns;
    Spec m_spec_set;

    // Cached columns
    Array m_cols;

    /// Get the subtable at the specified column and row index.
    ///
    /// The returned table pointer must always end up being wrapped in
    /// a TableRef.
    Table *get_subtable_ptr(size_t col_idx, size_t row_idx);

    /// Get the subtable at the specified column and row index.
    ///
    /// The returned table pointer must always end up being wrapped in
    /// a ConstTableRef.
    const Table *get_subtable_ptr(size_t col_idx, size_t row_idx) const;

    /// Compare the rows of two tables under the assumption that the
    /// two tables have the same spec, and therefore the same sequence
    /// of columns.
    bool compare_rows(const Table&) const;

private:
    Table(Table const &); // Disable copy construction
    Table &operator=(Table const &); // Disable copying assignment

    /// Put this table wrapper into the invalid state, which detaches
    /// it from the underlying structure of arrays. Also do this
    /// recursively for subtables. When this function returns,
    /// is_valid() will return false.
    ///
    /// This function may be called for a table wrapper that is
    /// already in the invalid state (idempotency).
    ///
    /// It is also valid to call this function for a table wrapper
    /// that has not yet been marked as invalid, but whose underlying
    /// structure of arrays have changed in an unpredictable/unknown
    /// way. This generally happens when a modifying table operation
    /// fails, and also when one transaction is ended and a new one is
    /// started.
    void invalidate();

    mutable size_t m_ref_count;
    void bind_ref() const { ++m_ref_count; }
    void unbind_ref() const { if (--m_ref_count == 0) delete this; }

    ColumnBase& GetColumnBase(size_t column_ndx);
    void InstantiateBeforeChange();

    /// Construct an empty table with independent spec and return just
    /// the reference to the underlying memory.
    ///
    /// \return Zero if allocation fails.
    static size_t create_empty_table(Allocator&);

    // Experimental
    TableView find_all_hamming(size_t column_ndx, uint64_t value, size_t max);
    ConstTableView find_all_hamming(size_t column_ndx, uint64_t value, size_t max) const;

#ifdef TIGHTDB_ENABLE_REPLICATION
    struct LocalTransactLog;
    LocalTransactLog get_local_transact_log();
    // Precondition: 1 <= end - begin
    size_t* record_subspec_path(const Spec*, size_t* begin, size_t* end) const;
    // Precondition: 1 <= end - begin
    size_t* record_subtable_path(size_t* begin, size_t* end) const;
    friend class Replication;
#endif

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

    /// Must be called whenever a child Table is destroyed.
    virtual void child_destroyed(size_t child_ndx) = 0;

#ifdef TIGHTDB_ENABLE_REPLICATION
    virtual size_t* record_subtable_path(size_t* begin, size_t* end);
#endif
};





// Implementation:

inline size_t Table::create_empty_table(Allocator& alloc)
{
    Array top(COLUMN_HASREFS, 0, 0, alloc);
    top.add(Spec::create_empty_spec(alloc));
    top.add(Array::create_empty_array(COLUMN_HASREFS, alloc)); // Columns
    return top.GetRef();
}

inline Table::Table(Allocator& alloc):
    m_size(0), m_top(alloc), m_columns(alloc), m_spec_set(this, alloc), m_ref_count(1)
{
    const size_t ref = create_empty_table(alloc);
    if (!ref) throw_error(ERROR_OUT_OF_MEMORY); // FIXME: Check that this exception is handled properly in callers
    init_from_ref(ref, 0, 0);
}

inline Table::Table(RefCountTag, Allocator& alloc, size_t top_ref,
                    Parent* parent, size_t ndx_in_parent):
    m_size(0), m_top(alloc), m_columns(alloc), m_spec_set(this, alloc), m_ref_count(0)
{
    init_from_ref(top_ref, parent, ndx_in_parent);
}

inline Table::Table(RefCountTag, Allocator& alloc, size_t spec_ref, size_t columns_ref,
                    Parent* parent, size_t ndx_in_parent):
    m_size(0), m_top(alloc), m_columns(alloc), m_spec_set(this, alloc), m_ref_count(0)
{
    init_from_ref(spec_ref, columns_ref, parent, ndx_in_parent);
}

inline TableRef Table::create(Allocator& alloc)
{
    const size_t ref = Table::create_empty_table(alloc);
    if (!ref) return TableRef();
    Table* const table = new (std::nothrow) Table(Table::RefCountTag(), alloc, ref, 0, 0);
    if (!table) return TableRef();
    return table->get_table_ref();
}


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

inline bool Table::operator==(const Table& t) const
{
    return m_spec_set == t.m_spec_set && compare_rows(t);
}

inline bool Table::operator!=(const Table& t) const
{
    return m_spec_set != t.m_spec_set || !compare_rows(t);
}


#ifdef TIGHTDB_ENABLE_REPLICATION

struct Table::LocalTransactLog {
    template<class T> error_code set_value(size_t column_ndx, size_t row_ndx, const T& value)
    {
        if (!m_repl) return ERROR_NONE;
        return m_repl->set_value(m_table, column_ndx, row_ndx, value);
    }

    template<class T> error_code insert_value(size_t column_ndx, size_t row_ndx, const T& value)
    {
        if (!m_repl) return ERROR_NONE;
        return m_repl->insert_value(m_table, column_ndx, row_ndx, value);
    }

    error_code row_insert_complete()
    {
        if (!m_repl) return ERROR_NONE;
        return m_repl->row_insert_complete(m_table);
    }

    error_code insert_empty_rows(std::size_t row_ndx, std::size_t num_rows)
    {
        if (!m_repl) return ERROR_NONE;
        return m_repl->insert_empty_rows(m_table, row_ndx, num_rows);
    }

    error_code remove_row(std::size_t row_ndx)
    {
        if (!m_repl) return ERROR_NONE;
        return m_repl->remove_row(m_table, row_ndx);
    }

    error_code add_int_to_column(std::size_t column_ndx, int64_t value)
    {
        if (!m_repl) return ERROR_NONE;
        return m_repl->add_int_to_column(m_table, column_ndx, value);
    }

    error_code add_index_to_column(std::size_t column_ndx)
    {
        if (!m_repl) return ERROR_NONE;
        return m_repl->add_index_to_column(m_table, column_ndx);
    }

    error_code clear_table()
    {
        if (!m_repl) return ERROR_NONE;
        return m_repl->clear_table(m_table);
    }

    error_code optimize_table()
    {
        if (!m_repl) return ERROR_NONE;
        return m_repl->optimize_table(m_table);
    }

    error_code add_column(ColumnType type, const char* name)
    {
        if (!m_repl) return ERROR_NONE;
        return m_repl->add_column(m_table, &m_table->m_spec_set, type, name);
    }

    void on_table_destroyed()
    {
        if (!m_repl) return;
        m_repl->on_table_destroyed(m_table);
    }

private:
    Replication* const m_repl;
    Table* const m_table;
    LocalTransactLog(Replication* r, Table* t): m_repl(r), m_table(t) {}
    friend class Table;
};

inline Table::LocalTransactLog Table::get_local_transact_log()
{
    return LocalTransactLog(m_top.GetAllocator().get_replication(), this);
}

inline size_t* Table::record_subspec_path(const Spec* spec, size_t* begin, size_t* end) const
{
    if (spec != &m_spec_set) {
        TIGHTDB_ASSERT(m_spec_set.m_subSpecs.IsValid());
        return spec->record_subspec_path(&m_spec_set.m_subSpecs, begin, end);
    }
    return begin;
}

inline size_t* Table::record_subtable_path(size_t* begin, size_t* end) const
{
    const Array& real_top = m_top.IsValid() ? m_top : m_columns;
    const size_t index_in_parent = real_top.GetParentNdx();
    TIGHTDB_ASSERT(begin < end);
    *begin++ = index_in_parent;
    ArrayParent* parent = real_top.GetParent();
    TIGHTDB_ASSERT(parent);
    TIGHTDB_ASSERT(dynamic_cast<Parent*>(parent));
    return static_cast<Parent*>(parent)->record_subtable_path(begin, end);
}

inline size_t* Table::Parent::record_subtable_path(size_t* begin, size_t*)
{
    return begin;
}

#endif // TIGHTDB_ENABLE_REPLICATION


} // namespace tightdb

#endif // TIGHTDB_TABLE_HPP
