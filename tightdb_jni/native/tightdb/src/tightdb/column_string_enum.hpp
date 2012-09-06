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
#ifndef TIGHTDB_COLUMN_STRING_ENUM_HPP
#define TIGHTDB_COLUMN_STRING_ENUM_HPP

#include <tightdb/column_string.hpp>

namespace tightdb {

// Pre-declarations
class StringIndex;

class ColumnStringEnum : public Column {
public:
    ColumnStringEnum(size_t ref_keys, size_t ref_values, ArrayParent* parent=NULL, size_t pndx=0,
                     Allocator& alloc=GetDefaultAllocator());
    ~ColumnStringEnum();
    void Destroy();

    size_t Size() const;
    bool is_empty() const;

    const char* Get(size_t ndx) const;
    bool add(const char* value);
    bool Set(size_t ndx, const char* value);
    bool Insert(size_t ndx, const char* value);
    void Delete(size_t ndx);
    void Clear();

    size_t count(const char* value) const;
    size_t find_first(const char* value, size_t start=0, size_t end=-1) const;
    void find_all(Array& res, const char* value, size_t start=0, size_t end=-1) const;

    size_t count(size_t key_index) const;
    size_t find_first(size_t key_index, size_t start=0, size_t end=-1) const;
    void find_all(Array& res, size_t key_index, size_t start=0, size_t end=-1) const;

    void UpdateParentNdx(int diff);
    void UpdateFromParent();

    // Index
    bool HasIndex() const {return m_index != NULL;}
    const StringIndex& GetIndex() const {return *m_index;}
    StringIndex& CreateIndex();
    void SetIndexRef(size_t ref, ArrayParent* parent, size_t pndx);
    void ReuseIndex(StringIndex& index);
    void RemoveIndex() {m_index = NULL;}

    /// Compare two string enumeration columns for equality
    bool Compare(const ColumnStringEnum&) const;

#ifdef TIGHTDB_DEBUG
    void Verify() const; // Must be upper case to avoid conflict with macro in ObjC
    void ToDot(std::ostream& out, const char* title) const;
#endif // TIGHTDB_DEBUG

    size_t GetKeyNdx(const char* value) const;
    size_t GetKeyNdxOrAdd(const char* value);

private:

    // Member variables
    AdaptiveStringColumn m_keys;
    StringIndex* m_index;
};


} // namespace tightdb

#endif // TIGHTDB_COLUMN_STRING_ENUM_HPP
