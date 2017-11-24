/*
 * Copyright 2015 Realm Inc.
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
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Date;

import io.realm.entities.IOSAllTypes;
import io.realm.entities.IOSChild;
import io.realm.internal.OsObjectStore;
import io.realm.internal.Table;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This class test interoperability with Realms created on iOS.
 */
@RunWith(AndroidJUnit4.class)
public class IOSRealmTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private static final String[] IOS_VERSIONS = new String[] {"0.98.0"};
    private static final String REALM_NAME = "alltypes.realm";
    private Realm realm;
    private Context context;

    @Before
    public void setUp() {
        RealmConfiguration defaultConfiguration = configFactory.createConfigurationBuilder()
                .name(REALM_NAME)
                .schema(IOSAllTypes.class, IOSChild.class)
                .build();
        Realm.setDefaultConfiguration(defaultConfiguration);
        context = InstrumentationRegistry.getInstrumentation().getContext();
    }

    @After
    public void tearDown() {
        Realm.removeDefaultConfiguration();
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void iOSDataTypes() throws IOException {
        for (String iosVersion : IOS_VERSIONS) {
            configFactory.copyRealmFromAssets(context,
                    "ios/" + iosVersion + "-alltypes.realm", REALM_NAME);
            realm = Realm.getDefaultInstance();
            RealmResults<IOSAllTypes> result = realm.where(IOSAllTypes.class).sort("id", Sort.ASCENDING).findAll();
            // Verifies metadata.
            Table table = realm.getTable(IOSAllTypes.class);
            assertEquals("id", OsObjectStore.getPrimaryKeyForObject(realm.getSharedRealm(), IOSAllTypes.CLASS_NAME));
            assertTrue(table.hasSearchIndex(table.getColumnIndex("id")));
            // Iterative check.
            for (int i = 0; i < 10; i++) {
                IOSAllTypes obj = result.get(i);
                assertTrue(obj.isBoolCol());
                assertEquals(i + 1, obj.getShortCol());
                assertEquals(10 + i, obj.getIntCol());
                assertEquals(100 + i, obj.getLongCol());
                assertEquals(100000000L + (long)i, obj.getLongLongCol());
                assertEquals(1.23F + (float)i, obj.getFloatCol(), 0F);
                assertEquals(1.234D + (double)i, obj.getDoubleCol(), 0D);
                assertArrayEquals(new byte[]{1, 2, 3}, obj.getByteCol());
                assertEquals("String " + Integer.toString(i), obj.getStringCol());
                assertEquals(new Date((1000L + i) * 1000), obj.getDateCol());
                assertEquals("Foo", result.get(i).getChild().getName());
                assertEquals(10, result.get(i).getChildren().size());
                for (int j = 0; j < 10; j++) {
                    assertEquals("Name: " + Integer.toString(i), result.get(i).getChildren().get(j).getName());
                }
            }
        }
    }

    @Test
    public void iOSDataTypesDefaultValues() throws IOException {
        for (String iosVersion : IOS_VERSIONS) {
            configFactory.copyRealmFromAssets(context,
                    "ios/" + iosVersion + "-alltypes-default.realm", REALM_NAME);
            realm = Realm.getDefaultInstance();

            IOSAllTypes obj = realm.where(IOSAllTypes.class).findFirst();
            assertFalse(obj.isBoolCol());
            assertEquals(0, obj.getShortCol());
            assertEquals(0, obj.getIntCol());
            assertEquals(0, obj.getLongCol());
            assertEquals(0L, obj.getLongLongCol());
            assertEquals(0.0F, obj.getFloatCol(), 0F);
            assertEquals(0.0D, obj.getDoubleCol(), 0D);
            assertArrayEquals(new byte[0], obj.getByteCol());
            assertEquals("", obj.getStringCol());
            assertEquals(new Date(0), obj.getDateCol());
            assertNull(obj.getChild());
            assertEquals(0, obj.getChildren().size());
        }
    }

    @Test
    public void iOSDataTypesNullValues() throws IOException {
        for (String iosVersion : IOS_VERSIONS) {
            configFactory.copyRealmFromAssets(context,
                    "ios/" + iosVersion + "-alltypes-null-value.realm", REALM_NAME);
            realm = Realm.getDefaultInstance();

            IOSAllTypes obj = realm.where(IOSAllTypes.class).findFirst();
            assertEquals(null, obj.getByteCol());
            assertEquals(null, obj.getStringCol());
            assertEquals(null, obj.getDateCol());
            assertEquals(null, obj.getChild());
            assertEquals(new RealmList<IOSChild>(), obj.getChildren());
        }
    }

    @Test
    @SuppressWarnings("ConstantOverflow")
    public void iOSDataTypesMinimumValues() throws IOException {
        for (String iosVersion : IOS_VERSIONS) {
            configFactory.copyRealmFromAssets(context,
                    "ios/" + iosVersion + "-alltypes-min.realm", REALM_NAME);
            realm = Realm.getDefaultInstance();

            IOSAllTypes obj = realm.where(IOSAllTypes.class).findFirst();
            assertFalse(obj.isBoolCol());
            assertEquals(Short.MIN_VALUE, obj.getShortCol());
            assertEquals(Integer.MIN_VALUE, obj.getIntCol());
            assertEquals(Integer.MIN_VALUE, obj.getLongCol());
            assertEquals(Long.MIN_VALUE, obj.getLongLongCol());
            assertEquals(-Float.MAX_VALUE, obj.getFloatCol(), 0F);
            assertEquals(-Double.MAX_VALUE, obj.getDoubleCol(), 0D);
            assertArrayEquals(new byte[0], obj.getByteCol());
            assertEquals("", obj.getStringCol());
            assertEquals(0x8000000000000000L * 1000L, obj.getDateCol().getTime());
        }
    }

    @Test
    @SuppressWarnings("ConstantOverflow")
    public void iOSDataTypesMaximumValues() throws IOException {
        for (String iosVersion : IOS_VERSIONS) {
            configFactory.copyRealmFromAssets(context,
                    "ios/" + iosVersion + "-alltypes-max.realm", REALM_NAME);
            realm = Realm.getDefaultInstance();

            IOSAllTypes obj = realm.where(IOSAllTypes.class).findFirst();
            assertEquals(Short.MAX_VALUE, obj.getShortCol());
            assertEquals(Integer.MAX_VALUE, obj.getIntCol());
            assertEquals(Integer.MAX_VALUE, obj.getLongCol());
            assertEquals(Long.MAX_VALUE, obj.getLongLongCol());
            assertEquals(Float.MAX_VALUE, obj.getFloatCol(), 0F);
            assertEquals(Double.MAX_VALUE, obj.getDoubleCol(), 0D);
            assertArrayEquals(new byte[0], obj.getByteCol());
            assertEquals("", obj.getStringCol());
            assertEquals(0x8000000000000000L * 1000L, obj.getDateCol().getTime());
        }
    }

    @Test
    public void iOSEncryptedRealm() throws IOException {
        for (String iosVersion : IOS_VERSIONS) {
            configFactory.copyRealmFromAssets(context,
                    "ios/" + iosVersion + "-alltypes-default-encrypted.realm", REALM_NAME);
            RealmConfiguration realmConfig = configFactory.createConfigurationBuilder()
                    .name(REALM_NAME)
                    .encryptionKey(getIOSKey())
                    .schema(IOSAllTypes.class, IOSChild.class)
                    .build();
            realm = Realm.getInstance(realmConfig);

            IOSAllTypes obj = realm.where(IOSAllTypes.class).findFirst();
            assertFalse(obj.isBoolCol());
            assertEquals(0, obj.getShortCol());
            assertEquals(0, obj.getIntCol());
            assertEquals(0L, obj.getLongCol());
            assertEquals(0.0F, obj.getFloatCol(), 0F);
            assertEquals(0.0D, obj.getDoubleCol(), 0D);
            assertArrayEquals(new byte[0], obj.getByteCol());
            assertEquals("", obj.getStringCol());
            assertEquals(new Date(0), obj.getDateCol());
            assertNull(obj.getChild());
            assertEquals(0, obj.getChildren().size());
        }
    }

    private byte[] getIOSKey() {
        byte[] keyData = new byte[64];
        for (int i = 0; i < keyData.length; i++) {
            keyData[i] = 1;
        }
        return keyData;
    }
}
