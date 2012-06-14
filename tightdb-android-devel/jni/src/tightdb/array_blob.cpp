#include "array_blob.hpp"
#include <assert.h>

namespace tightdb {

ArrayBlob::ArrayBlob(ArrayParent *parent, size_t pndx, Allocator& alloc):
    Array(COLUMN_NORMAL, parent, pndx, alloc)
{
    // Manually set wtype as array constructor in initiatializer list
    // will not be able to call correct virtual function
    set_header_wtype(TDB_IGNORE);
}

ArrayBlob::ArrayBlob(size_t ref, const ArrayParent *parent, size_t pndx, Allocator& alloc):
    Array(alloc)
{
    // Manually create array as doing it in initializer list
    // will not be able to call correct virtual functions
    Create(ref);
    SetParent(const_cast<ArrayParent *>(parent), pndx);
}

// Creates new array (but invalid, call UpdateRef to init)
ArrayBlob::ArrayBlob(Allocator& alloc) : Array(alloc) {}

ArrayBlob::~ArrayBlob() {}

const char* ArrayBlob::Get(size_t pos) const
{
    return reinterpret_cast<const char*>(m_data) + pos;
}

void ArrayBlob::add(const char* data, size_t len)
{
    Replace(m_len, m_len, data, len);
}

void ArrayBlob::Insert(size_t pos, const char* data, size_t len)
{
    Replace(pos, pos, data, len);
}

void ArrayBlob::Replace(size_t start, size_t end, const char* data, size_t len)
{
    assert(start <= end);
    assert(end <= m_len);
    assert(len == 0 || data);

    CopyOnWrite();

    // Reallocate if needed
    const size_t gapsize = end - start;
    const size_t newsize = (m_len - gapsize) + len;
    Alloc(newsize, 1); // also updates header

    // Resize previous space to fit new data
    // (not needed if we append to end)
    if (start != m_len && gapsize != len) {
        const size_t dst = start + len;
        const size_t src_len = m_len - end;
        memmove(m_data + dst, m_data + end, src_len);
    }

    // Insert the data
    memcpy(m_data + start, data, len);

    m_len = newsize;
}

void ArrayBlob::Delete(size_t start, size_t end)
{
    Replace(start, end, NULL, 0);
}

void ArrayBlob::Resize(size_t len)
{
    assert(len <= m_len);
    Replace(len, m_len, NULL, 0);
}

void ArrayBlob::Clear()
{
    Replace(0, m_len, NULL, 0);
}

size_t ArrayBlob::CalcByteLen(size_t count, size_t) const
{
    return 8 + count; // include room for header
}

size_t ArrayBlob::CalcItemCount(size_t bytes, size_t) const
{
    return bytes - 8;
}

#ifdef _DEBUG

void ArrayBlob::ToDot(std::ostream& out, const char* title) const
{
    const size_t ref = GetRef();

    if (title) {
        out << "subgraph cluster_" << ref << " {" << std::endl;
        out << " label = \"" << title << "\";" << std::endl;
        out << " color = white;" << std::endl;
    }

    out << "n" << std::hex << ref << std::dec << "[shape=none,label=<";
    out << "<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"4\"><TR>" << std::endl;

    // Header
    out << "<TD BGCOLOR=\"lightgrey\"><FONT POINT-SIZE=\"7\"> ";
    out << "0x" << std::hex << ref << std::dec << "<BR/>";
    out << "</FONT></TD>" << std::endl;

    // Values
    out << "<TD>";
    out << Size() << " bytes"; //TODO: write content
    out << "</TD>" << std::endl;

    out << "</TR></TABLE>>];" << std::endl;
    if (title) out << "}" << std::endl;

    out << std::endl;
}

#endif //_DEBUG

}
