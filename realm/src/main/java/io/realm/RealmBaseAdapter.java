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

public abstract class RealmBaseAdapter<T extends RealmObject> extends BaseAdapter {

    protected LayoutInflater inflater;
    protected RealmResults<T> realmResults;
    protected Context context;

    public RealmBaseAdapter(Context context, RealmResults<T> realmResults, boolean automaticUpdate) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (realmResults == null) {
            throw new IllegalArgumentException("RealmResults cannot be null");
        }

        this.context = context;
        this.realmResults = realmResults;
        this.inflater = LayoutInflater.from(context);
        if (automaticUpdate) {
            realmResults.getRealm().addChangeListener(new RealmChangeListener() {
                @Override
                public void onChange() {
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getCount() {
        return realmResults.size();
    }

    @Override
    public T getItem(int i) {
        return realmResults.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i; // TODO: find better solution once we have unique IDs
    }

    /**
     * Update the RealmResults associated to the Adapter. Useful when the query has been changed.
     * If the query does not change you might consider using the automaticUpdate feature
     * @param realmResults the new RealmResults coming from the new query.
     */
    public void updateRealmResults(RealmResults<T> realmResults) {
        this.realmResults = realmResults;
        notifyDataSetChanged();
    }
}
