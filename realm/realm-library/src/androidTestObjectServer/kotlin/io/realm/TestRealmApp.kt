package io.realm

import io.realm.log.LogLevel

/**
 * This class wraps a RealmApp that is configured for testing
 */

class TestRealmApp private constructor() {
    companion object {
        val config = RealmAppConfiguration.Builder("mongodb-realm-integration-tests")
                .logLevel(LogLevel.DEBUG)
                .appName("MongoDB Realm Integration Tests")
                .appVersion("1.0.")
                .build()

        fun getInstance(): RealmApp {
            return RealmApp(config)
        }
    }
}