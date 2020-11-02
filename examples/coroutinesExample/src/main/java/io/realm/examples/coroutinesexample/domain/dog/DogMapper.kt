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

package io.realm.examples.coroutinesexample.domain.dog

import io.realm.examples.coroutinesexample.data.dog.local.model.Dog
import io.realm.examples.coroutinesexample.data.dog.network.model.ApiDog
import io.realm.examples.coroutinesexample.domain.dog.model.DomainDog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DogMapper {

    fun Flow<List<ApiDog>>.toDogFlow(): Flow<List<Dog>> = map { apiDogList ->
        apiDogList.map { apiDog ->
            apiDog.toDog()
        }
    }

    private fun ApiDog.toDog(): Dog = Dog().also {
        it.name = name
        it.age = age
        it.owner = owner
    }

    fun Flow<List<Dog>>.toDomainDogFlow(): Flow<List<DomainDog>> = map { dogList ->
        dogList.map { dog ->
            dog.toDomainDog()
        }.sortedBy {
            it.age
        }
    }

    private fun Dog.toDomainDog(): DomainDog = DomainDog(
            name = name,
            age = age
    )
}
