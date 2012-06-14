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
#ifndef TIGHTDB_COLUMN_HPP
#define TIGHTDB_COLUMN_HPP

#include "array.hpp"

#ifdef _MSC_VER
#include <win32/stdint.h>
#else
#include <stdint.h> // unint8_t etc
#endif
//#include <climits> // size_t
#include <cstdlib> // size_t
#include <assert.h>

namespace tightdb {


// Pre-definitions
class Column;
class Index;

class ColumnBase {
public:
    virtual ~ColumnBase() {};

    virtual void SetHasRefs() {};

    virtual bool IsIntColumn() const {return false;}
    virtual bool IsStringColumn() const {return false;}
    virtual bool IsBinaryColumn() const {return false;}

    virtual size_t Size() const = 0;

    virtual bool add() = 0;
    virtual void insert(size_t ndx) = 0;
    virtual void Clear() = 0;
    virtual void Delete(size_t ndx) = 0;
    void Resize(size_t ndx) {m_array->Resize(ndx);}

    // Indexing
    virtual bool HasIndex() const = 0;
    //virtual Index& GetIndex() = 0;
    virtual void BuildIndex(Index& index) = 0;
    virtual void ClearIndex() = 0;
    virtual void SetIndexRef(size_t ref) { static_cast<void>(ref); }

    virtual size_t GetRef() const = 0;
    virtual void UpdateParentNdx(int diff) {m_array->UpdateParentNdx(diff);}
    virtual void UpdateFromParent() {m_array->UpdateFromParent();}

#ifdef _DEBUG
    virtual void Verify() const = 0; // Must be upper case to avoid conflict with macro in ObjC
    virtual void ToDot(std::ostream& out, const char* title=NULL) const;
#endif //_DEBUG

template<class C, class A> A* TreeGetArray(size_t start, size_t *first, size_t *last) const;
template<typename T, class C, class F> size_t TreeFind(T value, size_t start, size_t end) const;

protected:
    struct NodeChange {
        size_t ref1;
        size_t ref2;
        enum ChangeType {
            CT_ERROR,
            CT_NONE,
            CT_INSERT_BEFORE,
            CT_INSERT_AFTER,
            CT_SPLIT
        } type;
        NodeChange(ChangeType t, size_t r1=0, size_t r2=0) : ref1(r1), ref2(r2), type(t) {}
        NodeChange(bool success) : ref1(0), ref2(0), type(success ? CT_NONE : CT_ERROR) {}
    };

    // Tree functions
    template<typename T, class C> T TreeGet(size_t ndx) const;
    template<typename T, class C> bool TreeSet(size_t ndx, T value);
    template<typename T, class C> bool TreeInsert(size_t ndx, T value);
    template<typename T, class C> NodeChange DoInsert(size_t ndx, T value);
    template<typename T, class C> void TreeDelete(size_t ndx);
    template<typename T, class C> void TreeFindAll(Array &result, T value, size_t add_offset = 0, size_t start = 0, size_t end = -1) const;

    template<typename T, class C> void TreeVisitLeafs(size_t start, size_t end, size_t caller_offset, bool (*call)(T *arr, size_t start, size_t end, size_t caller_offset, void *state), void *state) const;


    template<typename T, class C, class S> size_t TreeWrite(S& out, size_t& pos) const;

    // Node functions
    bool IsNode() const {return m_array->IsNode();}
    const Array NodeGetOffsets() const;
    const Array NodeGetRefs() const;
    Array NodeGetOffsets();
    Array NodeGetRefs();
    template<class C> bool NodeInsert(size_t ndx, size_t ref);
    template<class C> bool NodeAdd(size_t ref);
    bool NodeUpdateOffsets(size_t ndx);
    template<class C> bool NodeInsertSplit(size_t ndx, size_t newRef);
    size_t GetRefSize(size_t ref) const;

#ifdef _DEBUG
    void ArrayToDot(std::ostream& out, const Array& array) const;
    virtual void LeafToDot(std::ostream& out, const Array& array) const;
#endif //_DEBUG

