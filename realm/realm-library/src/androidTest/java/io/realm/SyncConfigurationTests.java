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

package io.realm;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.realm.rule.RunInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
        User user = createTestUser();
        String[][] validUrls = {
                // <URL>, <Folder>, <FileName>
                { "realm://objectserver.realm.io/~/default", "realm-object-server/" + user.getIdentity(), "default" },
                { "realm://objectserver.realm.io/~/sub/default", "realm-object-server/" + user.getIdentity() + "/sub", "default" }
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
            "realm://objectserver.realm.io/~/default.realm.lock", // Ending with .realm.lock
            "realm://objectserver.realm.io/~/default.realm.management", // Ending with .realm.management
            "realm://objectserver.realm.io/<~>/default.realm", // Invalid chars <>
            "realm://objectserver.realm.io/~/default.realm/", // Ending with /
            "realm://objectserver.realm.io/~/Αθήνα", // Non-ascii
            "realm://objectserver.realm.io/~/foo/../bar", // .. is not allowed
            "realm://objectserver.realm.io/~/foo/./bar", // . is not allowed
            "http://objectserver.realm.io/~/default", // wrong scheme
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

    private String makeServerUrl(int len) {
        StringBuilder builder = new StringBuilder("realm://objectserver.realm.io/~/");
        for (int i = 0; i < len; i++) {
            builder.append('A');
        }
        return builder.toString();
    }

    @Test
    public void serverUrl_length() {
        int[] lengths = {1, SyncConfiguration.MAX_FILE_NAME_LENGTH - 1,
                SyncConfiguration.MAX_FILE_NAME_LENGTH, SyncConfiguration.MAX_FILE_NAME_LENGTH + 1, 1000};

        for (int len : lengths) {
            SyncConfiguration.Builder builder = new SyncConfiguration.Builder(context)
                .serverUrl(makeServerUrl(len))
                .user(createTestUser());

            SyncConfiguration config = builder.build();
            assertTrue("Length: " + len, config.getRealmFileName().length() <= SyncConfiguration.MAX_FILE_NAME_LENGTH);
            assertTrue("Length: " + len, config.getPath().length() <= SyncConfiguration.MAX_FULL_PATH_LENGTH);
        }
    }

    @Test
    public void serverUrl_invalidChars() {
        SyncConfiguration.Builder builder = new SyncConfiguration.Builder(context)
                .serverUrl("realm://objectserver.realm.io/~/?")
                .user(createTestUser());
        SyncConfiguration config = builder.build();
        assertFalse(config.getRealmFileName().contains("?"));
    }

    @Test
    public void serverUrl_port() {
        Map<String, Integer> urlPort = new HashMap<String, Integer>();
        urlPort.put("realm://objectserver.realm.io/~/default", SyncConfiguration.PORT_REALM);
        urlPort.put("realms://objectserver.realm.io/~/default", SyncConfiguration.PORT_REALMS);
        urlPort.put("realm://objectserver.realm.io:8080/~/default", 8080);
        urlPort.put("realms://objectserver.realm.io:2443/~/default", 2443);

        for (String url : urlPort.keySet()) {
            SyncConfiguration config = new SyncConfiguration.Builder(context)
                    .serverUrl(url)
                    .user(createTestUser())
                    .build();
            assertEquals(urlPort.get(url).intValue(), config.getServerUrl().getPort());
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
                .user(createTestUser())
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
                .user(createTestUser())
                .serverUrl("realm://objectserver.realm.io/default")
                .build();
        assertEquals(errorHandler, config.getErrorHandler());
        SyncManager.setDefaultSessionErrorHandler(null);
    }


    @Test
    public void errorHandler_nullThrows() {
        SyncConfiguration.Builder builder;
        builder = new SyncConfiguration.Builder(context);

        try {
            builder.errorHandler(null);
        } catch (IllegalArgumentException ignore) {
        }
    }

}
