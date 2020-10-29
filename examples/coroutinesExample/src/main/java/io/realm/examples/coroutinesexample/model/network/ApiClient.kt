package io.realm.examples.coroutinesexample.model.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ApiClient {
    fun getDogs(): Flow<List<ApiDog>>
}

class FakeApiClient : ApiClient {
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
