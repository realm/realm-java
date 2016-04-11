#include "util/test_file.hpp"

#include <realm/disable_sync_to_disk.hpp>

#include <cstdlib>
#include <unistd.h>

TestFile::TestFile()
{
    static std::string tmpdir = [] {
        realm::disable_sync_to_disk();

        const char* dir = getenv("TMPDIR");
        if (dir && *dir)
            return dir;
        return "/tmp";
    }();
    path = tmpdir + "/realm.XXXXXX";
    mktemp(&path[0]);
    unlink(path.c_str());
}

TestFile::~TestFile()
{
    unlink(path.c_str());
}

InMemoryTestFile::InMemoryTestFile()
{
    in_memory = true;
}
