package io.realm.tests.typed;

import android.test.AndroidTestCase;

import java.util.Date;

import io.realm.tests.typed.entities.AllColumns;
import io.realm.tests.typed.entities.User;
import io.realm.typed.Realm;
import io.realm.typed.RealmList;
import io.realm.typed.RealmTableOrViewList;

public class RealmListTest extends AndroidTestCase {

    private Realm realm;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        realm = new Realm(getContext());

        realm.clear();

    }

    public void addObjectsToRealm() {
        realm.beginWrite();

        for(int i = 0; i < 10; i++) {
            AllColumns allColumns = new AllColumns();

            allColumns.setColumnString("dsfs");
            allColumns.setColumnLong(i);
            allColumns.setColumnFloat(1.1F);
            allColumns.setColumnDouble(1.1);
            allColumns.setColumnBoolean(true);
            allColumns.setColumnDate(new Date());
            allColumns.setColumnBinary(new byte[20]);

            User user = new User();
            user.setId(i);
            user.setName("Test User");
            user.setEmail("user@test.com");

            allColumns.setColumnRealmObject(user);

            realm.add(allColumns);
        }

        realm.commit();

    }

    public void testAddObject() {

        AllColumns allColumns = new AllColumns();

        allColumns.setColumnString("dsfs");
        allColumns.setColumnLong(1);
        allColumns.setColumnFloat(1.1F);
        allColumns.setColumnDouble(1.1);
        allColumns.setColumnBoolean(true);
        allColumns.setColumnDate(new Date());
        allColumns.setColumnBinary(new byte[20]);


        realm.beginWrite();

        RealmTableOrViewList<AllColumns> list = realm.allObjects(AllColumns.class);

        assertEquals(0, list.size());

        try {
            list.add(allColumns);
            fail("Should throw UnsupportedOperationException");
            realm.commit();

        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }

    }

    public void testAllObjects() {

        addObjectsToRealm();

        RealmList<AllColumns> allColumnsList = realm.allObjects(AllColumns.class);

        assertEquals(10, allColumnsList.size());

    }

    public void testGetObjects() {

        addObjectsToRealm();

        RealmList<AllColumns> allColumnsList = realm.allObjects(AllColumns.class);

        assertEquals(5, allColumnsList.get(5).getColumnLong());

    }

    public void testGetFirstObject() {

        addObjectsToRealm();

        RealmList<AllColumns> allColumnsList = realm.allObjects(AllColumns.class);

        assertEquals(0, allColumnsList.first().getColumnLong());

    }

    public void testGetLastObject() {

        addObjectsToRealm();

        RealmList<AllColumns> allColumnsList = realm.allObjects(AllColumns.class);

        assertEquals(9, allColumnsList.last().getColumnLong());

    }

    public void testRemoveByIndex() {

        addObjectsToRealm();

        realm.beginWrite();

        RealmList<AllColumns> allColumnsList = realm.allObjects(AllColumns.class);

        try {
            allColumnsList.remove(5);
            fail("Should throw UnsupportedOperationException");
            realm.commit();
        } catch(UnsupportedOperationException e) {
        }

    }

    public void testRemoveByObject() {

        addObjectsToRealm();

        realm.beginWrite();

        RealmList<AllColumns> allColumnsList = realm.allObjects(AllColumns.class);

        try {

            allColumnsList.remove(allColumnsList.get(5));
            fail("Should throw UnsupportedOperationException");
            realm.commit();

        } catch(UnsupportedOperationException e) {
        }

    }

    public void testQuery() {

        addObjectsToRealm();

        RealmList all = realm.where(AllColumns.class).greaterThan("columnlong", 0).findAll();

        assertEquals(9, all.size());

        RealmList result = all.where().lessThan("columnlong", 5).findAll();

        assertEquals(4, result.size());

    }

}
