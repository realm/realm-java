/*
 * Copyright 2019 Realm Inc.
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

package io.realm.examples.objectserver.advanced;

import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.realm.OrderedRealmCollection;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;

/**
 * The RealmBaseAdapter class is an abstract utility class for binding UI elements to Realm data, much like an
 * {@link android.widget.CursorAdapter}.
 * <p>
 * This adapter will automatically handle any updates to its data and call {@link #notifyDataSetChanged()} as
 * appropriate.
 * <p>
 * The RealmAdapter will stop receiving updates if the Realm instance providing the {@link io.realm.RealmResults} is
 * closed. Trying to access Realm objects will at this point also result in a {@code IllegalStateException}.
 */
public abstract class RealmBaseAdapter<T extends RealmModel> extends BaseAdapter {
    @Nullable
    protected OrderedRealmCollection<T> adapterData;
    private final RealmChangeListener<OrderedRealmCollection<T>> listener;

    public RealmBaseAdapter(@Nullable OrderedRealmCollection<T> data) {
        if (data != null && !data.isManaged())
            throw new IllegalStateException("Only use this adapter with managed list, " +
                    "for un-managed lists you can just use the BaseAdapter");
        this.adapterData = data;
        this.listener = new RealmChangeListener<OrderedRealmCollection<T>>() {
            @Override
            public void onChange(OrderedRealmCollection<T> results) {
                notifyDataSetChanged();
            }
        };

        if (isDataValid()) {
            //noinspection ConstantConditions
            addListener(data);
        }
    }

    private void addListener(@NonNull OrderedRealmCollection<T> data) {
        if (data instanceof RealmResults) {
            RealmResults<T> results = (RealmResults<T>) data;
            //noinspection unchecked
            results.addChangeListener((RealmChangeListener) listener);
        } else if (data instanceof RealmList) {
            RealmList<T> list = (RealmList<T>) data;
            //noinspection unchecked
            list.addChangeListener((RealmChangeListener) listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    private void removeListener(@NonNull OrderedRealmCollection<T> data) {
        if (data instanceof RealmResults) {
            RealmResults<T> results = (RealmResults<T>) data;
            //noinspection unchecked
            results.removeChangeListener((RealmChangeListener) listener);
        } else if (data instanceof RealmList) {
            RealmList<T> list = (RealmList<T>) data;
            //noinspection unchecked
            list.removeChangeListener((RealmChangeListener) listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    /**
     * Returns how many items are in the data set.
     *
     * @return the number of items.
     */
    @Override
    public int getCount() {
        //noinspection ConstantConditions
        return isDataValid() ? adapterData.size() : 0;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     * data set.
     * @return The data at the specified position.
     */
    @Override
    @Nullable
    public T getItem(int position) {
        //noinspection ConstantConditions
        return isDataValid() ? adapterData.get(position) : null;
    }

    /**
     * Get the row value associated with the specified position in the list. Note that item IDs are not stable so you
     * cannot rely on the item ID being the same after {@link #notifyDataSetChanged()} or
     * {@link #updateData(OrderedRealmCollection)} has been called.
     *
     * @param position The position of the item within the adapter's data set whose row value we want.
     * @return The value of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        // TODO: find better solution once we have unique IDs
        return position;
    }

    /**
     * Updates the data associated with the Adapter.
     *
     * Note that RealmResults and RealmLists are "live" views, so they will automatically be updated to reflect the
     * latest changes. This will also trigger {@code notifyDataSetChanged()} to be called on the adapter.
     *
     * This method is therefore only useful if you want to display data based on a new query without replacing the
     * adapter.
     *
     * @param data the new {@link OrderedRealmCollection} to display.
     */
    @SuppressWarnings("WeakerAccess")
    public void updateData(@Nullable OrderedRealmCollection<T> data) {
        if (listener != null) {
            if (isDataValid()) {
                //noinspection ConstantConditions
                removeListener(adapterData);
            }
            if (data != null && data.isValid()) {
                addListener(data);
            }
        }

        this.adapterData = data;
        notifyDataSetChanged();
    }

    private boolean isDataValid() {
        return adapterData != null && adapterData.isValid();
    }
}