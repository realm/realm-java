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

package io.realm.examples.newsreader.ui.main;

import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import io.realm.examples.newsreader.model.Model;
import io.realm.examples.newsreader.model.entity.NYTimesStory;
import io.realm.examples.newsreader.ui.Presenter;
import io.realm.examples.newsreader.ui.details.DetailsActivity;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Presenter class for controlling the Main Activity
 */
public class MainPresenter implements Presenter {

    private final MainActivity view;
    private final Model model;
    private List<NYTimesStory> storiesData;
    private CompositeSubscription subscriptions;
    private Map<String, String> sections;

    public MainPresenter(MainActivity mainActivity, Model model) {
        this.view = mainActivity;
        this.model = model;
    }

    @Override
    public void onCreate() {
        sections = model.getSections();

        // Sort sections alphabetically, but always have Home at the top
        List<String> sectionList = new ArrayList<>(sections.keySet());
        Collections.sort(sectionList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                if (lhs.equals("Home")) return -1;
                if (rhs.equals("Home")) return 1;
                return lhs.compareToIgnoreCase(rhs);
            }
        });
        view.configureToolbar(sectionList);
        categorySelected(sectionList.get(0));
    }

    @Override
    public void onResume() {
        Subscription newsSubscription = model.getNewsFeed()
                .subscribe(new Action1<List<NYTimesStory>>() {
                    @Override
                    public void call(List<NYTimesStory> stories) {
                        storiesData = stories;
                        view.showList(stories);
                    }
                });

        Subscription loaderSubscription = model.isNetworkUsed()
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean networkInUse) {
                        view.showNetworkLoading(networkInUse);
                    }
                });

        subscriptions = new CompositeSubscription(newsSubscription, loaderSubscription);
    }

    @Override
    public void onPause() {
        subscriptions.unsubscribe();
    }

    @Override
    public void onDestroy() {
        // Do nothing
    }

    public void refreshList() {
        model.reloadNewsFeed();
        view.hideRefreshing();
    }

    public void listItemSelected(int position) {
        Intent intent = DetailsActivity.getIntent(view, storiesData.get(position));
        view.startActivity(intent);
    }

    public void categorySelected(@NonNull String categoryLabel) {
        String key = sections.get(categoryLabel);
        model.selectSection(key);
    }
}
