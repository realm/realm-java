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
import io.realm.examples.coroutinesexample.data.newsreader.network.model.NYTimesArticle
import io.realm.examples.coroutinesexample.util.runCloseableTransaction
import io.realm.kotlin.toFlow
import io.realm.kotlin.where
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.util.concurrent.Executors

interface NYTDao : Closeable {
    suspend fun insertArticles(articles: List<RealmNYTimesArticle>)
    suspend fun deleteArticles()
    fun getArticles(section: String): Flow<List<RealmNYTimesArticle>>
    fun countArticles(): Long
    fun countArticles(section: String): Long
}

class RealmNYTDao(
        private val realmConfiguration: RealmConfiguration
) : NYTDao {

    private val monoThreadDispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
    private val closeableRealm = Realm.getInstance(realmConfiguration)

    override suspend fun insertArticles(articles: List<RealmNYTimesArticle>) {
        withContext(monoThreadDispatcher) {
            runCloseableTransaction(realmConfiguration) { transactionRealm ->
                transactionRealm.insertOrUpdate(articles)
            }
        }
    }

    override suspend fun deleteArticles() {
        withContext(monoThreadDispatcher) {
            runCloseableTransaction(realmConfiguration) { transactionRealm ->
                transactionRealm.deleteAll()
            }
        }
    }

    override fun getArticles(section: String): Flow<List<RealmNYTimesArticle>> {
        return closeableRealm.where(RealmNYTimesArticle::class.java)
                .findAllAsync()
                .toFlow()
    }

    override fun countArticles(): Long =
            closeableRealm.where<RealmNYTimesArticle>().count()

    override fun countArticles(section: String): Long =
            closeableRealm.where<RealmNYTimesArticle>()
                    .equalTo("section", section)
                    .count()

    override fun close() {
        closeableRealm.close()
    }
}
