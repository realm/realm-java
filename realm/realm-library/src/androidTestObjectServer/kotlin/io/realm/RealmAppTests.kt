package io.realm

import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealmAppTests {

    private lateinit var app: RealmApp

    @Before
    fun setUp() {
        app = TestRealmApp.getInstance()
    }

    @After
    fun tearDown() {

    }

    @Test
    fun login() {
        val creds = RealmCredentials.anonymous()
        var user = app.login(creds)
    }
}