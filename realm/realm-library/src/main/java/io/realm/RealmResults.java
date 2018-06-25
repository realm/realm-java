/*
 * Copyright 2014 Realm Inc.
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

package io.realm;


import android.annotation.SuppressLint;
import android.os.Looper;

import javax.annotation.Nullable;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.realm.internal.CheckedRow;
import io.realm.internal.OsResults;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
import io.realm.log.RealmLog;
import io.realm.rx.CollectionChange;

/**
 * This class holds all the matches of a {@link RealmQuery} for a given Realm. The objects are not copied from
 * the Realm to the RealmResults list, but are just referenced from the RealmResult instead. This saves memory and
 * increases speed.
 * <p>
 * RealmResults are live views, which means that if it is on an {@link Looper} thread, it will automatically
 * update its query results after a transaction has been committed. If on a non-looper thread, {@link Realm#waitForChange()}
 * must be called to update the results.
 * <p>
 * Updates to RealmObjects from a RealmResults list must be done from within a transaction and the modified objects are
 * persisted to the Realm file during the commit of the transaction.
 * <p>
 * A RealmResults object cannot be passed between different threads.
 * <p>
 * Notice that a RealmResults is never {@code null} not even in the case where it contains no objects. You should always
 * use the {@link RealmResults#size()} method to check if a RealmResults is empty or not.
 * <p>
 * If a RealmResults is built on RealmList through {@link RealmList#where()}, it will become empty when the source
 * RealmList gets deleted.
 * <p>
 * {@link RealmResults} can contain more elements than {@code Integer.MAX_VALUE}.
 * In that case, you can access only first {@code Integer.MAX_VALUE} elements in it.
 *
 * @param <E> The class of objects in this list.
 * @see RealmQuery#findAll()
 * @see Realm#executeTransaction(Realm.Transaction)
 */
public class RealmResults<E> extends OrderedRealmCollectionImpl<E> {

    // Called from Realm Proxy classes
    @SuppressLint("unused")
    static <T extends RealmModel> RealmResults<T> createBacklinkResults(BaseRealm realm, Row row, Class<T> srcTableType, String srcFieldName) {
        UncheckedRow uncheckedRow = (UncheckedRow) row;
        Table srcTable = realm.getSchema().getTable(srcTableType);
        return new RealmResults<>(
                realm,
                OsResults.createForBacklinks(realm.sharedRealm, uncheckedRow, srcTable, srcFieldName),
                srcTableType);
    }

    // Abandon typing information, all ye who enter here
    static RealmResults<DynamicRealmObject> createDynamicBacklinkResults(DynamicRealm realm, CheckedRow row, Table srcTable, String srcFieldName) {
        final String srcClassName = Table.getClassNameForTable(srcTable.getName());
        //noinspection ConstantConditions
        return new RealmResults<>(
                realm,
                OsResults.createForBacklinks(realm.sharedRealm, row, srcTable, srcFieldName),
                srcClassName);
    }

    RealmResults(BaseRealm realm, OsResults osResults, Class<E> clazz) {
        super(realm, osResults, clazz);
    }

