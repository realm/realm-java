#include <cstdlib>
#include <cassert>
#include <cstring>
#include <cstdio> // debug
#include <iostream>
#include "utilities.hpp"
#include "column.hpp"
#include "array_string.hpp"

using namespace std;

namespace {

size_t round_up(size_t len)
{
    size_t width = 0;
    if (len == 0)     width = 0;
    else if (len < 3) width = 4;
    else {
        width = len;
        width |= width >> 1;
        width |= width >> 2;
        width |= width >> 4;
        ++width;
    }
    return width;
}

}


namespace tightdb {

ArrayString::ArrayString(ArrayParent *parent, size_t pndx, Allocator& alloc):
    Array(COLUMN_NORMAL, parent, pndx, alloc)
{
    // Manually set wtype as array constructor in initiatializer list
    // will not be able to call correct virtual function
    set_header_wtype(TDB_MULTIPLY);
}

ArrayString::ArrayString(size_t ref, const ArrayParent *parent, size_t pndx, Allocator& alloc):
    Array(alloc)
{
    // Manually create array as doing it in initializer list
    // will not be able to call correct virtual functions
    Create(ref);
    SetParent(const_cast<ArrayParent *>(parent), pndx);
}

// Creates new array (but invalid, call UpdateRef to init)
ArrayString::ArrayString(Allocator& alloc): Array(alloc) {}


ArrayString::~ArrayString() {}

const char* ArrayString::Get(size_t ndx) const
{
    assert(ndx < m_len);

    if (m_width == 0) return "";
    else return (const char*)(m_data + (ndx * m_width));
}

bool ArrayString::Set(size_t ndx, const char* value)
{
    assert(ndx < m_len);
    assert(value);

    return Set(ndx, value, strlen(value));
}

bool ArrayString::Set(size_t ndx, const char* value, size_t len)
{
    assert(ndx < m_len);
    assert(value);
    assert(len < 64); // otherwise we have to use another column type

    // Check if we need to copy before modifying
    if (!CopyOnWrite()) return false;

    // Calc min column width (incl trailing zero-byte)
    size_t width = ::round_up(len);

    // Make room for the new value
    if (width > m_width) {
        const size_t oldwidth = m_width;
        if (!Alloc(m_len, width)) return false;
        m_width = width;

        // Expand the old values
        int k = (int)m_len;
        while (--k >= 0) {
            const char* v = (const char*)m_data + (k * oldwidth);

            // Move the value
            char* data = (char*)m_data + (k * m_width);
            char* const end = data + m_width;
            memmove(data, v, oldwidth);
            for (data += oldwidth; data < end; ++data) {
                *data = '\0'; // pad with zeroes
            }
        }
    }

    // Set the value
    char* data = (char*)m_data + (ndx * m_width);
    char* const end = data + m_width;
    memmove(data, value, len);
    for (data += len; data < end; ++data) {
        *data = '\0'; // pad with zeroes
    }

    return true;
}

bool ArrayString::add()
{
    return Insert(m_len, "", 0);
}

bool ArrayString::add(const char* value)
{
    return Insert(m_len, value, strlen(value));
}

bool ArrayString::Insert(size_t ndx, const char* value)
{
    return Insert(ndx, value, strlen(value));
}



bool ArrayString::Insert(size_t ndx, const char* value, size_t len)
{
    assert(ndx <= m_len);
    assert(value);
    assert(len < 64); // otherwise we have to use another column type

    // Check if we need to copy before modifying
    if (!CopyOnWrite()) return false;

    // Calc min column width (incl trailing zero-byte)
    size_t width = ::round_up(len);

    const bool doExpand = width > m_width;

    // Make room for the new value
    const size_t oldwidth = m_width;
    if (!Alloc(m_len+1, doExpand ? width : m_width)) return false;
    if (doExpand) m_width = width;

    // Move values below insertion (may expand)
    if (doExpand) {
        // Expand the old values
        int k = (int)m_len;
        while (--k >= (int)ndx) {
            const char* v = (const char*)m_data + (k * oldwidth);

            // Move the value
            char* data = (char*)m_data + ((k+1) * m_width);
            char* const end = data + m_width;
            memmove(data, v, oldwidth);
            for (data += oldwidth; data < end; ++data) {
                *data = '\0'; // pad with zeroes
            }
        }
    }
    else if (ndx != m_len) {
        // when no expansion, use memmove
        unsigned char* src = m_data + (ndx * m_width);
        unsigned char* dst = src + m_width;
        const size_t count = (m_len - ndx) * m_width;
        memmove(dst, src, count);
    }

    // Set the value
    char* data = (char*)m_data + (ndx * m_width);
    memcpy(data, value, len);

    // Pad with zeroes
    char* const end = data + m_width;
    for (data += len; data < end; ++data) {
        *data = '\0';
    }

    // Expand values above insertion
    if (doExpand) {
        int k = (int)ndx;
        while (--k >= 0) {
            const char* v = (const char*)m_data + (k * oldwidth);

            // Move the value
            char* data = (char*)m_data + (k * m_width);
            char* const end = data + m_width;
            memmove(data, v, oldwidth);
            for (data += oldwidth; data < end; ++data) {
                *data = '\0'; // pad with zeroes
            }
        }
    }

    ++m_len;
    return true;
}

void ArrayString::Delete(size_t ndx)
{
    assert(ndx < m_len);

    // Check if we need to copy before modifying
    CopyOnWrite();

    --m_len;

    // move data under deletion up
    if (ndx < m_len) {
        char* src = (char*)m_data + ((ndx+1) * m_width);
        char* dst = (char*)m_data + (ndx * m_width);
        const size_t len = (m_len - ndx) * m_width;
        memmove(dst, src, len);
    }

    // Update length in header
    set_header_len(m_len);
}

size_t ArrayString::CalcByteLen(size_t count, size_t width) const
{
    return 8 + (count * width);
}

size_t ArrayString::CalcItemCount(size_t bytes, size_t width) const
{
    if (width == 0) return (size_t)-1; // zero-width gives infinite space

    const size_t bytes_without_header = bytes - 8;
    return bytes_without_header / width;
}

size_t ArrayString::find_first(const char* value, size_t start, size_t end) const
{
    assert(value);
    return FindWithLen(value, strlen(value), start, end);
}

void ArrayString::find_all(Array& result, const char* value, size_t add_offset, size_t start, size_t end)
{
    assert(value);

    const size_t len = strlen(value);

    size_t first = start - 1;
    for (;;) {
        first = FindWithLen(value, len, first + 1, end);
        if (first != (size_t)-1)
            result.add(first + add_offset);
        else break;
    }
}

size_t ArrayString::FindWithLen(const char* value, size_t len, size_t start, size_t end) const
{
    assert(value);

    if (end == (size_t)-1) end = m_len;
    if (start == end) return (size_t)-1;
    assert(start < m_len && end <= m_len && start < end);
    if (m_len == 0) return (size_t)-1; // empty list
    if (len >= m_width) return (size_t)-1; // A string can never be wider than the column width

    // todo, ensure behaves as expected when m_width = 0

    for (size_t i = start; i < end; ++i) {
        if (value[0] == (char)m_data[i * m_width] && value[len] == (char)m_data[i * m_width + len]) {
            const char* const v = (const char *)m_data + i * m_width;
            if (strncmp(value, v, len) == 0) return i;
        }
    }

    return (size_t)-1; // not found
}

#ifdef _DEBUG
#include "stdio.h"

bool ArrayString::Compare(const ArrayString& c) const
{
    if (c.Size() != Size()) return false;

    for (size_t i = 0; i < Size(); ++i) {
        if (strcmp(Get(i), c.Get(i)) != 0) return false;
    }

    return true;
}

void ArrayString::StringStats() const
{
    size_t total = 0;
    size_t longest = 0;

    for (size_t i = 0; i < m_len; ++i) {
        const char* str = Get(i);
        const size_t len = strlen(str)+1;

        total += len;
        if (len > longest) longest = len;
    }

    const size_t size = m_len * m_width;
    const size_t zeroes = size - total;
    const size_t zavg = zeroes / (m_len ? m_len : 1); // avoid possible div by zero

    cout << "Count: " << m_len << "\n";
    cout << "Width: " << m_width << "\n";
    cout << "Total: " << size << "\n";
    cout << "Capacity: " << m_capacity << "\n\n";
    cout << "Bytes string: " << total << "\n";
    cout << "     longest: " << longest << "\n";
    cout << "Bytes zeroes: " << zeroes << "\n";
    cout << "         avg: " << zavg << "\n";
}

/*
void ArrayString::ToDot(FILE* f) const
{
    const size_t ref = GetRef();

    fprintf(f, "n%zx [label=\"", ref);

    for (size_t i = 0; i < m_len; ++i) {
        if (i > 0) fprintf(f, " | ");

        fprintf(f, "%s", Get(i));
    }

    fprintf(f, "\"];\n");
}
*/

void ArrayString::ToDot(std::ostream& out, const char* title) const
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
    out << "<TD BGCOLOR=\"lightgrey\"><FONT POINT-SIZE=\"7\">";
    out << "0x" << std::hex << ref << std::dec << "</FONT></TD>" << std::endl;

    for (size_t i = 0; i < m_len; ++i) {
        out << "<TD>\"" << Get(i) << "\"</TD>" << std::endl;
    }

    out << "</TR></TABLE>>];" << std::endl;
    if (title) out << "}" << std::endl;
}

#endif //_DEBUG

}
