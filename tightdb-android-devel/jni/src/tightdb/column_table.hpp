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
#ifndef TIGHTDB_COLUMN_TABLE_HPP
#define TIGHTDB_COLUMN_TABLE_HPP

#include "column.hpp"
#include "table.hpp"

namespace tightdb {


/**
 * Base class for any column that can contain subtables.
 */
class ColumnSubtableParent: public Column, public Table::Parent
{
public:
    void UpdateFromParent();

protected:
    ColumnSubtableParent(ArrayParent* parent_array, std::size_t parent_ndx,
                         Allocator& alloc, const Table* tab);

    ColumnSubtableParent(std::size_t ref, ArrayParent* parent_array, std::size_t parent_ndx,
                         Allocator& alloc, const Table* tab);

    /**
     * Get the subtable at the specified index.
     *
     * This method must be used only for subtables with shared schema,
     * i.e. for elements of ColumnTable.
     *
     * The returned table pointer must always end up being wrapped in
     * a TableRef.
     */
    Table* get_subtable_ptr(std::size_t subtable_ndx, std::size_t schema_ref) const;

    /**
     * Get the subtable at the specified index.
     *
     * This method must be used only for subtables with independent
     * schemas, i.e. for elements of ColumnMixed.
     *
     * The returned table pointer must always end up being wrapped in
     * a TableRef.
     */
    Table* get_subtable_ptr(std::size_t subtable_ndx) const;

    /*
     * This method must be used only for subtables with shared schema,
     * i.e. for elements of ColumnTable.
     */
    TableRef get_subtable(std::size_t subtable_ndx, std::size_t schema_ref) const
    {
        return TableRef(get_subtable_ptr(subtable_ndx, schema_ref));
    }

    /*
     * This method must be used only for subtables with independent
     * schemas, i.e. for elements of ColumnMixed.
     */
    TableRef get_subtable(std::size_t subtable_ndx) const
    {
        return TableRef(get_subtable_ptr(subtable_ndx));
    }

    // Overriding methods in ArrayParent.
    virtual void update_child_ref(std::size_t subtable_ndx, std::size_t new_ref);
    virtual std::size_t get_child_ref(std::size_t subtable_ndx) const;

    // Overriding method in Table::Parent
    virtual void child_destroyed(std::size_t subtable_ndx);

private:
    struct SubtableMap {
        SubtableMap(Allocator& alloc): m_indices(alloc), m_wrappers(alloc) {}
        ~SubtableMap();
        bool empty() const { return !m_indices.IsValid() || m_indices.is_empty(); }
        Table* find(std::size_t subtable_ndx) const;
        void insert(std::size_t subtable_ndx, Table* wrapper);
        void remove(std::size_t subtable_ndx);
        void update_from_parents();
    private:
        Array m_indices;
        Array m_wrappers;
    };

    const Table* const m_table;
    mutable SubtableMap m_subtable_map;
};



class ColumnTable: public ColumnSubtableParent {
public:
    /**
     * Create a table column and have it instantiate a new array
     * structure.
     *
     * \param tab If this column is used as part of a table you must
     * pass a pointer to that table. Otherwise you may pass null.
     */
    ColumnTable(std::size_t schema_ref, ArrayParent* parent, std::size_t idx_in_parent,
                Allocator& alloc, const Table* tab);

    /**
     * Create a table column and attach it to an already existing
     * array structure.
     *
     * \param tab If this column is used as part of a table you must
     * pass a pointer to that table. Otherwise you may pass null.
     */
    ColumnTable(std::size_t columns_ref, std::size_t schema_ref,
                ArrayParent* parent, std::size_t idx_in_parent,
                Allocator& alloc, const Table* tab);

    size_t get_subtable_size(size_t ndx) const;

    /**
     * The returned table pointer must always end up being wrapped in
     * an instance of BasicTableRef.
     */
    Table* get_subtable_ptr(std::size_t subtable_ndx) const
    {
        return ColumnSubtableParent::get_subtable_ptr(subtable_ndx, m_ref_specSet);
    }

    bool add();
    void Insert(size_t ndx);
    void Delete(size_t ndx);
    void Clear(size_t ndx);

#ifdef _DEBUG
    void Verify() const; // Must be upper case to avoid conflict with macro in ObjC
#endif //_DEBUG

protected:
#ifdef _DEBUG
    virtual void LeafToDot(std::ostream& out, const Array& array) const;
#endif //_DEBUG

