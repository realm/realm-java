#include "util/test_file.hpp"

#include "impl/realm_coordinator.hpp"

#include <realm/disable_sync_to_disk.hpp>

#include <cstdlib>
#include <unistd.h>

#if defined(__has_feature) && __has_feature(thread_sanitizer)
#include <condition_variable>
#include <functional>
#include <thread>
#endif

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

#if defined(__has_feature) && __has_feature(thread_sanitizer)
// A helper which synchronously runs on_change() on a fixed background thread
// so that ThreadSanitizer can potentially detect issues
// This deliberately uses an unsafe spinlock for synchronization to ensure that
// the code being tested has to supply all required safety
static class TsanNotifyWorker {
public:
    TsanNotifyWorker()
    {
        m_thread = std::thread([&] { work(); });
    }

    void work()
    {
        while (true) {
            auto value = m_signal.load(std::memory_order_relaxed);
            if (value == 0 || value == 1)
                continue;
            if (value == 2)
                return;

            auto c = reinterpret_cast<realm::_impl::RealmCoordinator *>(value);
            c->on_change();
            m_signal.store(1, std::memory_order_relaxed);
        }
    }

    ~TsanNotifyWorker()
    {
        m_signal = 2;
        m_thread.join();
    }

    void on_change(realm::_impl::RealmCoordinator* c)
    {
        m_signal.store(reinterpret_cast<uintptr_t>(c), std::memory_order_relaxed);
        while (m_signal.load(std::memory_order_relaxed) != 1) ;
    }

private:
    std::atomic<uintptr_t> m_signal{0};
    std::thread m_thread;
} s_worker;

void advance_and_notify(realm::Realm& realm)
{
    s_worker.on_change(realm::_impl::RealmCoordinator::get_existing_coordinator(realm.config().path).get());
    realm.notify();
}

#else // __has_feature(thread_sanitizer)

void advance_and_notify(realm::Realm& realm)
{
    realm::_impl::RealmCoordinator::get_existing_coordinator(realm.config().path)->on_change();
    realm.notify();
}
#endif
