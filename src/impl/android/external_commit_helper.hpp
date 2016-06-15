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

#include <thread>

namespace realm {

namespace _impl {
class RealmCoordinator;

class ExternalCommitHelper {
public:
    ExternalCommitHelper(RealmCoordinator& parent);
    ~ExternalCommitHelper();

    void notify_others();

private:
    // A RAII holder for a file descriptor which automatically closes the wrapped
    // fd when it's deallocated
    class FdHolder {
    public:
        FdHolder() = default;
        ~FdHolder() { close(); }
        operator int() const { return m_fd; }

        FdHolder& operator=(int new_fd) {
            close();
            m_fd = new_fd;
            return *this;
        }

    private:
        int m_fd = -1;
        void close();

        FdHolder& operator=(FdHolder const&) = delete;
        FdHolder(FdHolder const&) = delete;
    };

    void listen();

    RealmCoordinator& m_parent;

    // The listener thread
    std::thread m_thread;

    // Read-write file descriptor for the named pipe which is waited on for
    // changes and written to when a commit is made
    FdHolder m_notify_fd;
    // File descriptor for epoll
    FdHolder m_epfd;
    // The two ends of an anonymous pipe used to notify the kqueue() thread that
    // it should be shut down.
    FdHolder m_shutdown_read_fd;
    FdHolder m_shutdown_write_fd;
};

} // namespace _impl
} // namespace realm

