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

package io.realm.mongodb;

import org.bson.BsonArray;

import java.io.IOException;

import io.realm.internal.network.StreamNetworkTransport;
import io.realm.internal.objectstore.OsJavaNetworkTransport;

public class StreamNetworkTransportImpl extends StreamNetworkTransport {
    private final App app;
    private final User user;

    StreamNetworkTransportImpl(App app, User user) {
        this.app = app;
        this.user = user;
    }

    @Override
    public OsJavaNetworkTransport.Request makeStreamingRequest(String functionName, BsonArray bsonArgs, String serviceName) {
        return app.makeStreamingRequest(user, functionName, bsonArgs, serviceName);
    }

    @Override
    public OsJavaNetworkTransport.Response sendRequest(OsJavaNetworkTransport.Request request) throws IOException, AppException {
        return app.networkTransport.sendStreamingRequest(request);
    }
}
