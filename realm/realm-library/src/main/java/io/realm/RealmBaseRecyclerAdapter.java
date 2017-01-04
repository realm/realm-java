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

package io.realm;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

/**
 * The RealmBaseRecyclerAdapter class is an abstract utility class for binding RecyclerView UI elements to Realm data.
 * <p>
 * This adapter will automatically handle any updates to its data and call notifyDataSetChanged() as appropriate.
 * Currently there is no support for RecyclerView's data callback methods like notifyItemInserted(int), notifyItemRemoved(int),
 * notifyItemChanged(int) etc.
 * It means that, there is no possibility to use default data animations.
 * <p>
 * The RealmAdapter will stop receiving updates if the Realm instance providing the {@link OrderedRealmCollection} is
 * closed.
 *
 * @param <T> type of {@link RealmObject} stored in the adapter.
 * @param <VH> type of RecyclerView.ViewHolder used in the adapter.
 */
public abstract class RealmBaseRecyclerAdapter<T extends RealmObject, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected final LayoutInflater inflater;
    protected final Context context;
    private final boolean hasAutoUpdates;
    private final RealmChangeListener listener;
    private OrderedRealmCollection<T> adapterData;

    public RealmBaseRecyclerAdapter(Context context, OrderedRealmCollection<T> data, boolean autoUpdate) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        this.context = context;
        this.adapterData = data;
        this.inflater = LayoutInflater.from(context);
        this.hasAutoUpdates = autoUpdate;
        this.listener = hasAutoUpdates ? new RealmChangeListener() {
            @Override
            public void onChange() {
                notifyDataSetChanged();
            }
        } : null;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (hasAutoUpdates && isDataValid()) {
            addListener(adapterData);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(final RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (hasAutoUpdates && isDataValid()) {
            removeListener(adapterData);
        }
    }

    /**
     * Returns the current ID for an item. Note that item IDs are not stable so you cannot rely on the item ID being the
     * same after notifyDataSetChanged() or {@link #updateData(OrderedRealmCollection)} has been called.
     *
     * @param index position of item in the adapter.
     * @return current item ID.
     */
    @Override
    public long getItemId(final int index) {
        return index;
    }

    @Override
    public int getItemCount() {
        return isDataValid() ? adapterData.size() : 0;
    }

    /**
     * Returns the item associated with the specified position.
     * Can return null if provided Realm instance by {@link OrderedRealmCollection} is closed.
     *
     * @param index index of the item.
     * @return the item at the specified position, null if adapter data is not valid.
     */
    public T getItem(int index) {
        return isDataValid() ? adapterData.get(index) : null;
    }

    /**
     * Returns data associated with this adapter.
     *
     * @return adapter data.
     */
    public OrderedRealmCollection<T> getData() {
        return adapterData;
    }

    /**
     * Updates the data associated to the Adapter. Useful when the query has been changed.
     * If the query does not change you might consider using the automaticUpdate feature.
     *
     * @param data the new {@link OrderedRealmCollection} to display.
     */
    public void updateData(OrderedRealmCollection<T> data) {
        if (hasAutoUpdates) {
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

    private void addListener(OrderedRealmCollection<T> data) {
        BaseRealm realm = getRealm(data);
        if (realm != null && !realm.isClosed()) {
            realm.addChangeListener(listener);
        }
    }

    private void removeListener(OrderedRealmCollection<T> data) {
        BaseRealm realm = getRealm(data);
        if (realm != null && !realm.isClosed()) {
            realm.removeChangeListener(listener);
        }
    }

    private BaseRealm getRealm(OrderedRealmCollection<T> data) {
        if (data instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) data;
            return realmResults.realm;
        } else if (data instanceof RealmList) {
            RealmList realmList = (RealmList) data;
            return realmList.realm;
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    private boolean isDataValid() {
        return adapterData != null && adapterData.isValid();
    }
}
