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

import android.test.AndroidTestCase;

import java.io.IOException;
import java.util.Date;

import io.realm.entities.IOSAllTypes;
import io.realm.entities.IOSChild;
import io.realm.internal.Table;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;

/**
 * This class test interoperability with Realms created on iOS.
 */
public class IOSRealmTests extends AndroidTestCase {

    private static final String[] IOS_VERSIONS = new String[] {"0.96.2"};
    private static final String REALM_NAME = "alltypes.realm";
    private Realm realm;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RealmConfiguration defaultConfiguration = new RealmConfiguration.Builder(getContext())
                .name(REALM_NAME)
                .schema(IOSAllTypes.class, IOSChild.class)
                .build();
        Realm.setDefaultConfiguration(defaultConfiguration);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Realm.removeDefaultConfiguration();
        if (realm != null) {
            realm.close();
        }
    }

    // Test relationships and that data in general can be retrieved from an iOS realm
    public void testIOSDatatypes() throws IOException {
        for (String iosVersion : IOS_VERSIONS) {
            TestHelper.prepareDatabaseFromAssets(getContext(),  "ios/" + iosVersion + "-alltypes.realm", REALM_NAME);
            realm = Realm.getDefaultInstance();
            RealmResults<IOSAllTypes> result = realm.allObjectsSorted(IOSAllTypes.class, "id", Sort.ASCENDING);

            // Verify metadata
            Table table = realm.getTable(IOSAllTypes.class);
            assertTrue(table.hasPrimaryKey());
            assertTrue(table.hasSearchIndex(table.getColumnIndex("id")));

            IOSAllTypes obj = result.get(1);
            assertTrue(obj.isBoolCol());
            assertEquals(2, obj.getShortCol());
            assertEquals(11, obj.getIntCol());
            assertEquals(101L, obj.getLongCol());
            assertEquals(2.23F, obj.getFloatCol());
            assertEquals(2.234D, obj.getDoubleCol());
            assertArrayEquals(new byte[]{1, 2, 3}, obj.getByteCol());
            assertEquals("String 1", obj.getStringCol());
            assertEquals(new Date(1001 * 1000), obj.getDateCol());
            assertEquals("Foo", result.get(1).getChild().getName());
            assertEquals(10, result.size());
            assertEquals(10, result.get(1).getChildren().size());
            assertEquals("Name: 1", result.get(1).getChildren().get(1).getName());
        }
    }

    public void testIOSDatatypesDefaultValues() throws IOException {
        for (String iosVersion : IOS_VERSIONS) {
            TestHelper.prepareDatabaseFromAssets(getContext(),  "ios/" + iosVersion + "-alltypes-default.realm", REALM_NAME);
            realm = Realm.getDefaultInstance();

            IOSAllTypes obj = realm.allObjects(IOSAllTypes.class).first();
            assertFalse(obj.isBoolCol());
            assertEquals(0, obj.getShortCol());
            assertEquals(0, obj.getIntCol());
            assertEquals(0L, obj.getLongCol());
            assertEquals(0.0F, obj.getFloatCol());
            assertEquals(0.0D, obj.getDoubleCol());
            assertArrayEquals(new byte[0], obj.getByteCol());
            assertEquals("", obj.getStringCol());
            assertEquals(new Date(0), obj.getDateCol());
            assertNull(obj.getChild());
            assertEquals(0, obj.getChildren().size());
        }
    }

    public void testIOSDatatypesMinimumValues() throws IOException {
        for (String iosVersion : IOS_VERSIONS) {
            TestHelper.prepareDatabaseFromAssets(getContext(),  "ios/" + iosVersion + "-alltypes-min.realm", REALM_NAME);
            realm = Realm.getDefaultInstance();

            IOSAllTypes obj = realm.allObjects(IOSAllTypes.class).first();
            assertEquals(Short.MIN_VALUE, obj.getShortCol());
            assertEquals(Integer.MIN_VALUE, obj.getIntCol());
            assertEquals(Long.MIN_VALUE, obj.getLongCol());
            assertEquals(Float.MIN_NORMAL, obj.getFloatCol());
            assertEquals(Double.MIN_NORMAL, obj.getDoubleCol());
        }
    }

    public void testIOSDatatypesMaximumValues() throws IOException {
        for (String iosVersion : IOS_VERSIONS) {
            TestHelper.prepareDatabaseFromAssets(getContext(), "ios/" + iosVersion + "-alltypes-max.realm", REALM_NAME);
            realm = Realm.getDefaultInstance();

            IOSAllTypes obj = realm.allObjects(IOSAllTypes.class).first();
            assertEquals(Short.MAX_VALUE, obj.getShortCol());
            assertEquals(Integer.MAX_VALUE, obj.getIntCol());
            assertEquals(Long.MAX_VALUE, obj.getLongCol());
            assertEquals(Float.MAX_VALUE, obj.getFloatCol());
            assertEquals(Double.MAX_VALUE, obj.getDoubleCol());
        }
    }

    public void testIOSEncryptedRealm() throws IOException {
        for (String iosVersion : IOS_VERSIONS) {
            TestHelper.prepareDatabaseFromAssets(getContext(),  "ios/" + iosVersion + "-alltypes-default-encrypted.realm", REALM_NAME);
            RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext())
                    .name(REALM_NAME)
                    .encryptionKey(getIOSKey())
                    .schema(IOSAllTypes.class, IOSChild.class)
                    .build();
            realm = Realm.getInstance(realmConfig);

            IOSAllTypes obj = realm.allObjects(IOSAllTypes.class).first();
            assertFalse(obj.isBoolCol());
            assertEquals(0, obj.getShortCol());
            assertEquals(0, obj.getIntCol());
            assertEquals(0L, obj.getLongCol());
            assertEquals(0.0F, obj.getFloatCol());
            assertEquals(0.0D, obj.getDoubleCol());
            assertArrayEquals(new byte[0], obj.getByteCol());
            assertEquals("", obj.getStringCol());
            assertEquals(new Date(0), obj.getDateCol());
            assertNull(obj.getChild());
            assertEquals(0, obj.getChildren().size());
        }
    }

    public byte[] getIOSKey() {
        byte[] keyData = new byte[64];
        for (int i = 0; i < keyData.length; i++) {
            keyData[i] = 1;
        }
        return keyData;
    }
}
