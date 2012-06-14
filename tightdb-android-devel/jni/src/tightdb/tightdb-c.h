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
#ifndef __C_TIGHTDB_H__
#define __C_TIGHTDB_H__

#include "c-table.hpp"
#include "query.h"


#define TIGHTSB_TABLE_1(TableName, CName0, CType0) \
\
Table* TableName##_new(void) { \
    Table *tbl = table_new(); \
    Spec* spec = table_get_spec(tbl); \
    spec_add_column(spec, COLUMN_TYPE_##CType0, #CName0); \
    table_update_from_spec(tbl, spec_get_ref(spec)); \
    spec_delete(spec); \
    return tbl; \
} \
\
void TableName##_add(Table* tbl, tdb_type_##CType0 value0) { \
    table_add(tbl, value0); \
} \
\
void TableName##_insert(Table* tbl, size_t row_ndx, tdb_type_##CType0 value0) { \
    table_insert(tbl, row_ndx, value0); \
} \
\
tdb_type_##CType0 TableName##_get_##CName0(Table* tbl, size_t row_ndx) { \
    return table_get_##CType0(tbl, 0, row_ndx); \
} \
void TableName##_set_##CName0(Table* tbl, size_t row_ndx, tdb_type_##CType0 value) { \
    return table_set_##CType0(tbl, 0, row_ndx, value); \
} \



#define TIGHTSB_TABLE_2(TableName, CName0, CType0, CName1, CType1) \
\
Table* TableName##_new(void) { \
    Table *tbl = table_new(); \
    Spec* spec = table_get_spec(tbl); \
    spec_add_column(spec, COLUMN_TYPE_##CType0, #CName0); \
    spec_add_column(spec, COLUMN_TYPE_##CType1, #CName1); \
    table_update_from_spec(tbl, spec_get_ref(spec)); \
    spec_delete(spec); \
    return tbl; \
} \
\
void TableName##_add(Table* tbl, tdb_type_##CType0 value0, tdb_type_##CType1 value1) { \
    table_add(tbl, value0, value1); \
} \
\
void TableName##_insert(Table* tbl, size_t row_ndx, tdb_type_##CType0 value0, tdb_type_##CType1 value1) { \
    table_insert(tbl, row_ndx, value0, value1); \
} \
\
tdb_type_##CType0 TableName##_get_##CName0(Table* tbl, size_t row_ndx) { \
    return table_get_##CType0(tbl, 0, row_ndx); \
} \
void TableName##_set_##CName0(Table* tbl, size_t row_ndx, tdb_type_##CType0 value) { \
    return table_set_##CType0(tbl, 0, row_ndx, value); \
} \
tdb_type_##CType1 TableName##_get_##CName1(Table* tbl, size_t row_ndx) { \
    return table_get_##CType1(tbl, 1, row_ndx); \
} \
void TableName##_set_##CName1(Table* tbl, size_t row_ndx, tdb_type_##CType1 value) { \
    return table_set_##CType1(tbl, 1, row_ndx, value); \
} \



#define TIGHTSB_TABLE_3(TableName, CName0, CType0, CName1, CType1, CName2, CType2) \
\
Table* TableName##_new(void) { \
    Table *tbl = table_new(); \
    Spec* spec = table_get_spec(tbl); \
    spec_add_column(spec, COLUMN_TYPE_##CType0, #CName0); \
    spec_add_column(spec, COLUMN_TYPE_##CType1, #CName1); \
    spec_add_column(spec, COLUMN_TYPE_##CType2, #CName2); \
    table_update_from_spec(tbl, spec_get_ref(spec)); \
    spec_delete(spec); \
    return tbl; \
} \
\
void TableName##_add(Table* tbl, tdb_type_##CType0 value0, tdb_type_##CType1 value1, tdb_type_##CType2 value2) { \
    table_add(tbl, value0, value1, value2); \
} \
\
void TableName##_insert(Table* tbl, size_t row_ndx, tdb_type_##CType0 value0, tdb_type_##CType1 value1, tdb_type_##CType2 value2) { \
    table_insert(tbl, row_ndx, value0, value1, value2); \
} \
\
tdb_type_##CType0 TableName##_get_##CName0(Table* tbl, size_t row_ndx) { \
    return table_get_##CType0(tbl, 0, row_ndx); \
} \
void TableName##_set_##CName0(Table* tbl, size_t row_ndx, tdb_type_##CType0 value) { \
    return table_set_##CType0(tbl, 0, row_ndx, value); \
} \
tdb_type_##CType1 TableName##_get_##CName1(Table* tbl, size_t row_ndx) { \
    return table_get_##CType1(tbl, 1, row_ndx); \
} \
void TableName##_set_##CName1(Table* tbl, size_t row_ndx, tdb_type_##CType1 value) { \
    return table_set_##CType1(tbl, 1, row_ndx, value); \
} \
tdb_type_##CType2 TableName##_get_##CName2(Table* tbl, size_t row_ndx) { \
    return table_get_##CType2(tbl, 2, row_ndx); \
} \
void TableName##_set_##CName2(Table* tbl, size_t row_ndx, tdb_type_##CType2 value) { \
    return table_set_##CType2(tbl, 2, row_ndx, value); \
} \



