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
import io.realm.RealmModel;
import io.realm.RealmObjectChangeListener;


/**
 * Java wrapper for Object Store's {@code Object} class. Currently it is only used for object notifications.
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
}
