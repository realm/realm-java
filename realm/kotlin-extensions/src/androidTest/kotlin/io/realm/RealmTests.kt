package io.realm

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.realm.entities.AllKotlinTypes
import io.realm.kotlin.Foo
import io.realm.kotlin.where2
import org.junit.Assert.assertEquals
//import io.realm.kotlin.increment

import org.junit.Test
import org.junit.runner.RunWith

import kotlin.reflect.KClass

@RunWith(AndroidJUnit4::class)
class RealmTests {

    @Test
    fun where() {
        Realm.init(InstrumentationRegistry.getTargetContext())
        val realm = Realm.getDefaultInstance()
        Foo();
        val kc: KClass<AllKotlinTypes> = AllKotlinTypes::class
//        Foo().increment()
        assertEquals(0, realm.where2(kc).count())
        realm.close()
    }



}