#define TIGHTSB_TABLE_4(TableName, CName0, CType0, CName1, CType1, CName2, CType2, CName3, CType3) \
\
Table* TableName##_new(void) { \
    Table *tbl = table_new(); \
    Spec* spec = table_get_spec(tbl); \
    spec_add_column(spec, COLUMN_TYPE_##CType0, #CName0); \
    spec_add_column(spec, COLUMN_TYPE_##CType1, #CName1); \
    spec_add_column(spec, COLUMN_TYPE_##CType2, #CName2); \
    spec_add_column(spec, COLUMN_TYPE_##CType3, #CName3); \
    table_update_from_spec(tbl, spec_get_ref(spec)); \
    spec_delete(spec); \
    return tbl; \
} \
\
void TableName##_add(Table* tbl, tdb_type_##CType0 value0, tdb_type_##CType1 value1, tdb_type_##CType2 value2, tdb_type_##CType3 value3) { \
    table_add(tbl, value0, value1, value2, value3); \
} \
\
void TableName##_insert(Table* tbl, size_t row_ndx, tdb_type_##CType0 value0, tdb_type_##CType1 value1, tdb_type_##CType2 value2, tdb_type_##CType3 value3) { \
    table_insert(tbl, row_ndx, value0, value1, value2, value3); \
} \
\
tdb_type_##CType0 TableName##_get_##CName0(Table* tbl, size_t row_ndx) { \
    return table_get_##CType0(tbl, 0, row_ndx); \
} \
void TableName##_set_##CName0(Table* tbl, size_t row_ndx, tdb_type_##CType0 value) { \
    return table_set_##CType0(tbl, 0, row_ndx, value); \
} \
tdb_type_##CType1 TableName##_get_##CName1(Table* tbl, size_t row_ndx) { \
    return table_get_##CType1(tbl, 1, row_ndx); \
} \
void TableName##_set_##CName1(Table* tbl, size_t row_ndx, tdb_type_##CType1 value) { \
    return table_set_##CType1(tbl, 1, row_ndx, value); \
} \
tdb_type_##CType2 TableName##_get_##CName2(Table* tbl, size_t row_ndx) { \
    return table_get_##CType2(tbl, 2, row_ndx); \
} \
void TableName##_set_##CName2(Table* tbl, size_t row_ndx, tdb_type_##CType2 value) { \
    return table_set_##CType2(tbl, 2, row_ndx, value); \
} \
tdb_type_##CType3 TableName##_get_##CName3(Table* tbl, size_t row_ndx) { \
    return table_get_##CType3(tbl, 3, row_ndx); \
} \
void TableName##_set_##CName3(Table* tbl, size_t row_ndx, tdb_type_##CType3 value) { \
    return table_set_##CType3(tbl, 3, row_ndx, value); \
} \



