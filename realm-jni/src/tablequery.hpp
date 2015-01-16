/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef __REALM_TABLEQUERY__
#define __REALM_TABLEQUERY__

#include <vector>
#include <assert.h>
#include <tightdb.hpp>

class TableQuery : public tightdb::Query {
    // 'subtables' is used to figure out which subtable the query
    // is currectly working on, so that we can lookup the correct
    // table and verify the parameters related to that table.
    std::vector<size_t> subtables;  // holds subtable column indeces 

public:
    TableQuery(const Query& copy) : tightdb::Query(copy, tightdb::Query::TCopyExpressionTag{}) {};
 
    void push_subtable(size_t index) {
        subtables.push_back(index);
    }

    bool pop_subtable() {
        if (subtables.empty())
            return false;
        subtables.pop_back();
        return true;
    }
    
    tightdb::TableRef get_current_table() {
        tightdb::TableRef table = get_table();

        // Go through the stack of subtables to find current subtable (if any)
        size_t size = subtables.size(); 
        for (size_t i = 0; i < size; ++i) {
            size_t index = subtables[i];
            table = table->get_subtable(index, 0);
        }
        return table;
    }
};

#define TQ(ptr) reinterpret_cast<TableQuery*>(ptr)

#endif // __REALM_TABLEQUERY__
