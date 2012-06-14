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
#ifndef TIGHTDB_ARRAY_HPP
#define TIGHTDB_ARRAY_HPP

#ifdef _MSC_VER
#include <win32/stdint.h>
#else
#include <stdint.h> // unint8_t etc
#endif
#include <cstdlib> // size_t
#include <cstring> // memmove
#include "alloc.hpp"
#include <iostream>
#include "utilities.hpp"
#include <vector>
#include <string>
#include <cassert>

#define TEMPEX(fun, arg) \
    if(m_width == 0) {fun<0> arg;} \
    else if (m_width == 1) {fun<1> arg;} \
    else if (m_width == 2) {fun<2> arg;} \
    else if (m_width == 4) {fun<4> arg;} \
    else if (m_width == 8) {fun<8> arg;} \
    else if (m_width == 16) {fun<16> arg;} \
    else if (m_width == 32) {fun<32> arg;} \
    else if (m_width == 64) {fun<64> arg;}

#ifdef USE_SSE42
/*
    MMX: mmintrin.h
    SSE: xmmintrin.h
    SSE2: emmintrin.h
    SSE3: pmmintrin.h
    SSSE3: tmmintrin.h
    SSE4A: ammintrin.h
    SSE4.1: smmintrin.h
    SSE4.2: nmmintrin.h
*/
    #include <nmmintrin.h> // __SSE42__
#elif defined (USE_SSE3)
    #include <pmmintrin.h> // __SSE3__
#endif

#ifdef _DEBUG
#include <stdio.h>
#endif

using namespace std;

namespace tightdb {

static const size_t not_found = (size_t)-1;

// Pre-definitions
class Array;

#ifdef _DEBUG
class MemStats {
public:
    MemStats() : allocated(0), used(0), array_count(0) {}
    MemStats(size_t allocated, size_t used, size_t array_count)
    : allocated(allocated), used(used), array_count(array_count) {}
    MemStats(const MemStats& m)
    {
        allocated = m.allocated;
        used = m.used;
        array_count = m.array_count;
    }
    void add(const MemStats& m)
    {
        allocated += m.allocated;
        used += m.used;
        array_count += m.array_count;
    }
    size_t allocated;
    size_t used;
    size_t array_count;
};
#endif

enum ColumnDef {
    COLUMN_NORMAL,
    COLUMN_NODE,
    COLUMN_HASREFS
};


class ArrayParent
{
public:
    virtual ~ArrayParent() {}

    // FIXME: Must be protected. Solve problem by having the Array constructor, that creates a new array, call it.
    virtual void update_child_ref(size_t subtable_ndx, size_t new_ref) = 0;

protected:
    friend class Array;

    virtual size_t get_child_ref(size_t subtable_ndx) const = 0;
};


/**
 * An Array can be copied, but it will leave the source in a truncated
 * (and therfore unusable) state.
 *
 * \note The parent information in an array ('pointer to parent' and
 * 'index in parent') may be valid even when the array is not valid,
 * that is IsValid() returns false.
 *
 * FIXME: Array should be endowed with proper copy and move semantics like TableView is.
 */
class Array: public ArrayParent {
public:
    Array(size_t ref, ArrayParent* parent=NULL, size_t pndx=0, Allocator& alloc=GetDefaultAllocator());
    Array(ColumnDef type=COLUMN_NORMAL, ArrayParent* parent=NULL, size_t pndx=0, Allocator& alloc=GetDefaultAllocator());
    Array(Allocator& alloc); ///< Create an array in the invalid state (a null array).
    Array(const Array& a); // FIXME: This is a moving copy and therfore it compromises constness.
    virtual ~Array();

    bool operator==(const Array& a) const;

    void SetType(ColumnDef type);
    void UpdateRef(size_t ref);
    bool Copy(const Array&); // Copy semantics for assignment
    void move_assign(Array&); // Move semantics for assignment

    // Parent tracking
    bool HasParent() const {return m_parent != NULL;}
    void SetParent(ArrayParent *parent, size_t pndx);
    void UpdateParentNdx(int diff) {m_parentNdx += diff;}
    ArrayParent *GetParent() const {return m_parent;}
    size_t GetParentNdx() const {return m_parentNdx;}
    bool UpdateFromParent();

