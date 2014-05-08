package io.realm.typed;

import android.test.AndroidTestCase;

import java.util.Collections;

import io.realm.typed.entities.User;


public class RealmTest extends AndroidTestCase {

    public void testRealm() {

        // Init
        RealmList<User> users = Realms.newList(this.getContext(), User.class);
        // Notice that RealmList implements List, which means that it can be used in a lot of existing code


        // Insert
        for(int i = 0; i < 120; i++) {

            User user = users.create();
            user.setId(i);
            user.setName("Rasmus");
            user.setEmail("ra@realm.io");

            users.add(user);

            user.setId(10);

        }

        // Get
        User user1 = users.get(100);
        assertEquals(user1.getName(), "Rasmus");
        user1.setName("TestName");

        assertEquals(user1.getName(), "TestName");

        assertEquals(120, users.size());

        // Iterable
        for(User user : users) {
            System.out.println(user.getId());
        }

        user1.setId(100);

        // Query
        RealmList<User> results = users.where().equalTo("id", 10).findAll();

        assertEquals(119, results.size());
        assertEquals(10, results.get(0).getId());

    }

}
