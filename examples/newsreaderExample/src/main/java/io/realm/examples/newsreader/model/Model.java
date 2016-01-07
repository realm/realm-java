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

package io.realm.examples.newsreader.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.RealmResults;
import io.realm.examples.newsreader.model.entity.NYTimesStory;
import rx.Observable;
import rx.functions.Func1;

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
        sections.put("Home", "home");
        sections.put("World", "world");
        sections.put("National", "national");
        sections.put("Politics", "politics");
        sections.put("NY Region", "nyregion");
        sections.put("Business", "business");
        sections.put("Opinion", "opinion");
        sections.put("Technology", "technology");
        sections.put("Science", "science");
        sections.put("Health", "health");
        sections.put("Sports", "sports");
        sections.put("Arts", "arts");
        sections.put("Fashion", "fashion");
        sections.put("Dining", "dining");
        sections.put("Travel", "travel");
        sections.put("Magazine", "magazine");
        sections.put("Real Estate", "realestate");
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
     * Returns a news feed observable for.
     */
    public Observable<RealmResults<NYTimesStory>> getNewsFeed() {
        return repository.loadNewsFeed(false);
    }

    /**
     * Forces a reload of the newsfeed
     */
    public void reloadNewsFeed() {
        repository.loadNewsFeed(true);
    }

    /**
     * Returns the current state of network usage.
     */
    public Observable<Boolean> isNetworkUsed() {
        return repository.networkInUse().distinctUntilChanged();
    }

    /**
     * Mark a story as being read.
     */
    public void markAsRead(String storyId, boolean read) {
        repository.updateStoryReadState(storyId, read);
    }

    /**
     * Returns the story with the given Id
     */
    public Observable<NYTimesStory> getStory(final String storyId) {
        // Repository is only responsible for loading the data
        // Any validation is done by the model
        // See http://blog.danlew.net/2015/12/08/error-handling-in-rxjava/
        if (TextUtils.isEmpty(storyId)) {
            throw new IllegalArgumentException("Invalid storyId: " + storyId);
        }
        return repository.loadStory(storyId)
                .filter(new Func1<NYTimesStory, Boolean>() {
                    @Override
                    public Boolean call(NYTimesStory story) {
                        return story.isValid();
                    }
                });
    }

    /**
     * Returns all sections available
     */
    public Map<String, String> getSections() {
        return sections;
    }

    public void selectSection(@NonNull String key) {
        selectedSection = key;
        repository.loadNewsFeed(selectedSection);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        repository.close();
    }
}
