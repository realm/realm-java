#include "c-table.h"

#include "lang_bind_helper.hpp"
#include "query.hpp"
#include <cstdarg>
#include <assert.h>

/*
C1X will be getting support for type generic expressions they look like this:
#define cbrt(X) _Generic((X), long double: cbrtl, \
                              default: cbrt, \
                              float: cbrtf)(X)
*/



extern "C" {

/*** Mixed ************************************/

Mixed *mixed_new_bool(bool value)
{
    return new Mixed(value);
}
Mixed *mixed_new_date(time_t value)
{
    return new Mixed(tightdb::Date(value));
}
Mixed *mixed_new_int(int64_t value)
{
    return new Mixed(value);
}
Mixed *mixed_new_string(const char* value)
{
    return new Mixed(value);
}
Mixed *mixed_new_binary(const char* value, size_t len)
{
    return new Mixed((const char*)value, len);
}
Mixed *mixed_new_table(void)
{
    return new Mixed(tightdb::COLUMN_TYPE_TABLE);
}
void mixed_delete(Mixed *mixed)
{
    delete mixed;
}

int64_t mixed_get_int(Mixed *mixed)
{
    return mixed->get_int();
}
bool mixed_get_bool(Mixed *mixed)
{
    return mixed->get_bool();
}
time_t mixed_get_date(Mixed *mixed)
{
    return mixed->get_date();
}
const char* mixed_get_string(Mixed *mixed)
{
    return mixed->get_string();
}
BinaryData* mixed_get_binary(Mixed *mixed)
{
    return new BinaryData(mixed->get_binary());
}


/*** Spec ************************************/

void spec_delete(Spec* spec)
{
    delete spec;
}

void spec_add_column(Spec* spec, TightdbColumnType type, const char* name)
{
    spec->add_column(type, name);
}

Spec* spec_add_column_table(Spec* spec, const char* name)
{
    return new Spec(spec->add_subtable_column(name));
}

Spec* spec_get_spec(Spec* spec, size_t column_ndx)
{
    return new Spec(spec->get_subspec(column_ndx));
}

size_t spec_get_column_count(Spec* spec)
{
    return spec->get_column_count();
}

TightdbColumnType spec_get_column_type(Spec* spec, size_t column_ndx)
{
    return spec->get_column_type(column_ndx);
}

const char* spec_get_column_name(Spec* spec, size_t column_ndx)
{
    return spec->get_column_name(column_ndx);
}

size_t spec_get_column_index(Spec* spec, const char* name)
{
    return spec->get_column_index(name);
}

// ??? get_subspec_ref ??

/*** Table ************************************/


// Pre-declare local functions
void table_insert_impl(Table* t, size_t ndx, va_list ap);

Table* table_new()
{
    return new Table();
}

void table_delete(Table* t)
{
    delete t;
}

void table_unbind(const Table* t)
{
    tightdb::LangBindHelper::unbind_table_ref(t);
}

Spec* table_get_spec(Table* t)
{
    return new Spec(t->get_spec());
}

void table_update_from_spec(Table* t)
{
    t->update_from_spec();
}

size_t table_register_column(Table* t, TightdbColumnType type, const char* name)
{
    return t->add_column(type, name);
}

size_t table_get_column_count(const Table* t)
{
    return t->get_column_count();
}

const char* table_get_column_name(const Table* t, size_t ndx)
{
    return t->get_column_name(ndx);
}

size_t table_get_column_index(const Table* t, const char* name)
{
    return t->get_column_index(name);
}

TightdbColumnType table_get_column_type(const Table* t, size_t ndx)
{
    return t->get_column_type(ndx);
}

bool table_is_empty(const Table* t)
{
    return t->is_empty();
}

size_t table_get_size(const Table* t)
{
    return t->size();
}

void table_clear(Table* t)
{
    t->clear();
}

void table_optimize(Table* t)
{
    t->optimize();
}

void table_remove(Table* t, size_t ndx)
{
    t->remove(ndx);
}

void table_remove_last(Table* t)
{
    t->remove_last();
}


/*** Getters *******/


int64_t table_get_int(const Table* t, size_t column_ndx, size_t ndx)
{
    return t->get_int(column_ndx, ndx);
}

bool table_get_bool(const Table* t, size_t column_ndx, size_t ndx)
{
    return t->get_bool(column_ndx, ndx);
}

time_t table_get_date(const Table* t, size_t column_ndx, size_t ndx)
{
    return t->get_date(column_ndx, ndx);
}

const char* table_get_string(const Table* t, size_t column_ndx, size_t ndx)
{
    return t->get_string(column_ndx, ndx);
}

BinaryData* table_get_binary(const Table* t, size_t column_ndx, size_t ndx)
{
    return new BinaryData(t->get_binary(column_ndx, ndx));
}

Mixed* table_get_mixed(const Table* t, size_t column_ndx, size_t ndx)
{
    return new Mixed(t->get_mixed(column_ndx, ndx));
}

TightdbColumnType table_get_mixed_type(const Table* t, size_t column_ndx, size_t ndx)
{
    return t->get_mixed_type(column_ndx, ndx);
}

Table* table_get_subtable(Table* t, size_t column_ndx, size_t ndx)
{
    return tightdb::LangBindHelper::get_subtable_ptr(t, column_ndx, ndx);
}

const Table* table_get_const_subtable(const Table* t, size_t column_ndx, size_t ndx)
{
    return tightdb::LangBindHelper::get_subtable_ptr(t, column_ndx, ndx);
}

/*** Setters *******/


void table_set_int(Table* t, size_t column_ndx, size_t ndx, int64_t value)
{
    t->set_int(column_ndx, ndx, value);
}

void table_set_bool(Table* t, size_t column_ndx, size_t ndx, bool value)
{
    t->set_bool(column_ndx, ndx, value);
}

void table_set_date(Table* t, size_t column_ndx, size_t ndx, time_t value)
{
    t->set_date(column_ndx, ndx, value);
}

void table_set_string(Table* t, size_t column_ndx, size_t ndx, const char* value)
{
    t->set_string(column_ndx, ndx, value);
}

void table_set_binary(Table* t, size_t column_ndx, size_t ndx, const char *value, size_t len)
{
    t->set_binary(column_ndx, ndx, value, len);
}

void table_set_mixed(Table* t, size_t column_ndx, size_t ndx, Mixed value)
{
    t->set_mixed(column_ndx, ndx, value);
}

void table_clear_table(Table* t, size_t column_ndx, size_t ndx)
{
    t->clear_subtable(column_ndx, ndx);
}

void table_insert_impl(Table* t, size_t ndx, va_list ap)
{
    assert(ndx <= t->size());

    const size_t count = t->get_column_count();
    for (size_t i = 0; i < count; ++i) {
        const tightdb::ColumnType type = t->get_column_type(i);
        switch (type) {
        case tightdb::COLUMN_TYPE_INT:
            {
                // int values should always be cast to 64bit in args
                const int64_t v = va_arg(ap, int64_t);
                t->insert_int(i, ndx, v);
            }
            break;
        case tightdb::COLUMN_TYPE_BOOL:
            {
                const int v = va_arg(ap, int);
                t->insert_bool(i, ndx, v != 0);
            }
            break;
        case tightdb::COLUMN_TYPE_DATE:
            {
                const time_t v = va_arg(ap, time_t);
                t->insert_date(i, ndx, v);
            }
            break;
        case tightdb::COLUMN_TYPE_STRING:
            {
                const char* v = va_arg(ap, const char*);
                t->insert_string(i, ndx, v);
            }
            break;
        case tightdb::COLUMN_TYPE_MIXED:
            {
                Mixed* const v = va_arg(ap, Mixed*);
                t->insert_mixed(i, ndx, *v);
            }
            break;
        case tightdb::COLUMN_TYPE_BINARY:
            {
                const char* ptr = va_arg(ap, const char*);
                size_t      len = va_arg(ap, size_t);
                t->insert_binary(i, ndx, ptr, len);
            }
            break;
        case tightdb::COLUMN_TYPE_TABLE:
            {
                t->insert_subtable(i, ndx);
            }
            break;
        default:
            assert(false);
        }
    }

    t->insert_done();
}

void table_add(Table* t,  ...)
{
    // initialize varable length arg list
    va_list ap;
    va_start(ap, t);

    table_insert_impl(t, t->size(), ap);

    va_end(ap);
}

void table_insert(Table* t, size_t ndx, ...)
{
    // initialize varable length arg list
    va_list ap;
    va_start(ap, ndx);

    table_insert_impl(t, ndx, ap);

    va_end(ap);
}



void table_insert_int(Table* t, size_t column_ndx, size_t ndx, int value)
{
    t->insert_int(column_ndx, ndx, value);
}

void table_insert_int64(Table* t, size_t column_ndx, size_t ndx, int64_t value)
{
    t->insert_int(column_ndx, ndx, value);
}

void table_insert_bool(Table* t, size_t column_ndx, size_t ndx, bool value)
{
    t->insert_bool(column_ndx, ndx, value);
}

void table_insert_date(Table* t, size_t column_ndx, size_t ndx, time_t value)
{
    t->insert_date(column_ndx, ndx, value);
}

void table_insert_string(Table* t, size_t column_ndx, size_t ndx, const char* value)
{
    t->insert_string(column_ndx, ndx, value);
}

void table_insert_binary(Table* t, size_t column_ndx, size_t ndx, const char* value, size_t len)
{
    t->insert_binary(column_ndx, ndx, value, len);
}

void table_insert_mixed(Table* t, size_t column_ndx, size_t ndx, Mixed value)
{
    t->insert_mixed(column_ndx, ndx, value);
}

void table_insert_table(Table* t, size_t column_ndx, size_t ndx)
{
    t->insert_subtable(column_ndx, ndx);
}

void table_insert_done(Table* t)
{
    t->insert_done();
}


/******* Index, Searching ******************************/


bool table_has_index(const Table* t, size_t column_ndx)
{
    return t->has_index(column_ndx);
}

void table_set_index(Table* t, size_t column_ndx)
{
    return t->set_index(column_ndx);
}

size_t table_find_int(const Table* t, size_t column_ndx, int value)
{
    return t->find_first_int(column_ndx, (int64_t)value);
}

size_t table_find_int64(const Table* t, size_t column_ndx, int64_t value)
{
    return t->find_first_int(column_ndx, value);
}

size_t table_find_bool(const Table* t, size_t column_ndx, bool value)
{
    return t->find_first_bool(column_ndx, value);
}

size_t table_find_date(const Table* t, size_t column_ndx, time_t value)
{
    return t->find_first_date(column_ndx, value);
}

size_t table_find_string(const Table* t, size_t column_ndx, const char* value)
{
    return t->find_first_string(column_ndx, value);
}

TableView* table_find_all_int64(Table* t, size_t column_ndx, int64_t value)
{
    return new TableView(t->find_all_int(column_ndx, value));
}


// *** TableView *********************************************************************


void tableview_delete(TableView* tv)
{
    delete tv;
}

bool tableview_is_empty(const TableView* tv)
{
    return tv->is_empty();
}

size_t tableview_get_size(const TableView* tv)
{
    return tv->size();
}
/* ??? Implement
size_t tableview_get_table_size(const TableView* tv, size_t column_ndx, size_t ndx)
{
    return tv->get_subtable_size();
*/


int64_t tableview_get_int(const TableView* tv, size_t column_ndx, size_t ndx)
{
    return tv->get_int(column_ndx, ndx);
}

bool tableview_get_bool(const TableView* tv, size_t column_ndx, size_t ndx)
{
    return tv->get_bool(column_ndx, ndx);
}

time_t tableview_get_date(const TableView* tv, size_t column_ndx, size_t ndx)
{
    return tv->get_date(column_ndx, ndx);
}

const char* tableview_get_string(const TableView* tv, size_t column_ndx, size_t ndx)
{
    return tv->get_string(column_ndx, ndx);
}

/* ??? Waiting for implementation
BinaryData tableview_get_binary(const TableView* tv, size_t column_ndx, size_t ndx)
{
    return tv->get_binary(column_ndx, ndx);
}

Mixed tableview_get_mixed(const TableView* tv, size_t column_ndx, size_t ndx)
{
    return tv->get_mixed(column_ndx, ndx);
}
*/


void tableview_set_int(TableView* tv, size_t column_ndx, size_t ndx, int64_t value)
{
    tv->set_int(column_ndx, ndx, value);
}

void tableview_set_bool(TableView* tv, size_t column_ndx, size_t ndx, bool value)
{
    tv->set_bool(column_ndx, ndx, value);
}

void tableview_set_date(TableView* tv, size_t column_ndx, size_t ndx, time_t value)
{
    tv->set_date(column_ndx, ndx, value);
}

void tableview_set_string(TableView* tv, size_t column_ndx, size_t ndx, const char* value)
{
    tv->set_string(column_ndx, ndx, value);
}

/*
//??? Waiting for implementation
void tableview_set_binary(TableView* tv, size_t column_ndx, size_t ndx, const char* value, size_t len)
{
    tv->set_binary(column_ndx, ndx, value, len);
}

void tableview_set_mixed(TableView* tv, size_t column_ndx, size_t ndx, Mixed value)
{
    tv->set_mixed(column_ndx, ndx, value);
}

void tableview_clear_table(TableView* tv, size_t column_ndx, size_t ndx)
{
    tv->clear_subtable(column_ndx, ndx);
}
*/

/* Search and sort */

size_t tableview_find(TableView* tv, size_t column_ndx, int64_t value)
{
    return tv->find_first_int(column_ndx, value);
}

size_t tableview_find_string(TableView* tv, size_t column_ndx, const char* value)
{
    return tv->find_first_string(column_ndx, value);
}

#if 0
//??? Waiting for implementation
void tableview_find_all(TableView* tv, size_t column_ndx, int64_t value)
{
    // ??? waiting for implementation: tv->find_all(*tv, column_ndx, value);
    assert(0);
}

void tableview_find_all_string(TableView* tv, size_t column_ndx, const char *value)
{
    tv->find_all(*tv, column_ndx, value);
}
#endif

/* Aggregation */
int64_t tableview_sum(TableView* tv, size_t column_ndx)
{
    return tv->sum(column_ndx);
}

int64_t tableview_min(TableView* tv, size_t column_ndx)
{
    return tv->minimum(column_ndx);
}

int64_t tableview_max(TableView* tv, size_t column_ndx)
{
    return tv->maximum(column_ndx);
}

void tableview_sort(TableView* tv, size_t column_ndx, bool ascending)
{
    tv->sort(column_ndx, ascending);
}


/**** Group *********************************************************************/

Group* group_new(void)
{
    return new Group();
}

Group* group_new_file(const char* filename)
{
    return new Group(filename);
}

Group* group_new_mem(const char* buffer, size_t len)
{
    return new Group(buffer, len);
}

void group_delete(Group* group)
{
    delete group;
}

bool group_is_valid(Group* group)
{
    return group->is_valid();
}

size_t group_get_table_count(Group* group)
{
    return group->get_table_count();
}

const char* group_get_table_name(Group* group, size_t table_ndx)
{
    return group->get_table_name(table_ndx);
}

bool group_has_table(Group* group, const char* name)
{
    return group->has_table(name);
}

#if 0
///???
Table* group_get_table(Group* group, const char* name)
{
    /*??? Waiting for removal of TopLevelTable*/
    /* return group->get_subtable(name); */
}
#endif

/* Serialization */
void group_write(Group* group, const char* filepath)
{
    group->write(filepath);
}

char* group_write_to_mem(Group* group, size_t* len)
{
    return group->write_to_mem(*len);
}



/**** Query *********************************************************************/


Query* query_new()
{
    return new Query();
}

void query_delete(Query* q)
{
    delete q;
}

void query_group(Query* q)
{
    q->end_group();
}

void query_end_group(Query* q)
{
    q->group();
}
void query_or(Query* q)
{
    q->Or();
}
#if 1
void query_subtable(Query* q, size_t column_ndx)
{
    q->subtable(column_ndx);
}
#endif
void query_parent(Query* q)
{
    q->parent();
}

Query* query_bool_equal(Query* q, size_t column_ndx, bool value)
{
    return new Query(q->equal(column_ndx, value));
}

Query* query_int_equal(Query* q, size_t column_ndx, int64_t value)
{
    return new Query(q->equal(column_ndx, value));
}

/* Integers */

Query*  query_int_not_equal(Query* q, size_t column_ndx, int64_t value)
{
    return new Query(q->not_equal(column_ndx, value));
}
Query*  query_int_greater(Query* q, size_t column_ndx, int64_t value)
{
    return new Query(q->greater(column_ndx, value));
}
Query*  query_int_greater_or_equal(Query* q, size_t column_ndx, int64_t value)
{
    return new Query(q->greater_equal(column_ndx, value));
}
Query*  query_int_less(Query* q, size_t column_ndx, int64_t value)
{
    return new Query(q->less(column_ndx, value));
}
Query*  query_int_less_or_equal(Query* q, size_t column_ndx, int64_t value)
{
    return new Query(q->less_equal(column_ndx, value));
}
Query*  query_int_between(Query* q, size_t column_ndx, int64_t from, int64_t to)
{
    return new Query(q->between(column_ndx, from , to));
}

/* Strings */

Query*  query_string_equal(Query* q, size_t column_ndx, const char* value, CaseSensitivity_t case_sensitive)
{
    return new Query(q->equal(column_ndx, value, (case_sensitive == CASE_SENSITIVE)));
}
Query*  query_string_not_equal(Query* q, size_t column_ndx, const char* value, CaseSensitivity_t case_sensitive)
{
    return new Query(q->not_equal(column_ndx, value, (case_sensitive == CASE_SENSITIVE)));
}
Query*  query_string_begins_with(Query* q, size_t column_ndx, const char* value, CaseSensitivity_t case_sensitive)
{
    return new Query(q->begins_with(column_ndx, value, (case_sensitive == CASE_SENSITIVE)));
}
Query*  query_string_ends_with(Query* q, size_t column_ndx, const char* value, CaseSensitivity_t case_sensitive)
{
    return new Query(q->ends_with(column_ndx, value, (case_sensitive == CASE_SENSITIVE)));
}
Query*  query_string_contains(Query* q, size_t column_ndx, const char* value, CaseSensitivity_t case_sensitive)
{
    return new Query(q->contains(column_ndx, value, (case_sensitive == CASE_SENSITIVE)));
}


/* ??? Currently missing support for Query on Mixed and Binary */


TableView* query_find_all(Query* q, Table* t)
{
    return new TableView(q->find_all(*t, 0, size_t(-1), size_t(-1)));
}

TableView* query_find_all_range(Query* q, Table* t, size_t start, size_t end, size_t limit)
{
    return new TableView(q->find_all(*t, start, end, limit));
}

/* Aggregations */

size_t query_count(Query* q, const Table* t)
{
    return q->count(*t, 0U, size_t(-1), size_t(-1));
}

size_t query_count_range(Query* q, const Table* t, size_t start, size_t end, size_t limit)
{
    return q->count(*t, start, end, limit);
}

int64_t query_min(Query* q, const Table* t, size_t column_ndx, size_t* resultcount)
{
    return q->minimum(*t, column_ndx, resultcount, 0, size_t(-1), size_t(-1));
}

int64_t query_min_range(Query* q, const Table* t, size_t column_ndx, size_t* resultcount,
                        size_t start, size_t end, size_t limit)
{
    return q->minimum(*t, column_ndx, resultcount, start, end, limit);
}

int64_t  query_max(Query* q, const Table* t, size_t column_ndx, size_t* resultcount)
{
    return q->maximum(*t, column_ndx, resultcount, 0, size_t(-1), size_t(-1));
}

int64_t  query_max_range(Query* q, const Table* t, size_t column_ndx, size_t* resultcount,
                         size_t start, size_t end, size_t limit)
{
    return q->maximum(*t, column_ndx, resultcount, start, end, limit);
}

int64_t  query_sum(Query* q, const Table* t, size_t column_ndx, size_t* resultcount)
{
    return q->sum(*t, column_ndx, resultcount, 0, size_t(-1), size_t(-1));
}

int64_t  query_sum_range(Query* q, const Table* t, size_t column_ndx, size_t* resultcount,
                         size_t start, size_t end, size_t limit)
{
    return q->sum(*t, column_ndx, resultcount, start, end, limit);
}

double  query_avg(Query* q, const Table* t, size_t column_ndx, size_t* resultcount)
{
    return q->average(*t, column_ndx, resultcount, 0, size_t(-1), size_t(-1));
}

double  query_avg_range(Query* q, const Table* t, size_t column_ndx, size_t* resultcount,
                         size_t start, size_t end, size_t limit)
{
    return q->average(*t, column_ndx, resultcount, start, end, limit);
}


} // extern "C"
