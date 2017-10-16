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

package io.realm.examples.newsreader.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.examples.newsreader.R;
import io.realm.examples.newsreader.model.Model;
import io.realm.examples.newsreader.model.entity.NYTimesStory;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.refresh_view) SwipeRefreshLayout refreshView;
    @BindView(R.id.list_view) ListView listView;
    @BindView(R.id.progressbar) MaterialProgressBar progressBar;
    @BindView(R.id.spinner) Spinner spinner;

    MainPresenter presenter = new MainPresenter(this, Model.getInstance());
    private ArrayAdapter<NYTimesStory> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup initial views
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        adapter = null;
        listView.setOnItemClickListener((parent, view, position, id) -> presenter.listItemSelected(position));
        listView.setEmptyView(getLayoutInflater().inflate(R.layout.common_emptylist, listView, false));

        refreshView.setOnRefreshListener(() -> presenter.refreshList());
        progressBar.setVisibility(View.INVISIBLE);

        // After setup, notify presenter
        presenter.onCreate();
    }

    /**
     * Setup the toolbar spinner with the available sections
     */
    public void configureToolbar(List<String> sections) {
        String[] sectionList = sections.toArray(new String[sections.size()]);
        final ArrayAdapter adapter = new ArrayAdapter<CharSequence>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, sectionList);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                presenter.titleSpinnerSectionSelected((String) adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }

    public void hideRefreshing() {
        refreshView.setRefreshing(false);
    }

    public void showList(List<NYTimesStory> items) {
        if (adapter == null) {
            adapter = new NewsListAdapter(MainActivity.this, items);
            listView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(items);
            adapter.notifyDataSetChanged();
        }
    }

    public void showNetworkLoading(Boolean networkInUse) {
        progressBar.setVisibility(networkInUse ? View.VISIBLE : View.INVISIBLE);
    }

    // ListView adapter class
    public static class NewsListAdapter extends ArrayAdapter<NYTimesStory> {

        private final LayoutInflater inflater;
        @LayoutRes private final int layoutResource;
        @ColorInt private final int readColor;
        @ColorInt private final int unreadColor;

        public NewsListAdapter(Context context, List<NYTimesStory> initialData) {
            super(context, android.R.layout.simple_list_item_1);
            setNotifyOnChange(false);
            addAll(initialData);
            inflater = LayoutInflater.from(context);
            layoutResource = android.R.layout.simple_list_item_1;
            readColor = context.getResources().getColor(android.R.color.darker_gray);
            unreadColor = context.getResources().getColor(android.R.color.primary_text_light);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(layoutResource, parent, false);
                ViewHolder holder = new ViewHolder(view);
                view.setTag(holder);
            }
            ViewHolder holder = (ViewHolder) view.getTag();
            NYTimesStory story = getItem(position);
            holder.titleView.setText(story.getTitle());
            holder.titleView.setTextColor(story.isRead() ? readColor : unreadColor);
            return view;
        }

        static class ViewHolder {
            @BindView(android.R.id.text1) TextView titleView;
            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
}
