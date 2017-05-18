/*
 * Copyright 2016 Realm Inc.
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

package io.realm.objectserver.utils;

public class Constants {

    public static final String USER_REALM = "realm://127.0.0.1:9080/~/tests";
    public static final String USER_REALM_SECURE = "realms://127.0.0.1:9443/~/tests";
    public static final String SYNC_SERVER_URL = "realm://127.0.0.1/tests";
    public static final String SYNC_SERVER_URL_2 = "realm://127.0.0.1/tests2";

    public static final String AUTH_SERVER_URL = "http://127.0.0.1:9080/";
    public static final String AUTH_URL = AUTH_SERVER_URL + "auth";

    public static final long TEST_TIMEOUT_SECS = 300;
}
