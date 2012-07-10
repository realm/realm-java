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
#ifndef TIGHTDB_COLUMN_STRING_HPP
#define TIGHTDB_COLUMN_STRING_HPP

#include "column.hpp"
#include "array_string.hpp"
#include "array_string_long.hpp"

namespace tightdb {


class AdaptiveStringColumn : public ColumnBase {
public:
    AdaptiveStringColumn(Allocator& alloc=GetDefaultAllocator());
    AdaptiveStringColumn(size_t ref, ArrayParent* parent=NULL, size_t pndx=0,
                         Allocator& alloc=GetDefaultAllocator());
    ~AdaptiveStringColumn();

    void Destroy();

    bool IsStringColumn() const {return true;}

    size_t Size() const;
    bool is_empty() const;

    const char* Get(size_t ndx) const;
    virtual bool add() {return add("");}
    bool add(const char* value);
    bool Set(size_t ndx, const char* value);
    virtual void insert(size_t ndx) { Insert(ndx, ""); } // FIXME: Ignoring boolean return value here!
    bool Insert(size_t ndx, const char* value);
    void Delete(size_t ndx);
    void Clear();
    void Resize(size_t ndx);

    size_t find_first(const char* value, size_t start=0 , size_t end=-1) const;
    void find_all(Array& result, const char* value, size_t start = 0, size_t end = -1) const;

    // Index
    bool HasIndex() const {return false;}
    void BuildIndex(Index&) {}
    void ClearIndex() {}
    size_t FindWithIndex(int64_t) const {return (size_t)-1;}

    size_t GetRef() const {return m_array->GetRef();}
    Allocator& GetAllocator() const {return m_array->GetAllocator();}
    void SetParent(ArrayParent* parent, size_t pndx) {m_array->SetParent(parent, pndx);}

    // Optimizing data layout
    bool AutoEnumerate(size_t& ref_keys, size_t& ref_values) const;

#ifdef _DEBUG
    bool Compare(const AdaptiveStringColumn& c) const;
    void Verify() const {}; // Must be upper case to avoid conflict with macro in ObjC
#endif //_DEBUG

protected:
    friend class ColumnBase;
    void UpdateRef(size_t ref);

    const char* LeafGet(size_t ndx) const;
    bool LeafSet(size_t ndx, const char* value);
    bool LeafInsert(size_t ndx, const char* value);
    template<class F> size_t LeafFind(const char* value, size_t start, size_t end) const;
    void LeafFindAll(Array& result, const char* value, size_t add_offset = 0, size_t start = 0, size_t end = -1) const;

    void LeafDelete(size_t ndx);

    bool IsLongStrings() const {return m_array->HasRefs();} // HasRefs indicates long string array

    bool FindKeyPos(const char* target, size_t& pos) const;

#ifdef _DEBUG
    virtual void LeafToDot(std::ostream& out, const Array& array) const;
#endif //_DEBUG
};


} // namespace tightdb

#endif // TIGHTDB_COLUMN_STRING_HPP
