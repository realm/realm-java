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
import android.support.annotation.UiThread;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.examples.newsreader.NewsReaderApplication;
import io.realm.examples.newsreader.R;
import io.realm.examples.newsreader.model.entity.NYTimesStory;
import io.realm.examples.newsreader.model.network.NYTimesDataLoader;
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
    private final String apiKey;
    private Map<String, Long> lastNetworkRequest = new HashMap<>();
    private BehaviorSubject<Boolean> networkLoading = BehaviorSubject.createDefault(false);

    @UiThread
    public Repository() {
        realm = Realm.getDefaultInstance();
        dataLoader = new NYTimesDataLoader();
        apiKey = NewsReaderApplication.getContext().getString(R.string.nyc_top_stories_api_key);
    }

    /**
     * Keeps track of the current network state.
     *
     * @return {@code true} if the network is currently being used, {@code false} otherwise.
     */
    @UiThread
    public Observable<Boolean> networkInUse() {
        return networkLoading.hide();
    }

    /**
     * Loads the news feed as well as all future updates.
     */
    @UiThread
    public Flowable<RealmResults<NYTimesStory>> loadNewsFeed(@NonNull String sectionKey, boolean forceReload) {
        // Start loading data from the network if needed
        // It will put all data into Realm
        if (forceReload || timeSinceLastNetworkRequest(sectionKey) > MINIMUM_NETWORK_WAIT_SEC) {
            dataLoader.loadData(sectionKey, apiKey, realm, networkLoading);
            lastNetworkRequest.put(sectionKey, System.currentTimeMillis());
        }

        // Return the data in Realm. The query result will be automatically updated when the network requests
        // save data in Realm
        return realm.where(NYTimesStory.class)
                .equalTo(NYTimesStory.API_SECTION, sectionKey)
                .sort(NYTimesStory.PUBLISHED_DATE, Sort.DESCENDING)
                .findAllAsync()
                .asFlowable();
    }

    private long timeSinceLastNetworkRequest(@NonNull String sectionKey) {
        Long lastRequest = lastNetworkRequest.get(sectionKey);
        if (lastRequest != null) {
            return TimeUnit.SECONDS.convert(System.currentTimeMillis() - lastRequest, TimeUnit.MILLISECONDS);
        } else {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Updates a story.
     *
     * @param storyId story to update
     * @param read {@code true} if the story has been read, {@code false} otherwise.
     */
    @UiThread
    public void updateStoryReadState(final String storyId, final boolean read) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                NYTimesStory persistedStory = realm.where(NYTimesStory.class).equalTo(NYTimesStory.URL, storyId).findFirst();
                if (persistedStory != null) {
                    persistedStory.setRead(read);
                } else {
                    Timber.e("Trying to update a story that no longer exists: %1$s", storyId);
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable throwable) {
                Timber.e(throwable, "Failed to save data.");
            }
        });
    }

    /**
     * Returns story details
     */
    @UiThread
    public Flowable<NYTimesStory> loadStory(final String storyId) {
        return realm.where(NYTimesStory.class).equalTo(NYTimesStory.URL, storyId).findFirstAsync()
                .<NYTimesStory>asFlowable()
                .filter(story -> story.isLoaded());
    }

    /**
     * Closes all underlying resources used by the Repository.
     */
    @UiThread
    public void close() {
        realm.close();
    }

}
