#include "command_file.hpp"

#include "list.hpp"
#include "object_schema.hpp"
#include "property.hpp"
#include "results.hpp"
#include "schema.hpp"
#include "impl/realm_coordinator.hpp"

#include <realm/commit_log.hpp>
#include <realm/disable_sync_to_disk.hpp>
#include <realm/group_shared.hpp>
#include <realm/link_view.hpp>

#include <iostream>
#include <sstream>
#include <sys/types.h>
#include <sys/uio.h>
#include <unistd.h>

using namespace realm;

#ifndef FUZZ_SORTED
#define FUZZ_SORTED 0
#endif

#ifndef FUZZ_LINKVIEW
#define FUZZ_LINKVIEW 0
#endif

#define FUZZ_LOG 0

// Read from a fd until eof into a string
// Needs to use unbuffered i/o to work properly with afl
static void read_all(std::string& buffer, int fd)
{
    buffer.clear();
    size_t offset = 0;
    while (true) {
        buffer.resize(offset + 4096);
        ssize_t bytes_read = read(fd, &buffer[offset], 4096);
        if (bytes_read < 4096) {
            buffer.resize(offset + bytes_read);
            break;
        }
        offset += 4096;
    }
}

static Query query(fuzzer::RealmState& state)
{
#if FUZZ_LINKVIEW
    return state.table.where(state.lv);
#else
    return state.table.where().greater(1, 100).less(1, 50000);
#endif
}

static TableView tableview(fuzzer::RealmState& state)
{
    auto tv = query(state).find_all();
#if FUZZ_SORTED
    tv.sort({1, 0}, {true, true});
#endif
    return tv;
}

// Apply the changes from the command file and then return whether a change
// notification should occur
static bool apply_changes(fuzzer::CommandFile& commands, fuzzer::RealmState& state)
{
    auto tv = tableview(state);
#if FUZZ_LOG
    for (size_t i = 0; i < tv.size(); ++i)
        fprintf(stderr, "pre: %lld\n", tv.get_int(0, i));
#endif

    commands.run(state);

    auto tv2 = tableview(state);
    if (tv.size() != tv2.size())
        return true;

    for (size_t i = 0; i < tv.size(); ++i) {
#if FUZZ_LOG
        fprintf(stderr, "%lld %lld\n", tv.get_int(0, i), tv2.get_int(0, i));
#endif
        if (!tv.is_row_attached(i))
            return true;
        if (tv.get_int(0, i) != tv2.get_int(0, i))
            return true;
        if (find(begin(state.modified), end(state.modified), tv.get_int(0, i)) != end(state.modified))
            return true;
    }

    return false;
}

static auto verify(CollectionChangeIndices const& changes, std::vector<int64_t> values, fuzzer::RealmState& state)
{
    auto tv = tableview(state);

    // Apply the changes from the transaction log to our copy of the
    // initial, using UITableView's batching rules (i.e. delete, then
    // insert, then update)
    auto it = util::make_reverse_iterator(changes.deletions.end());
    auto end = util::make_reverse_iterator(changes.deletions.begin());
    for (; it != end; ++it) {
        values.erase(values.begin() + it->first, values.begin() + it->second);
    }

    for (auto i : changes.insertions.as_indexes()) {
        values.insert(values.begin() + i, tv.get_int(1, i));
    }

    if (values.size() != tv.size()) {
        abort();
    }

    for (auto i : changes.modifications.as_indexes()) {
        if (changes.insertions.contains(i))
            abort();
        values[i] = tv.get_int(1, i);
    }

#if FUZZ_SORTED
    if (!std::is_sorted(values.begin(), values.end()))
        abort();
#endif

    for (size_t i = 0; i < values.size(); ++i) {
        if (values[i] != tv.get_int(1, i)) {
#if FUZZ_LOG
            fprintf(stderr, "%lld %lld\n", values[i], tv.get_int(1, i));
#endif
            abort();
        }
    }

    return values;
}

