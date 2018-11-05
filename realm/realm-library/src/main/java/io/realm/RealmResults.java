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
import java.util.Iterator;
import java.util.Locale;

import javax.annotation.Nullable;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.realm.internal.CheckedRow;
import io.realm.internal.ColumnInfo;
import io.realm.internal.OsList;
import io.realm.internal.OsResults;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
import io.realm.internal.Util;
import io.realm.internal.android.JsonUtils;
import io.realm.log.RealmLog;
import io.realm.rx.CollectionChange;

import static io.realm.RealmFieldType.LIST;

/**
 * This class holds all the matches of a {@link RealmQuery} for a given Realm. The objects are not copied from
 * the Realm to the RealmResults list, but are just referenced from the RealmResult instead. This saves memory and
 * increases speed.
 * <p>
 * RealmResults are live views, which means that if it is on an {@link Looper} thread, it will automatically
 * update its query results after a transaction has been committed. If on a non-looper thread,
 * {@link Realm#refresh()} must be called to update the results.
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
     * Updates the field given by {@code fieldName} in all objects inside the query result.
     * <p>
     * This method will automatically try to convert numbers and booleans that are given as
     * {@code String} to their appropriate type. For example {@code "10"} will be converted to
     * {@code 10} if the field type is {@link RealmFieldType#INTEGER}.
     * <p>
     * Using the typed setters like {@link #setInt(String, int)} will be faster than using
     * this method.
     *
     * @param fieldName field to update
     * @param value value to update with.
     * @throws IllegalArgumentException if the field could not be found, could not be updated or
     * the argument didn't match the field type or could not be converted to match the underlying
     * field type.
     */
    public void setValue(String fieldName, @Nullable Object value) {
        checkNonEmptyFieldName(fieldName);
        realm.checkIfValidAndInTransaction();
        fieldName = mapFieldNameToInternalName(fieldName);
        boolean isString = (value instanceof String);
        String strValue = isString ? (String) value : null;

        String className = osResults.getTable().getClassName();
        RealmObjectSchema schema = getRealm().getSchema().get(className);
        if (!schema.hasField(fieldName)) {
            throw new IllegalArgumentException(String.format("Field '%s' could not be found in class '%s'", fieldName, className));
        }

        // null values exit early
        if (value == null) {
            osResults.setNull(fieldName);
            return;
        }

        // Does implicit conversion if needed.
        RealmFieldType type = schema.getFieldType(fieldName);
        if (isString && type != RealmFieldType.STRING) {
            switch (type) {
                case BOOLEAN:
                    value = Boolean.parseBoolean(strValue);
                    break;
                case INTEGER:
                    value = Long.parseLong(strValue);
                    break;
                case FLOAT:
                    value = Float.parseFloat(strValue);
                    break;
                case DOUBLE:
                    value = Double.parseDouble(strValue);
                    break;
                case DATE:
                    value = JsonUtils.stringToDate(strValue);
                    break;
                default:
                    throw new IllegalArgumentException(String.format(Locale.US,
                            "Field %s is not a String field, " +
                                    "and the provide value could not be automatically converted: %s. Use a typed" +
                                    "setter instead", fieldName, value));
            }
        }

        //noinspection ConstantConditions
        Class<?> valueClass = value.getClass();
        if (valueClass == Boolean.class) {
            setBoolean(fieldName, (Boolean) value);
        } else if (valueClass == Short.class) {
            setShort(fieldName, (Short) value);
        } else if (valueClass == Integer.class) {
            setInt(fieldName, (Integer) value);
        } else if (valueClass == Long.class) {
            setLong(fieldName, (Long) value);
        } else if (valueClass == Byte.class) {
            setByte(fieldName, (Byte) value);
        } else if (valueClass == Float.class) {
            setFloat(fieldName, (Float) value);
        } else if (valueClass == Double.class) {
            setDouble(fieldName, (Double) value);
        } else if (valueClass == String.class) {
            //noinspection ConstantConditions
            setString(fieldName, (String) value);
        } else if (value instanceof Date) {
            setDate(fieldName, (Date) value);
        } else if (value instanceof byte[]) {
            setBlob(fieldName, (byte[]) value);
        } else if (value instanceof RealmModel) {
            setObject(fieldName, (RealmModel) value);
        } else if (valueClass == RealmList.class) {
            RealmList<?> list = (RealmList<?>) value;
            setList(fieldName, list);
        } else {
            throw new IllegalArgumentException("Value is of a type not supported: " + value.getClass());
        }
    }

    /**
     * Sets the value to {@code null} for the given field in all of the objects in the collection.
     *
     * @param fieldName name of the field to update.
     * @throws IllegalArgumentException if field name doesn't exist or is a primary key property.
     * @throws IllegalStateException if the field cannot hold {@code null} values.
     */
    public void setNull(String fieldName) {
        checkNonEmptyFieldName(fieldName);
        realm.checkIfValidAndInTransaction();
        osResults.setNull(fieldName);
    }

    /**
     * Sets the {@code boolean} value of the given field in all of the objects in the collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist, is a primary key property or isn't a boolean field.
     */
    public void setBoolean(String fieldName, boolean value) {
        checkNonEmptyFieldName(fieldName);
        realm.checkIfValidAndInTransaction();
        fieldName = mapFieldNameToInternalName(fieldName);
        checkType(fieldName, RealmFieldType.BOOLEAN);
        osResults.setBoolean(fieldName, value);
    }

    /**
     * Sets the {@code byte} value of the given field in all of the objects in the collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist, is a primary key property or isn't a byte field.
     */
    public void setByte(String fieldName, byte value) {
        checkNonEmptyFieldName(fieldName);
        realm.checkIfValidAndInTransaction();
        fieldName = mapFieldNameToInternalName(fieldName);
        checkType(fieldName, RealmFieldType.INTEGER);
        osResults.setInt(fieldName, value);
    }

    /**
     * Sets the {@code short} value of the given field in all of the objects in the collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist, is a primary key property or isn't a short field.
     */
    public void setShort(String fieldName, short value) {
        checkNonEmptyFieldName(fieldName);
        realm.checkIfValidAndInTransaction();
        fieldName = mapFieldNameToInternalName(fieldName);
        checkType(fieldName, RealmFieldType.INTEGER);
        osResults.setInt(fieldName, value);
    }

    /**
     * Sets the {@code int} value of the given field in all of the objects in the collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist, is a primary key property or isn't an integer field.
     */
    public void setInt(String fieldName, int value) {
        checkNonEmptyFieldName(fieldName);
        fieldName = mapFieldNameToInternalName(fieldName);
        checkType(fieldName, RealmFieldType.INTEGER);
        realm.checkIfValidAndInTransaction();
        osResults.setInt(fieldName, value);
    }

    /**
     * Sets the {@code long} value of the given field in all of the objects in the collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist, is a primary key property or isn't a long field.
     */
    public void setLong(String fieldName, long value) {
        checkNonEmptyFieldName(fieldName);
        realm.checkIfValidAndInTransaction();
        fieldName = mapFieldNameToInternalName(fieldName);
        checkType(fieldName, RealmFieldType.INTEGER);
        osResults.setInt(fieldName, value);
    }

    /**
     * Sets the {@code float} value of the given field in all of the objects in the collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist, is a primary key property or isn't a float field.
     */
    public void setFloat(String fieldName, float value) {
        checkNonEmptyFieldName(fieldName);
        realm.checkIfValidAndInTransaction();
        fieldName = mapFieldNameToInternalName(fieldName);
        checkType(fieldName, RealmFieldType.FLOAT);
        osResults.setFloat(fieldName, value);
    }

    /**
     * Sets the {@code double} value of the given field in all of the objects in the collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist, is a primary key property or isn't a double field.
     */
    public void setDouble(String fieldName, double value) {
        checkNonEmptyFieldName(fieldName);
        realm.checkIfValidAndInTransaction();
        fieldName = mapFieldNameToInternalName(fieldName);
        checkType(fieldName, RealmFieldType.DOUBLE);
        osResults.setDouble(fieldName, value);
    }

    /**
     * Sets the {@code String} value of the given field in all of the objects in the collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist, is a primary key property or isn't a String field.
     */
    public void setString(String fieldName, @Nullable String value) {
        checkNonEmptyFieldName(fieldName);
        realm.checkIfValidAndInTransaction();
        fieldName = mapFieldNameToInternalName(fieldName);
        checkType(fieldName, RealmFieldType.STRING);
        osResults.setString(fieldName, value);
    }

    /**
     * Sets the binary value of the given field in all of the objects in the collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist, is a primary key property or isn't a binary field.
     */
    public void setBlob(String fieldName, @Nullable byte[] value) {
        checkNonEmptyFieldName(fieldName);
        realm.checkIfValidAndInTransaction();
        fieldName = mapFieldNameToInternalName(fieldName);
        checkType(fieldName, RealmFieldType.BINARY);
        osResults.setBlob(fieldName, value);
    }

    /**
     * Sets the {@code Date} value of the given field in all of the objects in the collection.
     *
     * @param fieldName name of the field to update.
     * @param value new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist, is a primary key property or isn't a date field.
     */
    public void setDate(String fieldName, @Nullable Date value) {
        checkNonEmptyFieldName(fieldName);
        realm.checkIfValidAndInTransaction();
        fieldName = mapFieldNameToInternalName(fieldName);
        checkType(fieldName, RealmFieldType.DATE);
        osResults.setDate(fieldName, value);
    }

    /**
     * Sets a reference to another object on the given field in all of the objects in the collection.
     *
     * @param fieldName name of the field to update.
     * @param value new object referenced by this field.
     * @throws IllegalArgumentException if field name doesn't exist, is a primary key property or isn't an Object reference field.
     */
    public void setObject(String fieldName, @Nullable RealmModel value) {
        checkNonEmptyFieldName(fieldName);
        realm.checkIfValidAndInTransaction();
        fieldName = mapFieldNameToInternalName(fieldName);
        checkType(fieldName, RealmFieldType.OBJECT);
        Row row = checkRealmObjectConstraints(fieldName, value);
        osResults.setObject(fieldName, row);
    }

    private Row checkRealmObjectConstraints(String fieldName, @Nullable RealmModel value) {
        if (value != null) {
            if (!(RealmObject.isManaged(value) && RealmObject.isValid(value))) {
                throw new IllegalArgumentException("'value' is not a valid, managed Realm object.");
            }
            ProxyState proxyState = ((RealmObjectProxy) value).realmGet$proxyState();
            if (!proxyState.getRealm$realm().getPath().equals(realm.getPath())) {
                throw new IllegalArgumentException("'value' does not belong to the same Realm as the RealmResults.");
            }

            // Check that type matches the expected one
            Table currentTable = osResults.getTable();
            long columnIndex = currentTable.getColumnIndex(fieldName);
            Table expectedTable = currentTable.getLinkTarget(columnIndex);
            Table inputTable = proxyState.getRow$realm().getTable();
            if (!expectedTable.hasSameSchema(inputTable)) {
                throw new IllegalArgumentException(String.format(Locale.US,
                        "Type of object is wrong. Was '%s', expected '%s'",
                        inputTable.getClassName(), expectedTable.getClassName()));
            }
            return proxyState.getRow$realm();
        }

        return null;
    }

    /**
     * Replaces the RealmList at the given field on all objects in this collection.
     *
     *
     * @param fieldName name of the field to update.
     * @param list new value for the field.
     * @throws IllegalArgumentException if field name doesn't exist, isn't a RealmList field , if the
     * objects in the list are not managed or the type of the objects in the list are wrong.
     */
    @SuppressWarnings("unchecked")
    public <T> void setList(String fieldName, RealmList<T> list) {
        checkNonEmptyFieldName(fieldName);
        fieldName = mapFieldNameToInternalName(fieldName);
        realm.checkIfValidAndInTransaction();

        //noinspection ConstantConditions
        if (list == null) {
            throw new IllegalArgumentException("Non-null 'list' required");
        }

        // Due to type erasure of generics it is not possible to have multiple overloaded methods with the same signature.
        // So instead we fake  it by checking the first element in the list and verifies that
        // against the underlying type.
        RealmFieldType columnType = realm.getSchema().getSchemaForClass(osResults.getTable().getClassName()).getFieldType(fieldName);
        switch (columnType) {
            case LIST:
                checkTypeOfListElements(list, RealmModel.class);
                checkRealmObjectConstraints(fieldName, (RealmModel) list.first(null));
                osResults.setModelList(fieldName, (RealmList<? extends RealmModel>) list);
                break;
            case INTEGER_LIST:
                // Integers are a bit annoying as they are all stored as the same type in Core
                // but the Java type system cannot seamlessly translate between e.g Short and Long.
                Class<?> listType = getListType(list);
                if (listType.equals(Integer.class)) {
                    osResults.setIntegerList(fieldName, (RealmList<Integer>) list);
                } else if (listType.equals(Long.class)) {
                    osResults.setLongList(fieldName, (RealmList<Long>) list);
                } else if (listType.equals(Short.class)) {
                    osResults.setShortList(fieldName, (RealmList<Short>) list);
                } else if (listType.equals(Byte.class)) {
                    osResults.setByteList(fieldName, (RealmList<Byte>) list);
                } else {
                    throw new IllegalArgumentException(String.format("List contained the wrong type of elements. " +
                            "Elements that can be mapped to Integers was expected, but the actual type is '%s'",
                            listType));
                }
                break;
            case BOOLEAN_LIST:
                checkTypeOfListElements(list, Boolean.class);
                osResults.setBooleanList(fieldName, (RealmList<Boolean>) list);
                break;
            case STRING_LIST:
                checkTypeOfListElements(list, String.class);
                osResults.setStringList(fieldName, (RealmList<String>) list);
                break;
            case BINARY_LIST:
                checkTypeOfListElements(list, byte[].class);
                osResults.setByteArrayList(fieldName, (RealmList<byte[]>) list);
                break;
            case DATE_LIST:
                checkTypeOfListElements(list, Date.class);
                osResults.setDateList(fieldName, (RealmList<Date>) list);
                break;
            case FLOAT_LIST:
                checkTypeOfListElements(list, Float.class);
                osResults.setFloatList(fieldName, (RealmList<Float>) list);
                break;
            case DOUBLE_LIST:
                checkTypeOfListElements(list, Double.class);
                osResults.setDoubleList(fieldName, (RealmList<Double>) list);
                break;
            default:
                throw new IllegalArgumentException(String.format("Field '%s' is not a list but a %s", fieldName, columnType));
        }
    }

    private Class<?> getListType(RealmList list) {
        if (!list.isEmpty()) {
            return list.first().getClass();
        } else {
            return Long.class; // Any valid type that maps to INTEGER will do.
        }
    }

    private <T> void checkTypeOfListElements(RealmList<T> list, Class<?> clazz) {
        if (!list.isEmpty()) {
            T element = list.first();
            Class<?> elementType = element.getClass();
            if (!(clazz.isAssignableFrom(elementType))) {
                throw new IllegalArgumentException(String.format("List contained the wrong type of elements. Elements of type '%s' was " +
                        "expected, but the actual type is '%s'", clazz, elementType));
            }
        }
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
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete}
     * or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath or the
     * corresponding Realm instance doesn't support RxJava.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    @SuppressWarnings("unchecked")
    public Flowable<RealmResults<E>> asFlowable() {
        if (realm instanceof Realm) {
            return realm.configuration.getRxFactory().from((Realm) realm, this);
        }

        if (realm instanceof DynamicRealm) {
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

    private void checkNonEmptyFieldName(String fieldName) {
        if (Util.isEmptyString(fieldName)) {
            throw new IllegalArgumentException("Non-empty 'fieldname' required.");
        }
    }

    private void checkNotNull(@Nullable Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Non-null 'value' required. Use 'setNull(fieldName)' instead.");
        }
    }

    private void checkType(String fieldName, RealmFieldType expectedFieldType) {
        String className = osResults.getTable().getClassName();
        RealmFieldType fieldType = realm.getSchema().get(className).getFieldType(fieldName);
        if (fieldType != expectedFieldType) {
            throw new IllegalArgumentException(String.format("The field '%s.%s' is not of the expected type. " +
                    "Actual: %s, Expected: %s", className, fieldName, fieldType, expectedFieldType));
        }
    }

    private String mapFieldNameToInternalName(String fieldName) {
        if (realm instanceof Realm) {
            // We only need to map field names from typed Realms.
            String className = osResults.getTable().getClassName();
            String mappedFieldName = realm.getSchema().getColumnInfo(className).getInternalFieldName(fieldName);
            if (mappedFieldName == null) {
                throw new IllegalArgumentException(String.format("Field '%s' does not exists.", fieldName));
            } else {
                fieldName = mappedFieldName;
            }
        }
        return fieldName;
    }
}
