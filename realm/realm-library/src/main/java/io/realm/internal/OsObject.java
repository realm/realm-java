/*
 * Copyright 2017 Realm Inc.
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

package io.realm.internal;

import javax.annotation.Nullable;

import io.realm.ObjectChangeSet;
import io.realm.RealmFieldType;
import io.realm.RealmModel;
import io.realm.RealmObjectChangeListener;
import io.realm.exceptions.RealmException;


/**
 * Java wrapper for Object Store's {@code Object} class.
 */
@Keep
public class OsObject implements NativeObject {

    private static final String OBJECT_ID_COLUMN_NAME = nativeGetObjectIdColumName();

    private static class OsObjectChangeSet implements ObjectChangeSet {
        final String[] changedFields;
        final boolean deleted;

        OsObjectChangeSet(String[] changedFields, boolean deleted) {
            this.changedFields = changedFields;
            this.deleted = deleted;
        }

        @Override
        public boolean isDeleted() {
            return deleted;
        }

        @Override
        public String[] getChangedFields() {
            return changedFields;
        }

        @Override
        public boolean isFieldChanged(String fieldName) {
            for (String name : changedFields) {
                if (name.equals(fieldName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class ObjectObserverPair<T extends RealmModel>
            extends ObserverPairList.ObserverPair<T, RealmObjectChangeListener<T>> {
        public ObjectObserverPair(T observer, RealmObjectChangeListener<T> listener) {
            super(observer, listener);
        }

        public void onChange(T observer, @Nullable ObjectChangeSet changeSet) {
            listener.onChange(observer, changeSet);
        }
    }

    private static class Callback implements ObserverPairList.Callback<ObjectObserverPair> {
        private final String[] changedFields;

        Callback(String[] changedFields) {
            this.changedFields = changedFields;
        }

        private ObjectChangeSet createChangeSet() {
            boolean isDeleted = changedFields == null;
            return new OsObjectChangeSet(isDeleted ? new String[0] : changedFields, isDeleted);
        }

        @Override
        public void onCalled(ObjectObserverPair pair, Object observer) {
            //noinspection unchecked
            pair.onChange((RealmModel) observer, createChangeSet());
        }
    }

    private final long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private ObserverPairList<ObjectObserverPair> observerPairs = new ObserverPairList<ObjectObserverPair>();

    public OsObject(OsSharedRealm sharedRealm, UncheckedRow row) {
        nativePtr = nativeCreate(sharedRealm.getNativePtr(), row.getNativePtr());
        sharedRealm.context.addReference(this);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public <T extends RealmModel> void addListener(T observer, RealmObjectChangeListener<T> listener) {
        if (observerPairs.isEmpty()) {
            nativeStartListening(nativePtr);
        }
        ObjectObserverPair<T> pair = new ObjectObserverPair<T>(observer, listener);
        observerPairs.add(pair);
    }

    public <T extends RealmModel> void removeListener(T observer) {
        observerPairs.removeByObserver(observer);
        if (observerPairs.isEmpty()) {
            nativeStopListening(nativePtr);
        }
    }

    public <T extends RealmModel> void removeListener(T observer, RealmObjectChangeListener<T> listener) {
        observerPairs.remove(observer, listener);
        if (observerPairs.isEmpty()) {
            nativeStopListening(nativePtr);
        }
    }

    // Set the ObserverPairList. This is useful for the findAllAsync. When the pendingRow returns the results, the whole
    // listener list has to be moved from ProxyState to here.
    public void setObserverPairs(ObserverPairList<ObjectObserverPair> pairs) {
        if (!observerPairs.isEmpty()) {
            throw new IllegalStateException("'observerPairs' is not empty. Listeners have been added before.");
        }

        observerPairs = pairs;
        if (!pairs.isEmpty()) {
            nativeStartListening(nativePtr);
        }
    }

    // TODO: consider to return a OsObject instead when integrating with Object Store's object accessor.
    /**
     * Create an object in the given table which doesn't have a primary key column defined.
     *
     * @param table the table where the object is created. This table must be atached to {@link OsSharedRealm}.
     * @return a newly created {@code UncheckedRow}.
     */
    public static UncheckedRow create(Table table) {
        final OsSharedRealm sharedRealm = table.getSharedRealm();
        return new UncheckedRow(sharedRealm.context, table,
                nativeCreateNewObject(sharedRealm.getNativePtr(), table.getNativePtr()));
    }

    /**
     * Create a row in the given table which doesn't have a primary key column defined.
     * This is used for the fast bulk insertion.
     *
     * @param table the table where the object is created.
     * @return a newly created row's index.
     */
    public static long createRow(Table table) {
        final OsSharedRealm sharedRealm = table.getSharedRealm();
        return nativeCreateRow(sharedRealm.getNativePtr(), table.getNativePtr());
    }

    private static long getAndVerifyPrimaryKeyColumnIndex(Table table) {
        String pkField = OsObjectStore.getPrimaryKeyForObject(table.getSharedRealm(), table.getClassName());
        if (pkField == null) {
            throw new IllegalStateException(table.getName() + " has no primary key defined.");
        }
        return table.getColumnIndex(pkField);
    }

    // TODO: consider to return a OsObject instead when integrating with Object Store's object accessor.
    /**
     * Create an object in the given table which has a primary key column defined, and set the primary key with given
     * value.
     *
     * @param table the table where the object is created. This table must be atached to {@link OsSharedRealm}.
     * @return a newly created {@code UncheckedRow}.
     */
    public static UncheckedRow createWithPrimaryKey(Table table, @Nullable Object primaryKeyValue) {
        long primaryKeyColumnIndex = getAndVerifyPrimaryKeyColumnIndex(table);
        RealmFieldType type = table.getColumnType(primaryKeyColumnIndex);
        final OsSharedRealm sharedRealm = table.getSharedRealm();

        if (type == RealmFieldType.STRING) {
            if (primaryKeyValue != null && !(primaryKeyValue instanceof String)) {
                throw new IllegalArgumentException("Primary key value is not a String: " + primaryKeyValue);
            }
            return new UncheckedRow(sharedRealm.context, table,
                    nativeCreateNewObjectWithStringPrimaryKey(sharedRealm.getNativePtr(), table.getNativePtr(),
                            primaryKeyColumnIndex, (String) primaryKeyValue));

        } else if (type == RealmFieldType.INTEGER) {
            long value = primaryKeyValue == null ? 0 : Long.parseLong(primaryKeyValue.toString());
            return new UncheckedRow(sharedRealm.context, table,
                    nativeCreateNewObjectWithLongPrimaryKey(sharedRealm.getNativePtr(), table.getNativePtr(),
                            primaryKeyColumnIndex, value, primaryKeyValue == null));
        } else {
            throw new RealmException("Cannot check for duplicate rows for unsupported primary key type: " + type);
        }
    }

    /**
     * Create an object in the given table which has a primary key column defined, and set the primary key with given
     * value.
     * This is used for the fast bulk insertion.
     *
     * @param table the table where the object is created.
     * @param primaryKeyColumnIndex the column index of primary key field.
     * @param primaryKeyValue the primary key value.
     * @return a newly created {@code UncheckedRow}.
     */
    // FIXME: Proxy could just pass the pk index here which is much faster.
    public static long createRowWithPrimaryKey(Table table, long primaryKeyColumnIndex, Object primaryKeyValue) {
        RealmFieldType type = table.getColumnType(primaryKeyColumnIndex);
        final OsSharedRealm sharedRealm = table.getSharedRealm();

        if (type == RealmFieldType.STRING) {
            if (primaryKeyValue != null && !(primaryKeyValue instanceof String)) {
                throw new IllegalArgumentException("Primary key value is not a String: " + primaryKeyValue);
            }
            return nativeCreateRowWithStringPrimaryKey(sharedRealm.getNativePtr(), table.getNativePtr(),
                    primaryKeyColumnIndex, (String) primaryKeyValue);

        } else if (type == RealmFieldType.INTEGER) {
            long value = primaryKeyValue == null ? 0 : Long.parseLong(primaryKeyValue.toString());
            return nativeCreateRowWithLongPrimaryKey(sharedRealm.getNativePtr(), table.getNativePtr(),
                    primaryKeyColumnIndex, value, primaryKeyValue == null);
        } else {
            throw new RealmException("Cannot check for duplicate rows for unsupported primary key type: " + type);
        }
    }

    public static boolean isObjectIdColumn(String columnName) {
        return OBJECT_ID_COLUMN_NAME.equals(columnName);
    }

    // Called by JNI
    @SuppressWarnings("unused")
    private void notifyChangeListeners(String[] changedFields) {
        observerPairs.foreach(new Callback(changedFields));
    }

    private static native long nativeGetFinalizerPtr();

    private static native long nativeCreate(long shared_realm_ptr, long rowPtr);

    private native void nativeStartListening(long nativePtr);

    private native void nativeStopListening(long nativePtr);

    private static native long nativeCreateNewObject(long sharedRealmPtr, long tablePtr);

    private static native long nativeCreateRow(long sharedRealmPtr, long tablePtr);


    // Return a pointer to newly created Row. We may need to return a OsObject pointer in the future.
    private static native long nativeCreateNewObjectWithLongPrimaryKey(long sharedRealmPtr,
                                                                       long tablePtr, long pk_column_index,
                                                                       long primaryKeyValue, boolean isNullValue);

    // Return a index of newly created Row.
    private static native long nativeCreateRowWithLongPrimaryKey(long sharedRealmPtr,
                                                                 long tablePtr, long pk_column_index,
                                                                 long primaryKeyValue, boolean isNullValue);

    // Return a pointer to newly created Row. We may need to return a OsObject pointer in the future.
    private static native long nativeCreateNewObjectWithStringPrimaryKey(long sharedRealmPtr,
                                                                         long tablePtr, long pk_column_index,
                                                                         @Nullable String primaryKeyValue);

    // Return a index of newly created Row.
    private static native long nativeCreateRowWithStringPrimaryKey(long sharedRealmPtr,
                                                                   long tablePtr, long pk_column_index,
                                                                   String primaryKeyValue);

    // Return sync::object_id_column_name
    private static native String nativeGetObjectIdColumName();
}
