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
#ifndef TIGHTDB_GROUP_SHARED_HPP
#define TIGHTDB_GROUP_SHARED_HPP

#include "group.hpp"

namespace tightdb {

// Pre-declarations
struct ReadCount;
struct SharedInfo;

class SharedGroup {
public:
    SharedGroup(const char* filename);
    ~SharedGroup();
    
    bool is_valid() const {return m_isValid;}

    const Group& start_read();
    void end_read();
    
    Group& start_write();
    void end_write();

#ifdef _DEBUG
    void test_ringbuf();
#endif

private:
    // Ring buffer managment
    bool       ringbuf_is_empty() const;
    size_t     ringbuf_size() const;
    size_t     ringbuf_capacity() const;
    bool       ringbuf_is_first(size_t ndx) const;
    void       ringbuf_put(const ReadCount& v);
    void       ringbuf_remove_first();
    size_t     ringbuf_find(uint32_t version) const;
    ReadCount& ringbuf_get(size_t ndx);
    ReadCount& ringbuf_get_first();
    ReadCount& ringbuf_get_last();
    
    // Member variables
    Group       m_group;
    SharedInfo* m_info;
    size_t      m_info_len;
    bool        m_isValid;
    uint32_t    m_version;
    int         m_fd;
    const char* m_lockfile_path;
};

} //namespace tightdb

#endif //TIGHTDB_GROUP_SHARED_HPP
