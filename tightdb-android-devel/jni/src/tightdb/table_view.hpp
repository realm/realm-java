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

    // Getting values
    int64_t     get_int(size_t column_ndx, size_t ndx) const;
    bool        get_bool(size_t column_ndx, size_t ndx) const;
    time_t      get_date(size_t column_ndx, size_t ndx) const;
    const char* get_string(size_t column_ndx, size_t ndx) const;
    BinaryData  get_binary(size_t column_ndx, size_t ndx) const;
    Mixed       get_mixed(size_t column_ndx, size_t ndx) const;

    // Searching (Int and String)
    size_t find_first_int(size_t column_ndx, int64_t value) const;
    size_t find_first_string(size_t column_ndx, const char* value) const;

    // Aggregate functions
    int64_t sum(size_t column_ndx) const;
    int64_t maximum(size_t column_ndx) const;
    int64_t minimum(size_t column_ndx) const;

    /**
     * Sort the view according to the specified column and the
     * specified direction.
     */
    void sort(size_t column, bool ascending = true);

    // Get row index in the source table this view is "looking" at.
    size_t get_source_ndx(size_t row_ndx) const { return size_t(m_refs.Get(row_ndx)); }

protected:
    friend class Table;
    friend class Query;

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

    // Getting values
    TableRef get_subtable(size_t column_ndx, size_t ndx);
    ConstTableRef get_subtable(size_t column_ndx, size_t ndx) const;

    // Setting values
    void set_int(size_t column_ndx, size_t ndx, int64_t value);
    void set_bool(size_t column_ndx, size_t ndx, bool value);
    void set_date(size_t column_ndx, size_t ndx, time_t value);
    void set_string(size_t column_ndx, size_t ndx, const char* value);
    void set_binary(size_t column_ndx, size_t ndx, const char* value, size_t len);
    void set_mixed(size_t column_ndx, size_t ndx, Mixed value);

    // Deleting
    void clear();
    void remove(size_t ndx);
    void remove_last() { if (!is_empty()) remove(size()-1); }

    // Searching (Int and String)
    TableView find_all_int(size_t column_ndx, int64_t value);
    ConstTableView find_all_int(size_t column_ndx, int64_t value) const;
    TableView find_all_string(size_t column_ndx, const char *value);
    ConstTableView find_all_string(size_t column_ndx, const char *value) const;

    Table& get_parent() { return *m_table; }
    const Table& get_parent() const { return *m_table; }

private:
    friend class ConstTableView;
    friend class Table;
    friend class Query;

    TableView(Table& parent): TableViewBase(&parent) {}
    TableView(TableView* tv): TableViewBase(tv) {}
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
    ConstTableRef get_subtable(size_t column_ndx, size_t ndx) const;

    // Searching (Int and String)
    ConstTableView find_all_int(size_t column_ndx, int64_t value) const;
    ConstTableView find_all_string(size_t column_ndx, const char *value) const;

    const Table& get_parent() const { return *m_table; }

private:
    friend class TableView;
    friend class Table;
    friend class Query;

    ConstTableView(const Table& parent): TableViewBase(const_cast<Table*>(&parent)) {}
    ConstTableView(ConstTableView* tv): TableViewBase(tv) {}
};





// Implementation:

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

inline int64_t TableViewBase::get_int(size_t column_ndx, size_t ndx) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_INT);
    assert(ndx < m_refs.Size());

    const size_t real_ndx = size_t(m_refs.Get(ndx));
    return m_table->get_int(column_ndx, real_ndx);
}

inline bool TableViewBase::get_bool(size_t column_ndx, size_t ndx) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_BOOL);
    assert(ndx < m_refs.Size());

    const size_t real_ndx = size_t(m_refs.Get(ndx));
    return m_table->get_bool(column_ndx, real_ndx);
}

inline time_t TableViewBase::get_date(size_t column_ndx, size_t ndx) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_DATE);
    assert(ndx < m_refs.Size());

    const size_t real_ndx = size_t(m_refs.Get(ndx));
    return m_table->get_date(column_ndx, real_ndx);
}

inline const char* TableViewBase::get_string(size_t column_ndx, size_t ndx) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_STRING);
    assert(ndx < m_refs.Size());

    const size_t real_ndx = size_t(m_refs.Get(ndx));
    return m_table->get_string(column_ndx, real_ndx);
}

inline BinaryData TableViewBase::get_binary(std::size_t column_ndx, std::size_t ndx) const
{
    assert(m_table);
    assert(ndx < m_refs.Size());
    const size_t real_ndx = size_t(m_refs.Get(ndx));
    return m_table->get_binary(column_ndx, real_ndx);
}

inline Mixed TableViewBase::get_mixed(std::size_t column_ndx, std::size_t ndx) const
{
    assert(m_table);
    assert(ndx < m_refs.Size());
    const size_t real_ndx = size_t(m_refs.Get(ndx));
    return m_table->get_mixed(column_ndx, real_ndx);
}

inline TableRef TableView::get_subtable(size_t column_ndx, size_t ndx)
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_TABLE);
    assert(ndx < m_refs.Size());

    const size_t real_ndx = size_t(m_refs.Get(ndx));
    return m_table->get_subtable(column_ndx, real_ndx);
}

inline ConstTableRef TableView::get_subtable(size_t column_ndx, size_t ndx) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_TABLE);
    assert(ndx < m_refs.Size());

    const size_t real_ndx = size_t(m_refs.Get(ndx));
    return m_table->get_subtable(column_ndx, real_ndx);
}

inline ConstTableRef ConstTableView::get_subtable(size_t column_ndx, size_t ndx) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_TABLE);
    assert(ndx < m_refs.Size());

    const size_t real_ndx = size_t(m_refs.Get(ndx));
    return m_table->get_subtable(column_ndx, real_ndx);
}

inline void TableView::set_int(size_t column_ndx, size_t ndx, int64_t value)
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_INT);
    assert(ndx < m_refs.Size());

    const size_t real_ndx = size_t(m_refs.Get(ndx));
    m_table->set_int(column_ndx, real_ndx, value);
}

inline void TableView::set_bool(size_t column_ndx, size_t ndx, bool value)
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_BOOL);
    assert(ndx < m_refs.Size());

    const size_t real_ndx = size_t(m_refs.Get(ndx));
    m_table->set_bool(column_ndx, real_ndx, value);
}

inline void TableView::set_date(size_t column_ndx, size_t ndx, time_t value)
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_DATE);
    assert(ndx < m_refs.Size());

    const size_t real_ndx = size_t(m_refs.Get(ndx));
    m_table->set_date(column_ndx, real_ndx, value);
}

inline void TableView::set_string(size_t column_ndx, size_t ndx, const char* value)
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_STRING);
    assert(ndx < m_refs.Size());

    const size_t real_ndx = size_t(m_refs.Get(ndx));
    m_table->set_string(column_ndx, real_ndx, value);
}

inline void TableView::set_binary(std::size_t column_ndx, size_t ndx, const char* value, size_t len)
{
    assert(m_table);
    assert(ndx < m_refs.Size());
    const size_t real_ndx = size_t(m_refs.Get(ndx));
    m_table->set_binary(column_ndx, real_ndx, value, len);
}

inline void TableView::set_mixed(std::size_t column_ndx, size_t ndx, Mixed value)
{
    assert(m_table);
    assert(ndx < m_refs.Size());
    const size_t real_ndx = size_t(m_refs.Get(ndx));
    m_table->set_mixed(column_ndx, real_ndx, value);
}


} // namespace tightdb

#endif // TIGHTDB_TABLE_VIEW_HPP
