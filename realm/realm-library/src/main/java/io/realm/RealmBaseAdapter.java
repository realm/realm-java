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

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

/**
 * The RealmBaseAdapter class is an abstract utility class for binding UI elements to Realm data, much like an
 * {@link android.widget.CursorAdapter}.
 * <p>
 * This adapter will automatically handle any updates to its data and call {@link #notifyDataSetChanged()} as
 * appropriate.
 * <p>
 * The RealmAdapter will stop receiving updates if the Realm instance providing the {@link io.realm.RealmResults} is
 * closed. Trying to access read objects, will at this point also result in a
 * {@link io.realm.exceptions.RealmException}.
 */
public abstract class RealmBaseAdapter<T extends RealmModel> extends BaseAdapter {

    protected LayoutInflater inflater;
    protected OrderedRealmCollection<T> adapterData;
    protected Context context;
    private final RealmChangeListener listener;

    public RealmBaseAdapter(Context context, OrderedRealmCollection<T> data, boolean automaticUpdate) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
        this.adapterData = data;
        this.inflater = LayoutInflater.from(context);
        this.listener = (!automaticUpdate) ? null : new RealmChangeListener() {
            @Override
            public void onChange() {
                notifyDataSetChanged();
            }
        };

        if (listener != null && data != null) {
            addListener(data);
        }
    }

    private void addListener(OrderedRealmCollection<T> data) {
        if (data instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) data;
            realmResults.realm.handlerController.addChangeListenerAsWeakReference(listener);
        } else if (data instanceof RealmList) {
            RealmList realmList = (RealmList) data;
            realmList.realm.handlerController.addChangeListenerAsWeakReference(listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    private void removeListener(OrderedRealmCollection<T> data) {
        if (data instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) data;
            realmResults.realm.handlerController.removeWeakChangeListener(listener);
        } else if (data instanceof RealmList) {
            RealmList realmList = (RealmList) data;
            realmList.realm.handlerController.removeWeakChangeListener(listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    /**
     * Returns how many items are in the data set.
     *
     * @return count of items.
     */
    @Override
    public int getCount() {
        if (adapterData == null) {
            return 0;
        }
        return adapterData.size();
    }

    /**
     * Returns the item associated with the specified position.
     *
     * @param i index of item whose data we want.
     * @return the item at the specified position.
     */
    @Override
    public T getItem(int i) {
        if (adapterData == null) {
            return null;
        }
        return adapterData.get(i);
    }

    /**
     * Returns the current ID for an item. Note that item IDs are not stable so you cannot rely on the item ID being the
     * same after {@link #notifyDataSetChanged()} or {@link #updateRealmResults(RealmResults)} has been called.
     *
     * @param i index of item in the adapter.
     * @return current item ID.
     */
    @Override
    public long getItemId(int i) {
        // TODO: find better solution once we have unique IDs
        return i;
    }

    /**
     * DEPRECATED: Use {@link #updateData(OrderedRealmCollection)} instead.
     */
    @Deprecated
    public void updateRealmResults(RealmResults<T> queryResults) {
        updateData(queryResults);
    }

    /**
     * Updates the data associated with the Adapter.
     *
     * Note that RealmResults and RealmLists are "live" views, so they will automatically be updated to reflect the
     * latest changes. This will also trigger {@code notifyDataSetChanged()} to be called on the adapter.
     *
     * This method is therefor only useful if you want to display data based on a new query without replacing the
     * adapter.
     *
     * @param data the new {@link OrderedRealmCollection} to display.
     */
    public void updateData(OrderedRealmCollection<T> data) {
        if (listener != null) {
            if (adapterData != null) {
                removeListener(adapterData);
            }
            if (data != null) {
                addListener(data);
            }
        }

        this.adapterData = data;
        notifyDataSetChanged();
    }
}
