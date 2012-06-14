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
#ifndef TIGHTDB_COLUMN_BINARY_HPP
#define TIGHTDB_COLUMN_BINARY_HPP

#include "column.hpp"
#include "binary_data.hpp"
#include "array_binary.hpp"

namespace tightdb {


class ColumnBinary : public ColumnBase {
public:
    ColumnBinary(Allocator& alloc=GetDefaultAllocator());
    ColumnBinary(size_t ref, ArrayParent* parent=NULL, size_t pndx=0, Allocator& alloc=GetDefaultAllocator());
    ~ColumnBinary();

    void Destroy();

    bool IsBinaryColumn() const {return true;}

    size_t Size() const;
    bool is_empty() const;

    BinaryData Get(size_t ndx) const;
    const char* GetData(size_t ndx) const;
    size_t GetLen(size_t ndx) const;

    virtual bool add() { add(NULL, 0); return true; }
    void add(const char* value, size_t len);
    void Set(size_t ndx, const char* value, size_t len);
    virtual void insert(size_t ndx) { Insert(ndx, 0, 0); }
    void Insert(size_t ndx, const char* value, size_t len);
    void Delete(size_t ndx);
    void Resize(size_t ndx);
    void Clear();

    // Index
    bool HasIndex() const {return false;}
    void BuildIndex(Index&) {}
    void ClearIndex() {}
    size_t FindWithIndex(int64_t) const {return (size_t)-1;}

    size_t GetRef() const {return m_array->GetRef();}
    void SetParent(ArrayParent *parent, size_t pndx) {m_array->SetParent(parent, pndx);}
    void UpdateParentNdx(int diff) {m_array->UpdateParentNdx(diff);}

#ifdef _DEBUG
    void Verify() const {}; // Must be upper case to avoid conflict with macro in ObjC
#endif //_DEBUG

protected:
    friend class ColumnBase;

    bool add(BinaryData bin);
    bool Set(size_t ndx, BinaryData bin);
    bool Insert(size_t ndx, BinaryData bin);

    void UpdateRef(size_t ref);

    BinaryData LeafGet(size_t ndx) const;
    bool LeafSet(size_t ndx, BinaryData value);
    bool LeafInsert(size_t ndx, BinaryData value);
    void LeafDelete(size_t ndx);

#ifdef _DEBUG
    virtual void LeafToDot(std::ostream& out, const Array& array) const;
#endif //_DEBUG
};


} // namespace tightdb

#endif // TIGHTDB_COLUMN_BINARY_HPP
