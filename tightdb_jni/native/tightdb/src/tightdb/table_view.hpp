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
#ifndef TIGHTDB_TABLE_VIEW_HPP
#define TIGHTDB_TABLE_VIEW_HPP

#include <iostream>
#include "table.hpp"

namespace tightdb {


using std::size_t;
using std::time_t;


/**
 * Common base class for TableView and ConstTableView.
 */
class TableViewBase {
public:
    bool is_empty() const { return m_refs.is_empty(); }
    size_t size() const { return m_refs.Size(); }

    // Column information
    size_t      get_column_count() const;
    const char* get_column_name(size_t column_ndx) const;
    size_t      get_column_index(const char* name) const;
    ColumnType  get_column_type(size_t column_ndx) const;

    // Getting values
    int64_t     get_int(size_t column_ndx, size_t row_ndx) const;
    bool        get_bool(size_t column_ndx, size_t row_ndx) const;
    time_t      get_date(size_t column_ndx, size_t row_ndx) const;
    const char* get_string(size_t column_ndx, size_t row_ndx) const;
    BinaryData  get_binary(size_t column_ndx, size_t row_ndx) const;
    Mixed       get_mixed(size_t column_ndx, size_t row_ndx) const;
    ColumnType  get_mixed_type(size_t column_ndx, size_t row_ndx) const;

    // Subtables
    size_t      get_subtable_size(size_t column_ndx, size_t row_ndx) const;

    // Searching (Int and String)
    size_t find_first_int(size_t column_ndx, int64_t value) const;
    size_t find_first_bool(size_t column_ndx, bool value) const;
    size_t find_first_date(size_t column_ndx, time_t value) const;
    size_t find_first_string(size_t column_ndx, const char* value) const;
    // FIXME: Need: size_t find_first_binary(size_t column_ndx, const char* value, size_t len) const;

    // Aggregate functions
    int64_t sum(size_t column_ndx) const;
    int64_t maximum(size_t column_ndx) const;
    int64_t minimum(size_t column_ndx) const;

    // Sort the view according to the specified column and the specified direction.
    void sort(size_t column, bool ascending = true);

    // Get row index in the source table this view is "looking" at.
    size_t get_source_ndx(size_t row_ndx) const { return size_t(m_refs.Get(row_ndx)); }

protected:
    friend class Table;
    friend class Query;
    template <class R, class V> static R find_all_integer(V*, size_t, int64_t);
    template <class R, class V> static R find_all_string(V*, size_t, const char*);

    Table* m_table;
    Array m_refs;

    /**
     * Construct null view (no memory allocated).
     */
    TableViewBase(): m_table(0), m_refs(GetDefaultAllocator()) {}

    /**
     * Construct empty view, ready for addition of row indices.
     */
    TableViewBase(Table* parent): m_table(parent) {}

    /**
     * Copy constructor.
     */
    TableViewBase(const TableViewBase& tv): m_table(tv.m_table)
    {
        m_refs.Copy(tv.m_refs);
    }

    /**
     * Moving constructor.
     */
    TableViewBase(TableViewBase*);

    ~TableViewBase() { m_refs.Destroy(); }

    void move_assign(TableViewBase*);

    Array& get_ref_column() { return m_refs; }
    const Array& get_ref_column() const { return m_refs; }

private:
    size_t find_first_integer(size_t column_ndx, int64_t value) const;
};



class ConstTableView;



/**
 * A TableView gives read and write access to the parent table.
 *
 * A 'const TableView' cannot be changed (e.g. sorted), nor can the
 * parent table be modified through it.
 *
 * A TableView is both copyable and movable. Copying a TableView makes
 * a proper copy. Copying a temporary TableView is optimized away on
 * all modern compilers due to such things as 'return value
 * optimization'. Move semantics is accessed using the move()
 * function. For example, to efficiently return a non-temporary
 * TableView from a function, you would have to do something like
 * this:
 *
 * \code{.cpp}
 *
 *   tightdb::TableView func()
 *   {
 *      tightdb::TableView tv;
 *      return move(tv);
 *   }
 *
 * \endcode
 *
 * Note that move(tv) removes the contents from 'tv' and leaves it
 * truncated.
 *
 * FIXME: Add general documentation about move semantics, and refer to
 * it from here.
 */
class TableView: public TableViewBase {
public:
    TableView() {}
    TableView& operator=(TableView tv) { move_assign(&tv); return *this; }
    friend TableView move(TableView& tv) { return TableView(&tv); }

    // Subtables
    TableRef      get_subtable(size_t column_ndx, size_t row_ndx);
    ConstTableRef get_subtable(size_t column_ndx, size_t row_ndx) const;
    void          clear_subtable(size_t column_ndx, size_t row_ndx);

