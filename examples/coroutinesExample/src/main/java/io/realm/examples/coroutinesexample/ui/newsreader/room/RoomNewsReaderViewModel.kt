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

    private var currentJob: Job? = null

    init {
        val fetcher: Fetcher<String, List<NYTimesArticle>> = Fetcher.of { apiSection ->
            val apiResults = nytApiClient.getTopStories(apiSection).results
            apiResults
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

    fun getTopStories(apiSection: String = "home", refresh: Boolean = false) {
        viewModelScope.launch {
            // Cancel previous streams when selecting a different section
            currentJob?.cancelAndJoin()
            currentJob = store.stream(StoreRequest.cached(
                    key = apiSection,
                    refresh = true
            )).onEach { response ->
                Log.d(TAG, "--- response: $response")

                val origin = response.origin.toString()

                when (response) {
                    is StoreResponse.Loading -> RoomNewsReaderState.Loading(origin)
                    is StoreResponse.Data -> RoomNewsReaderState.Data(origin, response.value)
                    is StoreResponse.NoNewData -> RoomNewsReaderState.NoNewData(origin)
                    is StoreResponse.Error.Exception -> RoomNewsReaderState.ErrorException(origin, response.error)
                    is StoreResponse.Error.Message -> RoomNewsReaderState.ErrorMessage(origin, response.message)
                }.let {
                    _newsReaderState.postValue(it)
                }
            }.launchIn(viewModelScope)
        }
    }
}
