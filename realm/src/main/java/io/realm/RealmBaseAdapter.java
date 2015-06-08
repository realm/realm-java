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
 * The RealmBaseAdapter class is an abstract utility class for binding UI elements to Realm data,
 * much like an {@link android.widget.CursorAdapter}.
 * <p>
 * This adapter will automatically handle any updates to its data and call
 * {@link #notifyDataSetChanged()} as appropriate.
 * <p>
 * The RealmAdapter will stop receiving updates if the Realm instance providing the
 * {@link io.realm.RealmResults} is closed. Trying to access read objects, will at this point also
 * result in a {@link io.realm.exceptions.RealmException}.
 */
public abstract class RealmBaseAdapter<T extends RealmObject> extends BaseAdapter {

    protected LayoutInflater inflater;
    protected RealmResults<T> realmResults;
    protected Context context;
    private final RealmChangeListener listener;

    public RealmBaseAdapter(Context context, RealmResults<T> realmResults, boolean automaticUpdate) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
        this.realmResults = realmResults;
        this.inflater = LayoutInflater.from(context);
        this.listener = (!automaticUpdate) ? null : new RealmChangeListener() {
            @Override
            public void onChange() {
                notifyDataSetChanged();
            }
        };

        if (listener != null && realmResults != null) {
            realmResults.getRealm().addChangeListener(listener);
        }
    }

    @Override
    public int getCount() {
        if (realmResults == null) {
            return 0;
        }
        return realmResults.size();
    }

    @Override
    public T getItem(int i) {
        if (realmResults == null) {
            return null;
        }
        return realmResults.get(i);
    }

    /**
     * Returns the current ID for an item. Note that item IDs are not stable so you cannot rely on
     * the item ID being the same after {@link #notifyDataSetChanged()} or
     * {@link #updateRealmResults(RealmResults)} has been called.
     *
     * @param i Index of item in the adapter
     * @return Current item ID.
     */
    @Override
    public long getItemId(int i) {
        // TODO: find better solution once we have unique IDs
        return i;
    }

    /**
     * Update the RealmResults associated to the Adapter. Useful when the query has been changed.
     * If the query does not change you might consider using the automaticUpdate feature
     *
     * @param queryResults the new RealmResults coming from the new query.
     */
    public void updateRealmResults(RealmResults<T> queryResults) {
        if (listener != null) {
            // Making sure that Adapter is refreshed correctly if new RealmResults come from another Realm
            if (this.realmResults != null) {
                this.realmResults.getRealm().removeChangeListener(listener);
            }
            if (queryResults != null) {
                queryResults.getRealm().addChangeListener(listener);
            }
        }

        this.realmResults = queryResults;
        notifyDataSetChanged();
    }
}
