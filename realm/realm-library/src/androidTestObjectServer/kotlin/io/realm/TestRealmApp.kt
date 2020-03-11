package io.realm

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.internal.objectstore.OsJavaNetworkTransport
import io.realm.log.LogLevel

/**
 * This class wraps various methods making it easier to create an RealmApp that can be used
 * for testing.
 *
 * NOTE: This class must remain in the [io.realm] package in order to work.
 */
class TestRealmApp private constructor() {
    companion object {
        val config = RealmAppConfiguration.Builder("realm-sdk-integration-tests-baigd")
                .logLevel(LogLevel.DEBUG)
                .baseUrl("http://127.0.0.1:9090")
                .appName("MongoDB Realm Integration Tests")
                .appVersion("1.0.")
                .build()

        private fun init() {
            Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        }

        fun getInstance(networkTransport: OsJavaNetworkTransport? = null): RealmApp {
            init()
            val app = RealmApp(config)
            if (networkTransport != null) {
                app.networkTransport = networkTransport
            }
            return app
        }
    }
}