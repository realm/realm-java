package io.realm.unittestapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.realm.Realm
import io.realm.RealmApp
import io.realm.RealmAppConfiguration
import io.realm.RealmCredentials
import io.realm.log.LogLevel
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Realm.init(applicationContext)
        val config = RealmAppConfiguration.Builder("mongodb-realm-integration-tests")
                .logLevel(LogLevel.DEBUG)
                .baseUrl("127.0.0.1:9090")
                .appName("MongoDB Realm Integration Tests")
                .appVersion("1.0.")
                .build()
        val app = RealmApp(config)
        val creds = RealmCredentials.emailPassword("foo", "bar");
        val user = app.login(creds)
        if (user.id != "boom") {
            throw IllegalStateException()
        }

    }
}
