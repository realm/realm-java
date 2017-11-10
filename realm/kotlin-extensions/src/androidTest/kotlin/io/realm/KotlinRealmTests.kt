package io.realm

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.realm.entities.PrimaryKeyClass
import io.realm.entities.SimpleClass
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.rule.TestRealmConfigurationFactory
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith

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
}