static void verify_no_op(CollectionChangeIndices const& changes, std::vector<int64_t> values, fuzzer::RealmState& state)
{
    auto new_values = verify(changes, values, state);
    if (!std::equal(begin(values), end(values), begin(new_values), end(new_values)))
        abort();
}

static void test(Realm::Config const& config, SharedRealm& r, SharedRealm& r2, std::istream& input_stream)
{
    fuzzer::RealmState state = {
        *r,
        *_impl::RealmCoordinator::get_existing_coordinator(r->config().path),
        *r->read_group()->get_table("class_object"),
        r->read_group()->get_table("class_linklist")->get_linklist(0, 0),
        0,
        {}
    };

    fuzzer::CommandFile command(input_stream);
    if (command.initial_values.empty()) {
        return;
    }
    command.import(state);

    fuzzer::RealmState state2 = {
        *r2,
        state.coordinator,
        *r2->read_group()->get_table("class_object"),
#if FUZZ_LINKVIEW
        r2->read_group()->get_table("class_linklist")->get_linklist(0, 0),
#else
        {},
#endif
        state.uid,
        {}
    };

#if FUZZ_LINKVIEW && !FUZZ_SORTED
    auto results = List(r, ObjectSchema(), state.lv);
#else
    auto results = Results(r, ObjectSchema(), query(state))
#if FUZZ_SORTED
        .sort({{1, 0}, {true, true}})
#endif
        ;
#endif // FUZZ_LINKVIEW

    std::vector<int64_t> initial_values;
    for (size_t i = 0; i < results.size(); ++i)
        initial_values.push_back(results.get(i).get_int(1));

    CollectionChangeIndices changes;
    int notification_calls = 0;
    auto token = results.add_notification_callback([&](CollectionChangeIndices c, std::exception_ptr err) {
        if (notification_calls > 0 && c.empty())
            abort();
        changes = c;
        ++notification_calls;
    });

    state.coordinator.on_change(); r->notify();
    if (notification_calls != 1) {
        abort();
    }

    bool expect_notification = apply_changes(command, state2);
    state.coordinator.on_change(); r->notify();

    if (expect_notification) {
        if (notification_calls != 2)
            abort();
        verify(changes, initial_values, state);
    }
    else {
        if (notification_calls == 2)
            verify_no_op(changes, initial_values, state);
    }
}

int main(int argc, char** argv) {
    std::ios_base::sync_with_stdio(false);
    realm::disable_sync_to_disk();

    Realm::Config config;
    config.path = "fuzzer.realm";
    config.cache = false;
    config.in_memory = true;
    config.automatic_change_notifications = false;

    Schema schema{
        {"object", "", {
            {"id", PropertyTypeInt},
            {"value", PropertyTypeInt}
        }},
        {"linklist", "", {
            {"list", PropertyTypeArray, "object"}
        }}
    };

    config.schema = std::make_unique<Schema>(schema);
    unlink(config.path.c_str());

    auto r = Realm::get_shared_realm(config);
    auto r2 = Realm::get_shared_realm(config);
    auto& coordinator = *_impl::RealmCoordinator::get_existing_coordinator(config.path);

    r->begin_transaction();
    r->read_group()->get_table("class_linklist")->add_empty_row();
    r->commit_transaction();

    auto test_on = [&](auto& buffer) {
        std::istringstream ss(buffer);
        test(config, r, r2, ss);
        if (r->is_in_transaction())
            r->cancel_transaction();
        r2->invalidate();
        coordinator.on_change();
    };

    if (argc > 1) {
        std::string buffer;
        for (int i = 1; i < argc; ++i) {
            int fd = open(argv[i], O_RDONLY);
            if (fd < 0)
                abort();
            read_all(buffer, fd);
            close(fd);

            test_on(buffer);
        }
        unlink(config.path.c_str());
        return 0;
    }

#ifdef __AFL_HAVE_MANUAL_CONTROL
    std::string buffer;
    while (__AFL_LOOP(1000)) {
        read_all(buffer, 0);
        test_on(buffer);
    }
#else
    std::string buffer;
    read_all(buffer, 0);
    test_on(buffer);
#endif

    unlink(config.path.c_str());
    return 0;
}
