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
package io.realm.examples.realmadapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;
import io.realm.examples.realmadapters.models.TimeStamp;

public class MyAdapter extends RealmBaseAdapter<TimeStamp> {

    private class MyViewHolder {
        TextView tvTimeStamp;
    }

    public MyAdapter(@NotNull Context context, int resId, @NotNull RealmResults<TimeStamp> realmResults, boolean automaticUpdate) {
        super(context, resId, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder mViewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
            mViewHolder = new MyViewHolder();
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        mViewHolder.tvTimeStamp = detail(convertView, android.R.id.text1, realmResults.get(position).getTimeStamp());
        return convertView;
    }

    private TextView detail(View v, int resId, String text) {
        TextView tv = (TextView) v.findViewById(resId);
        tv.setText(text);
        return tv;
    }

    public RealmResults<TimeStamp> getRealmResults() {
        return realmResults;
    }
}
