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
import io.realm.RealmResults
import io.realm.examples.coroutinesexample.util.runCloseableTransaction
import io.realm.kotlin.toFlow
import io.realm.kotlin.where
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.util.concurrent.Executors

interface RealmNYTDao : Closeable {
    suspend fun insertArticles(articles: List<RealmNYTimesArticle>)
    suspend fun deleteArticles(section: String)
    suspend fun deleteAllArticles()
    fun getArticlesBlocking(section: String) : RealmResults<RealmNYTimesArticle>
    fun getArticles(section: String): Flow<List<RealmNYTimesArticle>>
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

    override fun getArticlesBlocking(section: String) : RealmResults<RealmNYTimesArticle> {
        return closeableRealm.where<RealmNYTimesArticle>()
                .equalTo(RealmNYTimesArticle.COLUMN_API_SECTION, section)
                .findAllAsync()
    }

    override fun getArticles(section: String): Flow<List<RealmNYTimesArticle>> {
        return closeableRealm.where<RealmNYTimesArticle>()
                .equalTo(RealmNYTimesArticle.COLUMN_API_SECTION, section)
                .findAllAsync()
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

