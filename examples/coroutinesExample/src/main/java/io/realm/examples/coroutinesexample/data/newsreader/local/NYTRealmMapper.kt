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

package io.realm.examples.coroutinesexample.data.newsreader.local

import io.realm.RealmList
import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTMultimedium
import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTimesArticle

suspend fun RealmNYTDao.insertArticles(apiSection: String, articles: List<NYTimesArticle>) {
    val realmArticles = articles.toRealmArticles(apiSection)
    insertArticles(realmArticles)
}

private fun List<NYTimesArticle>.toRealmArticles(apiQuerySection: String): List<RealmNYTimesArticle> {
    val timestamp = System.currentTimeMillis()
    return map { article ->
        RealmNYTimesArticle().apply {
            updateTime = timestamp
            apiSection = apiQuerySection
            section = article.section
            subsection = article.subsection
            title = article.title
            abstract = article.abstract
            url = article.url
            uri = article.uri
            byline = article.byline
            itemType = article.itemType
            updatedDate = article.updatedDate
            createDate = article.createDate
            publishedDate = article.publishedDate
            materialTypeFacet = article.materialTypeFacet
            kicker = article.kicker
            desFacet = RealmList<String>().apply { addAll(article.desFacet ?: listOf()) }
            orgFacet = RealmList<String>().apply { addAll(article.orgFacet ?: listOf()) }
            perFacet = RealmList<String>().apply { addAll(article.perFacet ?: listOf()) }
            geoFacet = RealmList<String>().apply { addAll(article.geoFacet ?: listOf()) }
            orgFacet = RealmList<String>().apply { addAll(article.orgFacet ?: listOf()) }
            perFacet = RealmList<String>().apply { addAll(article.perFacet ?: listOf()) }
            geoFacet = RealmList<String>().apply { addAll(article.geoFacet ?: listOf()) }
            multimedia = article.multimedia.toRealmMultimediumRealmList()
            shortUrl = article.shortUrl
        }
    }
}

private fun List<NYTMultimedium>?.toRealmMultimediumRealmList(): RealmList<RealmNYTMultimedium> {
    return RealmList<RealmNYTMultimedium>().also { realmList ->
        realmList.addAll(
                this?.map { multimedium ->
                    multimedium.toRealmMultimedium()
                } ?: listOf()
        )
    }
}

private fun NYTMultimedium.toRealmMultimedium(): RealmNYTMultimedium {
    return RealmNYTMultimedium().also {
        it.url = url
        it.format = format
        it.height = height
        it.width = width
        it.type = type
        it.subtype = subtype
        it.caption = caption
        it.copyright = copyright
    }
}
