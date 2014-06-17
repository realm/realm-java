package io.realm.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.List;

import io.realm.example.entities.User;
import io.realm.testApp.R;
import io.realm.typed.Realm;
import io.realm.typed.RealmChangeListener;

public class MainActivity extends Activity {

    private List<User> users;
    private ArrayAdapter<User> adapter;
    private Realm realm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the Realm
        if(realm == null) {
            realm = new Realm(this);
        } else {
            System.out.println("Already inited");
        }

      //  realm.clear();


        users = new ArrayList<User>();


        // Setup the ListView
        ListView listView = (ListView)findViewById(R.id.listView);
        this.adapter = new ArrayAdapter<User>(this, R.layout.list_item, this.users);
        listView.setAdapter(this.adapter);

        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                realm.refresh();
                users = realm.where(User.class).findAll();
                adapter.notifyDataSetChanged();
                System.out.println("Updated list");
            }
        });


    }

    public void createItem(View v) {

        Realm wrRealm = new Realm(this);
        wrRealm.beginWrite();

        User user = new User();

        user.setId(0);
        user.setName("Username " + this.users.size());
        user.setEmail("");

        wrRealm.add(user);
        wrRealm.commit();


    }





}