#define TIGHTSB_TABLE_5(TableName, CName0, CType0, CName1, CType1, CName2, CType2, CName3, CType3, CName4, CType4) \
\
Table* TableName##_new(void) { \
    Table *tbl = table_new(); \
    Spec* spec = table_get_spec(tbl); \
    spec_add_column(spec, COLUMN_TYPE_##CType0, #CName0); \
    spec_add_column(spec, COLUMN_TYPE_##CType1, #CName1); \
    spec_add_column(spec, COLUMN_TYPE_##CType2, #CName2); \
    spec_add_column(spec, COLUMN_TYPE_##CType3, #CName3); \
    spec_add_column(spec, COLUMN_TYPE_##CType4, #CName4); \
    table_update_from_spec(tbl, spec_get_ref(spec)); \
    spec_delete(spec); \
    return tbl; \
} \
\
void TableName##_add(Table* tbl, tdb_type_##CType0 value0, tdb_type_##CType1 value1, tdb_type_##CType2 value2, tdb_type_##CType3 value3, tdb_type_##CType4 value4) { \
    table_add(tbl, value0, value1, value2, value3, value4); \
} \
\
void TableName##_insert(Table* tbl, size_t row_ndx, tdb_type_##CType0 value0, tdb_type_##CType1 value1, tdb_type_##CType2 value2, tdb_type_##CType3 value3, tdb_type_##CType4 value4) { \
    table_insert(tbl, row_ndx, value0, value1, value2, value3, value4); \
} \
\
tdb_type_##CType0 TableName##_get_##CName0(Table* tbl, size_t row_ndx) { \
    return table_get_##CType0(tbl, 0, row_ndx); \
} \
void TableName##_set_##CName0(Table* tbl, size_t row_ndx, tdb_type_##CType0 value) { \
    return table_set_##CType0(tbl, 0, row_ndx, value); \
} \
tdb_type_##CType1 TableName##_get_##CName1(Table* tbl, size_t row_ndx) { \
    return table_get_##CType1(tbl, 1, row_ndx); \
} \
void TableName##_set_##CName1(Table* tbl, size_t row_ndx, tdb_type_##CType1 value) { \
    return table_set_##CType1(tbl, 1, row_ndx, value); \
} \
tdb_type_##CType2 TableName##_get_##CName2(Table* tbl, size_t row_ndx) { \
    return table_get_##CType2(tbl, 2, row_ndx); \
} \
void TableName##_set_##CName2(Table* tbl, size_t row_ndx, tdb_type_##CType2 value) { \
    return table_set_##CType2(tbl, 2, row_ndx, value); \
} \
tdb_type_##CType3 TableName##_get_##CName3(Table* tbl, size_t row_ndx) { \
    return table_get_##CType3(tbl, 3, row_ndx); \
} \
void TableName##_set_##CName3(Table* tbl, size_t row_ndx, tdb_type_##CType3 value) { \
    return table_set_##CType3(tbl, 3, row_ndx, value); \
} \
tdb_type_##CType4 TableName##_get_##CName4(Table* tbl, size_t row_ndx) { \
    return table_get_##CType4(tbl, 4, row_ndx); \
} \
void TableName##_set_##CName4(Table* tbl, size_t row_ndx, tdb_type_##CType4 value) { \
    return table_set_##CType4(tbl, 4, row_ndx, value); \
} \



