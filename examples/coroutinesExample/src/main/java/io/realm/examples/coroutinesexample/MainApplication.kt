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

package io.realm.examples.coroutinesexample

import androidx.multidex.MultiDexApplication
import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTDao
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTDaoImpl
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTimesArticle
import io.realm.examples.coroutinesexample.data.newsreader.local.insertArticles
import io.realm.examples.coroutinesexample.data.newsreader.network.NYTimesApiClient
import io.realm.examples.coroutinesexample.data.newsreader.network.NYTimesApiClientImpl
import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTimesArticle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map

const val TAG = "--- CoroutinesExample"

class MainApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}

object DependencyGraph {

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun provideDataStore(
            fetcher: Fetcher<String, List<NYTimesArticle>>,
            sourceOfTruth: SourceOfTruth<String, List<NYTimesArticle>, List<RealmNYTimesArticle>>
    ): Store<String, List<RealmNYTimesArticle>> {
        return StoreBuilder.from(fetcher, sourceOfTruth)
                .build()
    }

    private fun provideFetcher(nytApiClient: NYTimesApiClient): Fetcher<String, List<NYTimesArticle>> {
        return Fetcher.of { apiSection ->
            nytApiClient.getTopStories(apiSection).results
        }
    }

    private fun provideSourceOfTruth(realmDao: RealmNYTDao): SourceOfTruth<String, List<NYTimesArticle>, List<RealmNYTimesArticle>> {
        return SourceOfTruth.of(
                reader = { apiSection ->
                    realmDao.getArticles(apiSection).map { articles ->
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
    }

    // Database dependencies
    private fun provideRealmDao(): RealmNYTDao {
        return RealmNYTDaoImpl(provideRealmConfig())
    }

    private fun provideRealmConfig(): RealmConfiguration {
        return RealmConfiguration.Builder().build()
    }

    // Network dependencies
    private fun provideApiClient(): NYTimesApiClient {
        return NYTimesApiClientImpl()
    }
}
