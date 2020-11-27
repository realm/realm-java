/*
 * Copyright 2020 Realm Inc.
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

package io.realm.coroutines;

import javax.annotation.Nonnull;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.annotations.Beta;
import io.realm.internal.coroutines.InternalFlowFactory;
import io.realm.rx.CollectionChange;
import io.realm.rx.ObjectChange;
import kotlinx.coroutines.flow.Flow;

/**
 * Factory class used to create coroutine {@link Flow}s.
 * <p>
 * This class is used by default unless overridden in {@link io.realm.RealmConfiguration.Builder#flowFactory(FlowFactory)}.
 */
@Beta
public class RealmFlowFactory implements FlowFactory {

    private final InternalFlowFactory factory;

    /**
     * Constructor for the Flow factory.
     *
     * @param returnFrozenObjects whether the emissions should return frozen objects or not.
     */
    public RealmFlowFactory(Boolean returnFrozenObjects) {
        this.factory = new InternalFlowFactory(returnFrozenObjects);
    }

    @Override
    public Flow<Realm> from(@Nonnull Realm realm) {
        return factory.from(realm);
    }

    @Override
    public Flow<DynamicRealm> from(@Nonnull DynamicRealm dynamicRealm) {
        return factory.from(dynamicRealm);
    }

    @Override
    public <T> Flow<RealmResults<T>> from(@Nonnull Realm realm, @Nonnull RealmResults<T> results) {
        return factory.from(realm, results);
    }

    @Override
    public <T> Flow<CollectionChange<RealmResults<T>>> changesetFrom(@Nonnull Realm realm, @Nonnull RealmResults<T> results) {
        return factory.changesetFrom(realm, results);
    }

    @Override
    public <T> Flow<RealmResults<T>> from(@Nonnull DynamicRealm dynamicRealm, @Nonnull RealmResults<T> results) {
        return factory.from(dynamicRealm, results);
    }

    @Override
    public <T> Flow<CollectionChange<RealmResults<T>>> changesetFrom(@Nonnull DynamicRealm dynamicRealm, @Nonnull RealmResults<T> results) {
        return factory.changesetFrom(dynamicRealm, results);
    }

    @Override
    public <T> Flow<RealmList<T>> from(@Nonnull Realm realm, @Nonnull RealmList<T> realmList) {
        return factory.from(realm, realmList);
    }

    @Override
    public <T> Flow<CollectionChange<RealmList<T>>> changesetFrom(@Nonnull Realm realm, @Nonnull RealmList<T> list) {
        return factory.changesetFrom(realm, list);
    }

    @Override
    public <T> Flow<RealmList<T>> from(@Nonnull DynamicRealm dynamicRealm, @Nonnull RealmList<T> realmList) {
        return factory.from(dynamicRealm, realmList);
    }

    @Override
    public <T> Flow<CollectionChange<RealmList<T>>> changesetFrom(@Nonnull DynamicRealm dynamicRealm, @Nonnull RealmList<T> list) {
        return factory.changesetFrom(dynamicRealm, list);
    }

    @Override
    public <T extends RealmModel> Flow<T> from(@Nonnull Realm realm, @Nonnull T realmObject) {
        return factory.from(realm, realmObject);
    }

    @Override
    public <T extends RealmModel> Flow<ObjectChange<T>> changesetFrom(@Nonnull Realm realm, @Nonnull T realmObject) {
        return factory.changesetFrom(realm, realmObject);
    }

    @Override
    public Flow<DynamicRealmObject> from(@Nonnull DynamicRealm dynamicRealm, @Nonnull DynamicRealmObject dynamicRealmObject) {
        return factory.from(dynamicRealm, dynamicRealmObject);
    }

    @Override
    public Flow<ObjectChange<DynamicRealmObject>> changesetFrom(@Nonnull DynamicRealm dynamicRealm, @Nonnull DynamicRealmObject dynamicRealmObject) {
        return factory.changesetFrom(dynamicRealm, dynamicRealmObject);
    }
}
