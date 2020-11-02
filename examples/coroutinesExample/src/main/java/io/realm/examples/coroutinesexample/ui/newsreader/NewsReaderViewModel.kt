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

package io.realm.examples.coroutinesexample.ui.newsreader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.*
import io.realm.RealmConfiguration
import io.realm.examples.coroutinesexample.data.newsreader.local.NYTDao
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTDao
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTimesArticle
import io.realm.examples.coroutinesexample.data.newsreader.network.NYTimesApiClient
import io.realm.examples.coroutinesexample.data.newsreader.network.NYTimesApiClientImpl
import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTimesArticle
import io.realm.examples.coroutinesexample.domain.newsreader.NYTMapper.toRealmArticles
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalStoreApi
@ExperimentalCoroutinesApi
@FlowPreview
class NewsReaderViewModel : ViewModel() {

    private val store: Store<String, List<RealmNYTimesArticle>>

    // FIXME: inject RealmConfiguration
    private val nytDao: NYTDao = RealmNYTDao(RealmConfiguration.Builder().build())
    private val nytApiClient: NYTimesApiClient = NYTimesApiClientImpl()

    private val _articles = MutableLiveData<List<NYTimesArticle>>().apply { value = listOf() }
    val articles: LiveData<List<NYTimesArticle>>
        get() = _articles

    init {
        val fetcher: Fetcher<String, List<RealmNYTimesArticle>> = Fetcher.of { section ->
            nytApiClient.getTopStories(section).results.toRealmArticles()
        }

        val sourceOfTruth: SourceOfTruth<String, List<RealmNYTimesArticle>, List<RealmNYTimesArticle>> = SourceOfTruth.Companion.of(
                reader = { section ->
                    nytDao.getArticles(section)
                },
                writer = { section, articles ->
                    nytDao.insertArticles(articles)
                },
                deleteAll = {
                    nytDao.deleteArticles()
                }
        )

        store = StoreBuilder.from(fetcher, sourceOfTruth)
                .build()
                .also { setupStoreStream(it) }
    }

    override fun onCleared() {
        nytDao.close()
    }

    fun refreshTopStories() {
        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                nytApiClient.getTopStories("home")
//            }.also { response ->
//                response.results
//            }

            store.fresh("home")
        }
    }

    private fun setupStoreStream(store: Store<String, List<RealmNYTimesArticle>>) {
        store.stream(StoreRequest.cached(
                key = "home",
                refresh = true
        )).onEach { response ->
            val kjashdkhj = 0
        }.launchIn(viewModelScope)
    }
}

// list:        title
// details:     abstract
