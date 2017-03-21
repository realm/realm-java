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
import io.realm.RealmObjectChangeListener;


/**
 * Java wrapper for Object Store's {@code Object} class. Currently it is only used for object notifications.
 */
public class OsObject implements NativeObject {

    private static class RowObjectChangeSet implements ObjectChangeSet {
        final FieldChange[] fieldChanges;
        final boolean deleted;

        RowObjectChangeSet() {
            this.fieldChanges = null;
            this.deleted = true;
        }

        RowObjectChangeSet(FieldChange[] fieldChanges) {
            this.fieldChanges = fieldChanges;
            this.deleted = false;
        }

        @Override
        public FieldChange[] getFieldChanges() {
            return fieldChanges;
        }

        @Override
        public boolean isDeleted() {
            return deleted;
        }
    }

    private class ObjectObserverPair<T> extends ObserverPairList.ObserverPair<T, RealmObjectChangeListener<T>> {
        ObjectObserverPair(T observer, RealmObjectChangeListener<T> listener) {
            super(observer, listener);
        }

        void onChange(T observer, ObjectChangeSet changeSet) {
            listener.onChange(observer, changeSet);
        }
    }

    private static class Callback implements ObserverPairList.Callback<ObjectObserverPair> {
        private final String[] changedFields;

        Callback(String[] changedFields) {
            this.changedFields = changedFields;
        }

        private ObjectChangeSet createChangeSet() {
            if (changedFields == null) {
                return new RowObjectChangeSet();
            }

            ObjectChangeSet.FieldChange[] fieldChanges = new ObjectChangeSet.FieldChange[changedFields.length];
            for (int i = 0; i < changedFields.length; i++) {
                fieldChanges[i] = new ObjectChangeSet.FieldChange(changedFields[i]);
            }
            return new RowObjectChangeSet(fieldChanges);
        }

        @Override
        public void onCalled(ObjectObserverPair pair, Object observer) {
            //noinspection unchecked
            pair.onChange(observer, createChangeSet());
        }
    }

    private final long nativePtr;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    private ObserverPairList<ObjectObserverPair> observerPairs = new ObserverPairList<ObjectObserverPair>();

    public OsObject(SharedRealm sharedRealm, UncheckedRow row) {
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

    public <T> void addListener(T observer, RealmObjectChangeListener<T> listener) {
        if (observerPairs.isEmpty()) {
            nativeStartListening(nativePtr);
        }
        ObjectObserverPair<T> pair = new ObjectObserverPair<T>(observer, listener);
        observerPairs.add(pair);
    }

    public <T> void removeListener(T observer) {
        observerPairs.removeByObserver(observer);
        if (observerPairs.isEmpty()) {
            nativeStopListening(nativePtr);
        }
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
}
