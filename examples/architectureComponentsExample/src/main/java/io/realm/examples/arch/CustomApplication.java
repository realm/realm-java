/*
 * Copyright 2018 Realm Inc.
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

package io.realm.examples.arch;

import android.app.Application;
import android.support.annotation.NonNull;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.examples.arch.model.Person;


public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .initialData(new Realm.Transaction() {
                    @Override
                    public void execute(@NonNull Realm realm) {
                        Person person = realm.createObject(Person.class);
                        person.name = "Makoto Yamazaki";
                        person.setAge(32);

                        person = realm.createObject(Person.class);
                        person.name = "Christian Melchior";
                        person.setAge(34);

                        person = realm.createObject(Person.class);
                        person.name = "Chen Mulong";
                        person.setAge(29);

                        person = realm.createObject(Person.class);
                        person.name = "Nabil Hachicha";
                        person.setAge(31);
                    }
                })
                .build());
    }
}
