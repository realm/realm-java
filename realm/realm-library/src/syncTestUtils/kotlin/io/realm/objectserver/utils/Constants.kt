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
package io.realm.objectserver.utils

object Constants {
    var HOST = "127.0.0.1"
    val USER_REALM = "realm://$HOST:9080/~/tests"
    val USER_REALM_2 = "realm://$HOST:9080/~/tests2"
    val GLOBAL_REALM = "realm://$HOST:9080/tests"
    val USER_REALM_SECURE = "realms://$HOST:9443/~/tests"
    val SYNC_SERVER_URL = "realm://$HOST:9080/~/tests"
    val SYNC_SERVER_URL_2 = "realm://$HOST:9080/~/tests2"
    val DEFAULT_REALM = "realm://$HOST:9080/default"
    val AUTH_SERVER_URL = "http://$HOST:9080/"
    val AUTH_URL = AUTH_SERVER_URL + "auth"
    const val APP_ID = "mongdodb-realm-integrationtest-app" // FIXME: This doesn't work because the name changes
}
