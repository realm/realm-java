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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.examples.objectserver.model.CRDTCounter;
import io.realm.examples.objectserver.model.CounterOperation;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.User;

public class CounterActivity extends AppCompatActivity {

    private static final String REALM_URL = "realm://" + MyApplication.OBJECT_SERVER_IP + ":7800/~/default";

    private Realm realm;
    private RealmResults<CounterOperation> counter;
    private User user;


    @BindView(R.id.text_counter) TextView counterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        ButterKnife.bind(this);

        // Check if we have a valid user, otherwise redirect to login
        if (User.currentUser() == null) {
            gotoLoginActivity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = User.currentUser();
        if (user != null) {
            // Create a RealmConfiguration for our user
            SyncConfiguration config = new SyncConfiguration.Builder(user, REALM_URL)
                    .initialData(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            // Workaround for initialData right now https://github.com/realm/realm-java-private/issues/164
                            if (realm.isEmpty()) {
                                realm.createObject(CRDTCounter.class, 1);
                            }
                        }
                    })
                    .build();

            // This will automatically sync all changes in the background for as long as the Realm is open
            realm = Realm.getInstance(config);

            // FIXME Looks like PrimaryKey and lists are not working correctly yet
            // FIXME Also need support for the `setDefault` instruction for this to make sense.
//            counter = realm.where(CRDTCounter.class).findFirstAsync();
//            counter.addChangeListener(new RealmChangeListener<CRDTCounter>() {
//                @Override
//                public void onChange(CRDTCounter counter) {
//                    if (counter.isValid()) {
//                        counterView.setText(String.format(Locale.US, "%d", counter.getCount()));
//                    } else {
//                        counterView.setText("-");
//                    }
//                }
//            });

            counter = realm.where(CounterOperation.class).findAllAsync();
            counter.addChangeListener(new RealmChangeListener<RealmResults<CounterOperation>>() {
                @Override
                public void onChange(RealmResults<CounterOperation> result) {
                    // FIXME Why isn't this triggered when the DB is opened?
                    Number sum = result.sum("adjustment");
                    if (sum != null) {
                        counterView.setText(Long.toString(sum.longValue()));
                    } else {
                        counterView.setText("0");
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
        adjustCounter(new CounterOperation(1));
    }

    @OnClick(R.id.lower)
    public void decrementCounter() {
        adjustCounter(new CounterOperation(-1));
    }

    private void adjustCounter(final CounterOperation ops) {
        // A synchronized Realm can get written to at any point in time, so doing synchronous writes on the UI
        // thread is HIGHLY discouraged as it might block longer than intended. Only use async transactions.
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(ops);
            }
        });
    }

    private void gotoLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