    // Member variables
    mutable Array* m_array;

    static std::size_t get_size_from_ref(std::size_t ref, Allocator&);
};

class Column : public ColumnBase {
public:
    Column(Allocator& alloc);
    Column(ColumnDef type, Allocator& alloc);
    Column(ColumnDef type=COLUMN_NORMAL, ArrayParent *parent=NULL, size_t pndx=0, Allocator& alloc=GetDefaultAllocator());
    Column(size_t ref, ArrayParent* parent=NULL, size_t pndx=0, Allocator& alloc=GetDefaultAllocator());
    Column(const Column& column);
    ~Column();

    void Destroy();

    bool IsIntColumn() const {return true;}

    bool operator==(const Column& column) const;

    void SetParent(ArrayParent *parent, size_t pndx);
    void UpdateParentNdx(int diff);
    void SetHasRefs();

    size_t Size() const;
    bool is_empty() const;

    // Getting and setting values
    int64_t Get(size_t ndx) const;
    size_t GetAsRef(size_t ndx) const;
    bool Set(size_t ndx, int64_t value);
    virtual void insert(size_t ndx) { Insert(ndx, 0); } // FIXME: Ignoring boolean return value here!
    bool Insert(size_t ndx, int64_t value);
    virtual bool add() {return add(0);}
    bool add(int64_t value);

    int64_t sum(size_t start = 0, size_t end = -1) const;
    int64_t maximum(size_t start = 0, size_t end = -1) const;
    int64_t minimum(size_t start = 0, size_t end = -1) const;
    void sort(size_t start, size_t end);
    void ReferenceSort(size_t start, size_t end, Column &ref);

    intptr_t GetPtr(size_t ndx) const {return (intptr_t)Get(ndx);}

    void Clear();
    void Delete(size_t ndx);
    //void Resize(size_t len);
    bool Reserve(size_t len, size_t width=8);

    bool Increment64(int64_t value, size_t start=0, size_t end=-1);
    size_t find_first(int64_t value, size_t start=0, size_t end=-1) const;

    void find_all(Array& result, int64_t value, size_t caller_offset=0, size_t start=0, size_t end=-1) const;
    void find_all_hamming(Array& result, uint64_t value, size_t maxdist, size_t offset=0) const;
    size_t find_pos(int64_t value) const;

    // Query support methods
    void LeafFindAll(Array &result, int64_t value, size_t add_offset, size_t start, size_t end) const;
    void GetBlock(size_t ndx, Array& arr, size_t& off) const {
        m_array->GetBlock(ndx, arr, off);
    }

    // Index
    bool HasIndex() const {return m_index != NULL;}
    Index& GetIndex();
    void BuildIndex(Index& index);
    void ClearIndex();
    size_t FindWithIndex(int64_t value) const;

    size_t GetRef() const {return m_array->GetRef();}
    Allocator& GetAllocator() const {return m_array->GetAllocator();}

    void sort();

    // Debug
#ifdef _DEBUG
    bool Compare(const Column& c) const;
    void Print() const;
    virtual void Verify() const;
    MemStats Stats() const;
#endif //_DEBUG

protected:
    friend class ColumnBase;
    void Create();
    void UpdateRef(size_t ref);

    // Node functions
    int64_t LeafGet(size_t ndx) const {return m_array->Get(ndx);}
    bool LeafSet(size_t ndx, int64_t value) {return m_array->Set(ndx, value);}
    bool LeafInsert(size_t ndx, int64_t value) {return m_array->Insert(ndx, value);}
    void LeafDelete(size_t ndx) {m_array->Delete(ndx);}

    template <class F>size_t LeafFind(int64_t value, size_t start, size_t end) const
    {
        return m_array->Query<F>(value, start, end);
    }

    void DoSort(size_t lo, size_t hi);

    // Member variables
    Index* m_index;

private:
    Column &operator=(Column const &); // not allowed
};


} // namespace tightdb

// Templates
#include "column_tpl.hpp"

#endif // TIGHTDB_COLUMN_HPP
