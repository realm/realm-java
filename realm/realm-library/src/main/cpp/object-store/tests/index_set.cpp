#include "catch.hpp"

#include "index_set.hpp"

#include "util/index_helpers.hpp"

TEST_CASE("[index_set] contains()") {
    SECTION("returns false if the index is before the first entry in the set") {
        realm::IndexSet set = {1, 2, 5};
        REQUIRE_FALSE(set.contains(0));
    }

    SECTION("returns false if the index is after the last entry in the set") {
        realm::IndexSet set = {1, 2, 5};
        REQUIRE_FALSE(set.contains(6));
    }

    SECTION("returns false if the index is between ranges in the set") {
        realm::IndexSet set = {1, 2, 5};
        REQUIRE_FALSE(set.contains(4));
    }

    SECTION("returns true if the index is in the set") {
        realm::IndexSet set = {1, 2, 5};
        REQUIRE(set.contains(1));
        REQUIRE(set.contains(2));
        REQUIRE(set.contains(5));
    }
}

TEST_CASE("[index_set] count()") {
    SECTION("returns the number of indices in the set in the given range") {
        realm::IndexSet set = {1, 2, 3, 5};
        REQUIRE(set.count(0, 6) == 4);
        REQUIRE(set.count(0, 5) == 3);
        REQUIRE(set.count(0, 4) == 3);
        REQUIRE(set.count(0, 3) == 2);
        REQUIRE(set.count(0, 2) == 1);
        REQUIRE(set.count(0, 1) == 0);
        REQUIRE(set.count(0, 0) == 0);

        REQUIRE(set.count(0, 6) == 4);
        REQUIRE(set.count(1, 6) == 4);
        REQUIRE(set.count(2, 6) == 3);
        REQUIRE(set.count(3, 6) == 2);
        REQUIRE(set.count(4, 6) == 1);
        REQUIRE(set.count(5, 6) == 1);
        REQUIRE(set.count(6, 6) == 0);
    }

    SECTION("includes full ranges in the middle") {
        realm::IndexSet set = {1, 3, 4, 5, 10};
        REQUIRE(set.count(0, 11) == 5);
    }

    SECTION("truncates ranges at the beginning and end") {
        realm::IndexSet set = {1, 2, 3, 5, 6, 7, 8, 9};
        REQUIRE(set.count(3, 9) == 5);
    }

    SECTION("handles full chunks well") {
        size_t count = realm::_impl::ChunkedRangeVector::max_size * 4;
        realm::IndexSet set;
        for (size_t i = 0; i < count; ++i) {
            set.add(i * 3);
            set.add(i * 3 + 1);
        }

        for (size_t i = 0; i < count * 3; ++i) {
            REQUIRE(set.count(i) == 2 * count - (i + 1) * 2 / 3);
            REQUIRE(set.count(0, i) == (i + 1) / 3 + (i + 2) / 3);
        }
    }
}

