package io.realm

import android.support.test.runner.AndroidJUnit4
import io.realm.entities.KOTLIN_ALL_TYPES_STRING_FIELD
import io.realm.entities.KotlinAllTypes
import io.realm.rule.TestRealmConfigurationFactory
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class KotlinTests {
    @Rule @JvmField
    var configFactory = TestRealmConfigurationFactory()

    lateinit var realm : Realm
    lateinit var realmConfig : RealmConfiguration

    @Before
    fun setUp() {
        realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun realmList_add() {
        realm.executeTransaction {
            val allTypes1 = realm.createObject(KotlinAllTypes::class.java)
            allTypes1.stringField = "0"
            val allTypes2 = realm.createObject(KotlinAllTypes::class.java)
            allTypes2.stringField = "1"
            allTypes1.listField.add(allTypes2)
            val allTypes3 = KotlinAllTypes()
            allTypes3.stringField = "2"
            allTypes2.listField.add(allTypes3)
        }

        val results = realm.where(KotlinAllTypes::class.java).findAllSorted(KOTLIN_ALL_TYPES_STRING_FIELD)
        assertEquals(results[0].stringField, "0")
        assertEquals(results[1].stringField, "1")
        assertEquals(results[2].stringField, "2")

        assertEquals(results[0].listField[0].stringField, "1")
        assertEquals(results[1].listField[1].stringField, "2")
    }
}