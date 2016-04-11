#include "catch.hpp"

#include "util/test_file.hpp"

#include "impl/realm_coordinator.hpp"
#include "object_schema.hpp"
#include "property.hpp"
#include "results.hpp"
#include "schema.hpp"

#include <realm/commit_log.hpp>
#include <realm/group_shared.hpp>
#include <realm/link_view.hpp>

using namespace realm;

TEST_CASE("Results") {
    InMemoryTestFile config;
    config.cache = false;
    config.automatic_change_notifications = false;
    config.schema = std::make_unique<Schema>(Schema{
        {"object", "", {
            {"value", PropertyTypeInt},
            {"link", PropertyTypeObject, "linked to object", false, false, true}
        }},
        {"other object", "", {
            {"value", PropertyTypeInt}
        }},
        {"linking object", "", {
            {"link", PropertyTypeObject, "object", false, false, true}
        }},
        {"linked to object", "", {
            {"value", PropertyTypeInt}
        }}
    });

    auto r = Realm::get_shared_realm(config);
    auto coordinator = _impl::RealmCoordinator::get_existing_coordinator(config.path);
    auto table = r->read_group()->get_table("class_object");

    r->begin_transaction();
    table->add_empty_row(10);
    for (int i = 0; i < 10; ++i)
        table->set_int(0, i, i);
    r->commit_transaction();

    Results results(r, *config.schema->find("object"), table->where().greater(0, 0).less(0, 5));

    SECTION("notifications") {
        int notification_calls = 0;
        auto token = results.async([&](std::exception_ptr err) {
            REQUIRE_FALSE(err);
            ++notification_calls;
        });

        coordinator->on_change();
        r->notify();

        SECTION("initial results are delivered") {
            REQUIRE(notification_calls == 1);
        }

        SECTION("modifying the table sends a notification asynchronously") {
            r->begin_transaction();
            table->set_int(0, 0, 0);
            r->commit_transaction();

            REQUIRE(notification_calls == 1);
            coordinator->on_change();
            r->notify();
            REQUIRE(notification_calls == 2);
        }

        SECTION("modifying a linked-to table send a notification") {
            r->begin_transaction();
            r->read_group()->get_table("class_linked to object")->add_empty_row();
            r->commit_transaction();

            REQUIRE(notification_calls == 1);
            coordinator->on_change();
            r->notify();
            REQUIRE(notification_calls == 2);
        }

        SECTION("modifying a a linking table sends a notification") {
            r->begin_transaction();
            r->read_group()->get_table("class_linking object")->add_empty_row();
            r->commit_transaction();

            REQUIRE(notification_calls == 1);
            coordinator->on_change();
            r->notify();
            REQUIRE(notification_calls == 2);
        }

        SECTION("modifying a an unrelated table does not send a notification") {
            r->begin_transaction();
            r->read_group()->get_table("class_other object")->add_empty_row();
            r->commit_transaction();

            REQUIRE(notification_calls == 1);
            coordinator->on_change();
            r->notify();
            REQUIRE(notification_calls == 1);
        }

        SECTION("modifications from multiple transactions are collapsed") {
            r->begin_transaction();
            table->set_int(0, 0, 0);
            r->commit_transaction();

            r->begin_transaction();
            table->set_int(0, 1, 0);
            r->commit_transaction();

            REQUIRE(notification_calls == 1);
            coordinator->on_change();
            r->notify();
            REQUIRE(notification_calls == 2);
        }

        SECTION("notifications are not delivered when the token is destroyed before they are calculated") {
            r->begin_transaction();
            table->set_int(0, 0, 0);
            r->commit_transaction();

            REQUIRE(notification_calls == 1);
            token = {};
            coordinator->on_change();
            r->notify();
            REQUIRE(notification_calls == 1);
        }

        SECTION("notifications are not delivered when the token is destroyed before they are delivered") {
            r->begin_transaction();
            table->set_int(0, 0, 0);
            r->commit_transaction();

            REQUIRE(notification_calls == 1);
            coordinator->on_change();
            token = {};
            r->notify();
            REQUIRE(notification_calls == 1);
        }
    }
}
