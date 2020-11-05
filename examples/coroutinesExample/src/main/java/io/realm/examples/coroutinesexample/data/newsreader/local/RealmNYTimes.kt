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
import io.realm.RealmObject
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

open class RealmNYTimesArticle : RealmObject() {
    var apiSection: String = ""
    var section: String = ""
    var subsection: String? = null
    var title: String = ""
    var abstract: String = ""

    @PrimaryKey
    var url: String = UUID.randomUUID().toString()

    var uri: String? = null
    var byline: String? = null
    var itemType: String? = null
    var updatedDate: String? = null
    var createDate: String? = null
    var publishedDate: String? = null
    var materialTypeFacet: String? = null
    var kicker: String? = null
    var desFacet: RealmList<String> = RealmList()
    var orgFacet: RealmList<String> = RealmList()
    var perFacet: RealmList<String> = RealmList()
    var geoFacet: RealmList<String> = RealmList()
    var multimedia: RealmList<RealmNYTMultimedium> = RealmList()
    var shortUrl: String? = null

    companion object {
        const val EMBEDDED_MULTIMEDIA = "multimedia"
        const val COLUMN_API_SECTION = "apiSection"
    }
}

@RealmClass(embedded = true)
open class RealmNYTMultimedium : RealmObject() {
    var url: String? = null
    var format: String? = null
    var height: Int = 0
    var width: Int = 0
    var type: String? = null
    var subtype: String? = null
    var caption: String? = null
    var copyright: String? = null

    @LinkingObjects(RealmNYTimesArticle.EMBEDDED_MULTIMEDIA)
    val parent = RealmNYTimesArticle()
}
