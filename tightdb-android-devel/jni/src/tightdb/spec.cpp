#include "spec.hpp"

using namespace std;

namespace tightdb {

// Uninitialized Spec (call UpdateRef to init)
Spec::Spec(Allocator& alloc):
m_specSet(alloc), m_spec(alloc), m_names(alloc), m_subSpecs(alloc)
{
}

// Create a new Spec
Spec::Spec(Allocator& alloc, ArrayParent* parent, size_t pndx):
m_specSet(COLUMN_HASREFS, parent, pndx, alloc), m_spec(COLUMN_NORMAL, NULL, 0, alloc), m_names(NULL, 0, alloc), m_subSpecs(alloc)
{
    // The SpecSet contains the specification (types and names) of all columns and sub-tables
    m_specSet.add(m_spec.GetRef());
    m_specSet.add(m_names.GetRef());
    m_spec.SetParent(&m_specSet, 0);
    m_names.SetParent(&m_specSet, 1);
}

// Create Spec from ref
Spec::Spec(Allocator& alloc, size_t ref, ArrayParent* parent, size_t pndx):
m_specSet(alloc), m_spec(alloc), m_names(alloc), m_subSpecs(alloc)
{
    create(ref, parent, pndx);
}

Spec::Spec(const Spec& s):
m_specSet(s.m_specSet.GetAllocator()), m_spec(s.m_specSet.GetAllocator()),
m_names(s.m_specSet.GetAllocator()), m_subSpecs(s.m_specSet.GetAllocator())
{
    const size_t ref    = s.m_specSet.GetRef();
    ArrayParent *parent = s.m_specSet.GetParent();
    const size_t pndx   = s.m_specSet.GetParentNdx();

    create(ref, parent, pndx);
}

void Spec::create(size_t ref, ArrayParent* parent, size_t pndx)
{
    m_specSet.UpdateRef(ref);
    m_specSet.SetParent(parent, pndx);
    assert(m_specSet.Size() == 2 || m_specSet.Size() == 3);

    m_spec.UpdateRef(m_specSet.GetAsRef(0));
    m_spec.SetParent(&m_specSet, 0);
    m_names.UpdateRef(m_specSet.GetAsRef(1));
    m_names.SetParent(&m_specSet, 1);

    // SubSpecs array is only there when there are subtables
    if (m_specSet.Size() == 3) {
        m_subSpecs.UpdateRef(m_specSet.GetAsRef(2));
        m_subSpecs.SetParent(&m_specSet, 2);
    }
}

void Spec::destroy() {
    m_specSet.Destroy();
}

size_t Spec::get_ref() const {
    return m_specSet.GetRef();
}

void Spec::update_ref(size_t ref, ArrayParent* parent, size_t pndx) {
    create(ref, parent, pndx);
}

void Spec::set_parent(ArrayParent* parent, size_t pndx) {
    m_specSet.SetParent(parent, pndx);
}

bool Spec::update_from_parent() {
    if (m_specSet.UpdateFromParent()) {
        m_spec.UpdateFromParent();
        m_names.UpdateFromParent();
        if (m_specSet.Size() == 3) {
            m_subSpecs.UpdateFromParent();
        }
        return true;
    }
    else return false;
}

void Spec::add_column(ColumnType type, const char* name)
{
    assert(name);

    m_names.add(name);
    m_spec.add(type);

    if (type == COLUMN_TYPE_TABLE) {
        // SubSpecs array is only there when there are subtables
        if (m_specSet.Size() == 2) {
            m_subSpecs.SetType(COLUMN_HASREFS);
            //m_subSpecs.SetType((ColumnDef)4);
            //return;
            m_specSet.add(m_subSpecs.GetRef());
            m_subSpecs.SetParent(&m_specSet, 2);
        }

        Allocator& alloc = m_specSet.GetAllocator();

        // Create spec for new subtable
        Array spec(COLUMN_NORMAL, NULL, 0, alloc);
        ArrayString names(NULL, 0, alloc);
        Array specSet(COLUMN_HASREFS, NULL, 0, alloc);
        specSet.add(spec.GetRef());
        specSet.add(names.GetRef());

        // Add to list of subspecs
        const size_t ref = specSet.GetRef();
        m_subSpecs.add(ref);
    }
}

Spec Spec::add_subtable_column(const char* name)
{
    const size_t column_ndx = m_names.Size();
    add_column(COLUMN_TYPE_TABLE, name);

    return get_subspec(column_ndx);
}

Spec Spec::get_subspec(size_t column_ndx)
{
    assert(column_ndx < m_spec.Size());
    assert((ColumnType)m_spec.Get(column_ndx) == COLUMN_TYPE_TABLE);

    // The subspec array only keep info for subtables
    // so we need to count up to it's position
    size_t pos = 0;
    for (size_t i = 0; i < column_ndx; ++i) {
        if ((ColumnType)m_spec.Get(i) == COLUMN_TYPE_TABLE) ++pos;
    }

    Allocator& alloc = m_specSet.GetAllocator();
    const size_t ref = m_subSpecs.GetAsRef(pos);

    return Spec(alloc, ref, &m_subSpecs, pos);
}

const Spec Spec::get_subspec(size_t column_ndx) const
{
    assert(column_ndx < m_spec.Size());
    assert((ColumnType)m_spec.Get(column_ndx) == COLUMN_TYPE_TABLE);

    // The subspec array only keep info for subtables
    // so we need to count up to it's position
    size_t pos = 0;
    for (size_t i = 0; i < column_ndx; ++i) {
        if ((ColumnType)m_spec.Get(i) == COLUMN_TYPE_TABLE) ++pos;
    }

    Allocator& alloc = m_specSet.GetAllocator();
    const size_t ref = m_subSpecs.GetAsRef(pos);

    return Spec(alloc, ref, NULL, 0);
}

size_t Spec::get_subspec_ref(std::size_t subtable_ndx) const {
    assert(subtable_ndx < m_subSpecs.Size());

    // Note that this addresses subspecs directly, indexing
    // by number of sub-table columns
    return m_subSpecs.GetAsRef(subtable_ndx);
}

size_t Spec::get_type_attr_count() const {
    return m_spec.Size();
}

ColumnType Spec::get_type_attr(size_t ndx) const {
    return (ColumnType)m_spec.Get(ndx);
}

size_t Spec::get_column_count() const
{
    return m_names.Size();
}

ColumnType Spec::get_real_column_type(size_t ndx) const
{
    assert(ndx < get_column_count());

    ColumnType type;
    size_t column_ndx = 0;
    for (size_t i = 0; column_ndx <= ndx; ++i) {
        type = (ColumnType)m_spec.Get(i);
        if (type >= COLUMN_ATTR_INDEXED) continue; // ignore attributes
        ++column_ndx;
    }

    return type;
}

ColumnType Spec::get_column_type(size_t ndx) const
{
    assert(ndx < get_column_count());

    const ColumnType type = get_real_column_type(ndx);

    // Hide internal types
    if (type == COLUMN_TYPE_STRING_ENUM) return COLUMN_TYPE_STRING;
    else return type;
}

void Spec::set_column_type(std::size_t column_ndx, ColumnType type) {
    assert(column_ndx < get_column_count());

    size_t type_ndx = 0;
    size_t column_count = 0;
    const size_t count = m_spec.Size();

    for (;type_ndx < count; ++type_ndx) {
        const size_t t = (ColumnType)m_spec.Get(type_ndx);
        if (t >= COLUMN_ATTR_INDEXED) continue; // ignore attributes
        if (column_count == column_ndx) break;
        ++column_count;
    }

    // At this point we only support upgrading to string enum
    assert((ColumnType)m_spec.Get(type_ndx) == COLUMN_TYPE_STRING);
    assert(type == COLUMN_TYPE_STRING_ENUM);

    m_spec.Set(type_ndx, type);
}

ColumnType Spec::get_column_attr(size_t ndx) const
{
    assert(ndx < get_column_count());

    size_t column_ndx = 0;

    // The attribute is an optional prefix for the type
    for (size_t i = 0; column_ndx <= ndx; ++i) {
        const ColumnType type = (ColumnType)m_spec.Get(i);
        if (type >= COLUMN_ATTR_INDEXED) {
            if (column_ndx == ndx) return type;
        }
        else ++column_ndx;
    }

    return COLUMN_ATTR_NONE;
}

void Spec::set_column_attr(size_t ndx, ColumnType attr)
{
    assert(ndx < get_column_count());
    assert(attr >= COLUMN_ATTR_INDEXED);

    size_t column_ndx = 0;

    for (size_t i = 0; column_ndx <= ndx; ++i) {
        const ColumnType type = (ColumnType)m_spec.Get(i);
        if (type >= COLUMN_ATTR_INDEXED) {
            if (column_ndx == ndx) {
                // if column already has an attr, we replace it
                if (attr == COLUMN_ATTR_NONE) m_spec.Delete(i);
                else m_spec.Set(i, attr);
                return;
            }
        }
        else {
            if (column_ndx == ndx) {
                // prefix type with attr
                m_spec.Insert(i, attr);
                return;
            }
            ++column_ndx;
        }
    }
}

const char* Spec::get_column_name(size_t ndx) const
{
    assert(ndx < get_column_count());
    return m_names.Get(ndx);
}

size_t Spec::get_column_index(const char* name) const
{
    return m_names.find_first(name);
}

#ifdef _DEBUG

bool Spec::compare(const Spec& spec) const {
    if (!m_spec.Compare(spec.m_spec)) return false;
    if (!m_names.Compare(spec.m_names)) return false;

    return true;
}

void Spec::Verify() const {
    const size_t column_count = get_column_count();
    assert(column_count == m_names.Size());
    assert(column_count == m_spec.Size());
}

void Spec::to_dot(std::ostream& out, const char*) const
{
    const size_t ref = m_specSet.GetRef();

    out << "subgraph cluster_specset" << ref << " {" << endl;
    out << " label = \"specset\";" << endl;

    m_specSet.ToDot(out);
    m_spec.ToDot(out, "spec");
    m_names.ToDot(out, "names");
    if (m_subSpecs.IsValid()) {
        m_subSpecs.ToDot(out, "subspecs");

        const size_t count = m_subSpecs.Size();
        Allocator& alloc = m_specSet.GetAllocator();

        // Write out subspecs
        for (size_t i = 0; i < count; ++i) {
            const size_t ref = m_subSpecs.GetAsRef(i);
            const Spec s(alloc, ref, NULL, 0);

            s.to_dot(out);
        }
    }

    out << "}" << endl;
}

#endif //_DEBUG


} //namespace tightdb
