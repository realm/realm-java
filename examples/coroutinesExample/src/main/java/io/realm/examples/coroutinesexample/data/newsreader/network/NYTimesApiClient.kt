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

package io.realm.examples.coroutinesexample.data.newsreader.network

import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTimesResponse
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

private const val API_KEY = "YUPmyj0Q09Fm2VlCHmD9FU7rpCcI5dUD"

interface NYTimesApiClient {
    suspend fun getTopStories(section: String): NYTimesResponse
}

class NYTimesApiClientImpl : NYTimesApiClient {

    private val service: NYTimesService

    init {
        // FIXME: inject retrofit instead
        service = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .baseUrl("https://api.nytimes.com/")
                .build()
                .create<NYTimesService>(NYTimesService::class.java)
    }

    override suspend fun getTopStories(section: String): NYTimesResponse {
        return service.topStories(section, API_KEY)
    }
}
