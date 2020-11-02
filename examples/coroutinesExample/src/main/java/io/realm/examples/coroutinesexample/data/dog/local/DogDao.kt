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

package io.realm.examples.coroutinesexample.data.dog.local

import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.examples.coroutinesexample.TAG
import io.realm.examples.coroutinesexample.data.dog.local.model.Dog
import io.realm.examples.coroutinesexample.util.runCloseableTransaction
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.toFlow
import io.realm.kotlin.where
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.util.concurrent.Executors

interface DogDao : Closeable {
    suspend fun insertDogs(dogs: List<Dog>)
    suspend fun deleteDogs()
    fun getDogs(): Flow<List<Dog>>
    fun countDogs(): Long
}

class RealmDogDao(
        private val realmConfiguration: RealmConfiguration
) : DogDao {

    private val monoThreadDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
    private val closeableRealm = Realm.getInstance(realmConfiguration)

    override suspend fun insertDogs(dogs: List<Dog>) {
        withContext(monoThreadDispatcher) {
            runCloseableTransaction(realmConfiguration) { transactionRealm ->
                transactionRealm.insertOrUpdate(dogs)
            }
        }
    }

    override suspend fun deleteDogs() {
        withContext(monoThreadDispatcher) {
            runCloseableTransaction(realmConfiguration) { transactionRealm ->
                transactionRealm.deleteAll()
            }
        }
    }

    override fun getDogs(): Flow<List<Dog>> {
        return closeableRealm.where(Dog::class.java)
                .findAllAsync()
                .toFlow()
    }

    override fun countDogs(): Long = closeableRealm.where<Dog>().count()

    override fun close() {
        closeableRealm.close()
    }
}
