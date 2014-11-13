package io.realm.performancetest;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.squareup.otto.Bus;

import javax.inject.Inject;

public class BaseActivity extends FragmentActivity {

    protected @Inject Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.inject(this);
        bus.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }
}
