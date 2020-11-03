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

package io.realm.examples.coroutinesexample

import androidx.multidex.MultiDexApplication
import androidx.room.Room
import io.realm.Realm
import io.realm.examples.coroutinesexample.data.newsreader.local.room.NYTDatabase

const val TAG = "--- CoroutinesExample"

class MainApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

        ROOM_DB = Room.databaseBuilder(this, NYTDatabase::class.java, "articles_database")
                .allowMainThreadQueries()
                .build()
    }

    companion object {
        lateinit var ROOM_DB: NYTDatabase
    }
}
