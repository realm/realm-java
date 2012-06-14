#define _CRT_SECURE_NO_WARNINGS
#include "table.hpp"
#include <assert.h>
#include "index.hpp"
#include <iostream>
#include <iomanip>
#include <fstream>
#include "alloc_slab.hpp"
#include "column.hpp"
#include "column_string.hpp"
#include "column_string_enum.hpp"
#include "column_binary.hpp"
#include "column_table.hpp"
#include "column_mixed.hpp"

using namespace std;

namespace tightdb {


struct FakeParent: Table::Parent {
    virtual void update_child_ref(size_t, size_t) {} // Ignore
    virtual void child_destroyed(size_t) {} // Ignore
    virtual size_t get_child_ref(size_t) const { return 0; }
};


// -- Table ---------------------------------------------------------------------------------

// Create new Table
Table::Table(Allocator& alloc):
    m_size(0),
    m_top(COLUMN_HASREFS, NULL, 0, alloc),
    m_columns(COLUMN_HASREFS, NULL, 0, alloc),
    m_spec_set(alloc, NULL, 0),
    m_ref_count(1)
{
    m_top.add(m_spec_set.get_ref());
    m_top.add(m_columns.GetRef());
    m_spec_set.set_parent(&m_top, 0);
    m_columns.SetParent(&m_top, 1);
}

// Create Table from ref
Table::Table(Allocator& alloc, size_t top_ref, Parent* parent, size_t ndx_in_parent):
    m_size(0), m_top(alloc), m_columns(alloc), m_spec_set(alloc), m_ref_count(1)
{
    // Load from allocated memory
    m_top.UpdateRef(top_ref);
    m_top.SetParent(parent, ndx_in_parent);
    assert(m_top.Size() == 2);

    const size_t schema_ref  = m_top.GetAsRef(0);
    const size_t columns_ref = m_top.GetAsRef(1);

    Create(schema_ref, columns_ref, &m_top, 1);
    m_spec_set.set_parent(&m_top, 0);
}

// Create attached table from ref
Table::Table(SubtableTag, Allocator& alloc, size_t top_ref, Parent* parent, size_t ndx_in_parent):
    m_size(0), m_top(alloc), m_columns(alloc), m_spec_set(alloc), m_ref_count(0)
{
    // Load from allocated memory
    m_top.UpdateRef(top_ref);
    m_top.SetParent(parent, ndx_in_parent);
    assert(m_top.Size() == 2);

    const size_t schema_ref  = m_top.GetAsRef(0);
    const size_t columns_ref = m_top.GetAsRef(1);

    Create(schema_ref, columns_ref, &m_top, 1);
    m_spec_set.set_parent(&m_top, 0);
}

// Create attached sub-table from ref and schema_ref
Table::Table(SubtableTag, Allocator& alloc, size_t schema_ref, size_t columns_ref,
             Parent* parent, size_t ndx_in_parent):
    m_size(0), m_top(alloc), m_columns(alloc), m_spec_set(alloc), m_ref_count(0)
{
    Create(schema_ref, columns_ref, parent, ndx_in_parent);
}

void Table::Create(size_t ref_specSet, size_t columns_ref,
                   ArrayParent *parent, size_t ndx_in_parent)
{
    m_spec_set.update_ref(ref_specSet);

    // A table instatiated with a zero-ref is just an empty table
    // but it will have to create itself on first modification
    if (columns_ref != 0) {
        m_columns.UpdateRef(columns_ref);
        CacheColumns();
    }
    m_columns.SetParent(parent, ndx_in_parent);
}

void Table::CreateColumns()
{
    assert(!m_columns.IsValid() || m_columns.is_empty()); // only on initial creation

    // Instantiate first if we have an empty table (from zero-ref)
    if (!m_columns.IsValid()) {
        m_columns.SetType(COLUMN_HASREFS);
    }

    size_t subtable_count = 0;
    ColumnType attr = COLUMN_ATTR_NONE;
    Allocator& alloc = m_columns.GetAllocator();
    const size_t count = m_spec_set.get_type_attr_count();

    // Add the newly defined columns
    for (size_t i = 0; i < count; ++i) {
        const ColumnType type = m_spec_set.get_type_attr(i);
        const size_t ref_pos =  m_columns.Size();
        ColumnBase* newColumn = NULL;

        switch (type) {
            case COLUMN_TYPE_INT:
            case COLUMN_TYPE_BOOL:
            case COLUMN_TYPE_DATE:
                newColumn = new Column(COLUMN_NORMAL, alloc);
                m_columns.add(((Column*)newColumn)->GetRef());
                ((Column*)newColumn)->SetParent(&m_columns, ref_pos);
                break;
            case COLUMN_TYPE_STRING:
                newColumn = new AdaptiveStringColumn(alloc);
                m_columns.add(((AdaptiveStringColumn*)newColumn)->GetRef());
                ((Column*)newColumn)->SetParent(&m_columns, ref_pos);
                break;
            case COLUMN_TYPE_BINARY:
                newColumn = new ColumnBinary(alloc);
                m_columns.add(((ColumnBinary*)newColumn)->GetRef());
                ((ColumnBinary*)newColumn)->SetParent(&m_columns, ref_pos);
                break;
            case COLUMN_TYPE_TABLE:
            {
                const size_t subspec_ref = m_spec_set.get_subspec_ref(subtable_count);
                newColumn = new ColumnTable(subspec_ref, NULL, 0, alloc, this);
                m_columns.add(((ColumnTable*)newColumn)->GetRef());
                ((ColumnTable*)newColumn)->SetParent(&m_columns, ref_pos);
                ++subtable_count;
            }
                break;
            case COLUMN_TYPE_MIXED:
                newColumn = new ColumnMixed(alloc, this);
                m_columns.add(((ColumnMixed*)newColumn)->GetRef());
                ((ColumnMixed*)newColumn)->SetParent(&m_columns, ref_pos);
                break;

            // Attributes
            case COLUMN_ATTR_INDEXED:
            case COLUMN_ATTR_UNIQUE:
                attr = type;
                break;

            default:
                assert(false);
        }

        // Atributes on columns may define that they come with an index
        if (attr != COLUMN_ATTR_NONE) {
            assert(false); //TODO:
            //const index_ref = newColumn->CreateIndex(attr);
            //m_columns.add(index_ref);

            attr = COLUMN_ATTR_NONE;
        }

        // Cache Columns
        m_cols.add((intptr_t)newColumn);
    }
}

Spec& Table::get_spec()
{
    assert(m_top.IsValid()); // you can only change specs on top-level tablesu
    return m_spec_set;
}

const Spec& Table::get_spec() const
{
    return m_spec_set;
}


void Table::InstantiateBeforeChange()
{
    // Empty (zero-ref'ed) tables need to be instantiated before first modification
    if (!m_columns.IsValid()) CreateColumns();
}

void Table::CacheColumns()
{
    assert(m_cols.is_empty()); // only done on creation

    Allocator& alloc = m_columns.GetAllocator();
    ColumnType attr = COLUMN_ATTR_NONE;
    size_t size = (size_t)-1;
    size_t column_ndx = 0;
    const size_t count = m_spec_set.get_type_attr_count();
    size_t subtable_count = 0;

    // Cache columns
    for (size_t i = 0; i < count; ++i) {
        const ColumnType type = m_spec_set.get_type_attr(i);
        const size_t ref = m_columns.GetAsRef(column_ndx);

        ColumnBase* newColumn = NULL;
        size_t colsize = (size_t)-1;
        switch (type) {
            case COLUMN_TYPE_INT:
            case COLUMN_TYPE_BOOL:
            case COLUMN_TYPE_DATE:
                newColumn = new Column(ref, &m_columns, column_ndx, alloc);
                colsize = ((Column*)newColumn)->Size();
                break;
            case COLUMN_TYPE_STRING:
                newColumn = new AdaptiveStringColumn(ref, &m_columns, column_ndx, alloc);
                colsize = ((AdaptiveStringColumn*)newColumn)->Size();
                break;
            case COLUMN_TYPE_BINARY:
                newColumn = new ColumnBinary(ref, &m_columns, column_ndx, alloc);
                colsize = ((ColumnBinary*)newColumn)->Size();
                break;
            case COLUMN_TYPE_STRING_ENUM:
            {
                const size_t ref_values = m_columns.GetAsRef(column_ndx+1);
                newColumn = new ColumnStringEnum(ref, ref_values, &m_columns, column_ndx, alloc);
                colsize = ((ColumnStringEnum*)newColumn)->Size();
                ++column_ndx; // advance one extra pos to account for keys/values pair
                break;
            }
            case COLUMN_TYPE_TABLE:
            {
                const size_t ref_specSet = m_spec_set.get_subspec_ref(subtable_count);

                newColumn = new ColumnTable(ref, ref_specSet, &m_columns, column_ndx, alloc, this);
                colsize = ((ColumnTable*)newColumn)->Size();
                ++subtable_count;
                break;
            }
            case COLUMN_TYPE_MIXED:
                newColumn = new ColumnMixed(ref, &m_columns, column_ndx, alloc, this);
                colsize = ((ColumnMixed*)newColumn)->Size();
                break;

            // Attributes
            case COLUMN_ATTR_INDEXED:
            case COLUMN_ATTR_UNIQUE:
                attr = type;
                break;

            default:
                assert(false);
        }

        m_cols.add((intptr_t)newColumn);

        // Atributes on columns may define that they come with an index
        if (attr != COLUMN_ATTR_NONE) {
            const size_t index_ref = m_columns.GetAsRef(column_ndx+1);
            newColumn->SetIndexRef(index_ref);

            ++column_ndx; // advance one extra pos to account for index
            attr = COLUMN_ATTR_NONE;
        }

        // Set table size
        // (and verify that all column are same length)
        if (size == (size_t)-1) size = colsize;
        else assert(size == colsize);

        ++column_ndx;
    }

    if (size != (size_t)-1) m_size = size;
}

void Table::ClearCachedColumns()
{
    assert(m_cols.IsValid());

    const size_t count = m_cols.Size();
    for (size_t i = 0; i < count; ++i) {
        const ColumnType type = GetRealColumnType(i);
        if (type == COLUMN_TYPE_STRING_ENUM) {
            ColumnStringEnum* const column = (ColumnStringEnum* const)m_cols.Get(i);
            delete(column);
        }
        else {
            ColumnBase* const column = (ColumnBase* const)m_cols.Get(i);
            delete(column);
        }
    }
    m_cols.Destroy();
}

Table::~Table()
{
    // Delete cached columns
    ClearCachedColumns();

    if (m_top.IsValid()) {
        // 'm_top' has no parent if, and only if this is a free
        // standing instance of TopLevelTable. In that case it is the
        // responsibility of this destructor to deallocate all the
        // memory chunks that make up the entire hierarchy of
        // arrays. Otherwise we must notify our parent.
        if (ArrayParent *parent = m_top.GetParent()) {
            assert(m_ref_count == 0 || m_ref_count == 1);
            // assert(dynamic_cast<Parent *>(parent));
            static_cast<Parent *>(parent)->child_destroyed(m_top.GetParentNdx());
            return;
        }

        assert(m_ref_count == 1);
        m_top.Destroy();
        return;
    }

    // 'm_columns' has no parent if, and only if this is a free
    // standing instance of Table. In that case it is the
    // responsibility of this destructor to deallocate all the memory
    // chunks that make up the entire hierarchy of arrays. Otherwise
    // we must notify our parent.
    if (ArrayParent *parent = m_columns.GetParent()) {
        assert(m_ref_count == 0 || m_ref_count == 1);
        // assert(dynamic_cast<Parent *>(parent));
        static_cast<Parent *>(parent)->child_destroyed(m_columns.GetParentNdx());
        return;
    }

    assert(m_ref_count == 1);
    m_spec_set.destroy();
    m_columns.Destroy();
}

size_t Table::get_column_count() const
{
    return m_spec_set.get_column_count();
}

const char* Table::get_column_name(size_t ndx) const
{
    assert(ndx < get_column_count());
    return m_spec_set.get_column_name(ndx);
}

size_t Table::get_column_index(const char* name) const
{
    return m_spec_set.get_column_index(name);
}

ColumnType Table::GetRealColumnType(size_t ndx) const
{
    assert(ndx < get_column_count());
    return m_spec_set.get_real_column_type(ndx);
}

ColumnType Table::get_column_type(size_t ndx) const
{
    assert(ndx < get_column_count());

    // Hides internal types like COLUM_STRING_ENUM
    return m_spec_set.get_column_type(ndx);
}

size_t Table::GetColumnRefPos(size_t column_ndx) const
{
    size_t pos = 0;
    size_t current_column = 0;
    const size_t count = m_spec_set.get_type_attr_count();

    for (size_t i = 0; i < count; ++i) {
        if (current_column == column_ndx)
            return pos;

        const ColumnType type = (ColumnType)m_spec_set.get_type_attr(i);
        if (type >= COLUMN_ATTR_INDEXED)
            continue; // ignore attributes
        if (type < COLUMN_TYPE_STRING_ENUM)
            ++pos;
        else
            pos += 2;

        ++current_column;
    }

    assert(false);
    return (size_t)-1;
}

size_t Table::add_column(ColumnType type, const char* name)
{
    // Currently it's not possible to dynamically add columns to a table with content.
    assert(size() == 0);
    if (size() != 0)
        return (size_t)-1;

    const size_t column_ndx = m_cols.Size();

    ColumnBase* newColumn = NULL;
    Allocator& alloc = m_columns.GetAllocator();

    switch (type) {
    case COLUMN_TYPE_INT:
    case COLUMN_TYPE_BOOL:
    case COLUMN_TYPE_DATE:
        newColumn = new Column(COLUMN_NORMAL, alloc);
        m_columns.add(((Column*)newColumn)->GetRef());
        ((Column*)newColumn)->SetParent(&m_columns, m_columns.Size()-1);
        break;
    case COLUMN_TYPE_STRING:
        newColumn = new AdaptiveStringColumn(alloc);
        m_columns.add(((AdaptiveStringColumn*)newColumn)->GetRef());
        ((Column*)newColumn)->SetParent(&m_columns, m_columns.Size()-1);
        break;
    case COLUMN_TYPE_BINARY:
        newColumn = new ColumnBinary(alloc);
        m_columns.add(((ColumnBinary*)newColumn)->GetRef());
        ((ColumnBinary*)newColumn)->SetParent(&m_columns, m_columns.Size()-1);
        break;
    case COLUMN_TYPE_MIXED:
        newColumn = new ColumnMixed(alloc, this);
        m_columns.add(((ColumnMixed*)newColumn)->GetRef());
        ((ColumnMixed*)newColumn)->SetParent(&m_columns, m_columns.Size()-1);
        break;
    default:
        assert(false);
    }

    m_spec_set.add_column(type, name);
    m_cols.add((intptr_t)newColumn);

    return column_ndx;
}

bool Table::has_index(size_t column_ndx) const
{
    assert(column_ndx < get_column_count());
    const ColumnBase& col = GetColumnBase(column_ndx);
    return col.HasIndex();
}

void Table::set_index(size_t column_ndx)
{
    assert(column_ndx < get_column_count());
    if (has_index(column_ndx)) return;

    ColumnBase& col = GetColumnBase(column_ndx);

    if (col.IsIntColumn()) {
        Column& c = static_cast<Column&>(col);
        Index* index = new Index();
        c.BuildIndex(*index);
        m_columns.add((intptr_t)index->GetRef());
    }
    else {
        assert(false);
    }
}

ColumnBase& Table::GetColumnBase(size_t ndx)
{
    assert(ndx < get_column_count());
    InstantiateBeforeChange();
    return *reinterpret_cast<ColumnBase*>(m_cols.Get(ndx));
}

const ColumnBase& Table::GetColumnBase(size_t ndx) const
{
    assert(ndx < get_column_count());
    return *reinterpret_cast<ColumnBase*>(m_cols.Get(ndx));
}

Column& Table::GetColumn(size_t ndx)
{
    ColumnBase& column = GetColumnBase(ndx);
    assert(column.IsIntColumn());
    return static_cast<Column&>(column);
}

const Column& Table::GetColumn(size_t ndx) const
{
    const ColumnBase& column = GetColumnBase(ndx);
    assert(column.IsIntColumn());
    return static_cast<const Column&>(column);
}

AdaptiveStringColumn& Table::GetColumnString(size_t ndx)
{
    ColumnBase& column = GetColumnBase(ndx);
    assert(column.IsStringColumn());
    return static_cast<AdaptiveStringColumn&>(column);
}

const AdaptiveStringColumn& Table::GetColumnString(size_t ndx) const
{
    const ColumnBase& column = GetColumnBase(ndx);
    assert(column.IsStringColumn());
    return static_cast<const AdaptiveStringColumn&>(column);
}


ColumnStringEnum& Table::GetColumnStringEnum(size_t ndx)
{
    assert(ndx < get_column_count());
    InstantiateBeforeChange();
    return *reinterpret_cast<ColumnStringEnum*>(m_cols.Get(ndx));
}

const ColumnStringEnum& Table::GetColumnStringEnum(size_t ndx) const
{
    assert(ndx < get_column_count());
    return *reinterpret_cast<ColumnStringEnum*>(m_cols.Get(ndx));
}

ColumnBinary& Table::GetColumnBinary(size_t ndx)
{
    ColumnBase& column = GetColumnBase(ndx);
    assert(column.IsBinaryColumn());
    return static_cast<ColumnBinary&>(column);
}

const ColumnBinary& Table::GetColumnBinary(size_t ndx) const
{
    const ColumnBase& column = GetColumnBase(ndx);
    assert(column.IsBinaryColumn());
    return static_cast<const ColumnBinary&>(column);
}

ColumnTable &Table::GetColumnTable(size_t ndx)
{
    assert(ndx < get_column_count());
    InstantiateBeforeChange();
    return *reinterpret_cast<ColumnTable*>(m_cols.Get(ndx));
}

ColumnTable const &Table::GetColumnTable(size_t ndx) const
{
    assert(ndx < get_column_count());
    return *reinterpret_cast<ColumnTable*>(m_cols.Get(ndx));
}

ColumnMixed& Table::GetColumnMixed(size_t ndx)
{
    assert(ndx < get_column_count());
    InstantiateBeforeChange();
    return *reinterpret_cast<ColumnMixed*>(m_cols.Get(ndx));
}

const ColumnMixed& Table::GetColumnMixed(size_t ndx) const
{
    assert(ndx < get_column_count());
    return *reinterpret_cast<ColumnMixed*>(m_cols.Get(ndx));
}

size_t Table::add_empty_row(size_t num_of_rows)
{
    const size_t col_count = get_column_count();

    for (size_t row = 0; row < num_of_rows; ++row) {
        for (size_t i = 0; i < col_count; ++i) {
            ColumnBase& column = GetColumnBase(i);
            column.add();
        }
    }

    // Return index of first new added row
    size_t new_ndx = m_size;
    m_size += num_of_rows;
    return new_ndx;
}

void Table::insert_empty_row(size_t ndx, size_t num_of_rows)
{
    const size_t col_count = get_column_count();

    for (size_t row = 0; row < num_of_rows; ++row) {
        for (size_t i = 0; i < col_count; ++i) {
            ColumnBase& column = GetColumnBase(i);
            column.insert(ndx+i); // FIXME: This should be optimized by passing 'num_of_rows' to column.insert()
        }
    }
}

void Table::clear()
{
    const size_t count = get_column_count();
    for (size_t i = 0; i < count; ++i) {
        ColumnBase& column = GetColumnBase(i);
        column.Clear();
    }
    m_size = 0;
}

void Table::remove(size_t ndx)
{
    assert(ndx < m_size);

    const size_t count = get_column_count();
    for (size_t i = 0; i < count; ++i) {
        ColumnBase& column = GetColumnBase(i);
        column.Delete(ndx);
    }
    --m_size;
}

void Table::insert_subtable(size_t column_ndx, size_t ndx)
{
    assert(column_ndx < get_column_count());
    assert(GetRealColumnType(column_ndx) == COLUMN_TYPE_TABLE);
    assert(ndx <= m_size);

    ColumnTable& subtables = GetColumnTable(column_ndx);
    subtables.Insert(ndx);
}

Table* Table::get_subtable_ptr(size_t col_idx, size_t row_idx)
{
    assert(col_idx < get_column_count());
    assert(row_idx < m_size);

    const ColumnType type = GetRealColumnType(col_idx);
    if (type == COLUMN_TYPE_TABLE) {
        ColumnTable& subtables = GetColumnTable(col_idx);
        return subtables.get_subtable_ptr(row_idx);
    }
    if (type == COLUMN_TYPE_MIXED) {
        ColumnMixed& subtables = GetColumnMixed(col_idx);
        return subtables.get_subtable_ptr(row_idx);
    }
    assert(false);
    return 0;
}

const Table* Table::get_subtable_ptr(size_t col_idx, size_t row_idx) const
{
    assert(col_idx < get_column_count());
    assert(row_idx < m_size);

    const ColumnType type = GetRealColumnType(col_idx);
    if (type == COLUMN_TYPE_TABLE) {
        const ColumnTable& subtables = GetColumnTable(col_idx);
        return subtables.get_subtable_ptr(row_idx);
    }
    if (type == COLUMN_TYPE_MIXED) {
        const ColumnMixed& subtables = GetColumnMixed(col_idx);
        return subtables.get_subtable_ptr(row_idx);
    }
    assert(false);
    return 0;
}

size_t Table::get_subtable_size(size_t col_idx, size_t row_idx) const
{
    assert(col_idx < get_column_count());
    assert(row_idx < m_size);

    const ColumnType type = GetRealColumnType(col_idx);
    if (type == COLUMN_TYPE_TABLE) {
        const ColumnTable& subtables = GetColumnTable(col_idx);
        return subtables.get_subtable_size(row_idx);
    }
    if (type == COLUMN_TYPE_MIXED) {
        const ColumnMixed& subtables = GetColumnMixed(col_idx);
        return subtables.get_subtable_size(row_idx);
    }
    assert(false);
    return 0;
}

void Table::clear_subtable(size_t col_idx, size_t row_idx)
{
    assert(col_idx < get_column_count());
    assert(row_idx <= m_size);

    const ColumnType type = GetRealColumnType(col_idx);
    if (type == COLUMN_TYPE_TABLE) {
        ColumnTable& subtables = GetColumnTable(col_idx);
        subtables.Clear(row_idx);
    }
    else if (type == COLUMN_TYPE_MIXED) {
        ColumnMixed& subtables = GetColumnMixed(col_idx);
        subtables.SetTable(row_idx);
    }
    else {
        assert(false);
    }
}

int64_t Table::get_int(size_t column_ndx, size_t ndx) const
{
    assert(column_ndx < get_column_count());
    assert(ndx < m_size);

    const Column& column = GetColumn(column_ndx);
    return column.Get(ndx);
}

void Table::set_int(size_t column_ndx, size_t ndx, int64_t value)
{
    assert(column_ndx < get_column_count());
    assert(ndx < m_size);

    Column& column = GetColumn(column_ndx);
    column.Set(ndx, value);
}

void Table::add_int(size_t column_ndx, int64_t value)
{
    GetColumn(column_ndx).Increment64(value);
}

bool Table::get_bool(size_t column_ndx, size_t ndx) const
{
    assert(column_ndx < get_column_count());
    assert(GetRealColumnType(column_ndx) == COLUMN_TYPE_BOOL);
    assert(ndx < m_size);

    const Column& column = GetColumn(column_ndx);
    return column.Get(ndx) != 0;
}

void Table::set_bool(size_t column_ndx, size_t ndx, bool value)
{
    assert(column_ndx < get_column_count());
    assert(GetRealColumnType(column_ndx) == COLUMN_TYPE_BOOL);
    assert(ndx < m_size);

    Column& column = GetColumn(column_ndx);
    column.Set(ndx, value ? 1 : 0);
}

time_t Table::get_date(size_t column_ndx, size_t ndx) const
{
    assert(column_ndx < get_column_count());
    assert(GetRealColumnType(column_ndx) == COLUMN_TYPE_DATE);
    assert(ndx < m_size);

    const Column& column = GetColumn(column_ndx);
    return (time_t)column.Get(ndx);
}

void Table::set_date(size_t column_ndx, size_t ndx, time_t value)
{
    assert(column_ndx < get_column_count());
    assert(GetRealColumnType(column_ndx) == COLUMN_TYPE_DATE);
    assert(ndx < m_size);

    Column& column = GetColumn(column_ndx);
    column.Set(ndx, (int64_t)value);
}

void Table::insert_int(size_t column_ndx, size_t ndx, int64_t value)
{
    assert(column_ndx < get_column_count());
    assert(ndx <= m_size);

    Column& column = GetColumn(column_ndx);
    column.Insert(ndx, value);
}

const char* Table::get_string(size_t column_ndx, size_t ndx) const
{
    assert(column_ndx < m_columns.Size());
    assert(ndx < m_size);

    const ColumnType type = GetRealColumnType(column_ndx);

    if (type == COLUMN_TYPE_STRING) {
        const AdaptiveStringColumn& column = GetColumnString(column_ndx);
        return column.Get(ndx);
    }
    else {
        assert(type == COLUMN_TYPE_STRING_ENUM);
        const ColumnStringEnum& column = GetColumnStringEnum(column_ndx);
        return column.Get(ndx);
    }
}

void Table::set_string(size_t column_ndx, size_t ndx, const char* value)
{
    assert(column_ndx < get_column_count());
    assert(ndx < m_size);

    const ColumnType type = GetRealColumnType(column_ndx);

    if (type == COLUMN_TYPE_STRING) {
        AdaptiveStringColumn& column = GetColumnString(column_ndx);
        column.Set(ndx, value);
    }
    else {
        assert(type == COLUMN_TYPE_STRING_ENUM);
        ColumnStringEnum& column = GetColumnStringEnum(column_ndx);
        column.Set(ndx, value);
    }
}

void Table::insert_string(size_t column_ndx, size_t ndx, const char* value)
{
    assert(column_ndx < get_column_count());
    assert(ndx <= m_size);

    const ColumnType type = GetRealColumnType(column_ndx);

    if (type == COLUMN_TYPE_STRING) {
        AdaptiveStringColumn& column = GetColumnString(column_ndx);
        column.Insert(ndx, value);
    }
    else {
        assert(type == COLUMN_TYPE_STRING_ENUM);
        ColumnStringEnum& column = GetColumnStringEnum(column_ndx);
        column.Insert(ndx, value);
    }
}

BinaryData Table::get_binary(size_t column_ndx, size_t ndx) const
{
    assert(column_ndx < m_columns.Size());
    assert(ndx < m_size);

    const ColumnBinary& column = GetColumnBinary(column_ndx);
    return column.Get(ndx);
}

void Table::set_binary(size_t column_ndx, size_t ndx, const char* value, size_t len)
{
    assert(column_ndx < get_column_count());
    assert(ndx < m_size);

    ColumnBinary& column = GetColumnBinary(column_ndx);
    column.Set(ndx, value, len);
}

void Table::insert_binary(size_t column_ndx, size_t ndx, const char* value, size_t len)
{
    assert(column_ndx < get_column_count());
    assert(ndx <= m_size);

    ColumnBinary& column = GetColumnBinary(column_ndx);
    column.Insert(ndx, value, len);
}

Mixed Table::get_mixed(size_t column_ndx, size_t ndx) const
{
    assert(column_ndx < m_columns.Size());
    assert(ndx < m_size);

    const ColumnMixed& column = GetColumnMixed(column_ndx);
    const ColumnType   type   = column.GetType(ndx);

    switch (type) {
        case COLUMN_TYPE_INT:
            return Mixed(column.GetInt(ndx));
        case COLUMN_TYPE_BOOL:
            return Mixed(column.get_bool(ndx));
        case COLUMN_TYPE_DATE:
            return Mixed(Date(column.get_date(ndx)));
        case COLUMN_TYPE_STRING:
            return Mixed(column.get_string(ndx));
        case COLUMN_TYPE_BINARY:
            return Mixed(column.get_binary(ndx));
        case COLUMN_TYPE_TABLE:
            return Mixed(COLUMN_TYPE_TABLE);
        default:
            assert(false);
            return Mixed((int64_t)0);
    }
}

ColumnType Table::get_mixed_type(size_t column_ndx, size_t ndx) const
{
    assert(column_ndx < m_columns.Size());
    assert(ndx < m_size);

    const ColumnMixed& column = GetColumnMixed(column_ndx);
    return column.GetType(ndx);
}

void Table::set_mixed(size_t column_ndx, size_t ndx, Mixed value)
{
    assert(column_ndx < get_column_count());
    assert(ndx < m_size);

    ColumnMixed& column = GetColumnMixed(column_ndx);
    const ColumnType type = value.get_type();

    switch (type) {
        case COLUMN_TYPE_INT:
            column.SetInt(ndx, value.get_int());
            break;
        case COLUMN_TYPE_BOOL:
            column.set_bool(ndx, value.get_bool());
            break;
        case COLUMN_TYPE_DATE:
            column.set_date(ndx, value.get_date());
            break;
        case COLUMN_TYPE_STRING:
            column.set_string(ndx, value.get_string());
            break;
        case COLUMN_TYPE_BINARY:
        {
            const BinaryData b = value.get_binary();
            column.set_binary(ndx, (const char*)b.pointer, b.len);
            break;
        }
        case COLUMN_TYPE_TABLE:
            column.SetTable(ndx);
            break;
        default:
            assert(false);
    }
}

void Table::insert_mixed(size_t column_ndx, size_t ndx, Mixed value) {
    assert(column_ndx < get_column_count());
    assert(ndx <= m_size);

    ColumnMixed& column = GetColumnMixed(column_ndx);
    const ColumnType type = value.get_type();

    switch (type) {
        case COLUMN_TYPE_INT:
            column.insert_int(ndx, value.get_int());
            break;
        case COLUMN_TYPE_BOOL:
            column.insert_bool(ndx, value.get_bool());
            break;
        case COLUMN_TYPE_DATE:
            column.insert_date(ndx, value.get_date());
            break;
        case COLUMN_TYPE_STRING:
            column.insert_string(ndx, value.get_string());
            break;
        case COLUMN_TYPE_BINARY:
        {
            const BinaryData b = value.get_binary();
            column.insert_binary(ndx, (const char*)b.pointer, b.len);
            break;
        }
        case COLUMN_TYPE_TABLE:
            column.insert_table(ndx);
            break;
        default:
            assert(false);
    }
}

void Table::insert_done()
{
    ++m_size;

#ifdef _DEBUG
    Verify();
#endif //_DEBUG
}

int64_t Table::sum(size_t column_ndx) const
{
    assert(column_ndx < get_column_count());
    assert(get_column_type(column_ndx) == COLUMN_TYPE_INT);
    int64_t sum = 0;

    for(size_t i = 0; i < size(); ++i)
        sum += get_int(column_ndx, i);

    return sum;
}

int64_t Table::maximum(size_t column_ndx) const
{
    if (is_empty()) return 0;

    int64_t mv = get_int(column_ndx, 0);
    for (size_t i = 1; i < size(); ++i) {
        const int64_t v = get_int(column_ndx, i);
        if (v > mv) {
            mv = v;
        }
    }
    return mv;
}

int64_t Table::minimum(size_t column_ndx) const
{
    if (is_empty()) return 0;

    int64_t mv = get_int(column_ndx, 0);
    for (size_t i = 1; i < size(); ++i) {
        const int64_t v = get_int(column_ndx, i);
        if (v < mv) {
            mv = v;
        }
    }
    return mv;
}

size_t Table::find_first_int(size_t column_ndx, int64_t value) const
{
    assert(column_ndx < m_columns.Size());
    assert(GetRealColumnType(column_ndx) == COLUMN_TYPE_INT);
    const Column& column = GetColumn(column_ndx);

    return column.find_first(value);
}

size_t Table::find_first_bool(size_t column_ndx, bool value) const
{
    assert(column_ndx < m_columns.Size());
    assert(GetRealColumnType(column_ndx) == COLUMN_TYPE_BOOL);
    const Column& column = GetColumn(column_ndx);

    return column.find_first(value ? 1 : 0);
}

size_t Table::find_first_date(size_t column_ndx, time_t value) const
{
    assert(column_ndx < m_columns.Size());
    assert(GetRealColumnType(column_ndx) == COLUMN_TYPE_DATE);
    const Column& column = GetColumn(column_ndx);

    return column.find_first((int64_t)value);
}

size_t Table::find_first_string(size_t column_ndx, const char* value) const
{
    assert(column_ndx < m_columns.Size());

    const ColumnType type = GetRealColumnType(column_ndx);

    if (type == COLUMN_TYPE_STRING) {
        const AdaptiveStringColumn& column = GetColumnString(column_ndx);
        return column.find_first(value);
    }
    else {
        assert(type == COLUMN_TYPE_STRING_ENUM);
        const ColumnStringEnum& column = GetColumnStringEnum(column_ndx);
        return column.find_first(value);
    }
}

size_t Table::find_pos_int(size_t column_ndx, int64_t value) const
{
    return GetColumn(column_ndx).find_pos(value);
}

TableView Table::find_all_int(size_t column_ndx, int64_t value)
{
    assert(column_ndx < m_columns.Size());

    const Column& column = GetColumn(column_ndx);

    TableView tv(*this);
    column.find_all(tv.get_ref_column(), value);
    return move(tv);
}

ConstTableView Table::find_all_int(size_t column_ndx, int64_t value) const
{
    assert(column_ndx < m_columns.Size());

    const Column& column = GetColumn(column_ndx);

    ConstTableView tv(*this);
    column.find_all(tv.get_ref_column(), value);
    return move(tv);
}

TableView Table::find_all_bool(size_t column_ndx, bool value)
{
    assert(column_ndx < m_columns.Size());

    const Column& column = GetColumn(column_ndx);

    TableView tv(*this);
    column.find_all(tv.get_ref_column(), value ? 1 :0);
    return move(tv);
}

ConstTableView Table::find_all_bool(size_t column_ndx, bool value) const
{
    assert(column_ndx < m_columns.Size());

    const Column& column = GetColumn(column_ndx);

    ConstTableView tv(*this);
    column.find_all(tv.get_ref_column(), value ? 1 :0);
    return move(tv);
}

TableView Table::find_all_date(size_t column_ndx, time_t value)
{
    assert(column_ndx < m_columns.Size());

    const Column& column = GetColumn(column_ndx);

    TableView tv(*this);
    column.find_all(tv.get_ref_column(), int64_t(value));
    return move(tv);
}

ConstTableView Table::find_all_date(size_t column_ndx, time_t value) const
{
    assert(column_ndx < m_columns.Size());

    const Column& column = GetColumn(column_ndx);

    ConstTableView tv(*this);
    column.find_all(tv.get_ref_column(), int64_t(value));
    return move(tv);
}

TableView Table::find_all_string(size_t column_ndx, const char *value)
{
    assert(column_ndx < m_columns.Size());

    const ColumnType type = GetRealColumnType(column_ndx);

    TableView tv(*this);
    if (type == COLUMN_TYPE_STRING) {
        const AdaptiveStringColumn& column = GetColumnString(column_ndx);
        column.find_all(tv.get_ref_column(), value);
    }
    else {
        assert(type == COLUMN_TYPE_STRING_ENUM);
        const ColumnStringEnum& column = GetColumnStringEnum(column_ndx);
        column.find_all(tv.get_ref_column(), value);
    }
    return move(tv);
}

ConstTableView Table::find_all_string(size_t column_ndx, const char *value) const
{
    assert(column_ndx < m_columns.Size());

    const ColumnType type = GetRealColumnType(column_ndx);

    ConstTableView tv(*this);
    if (type == COLUMN_TYPE_STRING) {
        const AdaptiveStringColumn& column = GetColumnString(column_ndx);
        column.find_all(tv.get_ref_column(), value);
    }
    else {
        assert(type == COLUMN_TYPE_STRING_ENUM);
        const ColumnStringEnum& column = GetColumnStringEnum(column_ndx);
        column.find_all(tv.get_ref_column(), value);
    }
    return move(tv);
}



TableView Table::find_all_hamming(size_t column_ndx, uint64_t value, size_t max)
{
    assert(column_ndx < m_columns.Size());

    const Column& column = GetColumn(column_ndx);

    TableView tv(*this);
    column.find_all_hamming(tv.get_ref_column(), value, max);
    return move(tv);
}

ConstTableView Table::find_all_hamming(size_t column_ndx, uint64_t value, size_t max) const
{
    assert(column_ndx < m_columns.Size());

    const Column& column = GetColumn(column_ndx);

    ConstTableView tv(*this);
    column.find_all_hamming(tv.get_ref_column(), value, max);
    return move(tv);
}

TableView Table::sorted(size_t column_ndx, bool ascending)
{
    assert(column_ndx < m_columns.Size());

    TableView tv(*this);

    // Insert refs to all rows in table
    Array& refs = tv.get_ref_column();
    const size_t count = size();
    for (size_t i = 0; i < count; ++i) {
        refs.add(i);
    }

    // Sort the refs based on the given column
    tv.sort(column_ndx, ascending);

    return move(tv);
}

ConstTableView Table::sorted(size_t column_ndx, bool ascending) const
{
    assert(column_ndx < m_columns.Size());

    ConstTableView tv(*this);

    // Insert refs to all rows in table
    Array& refs = tv.get_ref_column();
    const size_t count = size();
    for (size_t i = 0; i < count; ++i) {
        refs.add(i);
    }

    // Sort the refs based on the given column
    tv.sort(column_ndx, ascending);

    return move(tv);
}

void Table::optimize()
{
    const size_t column_count = get_column_count();
    Allocator& alloc = m_columns.GetAllocator();

    for (size_t i = 0; i < column_count; ++i) {
        const ColumnType type = GetRealColumnType(i);

        if (type == COLUMN_TYPE_STRING) {
            AdaptiveStringColumn& column = GetColumnString(i);

            size_t ref_keys;
            size_t ref_values;
            const bool res = column.AutoEnumerate(ref_keys, ref_values);
            if (!res) continue;

            // Add to spec and column refs
            m_spec_set.set_column_type(i, COLUMN_TYPE_STRING_ENUM);
            const size_t column_ndx = GetColumnRefPos(i);
            m_columns.Set(column_ndx, ref_keys);
            m_columns.Insert(column_ndx+1, ref_values);

            // There are still same number of columns, but since
            // the enum type takes up two posistions in m_columns
            // we have to move refs in all following columns
            UpdateColumnRefs(column_ndx+1, 1);

            // Replace cached column
            ColumnStringEnum* e = new ColumnStringEnum(ref_keys, ref_values, &m_columns, column_ndx, alloc);
            m_cols.Set(i, (intptr_t)e);
            column.Destroy();
            delete &column;
        }
    }
}

void Table::UpdateColumnRefs(size_t column_ndx, int diff)
{
    for (size_t i = column_ndx; i < m_cols.Size(); ++i) {
        ColumnBase* const column = (ColumnBase*)m_cols.Get(i);
        column->UpdateParentNdx(diff);
    }
}

void Table::UpdateFromParent() {
    // There is no top for sub-tables sharing schema
    if (m_top.IsValid()) {
        if (!m_top.UpdateFromParent()) return;
    }

    m_spec_set.update_from_parent();
    if (!m_columns.UpdateFromParent()) return;

    // Update cached columns
    const size_t column_count = get_column_count();
    for (size_t i = 0; i < column_count; ++i) {
        ColumnBase* const column = (ColumnBase*)m_cols.Get(i);
        column->UpdateFromParent();
    }

    // Size may have changed
    if (column_count == 0) {
        m_size = 0;
    }
    else {
        const ColumnBase* const column = (ColumnBase*)m_cols.Get(0);
        m_size = column->Size();
    }
}


void Table::update_from_spec()
{
    assert(m_columns.is_empty() && m_cols.is_empty()); // only on initial creation

    CreateColumns();
}


size_t Table::create_table(Allocator& alloc)
{
    FakeParent fake_parent;
    Table t(alloc);
    t.m_top.SetParent(&fake_parent, 0);
    return t.m_top.GetRef();
}


void Table::to_json(std::ostream& out)
{
    // Represent table as list of objects
    out << "[";

    const size_t row_count    = size();
    const size_t column_count = get_column_count();

    // We need a buffer for formatting dates (and binary to hex). Max
    // size is 21 bytes (incl quotes and zero byte) "YYYY-MM-DD HH:MM:SS"\0
    char buffer[30];

    for (size_t r = 0; r < row_count; ++r) {
        if (r) out << ",";
        out << "{";

        for (size_t i = 0; i < column_count; ++i) {
            if (i) out << ",";

            const char* const name = get_column_name(i);
            out << "\"" << name << "\":";

            const ColumnType type = get_column_type(i);
            switch (type) {
                case COLUMN_TYPE_INT:
                    out << get_int(i, r);
                    break;
                case COLUMN_TYPE_BOOL:
                    out << (get_bool(i, r) ? "true" : "false");
                    break;
                case COLUMN_TYPE_STRING:
                    out << "\"" << get_string(i, r) << "\"";
                    break;
                case COLUMN_TYPE_DATE:
                {
                    const time_t rawtime = get_date(i, r);
                    struct tm* const t = gmtime(&rawtime);
                    const size_t res = strftime(buffer, 30, "\"%Y-%m-%d %H:%M:%S\"", t);
                    if (!res) break;

                    out << buffer;
                    break;
                }
                case COLUMN_TYPE_BINARY:
                {
                    const BinaryData bin = get_binary(i, r);
                    const char* const p = (char*)bin.pointer;

                    out << "\"";
                    for (size_t i = 0; i < bin.len; ++i) {
                        sprintf(buffer, "%02x", (unsigned int)p[i]);
                        out << buffer;
                    }
                    out << "\"";
                    break;
                }
                case COLUMN_TYPE_TABLE:
                {
                    get_subtable(i, r)->to_json(out);
                    break;
                }
                case COLUMN_TYPE_MIXED:
                {
                    const ColumnType mtype = get_mixed_type(i, r);
                    if (mtype == COLUMN_TYPE_TABLE) {
                        get_subtable(i, r)->to_json(out);
                    }
                    else {
                        const Mixed m = get_mixed(i, r);
                        switch (mtype) {
                            case COLUMN_TYPE_INT:
                                out << m.get_int();
                                break;
                            case COLUMN_TYPE_BOOL:
                                out << m.get_bool();
                                break;
                            case COLUMN_TYPE_STRING:
                                out << "\"" << m.get_string() << "\"";
                                break;
                            case COLUMN_TYPE_DATE:
                            {
                                const time_t rawtime = m.get_date();
                                struct tm* const t = gmtime(&rawtime);
                                const size_t res = strftime(buffer, 30, "\"%Y-%m-%d %H:%M:%S\"", t);
                                if (!res) break;

                                out << buffer;
                                break;
                            }
                            case COLUMN_TYPE_BINARY:
                            {
                                const BinaryData bin = m.get_binary();
                                const char* const p = (char*)bin.pointer;

                                out << "\"";
                                for (size_t i = 0; i < bin.len; ++i) {
                                    sprintf(buffer, "%02x", (unsigned int)p[i]);
                                    out << buffer;
                                }
                                out << "\"";
                                break;
                            }
                            default:
                                assert(false);
                        }

                    }
                    break;
                }

                default:
                    assert(false);
            }
        }

        out << "}";
    }

    out << "]";
}

#ifdef _DEBUG

bool Table::compare(const Table& c) const
{
    if (!m_spec_set.compare(c.m_spec_set)) return false;

    const size_t column_count = get_column_count();
    if (column_count != c.get_column_count()) return false;

    for (size_t i = 0; i < column_count; ++i) {
        const ColumnType type = GetRealColumnType(i);

        switch (type) {
            case COLUMN_TYPE_INT:
            case COLUMN_TYPE_BOOL:
                {
                    const Column& column1 = GetColumn(i);
                    const Column& column2 = c.GetColumn(i);
                    if (!column1.Compare(column2)) return false;
                }
                break;
            case COLUMN_TYPE_STRING:
                {
                    const AdaptiveStringColumn& column1 = GetColumnString(i);
                    const AdaptiveStringColumn& column2 = c.GetColumnString(i);
                    if (!column1.Compare(column2)) return false;
                }
                break;
            case COLUMN_TYPE_STRING_ENUM:
                {
                    const ColumnStringEnum& column1 = GetColumnStringEnum(i);
                    const ColumnStringEnum& column2 = c.GetColumnStringEnum(i);
                    if (!column1.Compare(column2)) return false;
                }
                break;

            default:
                assert(false);
        }
    }
    return true;
}

void Table::Verify() const
{
    if (m_top.IsValid()) m_top.Verify();
    m_columns.Verify();
    if (m_columns.IsValid()) {
        const size_t column_count = get_column_count();
        assert(column_count == m_cols.Size());

        for (size_t i = 0; i < column_count; ++i) {
            const ColumnType type = GetRealColumnType(i);
            switch (type) {
            case COLUMN_TYPE_INT:
            case COLUMN_TYPE_BOOL:
            case COLUMN_TYPE_DATE:
                {
                    const Column& column = GetColumn(i);
                    assert(column.Size() == m_size);
                    column.Verify();
                }
                break;
            case COLUMN_TYPE_STRING:
                {
                    const AdaptiveStringColumn& column = GetColumnString(i);
                    assert(column.Size() == m_size);
                    column.Verify();
                }
                break;
            case COLUMN_TYPE_STRING_ENUM:
                {
                    const ColumnStringEnum& column = GetColumnStringEnum(i);
                    assert(column.Size() == m_size);
                    column.Verify();
                }
                break;
            case COLUMN_TYPE_BINARY:
                {
                    const ColumnBinary& column = GetColumnBinary(i);
                    assert(column.Size() == m_size);
                    column.Verify();
                }
                break;
            case COLUMN_TYPE_TABLE:
                {
                    const ColumnTable& column = GetColumnTable(i);
                    assert(column.Size() == m_size);
                    column.Verify();
                }
                break;
            case COLUMN_TYPE_MIXED:
                {
                    const ColumnMixed& column = GetColumnMixed(i);
                    assert(column.Size() == m_size);
                    column.Verify();
                }
                break;
            default:
                assert(false);
            }
        }
    }

    m_spec_set.Verify();

    Allocator& alloc = m_columns.GetAllocator();
    alloc.Verify();
}

void Table::to_dot(std::ostream& out, const char* title) const
{
    if (m_top.IsValid()) {
        out << "subgraph cluster_topleveltable" << m_top.GetRef() << " {" << endl;
        out << " label = \"TopLevelTable";
        if (title) out << "\\n'" << title << "'";
        out << "\";" << endl;
        m_top.ToDot(out, "table_top");
        const Spec& specset = get_spec();
        specset.to_dot(out);
    }
    else {
        out << "subgraph cluster_table_"  << m_columns.GetRef() <<  " {" << endl;
        out << " label = \"Table";
        if (title) out << " " << title;
        out << "\";" << endl;
    }

    ToDotInternal(out);

    out << "}" << endl;
}

void Table::ToDotInternal(std::ostream& out) const
{
    m_columns.ToDot(out, "columns");

    // Columns
    const size_t column_count = get_column_count();
    for (size_t i = 0; i < column_count; ++i) {
        const ColumnBase& column = GetColumnBase(i);
        const char* const name = get_column_name(i);
        column.ToDot(out, name);
    }
}

void Table::print() const
{
    // Table header
    cout << "Table: len(" << m_size << ")\n    ";
    const size_t column_count = get_column_count();
    for (size_t i = 0; i < column_count; ++i) {
        const char* name = m_spec_set.get_column_name(i);
        cout << left << setw(10) << name << right << " ";
    }

    // Types
    cout << "\n    ";
    for (size_t i = 0; i < column_count; ++i) {
        const ColumnType type = GetRealColumnType(i);
        switch (type) {
        case COLUMN_TYPE_INT:
            cout << "Int        "; break;
        case COLUMN_TYPE_BOOL:
            cout << "Bool       "; break;
        case COLUMN_TYPE_STRING:
            cout << "String     "; break;
        default:
            assert(false);
        }
    }
    cout << "\n";

    // Columns
    for (size_t i = 0; i < m_size; ++i) {
        cout << setw(3) << i;
        for (size_t n = 0; n < column_count; ++n) {
            const ColumnType type = GetRealColumnType(n);
            switch (type) {
            case COLUMN_TYPE_INT:
                {
                    const Column& column = GetColumn(n);
                    cout << setw(10) << column.Get(i) << " ";
                }
                break;
            case COLUMN_TYPE_BOOL:
                {
                    const Column& column = GetColumn(n);
                    cout << (column.Get(i) == 0 ? "     false " : "      true ");
                }
                break;
            case COLUMN_TYPE_STRING:
                {
                    const AdaptiveStringColumn& column = GetColumnString(n);
                    cout << setw(10) << column.Get(i) << " ";
                }
                break;
            default:
                assert(false);
            }
        }
        cout << "\n";
    }
    cout << "\n";
}

MemStats Table::stats() const
{
    MemStats stats;
    m_top.Stats(stats);

    return stats;
}

#endif //_DEBUG


} // namespace tightdb