    RealmResults(BaseRealm realm, OsResults osResults, String className) {
        super(realm, osResults, className);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmQuery<E> where() {
        realm.checkIfValid();
        return RealmQuery.createQueryFromResult(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmResults<E> sort(String fieldName1, Sort sortOrder1, String fieldName2, Sort sortOrder2) {
        return sort(new String[] {fieldName1, fieldName2}, new Sort[] {sortOrder1, sortOrder2});
    }

    /**
     * Returns {@code false} if the results are not yet loaded, {@code true} if they are loaded.
     *
     * @return {@code true} if the query has completed and the data is available, {@code false} if the query is still
     * running in the background.
     */
    @Override
    public boolean isLoaded() {
        realm.checkIfValid();
        return osResults.isLoaded();
    }

    /**
     * Makes an asynchronous query blocking. This will also trigger any registered {@link RealmChangeListener} when
     * the query completes.
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    @Override
    public boolean load() {
        // The OsResults doesn't have to be loaded before accessing it if the query has not returned.
        // Instead, accessing the OsResults will just trigger the execution of query if needed. We add this flag is
        // only to keep the original behavior of those APIs. eg.: For a async RealmResults, before query returns, the
        // size() call should return 0 instead of running the query get the real size.
        realm.checkIfValid();
        osResults.load();
        return true;
    }

    /**
     * Adds a change listener to this {@link RealmResults}.
     * <p>
     * Registering a change listener will not prevent the underlying RealmResults from being garbage collected.
     * If the RealmResults is garbage collected, the change listener will stop being triggered. To avoid this, keep a
     * strong reference for as long as appropriate e.g. in a class variable.
     * <p>
     * <pre>
     * {@code
     * public class MyActivity extends Activity {
     *
     *     private RealmResults<Person> results; // Strong reference to keep listeners alive
     *
     *     \@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *       super.onCreate(savedInstanceState);
     *       results = realm.where(Person.class).findAllAsync();
     *       results.addChangeListener(new RealmChangeListener<RealmResults<Person>>() {
     *           \@Override
     *           public void onChange(RealmResults<Person> persons) {
     *               // React to change
     *           }
     *       });
     *     }
     * }
     * }
     * </pre>
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or
     * {@link android.app.IntentService} thread.
     */
    public void addChangeListener(RealmChangeListener<RealmResults<E>> listener) {
        checkForAddListener(listener);
        osResults.addListener(this, listener);
    }

    /**
     * Adds a change listener to this {@link RealmResults}.
     * <p>
     * Registering a change listener will not prevent the underlying RealmResults from being garbage collected.
     * If the RealmResults is garbage collected, the change listener will stop being triggered. To avoid this, keep a
     * strong reference for as long as appropriate e.g. in a class variable.
     * <p>
     * <pre>
     * {@code
     * public class MyActivity extends Activity {
     *
     *     private RealmResults<Person> results; // Strong reference to keep listeners alive
     *
     *     \@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *       super.onCreate(savedInstanceState);
     *       results = realm.where(Person.class).findAllAsync();
     *       results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Person>>() {
     *           \@Override
     *           public void onChange(RealmResults<Person> persons, OrderedCollectionChangeSet changeSet) {
     *               // React to change
     *           }
     *       });
     *     }
     * }
     * }
     * </pre>
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or
     * {@link android.app.IntentService} thread.
     */
    public void addChangeListener(OrderedRealmCollectionChangeListener<RealmResults<E>> listener) {
        checkForAddListener(listener);
        osResults.addListener(this, listener);
    }

    private void checkForAddListener(@Nullable Object listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        realm.checkIfValid();
        realm.sharedRealm.capabilities.checkCanDeliverNotification(BaseRealm.LISTENER_NOT_ALLOWED_MESSAGE);
    }

    private void checkForRemoveListener(@Nullable Object listener, boolean checkListener) {
        if (checkListener && listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }

        if (realm.isClosed()) {
            RealmLog.warn("Calling removeChangeListener on a closed Realm %s, " +
                    "make sure to close all listeners before closing the Realm.", realm.configuration.getPath());
        }
    }

    /**
     * Removes all user-defined change listeners.
     *
     * @throws IllegalStateException if you try to remove listeners from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeAllChangeListeners() {
        checkForRemoveListener(null, false);
        osResults.removeAllListeners();
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeChangeListener(RealmChangeListener<RealmResults<E>> listener) {
        checkForRemoveListener(listener, true);
        osResults.removeListener(this, listener);
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeChangeListener(OrderedRealmCollectionChangeListener<RealmResults<E>> listener) {
        checkForRemoveListener(listener, true);
        osResults.removeListener(this, listener);
    }

    /**
     * Returns an Rx Flowable that monitors changes to this RealmResults. It will emit the current RealmResults when
     * subscribed to. RealmResults will continually be emitted as the RealmResults are updated -
     * {@code onComplete} will never be called.
     * <p>
     * If you would like the {@code asFlowable()} to stop emitting items you can instruct RxJava to
     * only emit only the first item by using the {@code first()} operator:
     * <p>
     * <pre>
     * {@code
     * realm.where(Foo.class).findAllAsync().asFlowable()
     *      .filter(results -> results.isLoaded())
     *      .first()
     *      .subscribe( ... ) // You only get the results once
     * }
     * </pre>
     * <p>
     * <p>Note that when the {@link Realm} is accessed from threads other than where it was created,
     * {@link IllegalStateException} will be thrown. Care should be taken when using different schedulers
     * with {@code subscribeOn()} and {@code observeOn()}. Consider using {@code Realm.where().find*Async()}
     * instead.
     *
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath or the
     * corresponding Realm instance doesn't support RxJava.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    @SuppressWarnings("unchecked")
    public Flowable<RealmResults<E>> asFlowable() {
        if (realm instanceof Realm) {
            return realm.configuration.getRxFactory().from((Realm) realm, this);
        } else if (realm instanceof DynamicRealm) {
            DynamicRealm dynamicRealm = (DynamicRealm) realm;
            RealmResults<DynamicRealmObject> dynamicResults = (RealmResults<DynamicRealmObject>) this;
            @SuppressWarnings("UnnecessaryLocalVariable")
            Flowable results = realm.configuration.getRxFactory().from(dynamicRealm, dynamicResults);
            return results;
        } else {
            throw new UnsupportedOperationException(realm.getClass() + " does not support RxJava2.");
        }
    }

    /**
     * Returns an Rx Observable that monitors changes to this RealmResults. It will emit the current RealmResults when
     * subscribed. For each update to the RealmResult a pair consisting of the RealmResults and the
     * {@link OrderedCollectionChangeSet} will be sent. The changeset will be {@code null} the first
     * time an RealmResults is emitted.
     * <p>
     * RealmResults will continually be emitted as the RealmResults are updated - {@code onComplete} will never be called.
     * <p>Note that when the {@link Realm} is accessed from threads other than where it was created,
     * {@link IllegalStateException} will be thrown. Care should be taken when using different schedulers
     * with {@code subscribeOn()} and {@code observeOn()}. Consider using {@code Realm.where().find*Async()}
     * instead.
     *
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath or the
     * corresponding Realm instance doesn't support RxJava.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    public Observable<CollectionChange<RealmResults<E>>> asChangesetObservable() {
        if (realm instanceof Realm) {
            return realm.configuration.getRxFactory().changesetsFrom((Realm) realm, this);
        } else if (realm instanceof DynamicRealm) {
            DynamicRealm dynamicRealm = (DynamicRealm) realm;
            RealmResults<DynamicRealmObject> dynamicResults = (RealmResults<DynamicRealmObject>) this;
            return (Observable) realm.configuration.getRxFactory().changesetsFrom(dynamicRealm, dynamicResults);
        } else {
            throw new UnsupportedOperationException(realm.getClass() + " does not support RxJava2.");
        }
    }
}
