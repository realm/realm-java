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

package io.realm.objectserver;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.rule.TestRealmConfigurationFactory;

@RunWith(AndroidJUnit4.class)
public class SyncConfigurationtests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void user() {
//        new SyncConfiguration.Builder(context);
        // Check that user can be added
        // That the default local path is correct
    }

    @Test
    public void user_invalidUserThrows() {
        // Null user
        // Not authenticated user
    }

    @Test
    public void serverUrl() {

    }

    @Test
    public void serverUrl_invalidUrlThrows() {
        // null url
        // non-valid URI
        // Ending with .realm
    }

    @Test
    public void userAndServerUrlRequired() {
        // user/url is required
    }

    @Test
    public void autoConnect_true() {

    }

    @Test
    public void autoConnect_false() {

    }

    @Test
    public void errorHandler() {

    }

    @Test
    public void errorHandler_null() {

    }

    @Test
    public void syncPolicy() {

    }

    @Test
    public void syncPolicy_nullThrows() {

    }
}
