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

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTMultimedium
import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTimesArticle
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.toFlow
import io.realm.kotlin.where
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.util.concurrent.Executors

interface RealmNYTDao : Closeable {
    suspend fun insertArticles(articles: List<RealmNYTimesArticle>)
    suspend fun updateArticle(id: String)
    suspend fun deleteArticles(section: String)
    suspend fun deleteAllArticles()
    fun getArticlesBlocking(section: String): RealmResults<RealmNYTimesArticle>
    fun getArticles(section: String): Flow<List<RealmNYTimesArticle>>
    fun getArticleBlocking(id: String): RealmNYTimesArticle?
    fun getArticle(id: String): Flow<RealmNYTimesArticle?>
    fun countArticles(section: String): Long
}

class RealmNYTDaoImpl(
        private val realmConfiguration: RealmConfiguration
) : RealmNYTDao {

    private val monoThreadDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
    private val closeableRealm = Realm.getInstance(realmConfiguration)

    override suspend fun insertArticles(articles: List<RealmNYTimesArticle>) {
        withContext(monoThreadDispatcher) {
            runCloseableTransaction(realmConfiguration) { transactionRealm ->
                transactionRealm.insertOrUpdate(articles)
            }
        }
    }

    override suspend fun updateArticle(id: String) {
        withContext(monoThreadDispatcher) {
            runCloseableTransaction(realmConfiguration) { transactionRealm ->
                val article = transactionRealm.where<RealmNYTimesArticle>()
                        .equalTo(RealmNYTimesArticle.COLUMN_URL, id)
                        .findFirst()
                checkNotNull(article).read = true
            }
        }
    }

    override suspend fun deleteArticles(section: String) {
        withContext(monoThreadDispatcher) {
            runCloseableTransaction(realmConfiguration) { transactionRealm ->
                transactionRealm.deleteAll()
            }
        }
    }

    override suspend fun deleteAllArticles() {
        withContext(monoThreadDispatcher) {
            runCloseableTransaction(realmConfiguration) { transactionRealm ->
                transactionRealm.deleteAll()
            }
        }
    }

    override fun getArticlesBlocking(section: String): RealmResults<RealmNYTimesArticle> {
        return closeableRealm.where<RealmNYTimesArticle>()
                .equalTo(RealmNYTimesArticle.COLUMN_API_SECTION, section)
                .findAll()
    }

    override fun getArticles(section: String): Flow<List<RealmNYTimesArticle>> {
        return closeableRealm.where<RealmNYTimesArticle>()
                .equalTo(RealmNYTimesArticle.COLUMN_API_SECTION, section)
                .findAllAsync()
                .toFlow()
    }

    override fun getArticleBlocking(id: String): RealmNYTimesArticle? {
        return closeableRealm.where<RealmNYTimesArticle>()
                .equalTo(RealmNYTimesArticle.COLUMN_URL, id)
                .findFirst()
    }

    override fun getArticle(id: String): Flow<RealmNYTimesArticle?> {
        return closeableRealm.where<RealmNYTimesArticle>()
                .equalTo(RealmNYTimesArticle.COLUMN_URL, id)
                .findFirstAsync()
                .toFlow()
    }

    override fun countArticles(section: String): Long {
        return closeableRealm.where<RealmNYTimesArticle>()
                .equalTo(RealmNYTimesArticle.COLUMN_API_SECTION, section)
                .count()
    }

    override fun close() {
        closeableRealm.close()
    }
}

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
            abstractText = article.abstractText
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

private suspend fun runCloseableTransaction(
        realmConfiguration: RealmConfiguration,
        transaction: (realm: Realm) -> Unit
) {
    Realm.getInstance(realmConfiguration).use { realmInstance ->
        realmInstance.executeTransactionAwait(transaction = transaction)
    }
}
