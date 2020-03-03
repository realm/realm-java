/*
 * Copyright 2020 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef REALM_JAVA_NETWORK_TRANSPORT
#define REALM_JAVA_NETWORK_TRANSPORT

#include "sync/generic_network_transport.hpp"

namespace realm {

struct JavaNetworkTransport : public app::GenericNetworkTransport {
    void send_request_to_server(const app::Request request, std::function<void(const app::Response)> completionBlock)
    {
        (void) request;
        (void) completionBlock;
    }
};

} // realm namespace

#endif
