#ifndef __TIGHTDB_TABLEQUERY__
#define __TIGHTDB_TABLEQUERY__

#include <vector>
#include <assert.h>
#include <tightdb.hpp>

class TableQuery : public tightdb::Query {
    // 'subtables' is used to figure out which subtable the query
    // is currectly working on, so that we can lookup the correct
    // table and verify the parameters related to that table.
    std::vector<size_t> subtables;  // holds subtable column indeces 

public:
    TableQuery(const Query& copy) : tightdb::Query(copy) {};
 
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

#endif // __TIGHTDB_TABLEQUERY__
