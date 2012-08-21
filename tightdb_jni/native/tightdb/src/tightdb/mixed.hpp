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
#ifndef TIGHTDB_MIXED_HPP
#define TIGHTDB_MIXED_HPP

#ifndef _MSC_VER
#include <stdint.h> // int64_t - not part of C++03, not even required by C++11 to be present (see C++11 section 18.4.1)
#else
#include <win32/stdint.h>
#endif

#include <cstddef> // size_t
#include <cstring>

#include <tightdb/assert.hpp>
#include <tightdb/meta.hpp>
#include <tightdb/column_type.hpp>
#include <tightdb/date.hpp>
#include <tightdb/binary_data.hpp>

namespace tightdb {


class Mixed {
public:
    Mixed(int64_t v)     {m_type = COLUMN_TYPE_INT;    m_int  = v;}
    Mixed(bool v)        {m_type = COLUMN_TYPE_BOOL;   m_bool = v;}
    Mixed(Date v)        {m_type = COLUMN_TYPE_DATE;   m_date = v.get_date();}
    Mixed(const char* v) {m_type = COLUMN_TYPE_STRING; m_str  = v;}
    Mixed(BinaryData v)  {m_type = COLUMN_TYPE_BINARY; m_str = v.pointer; m_len = v.len;}
    Mixed(const char* v, std::size_t len) {m_type = COLUMN_TYPE_BINARY; m_str = v; m_len = len;}

    struct subtable_tag {};
    Mixed(subtable_tag): m_type(COLUMN_TYPE_TABLE) {}

    ColumnType get_type() const {return m_type;}

    int64_t     get_int()    const;
    bool        get_bool()   const;
    std::time_t get_date()   const;
    const char* get_string() const;
    BinaryData  get_binary() const;

