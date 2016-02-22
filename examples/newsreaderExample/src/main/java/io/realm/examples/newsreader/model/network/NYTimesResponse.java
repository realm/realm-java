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

import com.fasterxml.jackson.annotation.JsonProperty;

public class NYTimesResponse<T> {

    @JsonProperty("status")
    public String status;

    @JsonProperty("copyright")
    public String copyright;

    @JsonProperty("section")
    public String section;

    @JsonProperty("last_updated")
    public String lastUpdated;

    @JsonProperty("num_results")
    public Integer numResults;

    @JsonProperty("results")
    public T results;
}