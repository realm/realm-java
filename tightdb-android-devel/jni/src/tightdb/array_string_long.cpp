#include "array_string_long.hpp"
#include "array_blob.hpp"
#include "column.hpp"
#include <assert.h>

#ifdef _MSC_VER
#include <win32/types.h> //ssize_t
#endif

namespace tightdb {

ArrayStringLong::ArrayStringLong(ArrayParent* parent, size_t pndx, Allocator& alloc):
    Array(COLUMN_HASREFS, parent, pndx, alloc),
    m_offsets(COLUMN_NORMAL, NULL, 0, alloc), m_blob(NULL, 0, alloc)
{
    // Add subarrays for long string
    Array::add(m_offsets.GetRef());
    Array::add(m_blob.GetRef());
    m_offsets.SetParent(this, 0);
    m_blob.SetParent(this, 1);
}

ArrayStringLong::ArrayStringLong(size_t ref, ArrayParent* parent, size_t pndx, Allocator& alloc):
    Array(ref, parent, pndx, alloc), m_offsets(Array::GetAsRef(0), NULL, 0, alloc),
    m_blob(Array::GetAsRef(1), NULL, 0, alloc)
{
    assert(HasRefs() && !IsNode()); // HasRefs indicates that this is a long string
    assert(Array::Size() == 2);
    assert(m_blob.Size() == (m_offsets.is_empty() ? 0 : (size_t)m_offsets.back()));

    m_offsets.SetParent(this, 0);
    m_blob.SetParent(this, 1);
}

// Creates new array (but invalid, call UpdateRef to init)
//ArrayStringLong::ArrayStringLong(Allocator& alloc) : Array(alloc) {}

ArrayStringLong::~ArrayStringLong() {}

bool ArrayStringLong::is_empty() const
{
    return m_offsets.is_empty();
}
size_t ArrayStringLong::Size() const
{
    return m_offsets.Size();
}

const char* ArrayStringLong::Get(size_t ndx) const
{
    assert(ndx < m_offsets.Size());

    const size_t offset = ndx ? (size_t)m_offsets.Get(ndx-1) : 0;
    return (const char*)m_blob.Get(offset);
}

void ArrayStringLong::add(const char* value)
{
    add(value, strlen(value));
}

void ArrayStringLong::add(const char* value, size_t len)
{
    assert(value);

    len += 1; // include trailing null byte
    m_blob.add(value, len);
    m_offsets.add(m_offsets.is_empty() ? len : m_offsets.back() + len);
}

void ArrayStringLong::Set(size_t ndx, const char* value)
{
    Set(ndx, value, strlen(value));
}

void ArrayStringLong::Set(size_t ndx, const char* value, size_t len)
{
    assert(ndx < m_offsets.Size());
    assert(value);

    const size_t start = ndx ? (size_t)m_offsets.Get(ndx-1) : 0;
    const size_t current_end = (size_t)m_offsets.Get(ndx);

    len += 1; // include trailing null byte
    const ssize_t diff =  (start + len) - current_end;

    m_blob.Replace(start, current_end, value, len);
    m_offsets.Adjust(ndx, diff);
}

void ArrayStringLong::Insert(size_t ndx, const char* value)
{
    Insert(ndx, value, strlen(value));
}

void ArrayStringLong::Insert(size_t ndx, const char* value, size_t len)
{
    assert(ndx <= m_offsets.Size());
    assert(value);

    const size_t pos = ndx ? (size_t)m_offsets.Get(ndx-1) : 0;
    len += 1; // include trailing null byte

    m_blob.Insert(pos, value, len);
    m_offsets.Insert(ndx, pos + len);
    m_offsets.Adjust(ndx+1, len);
}

void ArrayStringLong::Delete(size_t ndx)
{
    assert(ndx < m_offsets.Size());

    const size_t start = ndx ? (size_t)m_offsets.Get(ndx-1) : 0;
    const size_t end = (size_t)m_offsets.Get(ndx);

    m_blob.Delete(start, end);
    m_offsets.Delete(ndx);
    m_offsets.Adjust(ndx, (int64_t)start - end);
}

void ArrayStringLong::Resize(size_t ndx)
{
    assert(ndx < m_offsets.Size());

    const size_t len = ndx ? (size_t)m_offsets.Get(ndx-1) : 0;

    m_offsets.Resize(ndx);
    m_blob.Resize(len);
}

void ArrayStringLong::Clear()
{
    m_blob.Clear();
    m_offsets.Clear();
}

size_t ArrayStringLong::find_first(const char* value, size_t start, size_t end) const
{
    assert(value);
    return FindWithLen(value, strlen(value), start, end);
}

void ArrayStringLong::find_all(Array& result, const char* value, size_t add_offset,
                              size_t start, size_t end) const
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

size_t ArrayStringLong::FindWithLen(const char* value, size_t len, size_t start, size_t end) const
{
    assert(value);

    len += 1; // include trailing null byte
    const size_t count = m_offsets.Size();
    size_t offset = (start == 0 ? 0 : (size_t)m_offsets.Get(start - 1)); // todo, verify
    for (size_t i = start; i < count && i < end; ++i) {
        const size_t end = (size_t)m_offsets.Get(i);

        // Only compare strings if length matches
        if ((end - offset) == len) {
            const char* const v = (const char*)m_blob.Get(offset);
            if (value[0] == *v && strcmp(value, v) == 0)
                return i;
        }
        offset = end;
    }

    return (size_t)-1; // not found
}

#ifdef _DEBUG

void ArrayStringLong::ToDot(std::ostream& out, const char* title) const
{
    const size_t ref = GetRef();

    out << "subgraph cluster_arraystringlong" << ref << " {" << std::endl;
    out << " label = \"ArrayStringLong";
    if (title) out << "\\n'" << title << "'";
    out << "\";" << std::endl;

    Array::ToDot(out, "stringlong_top");
    m_offsets.ToDot(out, "offsets");
    m_blob.ToDot(out, "blob");

    out << "}" << std::endl;
}

#endif //_DEBUG

}
