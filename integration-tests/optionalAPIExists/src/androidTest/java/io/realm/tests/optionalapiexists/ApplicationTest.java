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
package io.realm.tests.optionalapiexists;

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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this.getContext()).build();
        realm = Realm.getInstance(realmConfiguration);
        dynamicRealm = DynamicRealm.getInstance(realmConfiguration);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(Dog.class);
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        realm.close();
        dynamicRealm.close();
        super.tearDown();
    }

    public void testRealmAsObservableRemoved() {
        @SuppressWarnings("unused")
        Observable<Realm> observable = realm.asObservable();
    }

    public void testRealmObjectAsObservableRemoved() {
        Dog dog = realm.where(Dog.class).findFirst();
        @SuppressWarnings("unused")
        Observable<Dog> observable = dog.asObservable();
    }

    public void testDynamicRealmAsObservableRemoved() {
        @SuppressWarnings("unused")
        Observable<DynamicRealm> observable = dynamicRealm.asObservable();
    }

    public void testDynamicRealmObjectAsObservableRemoved() {
        DynamicRealmObject dog = dynamicRealm.where("Dog").findFirst();
        @SuppressWarnings("unused")
        Observable<DynamicRealmObject> observable = dog.asObservable();
    }

    public void testRealmResultsAsObservableRemoved() {
        RealmResults<Dog> results = realm.where(Dog.class).findAll();
        @SuppressWarnings("unused")
        Observable<RealmResults<Dog>> observable = results.asObservable();
    }
}