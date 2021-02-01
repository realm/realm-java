package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.rule.TestRealmConfigurationFactory
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


// FIXME: MIXED PARAMETRIZED TESTS FOR INDEXED AND UNINDEXED
@RunWith(AndroidJUnit4::class)
class DynamicMixedTests {
    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    private lateinit var realm: DynamicRealm

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realm = DynamicRealm.getInstance(configFactory.createConfiguration("Mixed"))

        realm.executeTransaction {
            realm.schema
                    .create("MixedObject")
                    .addField("myMixed", Mixed::class.java)
        }
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun writeRead_primitive() {
        realm.beginTransaction()

        val anObject = realm.createObject("MixedObject")
        anObject.setMixed("myMixed", Mixed.valueOf(Date(10)))

        realm.commitTransaction()

        val myMixed = anObject.getMixed("myMixed")

        assertEquals(Date(10), myMixed.asDate())
    }

    @Test
    fun defaultNullValue() {
        realm.beginTransaction()

        val anObject = realm.createObject("MixedObject")

        realm.commitTransaction()

        val myMixed = anObject.getMixed("myMixed")

        assertTrue(myMixed.isNull)
        assertEquals(MixedType.NULL, myMixed.type)
    }

    @Test
    fun setNullValue() {
        realm.beginTransaction()

        val anObject = realm.createObject("MixedObject")
        anObject.setMixed("myMixed", Mixed.nullValue())

        realm.commitTransaction()

        val myMixed = anObject.getMixed("myMixed")

        assertTrue(myMixed.isNull)
        assertEquals(MixedType.NULL, myMixed.type)
    }

    @Test
    fun writeRead_model() {
        realm.beginTransaction()

        val innerObject = realm.createObject("MixedObject")
        innerObject.setMixed("myMixed", Mixed.valueOf(Date(10)))

        val outerObject = realm.createObject("MixedObject")
        outerObject.setMixed("myMixed", Mixed.valueOf(innerObject))

        realm.commitTransaction()

        val innerMixed = innerObject.getMixed("myMixed")
        val outerMixed = outerObject.getMixed("myMixed")

        assertEquals(Date(10), innerMixed.asDate())
        assertEquals(DynamicRealmObject::class.java, outerMixed.valueClass)

        val aMixed = outerMixed
                .asRealmModel(DynamicRealmObject::class.java)
                .getMixed("myMixed")

        assertEquals(innerMixed.asDate(), aMixed.asDate())
    }
}
