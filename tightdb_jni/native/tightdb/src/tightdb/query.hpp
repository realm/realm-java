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
#ifndef TIGHTDB_QUERY_HPP
#define TIGHTDB_QUERY_HPP

#include <string>
#include <algorithm>
#include <vector>
#include <stdio.h>
#include <limits.h>
#if defined(_WIN32) || defined(__WIN32__) || defined(_WIN64)
    #include <win32/pthread/pthread.h>
    #include <win32/stdint.h>
#else
    #include <pthread.h>
#endif

#include "table_view.hpp"

namespace tightdb {


// Pre-declarations
class ParentNode;
class Table;

const size_t MAX_THREADS = 128;

class Query {
public:
    Query();
    Query(const Query& copy); // FIXME: Try to remove this
    ~Query();

    // Conditions: int
    Query& equal(size_t column_ndx, int64_t value);
    Query& not_equal(size_t column_ndx, int64_t value);
    Query& greater(size_t column_ndx, int64_t value);
    Query& greater_equal(size_t column_ndx, int64_t value);
    Query& less(size_t column_ndx, int64_t value);
    Query& less_equal(size_t column_ndx, int64_t value);
    Query& between(size_t column_ndx, int64_t from, int64_t to);

    // Conditions: bool
    Query& equal(size_t column_ndx, bool value);

    // Conditions: strings
    Query& equal(size_t column_ndx, const char* value, bool caseSensitive=true);
    Query& begins_with(size_t column_ndx, const char* value, bool caseSensitive=true);
    Query& ends_with(size_t column_ndx, const char* value, bool caseSensitive=true);
    Query& contains(size_t column_ndx, const char* value, bool caseSensitive=true);
    Query& not_equal(size_t column_ndx, const char* value, bool caseSensitive=true);

    // Conditions: date
    // FIXME: Maybe we can just use 'int' versions for date, but why then do we have a special 'date' column type?
    // FIXME: The '_date' suffix is needed because 'time_t' may not be distinguishable from 'int64_t' on all platforms.
/*
    Query& equal_date(size_t column_ndx, time_t value);
    Query& not_equal_date(size_t column_ndx, time_t value);
    Query& greater_date(size_t column_ndx, time_t value);
    Query& greater_equal_date(size_t column_ndx, time_t value);
    Query& less_date(size_t column_ndx, time_t value);
    Query& less_equal_date(size_t column_ndx, time_t value);
    Query& between_date(size_t column_ndx, time_t from, time_t to);
*/

    // Conditions: binary data
    // FIXME: We want case insensitivity here also, becaue these will be used for strings that are not zero-terminated such as regular C++ strings.
    // FIXME: The '_binary' suffix is needed to avoid ambiguity when only 4 arguments are specified.
/*
    Query& equal_binary(size_t column_ndx, const char* ptr, size_t len, bool caseSensitive=true);
    Query& begins_with_binary(size_t column_ndx, const char* ptr, size_t len, bool caseSensitive=true);
    Query& ends_with_binary(size_t column_ndx, const char* ptr, size_t len, bool caseSensitive=true);
    Query& contains_binary(size_t column_ndx, const char* ptr, size_t len, bool caseSensitive=true);
    Query& not_equal_binary(size_t column_ndx, const char* ptr, size_t len, bool caseSensitive=true);
*/

    // Grouping
    void group();
    void end_group();
    void subtable(size_t column);
    void parent();
    void Or();

    // Searching
    size_t         find_next(const Table& table, size_t lastmatch=-1);
    TableView      find_all(Table& table, size_t start=0, size_t end=size_t(-1), size_t limit=size_t(-1));
    ConstTableView find_all(const Table& table, size_t start=0, size_t end=size_t(-1), size_t limit=size_t(-1));

    // Aggregates
    int64_t sum(const Table& table, size_t column, size_t* resultcount=NULL, size_t start=0, size_t end = size_t(-1), size_t limit=size_t(-1)) const;
    int64_t maximum(const Table& table, size_t column, size_t* resultcount=NULL, size_t start=0, size_t end = size_t(-1), size_t limit=size_t(-1)) const;
    int64_t minimum(const Table& table, size_t column, size_t* resultcount=NULL, size_t start=0, size_t end = size_t(-1), size_t limit=size_t(-1)) const;
    double  average(const Table& table, size_t column_ndx, size_t* resultcount=NULL, size_t start=0, size_t end=size_t(-1), size_t limit=size_t(-1)) const;
    size_t  count(const Table& table, size_t start=0, size_t end=size_t(-1), size_t limit=size_t(-1)) const;
/*
    time_t maximum_date(const Table& table, size_t column, size_t* resultcount=NULL, size_t start=0, size_t end = size_t(-1), size_t limit=size_t(-1)) const;
    time_t minimum_date(const Table& table, size_t column, size_t* resultcount=NULL, size_t start=0, size_t end = size_t(-1), size_t limit=size_t(-1)) const;
*/

    // Deletion
    size_t  remove(Table& table, size_t start=0, size_t end=size_t(-1), size_t limit=size_t(-1)) const;

    // Multi-threading
    TableView      FindAllMulti(Table& table, size_t start=0, size_t end=size_t(-1));
    ConstTableView FindAllMulti(const Table& table, size_t start=0, size_t end=size_t(-1));
    int            SetThreads(unsigned int threadcount);

#ifdef _DEBUG
    std::string Verify(); // Must be upper case to avoid conflict with macro in ObjC
#endif

    std::string error_code;

protected:
    friend class XQueryAccessorInt;
    friend class XQueryAccessorString;

    void   Init(const Table& table) const;
    size_t FindInternal(const Table& table, size_t start=0, size_t end=size_t(-1)) const;
    void   UpdatePointers(ParentNode* p, ParentNode** newnode);

    static bool  comp(const std::pair<size_t, size_t>& a, const std::pair<size_t, size_t>& b);
    static void* query_thread(void* arg);

    struct thread_state {
        pthread_mutex_t result_mutex;
        pthread_cond_t  completed_cond;
        pthread_mutex_t completed_mutex;
        pthread_mutex_t jobs_mutex;
        pthread_cond_t  jobs_cond;
        size_t next_job;
        size_t end_job;
        size_t done_job;
        size_t count;
        ParentNode* node;
        Table* table;
        std::vector<size_t> results;
        std::vector<std::pair<size_t, size_t> > chunks;
    } ts;
    pthread_t threads[MAX_THREADS];

    std::vector<ParentNode*> first;
    std::vector<ParentNode**> update;
    std::vector<ParentNode**> update_override;
    std::vector<ParentNode**> subtables;
    std::vector<ParentNode*> all_nodes;
    mutable bool do_delete;
private:
    size_t m_threadcount;
};


} // namespace tightdb

#endif // TIGHTDB_QUERY_HPP
