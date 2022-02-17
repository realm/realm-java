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
import io.realm.Realm
import io.realm.examples.coroutinesexample.data.newsreader.local.repository.NewsReaderRepository
import io.realm.examples.coroutinesexample.di.DependencyGraph

const val TAG = "--- CoroutinesExample"

class MainApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

        repository = DependencyGraph.provideNewsReaderRepository()
    }

    companion object {
        lateinit var repository: NewsReaderRepository
    }
}
