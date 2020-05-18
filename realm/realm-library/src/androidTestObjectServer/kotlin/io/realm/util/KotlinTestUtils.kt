package io.realm.util

import io.realm.ErrorCode
import io.realm.ObjectServerError
import org.hamcrest.Matcher
import org.junit.Assert.*
import org.junit.rules.ErrorCollector
import kotlin.test.assertFailsWith

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
