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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.*
import io.realm.RealmConfiguration
import io.realm.examples.coroutinesexample.TAG
import io.realm.examples.coroutinesexample.model.Dog
import io.realm.examples.coroutinesexample.model.entity.DogMapper.toDogFlow
import io.realm.examples.coroutinesexample.model.entity.DogMapper.toDomainDogFlow
import io.realm.examples.coroutinesexample.model.network.ApiClient
import io.realm.examples.coroutinesexample.model.network.FakeApiClient
import io.realm.examples.coroutinesexample.repository.RealmDao
import io.realm.examples.coroutinesexample.repository.RealmDaoImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalStoreApi
@ExperimentalCoroutinesApi
@FlowPreview
class MainViewModel : ViewModel() {

    private val store: Store<Unit, List<DomainDog>>

    private val repository: RealmDao = RealmDaoImpl(RealmConfiguration.Builder().build())
    private val apiClient: ApiClient = FakeApiClient()

    private val _dogs = MutableLiveData<List<DomainDog>>().apply { value = listOf() }
    val dogs: LiveData<List<DomainDog>>
        get() = _dogs

    init {
        val fetcher: Fetcher<Unit, List<Dog>> = Fetcher.ofFlow {
            apiClient.getDogs().toDogFlow()
        }

        val sourceOfTruth: SourceOfTruth<Unit, List<Dog>, List<DomainDog>> = SourceOfTruth.of(
                reader = {
                    repository.getDogs().toDomainDogFlow()
                },
                writer = { _, dogs ->
                    repository.insertDogs(dogs)
                },
                deleteAll = {
                    repository.deleteDogs()
                }
        )

        // Create store instance and start listening for stream changes
        store = StoreBuilder
                .from(fetcher, sourceOfTruth)
                .build()
                .also { setupStoreStream(it) }
    }

    override fun onCleared() {
        repository.close()
    }

    fun refreshDogs() {
        viewModelScope.launch {
            store.fresh(Unit)
        }
    }

    private fun setupStoreStream(store: Store<Unit, List<DomainDog>>) {
        store.stream(StoreRequest.cached(
                key = Unit,
                refresh = true
        )).onEach { response ->
            // React to responses from the store
            when (response) {
                is StoreResponse.Loading ->
                    Log.d(TAG, "--- loading...")
                is StoreResponse.Data -> {
                    Log.d(TAG, "--- origin: ${response.origin}")
                    _dogs.postValue(response.value)
                }
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
