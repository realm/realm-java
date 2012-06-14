#include <cassert>
#include <vector>
#include <iostream>
#include <iomanip>
#include "array.hpp"
#include "column.hpp"
#include "utilities.hpp"
#include "query_conditions.hpp"
#include "static_assert.hpp"

#ifdef _MSC_VER
    #include <win32/types.h>
    #pragma warning (disable : 4127) // Condition is constant warning
#endif

#define MAX(a, b) (((a) > (b)) ? (a) : (b))

using namespace std;

namespace tightdb {

Array::Array(size_t ref, ArrayParent* parent, size_t pndx, Allocator& alloc):
    m_data(NULL), m_len(0), m_capacity(0), m_width(0), m_isNode(false), m_hasRefs(false),
    m_parent(parent), m_parentNdx(pndx), m_alloc(alloc), m_lbound(0), m_ubound(0)
{
    Create(ref);
}

Array::Array(ColumnDef type, ArrayParent* parent, size_t pndx, Allocator& alloc):
    m_data(NULL), m_len(0), m_capacity(0), m_width((size_t)-1), m_isNode(false), m_hasRefs(false),
    m_parent(parent), m_parentNdx(pndx), m_alloc(alloc), m_lbound(0), m_ubound(0)
{
    if (type == COLUMN_NODE) m_isNode = m_hasRefs = true;
    else if (type == COLUMN_HASREFS)    m_hasRefs = true;

    Alloc(0, 0);
    SetWidth(0);
}

// Creates new array (but invalid, call UpdateRef or SetType to init)
Array::Array(Allocator& alloc):
    m_data(NULL), m_ref(0), m_len(0), m_capacity(0), m_width((size_t)-1), m_isNode(false),
    m_parent(NULL), m_parentNdx(0), m_alloc(alloc) {}

// Copy-constructor
// Note that this array now own the ref. Should only be used when
// the source array goes away right after (like return values from functions)
Array::Array(const Array& src):
    m_parent(src.m_parent), m_parentNdx(src.m_parentNdx), m_alloc(src.m_alloc)
{
    const size_t ref = src.GetRef();
    Create(ref);
    src.Invalidate();
}

Array::~Array() {}

// Header format (8 bytes):
// |--------|--------|--------|--------|--------|--------|--------|--------|
// |12-33444|          length          |         capacity         |reserved|
//
//  1: isNode  2: hasRefs  3: multiplier  4: width (packed in 3 bits)

void Array::set_header_isnode(bool value, void* header)
{
    uint8_t* const header2 = header ? (uint8_t*)header : (m_data - 8);
    header2[0] = (header2[0] & (~0x80)) | (uint8_t)(value << 7);
}

void Array::set_header_hasrefs(bool value, void* header)
{
    uint8_t* const header2 = header ? (uint8_t*)header : (m_data - 8);
    header2[0] = (header2[0] & (~0x40)) | (uint8_t)(value << 6);
}

void Array::set_header_wtype(WidthType value, void* header)
{
    // Indicates how to calculate size in bytes based on width
    // 0: bits      (width/8) * length
    // 1: multiply  width * length
    // 2: ignore    1 * length
    uint8_t* const header2 = header ? (uint8_t*)header : (m_data - 8);
    header2[0] = (header2[0] & (~0x18)) | (uint8_t)(value << 3);
}

void Array::set_header_width(size_t value, void* header)
{
    // Pack width in 3 bits (log2)
    size_t w = 0;
    size_t b = (size_t)value;
    while (b) {++w; b >>= 1;}
    assert(w < 8);

    uint8_t* const header2 = header ? (uint8_t*)header : (m_data - 8);
    header2[0] = (header2[0] & (~0x7)) | (uint8_t)w;
}

void Array::set_header_len(size_t value, void* header)
{
    assert(value <= 0xFFFFFF);
    uint8_t* const header2 = header ? (uint8_t*)header : (m_data - 8);
    header2[1] = ((value >> 16) & 0x000000FF);
    header2[2] = (value >> 8) & 0x000000FF;
    header2[3] = value & 0x000000FF;
}

void Array::set_header_capacity(size_t value, void* header)
{
    assert(value <= 0xFFFFFF);
    uint8_t* const header2 = header ? (uint8_t*)header : (m_data - 8);
    header2[4] = (value >> 16) & 0x000000FF;
    header2[5] = (value >> 8) & 0x000000FF;
    header2[6] = value & 0x000000FF;
}

bool Array::get_header_isnode(const void* header) const
{
    const uint8_t* const header2 = header ? (const uint8_t*)header : (m_data - 8);
    return (header2[0] & 0x80) != 0;
}

bool Array::get_header_hasrefs(const void* header) const
{
    const uint8_t* const header2 = header ? (const uint8_t*)header : (m_data - 8);
    return (header2[0] & 0x40) != 0;
}

Array::WidthType Array::get_header_wtype(const void* header) const
{
    const uint8_t* const header2 = header ? (const uint8_t*)header : (m_data - 8);
    return (WidthType)((header2[0] & 0x18) >> 3);
}

size_t Array::get_header_width(const void* header) const
{
    const uint8_t* const header2 = header ? (const uint8_t*)header : (m_data - 8);
    return (1 << (header2[0] & 0x07)) >> 1;
}

size_t Array::get_header_len(const void* header) const
{
    const uint8_t* const header2 = header ? (const uint8_t*)header : (m_data - 8);
    return (header2[1] << 16) + (header2[2] << 8) + header2[3];
}

size_t Array::get_header_capacity(const void* header) const
{
    const uint8_t* const header2 = header ? (const uint8_t*)header : (m_data - 8);
    return (header2[4] << 16) + (header2[5] << 8) + header2[6];
}

void Array::Create(size_t ref)
{
    assert(ref);
    uint8_t* const header = (uint8_t*)m_alloc.Translate(ref);
    CreateFromHeader(header, ref);
}
    
void Array::CreateFromHeaderDirect(uint8_t* header, size_t ref) {
    // Parse header
    // We only need limited info for direct read-only use
    m_width    = get_header_width(header);
    m_len      = get_header_len(header);
    
    m_ref = ref;
    m_data = header + 8;
    
    SetWidth(m_width);
}

void Array::CreateFromHeader(uint8_t* header, size_t ref) {
    // Parse header
    m_isNode   = get_header_isnode(header);
    m_hasRefs  = get_header_hasrefs(header);
    m_width    = get_header_width(header);
    m_len      = get_header_len(header);
    const size_t byte_capacity = get_header_capacity(header);

    // Capacity is how many items there are room for
    m_capacity = CalcItemCount(byte_capacity, m_width);

    m_ref = ref;
    m_data = header + 8;

    SetWidth(m_width);
}


void Array::SetType(ColumnDef type)
{
    if (m_ref) CopyOnWrite();

    if (type == COLUMN_NODE) m_isNode = m_hasRefs = true;
    else if (type == COLUMN_HASREFS)    m_hasRefs = true;
    else m_isNode = m_hasRefs = false;

    if (!m_data) {
        // Create array
        Alloc(0, 0);
        SetWidth(0);
    }
    else {
        // Update Header
        set_header_isnode(m_isNode);
        set_header_hasrefs(m_hasRefs);
    }
}

bool Array::operator==(const Array& a) const
{
    return m_data == a.m_data;
}

void Array::UpdateRef(size_t ref)
{
    Create(ref);
    update_ref_in_parent(ref);
}

bool Array::UpdateFromParent() {
    if (!m_parent) return false;

    // After commit to disk, the array may have moved
    // so get ref from parent and see if it has changed
    const size_t new_ref = m_parent->get_child_ref(m_parentNdx);

    if (new_ref != m_ref) {
        Create(new_ref);
        return true;
    }

    return false; // not modified
}

/**
 * Takes a 64bit value and return the minimum number of bits needed to fit the
 * value.
 * For alignment this is rounded up to nearest log2.
 * Posssible results {0, 1, 2, 4, 8, 16, 32, 64}
 */
static size_t BitWidth(int64_t v)
{
    if ((v >> 4) == 0) {
        static const int8_t bits[] = {0, 1, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};
        return bits[(int8_t)v];
    }

    // First flip all bits if bit 63 is set (will now always be zero)
    if (v < 0) v = ~v;

    // Then check if bits 15-31 used (32b), 7-31 used (16b), else (8b)
    return v >> 31 ? 64 : v >> 15 ? 32 : v >> 7 ? 16 : 8;
}

// Allocates space for 'count' items being between min and min in size, both inclusive. Crashes! Why? Todo/fixme
void Array::Preset(size_t bitwidth, size_t count)
{
    Clear();
    SetWidth(bitwidth);
    assert(Alloc(count, bitwidth));
    m_len = count;
    for(size_t n = 0; n < count; n++)
        Set(n, 0);
}

void Array::Preset(int64_t min, int64_t max, size_t count)
{
    size_t w = MAX(BitWidth(max), BitWidth(min));
    Preset(w, count);
}

void Array::SetParent(ArrayParent *parent, size_t pndx)
{
    m_parent = parent;
    m_parentNdx = pndx;
}

Array Array::GetSubArray(size_t ndx)
{
    assert(ndx < m_len);
    assert(m_hasRefs);

    const size_t ref = (size_t)Get(ndx);
    assert(ref);

    return Array(ref, this, ndx, m_alloc);
}

const Array Array::GetSubArray(size_t ndx) const
{
    assert(ndx < m_len);
    assert(m_hasRefs);

    return Array(size_t(Get(ndx)), const_cast<Array *>(this), ndx, m_alloc);
}

void Array::Destroy()
{
    if (!m_data) return;

    if (m_hasRefs) {
        for (size_t i = 0; i < m_len; ++i) {
            const size_t ref = (size_t)Get(i);

            // null-refs signify empty sub-trees
            if (ref == 0) continue;

            // all refs are 64bit aligned, so the lowest bits
            // cannot be set. If they are it means that it should
            // not be interpreted as a ref
            if (ref & 0x1) continue;

            Array sub(ref, this, i, m_alloc);
            sub.Destroy();
        }
    }

    void* ref = m_data-8;
    m_alloc.Free(m_ref, ref);
    m_data = NULL;
}

void Array::Clear()
{
    CopyOnWrite();

    // Make sure we don't have any dangling references
    if (m_hasRefs) {
        for (size_t i = 0; i < Size(); ++i) {
            const size_t ref = GetAsRef(i);
            if (ref == 0 || ref & 0x1) continue; // zero-refs and refs that are not 64-aligned do not point to sub-trees

            Array sub(ref, this, i, m_alloc);
            sub.Destroy();
        }
    }

    // Truncate size to zero (but keep capacity)
    m_len      = 0;
    m_capacity = CalcItemCount(get_header_capacity(), 0);
    SetWidth(0);

    // Update header
    set_header_len(0);
    set_header_width(0);
}

void Array::Delete(size_t ndx)
{
    assert(ndx < m_len);

    // Check if we need to copy before modifying
    CopyOnWrite();

    // Move values below deletion up
    if (m_width < 8) {
        for (size_t i = ndx+1; i < m_len; ++i) {
            const int64_t v = (this->*m_getter)(i);
            (this->*m_setter)(i-1, v);
        }
    }
    else if (ndx < m_len-1) {
        // when byte sized, use memmove
        const size_t w = (m_width == 64) ? 8 : (m_width == 32) ? 4 : (m_width == 16) ? 2 : 1;
        unsigned char* dst = m_data + (ndx * w);
        unsigned char* src = dst + w;
        const size_t count = (m_len - ndx - 1) * w;
        memmove(dst, src, count);
    }

    // Update length (also in header)
    --m_len;
    set_header_len(m_len);
}

int64_t Array::Get(size_t ndx) const
{
    assert(ndx < m_len);
    return (this->*m_getter)(ndx);
}

size_t Array::GetAsRef(size_t ndx) const
{
    assert(ndx < m_len);
    assert(m_hasRefs);
    const int64_t v = (this->*m_getter)(ndx);
    return TO_REF(v);
}

int64_t Array::back() const
{
    assert(m_len);
    return (this->*m_getter)(m_len-1);
}

bool Array::Set(size_t ndx, int64_t value)
{
    assert(ndx < m_len);

    // Check if we need to copy before modifying
    if (!CopyOnWrite()) return false;

    // Make room for the new value
    size_t width = m_width;

    if(value < m_lbound || value > m_ubound)
        width = BitWidth(value);

    const bool doExpand = (width > m_width);
    if (doExpand) {

        Getter oldGetter = m_getter;
        if (!Alloc(m_len, width)) return false;
        SetWidth(width);

        // Expand the old values
        int k = (int)m_len;
        while (--k >= 0) {
            const int64_t v = (this->*oldGetter)(k);
            (this->*m_setter)(k, v);
        }
    }

    // Set the value
    (this->*m_setter)(ndx, value);

    return true;
}

// Optimization for the common case of adding
// positive values to a local array (happens a
// lot when returning results to TableViews)
bool Array::AddPositiveLocal(int64_t value)
{
    assert(value >= 0);
    assert(&m_alloc == &GetDefaultAllocator());

    if (value <= m_ubound) {
        if (m_len < m_capacity) {
            (this->*m_setter)(m_len, value);
            ++m_len;
            set_header_len(m_len);
            return true;
        }
    }

    return Insert(m_len, value);
}

bool Array::Insert(size_t ndx, int64_t value)
{
    assert(ndx <= m_len);

    // Check if we need to copy before modifying
    if (!CopyOnWrite()) return false;

    Getter getter = m_getter;

    // Make room for the new value
    size_t width = m_width;

    if(value < m_lbound || value > m_ubound)
        width = BitWidth(value);

    const bool doExpand = (width > m_width);
    if (doExpand) {
        if (!Alloc(m_len+1, width)) return false;
        SetWidth(width);
    }
    else {
        if (!Alloc(m_len+1, m_width)) return false;
    }

    // Move values below insertion (may expand)
    if (doExpand || m_width < 8) {
        int k = (int)m_len;
        while (--k >= (int)ndx) {
            const int64_t v = (this->*getter)(k);
            (this->*m_setter)(k+1, v);
        }
    }
    else if (ndx != m_len) {
        // when byte sized and no expansion, use memmove
        const size_t w = (m_width == 64) ? 8 : (m_width == 32) ? 4 : (m_width == 16) ? 2 : 1;
        unsigned char* src = m_data + (ndx * w);
        unsigned char* dst = src + w;
        const size_t count = (m_len - ndx) * w;
        memmove(dst, src, count);
    }

    // Insert the new value
    (this->*m_setter)(ndx, value);

    // Expand values above insertion
    if (doExpand) {
        int k = (int)ndx;
        while (--k >= 0) {
            const int64_t v = (this->*getter)(k);
            (this->*m_setter)(k, v);
        }
    }

    // Update length
    // (no need to do it in header as it has been done by Alloc)
    ++m_len;

    return true;
}


bool Array::add(int64_t value)
{
    return Insert(m_len, value);
}

void Array::Resize(size_t count)
{
    assert(count <= m_len);

    CopyOnWrite();

    // Update length (also in header)
    m_len = count;
    set_header_len(m_len);
}

void Array::SetAllToZero()
{
    CopyOnWrite();

    m_capacity = CalcItemCount(get_header_capacity(), 0);
    SetWidth(0);

    // Update header
    set_header_width(0);
}

bool Array::Increment(int64_t value, size_t start, size_t end)
{
    if (end == (size_t)-1) end = m_len;
    assert(start < m_len);
    assert(end >= start && end <= m_len);

    // Increment range
    for (size_t i = start; i < end; ++i) {
        Set(i, Get(i) + value);
    }
    return true;
}

bool Array::IncrementIf(int64_t limit, int64_t value)
{
    // Update (incr or decrement) values bigger or equal to the limit
    for (size_t i = 0; i < m_len; ++i) {
        const int64_t v = Get(i);
        if (v >= limit) Set(i, v + value);
    }
    return true;
}

void Array::Adjust(size_t start, int64_t diff)
{
    assert(start <= m_len);

    for (size_t i = start; i < m_len; ++i) {
        const int64_t v = Get(i);
        Set(i, v + diff);
    }
}

size_t Array::FindPos(int64_t target) const
{
    int low = -1;
    int high = (int)m_len;

    // Binary search based on:
    // http://www.tbray.org/ongoing/When/200x/2003/03/22/Binary
    // Finds position of largest value SMALLER than the target (for lookups in
    // nodes)
    while (high - low > 1) {
        const size_t probe = ((unsigned int)low + (unsigned int)high) >> 1;
        const int64_t v = (this->*m_getter)(probe);

        if (v > target) high = (int)probe;
        else            low = (int)probe;
    }
    if (high == (int)m_len) return (size_t)-1;
    else return (size_t)high;
}

size_t Array::FindPos2(int64_t target) const
{
    int low = -1;
    int high = (int)m_len;

    // Binary search based on:
    // http://www.tbray.org/ongoing/When/200x/2003/03/22/Binary
    // Finds position of closest value BIGGER OR EQUAL to the target (for
    // lookups in indexes)
    while (high - low > 1) {
        const size_t probe = ((unsigned int)low + (unsigned int)high) >> 1;
        const int64_t v = (this->*m_getter)(probe);

        if (v < target) low = (int)probe;
        else            high = (int)probe;
    }
    if (high == (int)m_len) return (size_t)-1;
    else return (size_t)high;
}



size_t Array::find_first(int64_t value, size_t start, size_t end) const
{
#if defined(USE_SSE42) || defined(USE_SSE3)
    if (end == (size_t)-1)
        end = m_len;

#if defined(USE_SSE42)
    if (end - start < sizeof(__m128i) || m_width < 8)
        return CompareEquality<true>(value, start, end);
#elif defined(USE_SSE3)
    if (end - start < sizeof(__m128i) || m_width < 8 || m_width == 64) // 64 bit not supported by sse3
        return CompareEquality<true>(value, start, end);
#endif

    // FindSSE() must start at 16-byte boundary, so search area before that using CompareEquality()
    __m128i* const a = (__m128i *)round_up(m_data + start * m_width / 8, sizeof(__m128i));
    __m128i* const b = (__m128i *)round_down(m_data + end * m_width / 8, sizeof(__m128i));
    size_t t = 0;

    t = CompareEquality<true>(value, start, ((unsigned char *)a - m_data) * 8 / m_width);
    if (t != (size_t)-1)
        return t;

    // Search aligned area with SSE
    if (b > a) {
        t = FindSSE(value, a, m_width / 8, b - a);
        if (t != (size_t)-1) {
            // FindSSE returns SSE chunk number, so we use CompareEquality() to find packed position
            t = CompareEquality<true>(value, t * sizeof(__m128i) * 8 / m_width  +  (((unsigned char *)a - m_data) * 8 / m_width), end);
            return t;
        }
    }

    // Search remainder with CompareEquality()
    t = CompareEquality<true>(value, ((unsigned char *)b - m_data) * 8 / m_width, end);
    return t;
#else
    return CompareEquality<true>(value, start, end); //FindNaive(value, start, end); // enable legacy find
#endif
}

#if defined(USE_SSE42) || defined(USE_SSE3)
// 'items' is the number of 16-byte SSE chunks. 'bytewidth' is the size of a packed data element.
// Return value is SSE chunk number where the element is guaranteed to exist (use CompareEquality() to
// find packed position)
size_t Array::FindSSE(int64_t value, __m128i *data, size_t bytewidth, size_t items) const
{
    __m128i search = {0}, next, compare = {1};
    size_t i = 0;

//    for (int j = 0; j < (int)(sizeof(__m128i) / bytewidth); ++j)
//        memcpy((char *)&search + j * bytewidth, &value, bytewidth);

    // The loops that initialie '__m128i search' are made simple so that compilers unroll or optimize them
    // automatically (VC and gcc converts the case bytewidth == 1 to memset(), and unrolls the remaining cases fully).

    if (bytewidth == 1) {
        for(size_t t = 0; t < 16; t++)
            *(((char*)&search) + t) = value;

        for (i = 0; i < items && _mm_movemask_epi8(compare) == 0; ++i) {
            next = _mm_load_si128(&data[i]);
            compare = _mm_cmpeq_epi8(search, next);
        }
    }
    else if (bytewidth == 2) {
        for(size_t t = 0; t < 8; t++)
            *(((short int*)&search) + t) = value;

        for (i = 0; i < items && _mm_movemask_epi8(compare) == 0; ++i) {
            next = _mm_load_si128(&data[i]);
            compare = _mm_cmpeq_epi16(search, next);
        }
    }
    else if (bytewidth == 4) {
        for(size_t t = 0; t < 4; t++)
            *(((int*)&search) + t) = value;

        for (i = 0; i < items && _mm_movemask_epi8(compare) == 0; ++i) {
            next = _mm_load_si128(&data[i]);
            compare = _mm_cmpeq_epi32(search, next);
        }
    }
#if defined(USE_SSE42)
    else if (bytewidth == 8) {
        for(size_t t = 0; t < 2; t++)
            *(((int64_t*)&search) + t) = value;

        // Only supported by SSE 4.1 because of _mm_cmpeq_epi64().
        for (i = 0; i < items && _mm_movemask_epi8(compare) == 0; ++i) {
            next = _mm_load_si128(&data[i]);
            compare = _mm_cmpeq_epi64(search, next);
        }
    }
#endif
    else
        assert(true);
    return _mm_movemask_epi8(compare) == 0 ? not_found : i - 1;
}
#endif //USE_SSE


// If gt = true: Find first element which is greater than value
// If gt = false: Find first element which is smaller than value
template <bool eq>size_t Array::CompareEquality(int64_t value, size_t start, size_t end) const
{
    if (end == (size_t)-1) end = m_len;
    assert(start <= m_len && end <= m_len && start <= end);

    // When starting from beginning of array the data is always 64bit aligned
    // but otherwise we have to ensure alignment.
    if (start != 0) {
        // Test 4 items with zero latency for cases where match frequency is high, such
        // as 2-bit values
        if (start + 0 < end && (eq ? (Get(start + 0) == value)   :   (Get(start + 0) != value)))
            return start + 0;
        if (start + 1 < end && (eq ? (Get(start + 1) == value)   :   (Get(start + 1) != value)))
            return start + 1;
        if (start + 2 < end && (eq ? (Get(start + 2) == value)   :   (Get(start + 2) != value)))
            return start + 2;
        if (start + 3 < end && (eq ? (Get(start + 3) == value)   :   (Get(start + 3) != value)))
            return start + 3;
        start += 4;

        if (start >= end)
            return (size_t)-1;

        // Test 64 items with no latency for cases where the first few 64-bit chunks are likely to
        // contain one or more matches (because the linear test we use later cannot extract the position)
        // Also stop at a 64-bit aligned position so we can do aligned chunk reads in later linear test
        size_t ee = round_up(start, 64);// + 64;
        ee = ee > end ? end : ee;
        for (; start < ee; ++start)
            if (eq ? (Get(start) == value) : (Get(start) != value))
                return start;
    }

    if(start >= end)
        return (size_t)-1;

    const int64_t* p = (const int64_t*)(m_data + (start * m_width / 8));
    const int64_t* const e = (int64_t*)(m_data + (end * m_width / 8)) - 1;

    // Test if p is aligned
	assert((size_t)p / 8 * 8 == (size_t)p);

    // Matches are rare enough to setup fast linear search for remaining items. We use
    // bit hacks from http://graphics.stanford.edu/~seander/bithacks.html#HasLessInWord
    if (m_width == 0) {
        if (eq ? (value == 0) : (value != 0))
            return start;
        else
            return not_found;
    }
    else if (m_width == 1) {

        if(value == 0) {
            while (p < e)
                if(eq ? *p != -1 : *p != 0)
                    break;
                else
                    ++p;
        }
        else if (value == 1) {
            while (p < e)
                if(eq ? *p != 0 : *p != -1)
                    break;
                else
                    ++p;
        }
        else
            return not_found;

        start = (p - (int64_t *)m_data) * 8 * 8;
        
        while (start < end)
            if (eq ? Get_1b(start) == value : Get_1b(start) != value)
                return start;
            else
                ++start;
    }
    else if (m_width == 2) {
        const int64_t v = ~0ULL/0x3 * value;
        while (p < e) {
            const uint64_t v2 = *p ^ v; // zero matching bit segments
            const uint64_t hasZeroByte = (v2 - 0x5555555555555555ULL) & ~v2 & 0xAAAAAAAAAAAAAAAAULL;
            if(eq ? hasZeroByte : !hasZeroByte)
                break;
            else
                ++p;
        }
        start = (p - (int64_t *)m_data) * 8 * 8 / 2;
        
        while (start < end)
            if (eq ? Get_2b(start) == value : Get_2b(start) != value)
                return start;
            else
                ++start;
    }
    else if (m_width == 4) {
        const int64_t v = ~0ULL/0xF * value;
        while (p < e) {
            const uint64_t v2 = *p ^ v; // zero matching bit segments
            const uint64_t hasZeroByte = (v2 - 0x1111111111111111ULL) & ~v2 & 0x8888888888888888ULL;
            if (eq ? hasZeroByte : !hasZeroByte)
                break;
            else
                ++p;
        }
        start = (p - (int64_t *)m_data) * 8 * 8 / 4;
        
        while (start < end)
            if (eq ? Get_4b(start) == value : Get_4b(start) != value)
                return start;
            else
                ++start;
    }
    else if (m_width == 8) {
        const int64_t v = ~0ULL/0xFF * value;
        while (p < e) {
            const uint64_t v2 = *p ^ v; // zero matching bit segments
            const uint64_t hasZeroByte = (v2 - 0x0101010101010101ULL) & ~v2 & 0x8080808080808080ULL;
            if (eq ? hasZeroByte : !hasZeroByte)
                break;
            else
                ++p;
        }
        start = (p - (int64_t *)m_data) * 8 * 8 / 8;
        
        while (start < end)
            if (eq ? Get_8b(start) == value : Get_8b(start) != value)
                return start;
            else
                ++start;
    }
    else if (m_width == 16) {
        const int64_t v = ~0ULL/0xFFFF * value;
        while (p < e) {
            const uint64_t v2 = *p ^ v; // zero matching bit segments
            const uint64_t hasZeroByte = (v2 - 0x0001000100010001ULL) & ~v2 & 0x8000800080008000ULL;
            if (eq ? hasZeroByte : !hasZeroByte)
                break;
            else
                ++p;
        }
        start = (p - (int64_t *)m_data) * 8 * 8 / 16;
        
        while (start < end)
            if (eq ? Get_16b(start) == value : Get_16b(start) != value)
                return start;
            else
                ++start;
    }
    else if (m_width == 32) {
        const int64_t v = ~0ULL/0xFFFFFFFF * value;
        while (p < e) {
            const uint64_t v2 = *p ^ v; // zero matching bit segments
            const uint64_t hasZeroByte = (v2 - 0x0000000100000001ULL) & ~v2 & 0x8000800080000000ULL;
            if (eq ? hasZeroByte : !hasZeroByte)
                break;
            else
                ++p;
        }
        start = (p - (int64_t *)m_data) * 8 * 8 / 32;
        
        while (start < end)
            if (eq ? Get_32b(start) == value : Get_32b(start) != value)
                return start;
            else
                ++start;
    }
    else if (m_width == 64) {
        while (p < e) {
            const int64_t v = *p;
            if (eq ? (v == value) : (v != value))
                break;
            else
                ++p;
        }
        start = (p - (int64_t *)m_data) * 8 * 8 / 64;
        
        while (start < end)
            if (eq ? Get_64b(start) == value : Get_64b(start) != value)
                return start;
            else
                ++start;
    }

    return not_found;
}


void Array::find_all(Array& result, int64_t value, size_t colOffset, size_t start, size_t end) const
{
    if (is_empty()) return;
    if (end == (size_t)-1) end = m_len;
    if (start == end) return;

    assert(start < m_len && end <= m_len && start < end);

    // If the value is wider than the column
    // then we know it can't be there
    const size_t width = BitWidth(value);
    if (width > m_width) return;

    size_t f = start - 1;
    for(;;) {
        f = find_first(value, f + 1, end);
        if (f == (size_t)-1)
            break;
        else
            result.AddPositiveLocal(f + colOffset);
    }
}


// If gt = true: Find first element which is greater than value
// If gt = false: Find first element which is smaller than value
template <bool gt>size_t Array::CompareRelation(int64_t value, size_t start, size_t end) const
{
    if (end == (size_t)-1) end = m_len;

    // Test 4 items with zero latency for cases where match frequency is high, such
    // as 2-bit values where each second item is greater on average
    if (start + 0 < end && (gt ? (Get(start + 0) > value)   :   (Get(start + 0) < value)))
        return start + 0;
    if (start + 1 < end && (gt ? (Get(start + 1) > value)   :   (Get(start + 1) < value)))
        return start + 1;
    if (start + 2 < end && (gt ? (Get(start + 2) > value)   :   (Get(start + 2) < value)))
        return start + 2;
    if (start + 3 < end && (gt ? (Get(start + 3) > value)   :   (Get(start + 3) < value)))
        return start + 3;

    start += 4;

    if (start >= end)
        return (size_t)-1;

    if (is_empty()) return (size_t)-1;
    if (start >= end) return (size_t)-1;

    assert(start < m_len && (end <= m_len || end == (size_t)-1) && start < end);

    // Test 64 items with no latency for cases where the first few 64-bit chunks are likely to
    // contain one or more matches (because the linear test we use later cannot extract the position)
    // Also stop at a 64-bit aligned position so we can do aligned chunk reads in later linear test
    size_t ee = round_up(start, 64);
    ee = ee > end ? end : ee;
    for (; start < ee; start++)
        if (gt ? (Get(start) > value)  :   (Get(start) < value))
            return start;

    if(start >= end)
        return (size_t)-1;

    const int64_t* p = (const int64_t*)(m_data + (start * m_width / 8));
    const int64_t* const e = (int64_t*)(m_data + (end * m_width / 8)) - 1;

    // Matches are rare enough to setup fast linear search for remaining items. We use
    // bit hacks from http://graphics.stanford.edu/~seander/bithacks.html#HasLessInWord
    if (m_width == 0) {
        if ((gt && value >= 0) || (!gt && value <= 0))
            return not_found;
    }
    else if (m_width == 1) {

        if ((value > 1 && gt) || (value < 0 && !gt)) {
            return not_found;
        }
        else if(value == 0 && gt) {
            while (p < e)
                if(*p != 0)
                    break;
                else
                    ++p;
        }
        else if (value == 1 && !gt) {
            while (p < e)
                if(*p != -1)
                    break;
                else
                    ++p;
        }

        start = (p - (int64_t *)m_data) * 8 * 8;
        
        while (start < end) 
            if (gt ? Get_1b(start) > value : Get_1b(start) < value)
                return start;
            else
                ++start;

    }
    else if (m_width == 2) {
        if(value <= 1) {
            const int64_t constant = gt ? (~0ULL / 3ULL * (3ULL - value))   :  (   ~0ULL / 3 * value );
            while(p < e) {
                int64_t v = *p;
                if( gt ? (((v + constant) | v) & ~0ULL / 3ULL * 2ULL)   :  ((v - constant) & ~v&~0ULL/3ULL*2ULL)      )
                    break;
                else
                    ++p;
            }
            start = (p - (int64_t *)m_data) * 8 * 8 / m_width;
        }
        else {
            while(start < end && gt ? (Get_2b(start) <= value) : (Get_2b(start) >= value))
                ++start;
        }
    }
    else if (m_width == 4) {
        if(value <= 7) {
            const int64_t constant = gt ? (~0ULL / 15ULL * (7ULL - value))  :   (   ~0ULL / 15ULL * value )  ;
            while(p < e) {
                const int64_t v = *p;
                if(gt ? (((v + constant) | v) & ~0ULL / 15ULL * 8ULL) :     ((v - constant) & ~v&~0ULL/15ULL*8ULL)    )
                    break;
                else
                    ++p;
            }
            start = (p - (int64_t *)m_data) * 8 * 8 / m_width;
        }
        else {
            while(start < end && gt ? (Get_4b(start) <= value) : (Get_4b(start) >= value))
                start++;
        }
    }
    else if (m_width == 8) {
        // Bit hacks only work if searched item <= 127 for 'greater than' and item <= 128 for 'less than'
        if(value <= 127) {
            const int64_t constant = gt ? (~0ULL / 255ULL * (127ULL - value))   :   (        ~0ULL / 255ULL * value           );
            while (p < e) {
                const int64_t v = *p;
                // Bit hacks also only works for positive items in chunk, so test their sign bits
                if(v & 0x8080808080808080ULL) {
                    if (gt ? ((char)(v>>0*8) > value || (char)(v>>1*8) > value || (char)(v>>2*8) > value || (char)(v>>3*8) > value || (char)(v>>4*8) > value || (char)(v>>5*8) > value || (char)(v>>6*8) > value || (char)(v>>7*8) > value)
                      :
                    ((char)(v>>0*8) < value || (char)(v>>1*8) < value || (char)(v>>2*8) < value || (char)(v>>3*8) < value || (char)(v>>4*8) < value || (char)(v>>5*8) < value || (char)(v>>6*8) < value || (char)(v>>7*8) < value))
                        break;
                }
                else if (gt ?  (((v + constant) | v) & ~0ULL / 255ULL * 128ULL) : (         (v - constant) & ~v&~0ULL/255ULL*128ULL             ))
                    break;
                else
                    ++p;
            }
            start = (p - (int64_t *)m_data) * 8 * 8 / m_width;
        }
        else {
            while (start < end && gt ? (Get_8b(start) <= value) : (Get_8b(start) >= value))
                ++start;
        }

    }
    else if (m_width == 16) {
        if (value <= 32767) {
            const int64_t constant = gt ? (~0ULL / 65535ULL * (32767ULL - value))   :   ( ~0ULL / 65535ULL * value);
            while(p < e) {
                const int64_t v = *p;
                if (v & 0x8000800080008000ULL) {
                    if (gt ? ((int)(v>>0*16) > value || (int)(v>>1*16) > value || (int)(v>>2*16) > value || (int)(v>>3*16) > value) :
                        ((int)(v>>0*16) < value || (int)(v>>1*16) < value || (int)(v>>2*16) < value || (int)(v>>3*16) < value))
                        break;
                }
                else if (gt ? (((v + constant) | v) & ~0ULL / 65535ULL * 32768ULL) : (         (v - constant) & ~v&~0ULL/65535ULL*32768ULL        ))
                    break;
                else
                    ++p;
            }
            start = (p - (int64_t *)m_data) * 8 * 8 / m_width;
        }
        else {
            while (start < end && gt ? (Get_16b(start) <= value)  :  (false))
                ++start;
        }


    }
    else if (m_width == 32) {
        // extra logic in SIMD no longer pays off because we have just 2 elements
        // Faster than below version
        while (start < end && gt ? (Get_32b(start) <= value) : (Get_32b(start) >= value) )
            ++start;
    }
    else if (m_width == 64) {
        while (start < end && gt ? (Get_64b(start) <= value) : (Get_64b(start) >= value))
            ++start;
    }

    // Above 'SIMD' search cannot tell the position of the match inside a chunk, so test remainder manually
    while (start < end)
        if (gt ? Get(start) > value : Get(start) < value)
            return start;
        else
            ++start;

    return (size_t)-1;
}

template <> size_t Array::Query<EQUAL>(int64_t value, size_t start, size_t end)
{
    return CompareEquality<true>(value, start, end);
}
template <> size_t Array::Query<NOTEQUAL>(int64_t value, size_t start, size_t end)
{
    return CompareEquality<false>(value, start, end);
}
template <> size_t Array::Query<GREATER>(int64_t value, size_t start, size_t end)
{
    return CompareRelation<true>(value, start, end);
}

template <> size_t Array::Query<LESS>(int64_t value, size_t start, size_t end)
{
    return CompareRelation<false>(value, start, end);
}


bool Array::maximum(int64_t& result, size_t start, size_t end) const
{
    if (end == (size_t)-1) end = m_len;
    if (start == end) return false;
    assert(start < m_len && end <= m_len && start < end);
    if (m_width == 0) {result = 0; return true;} // max value is zero

    result = Get(start);

    for (size_t i = start+1; i < end; ++i) {
        const int64_t v = Get(i);
        if (v > result) {
            result = v;
        }
    }

    return true;
}


bool Array::minimum(int64_t& result, size_t start, size_t end) const
{
    if (end == (size_t)-1) end = m_len;
    if (start == end) return false;
    assert(start < m_len && end <= m_len && start < end);
    if (m_width == 0) {result = 0; return true;} // min value is zero

    result = Get(start);

    for (size_t i = start+1; i < end; ++i) {
        const int64_t v = Get(i);
        if (v < result) {
            result = v;
        }
    }

    return true;
}


int64_t Array::sum(size_t start, size_t end) const
{
    if (is_empty()) return 0;
    if (end == (size_t)-1) end = m_len;
    if (start == end) return 0;
    assert(start < m_len && end <= m_len && start < end);

    int64_t sum = 0;

    if (m_width == 0)
        return 0;
    else if( m_width == 8) {
        for (size_t i = start; i < end; ++i)
            sum += Get_8b(i);
    }
    else if (m_width == 16) {
        for (size_t i = start; i < end; ++i)
            sum += Get_16b(i);
    }
    else if(m_width == 32) {
        for (size_t i = start; i < end; ++i)
            sum += Get_32b(i);
    }
    else if(m_width == 64) {
        for (size_t i = start; i < end; ++i)
            sum += Get_64b(i);
    }
    else {
        // Sum of bitwidths less than a byte (which are always positive)
        // uses a divide and conquer algorithm that is a variation of popolation count:
        // http://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetParallel

        // staiic values needed for fast sums
        const uint64_t m1  = 0x5555555555555555ULL;
        const uint64_t m2  = 0x3333333333333333ULL;
        const uint64_t m4  = 0x0f0f0f0f0f0f0f0fULL;
        const uint64_t h01 = 0x0101010101010101ULL;

        const uint64_t* const next = (const uint64_t*)m_data;
        size_t i = start;

        // Sum manully until 64 bit aligned
        for(; (i < end) && ((i * m_width) % 64 != 0); i++)
            sum += Get(i);

        if (m_width == 1) {
            const size_t chunkvals = 64;
            for (; i + chunkvals <= end; i += chunkvals) {
                uint64_t a = next[i / chunkvals];

                a -= (a >> 1) & m1;
                a = (a & m2) + ((a >> 2) & m2);
                a = (a + (a >> 4)) & m4;
                a = (a * h01) >> 56;

                // Could use intrinsic instead:
                // a = __builtin_popcountll(a); // gcc intrinsic

                sum += a;
            }
        }
        else if (m_width == 2) {
            const size_t chunkvals = 32;
            for (; i + chunkvals <= end; i += chunkvals) {
                uint64_t a = next[i / chunkvals];

                a = (a & m2) + ((a >> 2) & m2);
                a = (a + (a >> 4)) & m4;
                a = (a * h01) >> 56;

                sum += a;
            }
        }
        else if (m_width == 4) {
            const size_t chunkvals = 16;
            for (; i + chunkvals <= end; i += chunkvals) {
                uint64_t a = next[i / chunkvals];

                a = (a & m4) + ((a >> 4) & m4);
                a = (a * h01) >> 56;

                sum += a;
            }
        }

        // Sum remainding elements
        for(; i < end; i++)
            sum += Get(i);
    }

    return sum;
}


void Array::FindAllHamming(Array& result, uint64_t value, size_t maxdist, size_t offset) const
{
    (void)result;
    (void)value;
    (void)maxdist;
    (void)offset;
    /*
    // Only implemented for 64bit values
    if (m_width != 64) {
        assert(false);
        return;
    }

    const uint64_t* p = (const uint64_t*)m_data;
    const uint64_t* const e = (const uint64_t*)m_data + m_len;

    // static values needed for population count
    const uint64_t m1  = 0x5555555555555555ULL;
    const uint64_t m2  = 0x3333333333333333ULL;
    const uint64_t m4  = 0x0f0f0f0f0f0f0f0fULL;
    const uint64_t h01 = 0x0101010101010101ULL;

    while (p < e) {
        uint64_t x = *p ^ value;

        // population count
#if defined(WIN32) && defined(SSE42)
        x = _mm_popcnt_u64(x); // msvc sse4.2 intrinsic
#elif defined(GCC)
        x = __builtin_popcountll(x); // gcc intrinsic
#else
        x -= (x >> 1) & m1;
        x = (x & m2) + ((x >> 2) & m2);
        x = (x + (x >> 4)) & m4;
        x = (x * h01)>>56;
#endif

        if (x < maxdist) {
            const size_t pos = p - (const uint64_t*)m_data;
            result.AddPositiveLocal(offset + pos);
        }

        ++p;
    }
    */
}

size_t Array::GetByteSize(bool align) const {
    size_t len = CalcByteLen(m_len, m_width);
    if (align) {
        const size_t rest = (~len & 0x7)+1;
        if (rest < 8) len += rest; // 64bit blocks
    }
    return len;
}

size_t Array::CalcByteLen(size_t count, size_t width) const
{
    const size_t bits = (count * width);
    size_t bytes = (bits / 8) + 8; // add room for 8 byte header
    if (bits & 0x7) ++bytes;       // include partial bytes
    return bytes;
}

size_t Array::CalcItemCount(size_t bytes, size_t width) const
{
    if (width == 0) return (size_t)-1; // zero width gives infinite space

    const size_t bytes_data = bytes - 8; // ignore 8 byte header
    const size_t total_bits = bytes_data * 8;
    return total_bits / width;
}

bool Array::Copy(const Array& a)
{
    // Calculate size in bytes (plus a bit of extra room for expansion)
    size_t len = CalcByteLen(a.m_len, a.m_width);
    const size_t rest = (~len & 0x7)+1;
    if (rest < 8) len += rest; // 64bit blocks
    const size_t new_len = len + 64;

    // Create new copy of array
    const MemRef mref = m_alloc.Alloc(new_len);
    if (!mref.pointer) return false;
    memcpy(mref.pointer, a.m_data-8, len);

    // Clear old contents
    Destroy();

    // Update internal data
    UpdateRef(mref.ref);
    set_header_capacity(new_len); // uses m_data to find header, so m_data must be initialized correctly first

    // Copy sub-arrays as well
    if (m_hasRefs) {
        for (size_t i = 0; i < m_len; ++i) {
            const size_t ref = (size_t)Get(i);

            // null-refs signify empty sub-trees
            if (ref == 0) continue;

            // all refs are 64bit aligned, so the lowest bits
            // cannot be set. If they are it means that it should
            // not be interpreted as a ref
            if (ref & 0x1) continue;

            const Array sub(ref, NULL, 0, a.m_alloc);
            Array cp(m_alloc);
            cp.SetParent(this, i);
            cp.Copy(sub);
        }
    }

    return true;
}

bool Array::CopyOnWrite()
{
    if (!m_alloc.IsReadOnly(m_ref)) return true;

    // Calculate size in bytes (plus a bit of extra room for expansion)
    size_t len = CalcByteLen(m_len, m_width);
    const size_t rest = (~len & 0x7)+1;
    if (rest < 8) len += rest; // 64bit blocks
    const size_t new_len = len + 64;

    // Create new copy of array
    const MemRef mref = m_alloc.Alloc(new_len);
    if (!mref.pointer) return false;
    memcpy(mref.pointer, m_data-8, len);

    const size_t old_ref = m_ref;
    void* const  old_ptr = m_data - 8;

    // Update internal data
    m_ref = mref.ref;
    m_data = (unsigned char*)mref.pointer + 8;
    m_capacity = CalcItemCount(new_len, m_width);

    // Update capacity in header
    set_header_capacity(new_len); // uses m_data to find header, so m_data must be initialized correctly first

    update_ref_in_parent(mref.ref);

    // Mark original as deleted, so that the space can be reclaimed in
    // future commits, when no versions are using it anymore
    m_alloc.Free(old_ref, old_ptr);

    return true;
}

bool Array::Alloc(size_t count, size_t width)
{
    if (count > m_capacity || width != m_width) {
        const size_t len      = CalcByteLen(count, width);              // bytes needed
        const size_t capacity = m_capacity ? get_header_capacity() : 0; // bytes currently available
        size_t new_capacity   = capacity;

        if (len > capacity) {
            // Double to avoid too many reallocs
            new_capacity = capacity ? capacity * 2 : 128;
            if (new_capacity < len) {
                const size_t rest = (~len & 0x7)+1;
                new_capacity = len;
                if (rest < 8) new_capacity += rest; // 64bit align
            }

            // Allocate the space
            MemRef mref;
            if (m_data) mref = m_alloc.ReAlloc(m_ref, m_data-8, new_capacity);
            else mref = m_alloc.Alloc(new_capacity);

            if (!mref.pointer) return false;

            const bool isFirst = (capacity == 0);
            m_ref = mref.ref;
            m_data = (unsigned char*)mref.pointer + 8;

            // Create header
            if (isFirst) {
                // Note: Since the header layout contains unallocated
                // bit and/or bytes, it is important that we put the
                // entire 8 byte header into a well defined state
                // initially. Note also: The C++ standard does not
                // guarantee that int64_t is extactly 8 bytes wide. It
                // may be more, and it may be less. That is why we
                // need the statinc assert.
                TIGHTDB_STATIC_ASSERT(sizeof(int64_t) == 8,
                                      "Trouble if int64_t is not 8 bytes wide");
                *reinterpret_cast<int64_t*>(mref.pointer) = 0;
                set_header_isnode(m_isNode);
                set_header_hasrefs(m_hasRefs);
                set_header_wtype(GetWidthType());
                set_header_width(width);
            }
            set_header_capacity(new_capacity);

            update_ref_in_parent(mref.ref);
        }

        m_capacity = CalcItemCount(new_capacity, width);
        set_header_width(width);
    }

    // Update header
    set_header_len(count);

    return true;
}

void Array::SetWidth(size_t width)
{
    if (width == 0) {
        m_getter = &Array::Get_0b;
        m_setter = &Array::Set_0b;
        
        m_lbound = 0;
        m_ubound = 0;
    }
    else if (width == 1) {
        m_getter = &Array::Get_1b;
        m_setter = &Array::Set_1b;
        
        m_lbound = 0;
        m_ubound = 1;
    }
    else if (width == 2) {
        m_getter = &Array::Get_2b;
        m_setter = &Array::Set_2b;
        
        m_lbound = 0;
        m_ubound = 3;
    }
    else if (width == 4) {
        m_getter = &Array::Get_4b;
        m_setter = &Array::Set_4b;
        
        m_lbound = 0;
        m_ubound = 15;
    }
    else if (width == 8) {
        m_getter = &Array::Get_8b;
        m_setter = &Array::Set_8b;
        
        m_lbound = -0x80LL;
        m_ubound =  0x7FLL;
    }
    else if (width == 16) {
        m_getter = &Array::Get_16b;
        m_setter = &Array::Set_16b;
        
        m_lbound = -0x8000LL;
        m_ubound =  0x7FFFLL;
    }
    else if (width == 32) {
        m_getter = &Array::Get_32b;
        m_setter = &Array::Set_32b;
        
        m_lbound = -0x80000000LL;
        m_ubound =  0x7FFFFFFFLL;
    }
    else if (width == 64) {
        m_getter = &Array::Get_64b;
        m_setter = &Array::Set_64b;
        
        m_lbound = -0x8000000000000000LL;
        m_ubound =  0x7FFFFFFFFFFFFFFFLL;
    }
    else {
        assert(false);
    }

    m_width = width;
}

template <size_t w>int64_t Array::Get(size_t ndx) const
{
    if(w == 0) return Get_0b(ndx);
    else if(w == 1) return Get_1b(ndx);
    else if(w == 2) return Get_2b(ndx);
    else if(w == 4) return Get_4b(ndx);
    else if(w == 8) return Get_8b(ndx);
    else if(w == 16) return Get_16b(ndx);
    else if(w == 32) return Get_32b(ndx);
    else if(w == 64) return Get_64b(ndx);
}

int64_t Array::Get_0b(size_t) const
{
    return 0;
}

int64_t Array::Get_1b(size_t ndx) const
{
    const size_t offset = ndx >> 3;
    return (m_data[offset] >> (ndx & 7)) & 0x01;
}

int64_t Array::Get_2b(size_t ndx) const
{
    const size_t offset = ndx >> 2;
    return (m_data[offset] >> ((ndx & 3) << 1)) & 0x03;
}

int64_t Array::Get_4b(size_t ndx) const
{
    const size_t offset = ndx >> 1;
    return (m_data[offset] >> ((ndx & 1) << 2)) & 0x0F;
}

int64_t Array::Get_8b(size_t ndx) const
{
    return *((const signed char*)(m_data + ndx));
}

int64_t Array::Get_16b(size_t ndx) const
{
    const size_t offset = ndx * 2;
    return *(const int16_t*)(m_data + offset);
}

int64_t Array::Get_32b(size_t ndx) const
{
    const size_t offset = ndx * 4;
    return *(const int32_t*)(m_data + offset);
}

int64_t Array::Get_64b(size_t ndx) const
{
    const size_t offset = ndx * 8;
    return *(const int64_t*)(m_data + offset);
}

void Array::Set_0b(size_t, int64_t) {}

void Array::Set_1b(size_t ndx, int64_t value)
{
    const size_t offset = ndx >> 3;
    ndx &= 7;

    uint8_t* p = &m_data[offset];
    *p = (*p &~ (1 << ndx)) | (uint8_t)((value & 1) << ndx);
}

void Array::Set_2b(size_t ndx, int64_t value)
{
    const size_t offset = ndx >> 2;
    const uint8_t n = (uint8_t)((ndx & 3) << 1);

    uint8_t* p = &m_data[offset];
    *p = (*p &~ (0x03 << n)) | (uint8_t)((value & 0x03) << n);
}

void Array::Set_4b(size_t ndx, int64_t value)
{
    const size_t offset = ndx >> 1;
    const uint8_t n = (uint8_t)((ndx & 1) << 2);

    uint8_t* p = &m_data[offset];
    *p = (*p &~ (0x0F << n)) | (uint8_t)((value & 0x0F) << n);
}

void Array::Set_8b(size_t ndx, int64_t value)
{
    *((char*)m_data + ndx) = (char)value;
}

void Array::Set_16b(size_t ndx, int64_t value)
{
    const size_t offset = ndx * 2;
    *(int16_t*)(m_data + offset) = (int16_t)value;
}

void Array::Set_32b(size_t ndx, int64_t value)
{
    const size_t offset = ndx * 4;
    *(int32_t*)(m_data + offset) = (int32_t)value;
}

void Array::Set_64b(size_t ndx, int64_t value)
{
    const size_t offset = ndx * 8;
    *(int64_t*)(m_data + offset) = value;
}
#ifdef __MSVCRT__
#pragma warning (disable : 4127)
#endif
template <size_t w> void Array::Set(size_t ndx, int64_t value)
{
    if(w == 0) return Set_0b(ndx, value);
    else if(w == 1) Set_1b(ndx, value);
    else if(w == 2) Set_2b(ndx, value);
    else if(w == 4) Set_4b(ndx, value);
    else if(w == 8) Set_8b(ndx, value);
    else if(w == 16) Set_16b(ndx, value);
    else if(w == 32) Set_32b(ndx, value);
    else if(w == 64) Set_64b(ndx, value);
}


// Sort array.
void Array::sort()
{
    TEMPEX(sort, ());
}

// Find max and min value, but break search if difference exceeds 'maxdiff' (in which case *min and *max is set to 0)
// Useful for counting-sort functions
template <size_t w>bool Array::MinMax(size_t from, size_t to, uint64_t maxdiff, int64_t *min, int64_t *max)
{
    int64_t min2;
    int64_t max2;
    size_t t;

    max2 = Get<w>(from);
    min2 = max2;

    for(t = from + 1; t < to; t++) {
        int64_t v = Get<w>(t);
        // Utilizes that range test is only needed if max2 or min2 were changed
        if(v < min2) {
            min2 = v;
            if((uint64_t)(max2 - min2) > maxdiff)
                break;
        }
        else if(v > max2) {
            max2 = v;
            if((uint64_t)(max2 - min2) > maxdiff)
                break;
        }
    }

    if(t < to) {
        *max = 0;
        *min = 0;
        return false;
    }
    else {
        *max = max2;
        *min = min2;
        return true;
    }
}

// Take index pointers to elements as argument and sort the pointers according to values they point at. Leave m_array untouched. The ref array
// is allowed to contain fewer elements than m_array.
void Array::ReferenceSort(Array& ref)
{
    TEMPEX(ReferenceSort, (ref));
}

template <size_t w>void Array::ReferenceSort(Array& ref)
{
    if(m_len < 2)
        return;

    int64_t min;
    int64_t max;

    // in avg case QuickSort is O(n*log(n)) and CountSort O(n + range), and memory usage is sizeof(size_t)*range for CountSort.
    // So we chose range < m_len as treshold for deciding which to use

    // If range isn't suited for CountSort, it's *probably* discovered very early, within first few values, in most practical cases,
    // and won't add much wasted work. Max wasted work is O(n) which isn't much compared to QuickSort.

//  bool b = MinMax<w>(0, m_len, m_len, &min, &max); // auto detect
//  bool b = MinMax<w>(0, m_len, -1, &min, &max); // force count sort
    bool b = MinMax<w>(0, m_len, 0, &min, &max); // force quicksort

    if(b) {
        Array res;
        Array count;

        // Todo, Preset crashes for unknown reasons but would be faster.
//      res.Preset(0, m_len, m_len);
//      count.Preset(0, m_len, max - min + 1);

        for(int64_t t = 0; t < max - min + 1; t++)
            count.add(0);

        // Count occurences of each value
        for(size_t t = 0; t < m_len; t++) {
            size_t i = TO_REF(Get<w>(t) - min);
            count.Set(i, count.Get(i) + 1);
        }

        // Accumulate occurences
        for(size_t t = 1; t < count.Size(); t++) {
            count.Set(t, count.Get(t) + count.Get(t - 1));
        }

        for(size_t t = 0; t < m_len; t++)
            res.add(0);

        for(size_t t = m_len; t > 0; t--) {
            size_t v = TO_REF(Get<w>(t - 1) - min);
            size_t i = count.GetAsRef(v);
            count.Set(v, count.Get(v) - 1);
            res.Set(i - 1, ref.Get(t - 1));
        }

        // Copy result into ref
        for(size_t t = 0; t < res.Size(); t++)
            ref.Set(t, res.Get(t));

        res.Destroy();
        count.Destroy();
    }
    else {
        ReferenceQuickSort(ref);
    }
}

// Sort array
template <size_t w> void Array::sort()
{
    if(m_len < 2)
        return;

    size_t lo = 0;
    size_t hi = m_len - 1;
    std::vector<size_t> count;
    int64_t min;
    int64_t max;
    bool b = false;

    // in avg case QuickSort is O(n*log(n)) and CountSort O(n + range), and memory usage is sizeof(size_t)*range for CountSort.
    // Se we chose range < m_len as treshold for deciding which to use
    if(m_width <= 8) {
        max = m_ubound;
        min = m_lbound;
        b = true;
    }
    else {
        // If range isn't suited for CountSort, it's *probably* discovered very early, within first few values,
        // in most practical cases, and won't add much wasted work. Max wasted work is O(n) which isn't much
        // compared to QuickSort.
        b = MinMax<w>(lo, hi + 1, m_len, &min, &max);
    }

    if(b) {
        for(int64_t t = 0; t < max - min + 1; t++)
            count.push_back(0);

        // Count occurences of each value
        for(size_t t = lo; t <= hi; t++) {
            size_t i = TO_REF(Get<w>(t) - min);
            count[i]++;
        }

        // Overwrite original array with sorted values
        size_t dst = 0;
        for(int64_t i = 0; i < max - min + 1; i++) {
            size_t c = count[(unsigned int)i];
            for(size_t j = 0; j < c; j++) {
                Set<w>(dst, i + min);
                dst++;
            }
        }
    }
    else {
        QuickSort(lo, hi);
    }

    return;
}

void Array::ReferenceQuickSort(Array& ref)
{
    TEMPEX(ReferenceQuickSort, (0, m_len - 1, ref));
}



template<size_t w> void Array::ReferenceQuickSort(size_t lo, size_t hi, Array& ref)
{
    // Quicksort based on
    // http://www.inf.fh-flensburg.de/lang/algorithmen/sortieren/quick/quicken.htm
    int i = (int)lo;
    int j = (int)hi;

    /*
    // Swap both values and references but lookup values directly: 2.85 sec
    // comparison element x
    const size_t ndx = (lo + hi)/2;
    const int64_t x = (size_t)Get(ndx);

    // partition
    do {
        while (Get(i) < x) i++;
        while (Get(j) > x) j--;
        if (i <= j) {
            size_t h = ref.Get(i);
            ref.Set(i, ref.Get(j));
            ref.Set(j, h);
        //  h = Get(i);
        //  Set(i, Get(j));
        //  Set(j, h);
            i++; j--;
        }
    } while (i <= j);
*/

    // Lookup values indirectly through references, but swap only references: 2.60 sec
    // Templated get/set: 2.40 sec (todo, enable again)
    // comparison element x
    const size_t ndx = (lo + hi)/2;
    const size_t target_ndx = (size_t)ref.Get(ndx);
    const int64_t x = Get(target_ndx);

    // partition
    do {
        while (Get((size_t)ref.Get(i)) < x) ++i;
        while (Get((size_t)ref.Get(j)) > x) --j;
        if (i <= j) {
            const size_t h = (size_t)ref.Get(i);
            ref.Set(i, ref.Get(j));
            ref.Set(j, h);
            ++i; --j;
        }
    } while (i <= j);

    //  recursion
    if ((int)lo < j) ReferenceQuickSort<w>(lo, j, ref);
    if (i < (int)hi) ReferenceQuickSort<w>(i, hi, ref);
}


void Array::QuickSort(size_t lo, size_t hi)
{
    TEMPEX(QuickSort, (lo, hi);)
}

template<size_t w> void Array::QuickSort(size_t lo, size_t hi) {
    // Quicksort based on
    // http://www.inf.fh-flensburg.de/lang/algorithmen/sortieren/quick/quicken.htm
    int i = (int)lo;
    int j = (int)hi;

    // comparison element x
    const size_t ndx = (lo + hi)/2;
    const int64_t x = Get(ndx);

    // partition
    do {
        while (Get(i) < x) ++i;
        while (Get(j) > x) --j;
        if (i <= j) {
            const int64_t h = Get(i);
            Set(i, Get(j));
            Set(j, h);
            ++i; --j;
        }
    } while (i <= j);

    //  recursion
    if ((int)lo < j) QuickSort(lo, j);
    if (i < (int)hi) QuickSort(i, hi);
}

std::vector<int64_t> Array::ToVector(void) const {
    std::vector<int64_t> v;
    const size_t count = Size();
    for(size_t t = 0; t < count; ++t)
        v.push_back(Get(t));
    return v;
}

#ifdef _DEBUG
#include "stdio.h"

bool Array::Compare(const Array& c) const
{
    if (c.Size() != Size()) return false;

    for (size_t i = 0; i < Size(); ++i) {
        if (Get(i) != c.Get(i)) return false;
    }

    return true;
}

void Array::Print() const
{
    cout << hex << GetRef() << dec << ": (" << Size() << ") ";
    for (size_t i = 0; i < Size(); ++i) {
        if (i) cout << ", ";
        cout << Get(i);
    }
    cout << "\n";
}

void Array::Verify() const
{
    assert(!IsValid() || (m_width == 0 || m_width == 1 || m_width == 2 || m_width == 4 ||
                          m_width == 8 || m_width == 16 || m_width == 32 || m_width == 64));

    // Check that parent is set correctly
    if (!m_parent) return;

    const size_t ref_in_parent = m_parent->get_child_ref(m_parentNdx);
    assert(ref_in_parent == (IsValid() ? m_ref : 0));
}

void Array::ToDot(std::ostream& out, const char* title) const
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
    if (m_isNode) out << "IsNode<BR/>";
    if (m_hasRefs) out << "HasRefs<BR/>";
    out << "</FONT></TD>" << std::endl;

