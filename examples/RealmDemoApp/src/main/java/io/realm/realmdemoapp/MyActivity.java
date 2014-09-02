package io.realm.realmdemoapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.internal.SharedGroup;
import io.realm.realmdemoapp.entities.User;

public class MyActivity extends Activity {

    private List<User> users;
    private ArrayAdapter<User> adapter;
    private Realm realm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // Default Realm - no file name given but persist to disk
        try {
            realm = new Realm(this.getFilesDir());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove all objects and classes
        realm.clear();

        users = new ArrayList<User>();

        // Setup the ListView
        ListView listView = (ListView)findViewById(R.id.listView);
        this.adapter = new ArrayAdapter<User>(this, R.layout.list_item, this.users);
        listView.setAdapter(this.adapter);

        // Listen for changes to the Realm
        // The method onChange will be called when realm is
        // changed: objects are added, updated or removed.
        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                realm.refresh(); // automate the realm
                users = realm.where(User.class).findAll();
                adapter.notifyDataSetChanged();
                System.out.println("Updated list");
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // Listeners can be closed
        realm.removeAllChangeListeners();
    }

    Realm openRealm() {
        Realm wrRealm = null;
        try {
            wrRealm = new Realm(this.getFilesDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wrRealm;
    }

    Realm openRealmInMemory() {
        // In-memory Realm; no objects will be persisted to disk
        try {
            Realm.setDefaultDurability(SharedGroup.Durability.MEM_ONLY);
            realm = new Realm(this.getFilesDir());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return realm;
    }

    Realm openRealm(String filename) {
        // Initialize a particular Realm
        try {
            Realm.setDefaultDurability(SharedGroup.Durability.FULL);
            realm = new Realm(this.getFilesDir(), "demo.realm");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return realm;
    }

    public void createItem(View v) {

        Realm wrRealm = openRealm();

        // Begin a write transaction, create a new object,
        // set its properties, and add the object.
        // The commit method will persistent the object to disk.
        wrRealm.beginWrite();
        User user = new User();
        user.setId(this.users.size());
        user.setAge(15);
        user.setName("Username " + this.users.size());
        user.setEmail(null);
        wrRealm.add(user);
        wrRealm.commit();
    }

    public void createUserInRealm() {
        Realm wrRealm = openRealm();

        // Create an object directly in a Realm
        // and set its properties
        wrRealm.beginWrite();
        User user;
        user = wrRealm.create(User.class);
        user.setId(2);
        user.setName("Sarah Blue");
        user.setAge(34);
        user.setEmail(null);
        wrRealm.commit();
    }

    public void addUserToRealm() {
        Realm wrRealm = openRealm();

        // Create a user as a standalone object
        // and set its properties
        User user = new User();
        user.setId(1);
        user.setName("James Brown");
        user.setAge(10);
        System.out.println("User no. "+user.getId()+": "+user.getName());

        // Add two email addresses
/*        Email email1 = new Email();
        email1.setAddress("jb@example.com");
        user.getEmail().add(email1);

        Email email2 = new Email();
        email2.setAddress("jimmy@example.com");
        user.getEmail().add(email2);
*/
        // Add the object to the Realm
        wrRealm.beginWrite();
        wrRealm.add(user);
        wrRealm.commit();
    }

    public void queryRealm() {
        Realm wrRealm = openRealm();

        // Find all users with a name beginning with U
        // Print the names of the first and last user who fulfill that criteria
        RealmList<User> users = realm.where(User.class).beginsWith("name", "U").findAll();
        if (users.size() > 0) {
            User firstUser = users.first();
            User lastUser = users.last();
            System.out.println("First: "+firstUser.getName());
            System.out.println("Last:  "+lastUser.getName());
            System.out.println("Average: "+users.averageOfProperty("age"));
            System.out.println("Sum    : "+users.sumOfProperty("age"));
            System.out.println("Minimum: "+users.minOfProperty("age"));
            System.out.println("Maximum: "+users.maxOfProperty("age"));
        }
    }

    public void queryAndRemoveAll() {
        // FIXME: this realm could be read-only
        Realm wrRealm = openRealm();

        // Find user with id > 7 and remove them
        wrRealm.beginWrite();
        RealmList<User> users = realm.where(User.class).greaterThan("id", 7).findAll();
        users.clear();
        wrRealm.commit();
    }

    public void removeEvenObjects() {
        Realm wrRealm = openRealm();

        // Remove even numbered objects
        wrRealm.beginWrite();
        RealmList<User> users = wrRealm.allObjects(User.class);
        for (int i = 0; i < users.size(); i++) {
            if (i % 2 == 0) {
                users.remove(i);
            }
        }
        wrRealm.commit();
    }

    public void removeOddObjects() {
        Realm wrRealm = openRealm();

        wrRealm.beginWrite();
        RealmList<User> users = wrRealm.allObjects(User.class);
        for (int i = 0; i < users.size(); i++) {
            if (i % 2 == 0) {
                realm.remove(User.class, i);
            }
        }
    }

    public void removeLast() {
        Realm wrRealm = openRealm();

        wrRealm.beginWrite();
        RealmList<User> users = wrRealm.allObjects(User.class);
        users.removeLastObject();
        wrRealm.commit();
    }

    public void updateOddObjects() {
        Realm wrRealm = openRealm();

        // Update odd numbered objects
        wrRealm.beginWrite();
        RealmList<User> users = wrRealm.allObjects(User.class);
        for (int i=0; i<users.size(); i++) {
            if (i%2 == 1) {
                users.get(i).setId(2*i);
            }
        }
        wrRealm.commit();
    }

    public void updateAnObjects() {
        Realm wrRealm = openRealm();

        RealmList<User> users = wrRealm.allObjects(User.class);
        wrRealm.beginWrite();
        User user = new User();
        user.setId(2);
        user.setAge(42);
        user.setName("Susan");
        users.replaceObjectAtIndexWithObject(users.size() / 2, user);
        wrRealm.commit();
    }

    public void insertInMiddle() {
        Realm wrRealm = openRealm();

        RealmList<User> users = wrRealm.allObjects(User.class);
        User user = new User();
        user.setId(2);
        user.setAge(42);
        user.setName("Susan");

        wrRealm.beginWrite();
        users.insertObjectAtIndex(user, users.size()/2);
        wrRealm.commit();
    }

}
