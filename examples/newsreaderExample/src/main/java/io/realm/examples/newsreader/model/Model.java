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

package io.realm.examples.newsreader.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.realm.RealmResults;
import io.realm.examples.newsreader.model.entity.NYTimesStory;

/**
 * Model class for handling the business rules of the app.
 */
public class Model {

    /**
     * Map between section titles and their NYTimes API keys
     */
    private static final Map<String, String> sections;
    static {
        sections = new HashMap<>();
        sections.put("home", "Home");
        sections.put("world", "World");
        sections.put("national", "National");
        sections.put("politics", "Politics");
        sections.put("nyregion", "NY Region");
        sections.put("business", "Business");
        sections.put("opinion", "Opinion");
        sections.put("technology", "Technology");
        sections.put("science", "Science");
        sections.put("health", "Health");
        sections.put("sports", "Sports");
        sections.put("arts", "Arts");
        sections.put("fashion", "Fashion");
        sections.put("dining", "Dining");
        sections.put("travel", "Travel");
        sections.put("magazine", "Magazine");
        sections.put("realestate", "Real Estate");
    }

    private static Model instance = null;
    private final Repository repository;
    private String selectedSection;

    // This could be replaced by Dependency Injection for easier testing
    public static synchronized Model getInstance() {
        if (instance == null) {
            Repository repository = new Repository();
            instance = new Model(repository);
        }
        return instance;
    }

    private Model(Repository repository) {
        this.repository = repository;
        this.selectedSection = "home";
    }

    /**
     * Returns the news feed for the currently selected category.
     */
    public Flowable<RealmResults<NYTimesStory>> getSelectedNewsFeed() {
        return repository.loadNewsFeed(selectedSection, false);
    }

    /**
     * Forces a reload of the newsfeed
     */
    public void reloadNewsFeed() {
        repository.loadNewsFeed(selectedSection, true);
    }

    /**
     * Returns the current state of network usage.
     */
    public Observable<Boolean> isNetworkUsed() {
        return repository.networkInUse().distinctUntilChanged();
    }

    /**
     * Marks a story as being read.
     */
    public void markAsRead(@NonNull String storyId, boolean read) {
        repository.updateStoryReadState(storyId, read);
    }

    /**
     * Returns the story with the given Id
     */
    public Flowable<NYTimesStory> getStory(@NonNull final String storyId) {
        // Repository is only responsible for loading the data
        // Any validation is done by the model
        // See http://blog.danlew.net/2015/12/08/error-handling-in-rxjava/
        if (TextUtils.isEmpty(storyId)) {
            throw new IllegalArgumentException("Invalid storyId: " + storyId);
        }
        return repository.loadStory(storyId).filter(story -> story.isValid());
    }

    /**
     * Returns all sections available.
     *
     * @return A map of <key, title> pairs for all available sections.
     */
    public Map<String, String> getSections() {
        return sections;
    }

    public void selectSection(@NonNull String key) {
        selectedSection = key;
        repository.loadNewsFeed(selectedSection, false);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        repository.close();
    }

    public @NonNull String getCurrentSectionKey() {
        return selectedSection;
    }
}
