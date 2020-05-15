package io.realm.util

import android.util.ArraySet
import io.realm.ErrorCode
import io.realm.ObjectServerError
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.rules.ErrorCollector
import java.io.Closeable

// Helper methods for improving Kotlin unit tests.

/**
 * Verify that an [ObjectServerError] exception is thrown with a specific [ErrorCode]
 */
inline fun expectErrorCode(expectedCode: ErrorCode, method: () -> Unit) {
    try {
        method()
        fail()
    } catch (e: ObjectServerError) {
        assertEquals("Unexpected error code", expectedCode, e.errorCode)
    }
}

inline fun <reified T> ErrorCollector.assertFailsWith(block : () -> Unit){
    try {
        block()
    } catch (e : Exception) {
        if (e !is T) {
            addError(e)
        }
    }
}

/**
 * A **resource container** to keep references for objects that should later be closed.
 */
class ResourceContainer : Closeable {
    val resources = ArraySet<Closeable>()

    @Synchronized
    override fun close() {
        resources.map { it.close() }
    }

    @Synchronized
    fun add(resource: Closeable) {
        resources.add(resource)
    }
}

