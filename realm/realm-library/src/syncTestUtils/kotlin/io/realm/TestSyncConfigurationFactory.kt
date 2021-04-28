/*
 * Copyright 2017 Realm Inc.
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
package io.realm

import io.realm.internal.OsRealmConfig
import io.realm.mongodb.User
import io.realm.mongodb.sync.Builder
import io.realm.mongodb.sync.SyncConfiguration
import io.realm.mongodb.sync.SyncConfigurationExt
import org.bson.BsonValue;

import io.realm.mongodb.sync.testSessionStopPolicy

/**
 * Test rule used for creating SyncConfigurations. Will ensure that any Realm files are deleted when the
 * test ends.
 */
class TestSyncConfigurationFactory : TestRealmConfigurationFactory() {
    fun createSyncConfigurationBuilder(user: User?): SyncConfiguration.Builder {
        return SyncConfiguration.Builder(user, "default")
                .testSessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
    }

    fun createSyncConfigurationBuilder(user: User, partitionValue: String): SyncConfiguration.Builder {
        return SyncConfiguration.Builder(user, partitionValue)
                .testSessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
    }

    fun createSyncConfigurationBuilder(user: User, partitionValue: BsonValue): SyncConfiguration.Builder {
        return SyncConfigurationExt.Builder(user, partitionValue)
                .testSessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY);
    }
}
