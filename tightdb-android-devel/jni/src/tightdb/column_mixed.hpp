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
#ifndef TIGHTDB_COLUMN_MIXED_HPP
#define TIGHTDB_COLUMN_MIXED_HPP

#include "column.hpp"
#include "column_type.hpp"
#include "column_table.hpp"
#include "table.hpp"
#include "index.hpp"

namespace tightdb {


// Pre-declarations
class ColumnBinary;

class ColumnMixed : public ColumnBase {
public:
    /**
     * Create a freestanding mixed column.
     */
    ColumnMixed();

    /**
     * Create a mixed column and have it instantiate a new array
     * structure.
     *
     * \param tab If this column is used as part of a table you must
     * pass a pointer to that table. Otherwise you may pass null.
     */
    ColumnMixed(Allocator& alloc, const Table* tab);

    /**
     * Create a mixed column and attach it to an already existing
     * array structure.
     *
     * \param tab If this column is used as part of a table you must
     * pass a pointer to that table. Otherwise you may pass null.
     */
    ColumnMixed(size_t ref, ArrayParent* parent, size_t pndx, Allocator& alloc, const Table* tab);

    ~ColumnMixed();
    void Destroy();

    void SetParent(ArrayParent* parent, size_t pndx);
    void UpdateFromParent();

    ColumnType GetType(size_t ndx) const;
    size_t Size() const {return m_types->Size();}
    bool is_empty() const {return m_types->is_empty();}

    int64_t GetInt(size_t ndx) const;
    bool get_bool(size_t ndx) const;
    time_t get_date(size_t ndx) const;
    const char* get_string(size_t ndx) const;
    BinaryData get_binary(size_t ndx) const;

    /**
     * The returned size is zero if the specified row does not contain
     * a subtable.
     */
    size_t get_subtable_size(std::size_t row_idx) const;

    /**
     * Returns null if the specified row does not contain a subtable,
     * otherwise the returned table pointer must end up being wrapped
     * by an instance of BasicTableRef.
     */
    Table* get_subtable_ptr(std::size_t row_idx) const;

    void SetInt(size_t ndx, int64_t value);
    void set_bool(size_t ndx, bool value);
    void set_date(size_t ndx, time_t value);
    void set_string(size_t ndx, const char* value);
    void set_binary(size_t ndx, const char* value, size_t len);
    void SetTable(size_t ndx);

    void insert_int(size_t ndx, int64_t value);
    void insert_bool(size_t ndx, bool value);
    void insert_date(size_t ndx, time_t value);
    void insert_string(size_t ndx, const char* value);
    void insert_binary(size_t ndx, const char* value, size_t len);
    void insert_table(size_t ndx);

    virtual bool add() { insert_int(Size(), 0); return true; }
    virtual void insert(size_t ndx) { insert_int(ndx, 0); }
    void Clear();
    void Delete(size_t ndx);

    // Indexing
    bool HasIndex() const {return false;}
    void BuildIndex(Index& index) {(void)index;}
    void ClearIndex() {}

    size_t GetRef() const {return m_array->GetRef();}

#ifdef _DEBUG
    void Verify() const; // Must be upper case to avoid conflict with macro in ObjC
    void ToDot(std::ostream& out, const char* title) const;
#endif //_DEBUG

private:
    void Create(Allocator& alloc, const Table* tab);
    void Create(size_t ref, ArrayParent* parent, size_t pndx, Allocator& alloc, const Table* tab);
    void InitDataColumn();

    void ClearValue(size_t ndx, ColumnType newtype);

    class RefsColumn;

    // Member variables
    Column*       m_types;
    RefsColumn*   m_refs;
    ColumnBinary* m_data;
};


class ColumnMixed::RefsColumn: public ColumnSubtableParent
{
public:
    RefsColumn(Allocator& alloc, const Table* tab):
        ColumnSubtableParent(NULL, 0, alloc, tab) {}
    RefsColumn(std::size_t ref, ArrayParent* parent, std::size_t pndx,
               Allocator& alloc, const Table* tab):
        ColumnSubtableParent(ref, parent, pndx, alloc, tab) {}
    using ColumnSubtableParent::get_subtable_ptr;
    using ColumnSubtableParent::get_subtable;
};


inline ColumnMixed::ColumnMixed(): m_data(NULL)
{
    Create(GetDefaultAllocator(), 0);
}

inline ColumnMixed::ColumnMixed(Allocator& alloc, const Table* tab): m_data(NULL)
{
    Create(alloc, tab);
}

inline ColumnMixed::ColumnMixed(size_t ref, ArrayParent* parent, size_t pndx,
                                Allocator& alloc, const Table* tab): m_data(NULL)
{
    Create(ref, parent, pndx, alloc, tab);
}

inline size_t ColumnMixed::get_subtable_size(size_t row_idx) const
{
    // FIXME: If the table object is cached, it is possible to get the
    // size from it. Maybe it is faster in general to check for the
    // the presence of the cached object and use it when available.
    assert(row_idx < m_types->Size());
    if (m_types->Get(row_idx) != COLUMN_TYPE_TABLE) return 0;
    const size_t top_ref = m_refs->GetAsRef(row_idx);
    const size_t columns_ref = Array(top_ref, NULL, 0, m_refs->GetAllocator()).GetAsRef(1);
    const Array columns(columns_ref, NULL, 0, m_refs->GetAllocator());
    if (columns.is_empty()) return 0;
    const size_t first_col_ref = columns.GetAsRef(0);
    return get_size_from_ref(first_col_ref, m_refs->GetAllocator());
}

inline Table* ColumnMixed::get_subtable_ptr(size_t row_idx) const
{
    assert(row_idx < m_types->Size());
    if (m_types->Get(row_idx) != COLUMN_TYPE_TABLE) return 0;
    return m_refs->get_subtable_ptr(row_idx);
}


} // namespace tightdb

#endif // TIGHTDB_COLUMN_MIXED_HPP
