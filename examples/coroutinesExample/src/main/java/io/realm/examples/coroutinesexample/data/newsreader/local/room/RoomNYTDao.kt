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

package io.realm.examples.coroutinesexample.data.newsreader.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomNYTDao {
    @Insert
    suspend fun insertArticles(articles: List<RoomNYTimesArticle>)

    @Query("DELETE FROM article WHERE apiSection = :apiSection")
    suspend fun deleteArticles(apiSection: String)

    @Query("DELETE FROM article")
    suspend fun deleteAllArticles()

    @Query("SELECT * FROM article WHERE apiSection = :section")
    fun getArticles(section: String): Flow<List<RoomNYTimesArticle>>

    @Query("SELECT * FROM article WHERE apiSection = :section")
    fun getArticlesBlocking(section: String): List<RoomNYTimesArticle>
}
