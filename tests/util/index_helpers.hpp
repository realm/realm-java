#define REQUIRE_INDICES(index_set, ...) do { \
    index_set.verify(); \
    std::initializer_list<size_t> expected = {__VA_ARGS__}; \
    auto actual = index_set.as_indexes(); \
    INFO("Checking " #index_set); \
    REQUIRE(expected.size() == std::distance(actual.begin(), actual.end())); \
    auto begin = actual.begin(); \
    for (auto index : expected) { \
        REQUIRE(*begin++ == index); \
    } \
} while (0)

#define REQUIRE_MOVES(c, ...) do { \
    auto actual = (c); \
    std::initializer_list<CollectionChangeSet::Move> expected = {__VA_ARGS__}; \
    REQUIRE(expected.size() == actual.moves.size()); \
    auto begin = actual.moves.begin(); \
    for (auto move : expected) { \
        CHECK(begin->from == move.from); \
        CHECK(begin->to == move.to); \
        ++begin; \
    } \
} while (0)
