package io.realm.typed;

import android.test.AndroidTestCase;

import java.util.List;

import io.realm.typed.entities.User;


public class RealmTest extends AndroidTestCase {

    public void testRealm() {

        // Init
        Realm<User> users = Realms.newList(this.getContext(), User.class);
        // Notice that Realm implements List, which means that it can be used in a lot of existing code


        // Insert
        for(int i = 0; i < 120; i++) {
            User user = new User();
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

        // Query
        Realm<User> results = users.where().equalTo("id", 33).find();
        assertEquals(1, results.size());
        assertEquals(33, results.get(0).getId());

    }

}
