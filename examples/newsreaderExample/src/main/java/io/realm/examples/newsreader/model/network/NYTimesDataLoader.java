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

package io.realm.examples.newsreader.model.network;

import android.support.annotation.NonNull;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.realm.Realm;
import io.realm.examples.newsreader.model.entity.NYTimesStory;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Class that handles network requests for the New York Times API
 */
public class NYTimesDataLoader {

    private NYTimesService nyTimesService;
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZZZZZ", Locale.US);
    private SimpleDateFormat outputDateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
    private String apiKey;
    private Realm realm;
    private BehaviorSubject<Boolean> networkInUse;

    public NYTimesDataLoader() {
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl("http://api.nytimes.com/")
                .build();
        nyTimesService = retrofit.create(NYTimesService.class);
    }

    public void loadData(String sectionKey, String apiKey, Realm realm, BehaviorSubject<Boolean> networkLoading) {
        this.apiKey = apiKey;
        this.realm = realm;
        this.networkInUse = networkLoading;
        loadNextSection(sectionKey);
    }

    // Load all sections one by one.
    private void loadNextSection(@NonNull final String sectionKey) {
        networkInUse.onNext(true);
        nyTimesService.topStories(sectionKey, apiKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    Timber.d("Success - Data received: %s", sectionKey);
                    processAndAddData(realm, response.section, response.results);
                    networkInUse.onNext(false);
                }, throwable -> {
                    networkInUse.onNext(false);
                    Timber.d("Failure: Data not loaded: %s - %s", sectionKey, throwable.toString());
                });
    }

    // Converts data into a usable format and save it to Realm
    private void processAndAddData(final Realm realm, final String sectionKey, final List<NYTimesStory> stories) {
        if (stories.isEmpty()) return;

        realm.executeTransactionAsync(r -> {
            for (NYTimesStory story : stories) {
                Date parsedPublishedDate = inputDateFormat.parse(story.getPublishedDate(), new ParsePosition(0));
                story.setSortTimeStamp(parsedPublishedDate.getTime());
                story.setPublishedDate(outputDateFormat.format(parsedPublishedDate));

                // Find existing story in Realm (if any)
                // If it exists, we need to merge the local state with the remote, because the local state
                // contains more info than is available on the server.
                NYTimesStory persistedStory = r.where(NYTimesStory.class).equalTo(NYTimesStory.URL, story.getUrl()).findFirst();
                if (persistedStory != null) {
                    // Only local state is the `read` boolean.
                    story.setRead(persistedStory.isRead());
                }

                // Only create or update the local story if needed
                if (persistedStory == null || !persistedStory.getUpdatedDate().equals(story.getUpdatedDate())) {
                    story.setApiSection(sectionKey);
                    r.copyToRealmOrUpdate(story);
                }
            }
        }, throwable -> Timber.e(throwable, "Could not save data"));
    }
}