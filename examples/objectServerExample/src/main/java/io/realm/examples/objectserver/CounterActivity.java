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
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.examples.objectserver.model.CRDTCounter;
import io.realm.examples.objectserver.model.CounterOperation;
import io.realm.objectserver.Error;
import io.realm.objectserver.ErrorHandler;
import io.realm.objectserver.SyncConfiguration;
import io.realm.objectserver.User;
import io.realm.objectserver.util.UserStore;

public class CounterActivity extends AppCompatActivity {

    private Realm realm;
    private UserStore userStore = MyApplication.USER_STORE;
    private RealmResults<CounterOperation> counter;

    @BindView(R.id.text_counter) TextView counterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        ButterKnife.bind(this);

        // Check if we have a valid user, otherwise redirect to login
        User user = userStore.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (userStore.getCurrentUser() != null) {
            // Create a RealmConfiguration for our user
            SyncConfiguration config = new SyncConfiguration.Builder(this)
                    .initialData(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.createObject(CRDTCounter.class, 1);
                        }
                    })
                    .user(userStore.getCurrentUser())
                    .serverUrl("realm://192.168.1.3/~/default")
                    .build();

            // This will automatically sync all changes in the background for as long as the Realm is open
            realm = Realm.getInstance(config);

            // FIXME Looks like PrimaryKey and lists are not working correctly yet
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
        if (counter != null) {
            counter.removeChangeListeners();
            realm.close(); // Remember to close Realm when done.
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
}
