#include "query.hpp"
#include "query_engine.hpp"

#define MULTITHREAD 1

using namespace tightdb;

const size_t THREAD_CHUNK_SIZE = 1000;

#define MIN(a, b)  (((a) < (b)) ? (a) : (b))
#define MAX(a, b)  (((a) > (b)) ? (a) : (b))
 
Query::Query()
{
    update.push_back(0);
    update_override.push_back(0);
    first.push_back(0);
    m_threadcount = 0;
    do_delete = true;
}

// FIXME: Try to remove this
Query::Query(const Query& copy)
{
    all_nodes = copy.all_nodes;
    update = copy.update;
    update_override = copy.update_override;
    first = copy.first;
    error_code = copy.error_code;
    m_threadcount = copy.m_threadcount;
//    copy.first[0] = 0;
    copy.do_delete = false;
    do_delete = true;
}

Query::~Query()
{
#if MULTITHREAD
    for(size_t i = 0; i < m_threadcount; i++)
        pthread_detach(threads[i]);
#endif
    if(do_delete) {
        for(size_t t = 0; t < all_nodes.size(); t++) {
            ParentNode *p = all_nodes[t];
            delete p;
        }
    }
}

Query& Query::equal(size_t column_ndx, int64_t value)
{
    ParentNode* const p = new NODE<int64_t, Column, EQUAL>(value, column_ndx);
    UpdatePointers(p, &p->m_child);
    return *this;
}
Query& Query::not_equal(size_t column_ndx, int64_t value)
{
    ParentNode* const p = new NODE<int64_t, Column, NOTEQUAL>(value, column_ndx);
    UpdatePointers(p, &p->m_child);
    return *this;
}
Query& Query::greater(size_t column_ndx, int64_t value)
{
    ParentNode* const p = new NODE<int64_t, Column, GREATER>(value, column_ndx);
    UpdatePointers(p, &p->m_child);
    return *this;
}
Query& Query::greater_equal(size_t column_ndx, int64_t value)
{
    if(value > LLONG_MIN) {
        ParentNode* const p = new NODE<int64_t, Column, GREATER>(value - 1, column_ndx);
        UpdatePointers(p, &p->m_child);
    }
    // field >= LLONG_MIN has no effect
    return *this;
}
Query& Query::less_equal(size_t column_ndx, int64_t value)
{
    if(value < LLONG_MAX) {
        ParentNode* const p = new NODE<int64_t, Column, LESS>(value + 1, column_ndx);
        UpdatePointers(p, &p->m_child);
    }
    // field <= LLONG_MAX has no effect
    return *this;
}
Query& Query::less(size_t column_ndx, int64_t value)
{
    ParentNode* const p = new NODE<int64_t, Column, LESS>(value, column_ndx);
    UpdatePointers(p, &p->m_child);
    return *this;
}

Query& Query::between(size_t column_ndx, int64_t from, int64_t to)
{
    greater_equal(column_ndx, from);
    less_equal(column_ndx, to);
    return *this;
}
Query& Query::equal(size_t column_ndx, bool value)
{
    ParentNode* const p = new NODE<bool, Column, EQUAL>(value, column_ndx);
    UpdatePointers(p, &p->m_child);
    return *this;
}


