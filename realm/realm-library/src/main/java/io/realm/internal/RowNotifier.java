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

package io.realm.internal;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.realm.RealmChangeListener;

public class RowNotifier {

    private static class Observer {
        final RealmChangeListener listener;
        final Object object;
        UncheckedRow row;
        Observer(RealmChangeListener listener, Object object) {
            this.listener = listener;
            this.object = object;
            this.row = null;
        }
        public void notifyListener() {
            listener.onChange(object);
        }
    }

    // FIXME: Use weak ref for the key. And make the memory ownership clear in the doc.
    Map<UncheckedRow, Observer> rowObserverMap = new HashMap<>();

    public void registerListener(UncheckedRow row, RealmChangeListener listener, Object object) {
        Observer observer = new Observer(listener, object);
        rowObserverMap.put(row, observer);
    }

    // Called by JNI
    @SuppressWarnings("unused")
    private Observer[] getObservers() {
        Observer[] observers = new Observer[rowObserverMap.size()];
        int i = 0;
        for (Map.Entry<UncheckedRow, Observer> entry : rowObserverMap.entrySet()) {
            observers[i]  = entry.getValue();
            observers[i].row = entry.getKey();
        }
        return observers;
    }

    // Called by JNI
    @SuppressWarnings("unused")
    private long[] getObservedRowPtrs(Observer[] observers) {
        long[] ptrs = new long[observers.length];
        for (int i = 0; i < observers.length; i++) {
            ptrs[i]  = observers[i].row.getNativePtr();
        }
        return ptrs;
    }

    // Called by JNI
    @SuppressWarnings("unused")
    private void clearRowRefs() {
        for (Observer observer : rowObserverMap.values()) {
            observer.row = null;
        }
    }
}