    template<class Ch, class Tr>
    friend std::basic_ostream<Ch, Tr>& operator<<(std::basic_ostream<Ch, Tr>&, const Mixed&);

private:
    ColumnType m_type;
    union {
        int64_t m_int;
        bool    m_bool;
        std::time_t  m_date;
        const char* m_str;
    };
    std::size_t m_len;
};

// Note: We cannot compare two mixed values, since when the type of
// both would be COLUMN_TYPE_TABLE, we would have to compare the two
// tables, but the mixed values do not provide access to those tables.

// Note: The mixed values are specified as Wrap<Mixed>. If they were
// not, these operators would apply to simple comparisons, such as int
// vs int64_t, and cause ambiguity. This is because the constructors
// of Mixed are not explicit.

// Compare mixed with integer
template<class T> bool operator==(Wrap<Mixed>, const T&);
template<class T> bool operator!=(Wrap<Mixed>, const T&);
template<class T> bool operator==(const T&, Wrap<Mixed>);
template<class T> bool operator!=(const T&, Wrap<Mixed>);

// Compare mixed with boolean
bool operator==(Wrap<Mixed>, bool);
bool operator!=(Wrap<Mixed>, bool);
bool operator==(bool, Wrap<Mixed>);
bool operator!=(bool, Wrap<Mixed>);

// Compare mixed with date
bool operator==(Wrap<Mixed>, Date);
bool operator!=(Wrap<Mixed>, Date);
bool operator==(Date, Wrap<Mixed>);
bool operator!=(Date, Wrap<Mixed>);

// Compare mixed with zero-terminated string
bool operator==(Wrap<Mixed>, const char*);
bool operator!=(Wrap<Mixed>, const char*);
bool operator==(const char*, Wrap<Mixed>);
bool operator!=(const char*, Wrap<Mixed>);
bool operator==(Wrap<Mixed>, char*);
bool operator!=(Wrap<Mixed>, char*);
bool operator==(char*, Wrap<Mixed>);
bool operator!=(char*, Wrap<Mixed>);

// Compare mixed with binary data
bool operator==(Wrap<Mixed>, BinaryData);
bool operator!=(Wrap<Mixed>, BinaryData);
bool operator==(BinaryData, Wrap<Mixed>);
bool operator!=(BinaryData, Wrap<Mixed>);




// Implementation:

inline int64_t Mixed::get_int() const
{
    TIGHTDB_ASSERT(m_type == COLUMN_TYPE_INT);
    return m_int;
}

inline bool Mixed::get_bool() const
{
    TIGHTDB_ASSERT(m_type == COLUMN_TYPE_BOOL);
    return m_bool;
}

inline std::time_t Mixed::get_date() const
{
    TIGHTDB_ASSERT(m_type == COLUMN_TYPE_DATE);
    return m_date;
}

inline const char* Mixed::get_string() const
{
    TIGHTDB_ASSERT(m_type == COLUMN_TYPE_STRING);
    return m_str;
}

inline BinaryData Mixed::get_binary() const
{
    TIGHTDB_ASSERT(m_type == COLUMN_TYPE_BINARY);
    return BinaryData(m_str, m_len);
}

template<class Ch, class Tr>
inline std::basic_ostream<Ch, Tr>& operator<<(std::basic_ostream<Ch, Tr>& out, const Mixed& m)
{
    out << "Mixed(";
    switch (m.m_type) {
    case COLUMN_TYPE_INT: out << m.m_int; break;
    case COLUMN_TYPE_BOOL: out << m.m_bool; break;
    case COLUMN_TYPE_DATE: out << Date(m.m_date); break;
    case COLUMN_TYPE_STRING: out << m.m_str; break;
    case COLUMN_TYPE_BINARY: out << BinaryData(m.m_str, m.m_len); break;
    case COLUMN_TYPE_TABLE: out << "subtable"; break;
    default: TIGHTDB_ASSERT(false); break;
    }
    out << ")";
    return out;
}


// Compare mixed with integer

template<class T> inline bool operator==(Wrap<Mixed> a, const T& b)
{
    return Mixed(a).get_type() == COLUMN_TYPE_INT && Mixed(a).get_int() == b;
}

template<class T> inline bool operator!=(Wrap<Mixed> a, const T& b)
{
    return Mixed(a).get_type() == COLUMN_TYPE_INT && Mixed(a).get_int() != b;
}

template<class T> inline bool operator==(const T& a, Wrap<Mixed> b)
{
    return Mixed(b).get_type() == COLUMN_TYPE_INT && a == Mixed(b).get_int();
}

template<class T> inline bool operator!=(const T& a, Wrap<Mixed> b)
{
    return Mixed(b).get_type() == COLUMN_TYPE_INT && a != Mixed(b).get_int();
}


// Compare mixed with boolean

inline bool operator==(Wrap<Mixed> a, bool b)
{
    return Mixed(a).get_type() == COLUMN_TYPE_BOOL && Mixed(a).get_bool() == b;
}

inline bool operator!=(Wrap<Mixed> a, bool b)
{
    return Mixed(a).get_type() == COLUMN_TYPE_BOOL && Mixed(a).get_bool() != b;
}

inline bool operator==(bool a, Wrap<Mixed> b)
{
    return Mixed(b).get_type() == COLUMN_TYPE_BOOL && a == Mixed(b).get_bool();
}

inline bool operator!=(bool a, Wrap<Mixed> b)
{
    return Mixed(b).get_type() == COLUMN_TYPE_BOOL && a != Mixed(b).get_bool();
}


// Compare mixed with date

inline bool operator==(Wrap<Mixed> a, Date b)
{
    return Mixed(a).get_type() == COLUMN_TYPE_DATE && Date(Mixed(a).get_date()) == b;
}

inline bool operator!=(Wrap<Mixed> a, Date b)
{
    return Mixed(a).get_type() == COLUMN_TYPE_DATE && Date(Mixed(a).get_date()) != b;
}

inline bool operator==(Date a, Wrap<Mixed> b)
{
    return Mixed(b).get_type() == COLUMN_TYPE_DATE && a == Date(Mixed(b).get_date());
}

inline bool operator!=(Date a, Wrap<Mixed> b)
{
    return Mixed(b).get_type() == COLUMN_TYPE_DATE && a != Date(Mixed(b).get_date());
}


// Compare mixed with zero-terminated string

inline bool operator==(Wrap<Mixed> a, const char* b)
{
    return Mixed(a).get_type() == COLUMN_TYPE_STRING && std::strcmp(Mixed(a).get_string(), b) == 0;
}

inline bool operator!=(Wrap<Mixed> a, const char* b)
{
    return Mixed(a).get_type() == COLUMN_TYPE_STRING && std::strcmp(Mixed(a).get_string(), b) != 0;
}

inline bool operator==(const char* a, Wrap<Mixed> b)
{
    return Mixed(b).get_type() == COLUMN_TYPE_STRING && std::strcmp(a, Mixed(b).get_string()) == 0;
}

inline bool operator!=(const char* a, Wrap<Mixed> b)
{
    return Mixed(b).get_type() == COLUMN_TYPE_STRING && std::strcmp(a, Mixed(b).get_string()) != 0;
}

inline bool operator==(Wrap<Mixed> a, char* b)
{
    return Mixed(a).get_type() == COLUMN_TYPE_STRING && std::strcmp(Mixed(a).get_string(), b) == 0;
}

inline bool operator!=(Wrap<Mixed> a, char* b)
{
    return Mixed(a).get_type() == COLUMN_TYPE_STRING && std::strcmp(Mixed(a).get_string(), b) != 0;
}

inline bool operator==(char* a, Wrap<Mixed> b)
{
    return Mixed(b).get_type() == COLUMN_TYPE_STRING && std::strcmp(a, Mixed(b).get_string()) == 0;
}

inline bool operator!=(char* a, Wrap<Mixed> b)
{
    return Mixed(b).get_type() == COLUMN_TYPE_STRING && std::strcmp(a, Mixed(b).get_string()) != 0;
}


// Compare mixed with binary data

inline bool operator==(Wrap<Mixed> a, BinaryData b)
{
    return Mixed(a).get_type() == COLUMN_TYPE_BINARY && Mixed(a).get_binary() == b;
}

inline bool operator!=(Wrap<Mixed> a, BinaryData b)
{
    return Mixed(a).get_type() == COLUMN_TYPE_BINARY && Mixed(a).get_binary() != b;
}

inline bool operator==(BinaryData a, Wrap<Mixed> b)
{
    return Mixed(b).get_type() == COLUMN_TYPE_BINARY && a == Mixed(b).get_binary();
}

inline bool operator!=(BinaryData a, Wrap<Mixed> b)
{
    return Mixed(b).get_type() == COLUMN_TYPE_BINARY && a != Mixed(b).get_binary();
}


} // namespace tightdb

#endif // TIGHTDB_MIXED_HPP
