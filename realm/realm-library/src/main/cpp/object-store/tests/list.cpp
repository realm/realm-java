#include "catch.hpp"

#include "util/test_file.hpp"
#include "util/index_helpers.hpp"

#include "binding_context.hpp"
#include "list.hpp"
#include "object_schema.hpp"
#include "property.hpp"
#include "results.hpp"
#include "schema.hpp"

#include "impl/realm_coordinator.hpp"

#include <realm/commit_log.hpp>
#include <realm/group_shared.hpp>
#include <realm/link_view.hpp>

using namespace realm;

TEST_CASE("list") {
    InMemoryTestFile config;
    config.automatic_change_notifications = false;
    config.cache = false;
    config.schema = std::make_unique<Schema>(Schema{
        {"origin", "", {
            {"array", PropertyType::Array, "target"}
        }},
        {"target", "", {
            {"value", PropertyType::Int}
        }},
        {"other_origin", "", {
            {"array", PropertyType::Array, "other_target"}
        }},
        {"other_target", "", {
            {"value", PropertyType::Int}
        }},
    });

    auto r = Realm::get_shared_realm(config);
    auto& coordinator = *_impl::RealmCoordinator::get_existing_coordinator(config.path);

    auto origin = r->read_group()->get_table("class_origin");
    auto target = r->read_group()->get_table("class_target");

    r->begin_transaction();

    target->add_empty_row(10);
    for (int i = 0; i < 10; ++i)
        target->set_int(0, i, i);

    origin->add_empty_row(2);
    LinkViewRef lv = origin->get_linklist(0, 0);
    for (int i = 0; i < 10; ++i)
        lv->add(i);
    LinkViewRef lv2 = origin->get_linklist(0, 1);
    for (int i = 0; i < 10; ++i)
        lv2->add(i);

    r->commit_transaction();

    SECTION("add_notification_block()") {
        CollectionChangeSet change;
        List lst(r, *r->config().schema->find("origin"), lv);

        auto write = [&](auto&& f) {
            r->begin_transaction();
            f();
            r->commit_transaction();

            advance_and_notify(*r);
        };

        auto require_change = [&] {
            auto token = lst.add_notification_callback([&](CollectionChangeSet c, std::exception_ptr err) {
                change = c;
            });
            advance_and_notify(*r);
            return token;
        };

        auto require_no_change = [&] {
            bool first = true;
            auto token = lst.add_notification_callback([&, first](CollectionChangeSet c, std::exception_ptr err) mutable {
                REQUIRE(first);
                first = false;
            });
            advance_and_notify(*r);
            return token;
        };

        SECTION("modifying the list sends a change notifications") {
            auto token = require_change();
            write([&] { lst.remove(5); });
            REQUIRE_INDICES(change.deletions, 5);
        }

        SECTION("modifying a different list doesn't send a change notification") {
            auto token = require_no_change();
            write([&] { lv2->remove(5); });
        }

        SECTION("deleting the list sends a change notification") {
            auto token = require_change();
            write([&] { origin->move_last_over(0); });
            REQUIRE_INDICES(change.deletions, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

            // Should not resend delete all notification after another commit
            change = {};
            write([&] { target->add_empty_row(); });
            REQUIRE(change.empty());
        }

        SECTION("modifying one of the target rows sends a change notification") {
            auto token = require_change();
            write([&] { lst.get(5).set_int(0, 6); });
            REQUIRE_INDICES(change.modifications, 5);
        }

        SECTION("deleting a target row sends a change notification") {
            auto token = require_change();
            write([&] { target->move_last_over(5); });
            REQUIRE_INDICES(change.deletions, 5);
        }

        SECTION("adding a row and then modifying the target row does not mark the row as modified") {
            auto token = require_change();
            write([&] {
                lst.add(5);
                target->set_int(0, 5, 10);
            });
            REQUIRE_INDICES(change.insertions, 10);
            REQUIRE_INDICES(change.modifications, 5);
        }

        SECTION("modifying and then moving a row reports move/insert but not modification") {
            auto token = require_change();
            write([&] {
                target->set_int(0, 5, 10);
                lst.move(5, 8);
            });
            REQUIRE_INDICES(change.insertions, 8);
            REQUIRE_INDICES(change.deletions, 5);
            REQUIRE_MOVES(change, {5, 8});
            REQUIRE(change.modifications.empty());
        }

        SECTION("modifying a row which appears multiple times in a list marks them all as modified") {
            r->begin_transaction();
            lst.add(5);
            r->commit_transaction();

            auto token = require_change();
            write([&] { target->set_int(0, 5, 10); });
            REQUIRE_INDICES(change.modifications, 5, 10);
        }

        SECTION("deleting a row which appears multiple times in a list marks them all as modified") {
            r->begin_transaction();
            lst.add(5);
            r->commit_transaction();

            auto token = require_change();
            write([&] { target->move_last_over(5); });
            REQUIRE_INDICES(change.deletions, 5, 10);
        }

        SECTION("clearing the target table sends a change notification") {
            auto token = require_change();
            write([&] { target->clear(); });
            REQUIRE_INDICES(change.deletions, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        }

        SECTION("moving a target row does not send a change notification") {
            // Remove a row from the LV so that we have one to delete that's not in the list
            r->begin_transaction();
            lv->remove(2);
            r->commit_transaction();

            auto token = require_no_change();
            write([&] { target->move_last_over(2); });
        }

        SECTION("multiple LinkViws for the same LinkList can get notifications") {
            r->begin_transaction();
            target->clear();
            target->add_empty_row(5);
            r->commit_transaction();

            auto get_list = [&] {
                auto r = Realm::get_shared_realm(config);
                auto lv = r->read_group()->get_table("class_origin")->get_linklist(0, 0);
                return List(r, *r->config().schema->find("origin"), lv);
            };
            auto change_list = [&] {
                r->begin_transaction();
                if (lv->size()) {
                    target->set_int(0, lv->size() - 1, lv->size());
                }
                lv->add(lv->size());
                r->commit_transaction();
            };

            List lists[3];
            NotificationToken tokens[3];
            CollectionChangeSet changes[3];

            for (int i = 0; i < 3; ++i) {
                lists[i] = get_list();
                tokens[i] = lists[i].add_notification_callback([i, &changes](CollectionChangeSet c, std::exception_ptr) {
                    changes[i] = std::move(c);
                });
                change_list();
            }

            // Each of the Lists now has a different source version and state at
            // that version, so they should all see different changes despite
            // being for the same LinkList
            for (auto& list : lists)
                advance_and_notify(*list.get_realm());

            REQUIRE_INDICES(changes[0].insertions, 0, 1, 2);
            REQUIRE(changes[0].modifications.empty());

            REQUIRE_INDICES(changes[1].insertions, 1, 2);
            REQUIRE_INDICES(changes[1].modifications, 0);

            REQUIRE_INDICES(changes[2].insertions, 2);
            REQUIRE_INDICES(changes[2].modifications, 1);

            // After making another change, they should all get the same notification
            change_list();
            for (auto& list : lists)
                advance_and_notify(*list.get_realm());

            for (int i = 0; i < 3; ++i) {
                REQUIRE_INDICES(changes[i].insertions, 3);
                REQUIRE_INDICES(changes[i].modifications, 2);
            }
        }

        SECTION("tables-of-interest are tracked properly for multiple source versions") {
            auto other_origin = r->read_group()->get_table("class_other_origin");
            auto other_target = r->read_group()->get_table("class_other_target");

            r->begin_transaction();
            other_target->add_empty_row();
            other_origin->add_empty_row();
            LinkViewRef lv2 = other_origin->get_linklist(0, 0);
            lv2->add(0);
            r->commit_transaction();

            List lst2(r, *r->config().schema->find("other_origin"), lv2);

            // Add a callback for list1, advance the version, then add a
            // callback for list2, so that the notifiers added at each source
            // version have different tables watched for modifications
            CollectionChangeSet changes1, changes2;
            auto token1 = lst.add_notification_callback([&](CollectionChangeSet c, std::exception_ptr) {
                changes1 = std::move(c);
            });

            r->begin_transaction(); r->commit_transaction();

            auto token2 = lst2.add_notification_callback([&](CollectionChangeSet c, std::exception_ptr) {
                changes2 = std::move(c);
            });

            r->begin_transaction();
            target->set_int(0, 0, 10);
            r->commit_transaction();
            advance_and_notify(*r);

            REQUIRE_INDICES(changes1.modifications, 0);
            REQUIRE(changes2.empty());
        }

        SECTION("modifications are reported for rows that are moved and then moved back in a second transaction") {
            auto token = require_change();

            r->begin_transaction();
            lv->get(5).set_int(0, 10);
            lv->get(1).set_int(0, 10);
            lv->move(5, 8);
            lv->move(1, 2);
            r->commit_transaction();

            coordinator.on_change();

            write([&]{
                lv->move(8, 5);
            });

            REQUIRE_INDICES(change.deletions, 1);
            REQUIRE_INDICES(change.insertions, 2);
            REQUIRE_INDICES(change.modifications, 5);
            REQUIRE_MOVES(change, {1, 2});
        }
    }

    SECTION("sorted add_notification_block()") {
        List lst(r, *r->config().schema->find("origin"), lv);
        Results results = lst.sort({{0}, {false}});

        int notification_calls = 0;
        CollectionChangeSet change;
        auto token = results.add_notification_callback([&](CollectionChangeSet c, std::exception_ptr err) {
            REQUIRE_FALSE(err);
            change = c;
            ++notification_calls;
        });

        advance_and_notify(*r);

        auto write = [&](auto&& f) {
            r->begin_transaction();
            f();
            r->commit_transaction();

            advance_and_notify(*r);
        };

        SECTION("add duplicates") {
            write([&] {
                lst.add(5);
                lst.add(5);
                lst.add(5);
            });
            REQUIRE(notification_calls == 2);
            REQUIRE_INDICES(change.insertions, 5, 6, 7);
        }

        SECTION("change order by modifying target") {
            write([&] {
                lst.get(5).set_int(0, 15);
            });
            REQUIRE(notification_calls == 2);
            REQUIRE_INDICES(change.deletions, 4);
            REQUIRE_INDICES(change.insertions, 0);
        }

        SECTION("swap") {
            write([&] {
                lst.swap(1, 2);
            });
            REQUIRE(notification_calls == 1);
        }

        SECTION("move") {
            write([&] {
                lst.move(5, 3);
            });
            REQUIRE(notification_calls == 1);
        }
    }

    SECTION("filtered add_notification_block()") {
        List lst(r, *r->config().schema->find("origin"), lv);
        Results results = lst.filter(target->where().less(0, 9));

        int notification_calls = 0;
        CollectionChangeSet change;
        auto token = results.add_notification_callback([&](CollectionChangeSet c, std::exception_ptr err) {
            REQUIRE_FALSE(err);
            change = c;
            ++notification_calls;
        });

        advance_and_notify(*r);

        auto write = [&](auto&& f) {
            r->begin_transaction();
            f();
            r->commit_transaction();

            advance_and_notify(*r);
        };

        SECTION("add duplicates") {
            write([&] {
                lst.add(5);
                lst.add(5);
                lst.add(5);
            });
            REQUIRE(notification_calls == 2);
            REQUIRE_INDICES(change.insertions, 9, 10, 11);
        }

        SECTION("swap") {
            write([&] {
                lst.swap(1, 2);
            });
            REQUIRE(notification_calls == 2);
            REQUIRE_INDICES(change.deletions, 2);
            REQUIRE_INDICES(change.insertions, 1);

            write([&] {
                lst.swap(5, 8);
            });
            REQUIRE(notification_calls == 3);
            REQUIRE_INDICES(change.deletions, 5, 8);
            REQUIRE_INDICES(change.insertions, 5, 8);
        }

        SECTION("move") {
            write([&] {
                lst.move(5, 3);
            });
            REQUIRE(notification_calls == 2);
            REQUIRE_INDICES(change.deletions, 5);
            REQUIRE_INDICES(change.insertions, 3);
        }

        SECTION("move non-matching entry") {
            write([&] {
                lst.move(9, 3);
            });
            REQUIRE(notification_calls == 1);
        }
    }

    SECTION("sort()") {
        auto objectschema = &*r->config().schema->find("origin");
        List list(r, *objectschema, lv);
        auto results = list.sort({{0}, {false}});

        REQUIRE(&results.get_object_schema() == objectschema);
        REQUIRE(results.get_mode() == Results::Mode::LinkView);
        REQUIRE(results.size() == 10);
        REQUIRE(results.sum(0) == 45);

        for (size_t i = 0; i < 10; ++i) {
            REQUIRE(results.get(i).get_index() == 9 - i);
        }
    }

    SECTION("filter()") {
        auto objectschema = &*r->config().schema->find("origin");
        List list(r, *objectschema, lv);
        auto results = list.filter(target->where().greater(0, 5));

        REQUIRE(&results.get_object_schema() == objectschema);
        REQUIRE(results.get_mode() == Results::Mode::Query);
        REQUIRE(results.size() == 4);

        for (size_t i = 0; i < 4; ++i) {
            REQUIRE(results.get(i).get_index() == i + 6);
        }
    }
}