#define TIGHTSB_TABLE_6(TableName, CName0, CType0, CName1, CType1, CName2, CType2, CName3, CType3, CName4, CType4, CName5, CType5) \
\
Table* TableName##_new(void) { \
    Table *tbl = table_new(); \
    Spec* spec = table_get_spec(tbl); \
    spec_add_column(spec, COLUMN_TYPE_##CType0, #CName0); \
    spec_add_column(spec, COLUMN_TYPE_##CType1, #CName1); \
    spec_add_column(spec, COLUMN_TYPE_##CType2, #CName2); \
    spec_add_column(spec, COLUMN_TYPE_##CType3, #CName3); \
    spec_add_column(spec, COLUMN_TYPE_##CType4, #CName4); \
    spec_add_column(spec, COLUMN_TYPE_##CType5, #CName5); \
    table_update_from_spec(tbl, spec_get_ref(spec)); \
    spec_delete(spec); \
    return tbl; \
} \
\
void TableName##_add(Table* tbl, tdb_type_##CType0 value0, tdb_type_##CType1 value1, tdb_type_##CType2 value2, tdb_type_##CType3 value3, tdb_type_##CType4 value4, tdb_type_##CType5 value5) { \
    table_add(tbl, value0, value1, value2, value3, value4, value5); \
} \
\
void TableName##_insert(Table* tbl, size_t row_ndx, tdb_type_##CType0 value0, tdb_type_##CType1 value1, tdb_type_##CType2 value2, tdb_type_##CType3 value3, tdb_type_##CType4 value4, tdb_type_##CType5 value5) { \
    table_insert(tbl, row_ndx, value0, value1, value2, value3, value4, value5); \
} \
\
tdb_type_##CType0 TableName##_get_##CName0(Table* tbl, size_t row_ndx) { \
    return table_get_##CType0(tbl, 0, row_ndx); \
} \
void TableName##_set_##CName0(Table* tbl, size_t row_ndx, tdb_type_##CType0 value) { \
    return table_set_##CType0(tbl, 0, row_ndx, value); \
} \
tdb_type_##CType1 TableName##_get_##CName1(Table* tbl, size_t row_ndx) { \
    return table_get_##CType1(tbl, 1, row_ndx); \
} \
void TableName##_set_##CName1(Table* tbl, size_t row_ndx, tdb_type_##CType1 value) { \
    return table_set_##CType1(tbl, 1, row_ndx, value); \
} \
tdb_type_##CType2 TableName##_get_##CName2(Table* tbl, size_t row_ndx) { \
    return table_get_##CType2(tbl, 2, row_ndx); \
} \
void TableName##_set_##CName2(Table* tbl, size_t row_ndx, tdb_type_##CType2 value) { \
    return table_set_##CType2(tbl, 2, row_ndx, value); \
} \
tdb_type_##CType3 TableName##_get_##CName3(Table* tbl, size_t row_ndx) { \
    return table_get_##CType3(tbl, 3, row_ndx); \
} \
void TableName##_set_##CName3(Table* tbl, size_t row_ndx, tdb_type_##CType3 value) { \
    return table_set_##CType3(tbl, 3, row_ndx, value); \
} \
tdb_type_##CType4 TableName##_get_##CName4(Table* tbl, size_t row_ndx) { \
    return table_get_##CType4(tbl, 4, row_ndx); \
} \
void TableName##_set_##CName4(Table* tbl, size_t row_ndx, tdb_type_##CType4 value) { \
    return table_set_##CType4(tbl, 4, row_ndx, value); \
} \
tdb_type_##CType5 TableName##_get_##CName5(Table* tbl, size_t row_ndx) { \
    return table_get_##CType5(tbl, 5, row_ndx); \
} \
void TableName##_set_##CName5(Table* tbl, size_t row_ndx, tdb_type_##CType5 value) { \
    return table_set_##CType5(tbl, 5, row_ndx, value); \
} \



