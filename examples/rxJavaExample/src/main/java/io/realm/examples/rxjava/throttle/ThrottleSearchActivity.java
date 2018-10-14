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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.realm.Realm;
import io.realm.examples.rxjava.R;
import io.realm.examples.rxjava.model.Person;

public class ThrottleSearchActivity extends AppCompatActivity {

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
                .toFlowable(BackpressureStrategy.BUFFER)
                .switchMap(textChangeEvent -> {
                    // Use Async API to move Realm queries off the main thread.
                    // Realm currently doesn't support the standard Schedulers.
                    return realm.where(Person.class)
                            .beginsWith("name", textChangeEvent.text().toString())
                            .sort("name")
                            .findAllAsync()
                            .asFlowable();
                })
                // Only continue once data is actually loaded
                // RealmObservables will emit the unloaded (empty) list as its first item
                .filter(people -> people.isLoaded())
                .subscribe(people -> {
                    searchResultsView.removeAllViews();
                    for (Person person : people) {
                        TextView view = new TextView(ThrottleSearchActivity.this);
                        view.setText(person.getName());
                        searchResultsView.addView(view);
                    }
                }, throwable -> throwable.printStackTrace());
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
