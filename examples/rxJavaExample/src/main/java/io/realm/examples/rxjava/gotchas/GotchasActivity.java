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

package io.realm.examples.rxjava.gotchas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.Sort;
import io.realm.examples.rxjava.R;
import io.realm.examples.rxjava.model.Person;

/**
 * This class shows some of the current obstacles when combining RxJava and Realm. 2 things are
 * important to keep in mind.
 *
 * 1) Thread confinement: Realm objects are thread confined, so trying to access them from another thread will throw
 *    an exception.
 *
 * 2) Realm objects are live objects. This means that the same object will alter it's state automatically over time to
 *    automatically reflect the latest state in Realm.
 *
 * Both of these characteristics doesn't play well with RxJava's threading model which favor immutable thread-safe
 * objects.
 *
 * Work is in progress to make it easier to work around these constraints. See
 * - https://github.com/realm/realm-java/issues/1208
 * - https://github.com/realm/realm-java/issues/931
 */
public class GotchasActivity extends AppCompatActivity {
    private Realm realm;
    private Disposable disposable;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gotchas);
        container = findViewById(R.id.list);
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Disposable distinctSubscription = testDistinct();
        Disposable bufferSubscription = testBuffer();
        Disposable subscribeOnSubscription = testSubscribeOn();

        // Trigger updates
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Person.class).findAllSorted( "name", Sort.ASCENDING).get(0).setAge(new Random().nextInt(100));
            }
        });

        disposable = new CompositeDisposable(
                distinctSubscription,
                bufferSubscription,
                subscribeOnSubscription
        );
    }

    /**
     * Shows how to be careful with `subscribeOn()`
     */
    private Disposable testSubscribeOn() {
        Disposable subscribeOn = realm.asFlowable()
                .map(new Function<Realm, Person>() {
                    @Override
                    public Person apply(Realm realm) throws Exception {
                        return realm.where(Person.class).findAllSorted("name").get(0);
                    }
                })
                // The Realm was created on the UI thread. Accessing it on `Schedulers.io()` will crash.
                // Avoid using subscribeOn() and use Realms `findAllAsync*()` methods instead.
                .subscribeOn(Schedulers.io()) //
                .subscribe(new Consumer<Person>() {
                    @Override
                    public void accept(Person person) throws Exception {
                        // Do nothing
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        showStatus("subscribeOn: " + throwable.toString());
                    }
                });

        // Use Realms Async API instead
        Disposable asyncSubscribeOn = realm.where(Person.class).findAllSortedAsync("name").get(0).<Person>asFlowable()
                .subscribe(new Consumer<Person>() {
                    @Override
                    public void accept(Person person) throws Exception {
                        showStatus("subscribeOn/async: " + person.getName() + ":" + person.getAge());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        showStatus("subscribeOn/async: " +throwable.toString());
                    }
                });

        return new CompositeDisposable(subscribeOn, asyncSubscribeOn);
    }

    /**
     * Shows how to be careful with `buffer()`
     */
    private Disposable testBuffer() {
        Flowable<Person> personFlowable = realm.asFlowable().map(new Function<Realm, Person>() {
            @Override
            public Person apply(Realm realm) throws Exception {
                return realm.where(Person.class).findAllSorted("name").get(0);
            }
        });

        // buffer() caches objects until the buffer is full. Due to Realms auto-update of all objects it means
        // that all objects in the cache will contain the same data.
        // Either avoid using buffer or copy data into an unmanaged object.
        return personFlowable
                .buffer(2)
                .subscribe(new Consumer<List<Person>>() {
                    @Override
                    public void accept(List<Person> people) throws Exception {
                        showStatus("Buffer[0] : " + people.get(0).getName() + ":" + people.get(0).getAge());
                        showStatus("Buffer[1] : " + people.get(1).getName() + ":" + people.get(1).getAge());
                    }
                });
    }

    /**
     * Shows how to to be careful when using `distinct()`
     */
    private Disposable testDistinct() {
        Flowable<Person> personFlowable = realm.asFlowable().map(new Function<Realm, Person>() {
            @Override
            public Person apply(Realm realm) throws Exception {
                return realm.where(Person.class).findAllSorted("name").get(0);
            }
        });

        new Function<Realm, Person>() {
            @Override
            public Person apply(Realm realm) throws Exception {
                return realm.where(Person.class).findAllSorted("name").get(0);
            }
        };

        // distinct() and distinctUntilChanged() uses standard equals with older objects stored in a HashMap.
        // Realm objects auto-update which means the objects stored will also auto-update.
        // This makes comparing against older objects impossible (even if the new object has changed) because the
        // cached object will also have changed.
        // Use a keySelector function to work around this.
        Disposable distinctItemTest = personFlowable
                .distinct() // Because old == new. This will only allow the first version of the "Chris" object to pass.
                .subscribe(new Consumer<Person>() {
                    @Override
                    public void accept(Person person) throws Exception {
                        showStatus("distinct(): " + person.getName() + ":" + person.getAge());
                    }
                });

        Disposable distinctKeySelectorItemTest = personFlowable
                .distinct(new Function<Person, Object>() {
                    @Override
                    public Object apply(Person person) throws Exception {
                        return person.getAge();
                    }
                })
                .subscribe(new Consumer<Person>() {
                    @Override
                    public void accept(Person person) throws Exception {
                        showStatus("distinct(keySelector): " + person.getName() + ":" + person.getAge());
                    }
                });

        return new CompositeDisposable(distinctItemTest, distinctKeySelectorItemTest);
    }

    private void showStatus(String message) {
        TextView v = new TextView(this);
        v.setText(message);
        container.addView(v);
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
