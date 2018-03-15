/*
 * Copyright 2018 Realm Inc.
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

package io.realm.examples.realmgridview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

// This adapter is strictly to interface with the GridView and doesn't
// particular show much interesting Realm functionality.

// Alternatively from this example,
// a developer could update the getView() to pull items from the Realm.

public class CityAdapter extends BaseAdapter {
    private List<City> cities = Collections.emptyList();

    public CityAdapter() {
    }

    public void setData(List<City> details) {
        if (details == null) {
            details = Collections.emptyList();
        }
        this.cities = details;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cities.size();
    }

    @Override
    public City getItem(int position) {
        return cities.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // ViewHolder caches view resources so that `findViewById` is not called for each row
    private static class ViewHolder {
        private TextView name;
        private TextView vote;

        public ViewHolder(View view) {
            name = view.findViewById(R.id.name);
            vote = view.findViewById(R.id.votes);
        }

        public void bind(City city) {
            name.setText(city.getName());
            vote.setText(String.format(Locale.US, "%d", city.getVotes()));
        }
    }

    @Override
    public View getView(int position, View currentView, ViewGroup parent) {
        // GridView requires ViewHolder pattern to ensure optimal performance
        ViewHolder viewHolder;
        if (currentView == null) {
            currentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_listitem, parent, false);
            viewHolder = new ViewHolder(currentView);
            currentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)currentView.getTag();
        }

        City city = cities.get(position);
        viewHolder.bind(city);

        return currentView;
    }
}