    // Values
    for (size_t i = 0; i < m_len; ++i) {
        const int64_t v =  Get(i);
        if (m_hasRefs) {
            // zero-refs and refs that are not 64-aligned do not point to sub-trees
            if (v == 0) out << "<TD>none";
            else if (v & 0x1) out << "<TD BGCOLOR=\"grey90\">" << (v >> 1);
            else out << "<TD PORT=\"" << i << "\">";
        }
        else out << "<TD>" << v;
        out << "</TD>" << std::endl;
    }

    out << "</TR></TABLE>>];" << std::endl;
    if (title) out << "}" << std::endl;

    if (m_hasRefs) {
        for (size_t i = 0; i < m_len; ++i) {
            const int64_t target = Get(i);
            if (target == 0 || target & 0x1) continue; // zero-refs and refs that are not 64-aligned do not point to sub-trees

            out << "n" << std::hex << ref << std::dec << ":" << i;
            out << " -> n" << std::hex << target << std::dec << std::endl;
        }
    }

    out << std::endl;
}

void Array::Stats(MemStats& stats) const
{
    const MemStats m(m_capacity, CalcByteLen(m_len, m_width), 1);
    stats.add(m);

    // Add stats for all sub-arrays
    if (m_hasRefs) {
        for (size_t i = 0; i < m_len; ++i) {
            const size_t ref = GetAsRef(i);
            if (ref == 0 || ref & 0x1) continue; // zero-refs and refs that are not 64-aligned do not point to sub-trees

            const Array sub(ref, NULL, 0, GetAllocator());
            sub.Stats(stats);
        }
    }

}

