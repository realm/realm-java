/**
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
package io.realm.entities

import io.realm.annotations.RealmModule
import io.realm.entities.embedded.*

const val defaultPartitionValue = "default"

/**
 * The set of classes initially supported by MongoDB Realm.
 */
@RealmModule(classes = [
    SyncDog::class,
    SyncPerson::class,
    SyncAllTypes::class,
    EmbeddedSimpleParent::class,
    EmbeddedSimpleChild::class,
    EmbeddedSimpleListParent::class
    // FIXME: add these to schema once https://jira.mongodb.org/projects/HELP/queues/issue/HELP-17759 is fixed
//    EmbeddedTreeParent::class,
//    EmbeddedTreeNode::class,
//    EmbeddedTreeLeaf::class
])
class DefaultSyncSchema
