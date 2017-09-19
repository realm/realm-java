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

package io.realm.examples.newsreader.ui.details;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.realm.examples.newsreader.model.Model;
import io.realm.examples.newsreader.ui.Presenter;

/**
 * Presenter class for controlling the Main Activity
 */
public class DetailsPresenter implements Presenter {

    private final DetailsActivity view;
    private final Model model;
    private final String storyId;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public DetailsPresenter(DetailsActivity detailsActivity, Model model, String storyId) {
        this.storyId = storyId;
        this.view = detailsActivity;
        this.model = model;
    }

    @Override
    public void onCreate() {
        view.showLoader();
    }

    @Override
    public void onResume() {
        // Show story details
        Disposable detailsDisposable = model.getStory(storyId)
                .subscribe(story -> {
                    view.hideLoader();
                    view.showStory(story);
                    view.setRead(story.isRead());
                });
        compositeDisposable.add(detailsDisposable);

        // Mark story as read if screen is visible for 2 seconds
        Disposable timberDisposable = Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> model.markAsRead(storyId, true));
        compositeDisposable.add(timberDisposable);
    }

    @Override
    public void onPause() {
        compositeDisposable.clear();
    }

    @Override
    public void onDestroy() {
        // Do nothing
    }
}
