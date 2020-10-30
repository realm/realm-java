package io.realm.examples.coroutinesexample.domain

import io.realm.examples.coroutinesexample.data.local.model.Dog
import io.realm.examples.coroutinesexample.data.network.model.ApiDog
import io.realm.examples.coroutinesexample.domain.model.DomainDog
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
