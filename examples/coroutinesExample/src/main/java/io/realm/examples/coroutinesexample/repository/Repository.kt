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

package io.realm.examples.coroutinesexample.repository

import android.util.Log
import io.realm.Realm
import io.realm.examples.coroutinesexample.TAG
import io.realm.examples.coroutinesexample.model.Dog
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.toFlow
import io.realm.kotlin.where
import kotlinx.coroutines.flow.Flow

interface Repository {
    suspend fun insertDogs(number: Int)
    suspend fun deleteDogs()
    fun getDogs(): Flow<List<Dog>>
    fun countDogs(): Long
}

class RealmRepository(private val realm: Realm) : Repository {

    override suspend fun insertDogs(number: Int) {
        realm.executeTransactionAwait { transactionRealm ->
            Log.d(TAG, "--- BEFORE")
            for (i in 1..number) {
                transactionRealm.insertOrUpdate(Dog().apply {
                    name = "Mr. Snuffles $i"
                    age = 1
                    owner = "Mortimer Smith"
                })
            }
            Log.d(TAG, "--- AFTER")
        }
    }

    override suspend fun deleteDogs() {
        realm.executeTransactionAwait { transactionRealm ->
            transactionRealm.deleteAll()
        }
    }

    override fun getDogs(): Flow<List<Dog>> = realm.where(Dog::class.java)
            .findAllAsync()
            .toFlow()

    override fun countDogs(): Long = realm.where<Dog>().count()
}
