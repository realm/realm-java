package io.realm

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.realm.entities.PrimaryKeyClass
import io.realm.entities.SimpleClass
import io.realm.kotlin.callInTransaction
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.rule.TestRealmConfigurationFactory
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception

@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
class KotlinRealmTests {

//    companion object {
//        @BeforeClass
//        @JvmStatic
//        fun before() {
//            Realm.init(InstrumentationRegistry.getTargetContext())
//        }
//    }

    @Suppress("MemberVisibilityCanPrivate")
    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    private lateinit var realm: Realm

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getTargetContext())
        realm = Realm.getInstance(configFactory.createConfiguration())
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun createObject() {
        realm.executeTransaction {
            it.createObject<SimpleClass>()
        }
        assertEquals(1, realm.where<SimpleClass>().count())
    }


    @Test
    fun createObject_primaryKey() {
        realm.executeTransaction {
            it.createObject<PrimaryKeyClass>(1)
        }
        assertEquals(1, realm.where<PrimaryKeyClass>().count())
    }

    @Test
    fun where() {
        assertEquals(0, realm.where<SimpleClass>().count())
    }

    @Test
    fun where_getsResult() {
        assertEquals(0, realm.where<PrimaryKeyClass>().count())

        val pk = 1L
        val name = "complex"

        realm.executeTransaction {
            val pkClassObj = it.createObject(PrimaryKeyClass::class.java, pk)
            pkClassObj.name = name

        }

        val pkClassObjOut = realm.where<PrimaryKeyClass>()
                .equalTo(PrimaryKeyClass::id.name, pk)
                .findFirst()

        assertNotNull(pkClassObjOut)
        assertEquals(pk, pkClassObjOut?.id)
        assertEquals(name, pkClassObjOut?.name)
    }

    @Test
    fun callInTransaction_withPairOfRealmModelReturned() {

        val (simple, complex) = realm.callInTransaction {

             val simple = createObject<SimpleClass>().apply {
                 name = "Simple"
             }
             val complex = createObject<PrimaryKeyClass>(101).apply {
                 name = "PK"
             }
             Pair(simple, complex)
        }

        assertTrue("Expected simple to be managed", RealmObject.isManaged(simple))
        assertTrue("Expected simple to be valid", RealmObject.isValid(simple))
        assertEquals("Simple", simple.name)


        assertTrue("Expected complex to be managed", RealmObject.isManaged(complex))
        assertTrue("Expected complex to be valid", RealmObject.isValid(complex))
        assertEquals("PK", complex.name)

    }

    @Test
    fun callInTransaction_withLongValueReturn() {

        val pk = realm.callInTransaction {

            val complex = createObject<PrimaryKeyClass>(101).apply {
                name = "PK"
            }

            complex.id
        }

        assertEquals(101L, pk)
    }

    @Test
    fun callInTransaction_ExceptionCancelsTx() {

        var firedException = false

        assertFalse("Test Precondition expected Realm was not in TX before running",
                realm.isInTransaction)
        try {
            realm.callInTransaction {
                throw RuntimeException("Exeception")
            }
        } catch(e: Exception) {
            firedException = true
        } finally {
            assertTrue("Expected exception to fire",firedException)
            assertFalse("Expected tx to cancel", realm.isInTransaction)
        }

    }


}