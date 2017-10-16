/*
 * Copyright 2016 Realm Inc.
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

package io.realm.examples.newsreader.model.network;


import java.util.List;

import io.reactivex.Observable;
import io.realm.examples.newsreader.model.entity.NYTimesStory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit interface for the New York Times WebService
 */
public interface NYTimesService {
    @GET("svc/topstories/v1/{section}.json")
    Observable<NYTimesResponse<List<NYTimesStory>>> topStories(
            @Path("section") String section,
            @Query(value = "api-key", encoded = true) String apiKey);
}