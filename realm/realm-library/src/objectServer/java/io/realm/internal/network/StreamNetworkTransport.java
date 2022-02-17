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

package io.realm.internal.network;

import java.io.IOException;

import io.realm.internal.objectstore.OsApp;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.internal.objectstore.OsSyncUser;

/**
 * StreamNetworkTransport provides an interface to the Realm remote streaming functions.
 * Such functions are package protected and this class offers a portable way to access them.
 */
public class StreamNetworkTransport {
    private final OsApp app;
    private final OsSyncUser user;

    public StreamNetworkTransport(OsApp app, OsSyncUser user) {
        this.app = app;
        this.user = user;
    }

    /**
     * Creates a request for a streaming function
     *
     * @param functionName name of the function
     * @param arguments     function arguments encoded as a {@link String}
     * @param serviceName  service that will handle the function
     * @return {@link io.realm.internal.objectstore.OsJavaNetworkTransport.Request}
     */
    public OsJavaNetworkTransport.Request makeStreamingRequest(String functionName,
                                                                        String arguments,
                                                                        String serviceName){
        return app.makeStreamingRequest(user, functionName, arguments, serviceName);
    }

    /**
     * Executes a given request
     *
     * @param request to execute
     * @return {@link io.realm.internal.objectstore.OsJavaNetworkTransport.Response}
     * @throws IOException
     */
    public OsJavaNetworkTransport.Response
    sendRequest(OsJavaNetworkTransport.Request request) throws IOException{
        return app.getNetworkTransport().sendStreamingRequest(request);
    }
}