    // Setting values
    void set_int(size_t column_ndx, size_t row_ndx, int64_t value);
    void set_bool(size_t column_ndx, size_t row_ndx, bool value);
    void set_date(size_t column_ndx, size_t row_ndx, time_t value);
    template<class E> void set_enum(size_t column_ndx, size_t row_ndx, E value);
    void set_string(size_t column_ndx, size_t row_ndx, const char* value);
    void set_binary(size_t column_ndx, size_t row_ndx, const char* value, size_t len);
    void set_mixed(size_t column_ndx, size_t row_ndx, Mixed value);
    void add_int(size_t column_ndx, int64_t value);

    // Deleting
    void clear();
    void remove(size_t row_ndx);
    void remove_last() { if (!is_empty()) remove(size()-1); }

    // Searching (Int and String)
    TableView       find_all_int(size_t column_ndx, int64_t value);
    ConstTableView  find_all_int(size_t column_ndx, int64_t value) const;
    TableView       find_all_bool(size_t column_ndx, bool value);
    ConstTableView  find_all_bool(size_t column_ndx, bool value) const;
    TableView       find_all_date(size_t column_ndx, time_t value);
    ConstTableView  find_all_date(size_t column_ndx, time_t value) const;
    TableView       find_all_string(size_t column_ndx, const char *value);
    ConstTableView  find_all_string(size_t column_ndx, const char *value) const;
    // FIXME: Need: TableView find_all_binary(size_t column_ndx, const char* value, size_t len);
    // FIXME: Need: ConstTableView find_all_binary(size_t column_ndx, const char* value, size_t len) const;

    Table& get_parent() { return *m_table; }
    const Table& get_parent() const { return *m_table; }

private:
    friend class ConstTableView;
    friend class Table;
    friend class Query;
    friend class TableViewBase;

    TableView(Table& parent): TableViewBase(&parent) {}
    TableView(TableView* tv): TableViewBase(tv) {}

    TableView find_all_integer(size_t column_ndx, int64_t value);
    ConstTableView find_all_integer(size_t column_ndx, int64_t value) const;
};




/**
 * A ConstTableView gives read access to the parent table, but no
 * write access. The view itself, though, can be changed, for example,
 * it can be sorted.
 *
 * Note that methods are declared 'const' if, and only
 * if they leave the view unmodified, and this is irrespective of
 * whether they modify the parent table.
 *
 * A ConstTableView has both copy and move semantics. See TableView
 * for more on this.
 */
class ConstTableView: public TableViewBase {
public:
    ConstTableView() {}
    ConstTableView& operator=(ConstTableView tv) { move_assign(&tv); return *this; }
    friend ConstTableView move(ConstTableView& tv) { return ConstTableView(&tv); }

    ConstTableView(TableView tv): TableViewBase(&tv) {}
    ConstTableView& operator=(TableView tv) { move_assign(&tv); return *this; }

    // Getting values
    ConstTableRef get_subtable(size_t column_ndx, size_t row_ndx) const;

    // Searching (Int and String)
    ConstTableView find_all_int(size_t column_ndx, int64_t value) const;
    ConstTableView find_all_bool(size_t column_ndx, bool value) const;
    ConstTableView find_all_date(size_t column_ndx, time_t value) const;
    ConstTableView find_all_string(size_t column_ndx, const char *value) const;

   const Table& get_parent() const { return *m_table; }

private:
    friend class TableView;
    friend class Table;
    friend class Query;
    friend class TableViewBase;

    ConstTableView(const Table& parent): TableViewBase(const_cast<Table*>(&parent)) {}
    ConstTableView(ConstTableView* tv): TableViewBase(tv) {}

    ConstTableView find_all_integer(size_t column_ndx, int64_t value) const;
};


// ================================================================================================
// TableViewBase Implementation:

#define TIGHTDB_ASSERT_COLUMN(column_ndx)                                   \
    assert(m_table);                                                        \
    assert(column_ndx < m_table->get_column_count());

#define TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, column_type)             \
    TIGHTDB_ASSERT_COLUMN(column_ndx)                                       \
    assert(m_table->get_column_type(column_ndx) == column_type);

#define TIGHTDB_ASSERT_INDEX(column_ndx, row_ndx)                           \
    TIGHTDB_ASSERT_COLUMN(column_ndx)                                       \
    assert(row_ndx < m_refs.Size());

#define TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, column_type)     \
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, column_type)                 \
    assert(row_ndx < m_refs.Size());


inline TableViewBase::TableViewBase(TableViewBase* tv):
    m_table(tv->m_table),
    m_refs(tv->m_refs) // Note: This is a moving copy
{
    tv->m_table = 0;
}