// STRINGS
Query& Query::equal(size_t column_ndx, const char* value, bool caseSensitive)
{
    ParentNode* p;
    if(caseSensitive)
        p = new STRINGNODE<EQUAL>(value, column_ndx);
    else
        p = new STRINGNODE<EQUAL_INS>(value, column_ndx);
    UpdatePointers(p, &p->m_child);
    return *this;
}
Query& Query::begins_with(size_t column_ndx, const char* value, bool caseSensitive)
{
    ParentNode* p;
    if(caseSensitive)
        p = new STRINGNODE<BEGINSWITH>(value, column_ndx);
    else
        p = new STRINGNODE<BEGINSWITH_INS>(value, column_ndx);
    UpdatePointers(p, &p->m_child);
    return *this;
}
Query& Query::ends_with(size_t column_ndx, const char* value, bool caseSensitive)
{
    ParentNode* p;
    if(caseSensitive)
        p = new STRINGNODE<ENDSWITH>(value, column_ndx);
    else
        p = new STRINGNODE<ENDSWITH_INS>(value, column_ndx);
    UpdatePointers(p, &p->m_child);
    return *this;
}
Query& Query::contains(size_t column_ndx, const char* value, bool caseSensitive)
{
    ParentNode* p;
    if(caseSensitive)
        p = new STRINGNODE<CONTAINS>(value, column_ndx);
    else
        p = new STRINGNODE<CONTAINS_INS>(value, column_ndx);
    UpdatePointers(p, &p->m_child);
    return *this;
}
Query& Query::not_equal(size_t column_ndx, const char* value, bool caseSensitive)
{
    ParentNode* p;
    if(caseSensitive)
        p = new STRINGNODE<NOTEQUAL>(value, column_ndx);
    else
        p = new STRINGNODE<NOTEQUAL_INS>(value, column_ndx);
    UpdatePointers(p, &p->m_child);
    return *this;
}

void Query::group()
{
    update.push_back(0);
    update_override.push_back(0);
    first.push_back(0);
}
void Query::Or()
{
    ParentNode* const o = new OR_NODE(first[first.size()-1]);
    all_nodes.push_back(o);

    first[first.size()-1] = o;
    update[update.size()-1] = &((OR_NODE*)o)->m_cond2;
    update_override[update_override.size()-1] = &((OR_NODE*)o)->m_child;
}

void Query::subtable(size_t column)
{
    ParentNode* const p = new SUBTABLE(column);
    UpdatePointers(p, &p->m_child);
    // once subtable conditions have been evaluated, resume evaluation from m_child2
    subtables.push_back(&((SUBTABLE*)p)->m_child2);
    group();
}

void Query::parent()
{
    end_group();

    if (update[update.size()-1] != 0)
        update[update.size()-1] = subtables[subtables.size()-1];

    subtables.pop_back();
}

void Query::end_group()
{
    if(first.size() < 2) {
        error_code = "Unbalanced blockBegin/blockEnd";
        return;
    }

    if (update[update.size()-2] != 0)
        *update[update.size()-2] = first[first.size()-1];

    if(first[first.size()-2] == 0)
        first[first.size()-2] = first[first.size()-1];

    if(update_override[update_override.size()-1] != 0)
        update[update.size() - 2] = update_override[update_override.size()-1];
    else if(update[update.size()-1] != 0)
        update[update.size() - 2] = update[update.size()-1];

    first.pop_back();
    update.pop_back();
    update_override.pop_back();
}

size_t Query::find_next(const Table& table, size_t lastmatch)
{
    if (lastmatch == size_t(-1)) Init(table);

    const size_t end = table.size();
    const size_t res = first[0]->find_first(lastmatch + 1, end);

    return (res == end) ? -1 : res;
}

TableView Query::find_all(Table& table, size_t start, size_t end, size_t limit)
{
    Init(table);

    size_t r  = start - 1;
    if(end == size_t(-1))
        end = table.size();


    // User created query with no criteria; return everything
    if(first[0] == 0) {
        TableView tv(table);
        for(size_t i = start; i < end; i++)
            tv.get_ref_column().add(i);
        return move(tv);
    }

    if(m_threadcount > 0) {
        // Use multithreading
        return FindAllMulti(table, start, end);
    }

    TableView tv(table);

    // Use single threading
    for(;;) {
        r = first[0]->find_first(r + 1, end);
        if (r == end || tv.size() == limit)
            break;
        tv.get_ref_column().add(r);

    }

    return move(tv);
}

int64_t Query::sum(const Table& table, size_t column, size_t* resultcount, size_t start, size_t end,
            size_t limit) const
{
    Init(table);

    size_t r = start - 1;
    size_t results = 0;
    int64_t sum = 0;

    const Column& c = table.GetColumn(column);
    const size_t table_size = table.size();

    for (;;) {
        r = FindInternal(table, r + 1, end);
        if (r == size_t(-1) || r == table_size || results == limit)
            break;
        ++results;
        sum += c.Get(r);
    }

    if(resultcount != 0)
        *resultcount = results;
    return sum;
}