TEST_CASE("[index_set] add()") {
    realm::IndexSet set;

    SECTION("extends existing ranges when next to an edge") {
        set.add(1);
        REQUIRE_INDICES(set, 1);

        set.add(2);
        REQUIRE_INDICES(set, 1, 2);

        set.add(0);
        REQUIRE_INDICES(set, 0, 1, 2);
    }

    SECTION("does not extend ranges over gaps") {
        set.add(0);
        REQUIRE_INDICES(set, 0);

        set.add(2);
        REQUIRE_INDICES(set, 0, 2);
    }

    SECTION("does nothing when the index is already in the set") {
        set.add(0);
        set.add(0);
        REQUIRE_INDICES(set, 0);
    }

    SECTION("merges existing ranges when adding the index between them") {
        set = {0, 2, 4};

        set.add(1);
        REQUIRE_INDICES(set, 0, 1, 2, 4);
    }

    SECTION("combines multiple index sets without any shifting") {
        set = {0, 2, 6};

        set.add({1, 4, 5});
        REQUIRE_INDICES(set, 0, 1, 2, 4, 5, 6);
    }

    SECTION("handles front additions of ranges") {
        for (size_t i = 20; i > 0; i -= 2)
            set.add(i);
        REQUIRE_INDICES(set, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
    }

    SECTION("merges ranges even when they are in different chunks") {
        realm::IndexSet set2;
        for (int i = 0; i < 20; ++i) {
            set.add(i * 2);
            set2.add(i);
            set2.add(i * 2);
        }
        set.add(set2);
        REQUIRE(set.count() == 30);
    }
}

TEST_CASE("[index_set] add_shifted()") {
    realm::IndexSet set;

    SECTION("on an empty set is just add()") {
        set.add_shifted(5);
        REQUIRE_INDICES(set, 5);
    }

    SECTION("before the first range is just add()") {
        set = {10};
        set.add_shifted(5);
        REQUIRE_INDICES(set, 5, 10);
    }

    SECTION("on first index of a range extends the range") {
        set = {5};

        set.add_shifted(5);
        REQUIRE_INDICES(set, 5, 6);

        set.add_shifted(5);
        REQUIRE_INDICES(set, 5, 6, 7);
    }

    SECTION("in the middle of a range is shifted by that range") {
        set = {5, 6, 7};
        set.add_shifted(6);
        REQUIRE_INDICES(set, 5, 6, 7, 9);
    }

    SECTION("after the last range adds the total count to the index to be added") {
        set = {5};

        set.add_shifted(6);
        REQUIRE_INDICES(set, 5, 7);

        set.add_shifted(10);
        REQUIRE_INDICES(set, 5, 7, 12);
    }

    SECTION("in between ranges can be bumped into the next range") {
        set = {5, 7};
        set.add_shifted(6);
        REQUIRE_INDICES(set, 5, 7, 8);
    }
}

TEST_CASE("[index_set] add_shifted_by()") {
    realm::IndexSet set;

    SECTION("does nothing given an empty set to add") {
        set = {5, 6, 7};
        set.add_shifted_by({5, 6}, {});
        REQUIRE_INDICES(set, 5, 6, 7);
    }

    SECTION("does nothing if values is a subset of shifted_by") {
        set = {5, 6, 7};
        set.add_shifted_by({3, 4}, {3, 4});
        REQUIRE_INDICES(set, 5, 6, 7);
    }

    SECTION("just adds the indices when they are all before the old indices and the shifted-by set is empty") {
        set = {5, 6};
        set.add_shifted_by({}, {3, 4});
        REQUIRE_INDICES(set, 3, 4, 5, 6);
    }

    SECTION("adds the indices shifted by the old count when they are all after the old indices and the shifted-by set is empty") {
        set = {5, 6};
        set.add_shifted_by({}, {7, 9, 11, 13});
        REQUIRE_INDICES(set, 5, 6, 9, 11, 13, 15);
    }

    SECTION("acts like bulk add_shifted() when shifted_by is empty") {
        set = {5, 10, 15, 20, 25};
        set.add_shifted_by({}, {4, 5, 11});
        REQUIRE_INDICES(set, 4, 5, 6, 10, 13, 15, 20, 25);
    }

    SECTION("shifts indices in values back by the number of indices in shifted_by before them") {
        set = {5};
        set.add_shifted_by({0, 2, 3}, {6});
        REQUIRE_INDICES(set, 3, 5);

        set = {5};
        set.add_shifted_by({1, 3}, {4});
        REQUIRE_INDICES(set, 2, 5);
    }

    SECTION("discards indices in both shifted_by and values") {
        set = {5};
        set.add_shifted_by({2}, {2, 4});
        REQUIRE_INDICES(set, 3, 5);
    }
}

TEST_CASE("[index_set] set()") {
    realm::IndexSet set;

    SECTION("clears the existing indices and replaces with the range [0, value)") {
        set = {8, 9};
        set.set(5);
        REQUIRE_INDICES(set, 0, 1, 2, 3, 4);
    }
}

TEST_CASE("[index_set] insert_at()") {
    realm::IndexSet set;

    SECTION("on an empty set is add()") {
        set.insert_at(5);
        REQUIRE_INDICES(set, 5);

        set = {};
        set.insert_at({1, 3, 5});
        REQUIRE_INDICES(set, 1, 3, 5);
    }

    SECTION("with an empty set is a no-op") {
        set = {5, 6};
        set.insert_at(realm::IndexSet{});
        REQUIRE_INDICES(set, 5, 6);
    }

    SECTION("extends ranges containing the target range") {
        set = {5, 6};

        set.insert_at(5);
        REQUIRE_INDICES(set, 5, 6, 7);

        set.insert_at(6, 2);
        REQUIRE_INDICES(set, 5, 6, 7, 8, 9);

        set.insert_at({5, 7, 11});
        REQUIRE_INDICES(set, 5, 6, 7, 8, 9, 10, 11, 12);
    }

    SECTION("shifts ranges after the insertion point") {
        set = {5, 6};

        set.insert_at(3);
        REQUIRE_INDICES(set, 3, 6, 7);

        set.insert_at(0, 2);
        REQUIRE_INDICES(set, 0, 1, 5, 8, 9);
    }

    SECTION("does not shift ranges before the insertion point") {
        set = {5, 6};

        set.insert_at(10);
        REQUIRE_INDICES(set, 5, 6, 10);

        set.insert_at({15, 16});
        REQUIRE_INDICES(set, 5, 6, 10, 15, 16);
    }

    SECTION("can not join ranges") {
        set = {5, 7};
        set.insert_at(6);
        REQUIRE_INDICES(set, 5, 6, 8);
    }

    SECTION("adds later ranges after shifting for previous insertions") {
        set = {5, 10};
        set.insert_at({5, 10});
        REQUIRE_INDICES(set, 5, 6, 10, 12);
    }
}

TEST_CASE("[index_set] shift_for_insert_at()") {
    realm::IndexSet set;

    SECTION("does nothing given an empty set of insertion points") {
        set = {5, 8};
        set.shift_for_insert_at(realm::IndexSet{});
        REQUIRE_INDICES(set, 5, 8);
    }

    SECTION("does nothing when called on an empty set") {
        set = {};
        set.shift_for_insert_at({5, 8});
        REQUIRE(set.empty());
    }

    SECTION("does nothing when the insertion points are all after the current indices") {
        set = {10, 20};
        set.shift_for_insert_at({30, 40});
        REQUIRE_INDICES(set, 10, 20);
    }

    SECTION("does shift when the insertion points are all before the current indices") {
        set = {10, 20};
        set.shift_for_insert_at({2, 4});
        REQUIRE_INDICES(set, 12, 22);
    }

    SECTION("shifts indices at or after the insertion points") {
        set = {5};

        set.shift_for_insert_at(4);
        REQUIRE_INDICES(set, 6);

        set.shift_for_insert_at(6);
        REQUIRE_INDICES(set, 7);

        set.shift_for_insert_at({3, 8});
        REQUIRE_INDICES(set, 9);
    }

    SECTION("shifts indices by the count specified") {
        set = {5};
        set.shift_for_insert_at(3, 10);
        REQUIRE_INDICES(set, 15);
    }

    SECTION("does not shift indices before the insertion points") {
        set = {5};

        set.shift_for_insert_at(6);
        REQUIRE_INDICES(set, 5);

        set.shift_for_insert_at({3, 8});
        REQUIRE_INDICES(set, 6);
    }

    SECTION("splits ranges containing the insertion points") {
        set = {5, 6, 7, 8};

        set.shift_for_insert_at(6);
        REQUIRE_INDICES(set, 5, 7, 8, 9);

        set.shift_for_insert_at({8, 10, 12});
        REQUIRE_INDICES(set, 5, 7, 9, 11);
    }
}

TEST_CASE("[index_set] erase_at()") {
    realm::IndexSet set;

    SECTION("is a no-op on an empty set") {
        set.erase_at(10);
        REQUIRE(set.empty());

        set.erase_at({1, 5, 8});
        REQUIRE(set.empty());
    }

    SECTION("does nothing when given an empty set") {
        set = {5};
        set.erase_at(realm::IndexSet{});
        REQUIRE_INDICES(set, 5);
    }

    SECTION("removes the specified indices") {
        set = {5};
        set.erase_at(5);
        REQUIRE(set.empty());

        set = {4, 7};
        set.erase_at({4, 7});
        REQUIRE(set.empty());
    }

    SECTION("does not modify indices before the removed one") {
        set = {5, 8};
        set.erase_at(8);
        REQUIRE_INDICES(set, 5);

        set = {5, 8, 9};
        set.erase_at({8, 9});
        REQUIRE_INDICES(set, 5);
    }

    SECTION("shifts indices after the removed one") {
        set = {5, 8};
        set.erase_at(5);
        REQUIRE_INDICES(set, 7);

        set = {5, 10, 15, 20};
        set.erase_at({5, 10});
        REQUIRE_INDICES(set, 13, 18);
    }

    SECTION("shrinks ranges when used on one of the edges of them") {
        set = {5, 6, 7, 8};
        set.erase_at(8);
        REQUIRE_INDICES(set, 5, 6, 7);
        set.erase_at(5);
        REQUIRE_INDICES(set, 5, 6);

        set = {5, 6, 7, 8};
        set.erase_at({5, 8});
        REQUIRE_INDICES(set, 5, 6);
    }

    SECTION("shrinks ranges when used in the middle of them") {
        set = {5, 6, 7, 8};
        set.erase_at(7);
        REQUIRE_INDICES(set, 5, 6, 7);

        set = {5, 6, 7, 8};
        set.erase_at({6, 7});
        REQUIRE_INDICES(set, 5, 6);
    }

    SECTION("merges ranges when the gap between them is deleted") {
        set = {3, 5};
        set.erase_at(4);
        REQUIRE_INDICES(set, 3, 4);

        set = {3, 5, 7};
        set.erase_at({4, 6});
        REQUIRE_INDICES(set, 3, 4, 5);
    }
}

TEST_CASE("[index_set] erase_or_unshift()") {
    realm::IndexSet set;

    SECTION("removes the given index") {
        set = {1, 2};
        set.erase_or_unshift(2);
        REQUIRE_INDICES(set, 1);
    }

    SECTION("shifts indexes after the given index") {
        set = {1, 5};
        set.erase_or_unshift(2);
        REQUIRE_INDICES(set, 1, 4);
    }

    SECTION("returns npos for indices in the set") {
        set = {1, 3, 5};
        REQUIRE(realm::IndexSet(set).erase_or_unshift(1) == realm::IndexSet::npos);
        REQUIRE(realm::IndexSet(set).erase_or_unshift(3) == realm::IndexSet::npos);
        REQUIRE(realm::IndexSet(set).erase_or_unshift(5) == realm::IndexSet::npos);
    }

    SECTION("returns the number of indices in the set before the index for ones not in the set") {
        set = {1, 3, 5, 6};
        REQUIRE(realm::IndexSet(set).erase_or_unshift(0) == 0);
        REQUIRE(realm::IndexSet(set).erase_or_unshift(2) == 1);
        REQUIRE(realm::IndexSet(set).erase_or_unshift(4) == 2);
        REQUIRE(realm::IndexSet(set).erase_or_unshift(7) == 3);
    }

}

TEST_CASE("[index_set] remove()") {
    realm::IndexSet set;

    SECTION("is a no-op if the set is empty") {
        set.remove(4);
        REQUIRE(set.empty());

        set.remove({1, 2, 3});
        REQUIRE(set.empty());
    }

    SECTION("is a no-op if the set to remove is empty") {
        set = {5};
        set.remove(realm::IndexSet{});
        REQUIRE_INDICES(set, 5);
    }

    SECTION("is a no-op if the index to remove is not in the set") {
        set = {5};
        set.remove(4);
        set.remove(6);
        set.remove({4, 6});
        REQUIRE_INDICES(set, 5);
    }

    SECTION("removes one-element ranges") {
        set = {5};
        set.remove(5);
        REQUIRE(set.empty());

        set = {5};
        set.remove({3, 4, 5});
        REQUIRE(set.empty());
    }

    SECTION("shrinks ranges beginning with the index") {
        set = {5, 6, 7};
        set.remove(5);
        REQUIRE_INDICES(set, 6, 7);

        set = {5, 6, 7};
        set.remove({3, 5});
        REQUIRE_INDICES(set, 6, 7);
    }

    SECTION("shrinks ranges ending with the index") {
        set = {5, 6, 7};
        set.remove(7);
        REQUIRE_INDICES(set, 5, 6);

        set = {5, 6, 7};
        set.remove({3, 7});
        REQUIRE_INDICES(set, 5, 6);
    }

    SECTION("splits ranges containing the index") {
        set = {5, 6, 7};
        set.remove(6);
        REQUIRE_INDICES(set, 5, 7);

        set = {5, 6, 7};
        set.remove({3, 6});
        REQUIRE_INDICES(set, 5, 7);
    }

    SECTION("does not shift other indices and uses unshifted positions") {
        set = {5, 6, 7, 10, 11, 12, 13, 15};
        set.remove({6, 11, 13});
        REQUIRE_INDICES(set, 5, 7, 10, 12, 15);
    }
}

TEST_CASE("[index_set] shift()") {
    realm::IndexSet set;

    SECTION("is ind + count(0, ind), but adds the count-so-far to the stop index") {
        set = {1, 3, 5, 6};
        REQUIRE(set.shift(0) == 0);
        REQUIRE(set.shift(1) == 2);
        REQUIRE(set.shift(2) == 4);
        REQUIRE(set.shift(3) == 7);
        REQUIRE(set.shift(4) == 8);
    }
}

TEST_CASE("[index_set] unshift()") {
    realm::IndexSet set;

    SECTION("is index - count(0, index)") {
        set = {1, 3, 5, 6};
        REQUIRE(set.unshift(0) == 0);
        REQUIRE(set.unshift(2) == 1);
        REQUIRE(set.unshift(4) == 2);
        REQUIRE(set.unshift(7) == 3);
        REQUIRE(set.unshift(8) == 4);
    }
}

TEST_CASE("[index_set] clear()") {
    realm::IndexSet set;

    SECTION("removes all indices from the set") {
        set = {1, 2, 3};
        set.clear();
        REQUIRE(set.empty());
    }
}
