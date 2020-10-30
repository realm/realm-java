package io.realm.examples.coroutinesexample.data.network

import io.realm.examples.coroutinesexample.data.network.model.ApiDog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface DogApiClient {
    fun getDogs(): Flow<List<ApiDog>>
}

class FakeDogApiClient : DogApiClient {
    override fun getDogs(): Flow<List<ApiDog>> {
        return flowOf(
                (1..15).map { i ->
                    ApiDog(
                            name = "Mr. Snuffles $i",
                            age = i,
                            owner = "Mortimer Smith",
                            somethingElse = Unit
                    )
                }
        )
    }
}
