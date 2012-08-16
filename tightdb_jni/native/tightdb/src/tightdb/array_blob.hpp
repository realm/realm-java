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
#ifndef TIGHTDB_ARRAY_BLOB_HPP
#define TIGHTDB_ARRAY_BLOB_HPP

#include <tightdb/array.hpp>

namespace tightdb {


class ArrayBlob : public Array {
public:
    ArrayBlob(ArrayParent *parent=NULL, size_t pndx=0, Allocator& alloc=GetDefaultAllocator());
    ArrayBlob(size_t ref, const ArrayParent *parent, size_t pndx, Allocator& alloc=GetDefaultAllocator());
    ArrayBlob(Allocator& alloc);
    ~ArrayBlob();

    const char* Get(size_t pos) const;

    void add(const char* data, size_t len);
    void Insert(size_t pos, const char* data, size_t len);
    void Replace(size_t start, size_t end, const char* data, size_t len);
    void Delete(size_t start, size_t end);
    void Resize(size_t len);
    void Clear();

#ifdef TIGHTDB_DEBUG
    void ToDot(std::ostream& out, const char* title=NULL) const;
#endif // TIGHTDB_DEBUG

private:
    virtual size_t CalcByteLen(size_t count, size_t width) const;
    virtual size_t CalcItemCount(size_t bytes, size_t width) const;
    virtual WidthType GetWidthType() const {return TDB_IGNORE;}
};


} // namespace tightdb

#endif // TIGHTDB_ARRAY_BLOB_HPP
