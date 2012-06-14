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
#ifndef TIGHTDB_C_TABLE_H
#define TIGHTDB_C_TABLE_H


/* FIXME: Every function here should be qualified with a 'tightdb_'
 * prefix, and every type with 'Tightdb' prefix. Otherwise, the
 * customer risks name conflicts. Too many that came before us, have
 * made this mistake. */


/* TODO:
  MyTable_(...)
    setCOL, getCOL,
     queryCOL

 Test
 Document

*/


#ifdef _MSC_VER
#include "win32/stdint.h"
#else
#include <inttypes.h>
#include <stdint.h>
#endif

#include <time.h>
#include <stdlib.h> // size_t
#include "column_type.hpp"

#ifdef __cplusplus

typedef tightdb::ColumnType TightdbColumnType;

namespace tightdb {
    class Table;
    class TableView;
    class Spec;
    class Group;
    class Query;
    class Mixed;
    struct BinaryData;
}
using tightdb::Table;
using tightdb::TableView;
using tightdb::Spec;
using tightdb::Group;
using tightdb::Query;
using tightdb::Mixed;
using tightdb::BinaryData;

extern "C" {

#else

#ifdef _MSC_VER
typedef enum bool_enum {false = 0, true = 1} bool;
#else
#include <stdbool.h>
#endif
typedef enum TightdbColumnType TightdbColumnType;

/* From the point of view of C, these are all opaque structures */
typedef struct BinaryData BinaryData;
typedef struct Mixed Mixed;
typedef struct Table Table;
typedef struct TableView TableView;
typedef struct Spec Spec;
typedef struct Group Group;
typedef struct Query Query;

#endif // __cplusplus

/*
#define tdb_type_int          int64_t
#define tdb_type_bool         bool
#define tdb_type_string       const char*
#define tdb_type_date         time_t
#define tdb_type_binary       BinaryData
#define tdb_type_mixed        Mixed

#define COLUMN_TYPE_int     COLUMN_TYPE_INT
#define COLUMN_TYPE_bool    COUUMN_TYPE_BOOL
#define COLUMN_TYPE_string  COLUMN_TYPE_STRING
#define COLUMN_TYPE_date    COLUMN_TYPE_DATE
#define COLUMN_TYPE_binary  COLUMN_TYPE_BINARY
#define COLUMN_TYPE_mixed   COLUMN_TYPE_MIXED
*/


/*** Mixed ************************************/

    /* Allocate new Mixed type */
    Mixed   *mixed_new_bool(bool value);
    Mixed   *mixed_new_int(int64_t value);
    Mixed   *mixed_new_date(time_t value);
    Mixed   *mixed_new_string(const char* value);
    Mixed   *mixed_new_binary(const char* value, size_t len);
    Mixed   *mixed_new_table(void);
    /* Free Mixed type after use*/
    void    mixed_delete(Mixed *mixed);
    /* Getters */
    bool        mixed_get_bool(Mixed* mixed);
    int64_t     mixed_get_int(Mixed* mixed);
    time_t      mixed_get_date(Mixed* mixed);
    const char* mixed_get_string(Mixed* mixed);
    BinaryData* mixed_get_binary(Mixed* mixed);
//??? Wait for implementation:
//    Table*        mixed_get_table(Mixed* mixed);


/*** Spec ************************************/

    size_t      spec_get_ref(Spec* spec);
    void        spec_add_column(Spec* spec,  TightdbColumnType type, const char* name);
    Spec*       spec_add_column_table(Spec* spec, const char* name);

    Spec*       spec_get_spec(Spec* spec, size_t column_ndx);

    size_t      spec_get_column_count(Spec* spec);
    TightdbColumnType spec_get_column_type(Spec* spec, size_t column_idx);
    const char* spec_get_column_name(Spec* spec, size_t column_idx);
    size_t      spec_get_column_index(Spec* spec, const char* name);

                 /* Delete spec after use of functions that returns a Spec* */
    void        spec_delete(Spec* spec);

/*** Table ************************************/

    /* Creating and deleting tables */
    Table*      table_new();
    void        table_delete(Table* t);       /* Delete after use of table_new() */
    void        table_unbind(const Table* t); /* Ref-count delete of table* from table_get_table() */

    /* Specify table */
    Spec*       table_get_spec(Table* t);     /* Use spec_delete() when done */
    void        table_update_from_spec(Table* t);
    size_t      table_register_column(Table* t,  TightdbColumnType type, const char* name);

    /* Column meta information */
    size_t      table_get_column_count(const Table* t);
    size_t      table_get_column_index(const Table* t, const char* name);
    const char* table_get_column_name(const Table* t, size_t ndx);
    TightdbColumnType table_get_column_type(const Table* t, size_t ndx);

    /* Table size */
    bool        table_is_empty(const Table* t);
    size_t      table_get_size(const Table* t);

    /* Optimization */
    void table_optimize(Table* t);

    /* Removing rows */
    void table_clear(Table* t);
    void table_remove(Table* t, size_t ndx);
    void table_remove_last(Table* t);

    /* Inserting values */
    void table_add(Table* t, ...);
    void table_insert(Table* t, size_t ndx, ...);

    /* Getting values */
    int64_t     table_get_int(const Table* t, size_t column_ndx, size_t ndx);
    bool        table_get_bool(const Table* t, size_t column_ndx, size_t ndx);
    time_t      table_get_date(const Table* t, size_t column_ndx, size_t ndx);
    const char* table_get_string(const Table* t, size_t column_ndx, size_t ndx);
    BinaryData* table_get_binary(const Table* t, size_t column_ndx, size_t ndx);
    Mixed*      table_get_mixed(const Table* t, size_t column_ndx, size_t ndx);
    TightdbColumnType table_get_mixed_type(const Table* t, size_t column_ndx, size_t ndx);

    Table*      table_get_subtable(Table* t, size_t column_ndx, size_t ndx);
    const Table* table_get_const_subtable(const Table* t, size_t column_ndx, size_t ndx);
                /* Use table_unbind() to 'delete' the table after use */

    /* Setting values */
    void table_set_int(Table* t, size_t column_ndx, size_t ndx, int64_t value);
    void table_set_bool(Table* t, size_t column_ndx, size_t ndx, bool value);
    void table_set_date(Table* t, size_t column_ndx, size_t ndx, time_t value);
    void table_set_string(Table* t, size_t column_ndx, size_t ndx, const char* value);
    void table_set_binary(Table* t, size_t column_ndx, size_t ndx, const char* value, size_t len);
    void table_set_mixed(Table* t, size_t column_ndx, size_t ndx, Mixed value);

    void table_clear_table(Table* t, size_t column_ndx, size_t ndx);

    /* Indexing */
    bool table_has_index(const Table* t, size_t column_ndx);
    void table_set_index(Table* t, size_t column_ndx);

    /* Searching */
    size_t table_find_int(const Table* t, size_t column_ndx, int value);
    size_t table_find_int64(const Table* t, size_t column_ndx, int64_t value);
    size_t table_find_bool(const Table* t, size_t column_ndx, bool value);
    size_t table_find_date(const Table* t, size_t column_ndx, time_t value);
    size_t table_find_string(const Table* t, size_t column_ndx, const char* value);

    TableView* table_find_all_int64(Table* t, size_t column_ndx, int64_t value);
                /* Remeber to call tableview_delete(tv) after use of the returned TableView */

    /* NOTE: Low-level insert functions. Always insert in all columns at once
    ** and call table_insert_done after to avoid table getting un-balanced. */
    void table_insert_int(Table* t, size_t column_ndx, size_t ndx, int value);
    void table_insert_int64(Table* t, size_t column_ndx, size_t ndx, int64_t value);
    void table_insert_bool(Table* t, size_t column_ndx, size_t ndx, bool value);
    void table_insert_date(Table* t, size_t column_ndx, size_t ndx, time_t value);
    void table_insert_string(Table* t, size_t column_ndx, size_t ndx, const char* value);
    void table_insert_binary(Table* t, size_t column_ndx, size_t ndx, const char* value, size_t len);
    void table_insert_mixed(Table* t, size_t column_ndx, size_t ndx, Mixed value);
    void table_insert_table(Table* t, size_t column_ndx, size_t ndx);
    void table_insert_done(Table* t);

/*** TableView ************************************/
//???missing remove and remove_last

    /* Creating and deleting tableviews */
    void tableview_delete(TableView* t);

    /* TableView size */
    bool    tableview_is_empty(const TableView* tv);
    size_t  tableview_get_size(const TableView* tv);
    size_t  tableview_get_table_size(size_t column_ndx, size_t ndx);

    /* Getting values */
    int64_t     tableview_get_int(const TableView* tv, size_t column_ndx, size_t ndx);
    bool        tableview_get_bool(const TableView* tv, size_t column_ndx, size_t ndx);
    time_t      tableview_get_date(const TableView* tv, size_t column_ndx, size_t ndx);
    const char* tableview_get_string(const TableView* tv, size_t column_ndx, size_t ndx);
//???    BinaryData tableview_get_binary(const TableView* tv, size_t column_ndx, size_t ndx);
//???    Mixed tableview_get_mixed(const TableView* tv, size_t column_ndx, size_t ndx);

    Table*      tableview_get_table(const TableView* tv, size_t column_ndx, size_t ndx);
    /* Use table_unbind() to 'delete' the table after use */

    /* Setting values */
    void tableview_set_int(TableView* tv, size_t column_ndx, size_t ndx, int64_t value);
    void tableview_set_bool(TableView* tv, size_t column_ndx, size_t ndx, bool value);
    void tableview_set_date(TableView* tv, size_t column_ndx, size_t ndx, time_t value);
    void tableview_set_string(TableView* t, size_t column_ndx, size_t ndx, const char* value);
//???    void tableview_set_binary(TableView* tv, size_t column_ndx, size_t ndx, const char* value, size_t len);
//???    void tableview_set_mixed(TableView* tv, size_t column_ndx, size_t ndx, Mixed value);

//???    void tableview_clear_table(TableView* tv, size_t column_ndx, size_t ndx);

    /* Search and sort */
    size_t  tableview_find(TableView* tv, size_t column_ndx, int64_t value);
//???   void    tableview_find_all(TableView* tv, size_t column_ndx, int64_t value);
    size_t  tableview_find_string(TableView* tv, size_t column_ndx, const char* value);
//???   void    tableview_find_all_string(TableView* tv, size_t column_ndx, const char *value);

    void    tableview_sort(TableView* tv, size_t column_ndx, bool ascending);

    /* Aggregation */
    int64_t tableview_sum(TableView* tv, size_t column_ndx);
    int64_t tableview_max(TableView* tv, size_t column_ndx);
    int64_t tableview_min(TableView* tv, size_t column_ndx);


/*** Group ************************************/

    Group*      group_new(void);
    Group*      group_new_file(const char* filename);
    Group*      group_new_mem(const char* buffer, size_t len);
    void        group_delete(Group* group);

    bool        group_is_valid(Group* group);
    size_t      group_get_table_count(Group* group);
    const char* group_get_table_name(Group* group, size_t table_ndx);
    bool        group_has_table(Group* group, const char* name);

    Table*      group_get_table(Group* group, const char* name);

    // Serialization
    void        group_write(Group* group, const char* filepath);
    char*       group_write_to_mem(Group* group, size_t* len);


/*** Query ************************************/

    Query*  query_new();
    void    query_delete(Query* q);

    void    query_group(Query* q);
    void    query_end_group(Query* q);
    void    query_or(Query* q);

    void    query_subtable(Query* q, size_t column_ndx);
    void    query_parent(Query* q);

    Query*  query_bool_equal(Query* q, size_t column_ndx, bool value);

    Query*  query_int_equal(Query* q, size_t column_ndx, int64_t value);

    Query*  query_int_not_equal(Query* q, size_t column_ndx, int64_t value);
    Query*  query_int_greater(Query* q, size_t column_ndx, int64_t value);
    Query*  query_int_greater_or_equal(Query* q, size_t column_ndx, int64_t value);
    Query*  query_int_less(Query* q, size_t column_ndx, int64_t value);
    Query*  query_int_less_or_equal(Query* q, size_t column_ndx, int64_t value);
    Query*  query_int_between(Query* q, size_t column_ndx, int64_t from, int64_t to);

    typedef enum {
        CASE_INSENSITIVE = 0,
        CASE_SENSITIVE   = 1
    } CaseSensitivity_t;
    Query*  query_string_equal(Query* q, size_t column_ndx, const char* value, CaseSensitivity_t case_sensitive);
    Query*  query_string_not_equal(Query* q, size_t column_ndx, const char* value, CaseSensitivity_t case_sensitive);
    Query*  query_string_begins_with(Query* q, size_t column_ndx, const char* value, CaseSensitivity_t case_sensitive);
    Query*  query_string_ends_with(Query* q, size_t column_ndx, const char* value, CaseSensitivity_t case_sensitive);
    Query*  query_string_contains(Query* q, size_t column_ndx, const char* value, CaseSensitivity_t case_sensitive);

/* Currently missing support for Query on Mixed and Binary */

    TableView*  query_find_all(Query* q, Table* t);
    TableView*  query_find_all_range(Query* q, Table* t, size_t start, size_t end, size_t limit);
            /* Use tableview_delete(); to delete the tableview after use */

    size_t   query_count(Query* q, const Table* t);
    size_t   query_count_range(Query* q, const Table* t,
                               size_t start, size_t end, size_t limit);
    int64_t  query_min(Query* q, const Table* t, size_t column_ndx, size_t* resultcount);
    int64_t  query_min_range(Query* q, const Table* t, size_t column_ndx, size_t* resultcount,
                             size_t start, size_t end, size_t limit);
    int64_t  query_max(Query* q, const Table* t, size_t column_ndx, size_t* resultcount);
    int64_t  query_max_range(Query* q, const Table* t, size_t column_ndx, size_t* resultcount,
                             size_t start, size_t end, size_t limit);
    int64_t  query_sum(Query* q, const Table* t, size_t column_ndx, size_t* resultcount);
    int64_t  query_sum_range(Query* q, const Table* t, size_t column_ndx, size_t* resultcount,
                             size_t start, size_t end, size_t limit);
    double   query_avg(Query* q, const Table* t, size_t column_ndx, size_t* resultcount);
    double   query_avg_range(Query* q, const Table* t, size_t column_ndx, size_t* resultcount,
                             size_t start, size_t end, size_t limit);

#ifdef __cplusplus
} //extern "C"
#endif

#endif /* TIGHTDB_C_TABLE_H */
