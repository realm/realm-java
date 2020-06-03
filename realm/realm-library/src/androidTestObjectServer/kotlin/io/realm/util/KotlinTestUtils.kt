package io.realm.util

import android.util.ArraySet
import io.realm.mongodb.ErrorCode
import io.realm.mongodb.ObjectServerError
import org.hamcrest.Matcher
import org.junit.Assert.*
import org.junit.rules.ErrorCollector
import java.io.Closeable

// Helper methods for improving Kotlin unit tests.

/**
 * Verify that an [ObjectServerError] exception is thrown with a specific [ErrorCode]
 */
inline fun assertFailsWithErrorCode(expectedCode: ErrorCode, method: () -> Unit) {
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

inline fun <reified T> assertFailsWithMessage(matcher: Matcher<in String?>, block : () -> Unit){
    try {
        block()
        fail("assertFailsWithMessage completed without expected exception")
    } catch (e : Exception) {
        if (e !is T) {
            throw AssertionError("assertFailsWithMessage did not throw expected exception: " + T::class.java.name)
        }
        assertThat(e.message, matcher)
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
        resources.clear()
    }

    @Synchronized
    fun add(resource: Closeable) {
        resources.add(resource)
    }
}