    bool IsValid() const {return m_data != NULL;}
    void Invalidate() const {m_data = NULL;}

    virtual size_t Size() const {return m_len;}
    bool is_empty() const {return m_len == 0;}

    bool Insert(size_t ndx, int64_t value);
    bool add(int64_t value);
    bool Set(size_t ndx, int64_t value);
    template<size_t w> void Set(size_t ndx, int64_t value);
    int64_t Get(size_t ndx) const;
    size_t GetAsRef(size_t ndx) const;
    template<size_t w> int64_t Get(size_t ndx) const;
    int64_t operator[](size_t ndx) const {return Get(ndx);}
    int64_t back() const;
    void Delete(size_t ndx);
    void Clear();

    // Direct access methods
    void GetBlock(size_t ndx, Array& arr, size_t& off) const;
    int64_t ColumnGet(size_t ndx) const;
    const char* ColumnStringGet(size_t ndx) const;
    size_t ColumnFind(int64_t target, size_t ref, Array& cache) const;

    void SetAllToZero();
    bool Increment(int64_t value, size_t start=0, size_t end=(size_t)-1);
    bool IncrementIf(int64_t limit, int64_t value);
    void Adjust(size_t start, int64_t diff);

    size_t FindPos(int64_t value) const;
    size_t FindPos2(int64_t value) const;
    size_t find_first(int64_t value, size_t start=0, size_t end=(size_t)-1) const;

    template <class F> size_t find_first(F function_, int64_t value, size_t start, size_t end) const
    {
        static_cast<void>(function_); // FIXME: Why is this parameter never used?
        const F function = {};
        if(end == (size_t)-1)
            end = m_len;
        for(size_t s = start; s < end; s++) {
            if(function(value, Get(s)))
                return s;
        }
        return (size_t)-1;
    }
    void Preset(int64_t min, int64_t max, size_t count);
    void Preset(size_t bitwidth, size_t count);
    void find_all(Array& result, int64_t value, size_t offset=0, size_t start=0, size_t end=(size_t)-1) const;
    void FindAllHamming(Array& result, uint64_t value, size_t maxdist, size_t offset=0) const;
    int64_t sum(size_t start = 0, size_t end = (size_t)-1) const;
    bool maximum(int64_t& result, size_t start = 0, size_t end = (size_t)-1) const;
    bool minimum(int64_t& result, size_t start = 0, size_t end = (size_t)-1) const;
    template <class F> size_t Query(int64_t value, size_t start, size_t end);

    void sort(void);
    void ReferenceSort(Array &ref);
    void Resize(size_t count);

    bool IsNode() const {return m_isNode;}
    bool HasRefs() const {return m_hasRefs;}
    Array GetSubArray(size_t ndx);
    const Array GetSubArray(size_t ndx) const;
    size_t GetRef() const {return m_ref;};
    void Destroy();

    Allocator& GetAllocator() const {return m_alloc;}

    // Serialization
    template<class S> size_t Write(S& target, bool recurse=true, bool persist=false) const;
    template<class S> void WriteAt(size_t pos, S& out) const;
    size_t GetByteSize(bool align=false) const;
    vector<int64_t> ToVector(void) const;

    // Debug
    size_t GetBitWidth() const {return m_width;}
#ifdef _DEBUG
    bool Compare(const Array& c) const;
    void Print() const;
    void Verify() const;
    void ToDot(ostream& out, const char* title=NULL) const;
    void Stats(MemStats& stats) const;
#endif //_DEBUG
    mutable unsigned char* m_data; // FIXME: Should be 'char' not 'unsigned char'

private:
    template <size_t w>bool MinMax(size_t from, size_t to, uint64_t maxdiff, int64_t *min, int64_t *max);
    Array& operator=(const Array&) {return *this;} // not allowed
    template<size_t w> void QuickSort(size_t lo, size_t hi);
    void QuickSort(size_t lo, size_t hi);
    void ReferenceQuickSort(Array &ref);
    template<size_t w> void ReferenceQuickSort(size_t lo, size_t hi, Array &ref);
#if defined(USE_SSE42) || defined(USE_SSE3)
    size_t FindSSE(int64_t value, __m128i *data, size_t bytewidth, size_t items) const;
#endif //USE_SSE
    template <bool eq>size_t CompareEquality(int64_t value, size_t start, size_t end) const;
    template <bool gt>size_t CompareRelation(int64_t value, size_t start, size_t end) const;
    template <size_t w> void sort();
    template <size_t w>void ReferenceSort(Array &ref);
    void update_ref_in_parent(size_t ref);

protected:
    bool AddPositiveLocal(int64_t value);