#define TIGHTSB_TABLE_7(TableName, CName0, CType0, CName1, CType1, CName2, CType2, CName3, CType3, CName4, CType4, CName5, CType5, CName6, CType6) \
\
Table* TableName##_new(void) { \
    Table *tbl = table_new(); \
    Spec* spec = table_get_spec(tbl); \
    spec_add_column(spec, COLUMN_TYPE_##CType0, #CName0); \
    spec_add_column(spec, COLUMN_TYPE_##CType1, #CName1); \
    spec_add_column(spec, COLUMN_TYPE_##CType2, #CName2); \
    spec_add_column(spec, COLUMN_TYPE_##CType3, #CName3); \
    spec_add_column(spec, COLUMN_TYPE_##CType4, #CName4); \
    spec_add_column(spec, COLUMN_TYPE_##CType5, #CName5); \
    spec_add_column(spec, COLUMN_TYPE_##CType6, #CName6); \
    table_update_from_spec(tbl, spec_get_ref(spec)); \
    spec_delete(spec); \
    return tbl; \
} \
\
void TableName##_add(Table* tbl, tdb_type_##CType0 value0, tdb_type_##CType1 value1, tdb_type_##CType2 value2, tdb_type_##CType3 value3, tdb_type_##CType4 value4, tdb_type_##CType5 value5, tdb_type_##CType6 value6) { \
    table_add(tbl, value0, value1, value2, value3, value4, value5, value6); \
} \
\
void TableName##_insert(Table* tbl, size_t row_ndx, tdb_type_##CType0 value0, tdb_type_##CType1 value1, tdb_type_##CType2 value2, tdb_type_##CType3 value3, tdb_type_##CType4 value4, tdb_type_##CType5 value5, tdb_type_##CType6 value6) { \
    table_insert(tbl, row_ndx, value0, value1, value2, value3, value4, value5, value6); \
} \
\
tdb_type_##CType0 TableName##_get_##CName0(Table* tbl, size_t row_ndx) { \
    return table_get_##CType0(tbl, 0, row_ndx); \
} \
void TableName##_set_##CName0(Table* tbl, size_t row_ndx, tdb_type_##CType0 value) { \
    return table_set_##CType0(tbl, 0, row_ndx, value); \
} \
tdb_type_##CType1 TableName##_get_##CName1(Table* tbl, size_t row_ndx) { \
    return table_get_##CType1(tbl, 1, row_ndx); \
} \
void TableName##_set_##CName1(Table* tbl, size_t row_ndx, tdb_type_##CType1 value) { \
    return table_set_##CType1(tbl, 1, row_ndx, value); \
} \
tdb_type_##CType2 TableName##_get_##CName2(Table* tbl, size_t row_ndx) { \
    return table_get_##CType2(tbl, 2, row_ndx); \
} \
void TableName##_set_##CName2(Table* tbl, size_t row_ndx, tdb_type_##CType2 value) { \
    return table_set_##CType2(tbl, 2, row_ndx, value); \
} \
tdb_type_##CType3 TableName##_get_##CName3(Table* tbl, size_t row_ndx) { \
    return table_get_##CType3(tbl, 3, row_ndx); \
} \
void TableName##_set_##CName3(Table* tbl, size_t row_ndx, tdb_type_##CType3 value) { \
    return table_set_##CType3(tbl, 3, row_ndx, value); \
} \
tdb_type_##CType4 TableName##_get_##CName4(Table* tbl, size_t row_ndx) { \
    return table_get_##CType4(tbl, 4, row_ndx); \
} \
void TableName##_set_##CName4(Table* tbl, size_t row_ndx, tdb_type_##CType4 value) { \
    return table_set_##CType4(tbl, 4, row_ndx, value); \
} \
tdb_type_##CType5 TableName##_get_##CName5(Table* tbl, size_t row_ndx) { \
    return table_get_##CType5(tbl, 5, row_ndx); \
} \
void TableName##_set_##CName5(Table* tbl, size_t row_ndx, tdb_type_##CType5 value) { \
    return table_set_##CType5(tbl, 5, row_ndx, value); \
} \
tdb_type_##CType6 TableName##_get_##CName6(Table* tbl, size_t row_ndx) { \
    return table_get_##CType6(tbl, 6, row_ndx); \
} \
void TableName##_set_##CName6(Table* tbl, size_t row_ndx, tdb_type_##CType6 value) { \
    return table_set_##CType6(tbl, 6, row_ndx, value); \
} \



