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

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.*
import io.realm.RealmConfiguration
import io.realm.examples.coroutinesexample.TAG
import io.realm.examples.coroutinesexample.data.newsreader.local.realm.RealmNYTDao
import io.realm.examples.coroutinesexample.data.newsreader.local.realm.RealmNYTDaoImpl
import io.realm.examples.coroutinesexample.data.newsreader.local.realm.RealmNYTimesArticle
import io.realm.examples.coroutinesexample.data.newsreader.local.realm.insertArticles
import io.realm.examples.coroutinesexample.data.newsreader.network.NYTimesApiClient
import io.realm.examples.coroutinesexample.data.newsreader.network.NYTimesApiClientImpl
import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTimesArticle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalStoreApi
@ExperimentalCoroutinesApi
@FlowPreview
class NewsReaderViewModel : ViewModel() {

    private val realmDao: RealmNYTDao = RealmNYTDaoImpl(RealmConfiguration.Builder().build())
    private val store: Store<String, List<RealmNYTimesArticle>>

    private val nytApiClient: NYTimesApiClient = NYTimesApiClientImpl()

    private val _newsReaderState = MutableLiveData<NewsReaderState>()
    val newsReaderState: LiveData<NewsReaderState>
        get() = _newsReaderState

    var section: String = "home"

    init {
        val fetcher: Fetcher<String, List<NYTimesArticle>> = Fetcher.of { apiSection ->
            nytApiClient.getTopStories(apiSection).results
        }

        val sourceOfTruth: SourceOfTruth<String, List<NYTimesArticle>, List<RealmNYTimesArticle>> = SourceOfTruth.of(
                reader = { apiSection ->
                    realmDao.getArticles(apiSection)
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

        store = StoreBuilder.from(fetcher, sourceOfTruth)
                .build()

        setupStoreStream()
    }

    override fun onCleared() {
        realmDao.close()
    }

    fun getTopStories(apiSection: String, refresh: Boolean = false) {
        viewModelScope.launch {
            if (section != apiSection) {
                section = apiSection
                store.fresh(section)
            } else {
                if (refresh) {
                    store.fresh(section)
                } else {
                    store.get(section)
                }
            }
        }
    }

    private fun setupStoreStream() {
        store.stream(StoreRequest.cached(
                key = section,
                refresh = true
        )).onEach { response ->
            Log.e(TAG, "--- response: $response")

            val origin = response.origin.toString()

            when (response) {
                is StoreResponse.Loading -> NewsReaderState.Loading(origin)
                is StoreResponse.Data -> NewsReaderState.Data(origin, response.value)
                is StoreResponse.NoNewData -> NewsReaderState.NoNewData(origin)
                is StoreResponse.Error.Exception -> NewsReaderState.ErrorException(origin, response.error)
                is StoreResponse.Error.Message -> NewsReaderState.ErrorMessage(origin, response.message)
            }.let {
                _newsReaderState.postValue(it)
            }
        }.launchIn(viewModelScope)
    }
}
