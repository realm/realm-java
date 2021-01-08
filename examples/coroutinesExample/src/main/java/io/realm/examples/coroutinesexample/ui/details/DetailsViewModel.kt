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

package io.realm.examples.coroutinesexample.ui.details

import androidx.lifecycle.*
import io.realm.examples.coroutinesexample.data.newsreader.local.RealmNYTimesArticle
import io.realm.examples.coroutinesexample.di.DependencyGraph
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
class DetailsViewModel : ViewModel() {

    private val repository = DependencyGraph.provideNewsReaderRepository()

    private val article = MutableLiveData<RealmNYTimesArticle>()

    private val _read = MutableLiveData<Boolean>()
    val read: LiveData<Boolean>
        get() = _read

    val date = article.map { it.updatedDate.toString() }
    val title = article.map { it.title }
    val articleText = article.map { it.abstractText }

    override fun onCleared() {
        repository.close()
    }

    fun loadDetails(id: String) {
        repository.getStory(id)
                .onEach { realmArticle ->
                    checkNotNull(realmArticle)
                            .also {
                                if (article.value == null) {
                                    article.postValue(it)

                                    if (!it.read) {
                                        markAsRead(it)
                                    } else {
                                        markAsRead(it, true)
                                    }
                                }
                            }
                }.launchIn(viewModelScope)
    }

    private fun markAsRead(article: RealmNYTimesArticle, immediately: Boolean = false) {
        if (immediately) {
            _read.postValue(true)
        } else {
            flow<Unit> {
                delay(2.seconds)
                repository.updateArticle(viewModelScope, article.url)
                _read.postValue(true)
            }.launchIn(viewModelScope)
        }
    }
}
