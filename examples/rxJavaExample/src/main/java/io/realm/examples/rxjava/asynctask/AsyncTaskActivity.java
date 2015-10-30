package io.realm.examples.rxjava.asynctask;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import io.realm.examples.rxjava.R;

public class AsyncTaskActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_asynctask);
    }
}
