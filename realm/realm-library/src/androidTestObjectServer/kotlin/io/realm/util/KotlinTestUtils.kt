package io.realm.util

import io.realm.ErrorCode
import io.realm.ObjectServerError
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.rules.ErrorCollector

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
