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

package io.realm.kotlin

import io.realm.DynamicRealm
import io.realm.annotations.Beta
import kotlinx.coroutines.flow.Flow

/**
 * Creates a [Flow] for a [DynamicRealm]. It should emit the initial state of the Realm when subscribed to and
 * on each subsequent update of the Realm.
 *
 * @return Kotlin [Flow] that emit all updates to the Realm.
 */
@Beta
fun DynamicRealm.toflow(): Flow<DynamicRealm> {
    return configuration.flowFactory.from(this)
}
