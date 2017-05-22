package io.realm

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.realm.entities.AllKotlinTypes
import io.realm.kotlin.where
import org.junit.Assert.assertEquals

import org.junit.Test
import org.junit.runner.RunWith

import kotlin.reflect.KClass

@RunWith(AndroidJUnit4::class)
class KotlinRealmTests {

    @Test
    fun where() {
        Realm.init(InstrumentationRegistry.getTargetContext())
        val realm = Realm.getDefaultInstance()
        assertEquals(0, realm.where(AllKotlinTypes::class).count())
        realm.close()
    }

    @Test
    public
}
