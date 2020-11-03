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
import kotlinx.coroutines.flow.Flow
import okhttp3.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*
import kotlin.Comparator

interface NYTimesService {
    // FIXME: add API_KEY using an interceptor
    @GET("svc/topstories/v2/{section}.json")
    suspend fun topStories(
            @Path("section") section: String,
            @Query(value = "api-key", encoded = true) apiKey: String
    ): NYTimesResponse
}

val sectionsToNames = mapOf(
        "home" to "Home",
        "world" to "World",
        "national" to "National",
        "politics" to "Politics",
        "nyregion" to "NY Region",
        "business" to "Business",
        "opinion" to "Opinion",
        "technology" to "Technology",
        "science" to "Science",
        "health" to "Health",
        "sports" to "Sports",
        "arts" to "Arts",
        "fashion" to "Fashion",
        "dining" to "Dining",
        "travel" to "Travel",
        "magazine" to "Magazine",
        "realestate" to "Real Estate"
).toSortedMap(
        Comparator { o1, o2 ->
            if (o1.toLowerCase(Locale.ROOT) == "home") return@Comparator -1
            if (o2.toLowerCase(Locale.ROOT) == "home") return@Comparator 1
            return@Comparator o1.compareTo(o2, ignoreCase = true)
        }
)
