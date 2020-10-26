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

import androidx.lifecycle.*
import io.realm.Realm
import io.realm.examples.coroutinesexample.model.Dog
import io.realm.examples.coroutinesexample.repository.RealmRepository
import io.realm.examples.coroutinesexample.repository.Repository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val dogs: LiveData<String> = getDogsInternal().map { it.size.toString() }

    private val realm = Realm.getDefaultInstance()
    private val repository: Repository = RealmRepository(realm)

    private lateinit var insertDogsJob: Job
    private lateinit var deleteAllDogsJob: Job

    override fun onCleared() {
        realm.close()
    }

    fun insertDogs(number: Int = 10) {
        insertDogsJob = viewModelScope.launch {
            repository.insertDogs(number)
        }
    }

    fun deleteAll() {
        deleteAllDogsJob = viewModelScope.launch {
            repository.deleteDogs()
        }
    }

    fun cancel() {
        if (this::insertDogsJob.isInitialized
                && insertDogsJob.isActive
                && !insertDogsJob.isCancelled) {
            insertDogsJob.cancel()
        }
        if (this::deleteAllDogsJob.isInitialized
                && deleteAllDogsJob.isActive
                && !deleteAllDogsJob.isCancelled) {
            deleteAllDogsJob.cancel()
        }
    }

    private fun getDogsInternal(): LiveData<List<Dog>> {
        return liveData {
            repository.getDogs()
                    .collect {
                        emit(it)
                    }
        }
    }
}
