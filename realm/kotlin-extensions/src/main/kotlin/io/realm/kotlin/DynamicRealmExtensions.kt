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
    return configuration.flowFactory?.from(this)
            ?: throw IllegalStateException("Missing flow factory in Realm configuration.")
}
