#include "column_string_enum.hpp"

namespace tightdb {

ColumnStringEnum::ColumnStringEnum(size_t ref_keys, size_t ref_values, ArrayParent* parent,
                                   size_t pndx, Allocator& alloc):
    Column(ref_values, parent, pndx+1, alloc), m_keys(ref_keys, parent, pndx, alloc) {}

ColumnStringEnum::~ColumnStringEnum() {}

void ColumnStringEnum::Destroy()
{
    m_keys.Destroy();
    Column::Destroy();
}

void ColumnStringEnum::UpdateParentNdx(int diff)
{
    m_keys.UpdateParentNdx(diff);
    Column::UpdateParentNdx(diff);
}

void ColumnStringEnum::UpdateFromParent()
{
    m_array->UpdateFromParent();
    m_keys.UpdateFromParent();
}

size_t ColumnStringEnum::Size() const
{
    return Column::Size();
}

bool ColumnStringEnum::is_empty() const
{
    return Column::is_empty();
}

const char* ColumnStringEnum::Get(size_t ndx) const
{
    assert(ndx < Column::Size());
    const size_t key_ndx = Column::GetAsRef(ndx);
    return m_keys.Get(key_ndx);
}

bool ColumnStringEnum::add(const char* value)
{
    return Insert(Column::Size(), value);
}

bool ColumnStringEnum::Set(size_t ndx, const char* value)
{
    assert(ndx < Column::Size());
    assert(value);

    const size_t key_ndx = GetKeyNdxOrAdd(value);
    return Column::Set(ndx, key_ndx);
}

bool ColumnStringEnum::Insert(size_t ndx, const char* value)
{
    assert(ndx <= Column::Size());
    assert(value);

    const size_t key_ndx = GetKeyNdxOrAdd(value);
    return Column::Insert(ndx, key_ndx);
}

void ColumnStringEnum::Delete(size_t ndx)
{
    assert(ndx < Column::Size());
    Column::Delete(ndx);
}

void ColumnStringEnum::Clear()
{
    // Note that clearing a StringEnum does not remove keys
    Column::Clear();
}

void ColumnStringEnum::find_all(Array& res, const char* value, size_t start, size_t end) const
{
    const size_t key_ndx = m_keys.find_first(value);
    if (key_ndx == (size_t)-1) return;
    Column::find_all(res, key_ndx, 0, start, end);
    return;
}

void ColumnStringEnum::find_all(Array& res, size_t key_ndx, size_t start, size_t end) const
{
    if (key_ndx == (size_t)-1) return;
    Column::find_all(res, key_ndx, 0, start, end);
    return;
}


size_t ColumnStringEnum::find_first(size_t key_ndx, size_t start, size_t end) const
{
    // Find key
    if (key_ndx == (size_t)-1) return (size_t)-1;

    return Column::find_first(key_ndx, start, end);
}

size_t ColumnStringEnum::find_first(const char* value, size_t start, size_t end) const
{
    // Find key
    const size_t key_ndx = m_keys.find_first(value);
    if (key_ndx == (size_t)-1) return (size_t)-1;

    return Column::find_first(key_ndx, start, end);
}

size_t ColumnStringEnum::GetKeyNdx(const char* value) const
{
    return m_keys.find_first(value);
}

size_t ColumnStringEnum::GetKeyNdxOrAdd(const char* value)
{
    const size_t res = m_keys.find_first(value);
    if (res != (size_t)-1) return res;
    else {
        // Add key if it does not exist
        const size_t pos = m_keys.Size();
        m_keys.add(value);
        return pos;
    }
}

#ifdef _DEBUG

bool ColumnStringEnum::Compare(const ColumnStringEnum& c) const
{
    if (c.Size() != Size()) return false;

    for (size_t i = 0; i < Size(); ++i) {
        const char* s1 = Get(i);
        const char* s2 = c.Get(i);
        if (strcmp(s1, s2) != 0) return false;
    }

    return true;
}

void ColumnStringEnum::Verify() const
{
    m_keys.Verify();
    Column::Verify();
}

void ColumnStringEnum::ToDot(std::ostream& out, const char* title) const
{
    const size_t ref = m_keys.GetRef();

    out << "subgraph cluster_columnstringenum" << ref << " {" << std::endl;
    out << " label = \"ColumnStringEnum";
    if (title) out << "\\n'" << title << "'";
    out << "\";" << std::endl;

    m_keys.ToDot(out, "keys");
    Column::ToDot(out, "values");

    out << "}" << std::endl;
}

#endif //_DEBUG

}
