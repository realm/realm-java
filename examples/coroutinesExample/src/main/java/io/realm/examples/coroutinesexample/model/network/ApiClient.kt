package io.realm.examples.coroutinesexample.model.network

import io.realm.examples.coroutinesexample.model.Dog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ApiClient {
    fun getDogs(): Flow<List<Dog>>
}

class FakeApiClient : ApiClient {
    override fun getDogs(): Flow<List<Dog>> {
        return flowOf(
                (1..5).map { i ->
                    Dog().apply {
                        name = "Mr. Snuffles $i"
                        age = i
                        owner = "Mortimer Smith"
                    }
                }
        )
    }
}
