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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.examples.coroutinesexample.TAG
import io.realm.examples.coroutinesexample.di.DependencyGraph
import kotlinx.coroutines.launch

class RealmNewsReaderViewModel : ViewModel() {

    private val repository = DependencyGraph.provideNewsReaderRepository(viewModelScope)

    val newsReaderState: LiveData<NewsReaderState>
        get() = repository.newsReaderState

    override fun onCleared() {
        repository.close()
    }

    fun getTopStories(apiSection: String, refresh: Boolean = false) {
        Log.d(TAG, "------ apiSection: $apiSection - refresh '$refresh'")
        viewModelScope.launch {
            repository.getTopStories(apiSection, refresh)
        }
    }
}