int64_t Query::maximum(const Table& table, size_t column, size_t* resultcount, size_t start, size_t end,
                size_t limit) const
{
    Init(table);

    size_t r = start - 1;
    size_t results = 0;
    int64_t max = 0;

    for (;;) {
        r = FindInternal(table, r + 1, end);
        if (r == size_t(-1) || r == table.size() || results == limit)
            break;
        const int64_t g = table.get_int(column, r);
        if (results == 0 || g > max)
            max = g;
        results++;
    }

    if(resultcount != 0)
        *resultcount = results;
    return max;
}

int64_t Query::minimum(const Table& table, size_t column, size_t* resultcount, size_t start, size_t end, size_t limit) const
{
    Init(table);

    size_t r = start - 1;
    size_t results = 0;
    int64_t min = 0;

    for (;;) {
        r = FindInternal(table, r + 1, end);
        if (r == size_t(-1) || r == table.size() || results == limit)
            break;
        const int64_t g = table.get_int(column, r);
        if (results == 0 || g < min)
            min = g;
        ++results;
    }
    if(resultcount != 0)
        *resultcount = results;
    return min;
}

size_t Query::count(const Table& table, size_t start, size_t end, size_t limit) const
{
    Init(table);

    size_t r = start - 1;
    size_t results = 0;

    for(;;) {
        r = FindInternal(table, r + 1, end);
        if (r == size_t(-1) || r == table.size() || results == limit)
            break;
        ++results;
    }
    return results;
}

double Query::average(const Table& table, size_t column_ndx, size_t* resultcount, size_t start, size_t end, size_t limit) const
{
    Init(table);

    size_t resultcount2;

    const int64_t sum1 = sum(table, column_ndx, &resultcount2, start, end, limit);
    const double avg1 = (float)sum1 / (float)resultcount2;

    if (resultcount != NULL)
        *resultcount = resultcount2;
    return avg1;
}

// todo, not sure if start, end and limit could be useful for delete.
size_t Query::remove(Table& table, size_t start, size_t end, size_t limit) const
{
    size_t r = start - 1;
    size_t results = 0;
    Init(table);

    for (;;) {
        r = FindInternal(table, r + 1 - results, end);
        if (r == size_t(-1) || r == table.size() || results == limit)
            break;
        ++results;
        table.remove(r);
    }
    return results;
}

TableView Query::FindAllMulti(Table& table, size_t start, size_t end)
{
#if MULTITHREAD
    // Initialization
    Init(table);
    ts.next_job = start;
    ts.end_job = end;
    ts.done_job = 0;
    ts.count = 0;
    ts.table = &table;
    ts.node = first[0];

    // Signal all threads to start
    pthread_mutex_unlock(&ts.jobs_mutex);
    pthread_cond_broadcast(&ts.jobs_cond);

    // Wait until all threads have completed
    pthread_mutex_lock(&ts.completed_mutex);
    while(ts.done_job < ts.end_job)
        pthread_cond_wait(&ts.completed_cond, &ts.completed_mutex);
    pthread_mutex_lock(&ts.jobs_mutex);
    pthread_mutex_unlock(&ts.completed_mutex);

    TableView tv(table);

    // Sort search results because user expects ascending order
    std::sort (ts.chunks.begin(), ts.chunks.end(), &Query::comp);
    for (size_t i = 0; i < ts.chunks.size(); ++i) {
        const size_t from = ts.chunks[i].first;
        const size_t upto = (i == ts.chunks.size() - 1) ? size_t(-1) : ts.chunks[i + 1].first;
        size_t first = ts.chunks[i].second;

        while(first < ts.results.size() && ts.results[first] < upto && ts.results[first] >= from) {
            tv.get_ref_column().add(ts.results[first]);
            ++first;
        }
    }

    return move(tv);
#else
    return NULL;
#endif
}

