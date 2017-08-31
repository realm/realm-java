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

package io.realm.examples.rxjava.animation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.TextView;

import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.Flowable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.examples.rxjava.R;
import io.realm.examples.rxjava.model.Person;

public class AnimationActivity extends AppCompatActivity {

    private Realm realm;
    private Disposable disposable;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animations);
        container = findViewById(R.id.list);
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Load all persons and start inserting them with 1 sec. intervals.
        // All RealmObject access has to be done on the same thread `findAllAsync` was called on.
        // Warning: This example doesn't handle back pressure well.
        disposable = realm.where(Person.class).findAllAsync().asFlowable()
                .flatMap(new Function<RealmResults<Person>, Publisher<Person>>() {
                    @Override
                    public Publisher<Person> apply(RealmResults<Person> persons) throws Exception {
                        return Flowable.fromIterable(persons);
                    }
                })
                .zipWith(Flowable.interval(1, TimeUnit.SECONDS), new BiFunction<Person, Long, Person>() {

                    @Override
                    public Person apply(Person person, Long tick) throws Exception {
                        return person;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Person>() {
                    @Override
                    public void accept(Person person) throws Exception {
                        TextView personView = new TextView(AnimationActivity.this);
                        personView.setText(person.getName());
                        container.addView(personView);
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
