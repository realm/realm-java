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

package io.realm.examples.coroutinesexample.ui.dog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.android.external.store4.*
import io.realm.RealmConfiguration
import io.realm.examples.coroutinesexample.data.local.DogDao
import io.realm.examples.coroutinesexample.data.local.RealmDogDao
import io.realm.examples.coroutinesexample.data.local.model.Dog
import io.realm.examples.coroutinesexample.data.network.DogApiClient
import io.realm.examples.coroutinesexample.data.network.FakeDogApiClient
import io.realm.examples.coroutinesexample.domain.DogMapper.toDogFlow
import io.realm.examples.coroutinesexample.domain.DogMapper.toDomainDogFlow
import io.realm.examples.coroutinesexample.domain.model.DomainDog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalStoreApi
@ExperimentalCoroutinesApi
@FlowPreview
class DogViewModel : ViewModel() {

    private val store: Store<Unit, List<DomainDog>>

    private val dogDao: DogDao = RealmDogDao(RealmConfiguration.Builder().build())
    private val dogApiClient: DogApiClient = FakeDogApiClient()

    private val _storeResponse = MutableLiveData<StoreResponse<List<DomainDog>>>()
    val storeResponse: LiveData<StoreResponse<List<DomainDog>>>
        get() = _storeResponse

    init {
        val fetcher: Fetcher<Unit, List<Dog>> = Fetcher.ofFlow {
            dogApiClient.getDogs().toDogFlow()
        }

        val sourceOfTruth: SourceOfTruth<Unit, List<Dog>, List<DomainDog>> = SourceOfTruth.of(
                reader = {
                    dogDao.getDogs().toDomainDogFlow()
                },
                writer = { _, dogs ->
                    dogDao.insertDogs(dogs)
                },
                deleteAll = {
                    dogDao.deleteDogs()
                }
        )

        // Create store instance and start listening for stream changes
        store = StoreBuilder
                .from(fetcher, sourceOfTruth)
                .build()
                .also { setupStoreStream(it) }
    }

    override fun onCleared() {
        dogDao.close()
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
            _storeResponse.postValue(response)
        }.launchIn(viewModelScope)
    }
}
