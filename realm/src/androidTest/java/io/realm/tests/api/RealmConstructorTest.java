/*
 * Copyright 2014 Realm Inc.
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

package io.realm.tests.api;

import java.io.IOException;

import io.realm.Realm;


public class RealmConstructorTest extends RealmSetupTests {

    // Realm Constructors
    public void testShouldCreateRealm() {
        setupSharedGroup();

        try {
            Realm realm = new Realm(getContext().getFilesDir());
        } catch (Exception ex) {
            fail("Unexpected Exception: "+ex.getMessage());
        }
    }

    public void testShouldFailCreateRealmWithNullDir() {
        setupSharedGroup();

        try {
        Realm realm = new Realm(null);
        fail("Expected IOException");
        } catch (IOException ioe) {
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    public void testShouldFailWithNullFileName() {
        setupSharedGroup();

        try {
            Realm realm = new Realm(getContext().getFilesDir(), null);
            fail("Expected IOException");
        } catch (Exception ex) {
            if (!(ex instanceof IOException)) {
                ex.printStackTrace();
                fail("Unexpected exception: " + ex.getMessage());
            }
        }
    }
}