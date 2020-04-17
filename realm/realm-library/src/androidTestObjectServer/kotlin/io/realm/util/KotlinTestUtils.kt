package io.realm.util

import io.realm.ErrorCode
import io.realm.ObjectServerError
import org.junit.Assert.assertEquals
import org.junit.Assert.fail

// Helper methods for improving Kotlin unit tests.

/**
 * Verify that a specific exception is thrown
 */
inline fun <reified T : Exception> expectException(method: () -> Unit) {
    try {
        method()
        fail()
    } catch (e: Throwable) {
        if (e !is T) {
            fail("Unexpected exception: $e. Should have been ${T::class.java}")
        }
    }
}

/**
 * Verify that an [ObjectServerError] exception is thrown with a specific [ErrorCode]
 */
inline fun expectErrorCode(expectedCode: ErrorCode, method: () -> Unit) {
    try {
        method()
        fail()
    } catch (e: ObjectServerError) {
        assertEquals(expectedCode, e.errorCode)
    }
}
