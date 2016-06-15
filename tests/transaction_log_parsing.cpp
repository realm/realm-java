#include "catch.hpp"

#include "util/index_helpers.hpp"
#include "util/test_file.hpp"

#include "impl/collection_notifier.hpp"
#include "impl/transact_log_handler.hpp"
#include "property.hpp"
#include "object_schema.hpp"
#include "schema.hpp"

#include <realm/commit_log.hpp>
#include <realm/group_shared.hpp>
#include <realm/link_view.hpp>

using namespace realm;

class CaptureHelper {
public:
    CaptureHelper(std::string const& path, SharedRealm const& r, LinkViewRef lv)
    : m_history(make_client_history(path))
    , m_sg(*m_history, SharedGroup::durability_MemOnly)
    , m_realm(r)
    , m_group(m_sg.begin_read())
    , m_linkview(lv)
    {
        m_realm->begin_transaction();

        m_initial.reserve(lv->size());
        for (size_t i = 0; i < lv->size(); ++i)
            m_initial.push_back(lv->get(i).get_int(0));
    }

    CollectionChangeSet finish(size_t ndx) {
        m_realm->commit_transaction();

        _impl::CollectionChangeBuilder c;
        _impl::TransactionChangeInfo info;
        info.lists.push_back({ndx, 0, 0, &c});
        info.table_modifications_needed.resize(m_group.size(), true);
        info.table_moves_needed.resize(m_group.size(), true);
        _impl::transaction::advance(m_sg, info);

        if (info.lists.empty()) {
            REQUIRE(!m_linkview->is_attached());
            return {};
        }

        validate(c);
        return c;
    }

    explicit operator bool() const { return m_realm->is_in_transaction(); }

private:
    std::unique_ptr<Replication> m_history;
    SharedGroup m_sg;
    SharedRealm m_realm;
    Group const& m_group;

    LinkViewRef m_linkview;
    std::vector<int> m_initial;

    void validate(CollectionChangeSet const& info)
    {
        info.insertions.verify();
        info.deletions.verify();
        info.modifications.verify();

        std::vector<size_t> move_sources;
        for (auto const& move : info.moves)
            move_sources.push_back(m_initial[move.from]);

        // Apply the changes from the transaction log to our copy of the
        // initial, using UITableView's batching rules (i.e. delete, then
        // insert, then update)
        auto it = util::make_reverse_iterator(info.deletions.end());
        auto end = util::make_reverse_iterator(info.deletions.begin());
        for (; it != end; ++it) {
            m_initial.erase(m_initial.begin() + it->first, m_initial.begin() + it->second);
        }

        for (auto const& range : info.insertions) {
            for (auto i = range.first; i < range.second; ++i)
                m_initial.insert(m_initial.begin() + i, m_linkview->get(i).get_int(0));
        }

        for (auto const& range : info.modifications) {
            for (auto i = range.first; i < range.second; ++i)
                m_initial[i] = m_linkview->get(i).get_int(0);
        }

        REQUIRE(m_linkview->is_attached());

        // and make sure we end up with the same end result
        REQUIRE(m_initial.size() == m_linkview->size());
        for (size_t i = 0; i < m_initial.size(); ++i)
            CHECK(m_initial[i] == m_linkview->get(i).get_int(0));

        // Verify that everything marked as a move actually is one
        for (size_t i = 0; i < move_sources.size(); ++i) {
            if (!info.modifications.contains(info.moves[i].to)) {
                CHECK(m_linkview->get(info.moves[i].to).get_int(0) == move_sources[i]);
            }
        }
    }
};