#endif //_DEBUG

}


namespace {

// Direct access methods

// Pre-declarations
bool get_header_isnode_direct(const uint8_t* const header);
bool get_header_hasrefs_direct(const uint8_t* const header);
unsigned int get_header_width_direct(const uint8_t* const header);
size_t get_header_len_direct(const uint8_t* const header);
int64_t GetDirect(const char* const data, size_t width, const size_t ndx);
size_t FindPosDirect(const uint8_t* const header, const char* const data, const size_t width, const int64_t target);
template<size_t width> size_t FindPosDirectImp(const uint8_t* const header, const char* const data, const int64_t target);

bool get_header_isnode_direct(const uint8_t* const header)
{
    return (header[0] & 0x80) != 0;
}

bool get_header_hasrefs_direct(const uint8_t* const header)
{
    return (header[0] & 0x40) != 0;
}

unsigned int get_header_width_direct(const uint8_t* const header)
{
    return (1 << (header[0] & 0x07)) >> 1;
}

size_t get_header_len_direct(const uint8_t* const header)
{
    return (header[1] << 16) + (header[2] << 8) + header[3];
}

template<size_t w> int64_t GetDirect(const char* const data, const size_t ndx);

template<> int64_t GetDirect<0>(const char* const, const size_t)
{
    return 0;
}
template<> int64_t GetDirect<1>(const char* const data, const size_t ndx)
{
    const size_t offset = ndx >> 3;
    return (data[offset] >> (ndx & 7)) & 0x01;
}
template<> int64_t GetDirect<2>(const char* const data, const size_t ndx)
{
    const size_t offset = ndx >> 2;
    return (data[offset] >> ((ndx & 3) << 1)) & 0x03;
}
template<> int64_t GetDirect<4>(const char* const data, const size_t ndx)
{
    const size_t offset = ndx >> 1;
    return (data[offset] >> ((ndx & 1) << 2)) & 0x0F;
}
template<> int64_t GetDirect<8>(const char* const data, const size_t ndx)
{
    return *((const signed char*)(data + ndx));
}
template<> int64_t GetDirect<16>(const char* const data, const size_t ndx)
{
    const size_t offset = ndx * 2;
    return *(const int16_t*)(data + offset);
}
template<> int64_t GetDirect<32>(const char* const data, const size_t ndx)
{
    const size_t offset = ndx * 4;
    return *(const int32_t*)(data + offset);
}
template<> int64_t GetDirect<64>(const char* const data, const size_t ndx)
{
    const size_t offset = ndx * 8;
    return *(const int64_t*)(data + offset);
}

int64_t GetDirect(const char* const data, size_t width, const size_t ndx)
{
    switch (width) {
        case  0: return GetDirect<0>(data, ndx);
        case  1: return GetDirect<1>(data, ndx);
        case  2: return GetDirect<2>(data, ndx);
        case  4: return GetDirect<4>(data, ndx);
        case  8: return GetDirect<8>(data, ndx);
        case 16: return GetDirect<16>(data, ndx);
        case 32: return GetDirect<32>(data, ndx);
        case 64: return GetDirect<64>(data, ndx);
        default:
            assert(false);
            return 0;
    }
}

size_t FindPosDirect(const uint8_t* const header, const char* const data, const size_t width,
                     const int64_t target)
{
    switch (width) {
        case  0: return 0;
        case  1: return FindPosDirectImp<1>(header, data, target);
        case  2: return FindPosDirectImp<2>(header, data, target);
        case  4: return FindPosDirectImp<4>(header, data, target);
        case  8: return FindPosDirectImp<8>(header, data, target);
        case 16: return FindPosDirectImp<16>(header, data, target);
        case 32: return FindPosDirectImp<32>(header, data, target);
        case 64: return FindPosDirectImp<64>(header, data, target);
        default:
            assert(false);
            return 0;
    }
}

template<size_t width> size_t FindPosDirectImp(const uint8_t* const header, const char* const data,
                                               const int64_t target)
{
    const size_t len = get_header_len_direct(header);

    int low = -1;
    int high = (int)len;

    // Binary search based on:
    // http://www.tbray.org/ongoing/When/200x/2003/03/22/Binary
    // Finds position of largest value SMALLER than the target (for lookups in
    // nodes)
    while (high - low > 1) {
        const size_t probe = ((unsigned int)low + (unsigned int)high) >> 1;
        const int64_t v = GetDirect<width>(data, probe);

        if (v > target) high = (int)probe;
        else            low = (int)probe;
    }
    if (high == (int)len) return (size_t)-1;
    else return (size_t)high;
}

}


