#include "table_view.hpp"
#include "column.hpp"

namespace tightdb {


// Searching
size_t TableViewBase::find_first_int(size_t column_ndx, int64_t value) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_INT);

    for(size_t i = 0; i < m_refs.Size(); i++)
        if(get_int(column_ndx, i) == value)
            return i;

    return size_t(-1);
}


size_t TableViewBase::find_first_string(size_t column_ndx, const char* value) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_STRING);

    for(size_t i = 0; i < m_refs.Size(); i++)
    if(strcmp(get_string(column_ndx, i), value) == 0)
        return i;

    return size_t(-1);
}


TableView TableView::find_all_int(size_t column_ndx, int64_t value)
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_INT);

    TableView tv(*m_table);
    for(size_t i = 0; i < m_refs.Size(); i++)
        if(get_int(column_ndx, i) == value)
            tv.get_ref_column().add(i);
    return move(tv);
}


ConstTableView TableView::find_all_int(size_t column_ndx, int64_t value) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_INT);

    ConstTableView tv(*m_table);
    for(size_t i = 0; i < m_refs.Size(); i++)
        if(get_int(column_ndx, i) == value)
            tv.get_ref_column().add(i);
    return move(tv);
}


ConstTableView ConstTableView::find_all_int(size_t column_ndx, int64_t value) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_INT);

    ConstTableView tv(*m_table);
    for(size_t i = 0; i < m_refs.Size(); i++)
        if(get_int(column_ndx, i) == value)
            tv.get_ref_column().add(i);
    return move(tv);
}


TableView TableView::find_all_string(size_t column_ndx, const char* value)
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_STRING);

    TableView tv(*m_table);
    for(size_t i = 0; i < m_refs.Size(); i++)
    if(strcmp(get_string(column_ndx, i), value) == 0)
        tv.get_ref_column().add(i);
    return move(tv);
}


ConstTableView TableView::find_all_string(size_t column_ndx, const char* value) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_STRING);

    ConstTableView tv(*m_table);
    for(size_t i = 0; i < m_refs.Size(); i++)
    if(strcmp(get_string(column_ndx, i), value) == 0)
        tv.get_ref_column().add(i);
    return move(tv);
}


ConstTableView ConstTableView::find_all_string(size_t column_ndx, const char* value) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_STRING);

    ConstTableView tv(*m_table);
    for(size_t i = 0; i < m_refs.Size(); i++)
    if(strcmp(get_string(column_ndx, i), value) == 0)
        tv.get_ref_column().add(i);
    return move(tv);
}


int64_t TableViewBase::sum(size_t column_ndx) const
{
    assert(m_table);
    assert(column_ndx < m_table->get_column_count());
    assert(m_table->get_column_type(column_ndx) == COLUMN_TYPE_INT);
    int64_t sum = 0;

    for(size_t i = 0; i < m_refs.Size(); i++)
        sum += get_int(column_ndx, i);

    return sum;
}


int64_t TableViewBase::maximum(size_t column_ndx) const
{
    assert(m_table);
    if (is_empty()) return 0;
    if (m_refs.Size() == 0) return 0;

    int64_t mv = get_int(column_ndx, 0);
    for (size_t i = 1; i < m_refs.Size(); ++i) {
        const int64_t v = get_int(column_ndx, i);
        if (v > mv) {
            mv = v;
        }
    }
    return mv;
}


int64_t TableViewBase::minimum(size_t column_ndx) const
{
    assert(m_table);
    if (is_empty()) return 0;
    if (m_refs.Size() == 0) return 0;

    int64_t mv = get_int(column_ndx, 0);
    for (size_t i = 1; i < m_refs.Size(); ++i) {
        const int64_t v = get_int(column_ndx, i);
        if (v < mv) {
            mv = v;
        }
    }
    return mv;
}


void TableViewBase::sort(size_t column, bool Ascending)
{
    assert(m_table);
    assert(m_table->get_column_type(column) == COLUMN_TYPE_INT ||
           m_table->get_column_type(column) == COLUMN_TYPE_DATE ||
           m_table->get_column_type(column) == COLUMN_TYPE_BOOL);

    if(m_refs.Size() == 0)
        return;

    Array vals;
    Array ref;
    Array result;

    //ref.Preset(0, m_refs.Size() - 1, m_refs.Size());
    for(size_t t = 0; t < m_refs.Size(); t++)
        ref.add(t);

    // Extract all values from the Column and put them in an Array because Array is much faster to operate on
    // with rand access (we have ~log(n) accesses to each element, so using 1 additional read to speed up the rest is faster)
    if(m_table->get_column_type(column) == COLUMN_TYPE_INT) {
        for(size_t t = 0; t < m_refs.Size(); t++) {
            const int64_t v = m_table->get_int(column, size_t(m_refs.Get(t)));
            vals.add(v);
        }
    }
    else if(m_table->get_column_type(column) == COLUMN_TYPE_DATE) {
        for(size_t t = 0; t < m_refs.Size(); t++) {
            const size_t idx = size_t(m_refs.Get(t));
            const int64_t v = int64_t(m_table->get_date(column, idx));
            vals.add(v);
        }
    }
    else if(m_table->get_column_type(column) == COLUMN_TYPE_BOOL) {
        for(size_t t = 0; t < m_refs.Size(); t++) {
            const size_t idx = size_t(m_refs.Get(t));
            const int64_t v = int64_t(m_table->get_bool(column, idx));
            vals.add(v);
        }
    }

    vals.ReferenceSort(ref);
    vals.Destroy();

    for(size_t t = 0; t < m_refs.Size(); t++) {
        const size_t r  = (size_t)ref.Get(t);
        const size_t rr = (size_t)m_refs.Get(r);
        result.add(rr);
    }

    ref.Destroy();

    // Copy result to m_refs (todo, there might be a shortcut)
    m_refs.Clear();
    if(Ascending) {
        for(size_t t = 0; t < ref.Size(); t++) {
            const size_t v = (size_t)result.Get(t);
            m_refs.add(v);
        }
    }
    else {
        for(size_t t = 0; t < ref.Size(); t++) {
            const size_t v = (size_t)result.Get(ref.Size() - t - 1);
            m_refs.add(v);
        }
    }
    result.Destroy();
}


void TableView::remove(size_t ndx)
{
    assert(m_table);
    assert(ndx < m_refs.Size());

    // Delete row in source table
    const size_t real_ndx = size_t(m_refs.Get(ndx));
    m_table->remove(real_ndx);

    // Update refs
    m_refs.Delete(ndx);
    m_refs.IncrementIf(ndx, -1);
}


void TableView::clear()
{
    assert(m_table);
    m_refs.sort();

    // Delete all referenced rows in source table
    // (in reverse order to avoid index drift)
    const size_t count = m_refs.Size();
    for (size_t i = count; i; --i) {
        const size_t ndx = size_t(m_refs.Get(i-1));
        m_table->remove(ndx);
    }

    m_refs.Clear();
}


} // namespace tightdb
