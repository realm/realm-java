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

package io.realm.examples.coroutinesexample.data.newsreader.local.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dropbox.android.external.store4.*
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTDao
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTimesArticle
import io.realm.examples.coroutinesexample.ui.main.NewsReaderState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class NewsReaderRepository(
        private val dao: RealmNYTDao,
        private val store: Store<String, List<RealmNYTimesArticle>>,
        private val scope: CoroutineScope
) {

    private val _newsReaderState = MutableLiveData<NewsReaderState>()
    val newsReaderState: LiveData<NewsReaderState>
        get() = _newsReaderState

    private val sectionRefreshJobs = mutableMapOf<String, Job>()

    fun getTopStories(apiSection: String, refresh: Boolean = false) {
        scope.launch {
            if (refresh) {
                store.fresh(apiSection)
            } else {
                getFromStream(apiSection)
            }
        }
    }

    fun close() {
        dao.close()
        sectionRefreshJobs.values.forEach { it.cancel() }
        sectionRefreshJobs.clear()
    }

    private fun getFromStream(apiSection: String) {
        store.stream(StoreRequest.cached(
                key = apiSection,
                refresh = false
        )).onEach { response ->
            val origin = response.origin.toString()
            when (response) {
                is StoreResponse.Loading -> NewsReaderState.Loading(origin)
                is StoreResponse.Data -> getNewsReaderState(response, store, apiSection, origin)
                is StoreResponse.NoNewData -> NewsReaderState.NoNewData(origin)
                is StoreResponse.Error.Exception -> NewsReaderState.ErrorException(origin, response.error)
                is StoreResponse.Error.Message -> NewsReaderState.ErrorMessage(origin, response.message)
            }.also {
                _newsReaderState.postValue(it)
            }
        }.launchIn(
                scope
        ).also { job ->
            scope.launch {
                sectionRefreshJobs.values.forEach { it.cancelAndJoin() }
                sectionRefreshJobs.clear()
                sectionRefreshJobs[apiSection] = job
            }
        }
    }

    companion object {

        private const val THIRTY_MINUTES = 30 * 60 * 1000

        private suspend fun getNewsReaderState(
                response: StoreResponse.Data<List<RealmNYTimesArticle>>,
                store: Store<String, List<RealmNYTimesArticle>>,
                apiSection: String,
                origin: String
        ): NewsReaderState {
            val data = response.value
            return if (data.isNotEmpty()) {
                data.first()
                        .let { firstElement ->
                            val now = System.currentTimeMillis()
                            val entryExpired = (firstElement.updateTime + THIRTY_MINUTES) < now

                            if (!(response.origin == ResponseOrigin.Fetcher || !entryExpired)) {
                                store.fresh(apiSection)
                                NewsReaderState.Loading(origin)
                            } else {
                                NewsReaderState.Data(origin, response.value)
                            }
                        }
            } else {
                NewsReaderState.Data(origin, response.value)
            }
        }
    }
}
