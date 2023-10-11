package io.realm.internal.network

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppException
import io.realm.mongodb.ErrorCode
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class NetworkRequestTests {

	private lateinit var app: App

	@Before
	fun setUp() {
		Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
	}

	// Test for https://github.com/realm/realm-java/issues/7847
	@Test
	fun interruptedRequestReturnsError() {
		val request = object: NetworkRequest<Unit>() {
			override fun mapSuccess(result: Any?): Unit {
				Unit
			}

			override fun execute(callback: NetworkRequest<Unit>) {
				Thread.currentThread().interrupt()
			}
		}
		assertFailsWith<AppException> {
			request.resultOrThrow()
		}.also {
			assertEquals(it.errorCode, ErrorCode.NETWORK_INTERRUPTED)
			assertEquals(it.errorMessage, "Network request interrupted.")
		}
	}
}