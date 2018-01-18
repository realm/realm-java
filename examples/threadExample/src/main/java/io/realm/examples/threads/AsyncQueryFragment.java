/*
 * Copyright 2015 Realm Inc.
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

package io.realm.examples.threads;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.examples.threads.model.Dot;

/**
 * This fragment demonstrates how you can perform asynchronous queries with Realm.
 */
public class AsyncQueryFragment extends Fragment implements View.OnClickListener, RealmChangeListener<RealmResults<Dot>> {
    private Realm realm;
    private DotAdapter dotAdapter;
    private RealmResults<Dot> allSortedDots;
    private RealmAsyncTask asyncTransaction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_async_query, container, false);
        rootView.findViewById(R.id.translate_button).setOnClickListener(this);

        ListView listView = (ListView) rootView.findViewById(android.R.id.list);
        dotAdapter = new DotAdapter(getActivity());
        listView.setAdapter(dotAdapter);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Create Realm instance for the UI thread
        realm = Realm.getDefaultInstance();
        allSortedDots = realm.where(Dot.class)
                .between("x", 25, 75)
                .between("y", 0, 50)
                .sort(
                         "x", Sort.ASCENDING,
                         "y", Sort.DESCENDING
                 )
                .findAllAsync();
        dotAdapter.updateList(allSortedDots);
        allSortedDots.addChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remember to close the Realm instance when done with it.
        cancelAsyncTransaction();
        allSortedDots.removeChangeListener(this);
        allSortedDots = null;
        realm.close();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.translate_button: {
                cancelAsyncTransaction();
                // translate all points coordinates using an async transaction
                asyncTransaction = realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        // query for all points
                        RealmResults<Dot> dots = realm.where(Dot.class).findAll();

                        for (int i = dots.size() - 1; i >= 0; i--) {
                            Dot dot = dots.get(i);
                            if (dot.isValid()) {
                                int x = dot.getX();
                                int y = dot.getY();
                                dot.setX(y);
                                dot.setY(x);
                            }
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (isAdded()) {
                            Toast.makeText(getActivity(), "Translation completed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Realm.Transaction.OnError() {

                    @Override
                    public void onError(Throwable e) {
                        if (isAdded()) {
                            Toast.makeText(getActivity(), "Error while translating dots", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
                break;
            }
        }
    }

    private void cancelAsyncTransaction() {
        if (asyncTransaction != null && !asyncTransaction.isCancelled()) {
            asyncTransaction.cancel();
            asyncTransaction = null;
        }
    }

    @Override
    public void onChange(RealmResults<Dot> result) {
        dotAdapter.notifyDataSetChanged();
    }

    // Using a generic Adapter instead of RealmBaseAdapter, because
    // RealmBaseAdapter registers a listener against all Realm changes
    // whereas in this scenario we're only interested on the changes of our query
    private static class DotAdapter extends BaseAdapter {
        private List<Dot> dots = Collections.emptyList();
        private final LayoutInflater inflater;

        DotAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        void updateList(RealmResults<Dot> dots) {
            this.dots = dots;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return dots.size();
        }

        @Override
        public Dot getItem(int i) {
            return dots.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = inflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
                ViewHolder viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            }
            ViewHolder vh = (ViewHolder) view.getTag();
            vh.text.setText(view.getResources().getString(R.string.coordinate, getItem(i).getX(), getItem(i).getY()));
            return view;
        }

        private class ViewHolder {
            TextView text;

            ViewHolder(View view) {
                text = (TextView) view.findViewById(android.R.id.text1);
            }
        }
    }
}
