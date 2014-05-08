package io.realm.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;


import io.realm.example.entities.User;
import io.realm.testApp.R;
import io.realm.typed.RealmList;
import io.realm.typed.Realms;

public class MainActivity extends Activity {

    private RealmList<User> users;

    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize  the Realm
        this.users = Realms.newList(this, User.class);


        // Setup the ListView
        ListView listView = (ListView)findViewById(R.id.listView);
        adapter = new UserAdapter(this, users);
        listView.setAdapter(adapter);


    }

    public void createItem(View v) {
        User user = this.users.create();
        user.setId(0);
        user.setName("Username " + this.users.size());
        user.setEmail("");
        this.users.add(user);
        adapter.notifyDataSetChanged();
    }





}