TEST_CASE("Transaction log parsing") {
    InMemoryTestFile config;
    config.automatic_change_notifications = false;

    SECTION("schema change validation") {
        config.schema = std::make_unique<Schema>(Schema{
            {"table", "", {
                {"unindexed", PropertyType::Int},
                {"indexed", PropertyType::Int, "", "", false, true}
            }},
        });
        auto r = Realm::get_shared_realm(config);
        r->read_group();

        auto history = make_client_history(config.path);
        SharedGroup sg(*history, SharedGroup::durability_MemOnly);

        SECTION("adding a table is allowed") {
            WriteTransaction wt(sg);
            TableRef table = wt.add_table("new table");
            table->add_column(type_String, "new col");
            wt.commit();

            REQUIRE_NOTHROW(r->refresh());
        }

        SECTION("adding an index to an existing column is allowed") {
            WriteTransaction wt(sg);
            TableRef table = wt.get_table("class_table");
            table->add_search_index(0);
            wt.commit();

            REQUIRE_NOTHROW(r->refresh());
        }

        SECTION("removing an index from an existing column is allowed") {
            WriteTransaction wt(sg);
            TableRef table = wt.get_table("class_table");
            table->remove_search_index(1);
            wt.commit();

            REQUIRE_NOTHROW(r->refresh());
        }

        SECTION("adding a column to an existing table is not allowed (but eventually should be)") {
            WriteTransaction wt(sg);
            TableRef table = wt.get_table("class_table");
            table->add_column(type_String, "new col");
            wt.commit();

            REQUIRE_THROWS(r->refresh());
        }

        SECTION("removing a column is not allowed") {
            WriteTransaction wt(sg);
            TableRef table = wt.get_table("class_table");
            table->remove_column(1);
            wt.commit();

            REQUIRE_THROWS(r->refresh());
        }

        SECTION("removing a table is not allowed") {
            WriteTransaction wt(sg);
            wt.get_group().remove_table("class_table");
            wt.commit();

            REQUIRE_THROWS(r->refresh());
        }
    }

    SECTION("table change information") {
        config.schema = std::make_unique<Schema>(Schema{
            {"table", "", {
                {"value", PropertyType::Int}
            }},
        });

        auto r = Realm::get_shared_realm(config);
        auto& table = *r->read_group()->get_table("class_table");

        r->begin_transaction();
        table.add_empty_row(10);
        for (int i = 0; i < 10; ++i)
            table.set_int(0, i, i);
        r->commit_transaction();

        auto track_changes = [&](std::vector<bool> tables_needed, auto&& f) {
            auto history = make_client_history(config.path);
            SharedGroup sg(*history, SharedGroup::durability_MemOnly);
            sg.begin_read();

            r->begin_transaction();
            f();
            r->commit_transaction();

            _impl::TransactionChangeInfo info;
            info.table_modifications_needed = tables_needed;
            info.table_moves_needed = tables_needed;
            _impl::transaction::advance(sg, info);
            return info;
        };

        SECTION("modifying a row marks it as modified") {
            auto info = track_changes({false, false, true}, [&] {
                table.set_int(0, 1, 2);
            });
            REQUIRE(info.tables.size() == 3);
            REQUIRE_INDICES(info.tables[2].modifications, 1);
        }

        SECTION("modifications to untracked tables are ignored") {
            auto info = track_changes({false, false, false}, [&] {
                table.set_int(0, 1, 2);
            });
            REQUIRE(info.tables.empty());
        }

        SECTION("new row additions are reported") {
            auto info = track_changes({false, false, true}, [&] {
                table.add_empty_row();
                table.add_empty_row();
            });
            REQUIRE(info.tables.size() == 3);
            REQUIRE_INDICES(info.tables[2].insertions, 10, 11);
        }

        SECTION("deleting newly added rows makes them not be reported") {
            auto info = track_changes({false, false, true}, [&] {
                table.add_empty_row();
                table.add_empty_row();
                table.move_last_over(11);
            });
            REQUIRE(info.tables.size() == 3);
            REQUIRE_INDICES(info.tables[2].insertions, 10);
            REQUIRE(info.tables[2].deletions.empty());
        }

        SECTION("modifying newly added rows is reported as a modification") {
            auto info = track_changes({false, false, true}, [&] {
                table.add_empty_row();
                table.set_int(0, 10, 10);
            });
            REQUIRE(info.tables.size() == 3);
            REQUIRE_INDICES(info.tables[2].insertions, 10);
            REQUIRE_INDICES(info.tables[2].modifications, 10);
        }

        SECTION("move_last_over() does not shift rows other than the last one") {
            auto info = track_changes({false, false, true}, [&] {
                table.move_last_over(2);
                table.move_last_over(3);
            });
            REQUIRE(info.tables.size() == 3);
            REQUIRE_INDICES(info.tables[2].deletions, 2, 3, 8, 9);
            REQUIRE_INDICES(info.tables[2].insertions, 2, 3);
            REQUIRE_MOVES(info.tables[2], {8, 3}, {9, 2});
        }
    }

    SECTION("LinkView change information") {
        config.schema = std::make_unique<Schema>(Schema{
            {"origin", "", {
                {"array", PropertyType::Array, "target"}
            }},
            {"target", "", {
                {"value", PropertyType::Int}
            }},
        });

        auto r = Realm::get_shared_realm(config);

        auto origin = r->read_group()->get_table("class_origin");
        auto target = r->read_group()->get_table("class_target");

        r->begin_transaction();

        target->add_empty_row(10);
        for (int i = 0; i < 10; ++i)
            target->set_int(0, i, i);

        origin->add_empty_row();
        LinkViewRef lv = origin->get_linklist(0, 0);
        for (int i = 0; i < 10; ++i)
            lv->add(i);

        r->commit_transaction();

#define VALIDATE_CHANGES(out) \
    for (CaptureHelper helper(config.path, r, lv); helper; out = helper.finish(origin->get_index_in_group()))

        CollectionChangeSet changes;
        SECTION("single change type") {
            SECTION("add single") {
                VALIDATE_CHANGES(changes) {
                    lv->add(0);
                }
                REQUIRE_INDICES(changes.insertions, 10);
            }
            SECTION("add multiple") {
                VALIDATE_CHANGES(changes) {
                    lv->add(0);
                    lv->add(0);
                }
                REQUIRE_INDICES(changes.insertions, 10, 11);
            }

            SECTION("erase single") {
                VALIDATE_CHANGES(changes) {
                    lv->remove(5);
                }
                REQUIRE_INDICES(changes.deletions, 5);
            }
            SECTION("erase contiguous forward") {
                VALIDATE_CHANGES(changes) {
                    lv->remove(5);
                    lv->remove(5);
                    lv->remove(5);
                }
                REQUIRE_INDICES(changes.deletions, 5, 6, 7);
            }
            SECTION("erase contiguous reverse") {
                VALIDATE_CHANGES(changes) {
                    lv->remove(7);
                    lv->remove(6);
                    lv->remove(5);
                }
                REQUIRE_INDICES(changes.deletions, 5, 6, 7);
            }
            SECTION("erase contiguous mixed") {
                VALIDATE_CHANGES(changes) {
                    lv->remove(5);
                    lv->remove(6);
                    lv->remove(5);
                }
                REQUIRE_INDICES(changes.deletions, 5, 6, 7);
            }
            SECTION("erase scattered forward") {
                VALIDATE_CHANGES(changes) {
                    lv->remove(3);
                    lv->remove(4);
                    lv->remove(5);
                }
                REQUIRE_INDICES(changes.deletions, 3, 5, 7);
            }
            SECTION("erase scattered backwards") {
                VALIDATE_CHANGES(changes) {
                    lv->remove(7);
                    lv->remove(5);
                    lv->remove(3);
                }
                REQUIRE_INDICES(changes.deletions, 3, 5, 7);
            }
            SECTION("erase scattered mixed") {
                VALIDATE_CHANGES(changes) {
                    lv->remove(3);
                    lv->remove(6);
                    lv->remove(4);
                }
                REQUIRE_INDICES(changes.deletions, 3, 5, 7);
            }

            SECTION("set single") {
                VALIDATE_CHANGES(changes) {
                    lv->set(5, 0);
                }
                REQUIRE_INDICES(changes.modifications, 5);
            }
            SECTION("set contiguous") {
                VALIDATE_CHANGES(changes) {
                    lv->set(5, 0);
                    lv->set(6, 0);
                    lv->set(7, 0);
                }
                REQUIRE_INDICES(changes.modifications, 5, 6, 7);
            }
            SECTION("set scattered") {
                VALIDATE_CHANGES(changes) {
                    lv->set(5, 0);
                    lv->set(7, 0);
                    lv->set(9, 0);
                }
                REQUIRE_INDICES(changes.modifications, 5, 7, 9);
            }
            SECTION("set redundant") {
                VALIDATE_CHANGES(changes) {
                    lv->set(5, 0);
                    lv->set(5, 0);
                    lv->set(5, 0);
                }
                REQUIRE_INDICES(changes.modifications, 5);
            }

            SECTION("clear") {
                VALIDATE_CHANGES(changes) {
                    lv->clear();
                }
                REQUIRE_INDICES(changes.deletions, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            }

            SECTION("move backward") {
                VALIDATE_CHANGES(changes) {
                    lv->move(5, 3);
                }
                REQUIRE_MOVES(changes, {5, 3});
            }

            SECTION("move forward") {
                VALIDATE_CHANGES(changes) {
                    lv->move(1, 3);
                }
                REQUIRE_MOVES(changes, {1, 3});
            }

            SECTION("chained moves") {
                VALIDATE_CHANGES(changes) {
                    lv->move(1, 3);
                    lv->move(3, 5);
                }
                REQUIRE_MOVES(changes, {1, 5});
            }

            SECTION("backwards chained moves") {
                VALIDATE_CHANGES(changes) {
                    lv->move(5, 3);
                    lv->move(3, 1);
                }
                REQUIRE_MOVES(changes, {5, 1});
            }

            SECTION("moves shifting other moves") {
                VALIDATE_CHANGES(changes) {
                    lv->move(1, 5);
                    lv->move(2, 7);
                }
                REQUIRE_MOVES(changes, {1, 4}, {3, 7});

                VALIDATE_CHANGES(changes) {
                    lv->move(1, 5);
                    lv->move(7, 0);
                }
                REQUIRE_MOVES(changes, {1, 6}, {7, 0});
            }

            SECTION("move to current location is a no-op") {
                VALIDATE_CHANGES(changes) {
                    lv->move(5, 5);
                }
                REQUIRE(changes.insertions.empty());
                REQUIRE(changes.deletions.empty());
                REQUIRE(changes.moves.empty());
            }

            SECTION("delete a target row") {
                VALIDATE_CHANGES(changes) {
                    target->move_last_over(5);
                }
                REQUIRE_INDICES(changes.deletions, 5);
            }

            SECTION("delete all target rows") {
                VALIDATE_CHANGES(changes) {
                    lv->remove_all_target_rows();
                }
                REQUIRE_INDICES(changes.deletions, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            }

            SECTION("clear target table") {
                VALIDATE_CHANGES(changes) {
                    target->clear();
                }
                REQUIRE_INDICES(changes.deletions, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            }

            SECTION("swap()") {
                VALIDATE_CHANGES(changes) {
                    lv->swap(3, 5);
                }
                REQUIRE_INDICES(changes.modifications, 3, 5);
            }
        }

        SECTION("mixed change types") {
            SECTION("set -> insert") {
                VALIDATE_CHANGES(changes) {
                    lv->set(5, 0);
                    lv->insert(5, 0);
                }
                REQUIRE_INDICES(changes.insertions, 5);
                REQUIRE_INDICES(changes.modifications, 6);

                VALIDATE_CHANGES(changes) {
                    lv->set(4, 0);
                    lv->insert(5, 0);
                }
                REQUIRE_INDICES(changes.insertions, 5);
                REQUIRE_INDICES(changes.modifications, 4);
            }
            SECTION("insert -> set") {
                VALIDATE_CHANGES(changes) {
                    lv->insert(5, 0);
                    lv->set(5, 1);
                }
                REQUIRE_INDICES(changes.insertions, 5);
                REQUIRE_INDICES(changes.modifications, 5);

                VALIDATE_CHANGES(changes) {
                    lv->insert(5, 0);
                    lv->set(6, 1);
                }
                REQUIRE_INDICES(changes.insertions, 5);
                REQUIRE_INDICES(changes.modifications, 6);

                VALIDATE_CHANGES(changes) {
                    lv->insert(6, 0);
                    lv->set(5, 1);
                }
                REQUIRE_INDICES(changes.insertions, 6);
                REQUIRE_INDICES(changes.modifications, 5);
            }

            SECTION("set -> erase") {
                VALIDATE_CHANGES(changes) {
                    lv->set(5, 0);
                    lv->remove(5);
                }
                REQUIRE_INDICES(changes.deletions, 5);
                REQUIRE(changes.modifications.empty());

                VALIDATE_CHANGES(changes) {
                    lv->set(5, 0);
                    lv->remove(4);
                }
                REQUIRE_INDICES(changes.deletions, 4);
                REQUIRE_INDICES(changes.modifications, 4);

                VALIDATE_CHANGES(changes) {
                    lv->set(5, 0);
                    lv->remove(4);
                    lv->remove(4);
                }
                REQUIRE_INDICES(changes.deletions, 4, 5);
                REQUIRE(changes.modifications.empty());
            }

            SECTION("erase -> set") {
                VALIDATE_CHANGES(changes) {
                    lv->remove(5);
                    lv->set(5, 0);
                }
                REQUIRE_INDICES(changes.deletions, 5);
                REQUIRE_INDICES(changes.modifications, 5);
            }

            SECTION("insert -> clear") {
                VALIDATE_CHANGES(changes) {
                    lv->add(0);
                    lv->clear();
                }
                REQUIRE_INDICES(changes.deletions, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
                REQUIRE(changes.insertions.empty());
            }

            SECTION("set -> clear") {
                VALIDATE_CHANGES(changes) {
                    lv->set(0, 5);
                    lv->clear();
                }
                REQUIRE_INDICES(changes.deletions, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
                REQUIRE(changes.modifications.empty());
            }

            SECTION("clear -> insert") {
                VALIDATE_CHANGES(changes) {
                    lv->clear();
                    lv->add(0);
                }
                REQUIRE_INDICES(changes.deletions, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
                REQUIRE_INDICES(changes.insertions, 0);
            }

            SECTION("insert -> delete") {
                VALIDATE_CHANGES(changes) {
                    lv->add(0);
                    lv->remove(10);
                }
                REQUIRE(changes.insertions.empty());
                REQUIRE(changes.deletions.empty());

                VALIDATE_CHANGES(changes) {
                    lv->add(0);
                    lv->remove(9);
                }
                REQUIRE_INDICES(changes.deletions, 9);
                REQUIRE_INDICES(changes.insertions, 9);

                VALIDATE_CHANGES(changes) {
                    lv->insert(1, 1);
                    lv->insert(3, 3);
                    lv->insert(5, 5);
                    lv->remove(6);
                    lv->remove(4);
                    lv->remove(2);
                }
                REQUIRE_INDICES(changes.deletions, 1, 2, 3);
                REQUIRE_INDICES(changes.insertions, 1, 2, 3);

                VALIDATE_CHANGES(changes) {
                    lv->insert(1, 1);
                    lv->insert(3, 3);
                    lv->insert(5, 5);
                    lv->remove(2);
                    lv->remove(3);
                    lv->remove(4);
                }
                REQUIRE_INDICES(changes.deletions, 1, 2, 3);
                REQUIRE_INDICES(changes.insertions, 1, 2, 3);
            }

            SECTION("delete -> insert") {
                VALIDATE_CHANGES(changes) {
                    lv->remove(9);
                    lv->add(0);
                }
                REQUIRE_INDICES(changes.deletions, 9);
                REQUIRE_INDICES(changes.insertions, 9);
            }

            SECTION("interleaved delete and insert") {
                VALIDATE_CHANGES(changes) {
                    lv->remove(9);
                    lv->remove(7);
                    lv->remove(5);
                    lv->remove(3);
                    lv->remove(1);

                    lv->insert(4, 9);
                    lv->insert(3, 7);
                    lv->insert(2, 5);
                    lv->insert(1, 3);
                    lv->insert(0, 1);

                    lv->remove(9);
                    lv->remove(7);
                    lv->remove(5);
                    lv->remove(3);
                    lv->remove(1);
                }

                REQUIRE_INDICES(changes.deletions, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
                REQUIRE_INDICES(changes.insertions, 0, 1, 2, 3, 4);
            }

            SECTION("move after set is just insert+delete") {
                VALIDATE_CHANGES(changes) {
                    lv->set(5, 6);
                    lv->move(5, 0);
                }

                REQUIRE_INDICES(changes.deletions, 5);
                REQUIRE_INDICES(changes.insertions, 0);
                REQUIRE_MOVES(changes, {5, 0});
            }

            SECTION("set after move is just insert+delete") {
                VALIDATE_CHANGES(changes) {
                    lv->move(5, 0);
                    lv->set(0, 6);
                }

                REQUIRE_INDICES(changes.deletions, 5);
                REQUIRE_INDICES(changes.insertions, 0);
                REQUIRE_MOVES(changes, {5, 0});
            }

            SECTION("delete after move removes original row") {
                VALIDATE_CHANGES(changes) {
                    lv->move(5, 0);
                    lv->remove(0);
                }

                REQUIRE_INDICES(changes.deletions, 5);
                REQUIRE(changes.moves.empty());
            }

            SECTION("moving newly inserted row just changes reported index of insert") {
                VALIDATE_CHANGES(changes) {
                    lv->move(5, 0);
                    lv->remove(0);
                }

                REQUIRE_INDICES(changes.deletions, 5);
                REQUIRE(changes.moves.empty());
            }

            SECTION("moves shift insertions/changes like any other insertion") {
                VALIDATE_CHANGES(changes) {
                    lv->insert(5, 5);
                    lv->set(6, 6);
                    lv->move(7, 4);
                }
                REQUIRE_INDICES(changes.deletions, 6);
                REQUIRE_INDICES(changes.insertions, 4, 6);
                REQUIRE_INDICES(changes.modifications, 7);
                REQUIRE_MOVES(changes, {6, 4});
            }

            SECTION("clear after delete") {
                VALIDATE_CHANGES(changes) {
                    lv->remove(5);
                    lv->clear();
                }
                REQUIRE_INDICES(changes.deletions, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            }

            SECTION("erase before previous move target") {
                VALIDATE_CHANGES(changes) {
                    lv->move(2, 8);
                    lv->remove(5);
                }
                REQUIRE_INDICES(changes.insertions, 7);
                REQUIRE_INDICES(changes.deletions, 2, 6);
                REQUIRE_MOVES(changes, {2, 7});
            }

            SECTION("insert after move updates move destination") {
                VALIDATE_CHANGES(changes) {
                    lv->move(2, 8);
                    lv->insert(5, 5);
                }
                REQUIRE_MOVES(changes, {2, 9});
            }
        }

        SECTION("deleting the linkview") {
            SECTION("directly") {
                VALIDATE_CHANGES(changes) {
                    origin->move_last_over(0);
                }
                REQUIRE(!lv->is_attached());
                REQUIRE(changes.insertions.empty());
                REQUIRE(changes.deletions.empty());
                REQUIRE(changes.modifications.empty());
            }

            SECTION("table clear") {
                VALIDATE_CHANGES(changes) {
                    origin->clear();
                }
                REQUIRE(!lv->is_attached());
                REQUIRE(changes.insertions.empty());
                REQUIRE(changes.deletions.empty());
                REQUIRE(changes.modifications.empty());
            }

            SECTION("delete a different lv") {
                r->begin_transaction();
                origin->add_empty_row();
                r->commit_transaction();

                VALIDATE_CHANGES(changes) {
                    origin->move_last_over(1);
                }
                REQUIRE(changes.insertions.empty());
                REQUIRE(changes.deletions.empty());
                REQUIRE(changes.modifications.empty());
            }
        }

        SECTION("modifying a different linkview should not produce notifications") {
            r->begin_transaction();
            origin->add_empty_row();
            LinkViewRef lv2 = origin->get_linklist(0, 1);
            lv2->add(5);
            r->commit_transaction();

            VALIDATE_CHANGES(changes) {
                lv2->add(1);
                lv2->add(2);
                lv2->remove(0);
                lv2->set(0, 6);
                lv2->move(1, 0);
                lv2->swap(0, 1);
                lv2->clear();
                lv2->add(1);
            }

            REQUIRE(changes.insertions.empty());
            REQUIRE(changes.deletions.empty());
            REQUIRE(changes.modifications.empty());
        }
    }
}

TEST_CASE("DeepChangeChecker") {
    InMemoryTestFile config;
    config.automatic_change_notifications = false;

    config.schema = std::make_unique<Schema>(Schema{
        {"table", "", {
            {"int", PropertyType::Int},
            {"link", PropertyType::Object, "table", "", false, false, true},
            {"array", PropertyType::Array, "table"}
        }},
    });

    auto r = Realm::get_shared_realm(config);
    auto table = r->read_group()->get_table("class_table");

    r->begin_transaction();
    table->add_empty_row(10);
    for (int i = 0; i < 10; ++i)
        table->set_int(0, i, i);
    r->commit_transaction();

    auto track_changes = [&](auto&& f) {
        auto history = make_client_history(config.path);
        SharedGroup sg(*history, SharedGroup::durability_MemOnly);
        Group const& g = sg.begin_read();

        r->begin_transaction();
        f();
        r->commit_transaction();

        _impl::TransactionChangeInfo info;
        info.table_modifications_needed.resize(g.size(), true);
        info.table_moves_needed.resize(g.size(), true);
        _impl::transaction::advance(sg, info);
        return info;
    };

    std::vector<_impl::DeepChangeChecker::RelatedTable> tables;
    _impl::DeepChangeChecker::find_related_tables(tables, *table);

    SECTION("direct changes are tracked") {
        auto info = track_changes([&] {
            table->set_int(0, 9, 10);
        });

        _impl::DeepChangeChecker checker(info, *table, tables);
        REQUIRE_FALSE(checker(8));
        REQUIRE(checker(9));
    }

    SECTION("changes over links are tracked") {
        r->begin_transaction();
        for (int i = 0; i < 9; ++i)
            table->set_link(1, i, i + 1);
        r->commit_transaction();

        auto info = track_changes([&] {
            table->set_int(0, 9, 10);
        });

        REQUIRE(_impl::DeepChangeChecker(info, *table, tables)(0));
    }

    SECTION("changes over linklists are tracked") {
        r->begin_transaction();
        for (int i = 0; i < 9; ++i)
            table->get_linklist(2, i)->add(i + 1);
        r->commit_transaction();

        auto info = track_changes([&] {
            table->set_int(0, 9, 10);
        });

        REQUIRE(_impl::DeepChangeChecker(info, *table, tables)(0));
    }

    SECTION("cycles over links do not loop forever") {
        r->begin_transaction();
        table->set_link(1, 0, 0);
        r->commit_transaction();

        auto info = track_changes([&] {
            table->set_int(0, 9, 10);
        });
        REQUIRE_FALSE(_impl::DeepChangeChecker(info, *table, tables)(0));
    }

    SECTION("cycles over linklists do not loop forever") {
        r->begin_transaction();
        table->get_linklist(2, 0)->add(0);
        r->commit_transaction();

        auto info = track_changes([&] {
            table->set_int(0, 9, 10);
        });
        REQUIRE_FALSE(_impl::DeepChangeChecker(info, *table, tables)(0));
    }

    SECTION("link chains are tracked up to 16 levels deep") {
        r->begin_transaction();
        table->add_empty_row(10);
        for (int i = 0; i < 19; ++i)
            table->set_link(1, i, i + 1);
        r->commit_transaction();

        auto info = track_changes([&] {
            table->set_int(0, 19, -1);
        });

        _impl::DeepChangeChecker checker(info, *table, tables);
        CHECK(checker(19));
        CHECK(checker(18));
        CHECK(checker(4));
        CHECK_FALSE(checker(3));
        CHECK_FALSE(checker(2));

        // Check in other orders to make sure that the caching doesn't effect
        // the results
        _impl::DeepChangeChecker checker2(info, *table, tables);
        CHECK_FALSE(checker2(2));
        CHECK_FALSE(checker2(3));
        CHECK(checker2(4));
        CHECK(checker2(18));
        CHECK(checker2(19));

        _impl::DeepChangeChecker checker3(info, *table, tables);
        CHECK(checker2(4));
        CHECK_FALSE(checker2(3));
        CHECK_FALSE(checker2(2));
        CHECK(checker2(18));
        CHECK(checker2(19));
    }

    SECTION("targets moving is not a change") {
        r->begin_transaction();
        table->set_link(1, 0, 9);
        table->get_linklist(2, 0)->add(9);
        r->commit_transaction();

        auto info = track_changes([&] {
            table->move_last_over(5);
        });
        REQUIRE_FALSE(_impl::DeepChangeChecker(info, *table, tables)(0));
    }

    SECTION("changes made before a row is moved are reported") {
        r->begin_transaction();
        table->set_link(1, 0, 9);
        r->commit_transaction();

        auto info = track_changes([&] {
            table->set_int(0, 9, 5);
            table->move_last_over(5);
        });
        REQUIRE(_impl::DeepChangeChecker(info, *table, tables)(0));

        r->begin_transaction();
        table->get_linklist(2, 0)->add(8);
        r->commit_transaction();

        info = track_changes([&] {
            table->set_int(0, 8, 5);
            table->move_last_over(5);
        });
        REQUIRE(_impl::DeepChangeChecker(info, *table, tables)(0));
    }

    SECTION("changes made after a row is moved are reported") {
        r->begin_transaction();
        table->set_link(1, 0, 9);
        r->commit_transaction();

        auto info = track_changes([&] {
            table->move_last_over(5);
            table->set_int(0, 5, 5);
        });
        REQUIRE(_impl::DeepChangeChecker(info, *table, tables)(0));

        r->begin_transaction();
        table->get_linklist(2, 0)->add(8);
        r->commit_transaction();

        info = track_changes([&] {
            table->move_last_over(5);
            table->set_int(0, 5, 5);
        });
        REQUIRE(_impl::DeepChangeChecker(info, *table, tables)(0));
    }
}
