package io.realm.util

import io.realm.ErrorCode
import io.realm.ObjectServerError
import org.junit.Assert.assertEquals
import org.junit.Assert.fail

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
