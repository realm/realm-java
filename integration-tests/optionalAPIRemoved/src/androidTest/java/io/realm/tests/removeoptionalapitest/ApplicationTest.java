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
package io.realm.tests.removeoptionalapitest;

import android.app.Application;
import android.test.ApplicationTestCase;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import rx.Observable;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    private Realm realm;
    private DynamicRealm dynamicRealm;
    private RealmConfiguration realmConfiguration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        realmConfiguration = new RealmConfiguration.Builder(this.getContext()).build();

        realm = Realm.getInstance(realmConfiguration);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(Dog.class);
            }
        });
        // Open the dynamic Realm after transaction.
        dynamicRealm = DynamicRealm.getInstance(realmConfiguration);
    }

    @Override
    protected void tearDown() throws Exception {
        realm.close();
        dynamicRealm.close();
        Realm.deleteRealm(realmConfiguration);
        super.tearDown();
    }

    public void testRealmAsObservableRemoved() {
        try {
            @SuppressWarnings("unused")
            Observable<Realm> observable = realm.asObservable();
            fail();
        } catch (NoSuchMethodError ignored) {
        }
    }

    public void testRealmObjectAsObservableRemoved() {
        Dog dog = realm.where(Dog.class).findFirst();

        try {
            @SuppressWarnings("unused")
            Observable<Dog> observable = dog.asObservable();
            fail();
        } catch (NoSuchMethodError ignored) {
        }
    }

    public void testDynamicRealmAsObservableRemoved() {
        try {
            @SuppressWarnings("unused")
            Observable<DynamicRealm> observable = dynamicRealm.asObservable();
            fail();
        } catch (NoSuchMethodError ignored) {
        }
    }

    public void testDynamicRealmObjectAsObservableRemoved() {
        DynamicRealmObject dog = dynamicRealm.where("Dog").findFirst();

        try {
            @SuppressWarnings("unused")
            Observable<DynamicRealmObject> observable = dog.asObservable();
            fail();
        } catch (NoSuchMethodError ignored) {
        }
    }

    public void testRealmResultsAsObservableRemoved() {
        RealmResults<Dog> results = realm.where(Dog.class).findAll();

        try {
            @SuppressWarnings("unused")
            Observable<RealmResults<Dog>> observable = results.asObservable();
            fail();
        } catch (NoSuchMethodError ignored) {
        }
    }
}
