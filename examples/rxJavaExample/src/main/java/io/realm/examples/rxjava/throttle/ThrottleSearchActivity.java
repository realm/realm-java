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

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.rxjava.R;
import io.realm.examples.rxjava.model.Person;

public class ThrottleSearchActivity extends Activity {

    private Realm realm;
    private Disposable disposable;
    private EditText searchInputView;
    private ViewGroup searchResultsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_throttlesearch);
        searchInputView = findViewById(R.id.search);
        searchResultsView = findViewById(R.id.search_results);
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Listen to key presses and only start search after user paused to avoid excessive redrawing on the screen.
        disposable = RxTextView.textChangeEvents(searchInputView)
                .debounce(200, TimeUnit.MILLISECONDS) // default Scheduler is Schedulers.computation()
                .observeOn(AndroidSchedulers.mainThread()) // Needed to access Realm data
                .toFlowable(    BackpressureStrategy.BUFFER)
                .flatMap(new Function<TextViewTextChangeEvent, Publisher<RealmResults<Person>>>() {

                    public Publisher<RealmResults<Person>> apply(TextViewTextChangeEvent textViewTextChangeEvent) throws Exception {
                        // Use Async API to move Realm queries off the main thread.
                        // Realm currently doesn't support the standard Schedulers.
                        return realm.where(Person.class)
                                .beginsWith("name", textViewTextChangeEvent.text().toString())
                                .findAllSortedAsync("name")
                                .asFlowable();
                    }
                })
                .filter(new Predicate<RealmResults<Person>>() {
                    @Override
                    public boolean test(RealmResults<Person> people) throws Exception {
                        // Only continue once data is actually loaded
                        // RealmObservables will emit the unloaded (empty) list as its first item
                        return people.isLoaded();
                    }
                })
                .subscribe(new Consumer<RealmResults<Person>>() {
                    @Override
                    public void accept(RealmResults<Person> people) throws Exception {
                        searchResultsView.removeAllViews();
                        for (Person person : people) {
                            TextView view = new TextView(ThrottleSearchActivity.this);
                            view.setText(person.getName());
                            searchResultsView.addView(view);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        disposable.dispose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
