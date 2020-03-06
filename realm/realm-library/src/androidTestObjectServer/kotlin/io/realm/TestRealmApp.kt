package io.realm

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.log.LogLevel

/**
 * This class wraps a RealmApp that is configured for testing
 */

class TestRealmApp private constructor() {
    companion object {
        val config = RealmAppConfiguration.Builder("mongodb-realm-integration-tests")
                .logLevel(LogLevel.DEBUG)
                .baseUrl("127.0.0.1:9090")
                .appName("MongoDB Realm Integration Tests")
                .appVersion("1.0.")
                .build()

        fun getInstance(): RealmApp {
            Realm.init(InstrumentationRegistry.getTargetContext())
            return RealmApp(config)
        }
    }
}