    void Create(size_t ref);
    void CreateFromHeader(uint8_t* header, size_t ref=0);
    void CreateFromHeaderDirect(uint8_t* header, size_t ref=0);

    // Getters and Setters for adaptive-packed arrays
    typedef int64_t(Array::*Getter)(size_t) const;
    typedef void(Array::*Setter)(size_t, int64_t);
    int64_t Get_0b(size_t ndx) const;
    int64_t Get_1b(size_t ndx) const;
    int64_t Get_2b(size_t ndx) const;
    int64_t Get_4b(size_t ndx) const;
    int64_t Get_8b(size_t ndx) const;
    int64_t Get_16b(size_t ndx) const;
    int64_t Get_32b(size_t ndx) const;
    int64_t Get_64b(size_t ndx) const;
    void Set_0b(size_t ndx, int64_t value);
    void Set_1b(size_t ndx, int64_t value);
    void Set_2b(size_t ndx, int64_t value);
    void Set_4b(size_t ndx, int64_t value);
    void Set_8b(size_t ndx, int64_t value);
    void Set_16b(size_t ndx, int64_t value);
    void Set_32b(size_t ndx, int64_t value);
    void Set_64b(size_t ndx, int64_t value);

    enum WidthType {
        TDB_BITS     = 0,
        TDB_MULTIPLY = 1,
        TDB_IGNORE   = 2
    };

    virtual size_t CalcByteLen(size_t count, size_t width) const;
    virtual size_t CalcItemCount(size_t bytes, size_t width) const;
    virtual WidthType GetWidthType() const {return TDB_BITS;}

    void set_header_isnode(bool value, void* header=NULL);
    void set_header_hasrefs(bool value, void* header=NULL);
    void set_header_wtype(WidthType value, void* header=NULL);
    void set_header_width(size_t value, void* header=NULL);
    void set_header_len(size_t value, void* header=NULL);
    void set_header_capacity(size_t value, void* header=NULL);
    bool get_header_isnode(const void* header=NULL) const;
    bool get_header_hasrefs(const void* header=NULL) const;
    WidthType get_header_wtype(const void* header=NULL) const;
    size_t get_header_width(const void* header=NULL) const;
    size_t get_header_len(const void* header=NULL) const;
    size_t get_header_capacity(const void* header=NULL) const;

    void SetWidth(size_t width);
    bool Alloc(size_t count, size_t width);
    bool CopyOnWrite();

    // Member variables
    Getter m_getter;
    Setter m_setter;

private:
    size_t m_ref;

protected:
    size_t m_len;
    size_t m_capacity;
    size_t m_width;
    bool m_isNode;
    bool m_hasRefs;

private:
    ArrayParent *m_parent;
    size_t m_parentNdx;

    Allocator& m_alloc;

protected:
    int64_t m_lbound;
    int64_t m_ubound;

