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

import android.content.Context;
import android.support.annotation.UiThread;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.examples.newsreader.NewsReaderApplication;
import io.realm.examples.newsreader.R;
import io.realm.examples.newsreader.model.entity.NYTimesStory;
import io.realm.examples.newsreader.model.network.NYTimesDataLoader;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * Class for handling loading and saving data.
 *
 * A repository is a potentially expensive resource to have in memory, so should be closed when no longer needed/used.
 *
 * @see <a href="http://martinfowler.com/eaaCatalog/repository.html">Repository pattern</a>
 */
public class Repository implements Closeable {

    private static final long MINIMUM_NETWORK_WAIT_SEC = 120; // Minimum 2 minutes between each network request

    private final Realm realm;
    private final NYTimesDataLoader dataLoader;
    private final Context context;
    private long lastNetworkRequest;
    private BehaviorSubject<Boolean> networkLoading = BehaviorSubject.create(false);

    @UiThread
    public Repository() {
        realm = Realm.getDefaultInstance();
        context = NewsReaderApplication.getContext();
        dataLoader = new NYTimesDataLoader();
    }

    /**
     * Keep track of the current network state.
     *
     * @returns {@code true} if the network is currently being used, {@code false} otherwise.
     */
    public Observable<Boolean> networkInUse() {
        return networkLoading.asObservable();
    }

    /**
     * Load the news feed as well as all future updates.
     */
    public Observable<RealmResults<NYTimesStory>> loadNewsFeed(boolean forceReload) {
        // Start loading data from the network if needed
        // It will put all data into Realm
        if (forceReload || timeSinceLastNetworkRequest() > MINIMUM_NETWORK_WAIT_SEC) {
            dataLoader.loadAllData(realm, context.getString(R.string.nyc_top_stories_api_key), networkLoading);
            lastNetworkRequest = System.currentTimeMillis();
        }

        // Return the data in Realm. The query result will be automatically updated when the network requests
        // save data in Realm
        return realm.where(NYTimesStory.class).findAllSorted(NYTimesStory.PUBLISHED_DATE, Sort.DESCENDING).asObservable()
                .filter(new Func1<RealmResults<NYTimesStory>, Boolean>() {
                    @Override
                    public Boolean call(RealmResults<NYTimesStory> stories) {
                        return stories.isLoaded();
                    }
                });
    }

    private long timeSinceLastNetworkRequest() {
        return TimeUnit.SECONDS.convert(System.currentTimeMillis() - lastNetworkRequest, TimeUnit.MILLISECONDS);
    }

    /**
     * Updates a story.
     *
     * @param storyId story to update
     * @param read {@code true} if the story has been read, {@code false} otherwise.
     */
    public void updateStoryReadState(final String storyId, final boolean read) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                NYTimesStory persistedStory = realm.where(NYTimesStory.class).equalTo(NYTimesStory.URL, storyId).findFirst();
                if (persistedStory != null) {
                    persistedStory.setRead(read);
                } else {
                    Timber.e("Trying to update a story that no longer exists: " + storyId);
                }
            }
        }, new DefaultTransactionCallback());
    }

    /**
     * Returns story details
     */
    public Observable<NYTimesStory> loadStory(final String storyId) {
        return realm.where(NYTimesStory.class).equalTo(NYTimesStory.URL, storyId).findFirstAsync()
                .asObservable()
                .filter(new Func1<NYTimesStory, Boolean>() {
                    @Override
                    public Boolean call(NYTimesStory story) {
                        return story.isLoaded();
                    }
                });
    }

    /**
     * Close all underlying resources used byt the Repository.
     */
    public void close() {
        realm.close();
    }
}
