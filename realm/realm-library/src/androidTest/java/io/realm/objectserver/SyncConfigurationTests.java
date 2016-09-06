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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

import io.realm.Realm;
import io.realm.internal.objectserver.Token;
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
            "objectserver.realm.io/~/default", // Missing protocol. TODO Should we just default to one?
            "/~/default", // Missing server
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

        // serverUrl missing
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
    public void syncPolicy() {

    }

    @Test
    public void syncPolicy_nullThrows() {
//        User user = User.createLocal();
//        user.add(con);
//
    }

    @Ignore("Only used for quick testing without needing to spin up a full integration test")
    @Test
    @RunTestInLooperThread
    public void basicIntegrationTest2() {
        User.loginAsync(Credentials.fromUsernamePassword("cm", "test", false), "http://192.168.1.21:8080/auth", new User.Callback() {
            @Override
            public void onSuccess(User user) {
                SyncConfiguration config = new SyncConfiguration.Builder(context)
                        .user(user)
                        .serverUrl("realm://192.168.1.21/~/default")
                        .build();
                Realm realm = Realm.getInstance(config);
                realm.beginTransaction();
                realm.commitTransaction();
            }

            @Override
            public void onError(ObjectServerError error) {
                fail(error.toString());
            }
        });
    }

    private User createTestUser(long expires) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("identifier", UUID.randomUUID().toString());
            JSONObject token = new JSONObject();
            JSONArray perms = new JSONArray(); // Grant all permissions
            for (int i = 0; i < Token.Permission.values().length; i++) {
                perms.put(Token.Permission.values()[i].toString().toLowerCase(Locale.US));
            }
            token.put("access", perms);
            token.put("token", UUID.randomUUID().toString());
            token.put("expires", expires);
            obj.put("refreshToken", token);
            obj.put("authUrl", "http://dummy.org/auth");
            return User.fromJson(obj.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
