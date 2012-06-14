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
#ifndef TIGHTDB_LANG_BIND_HELPER_HPP
#define TIGHTDB_LANG_BIND_HELPER_HPP

#include "table.hpp"
#include "table_view.hpp"
#include "group.hpp"

namespace tightdb {


/**
 * These functions are only to be used by language bindings to gain
 * access to certain otherwise private memebers.
 *
 * \note An application must never call these functions directly.
 *
 * All the get_*_ptr() functions in this class will return a Table
 * pointer where the reference count has already been incremented. 
 *
 * The application must make sure that the unbind_table_ref() function is
 * called to decrement the reference count when it no longer needs
 * access to that table. The order of unbinding is important as you must 
 * unbind subtables to a table before unbinding the table itself.
 * 
 */
class LangBindHelper {
public:
    static Table* get_subtable_ptr(Table*, size_t column_ndx, size_t row_ndx);
    static const Table* get_subtable_ptr(const Table*, size_t column_ndx, size_t row_ndx);

    static Table* get_subtable_ptr(TableView*, size_t column_ndx, size_t row_ndx);
    static const Table* get_subtable_ptr(const TableView*, size_t column_ndx, size_t row_ndx);
    static const Table* get_subtable_ptr(const ConstTableView*, size_t column_ndx, size_t row_ndx);

    static Table* get_table_ptr(Group* grp, const char* name);
    static const Table* get_table_ptr(const Group* grp, const char* name);

    static void unbind_table_ref(const Table*);
};




// Implementation:

inline Table* LangBindHelper::get_subtable_ptr(Table* t, size_t column_ndx, size_t row_ndx)
{
    Table* subtab = t->get_subtable_ptr(column_ndx, row_ndx);
    subtab->bind_ref();
    return subtab;
}

inline const Table* LangBindHelper::get_subtable_ptr(const Table* t, size_t column_ndx, size_t row_ndx)
{
    const Table* subtab = t->get_subtable_ptr(column_ndx, row_ndx);
    subtab->bind_ref();
    return subtab;
}

inline Table* LangBindHelper::get_subtable_ptr(TableView* tv, size_t column_ndx, size_t row_ndx)
{
    return get_subtable_ptr(&tv->get_parent(), column_ndx, tv->get_source_ndx(row_ndx));
}

inline const Table* LangBindHelper::get_subtable_ptr(const TableView* tv, size_t column_ndx, size_t row_ndx)
{
    return get_subtable_ptr(&tv->get_parent(), column_ndx, tv->get_source_ndx(row_ndx));
}

inline const Table* LangBindHelper::get_subtable_ptr(const ConstTableView* tv, size_t column_ndx, size_t row_ndx)
{
    return get_subtable_ptr(&tv->get_parent(), column_ndx, tv->get_source_ndx(row_ndx));
}

inline Table* LangBindHelper::get_table_ptr(Group* grp, const char* name)
{
    Table* subtab = grp->get_table_ptr(name);
    subtab->bind_ref();
    return subtab;
}

inline const Table* LangBindHelper::get_table_ptr(const Group* grp, const char* name)
{
    const Table* subtab = grp->get_table_ptr(name);
    subtab->bind_ref();
    return subtab;
}

inline void LangBindHelper::unbind_table_ref(const Table* t)
{
   t->unbind_ref();
}


} // namespace tightdb

#endif // TIGHTDB_LANG_BIND_HELPER_HPP
