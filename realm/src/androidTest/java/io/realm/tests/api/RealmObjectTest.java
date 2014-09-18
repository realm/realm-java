package io.realm.tests.api;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.ResultList;
import io.realm.internal.Row;
import io.realm.tests.api.entities.AllTypes;


public class RealmObjectTest extends RealmSetupTests {
    final static int TEST_DATA_SIZE = 102;

    // Row realmGetRow()
    public void testRealmGetRowReturnRow() throws IOException {
        Realm realm = getTestRealm();

        realm.beginWrite();
        RealmObject realmObject = getTestObject(realm, AllTypes.class);

        Row row = realmObject.realmGetRow();

        realm.commit();
        assertNotNull("RealmObject.realmGetRow returns zero ", row);
    }

    public void testResultListCheckSize() throws IOException {
        Realm realm = getTestRealm();
        buildAllTypesTestData(realm, TEST_DATA_SIZE);

        ResultList<AllTypes> allTypes = realm.where(AllTypes.class).findAll();

        assertNotNull("ResultList.get has returned null", allTypes);
        assertEquals("ResultList.size returned invalid size", TEST_DATA_SIZE, allTypes.size());
    }


}