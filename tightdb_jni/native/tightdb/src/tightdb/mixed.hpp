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
#include <stdint.h> // int64_t - not part of C++03
#else
#include <win32/stdint.h>
#endif

#include <cassert>
#include <cstddef> // size_t

#include "column_type.hpp"
#include "date.hpp"
#include "binary_data.hpp"

namespace tightdb {


class Mixed {
public:
    explicit Mixed(ColumnType v)
    {
        assert(v == COLUMN_TYPE_TABLE);
        static_cast<void>(v);
        m_type = COLUMN_TYPE_TABLE;
    }

    Mixed(bool v)        {m_type = COLUMN_TYPE_BOOL;   m_bool = v;}
    Mixed(Date v)        {m_type = COLUMN_TYPE_DATE;   m_date = v.get_date();}
    Mixed(int64_t v)     {m_type = COLUMN_TYPE_INT;    m_int  = v;}
    Mixed(const char* v) {m_type = COLUMN_TYPE_STRING; m_str  = v;}
    Mixed(BinaryData v)  {m_type = COLUMN_TYPE_BINARY; m_str = v.pointer; m_len = v.len;}
    Mixed(const char* v, std::size_t len) {m_type = COLUMN_TYPE_BINARY; m_str = v; m_len = len;}

    ColumnType get_type() const {return m_type;}

    int64_t     get_int()    const { assert(m_type == COLUMN_TYPE_INT);    return m_int; }
    bool        get_bool()   const { assert(m_type == COLUMN_TYPE_BOOL);   return m_bool; }
    std::time_t get_date()   const { assert(m_type == COLUMN_TYPE_DATE);   return m_date; }
    const char* get_string() const { assert(m_type == COLUMN_TYPE_STRING); return m_str; }
    BinaryData  get_binary() const { assert(m_type == COLUMN_TYPE_BINARY); return BinaryData(m_str, m_len); }

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


} // namespace tightdb

#endif // TIGHTDB_MIXED_HPP
