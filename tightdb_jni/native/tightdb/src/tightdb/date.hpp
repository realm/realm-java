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
#ifndef TIGHTDB_DATE_HPP
#define TIGHTDB_DATE_HPP

#include <ctime>
#include <ostream>

namespace tightdb {


class Date {
public:
    Date(std::time_t d): m_date(d) {}
    std::time_t get_date() const { return m_date; }

    bool operator==(const Date& d) const { return m_date == d.m_date; }
    bool operator!=(const Date& d) const { return m_date != d.m_date; }

    template<class Ch, class Tr>
    friend std::basic_ostream<Ch, Tr>& operator<<(std::basic_ostream<Ch, Tr>& out, const Date& d)
    {
        out << "Date("<<d.m_date<<")";
        return out;
    }

private:
    std::time_t m_date;
};


} // namespace tightdb

#endif // TIGHTDB_DATE_HPP
