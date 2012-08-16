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
#ifndef TIGHTDB_ARRAY_STRING_LONG_HPP
#define TIGHTDB_ARRAY_STRING_LONG_HPP

#include <tightdb/array_blob.hpp>

namespace tightdb {


class ArrayStringLong : public Array {
public:
    ArrayStringLong(ArrayParent* parent=NULL, size_t pndx=0, Allocator& alloc=GetDefaultAllocator());
    ArrayStringLong(size_t ref, ArrayParent* parent, size_t pndx, Allocator& alloc=GetDefaultAllocator());
    //ArrayStringLong(Allocator& alloc);
    ~ArrayStringLong();

    bool is_empty() const;
    size_t Size() const;

    const char* Get(size_t ndx) const;
    void add(const char* value);
    void add(const char* value, size_t len);
    void Set(size_t ndx, const char* value);
    void Set(size_t ndx, const char* value, size_t len);
    void Insert(size_t ndx, const char* value);
    void Insert(size_t ndx, const char* value, size_t len);
    void Delete(size_t ndx);
    void Resize(size_t ndx);
    void Clear();

    size_t find_first(const char* value, size_t start=0 , size_t end=-1) const;
    void find_all(Array &result, const char* value, size_t add_offset = 0, size_t start = 0, size_t end = -1) const;

#ifdef TIGHTDB_DEBUG
    void ToDot(std::ostream& out, const char* title=NULL) const;
#endif // TIGHTDB_DEBUG

private:
    size_t FindWithLen(const char* value, size_t len, size_t start , size_t end) const;

    // Member variables
    Array m_offsets;
    ArrayBlob m_blob;
};


} // namespace tightdb

#endif // TIGHTDB_ARRAY_STRING_LONG_HPP