namespace tightdb {

// Get containing array block direct through column b-tree
// without instatiating any Arrays.
void Array::GetBlock(size_t ndx, Array& arr, size_t& off) const
{
    char* data = (char*)m_data;
    uint8_t* header = (uint8_t*)data-8;
    size_t width  = m_width;
    bool isNode   = m_isNode;
    size_t offset = 0;
    
    while (1) {
        if (isNode) {
            // Get subnode table
            const size_t ref_offsets = GetDirect(data, width, 0);
            const size_t ref_refs    = GetDirect(data, width, 1);
            
            // Find the subnode containing the item
            const uint8_t* const offsets_header = (const uint8_t*)m_alloc.Translate(ref_offsets);
            const char* const offsets_data = (const char*)offsets_header + 8;
            const size_t offsets_width  = get_header_width_direct(offsets_header);
            const size_t node_ndx = FindPosDirect(offsets_header, offsets_data, offsets_width, ndx);
            
            // Calc index in subnode
            const size_t localoffset = node_ndx ? TO_REF(GetDirect(offsets_data, offsets_width, node_ndx-1)) : 0;
            ndx -= localoffset; // local index
            offset += localoffset;
            
            // Get ref to array
            const uint8_t* const refs_header = (const uint8_t*)m_alloc.Translate(ref_refs);
            const char* const refs_data = (const char*)refs_header + 8;
            const size_t refs_width  = get_header_width_direct(refs_header);
            const size_t ref = GetDirect(refs_data, refs_width, node_ndx);
            
            // Set vars for next iteration
            header = (uint8_t*)m_alloc.Translate(ref);
            data   = (char*)header + 8;
            width  = get_header_width_direct(header);
            isNode = get_header_isnode_direct(header);
        }
        else {
            arr.CreateFromHeaderDirect(header);
            off = offset;
            return;
        }
    }
}


// Get value direct through column b-tree without instatiating any Arrays.
int64_t Array::ColumnGet(size_t ndx) const
{
    const char* data   = (const char*)m_data;
    const uint8_t* header;
    size_t width = m_width;
    bool isNode = m_isNode;

    while (1) {
        if (isNode) {
            // Get subnode table
            const size_t ref_offsets = GetDirect(data, width, 0);
            const size_t ref_refs    = GetDirect(data, width, 1);

            // Find the subnode containing the item
            const uint8_t* const offsets_header = (const uint8_t*)m_alloc.Translate(ref_offsets);
            const char* const offsets_data = (const char*)offsets_header + 8;
            const size_t offsets_width  = get_header_width_direct(offsets_header);
            const size_t node_ndx = FindPosDirect(offsets_header, offsets_data, offsets_width, ndx);

            // Calc index in subnode
            const size_t offset = node_ndx ? TO_REF(GetDirect(offsets_data, offsets_width, node_ndx-1)) : 0;
            ndx = ndx - offset; // local index

            // Get ref to array
            const uint8_t* const refs_header = (const uint8_t*)m_alloc.Translate(ref_refs);
            const char* const refs_data = (const char*)refs_header + 8;
            const size_t refs_width  = get_header_width_direct(refs_header);
            const size_t ref = GetDirect(refs_data, refs_width, node_ndx);

            // Set vars for next iteration
            header = (const uint8_t*)m_alloc.Translate(ref);
            data   = (const char*)header + 8;
            width  = get_header_width_direct(header);
            isNode = get_header_isnode_direct(header);
        }
        else {
            return GetDirect(data, width, ndx);
        }
    }
}

const char* Array::ColumnStringGet(size_t ndx) const
{
    const char* data   = (const char*)m_data;
    const uint8_t* header = m_data - 8;
    size_t width = m_width;
    bool isNode = m_isNode;

    while (1) {
        if (isNode) {
            // Get subnode table
            const size_t ref_offsets = GetDirect(data, width, 0);
            const size_t ref_refs    = GetDirect(data, width, 1);

            // Find the subnode containing the item
            const uint8_t* const offsets_header = (const uint8_t*)m_alloc.Translate(ref_offsets);
            const char* const offsets_data = (const char*)offsets_header + 8;
            const size_t offsets_width  = get_header_width_direct(offsets_header);
            const size_t node_ndx = FindPosDirect(offsets_header, offsets_data, offsets_width, ndx);

            // Calc index in subnode
            const size_t offset = node_ndx ? TO_REF(GetDirect(offsets_data, offsets_width, node_ndx-1)) : 0;
            ndx = ndx - offset; // local index

            // Get ref to array
            const uint8_t* const refs_header = (const uint8_t*)m_alloc.Translate(ref_refs);
            const char* const refs_data = (const char*)refs_header + 8;
            const size_t refs_width  = get_header_width_direct(refs_header);
            const size_t ref = GetDirect(refs_data, refs_width, node_ndx);

            // Set vars for next iteration
            header = (const uint8_t*)m_alloc.Translate(ref);
            data   = (const char*)header + 8;
            width  = get_header_width_direct(header);
            isNode = get_header_isnode_direct(header);
        }
        else {
            const bool hasRefs = get_header_hasrefs_direct(header);
            if (hasRefs) {
                // long strings
                const size_t ref_offsets = GetDirect(data, width, 0);
                const size_t ref_blob    = GetDirect(data, width, 1);

                size_t offset = 0;
                if (ndx) {
                    const uint8_t* const offsets_header = (const uint8_t*)m_alloc.Translate(ref_offsets);
                    const char* const offsets_data = (const char*)offsets_header + 8;
                    const size_t offsets_width  = get_header_width_direct(offsets_header);

                    offset = GetDirect(offsets_data, offsets_width, ndx-1);
                }

                const uint8_t* const blob_header = (const uint8_t*)m_alloc.Translate(ref_blob);
                const char* const blob_data = (const char*)blob_header + 8;

                return (const char*)blob_data + offset;
            }
            else {
                // short strings
                if (width == 0) return "";
                else return (const char*)(data + (ndx * width));
            }
        }
    }
}

// Find value direct through column b-tree without instatiating any Arrays.
size_t Array::ColumnFind(int64_t target, size_t ref, Array& cache) const
{
    uint8_t* const header = (uint8_t*)m_alloc.Translate(ref);
    const bool isNode = get_header_isnode_direct(header);
        
    if (isNode) {
        const char* const data = (const char*)header + 8;
        const size_t width = get_header_width_direct(header);
        
        // Get subnode table
        const size_t ref_offsets = GetDirect(data, width, 0);
        const size_t ref_refs    = GetDirect(data, width, 1);
        
        const uint8_t* const offsets_header = (const uint8_t*)m_alloc.Translate(ref_offsets);
        const char* const offsets_data = (const char*)offsets_header + 8;
        const size_t offsets_width  = get_header_width_direct(offsets_header);
        const size_t offsets_len = get_header_len_direct(offsets_header);
        
        const uint8_t* const refs_header = (const uint8_t*)m_alloc.Translate(ref_refs);
        const char* const refs_data = (const char*)refs_header + 8;
        const size_t refs_width  = get_header_width_direct(refs_header);
        
        // Iterate over nodes until we find a match
        size_t offset = 0;
        for (size_t i = 0; i < offsets_len; ++i) {
            const size_t ref = GetDirect(refs_data, refs_width, i);
            const size_t result = ColumnFind(target, ref, cache);
            if (result != not_found)
                return offset + result;
            
            const size_t off = GetDirect(offsets_data, offsets_width, i);
            offset = off;
        }
        
        // if we get to here there is no match
        return not_found;
    }
    else {
        cache.CreateFromHeaderDirect(header);
        return cache.CompareEquality<true>(target, 0, -1);
    }
}

} //namespace tightdb
