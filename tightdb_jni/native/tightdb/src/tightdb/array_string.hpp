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
#ifndef TIGHTDB_ARRAY_STRING_HPP
#define TIGHTDB_ARRAY_STRING_HPP

#include "array.hpp"

namespace tightdb {

class ArrayString : public Array {
public:
    ArrayString(ArrayParent *parent=NULL, size_t pndx=0, Allocator& alloc=GetDefaultAllocator());
    ArrayString(size_t ref, const ArrayParent *parent, size_t pndx, Allocator& alloc=GetDefaultAllocator());
    ArrayString(Allocator& alloc);
    ~ArrayString();

    const char* Get(size_t ndx) const;
    bool add();
    bool add(const char* value);
    bool Set(size_t ndx, const char* value);
    bool Set(size_t ndx, const char* value, size_t len);
    bool Insert(size_t ndx, const char* value);
    bool Insert(size_t ndx, const char* value, size_t len);
    void Delete(size_t ndx);

    size_t find_first(const char* value, size_t start=0 , size_t end=-1) const;
    void find_all(Array& result, const char* value, size_t add_offset = 0, size_t start = 0, size_t end = -1);

    /// Construct an empty string array and return just the reference
    /// to the underlying memory.
    ///
    /// \return Zero if allocation fails.
    ///
    static size_t create_empty_string_array(Allocator&);

#ifdef _DEBUG
    bool Compare(const ArrayString& c) const;
    void StringStats() const;
    //void ToDot(FILE* f) const;
    void ToDot(std::ostream& out, const char* title=NULL) const;
#endif //_DEBUG

private:
    size_t FindWithLen(const char* value, size_t len, size_t start , size_t end) const;
    virtual size_t CalcByteLen(size_t count, size_t width) const;
    virtual size_t CalcItemCount(size_t bytes, size_t width) const;
    virtual WidthType GetWidthType() const {return TDB_MULTIPLY;}
};





// Implementation:

inline size_t ArrayString::create_empty_string_array(Allocator& alloc)
{
    return create_empty_array(COLUMN_NORMAL, TDB_MULTIPLY, alloc);
}

inline ArrayString::ArrayString(ArrayParent *parent, size_t ndx_in_parent,
                                Allocator& alloc): Array(alloc)
{
    const size_t ref = create_empty_string_array(alloc);
    if (!ref) throw_error(ERROR_OUT_OF_MEMORY); // FIXME: Check that this exception is handled properly in callers
    init_from_ref(ref);
    SetParent(parent, ndx_in_parent);
    update_ref_in_parent();
}

inline ArrayString::ArrayString(size_t ref, const ArrayParent *parent, size_t ndx_in_parent,
                                Allocator& alloc): Array(alloc)
{
    // Manually create array as doing it in initializer list
    // will not be able to call correct virtual functions
    init_from_ref(ref);
    SetParent(const_cast<ArrayParent *>(parent), ndx_in_parent);
}

// Creates new array (but invalid, call UpdateRef to init)
inline ArrayString::ArrayString(Allocator& alloc): Array(alloc) {}

inline ArrayString::~ArrayString() {}


} // namespace tightdb

#endif // TIGHTDB_ARRAY_STRING_HPP
