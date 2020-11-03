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

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "article")
data class RoomNYTimesArticle (
        var url: String = "",
        var apiSection: String = "",
        var section: String = "",
        var subsection: String? = null,
        @PrimaryKey var title: String = "",
        var abstract: String = ""
)
