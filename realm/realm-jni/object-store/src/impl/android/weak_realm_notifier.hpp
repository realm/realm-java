////////////////////////////////////////////////////////////////////////////
//
// Copyright 2016 Realm Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////

#include "impl/weak_realm_notifier_base.hpp"

#include <atomic>

namespace realm {
class Realm;

namespace _impl {

class WeakRealmNotifier : public WeakRealmNotifierBase {
public:
    WeakRealmNotifier(const std::shared_ptr<Realm>& realm, bool cache);
    ~WeakRealmNotifier() { close(); }

    WeakRealmNotifier(WeakRealmNotifier&&);
    WeakRealmNotifier& operator=(WeakRealmNotifier&&);

    WeakRealmNotifier(const WeakRealmNotifier&) = delete;
    WeakRealmNotifier& operator=(const WeakRealmNotifier&) = delete;

    // Asyncronously call notify() on the Realm on the appropriate thread
    void notify();

private:
    void close();
    
    static int looper_callback(int fd, int events, void* data); 

    std::atomic<bool> m_thread_has_looper;

    // pipe file descriptor pair we use to signal ALooper
    struct {
      int read = -1;
      int write = -1;
    } m_message_pipe;
};

} // namespace _impl
} // namespace realm