#define TIGHTSB_TABLE_8(TableName, CName0, CType0, CName1, CType1, CName2, CType2, CName3, CType3, CName4, CType4, CName5, CType5, CName6, CType6, CName7, CType7) \
\
Table* TableName##_new(void) { \
    Table *tbl = table_new(); \
    Spec* spec = table_get_spec(tbl); \
    spec_add_column(spec, COLUMN_TYPE_##CType0, #CName0); \
    spec_add_column(spec, COLUMN_TYPE_##CType1, #CName1); \
    spec_add_column(spec, COLUMN_TYPE_##CType2, #CName2); \
    spec_add_column(spec, COLUMN_TYPE_##CType3, #CName3); \
    spec_add_column(spec, COLUMN_TYPE_##CType4, #CName4); \
    spec_add_column(spec, COLUMN_TYPE_##CType5, #CName5); \
    spec_add_column(spec, COLUMN_TYPE_##CType6, #CName6); \
    spec_add_column(spec, COLUMN_TYPE_##CType7, #CName7); \
    table_update_from_spec(tbl, spec_get_ref(spec)); \
    spec_delete(spec); \
    return tbl; \
} \
\
void TableName##_add(Table* tbl, tdb_type_##CType0 value0, tdb_type_##CType1 value1, tdb_type_##CType2 value2, tdb_type_##CType3 value3, tdb_type_##CType4 value4, tdb_type_##CType5 value5, tdb_type_##CType6 value6, tdb_type_##CType7 value7) { \
    table_add(tbl, value0, value1, value2, value3, value4, value5, value6, value7); \
} \
\
void TableName##_insert(Table* tbl, size_t row_ndx, tdb_type_##CType0 value0, tdb_type_##CType1 value1, tdb_type_##CType2 value2, tdb_type_##CType3 value3, tdb_type_##CType4 value4, tdb_type_##CType5 value5, tdb_type_##CType6 value6, tdb_type_##CType7 value7) { \
    table_insert(tbl, row_ndx, value0, value1, value2, value3, value4, value5, value6, value7); \
} \
\
tdb_type_##CType0 TableName##_get_##CName0(Table* tbl, size_t row_ndx) { \
    return table_get_##CType0(tbl, 0, row_ndx); \
} \
void TableName##_set_##CName0(Table* tbl, size_t row_ndx, tdb_type_##CType0 value) { \
    return table_set_##CType0(tbl, 0, row_ndx, value); \
} \
tdb_type_##CType1 TableName##_get_##CName1(Table* tbl, size_t row_ndx) { \
    return table_get_##CType1(tbl, 1, row_ndx); \
} \
void TableName##_set_##CName1(Table* tbl, size_t row_ndx, tdb_type_##CType1 value) { \
    return table_set_##CType1(tbl, 1, row_ndx, value); \
} \
tdb_type_##CType2 TableName##_get_##CName2(Table* tbl, size_t row_ndx) { \
    return table_get_##CType2(tbl, 2, row_ndx); \
} \
void TableName##_set_##CName2(Table* tbl, size_t row_ndx, tdb_type_##CType2 value) { \
    return table_set_##CType2(tbl, 2, row_ndx, value); \
} \
tdb_type_##CType3 TableName##_get_##CName3(Table* tbl, size_t row_ndx) { \
    return table_get_##CType3(tbl, 3, row_ndx); \
} \
void TableName##_set_##CName3(Table* tbl, size_t row_ndx, tdb_type_##CType3 value) { \
    return table_set_##CType3(tbl, 3, row_ndx, value); \
} \
tdb_type_##CType4 TableName##_get_##CName4(Table* tbl, size_t row_ndx) { \
    return table_get_##CType4(tbl, 4, row_ndx); \
} \
void TableName##_set_##CName4(Table* tbl, size_t row_ndx, tdb_type_##CType4 value) { \
    return table_set_##CType4(tbl, 4, row_ndx, value); \
} \
tdb_type_##CType5 TableName##_get_##CName5(Table* tbl, size_t row_ndx) { \
    return table_get_##CType5(tbl, 5, row_ndx); \
} \
void TableName##_set_##CName5(Table* tbl, size_t row_ndx, tdb_type_##CType5 value) { \
    return table_set_##CType5(tbl, 5, row_ndx, value); \
} \
tdb_type_##CType6 TableName##_get_##CName6(Table* tbl, size_t row_ndx) { \
    return table_get_##CType6(tbl, 6, row_ndx); \
} \
void TableName##_set_##CName6(Table* tbl, size_t row_ndx, tdb_type_##CType6 value) { \
    return table_set_##CType6(tbl, 6, row_ndx, value); \
} \
tdb_type_##CType7 TableName##_get_##CName7(Table* tbl, size_t row_ndx) { \
    return table_get_##CType7(tbl, 7, row_ndx); \
} \
void TableName##_set_##CName7(Table* tbl, size_t row_ndx, tdb_type_##CType7 value) { \
    return table_set_##CType7(tbl, 7, row_ndx, value); \
} \



#endif //__C_TIGHTDB_H__
