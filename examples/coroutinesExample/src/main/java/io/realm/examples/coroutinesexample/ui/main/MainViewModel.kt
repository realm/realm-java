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

package io.realm.examples.coroutinesexample.ui.main

import android.util.Log
import androidx.lifecycle.*
import com.dropbox.android.external.store4.*
import io.realm.RealmConfiguration
import io.realm.examples.coroutinesexample.TAG
import io.realm.examples.coroutinesexample.model.Dog
import io.realm.examples.coroutinesexample.model.network.ApiClient
import io.realm.examples.coroutinesexample.model.network.FakeApiClient
import io.realm.examples.coroutinesexample.repository.RealmDaoImpl
import io.realm.examples.coroutinesexample.repository.RealmDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalStoreApi
@ExperimentalCoroutinesApi
@FlowPreview
class MainViewModel : ViewModel() {

    private var store: Store<Unit, List<Dog>>

    private val repository: RealmDao = RealmDaoImpl(RealmConfiguration.Builder().build())
    private val apiClient: ApiClient = FakeApiClient()

    init {
        val fetcher: Fetcher<Unit, List<Dog>> = Fetcher.ofFlow {
            apiClient.getDogs()
        }

        val sourceOfTruth: SourceOfTruth<Unit, List<Dog>, List<Dog>> = SourceOfTruth.of(
                reader = { key ->
                       repository.getDogs()
                },
                writer = { key, dogs ->
                    repository.insertDogs(dogs)
                },
                deleteAll = {
                    repository.deleteDogs()
                }
        )

        store = StoreBuilder
                .from(fetcher, sourceOfTruth)
                .build()
                .also { setupStoreStream(it) }
    }

    override fun onCleared() {
        repository.close()
    }

    fun getDogs() {
        viewModelScope.launch {
            store.get(Unit)
        }
    }

    fun refreshDogs() {
        viewModelScope.launch {
            store.fresh(Unit)
        }
    }

    fun deleteDogs() {
        viewModelScope.launch {
            store.clearAll()
        }
    }

    private fun setupStoreStream(store: Store<Unit, List<Dog>>) {
        store.stream(StoreRequest.cached(
                key = Unit,
                refresh = true
        )).onEach { response ->
            when (response) {
                is StoreResponse.Loading ->
                    Log.d(TAG, "--- loading...")
                is StoreResponse.Data ->
                    Log.d(TAG, "--- origin: ${response.origin}")
                is StoreResponse.NoNewData ->
                    Log.d(TAG, "--- no new data")
                is StoreResponse.Error.Exception -> {
                    val stacktrace = response.error.cause?.stackTrace?.joinToString {
                        "$it\n"
                    }
                    Log.e(TAG, "--- error (exception): ${response.error.message} - ${response.error.cause?.message}: $stacktrace")
                }
                is StoreResponse.Error.Message ->
                    Log.e(TAG, "--- error (message): ${response.message}")
            }
        }.launchIn(viewModelScope)
    }
}
