package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.entities.PrimaryKeyClass
import io.realm.entities.SimpleClass
import io.realm.kotlin.createObject
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.where
import io.realm.rule.BlockingLooperThread
import io.realm.rule.TestRealmConfigurationFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
class KotlinRealmTests {

    @Suppress("MemberVisibilityCanPrivate")
    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    private lateinit var realm: Realm

//    private val testDispatcher = TestCoroutineDispatcher()

//    @ExperimentalCoroutinesApi
//    @get:Rule
//    var coroutinesTestRule = CoroutineTestRule()

    private val looperThread = BlockingLooperThread()

    @Before
    fun setUp() {
        realm = Realm.getInstance(configFactory.createConfiguration())
    }

    @After
    fun tearDown() {
        realm.executeTransaction { it.deleteAll() }
        realm.close()

        with(Realm.getDefaultInstance()) {
            executeTransaction { it.deleteAll() }
            close()
        }
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
    fun executeTransactionAwait() {
        looperThread.runBlocking {
            CoroutineScope(Dispatchers.Main).launch {
                val defaultRealm = Realm.getDefaultInstance()
                assertEquals(0, defaultRealm.where(SimpleClass::class.java).findAll().size)
                defaultRealm.executeTransactionAwait { transactionRealm ->
                    val simpleObject = SimpleClass().apply { name = "simpleName" }
                    transactionRealm.insert(simpleObject)
                    looperThread.testComplete()
                }
            }
        }
        assertEquals(1, Realm.getDefaultInstance().where(SimpleClass::class.java).findAll().size)
    }
}
