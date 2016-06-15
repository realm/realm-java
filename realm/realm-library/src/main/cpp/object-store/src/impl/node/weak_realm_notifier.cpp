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

#include <nan.h>
#include <uv.h>

#include "impl/weak_realm_notifier.hpp"

#include "shared_realm.hpp"

using namespace realm;
using namespace realm::_impl;

WeakRealmNotifier::WeakRealmNotifier(const std::shared_ptr<Realm>& realm, bool cache)
: WeakRealmNotifierBase(realm, cache)
, m_handle(new uv_async_t)
{
    m_handle->data = new std::weak_ptr<Realm>(realm);

    // This assumes that only one thread matters: the main thread (default loop).
    uv_async_init(uv_default_loop(), m_handle, [](uv_async_t* handle) {
        auto realm_weak_ptr = static_cast<std::weak_ptr<Realm>*>(handle->data);
        auto realm = realm_weak_ptr->lock();

        if (realm) {
            // The v8::Local handles need a "scope" to be present or will crash.
            Nan::HandleScope scope;
            realm->notify();
        }
    });
}

WeakRealmNotifier::WeakRealmNotifier(WeakRealmNotifier&& rgt)
: WeakRealmNotifierBase(std::move(rgt))
, m_handle(rgt.m_handle)
{
    rgt.m_handle = nullptr;
}

WeakRealmNotifier& WeakRealmNotifier::operator=(WeakRealmNotifier&& rgt)
{
    WeakRealmNotifierBase::operator=(std::move(rgt));
    std::swap(m_handle, rgt.m_handle);

    return *this;
}

WeakRealmNotifier::~WeakRealmNotifier()
{
    if (m_handle) {
        uv_close((uv_handle_t*)m_handle, [](uv_handle_t* handle) {
            auto realm_weak_ptr = static_cast<std::weak_ptr<Realm>*>(handle->data);
            delete realm_weak_ptr;
            delete handle;
        });
    }
}

void WeakRealmNotifier::notify()
{
    if (m_handle) {
        uv_async_send(m_handle);
    }
}
