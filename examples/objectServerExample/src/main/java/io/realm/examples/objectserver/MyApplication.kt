/*
 * Copyright 2019 Realm Inc.
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

package io.realm.examples.objectserver

import android.app.Application
import android.util.Log

import io.realm.Realm
import io.realm.RealmApp
import io.realm.RealmAppConfiguration
import io.realm.log.LogLevel
import io.realm.log.RealmLog

lateinit var APP: RealmApp

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        APP = RealmApp(RealmAppConfiguration.Builder(BuildConfig.MONGODB_REALM_APP_ID)
                .baseUrl(BuildConfig.MONGODB_REALM_URL)
                .appName(BuildConfig.VERSION_NAME)
                .appVersion(BuildConfig.VERSION_CODE.toString())
                .build())

        // Enable more logging in debug mode
        if (BuildConfig.DEBUG) {
            RealmLog.setLevel(LogLevel.ALL)
        }
    }
}
