#include "command_file.hpp"

#include "impl/realm_coordinator.hpp"
#include "shared_realm.hpp"

#include <realm/link_view.hpp>
#include <realm/table.hpp>

#include <istream>

using namespace fuzzer;
using namespace realm;

#if 0
#define log(...) fprintf(stderr, __VA_ARGS__)
#else
#define log(...)
#endif

template<typename T>
static T read_value(std::istream& input)
{
    T ret;
    input >> ret;
    return ret;
}

template<typename... Args>
static auto make_reader(void (*fn)(RealmState&, Args...)) {
    return [=](std::istream& input) {
        return std::bind(fn, std::placeholders::_1, read_value<Args>(input)...);
    };
}

static void run_add(RealmState& state, int64_t value)
{
    log("add %lld\n", value);
    size_t ndx = state.table.add_empty_row();
    state.table.set_int(0, ndx, state.uid++);
    state.table.set_int(1, ndx, value);
}

static void run_modify(RealmState& state, size_t index, int64_t value)
{
    if (index < state.table.size()) {
        log("modify %zu %lld\n", index, value);
        state.table.set_int(1, index, value);
        state.modified.push_back(state.table.get_int(0, index));
    }
}

static void run_delete(RealmState& state, size_t index)
{
    if (index < state.table.size()) {
        log("delete %zu (%lld)\n", index, state.table.get_int(1, index));
        state.table.move_last_over(index);
    }
}

static void run_commit(RealmState& state)
{
    log("commit\n");
    state.realm.commit_transaction();
    state.coordinator.on_change();
    state.realm.begin_transaction();
}

static void run_lv_insert(RealmState& state, size_t pos, size_t target)
{
    if (!state.lv) return;
    if (target < state.table.size() && pos <= state.lv->size()) {
        log("lv insert %zu %zu\n", pos, target);
        state.lv->insert(pos, target);
    }
}

static void run_lv_set(RealmState& state, size_t pos, size_t target)
{
    if (!state.lv) return;
    if (target < state.table.size() && pos < state.lv->size()) {
        log("lv set %zu %zu\n", pos, target);
        // We can't reliably detect self-assignment for verification, so don't do it
        if (state.lv->get(pos).get_index() != target)
            state.lv->set(pos, target);
    }
}

static void run_lv_move(RealmState& state, size_t from, size_t to)
{
    if (!state.lv) return;
    if (from < state.lv->size() && to < state.lv->size()) {
        log("lv move %zu %zu\n", from, to);
        // FIXME: only do the move if it has an effect to avoid getting a
        // notification which we weren't expecting. This is really urgh.
        for (size_t i = std::min(from, to); i < std::max(from, to); ++i) {
            if (state.lv->get(i).get_index() != state.lv->get(i + 1).get_index()) {
                state.lv->move(from, to);
                break;
            }
        }
    }
}

static void run_lv_swap(RealmState& state, size_t ndx1, size_t ndx2)
{
    if (!state.lv) return;
    if (ndx1 < state.lv->size() && ndx2 < state.lv->size()) {
        log("lv swap %zu %zu\n", ndx1, ndx2);
        if (state.lv->get(ndx1).get_index() != state.lv->get(ndx2).get_index()) {
            state.lv->swap(ndx1, ndx2);
            // FIXME: swap() needs to produce moves so that a pair of swaps can
            // be collapsed away. Currently it just marks the rows as modified.
            state.modified.push_back(state.lv->get(ndx1).get_int(0));
            state.modified.push_back(state.lv->get(ndx2).get_int(0));
        }
    }
}

static void run_lv_remove(RealmState& state, size_t pos)
{
    if (!state.lv) return;
    if (pos < state.lv->size()) {
        log("lv remove %zu\n", pos);
        state.lv->remove(pos);
    }
}

static void run_lv_remove_target(RealmState& state, size_t pos)
{
    if (!state.lv) return;
    if (pos < state.lv->size()) {
        log("lv target remove %zu\n", pos);
        state.lv->remove_target_row(pos);
    }
}

static std::map<char, std::function<std::function<void (RealmState&)>(std::istream&)>> readers = {
    // Row functions
    {'a', make_reader(run_add)},
    {'c', make_reader(run_commit)},
    {'d', make_reader(run_delete)},
    {'m', make_reader(run_modify)},

    // LinkView functions
    {'i', make_reader(run_lv_insert)},
    {'s', make_reader(run_lv_set)},
    {'o', make_reader(run_lv_move)},
    {'w', make_reader(run_lv_swap)},
    {'r', make_reader(run_lv_remove)},
    {'t', make_reader(run_lv_remove_target)},
};

template<typename T>
static std::vector<T> read_int_list(std::istream& input_stream)
{
    std::vector<T> ret;
    std::string line;
    while (std::getline(input_stream, line) && !line.empty()) {
        try {
            ret.push_back(std::stoll(line));
            log("%lld\n", (long long)ret.back());
        }
        catch (std::invalid_argument) {
            // not an error
        }
        catch (std::out_of_range) {
            // not an error
        }
    }
    log("\n");
    return ret;
}

CommandFile::CommandFile(std::istream& input)
: initial_values(read_int_list<int64_t>(input))
, initial_list_indices(read_int_list<size_t>(input))
{
    if (!input.good())
        return;

    while (input.good()) {
        char op = '\0';
        input >> op;
        if (!input.good())
            break;

        auto it = readers.find(op);
        if (it == readers.end())
            continue;

        auto fn = it->second(input);
        if (!input.good())
            return;
        commands.push_back(std::move(fn));
    }
}

void CommandFile::import(RealmState& state)
{
    auto& table = state.table;

    state.realm.begin_transaction();

    table.clear();
    size_t ndx = table.add_empty_row(initial_values.size());
    for (auto value : initial_values) {
        table.set_int(0, ndx, state.uid++);
        table.set_int(1, ndx++, value);
    }

    state.lv->clear();
    for (auto value : initial_list_indices) {
        if (value < table.size())
            state.lv->add(value);
    }

    state.realm.commit_transaction();

}

void CommandFile::run(RealmState& state)
{
    state.realm.begin_transaction();
    for (auto& command : commands) {
        command(state);
    }
    state.realm.commit_transaction();
}
