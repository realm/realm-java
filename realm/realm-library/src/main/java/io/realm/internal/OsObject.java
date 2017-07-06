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

import io.realm.ObjectChangeSet;
import io.realm.RealmFieldType;
import io.realm.RealmModel;
import io.realm.RealmObjectChangeListener;
import io.realm.exceptions.RealmException;


/**
 * Java wrapper for Object Store's {@code Object} class.
 */
@KeepMember
public class OsObject implements NativeObject {

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

        public void onChange(T observer, ObjectChangeSet changeSet) {
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

    public OsObject(OsSharedRealm osSharedRealm, UncheckedRow row) {
        nativePtr = nativeCreate(osSharedRealm.getNativePtr(), row.getNativePtr());
        osSharedRealm.context.addReference(this);
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
        final OsSharedRealm osSharedRealm = table.getOsSharedRealm();
        return new UncheckedRow(osSharedRealm.context, table,
                nativeCreateNewObject(osSharedRealm.getNativePtr(), table.getNativePtr()));
    }

    /**
     * Create a row in the given table which doesn't have a primary key column defined.
     * This is used for the fast bulk insertion.
     *
     * @param table the table where the object is created.
     * @return a newly created row's index.
     */
    public static long createRow(Table table) {
        final OsSharedRealm osSharedRealm = table.getOsSharedRealm();
        return nativeCreateRow(osSharedRealm.getNativePtr(), table.getNativePtr());
    }

    private static long getAndVerifyPrimaryKeyColumnIndex(Table table) {
        long primaryKeyColumnIndex = table.getPrimaryKey();
        if (primaryKeyColumnIndex == Table.NO_PRIMARY_KEY) {
            throw new IllegalStateException(table.getName() + " has no primary key defined.");
        }
        return primaryKeyColumnIndex;
    }

    // TODO: consider to return a OsObject instead when integrating with Object Store's object accessor.
    /**
     * Create an object in the given table which has a primary key column defined, and set the primary key with given
     * value.
     *
     * @param table the table where the object is created. This table must be atached to {@link OsSharedRealm}.
     * @return a newly created {@code UncheckedRow}.
     */
    public static UncheckedRow createWithPrimaryKey(Table table, Object primaryKeyValue) {
        long primaryKeyColumnIndex = getAndVerifyPrimaryKeyColumnIndex(table);
        RealmFieldType type = table.getColumnType(primaryKeyColumnIndex);
        final OsSharedRealm osSharedRealm = table.getOsSharedRealm();

        if (type == RealmFieldType.STRING) {
            if (primaryKeyValue != null && !(primaryKeyValue instanceof String)) {
                throw new IllegalArgumentException("Primary key value is not a String: " + primaryKeyValue);
            }
            return new UncheckedRow(osSharedRealm.context, table,
                    nativeCreateNewObjectWithStringPrimaryKey(osSharedRealm.getNativePtr(), table.getNativePtr(),
                            primaryKeyColumnIndex, (String) primaryKeyValue));

        } else if (type == RealmFieldType.INTEGER) {
            long value = primaryKeyValue == null ? 0 : Long.parseLong(primaryKeyValue.toString());
            return new UncheckedRow(osSharedRealm.context, table,
                    nativeCreateNewObjectWithLongPrimaryKey(osSharedRealm.getNativePtr(), table.getNativePtr(),
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
     * @return a newly created {@code UncheckedRow}.
     */
    public static long createRowWithPrimaryKey(Table table, Object primaryKeyValue) {
        long primaryKeyColumnIndex = getAndVerifyPrimaryKeyColumnIndex(table);
        RealmFieldType type = table.getColumnType(primaryKeyColumnIndex);
        final OsSharedRealm osSharedRealm = table.getOsSharedRealm();

        if (type == RealmFieldType.STRING) {
            if (primaryKeyValue != null && !(primaryKeyValue instanceof String)) {
                throw new IllegalArgumentException("Primary key value is not a String: " + primaryKeyValue);
            }
            return nativeCreateRowWithStringPrimaryKey(osSharedRealm.getNativePtr(), table.getNativePtr(),
                    primaryKeyColumnIndex, (String) primaryKeyValue);

        } else if (type == RealmFieldType.INTEGER) {
            long value = primaryKeyValue == null ? 0 : Long.parseLong(primaryKeyValue.toString());
            return nativeCreateRowWithLongPrimaryKey(osSharedRealm.getNativePtr(), table.getNativePtr(),
                    primaryKeyColumnIndex, value, primaryKeyValue == null);
        } else {
            throw new RealmException("Cannot check for duplicate rows for unsupported primary key type: " + type);
        }
    }

    // Called by JNI
    @SuppressWarnings("unused")
    @KeepMember
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
                                                                         String primaryKeyValue);

    // Return a index of newly created Row.
    private static native long nativeCreateRowWithStringPrimaryKey(long sharedRealmPtr,
                                                                   long tablePtr, long pk_column_index,
                                                                   String primaryKeyValue);
}
