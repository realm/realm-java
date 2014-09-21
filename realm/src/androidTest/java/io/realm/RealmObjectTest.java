package io.realm;

import io.realm.entities.AllTypes;
import io.realm.internal.Row;


public class RealmObjectTest extends RealmSetupTests {

    // test io.realm.RealmObject Api

    // Row realmGetRow()
    public void testRealmGetRowReturnRow() {

        testRealm.beginWrite();
        RealmObject realmObject = testRealm.create( AllTypes.class);

        Row row = realmObject.realmGetRow();

        testRealm.commit();
        assertNotNull("RealmObject.realmGetRow returns zero ", row);
    }

    public void testResultListCheckSize() {

        RealmResults<AllTypes> allTypes = testRealm.where(AllTypes.class).findAll();

        assertNotNull("ResultList.get has returned null", allTypes);
        assertEquals("ResultList.size returned invalid size", TEST_DATA_SIZE, allTypes.size());
    }


}