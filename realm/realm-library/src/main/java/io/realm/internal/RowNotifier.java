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
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmChangeListener;

/**
 * To bridge object store's row notification to java. {@link SharedRealm} is supposed to hold a instance of this class
 * and pass it to JavaBindingContext. Row notifications callback will be executed when there are changes on a specific
 * row.
 */
@Keep
public class RowNotifier {
    @Keep
    private static class RowObserverPair<T> extends ObserverPairList.ObserverPair<T, RealmChangeListener<T>> {
        final WeakReference<UncheckedRow> rowRef;
        // Keep a strong ref to row when getRowRefs called and set it to null in clearRowRefs.
        // This is to avoid the row gets GCed in between.
        UncheckedRow row;
        public RowObserverPair(UncheckedRow row, T observer, RealmChangeListener<T> listener) {
            super(observer, listener);
            this.rowRef = new WeakReference<UncheckedRow>(row);
        }

        // Called by JNI in JavaBindingContext::did_change().
        @SuppressWarnings("unused")
        private void onChange() {
            T observer = observerRef.get();
            if (observer != null) {
                listener.onChange(observer);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof ObserverPairList.ObserverPair) {
                RowObserverPair anotherPair = (RowObserverPair) obj;
                return listener.equals(anotherPair.listener) &&
                        observerRef.get() == anotherPair.observerRef.get() &&
                        rowRef.get() == anotherPair.rowRef.get();
            }
            return false;
        }
    }

    // We don't take care of the duplicated rows here. The duplicated rows means the same Row object or different
    // Row objects point to the same row in the same table. The duplicated rows will all get notifications but there are
    // overheads when duplicated rows added since they all need to be processed to compute the differences for the row
    // level fine grained notifications in the object store.
    //private CopyOnWriteArrayList<RowObserverPair> rowObserverPairs = new CopyOnWriteArrayList<RowObserverPair>();
    private ObserverPairList<RowObserverPair> rowObserverPairs  = new ObserverPairList<RowObserverPair>();
    private static final ObserverPairList.Callback<RowObserverPair> toClearRowCallback =
            new ObserverPairList.Callback<RowObserverPair>() {
                @Override
                public void onCalled(RowObserverPair pair, Object observer) {
                    pair.row = null;
                }
            };

    /**
     * Register a listener on a row.
     *
     * @param row row to be observed.
     * @param observer the observer which will be passed back in the {@link RealmChangeListener#onChange(Object)}.
     * @param listener the listener.
     * @param <T> observer class.
     */
    public <T> void registerListener(UncheckedRow row, T observer, RealmChangeListener<T> listener) {
        RowObserverPair rowObserverPair = new RowObserverPair<T>(row, observer, listener);
        rowObserverPairs.add(rowObserverPair);
    }


    // The calling orders in JNI:
    // 1. getObservers() to get the array of current ObserverPair. (called in BindingContext::get_observed_rows)
    // 2. getObservedRowPtrs() with return value from step 1. To get an array of Row pointers. (called in
    //    BindingContext::get_observed_rows)
    // 3. Every RowObserverPair.onChange() deliver the changes to java. (called in BindingContext::did_change())
    // 4. clearRowRefs() to reset the strong reference we hold in the ObserverPair. (called in
    //    BindingContext::did_change())
    // Called by JNI
    @SuppressWarnings("unused")
    private RowObserverPair[] getObservers() {
        final List<RowObserverPair> pairList = new ArrayList<RowObserverPair>(rowObserverPairs.size());
        rowObserverPairs.foreach(new ObserverPairList.Callback<RowObserverPair>() {
            @Override
            public void onCalled(RowObserverPair pair, Object observer) {
                // TODO: Anyone knows why do we need to cast it here?
                // Keep a strong ref of the row! in case it gets GCed before clearRowRefs!
                pair.row = (UncheckedRow) pair.rowRef.get();
                pairList.add(pair);
            }
        });
        return pairList.toArray(new RowObserverPair[pairList.size()]);
    }

    // Called by JNI
    @SuppressWarnings("unused")
    private long[] getObservedRowPtrs(RowObserverPair[] observerPairs) {
        long[] ptrs = new long[observerPairs.length];
        for (int i = 0; i < observerPairs.length; i++) {
            ptrs[i]  = observerPairs[i].row.getNativePtr();
        }
        return ptrs;
    }

    // Called by JNI
    @SuppressWarnings("unused")
    private void clearRowRefs() {
        rowObserverPairs.foreach(toClearRowCallback);
    }
}
