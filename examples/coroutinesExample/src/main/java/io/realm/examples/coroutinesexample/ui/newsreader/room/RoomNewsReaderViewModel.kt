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

package io.realm.examples.coroutinesexample.ui.newsreader.room

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.*
import io.realm.examples.coroutinesexample.MainApplication
import io.realm.examples.coroutinesexample.TAG
import io.realm.examples.coroutinesexample.data.newsreader.local.room.RoomNYTDao
import io.realm.examples.coroutinesexample.data.newsreader.local.room.RoomNYTimesArticle
import io.realm.examples.coroutinesexample.data.newsreader.local.room.insertArticles
import io.realm.examples.coroutinesexample.data.newsreader.network.NYTimesApiClient
import io.realm.examples.coroutinesexample.data.newsreader.network.NYTimesApiClientImpl
import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTimesArticle
import io.realm.examples.coroutinesexample.ui.newsreader.realm.RealmNewsReaderState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@ExperimentalStoreApi
@ExperimentalCoroutinesApi
@FlowPreview
class RoomNewsReaderViewModel : ViewModel() {

    private val dao: RoomNYTDao = MainApplication.ROOM_DB.nytDao()
    private val store: Store<String, List<RoomNYTimesArticle>>

    private val nytApiClient: NYTimesApiClient = NYTimesApiClientImpl()

    private val _newsReaderState = MutableLiveData<RoomNewsReaderState>()
    val newsReaderState: LiveData<RoomNewsReaderState>
        get() = _newsReaderState

    private val sectionRefreshJobs = mutableMapOf<String, Job>()

    init {
        val fetcher: Fetcher<String, List<NYTimesArticle>> = Fetcher.of { apiSection ->
            nytApiClient.getTopStories(apiSection).results
        }

        val sourceOfTruth: SourceOfTruth<String, List<NYTimesArticle>, List<RoomNYTimesArticle>> = SourceOfTruth.of(
                reader = { apiSection ->
                    dao.getArticles(apiSection).map { articles ->
                        if (articles.isEmpty()) null
                        else articles
                    }
                },
                writer = { apiSection, articles ->
                    dao.insertArticles(apiSection, articles)
                },
                delete = { apiSection ->
                    dao.deleteArticles(apiSection)
                },
                deleteAll = {
                    dao.deleteAllArticles()
                }
        )

        store = StoreBuilder.from(fetcher, sourceOfTruth)
                .build()
    }

    fun getTopStories(apiSection: String, refresh: Boolean = false) {
        Log.d(TAG, "--- apiSection: $apiSection - refresh '$refresh'")
        viewModelScope.launch {
            if (refresh) {
                store.fresh(apiSection)
            } else {
                if (sectionRefreshJobs[apiSection] != null) {
                    getFromCache(apiSection)
                } else {
                    getFromStream(apiSection)
                }
            }
        }
    }

    private suspend fun getFromCache(apiSection: String) {
        val cachedResults = store.get(apiSection)
        Log.d(TAG, "--- cached data, - '$apiSection': ${cachedResults.size}")
        _newsReaderState.postValue(RoomNewsReaderState.Data("Cache", cachedResults))
    }

    private fun getFromStream(apiSection: String) {
        store.stream(StoreRequest.cached(
                key = apiSection,
                refresh = false
        )).onEach { response ->
            val origin = response.origin.toString()
            when (response) {
                is StoreResponse.Loading -> {
                    Log.d(TAG, "--- response origin: ${response.origin} - Loading '$apiSection'")
                    RoomNewsReaderState.Loading(origin)
                }
                is StoreResponse.Data -> {
                    Log.d(TAG, "--- response origin: ${response.origin} - Data '$apiSection': ${response.value.size}")
                    RoomNewsReaderState.Data(origin, response.value)
                }
                is StoreResponse.NoNewData -> {
                    Log.d(TAG, "--- response origin: ${response.origin} - NoNewData '$apiSection'")
                    RoomNewsReaderState.NoNewData(origin)
                }
                is StoreResponse.Error.Exception -> {
                    Log.e(TAG, "--- response origin: ${response.origin} - Error.Exception '$apiSection': ${response.error}")
                    RoomNewsReaderState.ErrorException(origin, response.error)
                }
                is StoreResponse.Error.Message -> {
                    Log.e(TAG, "--- response origin: ${response.origin} - Error.Message '$apiSection': ${response.message}")
                    RoomNewsReaderState.ErrorMessage(origin, response.message)
                }
            }.let {
                _newsReaderState.postValue(it)
            }
        }.launchIn(
                viewModelScope
        ).also { job ->
            sectionRefreshJobs[apiSection] = job
        }
    }
}
