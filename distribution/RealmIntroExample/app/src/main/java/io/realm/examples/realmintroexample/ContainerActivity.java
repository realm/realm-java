package io.realm.examples.realmintroexample;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.realmintroexample.model.Cat;
import io.realm.examples.realmintroexample.model.Dog;
import io.realm.examples.realmintroexample.model.Person;


public class ContainerActivity extends Activity {

    public static final String TAG = ContainerActivity.class.getName();

    public static final String FRAGMENT_NAME_EXTRA = "fragmentExtra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        Class clazz = (Class)getIntent().getSerializableExtra(FRAGMENT_NAME_EXTRA);
        Fragment newFragment = null;
        try {
            newFragment = (Fragment)clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
