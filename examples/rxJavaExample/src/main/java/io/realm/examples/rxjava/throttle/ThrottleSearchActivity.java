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
 */

package io.realm.examples.rxjava.throttle;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.rxjava.R;
import io.realm.examples.rxjava.model.Person;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class ThrottleSearchActivity extends Activity {

    private Realm realm;
    private Subscription subscription;
    private EditText searchInputView;
    private ViewGroup searchResultsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_throttlesearch);
        searchInputView = (EditText) findViewById(R.id.search);
        searchResultsView = (ViewGroup) findViewById(R.id.search_results);
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Listen to key presses and only start search after user paused to avoid excessive redrawing on the screen.
        subscription = RxTextView.textChangeEvents(searchInputView)
                .debounce(200, TimeUnit.MILLISECONDS) // default Scheduler is Schedulers.computation()
                .observeOn(AndroidSchedulers.mainThread()) // Needed to access Realm data
                .flatMap(new Func1<TextViewTextChangeEvent, Observable<RealmResults<Person>>>() {
                    @Override
                    public Observable<RealmResults<Person>> call(TextViewTextChangeEvent event) {
                        // Use Async API to move Realm queries off the main thread.
                        // Realm currently doesn't support the standard Schedulers.
                        return realm.where(Person.class)
                                .beginsWith("name", event.text().toString())
                                .findAllSortedAsync("name").asObservable();
                    }
                })
                .filter(new Func1<RealmResults<Person>, Boolean>() {
                    @Override
                    public Boolean call(RealmResults<Person> persons) {
                        // Only continue once data is actually loaded
                        // RealmObservables will emit the unloaded (empty) list as it's first item
                        return persons.isLoaded();
                    }
                })
                .subscribe(new Action1<RealmResults<Person>>() {
                    @Override
                    public void call(RealmResults<Person> persons) {
                        searchResultsView.removeAllViews();
                        for (Person person : persons) {
                            TextView view = new TextView(ThrottleSearchActivity.this);
                            view.setText(person.getName());
                            searchResultsView.addView(view);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
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
