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
    // to generate a valid token follow the guide in
    //<root>/integration-tests/sync/test_server/keys/HowToGenerateKey.txt
    public static String USER_TOKEN = "ewogICJpZGVudGl0eSI6ICJ0ZXN0MiIsCiAgImFjY2VzcyI6IFsKICAgICJkb3dubG9hZCIsCiAgICAidXBsb2FkIgogIF0sCiAgInRpbWVzdGFtcCI6IDE0NTU1MzA2MTQsCiAgImV4cGlyZXMiOiBudWxsLAogICJhcHBfaWQiOiAiaW8ucmVhbG0udGVzdHMuc3luYyIKfQ=="
            + ":" +
            "mR0/GMc0b5XHFNJEM4D9fb94oXMjho0jKxopaU1lQW4FqY1QPBa/bPiVCMhAosZVSNhEP6vEZxVjFHAxoPODKoml1Ry78geKt5Iql395HRvO6KCCN0VkMpx2eXy+SzF2pcEjU5jlldbTAcO6nMyVaQ9g2XF2SZPVjBqpkY1cy2IjMHN0HRWy9SfGelwZY/jW72jZM7+89kWpIB0SmNH8kEPKVZlnRMW4KwNAUPA8P0/+qyoRTr/4l7k7N6z5kBxIKB/+m55AeOUDiFsxA53QPlpHGvF7ThZpiv8i+UhyKZcQlXi1utoj8H1CzpeU/YzrrEf3xrr2qCO3/niU5WdnHA==";
    public static String SYNC_SERVER_URL = "realm://127.0.0.1:7800/tests";
    public static String SYNC_SERVER_URL_2 = "realm://127.0.0.1:7800/tests2";

    public static String AUTH_SERVER_URL = "http://127.0.0.1:8080/";
    public static String AUTH_URL = AUTH_SERVER_URL + "auth";
}