    // Overriding methods in ArrayParent
    virtual void update_child_ref(size_t subtable_ndx, size_t new_ref);
    virtual size_t get_child_ref(size_t subtable_ndx) const;
};




// Implementation:

template<class S> size_t Array::Write(S& out, bool recurse, bool persist) const
{
    assert(IsValid());

    // Ignore un-changed arrays when persisting
    if (persist && m_alloc.IsReadOnly(m_ref)) return m_ref;

    if (recurse && m_hasRefs) {
        // Temp array for updated refs
        Array newRefs(m_isNode ? COLUMN_NODE : COLUMN_HASREFS);

        // First write out all sub-arrays
        for (size_t i = 0; i < Size(); ++i) {
            const size_t ref = GetAsRef(i);
            if (ref == 0 || ref & 0x1) {
                // zero-refs and refs that are not 64-aligned do not point to sub-trees
                newRefs.add(ref);
            }
            else if (persist && m_alloc.IsReadOnly(ref)) {
                // Ignore un-changed arrays when persisting
                newRefs.add(ref);
            }
            else {
                const Array sub(ref, NULL, 0, GetAllocator());
                const size_t sub_pos = sub.Write(out, true, persist);
                newRefs.add(sub_pos);
            }
        }

        // Write out the replacement array
        // (but don't write sub-tree as it has alredy been written)
        const size_t refs_pos = newRefs.Write(out, false, persist);

        // Clean-up
        newRefs.SetType(COLUMN_NORMAL); // avoid recursive del
        newRefs.Destroy();

        return refs_pos; // Return position
    }

    // parse header
    size_t len          = get_header_len();
    const WidthType wt  = get_header_wtype();
    int bits_in_partial_byte = 0;

    // Adjust length to number of bytes
    if (wt == TDB_BITS) {
        const size_t bits = (len * m_width);
        len = bits / 8;
        bits_in_partial_byte = bits & 0x7;
    }
    else if (wt == TDB_MULTIPLY) {
        len *= m_width;
    }

    // TODO: replace capacity with checksum

    // Calculate complete size
    len += 8; // include header in total

    // Write array
    const char* const data = reinterpret_cast<const char*>(m_data-8);
    const size_t array_pos = out.write(data, len);

    // Write last partial byte. Note: Since the memory is not
    // explicitely cleared initially, we cannot rely on the unused
    // bits to be in a well defined state here.
    if (bits_in_partial_byte != 0) {
        char_traits<char>::int_type i = char_traits<char>::to_int_type(data[len]);
        i &= (1 << bits_in_partial_byte) - 1;
        char b = char_traits<char>::to_char_type(i);
        out.write(&b, 1);
        ++len;
    }

    // Add padding for 64bit alignment
    const size_t rest = (~len & 0x7)+1;
    if (rest < 8)
        out.write("\0\0\0\0\0\0\0\0", rest);

    return array_pos; // Return position of this array
}

template<class S> void Array::WriteAt(size_t pos, S& out) const
{
    assert(IsValid());

    // parse header
    size_t len          = get_header_len();
    const WidthType wt  = get_header_wtype();
    int bits_in_partial_byte = 0;

    // Adjust length to number of bytes
    if (wt == TDB_BITS) {
        const size_t bits = (len * m_width);
        len = bits / 8;
        bits_in_partial_byte = bits & 0x7;
    }
    else if (wt == TDB_MULTIPLY) {
        len *= m_width;
    }

    // TODO: replace capacity with checksum

    // Calculate complete size
    len += 8; // include header in total

    // Write array
    const char* const data = reinterpret_cast<const char*>(m_data-8);
    out.WriteAt(pos, data, len);

    // Write last partial byte. Note: Since the memory is not
    // explicitely cleared initially, we cannot rely on the unused
    // bits to be in a well defined state here.
    if (bits_in_partial_byte != 0) {
        char_traits<char>::int_type i = char_traits<char>::to_int_type(data[len]);
        i &= (1 << bits_in_partial_byte) - 1;
        char b = char_traits<char>::to_char_type(i);
        out.WriteAt(pos+len, &b, 1);
        ++len;
    }

    // Add padding for 64bit alignment
    const size_t rest = (~len & 0x7)+1;
    if (rest < 8)
        out.WriteAt(pos+len, "\0\0\0\0\0\0\0\0", rest);
}

inline void Array::move_assign(Array& a)
{
    // FIXME: It will likely be a lot better for the optimizer if we
    // did a member-wise copy, rather than recreating the state from
    // the referenced data. This is important because TableView, for
    // example, relies on long chains of moves to be optimized away
    // completely. This change should be a 'no-brainer'.
    Destroy();
    UpdateRef(a.GetRef());
    a.Invalidate();
}

inline void Array::update_ref_in_parent(size_t ref)
{
  if (!m_parent) return;
  m_parent->update_child_ref(m_parentNdx, ref);
}


inline void Array::update_child_ref(size_t subtable_ndx, size_t new_ref)
{
    Set(subtable_ndx, new_ref);
}

inline size_t Array::get_child_ref(size_t subtable_ndx) const
{
    return GetAsRef(subtable_ndx);
}


} // namespace tightdb

#endif // TIGHTDB_ARRAY_HPP
