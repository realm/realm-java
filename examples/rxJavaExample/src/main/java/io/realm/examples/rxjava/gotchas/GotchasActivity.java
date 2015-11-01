/*
 * Copyright 2015 Realm Inc.
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
 *
 *
 */

package io.realm.examples.rxjava.gotchas;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.examples.rxjava.R;
import io.realm.examples.rxjava.common.model.Person;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class GotchasActivity extends Activity {


    private Realm realm;
    private Subscription subscription;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animations);
        container = (ViewGroup) findViewById(R.id.list);
        container.removeAllViews();
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final RealmResults<Person> results = realm.where(Person.class).findAllSorted("name");
        // distinct() and distinctUntilChanged() uses standard equals, with older objects stored in a HashMap.
        // Realm objects auto-update which means the objects stored will also auto-update.
        // This makes comparing against older objects impossible (even if the new object has changed) because the
        // cached object will also have changed.
        // `freeze()` will solve this, as we then also compare against a certain point in time.
        // Until then, use specific keySelector function instead.
        Subscription distinctItemTest = results.get(0).observable()
                .distinct()
                .subscribe(new Action1<Person>() {
                    @Override
                    public void call(Person p) {
                        showStatus("This is called once: " + p.getName());
                    }
                });

        Subscription distinctKeySelectorItemTest = results.get(0).observable()
                .distinct(new Func1<Person, String>() {
                    @Override
                    public String call(Person p) {
                        return p.getName();
                    }
                })
                .subscribe(new Action1<Person>() {
                    @Override
                    public void call(Person p) {
                        showStatus("This is called twice: " + p.getName());
                    }
                });


        // Trigger updates
        // FIXME: Changelisteners doesn't seem to trigger correctly.
        // Investigate https://github.com/realm/realm-java/issues/1676 further
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.beginTransaction();
                realm.allObjectsSorted(Person.class, "name", true).get(0).setName("Foo" + new Random().nextInt(100));
                realm.commitTransaction();
            }
        }, null);

        // Objects are auto-updatable (not immutable)
        // TODO Test:
        // ReplaySubject
        // buffer
        // cache()

        subscription = new CompositeSubscription(distinctItemTest, distinctKeySelectorItemTest);
    }

    private void showStatus(String message) {
        TextView v = new TextView(this);
        v.setText(message);
        container.addView(v);
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscription.unsubscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }



}
