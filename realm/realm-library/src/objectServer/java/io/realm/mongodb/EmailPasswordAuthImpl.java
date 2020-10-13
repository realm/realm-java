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

import io.realm.annotations.Beta;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.mongodb.auth.EmailPasswordAuth;

@Beta
class EmailPasswordAuthImpl extends EmailPasswordAuth {

    EmailPasswordAuthImpl(App app) {
        super(app);
    }

    @Override
    protected void call(int functionType, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback, String... args) {
        nativeCallFunction(functionType, app.osApp.getNativePtr(), callback, args);
    }

    private static native void nativeCallFunction(int functionType,
                                                  long appNativePtr,
                                                  OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback,
                                                  String... args);

}