    size_t m_ref_specSet;
};




// Implementation

inline void ColumnSubtableParent::UpdateFromParent()
{
    if (!m_array->UpdateFromParent()) return;
    m_subtable_map.update_from_parents();
}

inline Table* ColumnSubtableParent::get_subtable_ptr(std::size_t subtable_ndx) const
{
    assert(subtable_ndx < Size());

    Table *subtable = m_subtable_map.find(subtable_ndx);
    if (!subtable) {
        const std::size_t top_ref = GetAsRef(subtable_ndx);
        Allocator& alloc = GetAllocator();
        subtable = new Table(Table::SubtableTag(), alloc, top_ref,
                             const_cast<ColumnSubtableParent*>(this), subtable_ndx);
        const bool was_empty = m_subtable_map.empty();
        m_subtable_map.insert(subtable_ndx, subtable);
        if (was_empty && m_table) m_table->bind_ref();
    }
    return subtable;
}

inline Table* ColumnSubtableParent::get_subtable_ptr(std::size_t subtable_ndx,
                                                     std::size_t schema_ref) const
{
    assert(subtable_ndx < Size());

    Table *subtable = m_subtable_map.find(subtable_ndx);
    if (!subtable) {
        const std::size_t columns_ref = GetAsRef(subtable_ndx);
        Allocator& alloc = GetAllocator();
        subtable = new Table(Table::SubtableTag(), alloc, schema_ref, columns_ref,
                             const_cast<ColumnSubtableParent*>(this), subtable_ndx);
        const bool was_empty = m_subtable_map.empty();
        m_subtable_map.insert(subtable_ndx, subtable);
        if (was_empty && m_table) m_table->bind_ref();
    }
    return subtable;
}

inline ColumnSubtableParent::SubtableMap::~SubtableMap()
{
    if (m_indices.IsValid()) {
        assert(m_indices.is_empty());
        m_indices.Destroy();
        m_wrappers.Destroy();
    }
}

inline Table* ColumnSubtableParent::SubtableMap::find(size_t subtable_ndx) const
{
    if (!m_indices.IsValid()) return 0;
    size_t const pos = m_indices.find_first(subtable_ndx);
    return pos != size_t(-1) ? reinterpret_cast<Table *>(m_wrappers.Get(pos)) : 0;
}

inline void ColumnSubtableParent::SubtableMap::insert(size_t subtable_ndx, Table* wrapper)
{
    if (!m_indices.IsValid()) {
        m_indices.SetType(COLUMN_NORMAL);
        m_wrappers.SetType(COLUMN_NORMAL);
    }
    m_indices.add(subtable_ndx);
    m_wrappers.add(reinterpret_cast<unsigned long>(wrapper));
}

inline void ColumnSubtableParent::SubtableMap::remove(size_t subtable_ndx)
{
    assert(m_indices.IsValid());
    const size_t pos = m_indices.find_first(subtable_ndx);
    assert(pos != size_t(-1));
    m_indices.Delete(pos);
    m_wrappers.Delete(pos);
}

inline void ColumnSubtableParent::SubtableMap::update_from_parents()
{
    if (!m_indices.IsValid()) return;

    const size_t count = m_wrappers.Size();
    for (size_t i = 0; i < count; ++i) {
        Table* const t = reinterpret_cast<Table*>(m_wrappers.Get(i));
        t->UpdateFromParent();
    }
}

inline ColumnSubtableParent::ColumnSubtableParent(ArrayParent* parent_array, size_t parent_ndx,
                                                  Allocator& alloc, const Table* tab):
                            Column(COLUMN_HASREFS, parent_array, parent_ndx, alloc),
                            m_table(tab), m_subtable_map(GetDefaultAllocator()) {}

inline ColumnSubtableParent::ColumnSubtableParent(size_t ref, ArrayParent* parent_array, size_t parent_ndx,
                                                  Allocator& alloc, const Table* tab):
                            Column(ref, parent_array, parent_ndx, alloc),
                            m_table(tab), m_subtable_map(GetDefaultAllocator()) {}

inline void ColumnSubtableParent::update_child_ref(size_t subtable_ndx, size_t new_ref)
{
    Set(subtable_ndx, new_ref);
}

inline size_t ColumnSubtableParent::get_child_ref(size_t subtable_ndx) const
{
    return Get(subtable_ndx);
}


} // namespace tightdb

#endif // TIGHTDB_COLUMN_TABLE_HPP
