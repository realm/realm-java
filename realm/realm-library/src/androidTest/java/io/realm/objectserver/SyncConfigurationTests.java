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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmMigration;
import io.realm.objectserver.android.SharedPrefsUserStore;
import io.realm.objectserver.internal.Token;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SyncConfigurationTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getContext();
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
        SyncConfiguration.Builder builder = new SyncConfiguration.Builder(context);

        try {
            builder.user(null);
        } catch (IllegalArgumentException ignore) {
        }

        User user = createTestUser(0); // Create user that has expired credentials
        try {
            builder.user(user);
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void serverUrl_setsFolderAndFileName() {
        User user = User.createLocal();
        String[][] validUrls = {
                // <URL>, <Folder>, <FileName>
                { "realm://objectserver.realm.io/~/default", "realm-object-server/" + user.getIdentifier(), "default" },
                { "realm://objectserver.realm.io/~/sub/default", "realm-object-server/" + user.getIdentifier() + "/sub", "default" }
        };

        for (String[] validUrl : validUrls) {
            String serverUrl  = validUrl[0];
            String expectedFolder = validUrl[1];
            String expectedFileName = validUrl[2];

            SyncConfiguration config = new SyncConfiguration.Builder(context)
                    .serverUrl(serverUrl)
                    .user(user)
                    .build();

            assertEquals(new File(context.getFilesDir(), expectedFolder), config.getRealmDirectory());
            assertEquals(expectedFileName, config.getRealmFileName());
        }
    }

    @Test
    public void serverUrl_invalidUrlThrows() {
        String[] invalidUrls = {
            null,
// TODO Should these two fail?
//            "objectserver.realm.io/~/default", // Missing protocol. TODO Should we just default to one?
//            "/~/default", // Missing server
            "realm://objectserver.realm.io/~/default.realm", // Ending with .realm
            "realm://objectserver.realm.io/<~>/default.realm", // Invalid chars <>
            "realm://objectserver.realm.io/~/default.realm/", // Ending with /
        };

        SyncConfiguration.Builder builder = new SyncConfiguration.Builder(context);
        for (String invalidUrl : invalidUrls) {
            try {
                builder.serverUrl(invalidUrl);
                fail(invalidUrl + " should have failed.");
            } catch (IllegalArgumentException ignore) {
            }
        }
    }

    @Test
    public void userAndServerUrlRequired() {
        SyncConfiguration.Builder builder;

        // Both missing
        builder = new SyncConfiguration.Builder(context);
        try {
            builder.build();
        } catch (IllegalStateException ignore) {
        }

        builder = new SyncConfiguration.Builder(context);
        try {
            builder.user(createTestUser(Long.MAX_VALUE)).build();
        } catch (IllegalStateException ignore) {
        }

        // user missing
        builder = new SyncConfiguration.Builder(context);
        try {
            builder.serverUrl("realm://foo.bar/~/default").build();
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    public void errorHandler() {
        SyncConfiguration.Builder builder;
        builder = new SyncConfiguration.Builder(context)
                .user(User.createLocal())
                .serverUrl("realm://objectserver.realm.io/default");

        Session.ErrorHandler errorHandler = new Session.ErrorHandler() {
            @Override
            public void onError(Session session, ObjectServerError error) {

            }
        };

        SyncConfiguration config = builder.errorHandler(errorHandler).build();
        assertEquals(errorHandler, config.getErrorHandler());
    }

    @Test
    public void errorHandler_fromSyncManager() {
        // Set default error handler
        Session.ErrorHandler errorHandler = new Session.ErrorHandler() {
            @Override
            public void onError(Session session, ObjectServerError error) {

            }
        };
        SyncManager.setDefaultSessionErrorHandler(errorHandler);

        // Create configuration using the default handler
        SyncConfiguration config = new SyncConfiguration.Builder(context)
                .user(User.createLocal())
                .serverUrl("realm://objectserver.realm.io/default")
                .build();
        assertEquals(errorHandler, config.getErrorHandler());
        SyncManager.setDefaultSessionErrorHandler(null);
    }


    @Test
    public void errorHandler_nullThrows() {
        SyncConfiguration.Builder builder;
        builder = new SyncConfiguration.Builder(context)
                .user(User.createLocal())
                .serverUrl("realm://objectserver.realm.io/default");

        try {
            builder.errorHandler(null);
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void migration_alwaysThrows() {
        SyncConfiguration.Builder builder;
        builder = new SyncConfiguration.Builder(context)
                .user(User.createLocal())
                .serverUrl("realm://objectserver.realm.io/default");

        try {
            builder.migration(null);
        } catch (IllegalArgumentException ignore) {
        }

        try {
            builder.migration(new RealmMigration() {
                @Override
                public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                    // Nothing
                }
            });
        } catch (IllegalArgumentException ignore) {
        }
    }
    
    private User createTestUser(long expires) {
        JSONObject obj = new JSONObject();
        try {
            JSONObject token = new JSONObject();
            token.put("token", UUID.randomUUID().toString());
            JSONObject tokenData = new JSONObject();
            JSONArray perms = new JSONArray(); // Grant all permissions
            for (int i = 0; i < Token.Permission.values().length; i++) {
                perms.put(Token.Permission.values()[i].toString().toLowerCase(Locale.US));
            }
            tokenData.put("identity", UUID.randomUUID().toString());
            tokenData.put("path", null);
            tokenData.put("expires", expires);
            tokenData.put("access", perms);
            token.put("token_data", tokenData);
            obj.put("refreshToken", token);
            obj.put("authUrl", "http://dummy.org/auth");
            return User.fromJson(obj.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
