package io.realm;

import io.realm.entities.AllTypes;
import io.realm.internal.Row;


public class RealmObjectTest extends RealmSetupTests {

    // test io.realm.RealmObject Api

    // Row realmGetRow()
    public void testRealmGetRowReturnsValidRow() {

        testRealm.beginTransaction();
        RealmObject realmObject = testRealm.createObject( AllTypes.class);

        Row row = realmObject.realmGetRow();

        testRealm.commitTransaction();
        assertNotNull("RealmObject.realmGetRow returns zero ", row);
        assertEquals("RealmObject.realmGetRow seems to return wrong row type: ",7 , row.getColumnCount());
    }

}
