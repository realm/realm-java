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

#include "impl/weak_realm_notifier.hpp"
#include "shared_realm.hpp"

#include <errno.h>
#include <fcntl.h> 
#include <unistd.h>
#include <android/log.h>
#include <android/looper.h>

#define LOGE(fmt...) do { \
    fprintf(stderr, fmt); \
    __android_log_print(ANDROID_LOG_ERROR, "REALM", fmt); \
} while (0)

namespace realm {
namespace _impl {

WeakRealmNotifier::WeakRealmNotifier(const std::shared_ptr<Realm>& realm, bool cache)
: WeakRealmNotifierBase(realm, cache)
, m_thread_has_looper(false)
{
    ALooper* looper = ALooper_forThread();
    if (!looper) {
        return;
    }

    int message_pipe[2];
    if (pipe2(message_pipe, O_CLOEXEC | O_NONBLOCK)) {
        LOGE("could not create WeakRealmNotifier ALooper message pipe: %s", strerror(errno));
        return;
    }

    if (ALooper_addFd(looper, message_pipe[0], 3 /* LOOPER_ID_USER */, ALOOPER_EVENT_INPUT | ALOOPER_EVENT_HANGUP, &looper_callback, nullptr) != 1) {
        LOGE("Error adding WeakRealmNotifier callback to looper.");
        ::close(message_pipe[0]);
        ::close(message_pipe[1]);
        
        return;
    }

    m_message_pipe.read = message_pipe[0];
    m_message_pipe.write = message_pipe[1];
    m_thread_has_looper = true;
}

WeakRealmNotifier::WeakRealmNotifier(WeakRealmNotifier&& rgt)
: WeakRealmNotifierBase(std::move(rgt))
, m_message_pipe(std::move(rgt.m_message_pipe))
{
    bool flag = true;
    m_thread_has_looper = rgt.m_thread_has_looper.compare_exchange_strong(flag, false);
}

WeakRealmNotifier& WeakRealmNotifier::operator=(WeakRealmNotifier&& rgt)
{
    close();

    WeakRealmNotifierBase::operator=(std::move(rgt));
    m_message_pipe = std::move(rgt.m_message_pipe);

    bool flag = true;
    m_thread_has_looper = rgt.m_thread_has_looper.compare_exchange_strong(flag, false);

    return *this;
}

void WeakRealmNotifier::close()
{
    bool flag = true;
    if (m_thread_has_looper.compare_exchange_strong(flag, false)) {
        // closing one end of the pipe here will trigger ALOOPER_EVENT_HANGUP in the callback
        // which will do the rest of the cleanup
        ::close(m_message_pipe.write);
    }
}

void WeakRealmNotifier::notify()
{
    if (m_thread_has_looper && !expired()) {
        
        // we need to pass the weak Realm pointer to the other thread.
        // to do so we allocate a weak pointer on the heap and send its address over a pipe.
        // the other end of the pipe is read by the realm thread. when it's done with the pointer, it deletes it.
        auto realm_ptr = new std::weak_ptr<Realm>(realm());
        if (write(m_message_pipe.write, &realm_ptr, sizeof(realm_ptr)) != sizeof(realm_ptr)) {
            delete realm_ptr;
            LOGE("Buffer overrun when writing to WeakRealmNotifier's ALooper message pipe.");
        }
    }
}

int WeakRealmNotifier::looper_callback(int fd, int events, void* data)
{
    if ((events & ALOOPER_EVENT_INPUT) != 0) {
        // this is a pointer to a heap-allocated weak Realm pointer created by the notifiying thread.
        // the actual address to the pointer is communicated over a pipe.
        // we have to delete it so as to not leak, using the same memory allocation facilities it was allocated with.
        std::weak_ptr<Realm>* realm_ptr = nullptr;
        while (read(fd, &realm_ptr, sizeof(realm_ptr)) == sizeof(realm_ptr)) {
            if (auto realm = realm_ptr->lock()) {
                if (!realm->is_closed()) {
                    realm->notify();
                }
            }

            delete realm_ptr;
        }
    }

    if ((events & ALOOPER_EVENT_HANGUP) != 0) {
        // this callback is always invoked on the looper thread so it's fine to get the looper like this
        ALooper_removeFd(ALooper_forThread(), fd);
        ::close(fd);
    }

    if ((events & ALOOPER_EVENT_ERROR) != 0) {
        LOGE("Unexpected error on WeakRealmNotifier's ALooper message pipe.");
    }

    // return 1 to continue receiving events
    return 1;
}

}
}
