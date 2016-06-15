#ifndef REALM_TEST_UTIL_TEST_FILE_HPP
#define REALM_TEST_UTIL_TEST_FILE_HPP

#include "shared_realm.hpp"

struct TestFile : realm::Realm::Config {
    TestFile();
    ~TestFile();
};

struct InMemoryTestFile : TestFile {
    InMemoryTestFile();
};

void advance_and_notify(realm::Realm& realm);

#endif
