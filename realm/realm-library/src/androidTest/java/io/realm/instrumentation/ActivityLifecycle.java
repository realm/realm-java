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

package io.realm.instrumentation;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.entities.AllTypes;

public class ActivityLifecycle implements Lifecycle, RealmChangeListener<RealmResults<AllTypes>> {
    private final RealmConfiguration realmConfiguration;
    private Realm realm;
    private RealmResults<AllTypes> mAllTypes;

    public ActivityLifecycle (RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
    }

    @Override
    public void onStart() {
        realm = Realm.getInstance(realmConfiguration);
        mAllTypes = realm.where(AllTypes.class).findAllAsync();
        mAllTypes.addChangeListener(this);
    }

    @Override
    public void onStop() {
        mAllTypes.removeChangeListener(this);
        realm.close();
    }

    @Override
    public void onChange(RealmResults<AllTypes> object) {
    }
}
