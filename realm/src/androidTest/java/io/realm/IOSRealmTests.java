package io.realm;

import android.test.AndroidTestCase;

import java.io.IOException;
import java.util.Date;

import io.realm.entities.IOSAllTypes;
import io.realm.entities.IOSChild;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;

/**
 * This class test interoperability with Realms created on iOS.
 */
public class IOSRealmTests extends AndroidTestCase {

    private static final String REALM_NAME = "alltypes.realm";
    private Realm realm;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Realm.setSchema(IOSAllTypes.class, IOSChild.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Realm.setSchema(null);
        if (realm != null) {
            realm.close();
        }
    }

    // Test relationships and that data in general can be retrieved from an iOS realm
    public void testIOSDatatypes() throws IOException {
        TestHelper.prepareDatabaseFromAssets(getContext(),  "ios/0.90.4-alltypes.realm", REALM_NAME);
        realm = Realm.getInstance(getContext(), REALM_NAME);
        RealmResults<IOSAllTypes> result = realm.allObjectsSorted(IOSAllTypes.class, "longCol", RealmResults.SORT_ORDER_ASCENDING);

        IOSAllTypes obj = result.get(1);
        assertTrue(obj.isBoolCol());
        assertEquals(2, obj.getShortCol());
        assertEquals(11, obj.getIntCol());
        assertEquals(101L, obj.getLongCol());
        assertEquals(2.23F, obj.getFloatCol());
        assertEquals(2.234D, obj.getDoubleCol());
        assertArrayEquals(new byte[] {1,2,3}, obj.getByteCol());
        assertEquals("String 1", obj.getStringCol());
        assertEquals(new Date(1001 * 1000), obj.getDateCol());
        assertEquals("Foo", result.get(1).getChild().getName());
        assertEquals(10, result.size());
        assertEquals(10, result.get(1).getChildren().size());
        assertEquals("Name: 1", result.get(1).getChildren().get(1).getName());
    }

    public void testIOSDatatypesDefaultValues() throws IOException {
        TestHelper.prepareDatabaseFromAssets(getContext(),  "ios/0.90.4-alltypes-default.realm", REALM_NAME);
        realm = Realm.getInstance(getContext(), REALM_NAME);

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

    public void testIOSDatatypesMinimumValues() throws IOException {
        TestHelper.prepareDatabaseFromAssets(getContext(),  "ios/0.90.4-alltypes-min.realm", REALM_NAME);
        realm = Realm.getInstance(getContext(), REALM_NAME);

        IOSAllTypes obj = realm.allObjects(IOSAllTypes.class).first();
        assertEquals(Short.MIN_VALUE, obj.getShortCol());
        assertEquals(Integer.MIN_VALUE, obj.getIntCol());
        assertEquals(Long.MIN_VALUE, obj.getLongCol());
        assertEquals(Float.MIN_NORMAL, obj.getFloatCol());
        assertEquals(Double.MIN_NORMAL, obj.getDoubleCol());
    }

    public void testIOSDatatypesMaximumValues() throws IOException {
        TestHelper.prepareDatabaseFromAssets(getContext(),  "ios/0.90.4-alltypes-max.realm", REALM_NAME);
        realm = Realm.getInstance(getContext(), REALM_NAME);

        IOSAllTypes obj = realm.allObjects(IOSAllTypes.class).first();
        assertEquals(Short.MAX_VALUE, obj.getShortCol());
        assertEquals(Integer.MAX_VALUE, obj.getIntCol());
        assertEquals(Long.MAX_VALUE, obj.getLongCol());
        assertEquals(Float.MAX_VALUE, obj.getFloatCol());
        assertEquals(Double.MAX_VALUE, obj.getDoubleCol());
    }

    public void testIOSEncryptedRealm() throws IOException {
        TestHelper.prepareDatabaseFromAssets(getContext(),  "ios/0.90.5-alltypes-default-encrypted.realm", REALM_NAME);
        realm = Realm.getInstance(getContext(), REALM_NAME, getIOSKey());

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

    public byte[] getIOSKey() {
        byte[] keyData = new byte[64];
        for (int i = 0; i < keyData.length; i++) {
            keyData[i] = 1;
        }
        return keyData;
    }
}
