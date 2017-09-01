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

import android.content.Intent;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.realm.examples.newsreader.model.Model;
import io.realm.examples.newsreader.model.entity.NYTimesStory;
import io.realm.examples.newsreader.ui.Presenter;
import io.realm.examples.newsreader.ui.details.DetailsActivity;

/**
 * Presenter class for controlling the Main Activity
 */
public class MainPresenter implements Presenter {

    private final MainActivity view;
    private final Model model;
    private List<NYTimesStory> storiesData;
    private Map<String, String> sections;
    private Disposable loaderDisposable;
    private Disposable listDataDisposable;

    public MainPresenter(MainActivity mainActivity, Model model) {
        this.view = mainActivity;
        this.model = model;
    }

    @Override
    public void onCreate() {
        sections = model.getSections();
        // Sort sections alphabetically, but always have Home at the top
        ArrayList<String> sectionList = new ArrayList<>(sections.values());
        Collections.sort(sectionList, (lhs, rhs) -> {
            if (lhs.equals("Home")) return -1;
            if (rhs.equals("Home")) return 1;
            return lhs.compareToIgnoreCase(rhs);
        });
        view.configureToolbar(sectionList);
    }

    @Override
    public void onResume() {
        loaderDisposable = model.isNetworkUsed().subscribe(networkInUse -> view.showNetworkLoading(networkInUse));
        sectionSelected(model.getCurrentSectionKey());
    }

    @Override
    public void onPause() {
        loaderDisposable.dispose();
        listDataDisposable.dispose();
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

    public void titleSpinnerSectionSelected(@NonNull String sectionLabel) {
        for (String key : sections.keySet()) {
            if (sections.get(key).equals(sectionLabel)) {
                sectionSelected(key);
                break;
            }
        }
    }

    private void sectionSelected(@NonNull String sectionKey) {
        model.selectSection(sectionKey);
        if (listDataDisposable != null) {
            listDataDisposable.dispose();
        }
        listDataDisposable = model.getSelectedNewsFeed()
                .subscribe(stories -> {
                    storiesData = stories;
                    view.showList(stories);
                });
    }
}
