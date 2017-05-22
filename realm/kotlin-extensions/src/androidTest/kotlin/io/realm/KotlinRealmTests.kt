package io.realm

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.realm.entities.AllKotlinTypes
import io.realm.kotlin.where
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass

import org.junit.Test
import org.junit.runner.RunWith

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

@RunWith(AndroidJUnit4::class)
class KotlinRealmTests {

    companion object {
        @BeforeClass
        @JvmStatic
        fun before() {
            Realm.init(InstrumentationRegistry.getTargetContext())
        }
    }

    @Test
    fun kclassExtensionMethods() {
        // Extend all <x>(Class) methods with <x>(KClass)
        val realm = Realm.getDefaultInstance()
        assertEquals(0, realm.where(AllKotlinTypes::class).count())
        realm.close()
    }

    @Test
    fun kotlinNullability() {
        // Add support for org.jetbrains.annotations.NotNull will add support for Kotlin null/non-null types
        val realm = Realm.getDefaultInstance()
        // This should return false
        assertTrue(realm.getSchema().get(AllKotlinTypes::class.java.simpleName).isNullable("requiredString"))
    }

    @Test
    fun typeSafeFieldNames() {
// Requires kotlin-reflect.jar which is a rather large dependency
//        val realm = Realm.getDefaultInstance()
//        val field: KMutableProperty1<AllKotlinTypes, String> = AllKotlinTypes::requiredString
//        assertFalse(realm.getSchema().get(AllKotlinTypes::class.simpleName).isNullable(AllKotlinTypes::requiredString))
    }
}
