#include "index_set.hpp"

#include <string>

// Catch doesn't have an overload for std::pair, so define one ourselves
// The declaration needs to be before catch.hpp is included for it to be used,
// but the definition needs to be after since it uses Catch's toString()
namespace Catch {
template<typename T, typename U>
std::string toString(std::pair<T, U> const& value);
}

#include "catch.hpp"

namespace Catch {
template<typename T, typename U>
    std::string toString(std::pair<T, U> const& value) {
    return "{" + toString(value.first) + ", " + toString(value.second) + "}";
}
}

#define REQUIRE_RANGES(index_set, ...) do { \
    std::initializer_list<std::pair<size_t, size_t>> expected = {__VA_ARGS__}; \
    REQUIRE(index_set.size() == expected.size()); \
    auto begin = index_set.begin(), end = index_set.end(); \
    for (auto range : expected) { \
        REQUIRE(*begin++ == range); \
    } \
} while (0)

TEST_CASE("index set") {
    realm::IndexSet set;

    SECTION("add() extends existing ranges") {
        set.add(1);
        REQUIRE_RANGES(set, {1, 2});

        set.add(2);
        REQUIRE_RANGES(set, {1, 3});

        set.add(0);
        REQUIRE_RANGES(set, {0, 3});
    }

    SECTION("add() with gaps") {
        set.add(0);
        REQUIRE_RANGES(set, {0, 1});

        set.add(2);
        REQUIRE_RANGES(set, {0, 1}, {2, 3});
    }

    SECTION("add() is idempotent") {
        set.add(0);
        set.add(0);
        REQUIRE_RANGES(set, {0, 1});
    }

    SECTION("add() merges existing ranges") {
        set.add(0);
        set.add(2);
        set.add(4);

        set.add(1);
        REQUIRE_RANGES(set, {0, 3}, {4, 5});
    }

    SECTION("set() from empty") {
        set.set(5);
        REQUIRE_RANGES(set, {0, 5});
    }

    SECTION("set() discards existing data") {
        set.add(8);
        set.add(9);

        set.set(5);
        REQUIRE_RANGES(set, {0, 5});
    }

    SECTION("insert_at() extends ranges containing the target index") {
        set.add(5);
        set.add(6);

        set.insert_at(5);
        REQUIRE_RANGES(set, {5, 8});

        set.insert_at(4);
        REQUIRE_RANGES(set, {4, 5}, {6, 9});

        set.insert_at(9);
        REQUIRE_RANGES(set, {4, 5}, {6, 10});
    }

    SECTION("insert_at() does not modify ranges entirely before it") {
        set.add(5);
        set.add(6);

        set.insert_at(8);
        REQUIRE_RANGES(set, {5, 7}, {8, 9});
    }

    SECTION("insert_at() shifts ranges after it") {
        set.add(5);
        set.add(6);

        set.insert_at(3);
        REQUIRE_RANGES(set, {3, 4}, {6, 8});
    }

    SECTION("insert_at() cannot join ranges") {
        set.add(5);
        set.add(7);

        set.insert_at(6);
        REQUIRE_RANGES(set, {5, 7}, {8, 9});
    }

    SECTION("add_shifted() on an empty set is just add()") {
        set.add_shifted(5);
        REQUIRE_RANGES(set, {5, 6});
    }

    SECTION("add_shifted() before the first range is just add()") {
        set.add(10);
        set.add_shifted(5);
        REQUIRE_RANGES(set, {5, 6}, {10, 11});
    }

    SECTION("add_shifted() on first index of range extends range") {
        set.add(5);
        set.add_shifted(5);
        REQUIRE_RANGES(set, {5, 7});

        set.add_shifted(5);
        REQUIRE_RANGES(set, {5, 8});

        set.add_shifted(6);
        REQUIRE_RANGES(set, {5, 8}, {9, 10});
    }

    SECTION("add_shifted() after ranges shifts by the size of those ranges") {
        set.add(5);
        set.add_shifted(6);
        REQUIRE_RANGES(set, {5, 6}, {7, 8});

        set.add_shifted(6); // bumped into second range
        REQUIRE_RANGES(set, {5, 6}, {7, 9});

        set.add_shifted(8);
        REQUIRE_RANGES(set, {5, 6}, {7, 9}, {11, 12});
    }
}
