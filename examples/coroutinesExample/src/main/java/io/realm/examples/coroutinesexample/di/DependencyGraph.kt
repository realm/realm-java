/*
 * Copyright 2020 Realm Inc.
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

package io.realm.examples.coroutinesexample.di

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import io.realm.RealmConfiguration
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTDao
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTDaoImpl
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTimesArticle
import io.realm.examples.coroutinesexample.data.newsreader.local.insertArticles
import io.realm.examples.coroutinesexample.data.newsreader.local.repository.NewsReaderRepository
import io.realm.examples.coroutinesexample.data.newsreader.network.NYTimesApiClient
import io.realm.examples.coroutinesexample.data.newsreader.network.NYTimesApiClientImpl
import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTimesArticle
import io.realm.examples.coroutinesexample.util.NewsReaderFlowFactory
import kotlinx.coroutines.flow.map

/**
 * Homemade, simple DI solution - ideally, we should use a proper DI framework instead.
 */
object DependencyGraph {

    // Repository dependencies
    fun provideNewsReaderRepository(): NewsReaderRepository =
            NewsReaderRepository(provideRealmDao(), provideStore())

    private fun provideStore(): Store<String, List<RealmNYTimesArticle>> = StoreBuilder.from(
            fetcher = provideFetcher(provideApiClient()),
            sourceOfTruth = provideSourceOfTruth(provideRealmDao())
    ).build()

    private fun provideFetcher(nytApiClient: NYTimesApiClient): Fetcher<String, List<NYTimesArticle>> =
            Fetcher.of { apiSection ->
                nytApiClient.getTopStories(apiSection).results
            }

    private fun provideSourceOfTruth(realmDao: RealmNYTDao): SourceOfTruth<String, List<NYTimesArticle>, List<RealmNYTimesArticle>> =
            SourceOfTruth.of(
                    reader = { apiSection ->
                        realmDao.getArticles(apiSection)
                                .map { articles ->
                                    if (articles.isEmpty()) null
                                    else articles
                                }
                    },
                    writer = { apiSection, articles ->
                        realmDao.insertArticles(apiSection, articles)
                    },
                    delete = { apiSection ->
                        realmDao.deleteArticles(apiSection)
                    },
                    deleteAll = {
                        realmDao.deleteAllArticles()
                    }
            )

    // Database dependencies
    private fun provideRealmDao(): RealmNYTDao = RealmNYTDaoImpl(provideRealmConfig())

    private fun provideRealmConfig(): RealmConfiguration = RealmConfiguration.Builder()
            .flowFactory(NewsReaderFlowFactory())
            .build()

    // Network dependencies
    private fun provideApiClient(): NYTimesApiClient = NYTimesApiClientImpl()
}
