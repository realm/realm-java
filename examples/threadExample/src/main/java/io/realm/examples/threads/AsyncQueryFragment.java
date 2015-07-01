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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.examples.threads.model.Dot;

/**
 * This fragment demonstrates how you can perform asynchronous queries with Realm
 */
public class AsyncQueryFragment extends Fragment implements View.OnClickListener {
    private Realm realm;
    private Button mBtnStart;
    private DotAdapter mAdapter;

    private RealmQuery.Request mAsyncRequest;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_async_query, container, false);
        mBtnStart = (Button) rootView.findViewById(R.id.start_button);
        mBtnStart.setOnClickListener(this);

        ListView listView = (ListView) rootView.findViewById(android.R.id.list);
        mAdapter = new DotAdapter(getActivity());
        listView.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Create Realm instance for the UI thread
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remember to close the Realm instance when done with it.
        realm.close();
        cancelRequest();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_button: {
                // cancel any previously running request
                cancelRequest();

                mAsyncRequest = realm.where(Dot.class)
                                    .between("x", 25, 75)
                                    .between("y", 0, 50)
                                    .findAllSorted(
                                            "x", true,
                                            "y", true,
                                            new RealmResults.QueryCallback<Dot>() {
                                                @Override
                                                public void onSuccess(RealmResults<Dot> results) {
                                                    mAdapter.updateList(results);
                                                }

                                                @Override
                                                public void onError(Exception t) {
                                                    t.printStackTrace();
                                                }
                                            });
                break;
            }
        }
    }

    private void cancelRequest() {
        if (mAsyncRequest != null && !mAsyncRequest.isCancelled()) {
            mAsyncRequest.cancel();
            mAsyncRequest = null;
        }
    }

    private class DotAdapter extends BaseAdapter {
        private List<Dot> dots = Collections.emptyList();
        private final LayoutInflater inflater;

        DotAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        void updateList(List<Dot> dots) {
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
            vh.text.setText("[X= " + getItem(i).getX() + " Y= " + getItem(i).getY() + "]");

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
