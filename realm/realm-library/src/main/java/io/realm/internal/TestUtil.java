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

package io.realm.internal;

class TestUtil {

    static {
        // Any internal class with static native methods that uses Realm Core must load the Realm Core library
        // themselves as it otherwise might not have been loaded.
        RealmCore.loadLibrary();
    }

    public native static long getMaxExceptionNumber();
    public native static String getExpectedMessage(long exceptionKind);
    public native static void testThrowExceptions(long exceptionKind);
}