int Query::SetThreads(unsigned int threadcount)
{
#if MULTITHREAD
#if defined(_WIN32) || defined(__WIN32__) || defined(_WIN64)
    pthread_win32_process_attach_np ();
#endif
    pthread_mutex_init(&ts.result_mutex, NULL);
    pthread_cond_init(&ts.completed_cond, NULL);
    pthread_mutex_init(&ts.jobs_mutex, NULL);
    pthread_mutex_init(&ts.completed_mutex, NULL);
    pthread_cond_init(&ts.jobs_cond, NULL);

    pthread_mutex_lock(&ts.jobs_mutex);

    for (size_t i = 0; i < m_threadcount; ++i)
        pthread_detach(threads[i]);

    for (size_t i = 0; i < threadcount; ++i) {
        int r = pthread_create(&threads[i], NULL, query_thread, (void*)&ts);
        if(r != 0)
            assert(false); //todo
    }
#endif
    m_threadcount = threadcount;
    return 0;
}

#ifdef _DEBUG
std::string Query::Verify()
{
    if(first.size() == 0)
        return "";

    if(error_code != "") // errors detected by QueryInterface
        return error_code;

    if(first[0] == 0)
        return "Syntax error";

    return first[0]->Verify(); // errors detected by QueryEngine
}
#endif // _DEBUG

void Query::Init(const Table& table) const
{
    if (first[0] != NULL) {
        ParentNode* top = (ParentNode*)first[0];
        top->Init(table);
    }
}

size_t Query::FindInternal(const Table& table, size_t start, size_t end) const
{
    if (end == size_t(-1)) end = table.size();
    if (start == end) return size_t(-1);

    size_t r;
    if (first[0] != 0)
        r = first[0]->find_first(start, end);
    else
        r = start; // user built an empty query; return any first

    if (r == table.size())
        return size_t(-1);
    else
        return r;
}

void Query::UpdatePointers(ParentNode* p, ParentNode** newnode)
{
    all_nodes.push_back(p);
    if(first[first.size()-1] == 0)
        first[first.size()-1] = p;

    if(update[update.size()-1] != 0)
        *update[update.size()-1] = p;

    update[update.size()-1] = newnode;
}

bool Query::comp(const std::pair<size_t, size_t>& a, const std::pair<size_t, size_t>& b)
{
    return a.first < b.first;
}


void* Query::query_thread(void* arg)
{
#if MULTITHREAD
    thread_state* ts = (thread_state*)arg;

    std::vector<size_t> res;
    std::vector<std::pair<size_t, size_t> > chunks;

    for(;;) {
        // Main waiting loop that waits for a query to start
        pthread_mutex_lock(&ts->jobs_mutex);
        while(ts->next_job == ts->end_job)
            pthread_cond_wait(&ts->jobs_cond, &ts->jobs_mutex);
        pthread_mutex_unlock(&ts->jobs_mutex);

        for(;;) {
            // Pick a job
            pthread_mutex_lock(&ts->jobs_mutex);
            if(ts->next_job == ts->end_job)
                break;
            const size_t chunk = MIN(ts->end_job - ts->next_job, THREAD_CHUNK_SIZE);
            const size_t mine = ts->next_job;
            ts->next_job += chunk;
            size_t r = mine - 1;
            const size_t end = mine + chunk;

            pthread_mutex_unlock(&ts->jobs_mutex);

            // Execute job
            for(;;) {
                r = ts->node->find_first(r + 1, end);
                if(r == end)
                    break;
                res.push_back(r);
            }

            // Append result in common queue shared by all threads.
            pthread_mutex_lock(&ts->result_mutex);
            ts->done_job += chunk;
            if(res.size() > 0) {
                ts->chunks.push_back(std::pair<size_t, size_t>(mine, ts->results.size()));
                ts->count += res.size();
                for(size_t i = 0; i < res.size(); i++) {
                    ts->results.push_back(res[i]);
                }
                res.clear();
            }
            pthread_mutex_unlock(&ts->result_mutex);

            // Signal main thread that we might have compleeted
            pthread_mutex_lock(&ts->completed_mutex);
            pthread_cond_signal(&ts->completed_cond);
            pthread_mutex_unlock(&ts->completed_mutex);
        }
    }
#endif
    return 0;
}


