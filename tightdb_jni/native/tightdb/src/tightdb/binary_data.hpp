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
#ifndef TIGHTDB_BINARY_DATA_HPP
#define TIGHTDB_BINARY_DATA_HPP

#include <cstddef>
#include <ostream>

namespace tightdb {

struct BinaryData {
    const char* pointer;
    std::size_t len;
    BinaryData() {}
    BinaryData(const char* p, std::size_t l): pointer(p), len(l) {}

    template<class Ch, class Tr>
    friend std::basic_ostream<Ch, Tr>& operator<<(std::basic_ostream<Ch, Tr>& out, const BinaryData& d)
    {
        out << "BinaryData("<<static_cast<const void*>(d.pointer)<<", "<<d.len<<")";
        return out;
    }
};

} // namespace tightdb

#endif // TIGHTDB_BINARY_DATA_HPP
