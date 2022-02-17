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

package io.realm.mongodb.sync

import io.realm.RealmModel
import io.realm.internal.OsRealmConfig
import io.realm.mongodb.User
import org.bson.BsonValue

class SyncConfigurationExt {
    companion object
}

fun SyncConfiguration.testRealmExists(): Boolean{
    return this.realmExists()
}

// Added to expose Builder(User, BsonValue) outside io.realm.mongodb.sync package for test
fun SyncConfigurationExt.Companion.Builder(user: User, partitionValue: BsonValue): SyncConfiguration.Builder {
    return SyncConfiguration.Builder(user, partitionValue)
}

// Added to expose schema outside io.realm.mongodb.sync package for test
fun SyncConfiguration.Builder.testSchema(firstClass: Class<out RealmModel>, vararg x: Class<out RealmModel> ) : SyncConfiguration.Builder {
    return this.schema(firstClass, *x)
}

// Added to expose sesssionStopPolicy outside io.realm.mongodb.sync package for test
fun SyncConfiguration.Builder.testSessionStopPolicy(policy: OsRealmConfig.SyncSessionStopPolicy): SyncConfiguration.Builder {
    return this.sessionStopPolicy(policy)
}
