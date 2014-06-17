package io.realm.typed;

import android.test.AndroidTestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.ColumnType;
import io.realm.Table;
import io.realm.typed.entities.AllColumns;
import io.realm.typed.entities.Dog;
import io.realm.typed.entities.User;


public class RealmTest extends AndroidTestCase {

    private Realm realm;

    @Override
    public void setUp() throws Exception {
        realm = new Realm(getContext());

        realm.clear();
    }


    public void testRealm() {

        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                System.out.println("Realm changed");
            }
        });

        try {
            realm.beginWrite();

            // Insert
            for (int i = 0; i < 120; i++) {

                User user = realm.create(User.class);

                user.setId(i);
                user.setName("Rasmus");
                user.setEmail("ra@realm.io");

                user.setId(10);

            }

            realm.commit();

        } catch(Throwable t) {
            t.printStackTrace();
        }

        RealmList<User> users = realm.where(User.class).findAll();


        // Get
        User user1 = users.get(100);
        assertEquals("Rasmus", user1.getName());


        try {

            realm.beginWrite();
            users = realm.where(User.class).findAll();
            user1 = users.get(100);

            user1.setName("TestName");

            realm.commit();

        } catch(Throwable t) {
        }

        users = realm.where(User.class).findAll();

        // Get
        user1 = users.get(100);

        assertEquals("TestName", user1.getName());

        assertEquals(120, users.size());

        // Iterable
        for(User user : users) {
            System.out.println(user.getId());
        }



        realm.beginWrite();
        users = realm.where(User.class).findAll();
        user1 = users.get(100);
        user1.setId(100);

        realm.commit();



        // Query
        RealmList<User> results = realm.where(User.class).equalTo("id", 10).findAll();

        assertEquals(119, results.size());
        assertEquals(10, results.get(0).getId());

    }


    public void testCreate() {

        realm.beginWrite();

        AllColumns obj = realm.create(AllColumns.class);

        obj.setColumnString("dsfs");
        obj.setColumnLong(1);
        obj.setColumnFloat(1.1F);
        obj.setColumnDouble(1.1);
        obj.setColumnBoolean(true);
        obj.setColumnDate(new Date());
        obj.setColumnBinary(new byte[20]);

        realm.commit();

        RealmList<AllColumns> result = realm.where(AllColumns.class).findAll();

        assertEquals(1, result.size());

    }

    public void testAdd() {

        realm.beginWrite();

        AllColumns obj = new AllColumns();

        obj.setColumnString("dsfs");
        obj.setColumnLong(1);
        obj.setColumnFloat(1.1F);
        obj.setColumnDouble(1.1);
        obj.setColumnBoolean(true);
        obj.setColumnDate(new Date());
        obj.setColumnBinary(new byte[20]);

        User user = new User();
        user.setName("Rasmus");
        user.setEmail("ra@realm.io");
        user.setId(0);

        obj.setColumnRealmObject(user);

        realm.add(obj);


        realm.commit();

        assertEquals(1, realm.allObjects(AllColumns.class).size());
        assertEquals(1, realm.allObjects(User.class).size());

    }

    public void testLinkList() {

        User user1 = new User();
        user1.setName("Rasmus");
        user1.setEmail("ra@realm.io");
        user1.setId(0);

        User user2 = new User();
        user2.setName("Morten");
        user2.setEmail("mk@realm.io");
        user2.setId(1);


        Dog dog = new Dog();
        dog.setName("Fido");
        dog.getOwners().add(user1);
        dog.getOwners().add(user2);


        realm.beginWrite();
        realm.add(dog);
        realm.commit();

        Dog fido = realm.get(Dog.class, 0);

        assertEquals("Fido", fido.getName());

        List<User> owners = fido.getOwners();

        assertEquals("Rasmus", owners.get(0).getName());



/*
        assertEquals(1, realm.allObjects(Dog.class).size());
        assertEquals(2, realm.allObjects(User.class).size());
*/

    }

    public void testMigration() {
        /*
        realm.ensureRealmAtVersion(2, new RealmMigration() {
            @Override
            public void execute(Realm realm, int version) {

                Table table = realm.getTable(User.class);

                if(realm.getVersion() < 1) {
                    table.addColumn(ColumnType.STRING, "newStringCol");
                }

                if(realm.getVersion() < 2) {
                    table.removeColumn(table.getColumnIndex("newStringCol"));
                }

                realm.setVersion(version);

            }
        });
        */
    }

}
