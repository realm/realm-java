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

package io.realm.examples.coroutinesexample.data.newsreader.network.model

import com.squareup.moshi.Json

data class NYTimesResponse(
        val status: String,
        val copyright: String,
        val section: String,
        @field:Json(name = "last_updated") val lastUpdated: String,
        @field:Json(name = "num_results") val numResults: Int,
        val results: List<NYTimesArticle>
)

data class NYTimesArticle(
        val section: String,
        val subsection: String,
        val title: String,
        @field:Json(name = "abstract") val abstractText: String?,
        val url: String,
        val uri: String,
        val byline: String,
        @field:Json(name = "item_type") val itemType: String?,
        @field:Json(name = "updated_date") val updatedDate: String?,
        @field:Json(name = "created_date") val createDate: String?,
        @field:Json(name = "published_date") val publishedDate: String?,
        @field:Json(name = "material_type_facet") val materialTypeFacet: String?,
        val kicker: String,
        @field:Json(name = "des_facet") val desFacet: List<String>?,
        @field:Json(name = "org_facet") val orgFacet: List<String>?,
        @field:Json(name = "per_facet") val perFacet: List<String>?,
        @field:Json(name = "geo_facet") val geoFacet: List<String>?,
        val multimedia: List<NYTMultimedium>,
        @field:Json(name = "short_url") val shortUrl: String?
)

data class NYTMultimedium(
        val url: String,
        val format: String,
        val height: Int,
        val width: Int,
        val type: String,
        val subtype: String,
        val caption: String,
        val copyright: String
)
