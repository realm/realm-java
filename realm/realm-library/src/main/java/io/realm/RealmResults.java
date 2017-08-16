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

import java.util.Date;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.realm.exceptions.RealmException;
import io.realm.internal.CheckedRow;
import io.realm.internal.Collection;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.SortDescriptor;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
import io.realm.internal.Util;
import io.realm.internal.android.JsonUtils;
import rx.Observable;


/**
 * This class holds all the matches of a {@link RealmQuery} for a given Realm. The objects are not copied from
 * the Realm to the RealmResults list, but are just referenced from the RealmResult instead. This saves memory and
 * increases speed.
 * <p>
 * RealmResults are live views, which means that if it is on an {@link Looper} thread, it will automatically
 * update its query results after a transaction has been committed. If on a non-looper thread,
 * {@link Realm#waitForChange()} must be called to update the results.
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
public class RealmResults<E extends RealmModel> extends OrderedRealmCollectionImpl<E> {

    // Called from Realm Proxy classes
    @SuppressLint("unused")
    static <T extends RealmModel> RealmResults<T> createBacklinkResults(BaseRealm realm, Row row, Class<T> srcTableType, String srcFieldName) {
        UncheckedRow uncheckedRow = (UncheckedRow) row;
        Table srcTable = realm.getSchema().getTable(srcTableType);
        return new RealmResults<>(
                realm,
                Collection.createBacklinksCollection(realm.sharedRealm, uncheckedRow, srcTable, srcFieldName),
                srcTableType);
    }

    // Abandon typing information, all ye who enter here
    static RealmResults<DynamicRealmObject> createDynamicBacklinkResults(DynamicRealm realm, CheckedRow row, Table srcTable, String srcFieldName) {
        final String srcClassName = Table.getClassNameForTable(srcTable.getName());
        //noinspection ConstantConditions
        return new RealmResults<>(
                realm,
                Collection.createBacklinksCollection(realm.sharedRealm, row, srcTable, srcFieldName),
                srcClassName);
    }

    RealmResults(BaseRealm realm, Collection collection, Class<E> clazz) {
        super(realm, collection, clazz);
    }

    RealmResults(BaseRealm realm, Collection collection, String className) {
        super(realm, collection, className);
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
        return collection.isLoaded();
    }

    /**
     * Makes an asynchronous query blocking. This will also trigger any registered {@link RealmChangeListener} when
     * the query completes.
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    @Override
    public boolean load() {
        // The Collection doesn't have to be loaded before accessing it if the query has not returned.
        // Instead, accessing the Collection will just trigger the execution of query if needed. We add this flag is
        // only to keep the original behavior of those APIs. eg.: For a async RealmResults, before query returns, the
        // size() call should return 0 instead of running the query get the real size.
        realm.checkIfValid();
        collection.load();
        return true;
    }

    /**
     * Sets the value to {@code null} for the given field in all of the objects in the result collection.
     *
     * @param fieldName name of the field to update.
     * @throws IllegalArgumentException if field name doesn't exist or isn't nullable.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    public void setNull(@Nonnull String fieldName) {
        verifyString(fieldName);
        realm.checkIfValid();
        collection.setNull(getColumnIndex(fieldName));
    }


    /**
     * Sets the {@code boolean} value of the given field in all of the objects in the result collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't boolean.
     */
    public void setBoolean(@Nonnull String fieldName, @Nullable Boolean value) {
        verifyString(fieldName);

        realm.checkIfValid();

        if (checkAndSetNullValue(fieldName, value)) { return; }

        collection.setBoolean(getColumnIndex(fieldName), value);
    }

    /**
     * Sets the {@code byte} value of the given field in all of the objects in the result collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist or isn't integer.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    public void setByte(@Nonnull String fieldName, @Nullable Byte value) {
        verifyString(fieldName);

        realm.checkIfValid();

        if (checkAndSetNullValue(fieldName, value)) { return; }

        collection.setInt(getColumnIndex(fieldName), value);
    }

    /**
     * Sets the {@code short} value of the given field in all of the objects in the result collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist or isn't integer.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    public void setShort(@Nonnull String fieldName, @Nullable Short value) {
        verifyString(fieldName);

        realm.checkIfValid();

        if (checkAndSetNullValue(fieldName, value)) { return; }

        collection.setInt(getColumnIndex(fieldName), value);
    }

    /**
     * Sets the {@code int} value of the given field in all of the objects in the result collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't integer.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    public void setInt(@Nonnull String fieldName, @Nullable Integer value) {
        verifyString(fieldName);

        realm.checkIfValid();

        if (checkAndSetNullValue(fieldName, value)) { return; }

        collection.setInt(getColumnIndex(fieldName), value);
    }

    /**
     * Sets the {@code long} value of the given field in all of the objects in the result collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't integer.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    public void setLong(@Nonnull String fieldName, @Nullable Long value) {
        verifyString(fieldName);

        realm.checkIfValid();

        if (checkAndSetNullValue(fieldName, value)) { return; }

        collection.setInt(getColumnIndex(fieldName), value);
    }

    /**
     * Sets the {@code float} value of the given field in all of the objects in the result collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't float.
     */
    public void setFloat(@Nonnull String fieldName, @Nullable Float value) {
        verifyString(fieldName);

        realm.checkIfValid();

        if (checkAndSetNullValue(fieldName, value)) { return; }

        collection.setFloat(getColumnIndex(fieldName), value);
    }

    /**
     * Sets the {@code double} value of the given field in all of the objects in the result collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't double.
     */
    public void setDouble(@Nonnull String fieldName, @Nullable Double value) {
        verifyString(fieldName);

        realm.checkIfValid();

        if (checkAndSetNullValue(fieldName, value)) { return; }

        collection.setDouble(getColumnIndex(fieldName), value);
    }

    /**
     * Sets the {@code String} value of the given field in all of the objects in the result collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't string.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    public void setString(@Nonnull String fieldName, @Nullable String value) {
        verifyString(fieldName);

        realm.checkIfValid();

        if (checkAndSetNullValue(fieldName, value)) { return; }

        collection.setString(getColumnIndex(fieldName), value);
    }

    /**
     * Sets the binary value of the given field in all of the objects in the result collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't binary.
     */
    public void setBlob(@Nonnull String fieldName, @Nullable byte[] value) {
        verifyString(fieldName);

        realm.checkIfValid();

        if (checkAndSetNullValue(fieldName, value)) { return; }

        collection.setBinary(getColumnIndex(fieldName), value);
    }

    /**
     * Sets the {@code Date} value of the given field in all of the objects in the result collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't date.
     */
    public void setDate(@Nonnull String fieldName, @Nullable Date value) {
        verifyString(fieldName);

        realm.checkIfValid();

        if (checkAndSetNullValue(fieldName, value)) { return; }

        collection.setDate(getColumnIndex(fieldName), value.getTime());
    }

    /**
     * Sets a reference to another object on the given field in all of the objects in the result collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist or is of the wrong type.
     */
    public void setObject(@Nonnull String fieldName, @Nullable RealmObject value) {
        verifyString(fieldName);

        realm.checkIfValid();

        if (checkAndSetNullValue(fieldName, value)) { return; }

        long columnIndex = getColumnIndex(fieldName);

        if (!(value instanceof RealmObjectProxy)) {
            throw new IllegalArgumentException("A field value must be a managed Realm object.");
        }

        ProxyState proxyState = ((RealmObjectProxy) value).realmGet$proxyState();
        if (proxyState.getRealm$realm() != realm) {
            throw new IllegalArgumentException("A field value must belong to the same Realm as its owner.");
        }

        Row row = proxyState.getRow$realm();
        if (row == null) {
            throw new IllegalArgumentException("A field value must be a managed Realm object.");
        }
        if (!(row instanceof UncheckedRow)) {
            throw new IllegalArgumentException("WTF?");
        }

        collection.setObject(columnIndex, (UncheckedRow) row);
    }

    /**
     * Sets the value for the given field  in all of the objects in the result collection, converting {@code String}
     * representations of numbers and booleans to their appropriate type. For example {@code "10"}
     * will be converted to {@code 10} if the field type is {@code int}.
     * <p>
     * Using the typed setters is faster than using this method.
     *
     * @throws IllegalArgumentException if field name doesn't exist or if the input value cannot be converted
     * to the appropriate input type.
     * @throws NumberFormatException if a String based number cannot be converted properly.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    @SuppressWarnings("unchecked")
    public void set(@Nonnull String fieldName, @Nullable String strValue) {
        if (checkAndSetNullValue(fieldName, strValue)) { return; }

        RealmFieldType type = null;
        switch (type) {
            case BOOLEAN:
                setBoolean(fieldName, Boolean.parseBoolean(strValue));
                return;
            case INTEGER:
                setLong(fieldName, Long.parseLong(strValue));
                return;
            case FLOAT:
                setFloat(fieldName, Float.parseFloat(strValue));
                return;
            case DOUBLE:
                setDouble(fieldName, Double.parseDouble(strValue));
                return;
            case DATE:
                setDate(fieldName, JsonUtils.stringToDate(strValue));
                return;
            case STRING:
                setString(fieldName, strValue);
                return;
            default:
        }

        throw new IllegalArgumentException(String.format(
                Locale.US,
                "Field %s is not a String field and the value, %s, could not be automatically converted. Use a typed setter instead.",
                fieldName,
                strValue));
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
        checkForAddRemoveListener(listener, true);
        collection.addListener(this, listener);
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
        checkForAddRemoveListener(listener, true);
        collection.addListener(this, listener);
    }

    private void checkForAddRemoveListener(@Nullable Object listener, boolean checkListener) {
        if (checkListener && listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        realm.checkIfValid();
        realm.sharedRealm.capabilities.checkCanDeliverNotification(BaseRealm.LISTENER_NOT_ALLOWED_MESSAGE);
    }

    /**
     * Removes all user-defined change listeners.
     *
     * @throws IllegalStateException if you try to remove listeners from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeAllChangeListeners() {
        checkForAddRemoveListener(null, false);
        collection.removeAllListeners();
    }

    /**
     * Use {@link #removeAllChangeListeners()} instead.
     */
    @SuppressWarnings("unused")
    @Deprecated
    public void removeChangeListeners() {
        removeAllChangeListeners();
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
        checkForAddRemoveListener(listener, true);
        collection.removeListener(this, listener);
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
        checkForAddRemoveListener(listener, true);
        collection.removeListener(this, listener);
    }

    /**
     * Returns an Rx Observable that monitors changes to this RealmResults. It will emit the current RealmResults when
     * subscribed to. RealmResults will continually be emitted as the RealmResults are updated -
     * {@code onComplete} will never be called.
     * <p>
     * If you would like the {@code asObservable()} to stop emitting items you can instruct RxJava to
     * only emit only the first item by using the {@code first()} operator:
     * <p>
     * <pre>
     * {@code
     * realm.where(Foo.class).findAllAsync().asObservable()
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
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete}
     * or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath or the
     * corresponding Realm instance doesn't support RxJava.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    @SuppressWarnings("unchecked")
    public Observable<RealmResults<E>> asObservable() {
        if (realm instanceof Realm) {
            return realm.configuration.getRxFactory().from((Realm) realm, this);
        }

        if (realm instanceof DynamicRealm) {
            DynamicRealm dynamicRealm = (DynamicRealm) realm;
            RealmResults<DynamicRealmObject> dynamicResults = (RealmResults<DynamicRealmObject>) this;
            @SuppressWarnings("UnnecessaryLocalVariable")
            Observable results = realm.configuration.getRxFactory().from(dynamicRealm, dynamicResults);
            return results;
        }

        throw new UnsupportedOperationException(realm.getClass() + " does not support RxJava.");
    }

    /**
     * @deprecated use {@link RealmQuery#distinct(String)} on the return value of {@link #where()} instead. This will
     * be removed in coming 3.x.x minor releases.
     */
    @Deprecated
    public RealmResults<E> distinct(String fieldName) {
        SortDescriptor distinctDescriptor = SortDescriptor.getInstanceForDistinct(
                new SchemaConnector(realm.getSchema()), collection.getTable(), fieldName);
        Collection distinctCollection = collection.distinct(distinctDescriptor);
        return createLoadedResults(distinctCollection);
    }

    /**
     * @deprecated use {@link RealmQuery#distinctAsync(String)} on the return value of {@link #where()} instead. This
     * will be removed in coming 3.x.x minor releases.
     */
    @Deprecated
    public RealmResults<E> distinctAsync(String fieldName) {
        return where().distinctAsync(fieldName);
    }

    /**
     * @deprecated use {@link RealmQuery#distinct(String, String...)} on the return value of {@link #where()} instead.
     * This will be removed in coming 3.x.x minor releases.
     */
    @Deprecated
    public RealmResults<E> distinct(String firstFieldName, String... remainingFieldNames) {
        return where().distinct(firstFieldName, remainingFieldNames);
    }

    private boolean checkAndSetNullValue(@Nonnull String fieldName, @Nullable Object value) {
        if (value != null) { return false; }

        setNull(fieldName);
        return true;
    }

    private void verifyString(@Nonnull String fieldName) {
        if (Util.isEmptyString(fieldName)) {
            throw new IllegalArgumentException("Field name must not be null");
        }
    }
}
