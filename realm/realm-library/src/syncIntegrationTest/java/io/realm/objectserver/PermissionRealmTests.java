/*
 * Copyright 2017 Realm Inc.
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

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.HttpUtils;
import io.realm.objectserver.utils.UserFactory;

import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class PermissionRealmTests extends RealmIntegrationTest {


    @Test
    public void create_acceptOffer() {
        SyncUser user = login();



    }

    private SyncUser login() {
        SyncCredentials credentials = SyncCredentials.accessToken(Constants.USER_TOKEN, "access-token-user");
        return SyncUser.login(credentials, Constants.AUTH_URL);
    }

}
