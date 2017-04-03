/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.examples.objectserver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.SyncConfiguration;
import io.realm.SyncUser;
import io.realm.examples.objectserver.model.CRDTCounter;

public class CounterActivity extends AppCompatActivity {

    private static final String REALM_URL = "realm://" + BuildConfig.OBJECT_SERVER_IP + ":9080/~/default";

    private Realm realm;
    private CRDTCounter counter;
    private SyncUser user;

    @BindView(R.id.text_counter) TextView counterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        ButterKnife.bind(this);

        // Check if we have a valid user, otherwise redirect to login
        if (SyncUser.currentUser() == null) {
            gotoLoginActivity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = SyncUser.currentUser();
        if (user != null) {
            // Create a RealmConfiguration for our user
            SyncConfiguration config = new SyncConfiguration.Builder(user, REALM_URL)
                    .initialData(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.createObject(CRDTCounter.class, 1);
                        }
                    })
                    .build();

            // This will automatically sync all changes in the background for as long as the Realm is open
            realm = Realm.getInstance(config);

            counter = realm.where(CRDTCounter.class).findFirstAsync();
            counter.addChangeListener(new RealmChangeListener<CRDTCounter>() {
                @Override
                public void onChange(CRDTCounter counter) {
                    if (counter.isValid()) {
                        counterView.setText(String.format(Locale.US, "%d", counter.getCount()));
                    } else {
                        counterView.setText("-");
                    }
                }
            });
            counterView.setText("0");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeRealm();
        user = null;
    }

    private void closeRealm() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_counter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_logout:
                closeRealm();
                user.logout();
                gotoLoginActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.upper)
    public void incrementCounter() {
        adjustCounter(1);
    }

    @OnClick(R.id.lower)
    public void decrementCounter() {
        adjustCounter(-1);
    }

    private void adjustCounter(final int adjustment) {
        // A synchronized Realm can get written to at any point in time, so doing synchronous writes on the UI
        // thread is HIGHLY discouraged as it might block longer than intended. Only use async transactions.
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                CRDTCounter counter = realm.where(CRDTCounter.class).findFirst();
                counter.add(adjustment);
            }
        });
    }

    private void gotoLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