inline void TableViewBase::move_assign(TableViewBase* tv)
{
    m_table = tv->m_table;
    tv->m_table = 0;
    m_refs.move_assign(tv->m_refs);
}


// Column information


inline size_t TableViewBase::get_column_count() const
{
    assert(m_table);
    return m_table->get_column_count();
}

inline const char* TableViewBase::get_column_name(size_t column_ndx) const
{
    assert(m_table);
    return m_table->get_column_name(column_ndx);
}

inline size_t TableViewBase::get_column_index(const char* name) const
{
    assert(m_table);
    return m_table->get_column_index(name);
}

inline ColumnType TableViewBase::get_column_type(size_t column_ndx) const
{
    assert(m_table);
    return m_table->get_column_type(column_ndx);
}


// Getters


inline int64_t TableViewBase::get_int(size_t column_ndx, size_t row_ndx) const
{
    TIGHTDB_ASSERT_INDEX(column_ndx, row_ndx);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    return m_table->get_int(column_ndx, real_ndx);
}

inline bool TableViewBase::get_bool(size_t column_ndx, size_t row_ndx) const
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_BOOL);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    return m_table->get_bool(column_ndx, real_ndx);
}

inline time_t TableViewBase::get_date(size_t column_ndx, size_t row_ndx) const
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_DATE);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    return m_table->get_date(column_ndx, real_ndx);
}

inline const char* TableViewBase::get_string(size_t column_ndx, size_t row_ndx) const
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_STRING);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    return m_table->get_string(column_ndx, real_ndx);
}

inline BinaryData TableViewBase::get_binary(size_t column_ndx, size_t row_ndx) const
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_BINARY);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    return m_table->get_binary(column_ndx, real_ndx);
}

inline Mixed TableViewBase::get_mixed(size_t column_ndx, size_t row_ndx) const
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_MIXED);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    return m_table->get_mixed(column_ndx, real_ndx);
}

inline ColumnType TableViewBase::get_mixed_type(size_t column_ndx, size_t row_ndx) const
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_MIXED);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    return m_table->get_mixed_type(column_ndx, real_ndx);
}

inline size_t TableViewBase::get_subtable_size(size_t column_ndx, size_t row_ndx) const
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_TABLE);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    return m_table->get_subtable_size(column_ndx, real_ndx);
}


// Searching


inline size_t TableViewBase::find_first_int(size_t column_ndx, int64_t value) const
{
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, COLUMN_TYPE_INT);
    return find_first_integer(column_ndx, value);
}

inline size_t TableViewBase::find_first_bool(size_t column_ndx, bool value) const
{
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, COLUMN_TYPE_BOOL);
    return find_first_integer(column_ndx, value ? 1 : 0);
}

inline size_t TableViewBase::find_first_date(size_t column_ndx, time_t value) const
{
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, COLUMN_TYPE_DATE);
    return find_first_integer(column_ndx, (int64_t)value);
}


template <class R, class V>
R TableViewBase::find_all_integer(V* view, size_t column_ndx, int64_t value)
{
    R tv(*view->m_table);
    for (size_t i = 0; i < view->m_refs.Size(); i++)
        if (view->get_int(column_ndx, i) == value)
            tv.get_ref_column().add(i);
    return move(tv);
}

template <class R, class V>
R TableViewBase::find_all_string(V* view, size_t column_ndx, const char* value)
{
    assert(view->m_table);
    assert(column_ndx < view->m_table->get_column_count());
    assert(view->m_table->get_column_type(column_ndx) == COLUMN_TYPE_STRING);

    R tv(*view->m_table);
    for (size_t i = 0; i < view->m_refs.Size(); i++)
        if (strcmp(view->get_string(column_ndx, i), value) == 0)
            tv.get_ref_column().add(i);
    return move(tv);
}


// TableView, ConstTableView implementation:


inline TableView TableView::find_all_string(size_t column_ndx, const char* value)
{
    return TableViewBase::find_all_string<TableView>(this, column_ndx, value);
}

inline ConstTableView TableView::find_all_string(size_t column_ndx, const char* value) const
{
    return TableViewBase::find_all_string<ConstTableView>(this, column_ndx, value);
}

inline ConstTableView ConstTableView::find_all_string(size_t column_ndx, const char* value) const
{
    return TableViewBase::find_all_string<ConstTableView>(this, column_ndx, value);
}


inline TableView TableView::find_all_integer(size_t column_ndx, int64_t value)
{
    return TableViewBase::find_all_integer<TableView>(this, column_ndx, value);
}

