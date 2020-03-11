package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
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
        val creds = RealmCredentials.emailPassword("unique_user@domain.com", "password")
        var user = app.login(creds)
        assertEquals("this is going to blow up", user.id) // Parse error is reported wrongly
    }
}