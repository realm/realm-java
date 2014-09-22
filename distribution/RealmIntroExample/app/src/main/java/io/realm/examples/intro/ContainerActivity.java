package io.realm.examples.intro;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class ContainerActivity extends Activity {

    public static final String TAG = ContainerActivity.class.getName();

    public static final String FRAGMENT_NAME_EXTRA = "fragmentExtra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        Class clazz = (Class) getIntent().getSerializableExtra(FRAGMENT_NAME_EXTRA);
        Fragment newFragment = null;
        try {
            newFragment = (Fragment) clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.commit();
    }
}