inline ConstTableView TableView::find_all_integer(size_t column_ndx, int64_t value) const
{
    return TableViewBase::find_all_integer<ConstTableView>(this, column_ndx, value);
}

inline ConstTableView ConstTableView::find_all_integer(size_t column_ndx, int64_t value) const
{
    return TableViewBase::find_all_integer<ConstTableView>(this, column_ndx, value);
}


inline TableView TableView::find_all_int(size_t column_ndx, int64_t value)
{
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, COLUMN_TYPE_INT);
    return find_all_integer(column_ndx, value);
}

inline TableView TableView::find_all_bool(size_t column_ndx, bool value)
{
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, COLUMN_TYPE_BOOL);
    return find_all_integer(column_ndx, value ? 1 : 0);
}

inline TableView TableView::find_all_date(size_t column_ndx, time_t value)
{
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, COLUMN_TYPE_DATE);
    return find_all_integer(column_ndx, (int64_t)value);
}


inline ConstTableView TableView::find_all_int(size_t column_ndx, int64_t value) const
{
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, COLUMN_TYPE_INT);
    return find_all_integer(column_ndx, value);
}

inline ConstTableView TableView::find_all_bool(size_t column_ndx, bool value) const
{
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, COLUMN_TYPE_BOOL);
    return find_all_integer(column_ndx, value ? 1 : 0);
}

inline ConstTableView TableView::find_all_date(size_t column_ndx, time_t value) const
{
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, COLUMN_TYPE_DATE);
    return find_all_integer(column_ndx, (int64_t)value);
}


inline ConstTableView ConstTableView::find_all_int(size_t column_ndx, int64_t value) const
{
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, COLUMN_TYPE_INT);
    return find_all_integer(column_ndx, value);
}

inline ConstTableView ConstTableView::find_all_bool(size_t column_ndx, bool value) const
{
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, COLUMN_TYPE_BOOL);
    return find_all_integer(column_ndx, value ? 1 : 0);
}

inline ConstTableView ConstTableView::find_all_date(size_t column_ndx, time_t value) const
{
    TIGHTDB_ASSERT_COLUMN_AND_TYPE(column_ndx, COLUMN_TYPE_DATE);
    return find_all_integer(column_ndx, (int64_t)value);
}


// Subtables


inline TableRef TableView::get_subtable(size_t column_ndx, size_t row_ndx)
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_TABLE);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    return m_table->get_subtable(column_ndx, real_ndx);
}

inline ConstTableRef TableView::get_subtable(size_t column_ndx, size_t row_ndx) const
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_TABLE);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    return m_table->get_subtable(column_ndx, real_ndx);
}

inline ConstTableRef ConstTableView::get_subtable(size_t column_ndx, size_t row_ndx) const
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_TABLE);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    return m_table->get_subtable(column_ndx, real_ndx);
}

inline void TableView::clear_subtable(size_t column_ndx, size_t row_ndx)
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_TABLE);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    return m_table->clear_subtable(column_ndx, real_ndx);
}


// Setters


inline void TableView::set_int(size_t column_ndx, size_t row_ndx, int64_t value)
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_INT);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    m_table->set_int(column_ndx, real_ndx, value);
}

inline void TableView::set_bool(size_t column_ndx, size_t row_ndx, bool value)
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_BOOL);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    m_table->set_bool(column_ndx, real_ndx, value);
}

inline void TableView::set_date(size_t column_ndx, size_t row_ndx, time_t value)
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_DATE);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    m_table->set_date(column_ndx, real_ndx, value);
}

template<class E> inline void TableView::set_enum(size_t column_ndx, size_t row_ndx, E value)
{
    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    m_table->set_int(column_ndx, real_ndx, value);
}

inline void TableView::set_string(size_t column_ndx, size_t row_ndx, const char* value)
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_STRING);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    m_table->set_string(column_ndx, real_ndx, value);
}

inline void TableView::set_binary(size_t column_ndx, size_t row_ndx, const char* value, size_t len)
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_BINARY);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    m_table->set_binary(column_ndx, real_ndx, value, len);
}

inline void TableView::set_mixed(size_t column_ndx, size_t row_ndx, Mixed value)
{
    TIGHTDB_ASSERT_INDEX_AND_TYPE(column_ndx, row_ndx, COLUMN_TYPE_MIXED);

    const size_t real_ndx = size_t(m_refs.Get(row_ndx));
    m_table->set_mixed(column_ndx, real_ndx, value);
}

inline void TableView::add_int(size_t column_ndx, int64_t value)
{
    m_table->add_int(column_ndx, value);
}

} // namespace tightdb

#endif // TIGHTDB_TABLE_VIEW_HPP
