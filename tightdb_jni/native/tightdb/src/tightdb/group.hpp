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

#ifndef TIGHTDB_GROUP_HPP
#define TIGHTDB_GROUP_HPP

#include "table.hpp"
#include "alloc_slab.hpp"

namespace tightdb {

// Pre-declarations
class SharedGroup;

enum GroupMode {
    GROUP_DEFAULT  =  0,
    GROUP_READONLY =  1,
    GROUP_SHARED   =  2,
    GROUP_APPEND   =  4,
    GROUP_ASYNC    =  8,
    GROUP_SWAPONLY = 16
};

class Group: private Table::Parent {
public:
    Group();
    Group(const char* filename, int mode=GROUP_DEFAULT);
    Group(const char* buffer, size_t len);
    ~Group();

    bool is_valid() const {return m_isValid;}
    bool is_shared() const {return (m_persistMode & GROUP_SHARED) != 0;}

    size_t get_table_count() const;
    const char* get_table_name(size_t table_ndx) const;
    bool has_table(const char* name) const;

    TableRef      get_table(const char* name);
    ConstTableRef get_table(const char* name) const;
    template<class T> BasicTableRef<T>       get_table(const char* name);
    template<class T> BasicTableRef<const T> get_table(const char* name) const;

    // Serialization
    bool write(const char* filepath);
    char* write_to_mem(size_t& len);

    bool commit();

    // Conversion
    template<class S> void to_json(S& out);

#ifdef _DEBUG
    void Verify(); // Must be upper case to avoid conflict with macro in ObjC
    void print() const;
    MemStats stats();
    void enable_mem_diagnostics(bool enable=true) {m_alloc.EnableDebug(enable);}
    void to_dot(std::ostream& out = std::cerr);
#endif //_DEBUG

protected:
    friend class GroupWriter;
    friend class SharedGroup;

    void init_shared();
    bool commit(size_t current_version, size_t readlock_version);

    SlabAlloc& get_allocator() {return m_alloc;}
    size_t get_free_space(size_t len, size_t& filesize, bool testOnly=false);
    Array& get_top_array() {return m_top;}

    // Recursively update all internal refs after commit
    void update_refs(size_t top_ref);

    void update_from_shared(size_t top_ref, size_t len);

    // Overriding method in ArrayParent
    virtual void update_child_ref(size_t subtable_ndx, size_t new_ref)
    {
        m_tables.Set(subtable_ndx, new_ref);
    }

    // Overriding method in Table::Parent
    virtual void child_destroyed(std::size_t) {} // Ignore

    // Overriding method in ArrayParent
    virtual size_t get_child_ref(size_t subtable_ndx) const
    {
        return m_tables.GetAsRef(subtable_ndx);
    }

    void create();
    void create_from_ref();

    Table& get_table(size_t ndx);

    template<class S> size_t write(S& out);

    // Member variables
    SlabAlloc m_alloc;
    Array m_top;
    Array m_tables;
    ArrayString m_tableNames;
    Array m_freePositions;
    Array m_freeLengths;
    Array m_freeVersions;
    Array m_cachedtables;
    uint32_t m_persistMode;
    size_t m_readlock_version;
    bool m_isValid;

private:
    friend class LangBindHelper;

    Table* get_table_ptr(const char* name);
    const Table* get_table_ptr(const char* name) const;
    template<class T> T* get_table_ptr(const char* name);
    template<class T> const T* get_table_ptr(const char* name) const;
};



// Implementation

inline TableRef Group::get_table(const char* name)
{
    return get_table_ptr(name)->get_table_ref();
}

inline ConstTableRef Group::get_table(const char* name) const
{
    return get_table_ptr(name)->get_table_ref();
}

template<class T> inline BasicTableRef<T> Group::get_table(const char* name)
{
    return get_table_ptr<T>(name)->get_table_ref();
}

template<class T> inline BasicTableRef<const T> Group::get_table(const char* name) const
{
    return get_table_ptr<T>(name)->get_table_ref();
}

inline const Table* Group::get_table_ptr(const char* name) const
{
    return const_cast<Group*>(this)->get_table_ptr(name);
}

template<class T> inline const T* Group::get_table_ptr(const char* name) const
{
    return const_cast<Group*>(this)->get_table_ptr<T>(name);
}

template<class T> T* Group::get_table_ptr(const char* name)
{
    const size_t n = m_tableNames.find_first(name);
    if (n == size_t(-1)) {
        // Create new table
        T* const t = new T(m_alloc);
        t->m_top.SetParent(this, m_tables.Size());

        const size_t ref = t->m_top.GetRef();
        m_tables.add(ref);
        m_tableNames.add(name);
        m_cachedtables.add(intptr_t(t));

        return t;
    }
    else {
        // Get table from cache if exists, else create
        return static_cast<T*>(&get_table(n));
    }
}

template<class S>
size_t Group::write(S& out)
{
    // Space for ref to top array
    out.write("\0\0\0\0\0\0\0\0", 8);

    // When serializing to disk we dont want
    // to include free space tracking as serialized
    // files are written without any free space.
    Array top(COLUMN_HASREFS, NULL, 0, m_alloc);
    top.add(m_top.Get(0));
    top.add(m_top.Get(1));

    // Recursively write all arrays
    const uint64_t topPos = top.Write(out);
    const size_t byte_size = out.getpos();

    // top ref
    out.seek(0);
    out.write((const char*)&topPos, 8);

    // Clean up temporary top
    top.Set(0, 0); // reset to avoid recursive delete
    top.Set(1, 0); // reset to avoid recursive delete
    top.Destroy();

    // return bytes written
    return byte_size;
}

template<class S>
void Group::to_json(S& out)
{
    out << "{";

    for (size_t i = 0; i < m_tables.Size(); ++i) {
        const char* const name = m_tableNames.Get(i);
        Table& table = get_table(i);

        if (i) out << ",";
        out << "\"" << name << "\"";
        out << ":";
        table.to_json(out);
    }

    out << "}";
}


} // namespace tightdb

#endif // TIGHTDB_GROUP_HPP
