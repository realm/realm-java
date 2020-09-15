package io.realm

import io.realm.entities.SimpleClass
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.toFlow
import io.realm.kotlin.where
import io.realm.rule.TestRealmConfigurationFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.test.*

class CoroutineTests {

    @Suppress("MemberVisibilityCanPrivate")
    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    private lateinit var configuration: RealmConfiguration
    private lateinit var testDispatcher: TestCoroutineDispatcher
    private lateinit var testScope: TestCoroutineScope
    private lateinit var realm: Realm

    @Before
    fun setUp() {
        testDispatcher = TestCoroutineDispatcher()
        testScope = TestCoroutineScope(testDispatcher)
        configuration = configFactory.createConfiguration()
        realm = Realm.getInstance(configuration)
    }

    @After
    fun tearDown() {
        realm.executeTransaction { it.deleteAll() }
        realm.close()
    }

    @Test
    fun toFlow_emittedOnCollect() {
        realm.executeTransactionAsync { transactionRealm ->
            transactionRealm.createObject(SimpleClass::class.java).name = "Foo"
            transactionRealm.createObject(SimpleClass::class.java).name = "Bar"
        }

        val countDownLatch = CountDownLatch(1)

        // TODO check this out for better testing: https://proandroiddev.com/from-rxjava-to-kotlin-flow-testing-42f1641d8433
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            val realmInstance = Realm.getInstance(configuration)
            realmInstance.where(SimpleClass::class.java)
                    .findAll()
                    .toFlow()
                    .onEach { flowResults ->
                        assertTrue(flowResults.isFrozen)
                        if (flowResults.size == 2) {
                            scope.cancel()
                        }
                    }.onCompletion {
                        realmInstance.close()
                        countDownLatch.countDown()
                    }.launchIn(scope)
        }
        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun toFlow_throwsDueToThreadViolation() {
        val countDownLatch = CountDownLatch(1)

        // Get results from the test thread
        val findAll = realm.where<SimpleClass>().findAll()

        CoroutineScope(Dispatchers.Main).launch {
            assertFailsWith<IllegalStateException> {
                // Now we are on the main thread, which means crash
                findAll.toFlow()
                fail("toFlow() must be called from the thread that retrieved the results!")
            }
            countDownLatch.countDown()
        }
        TestHelper.awaitOrFail(countDownLatch)
    }

    @Test
    fun executeTransactionAwait() {
        testScope.runBlockingTest {
            Realm.getInstance(configuration).use { realmInstance ->
                assertEquals(0, realmInstance.where<SimpleClass>().findAll().size)

                realmInstance.executeTransactionAwait(testDispatcher) { transactionRealm ->
                    val simpleObject = SimpleClass().apply { name = "simpleName" }
                    transactionRealm.insert(simpleObject)
                }
                assertEquals(1, realmInstance.where<SimpleClass>().findAll().size)
            }
        }
    }

    @Test
    fun executeTransactionAwait_throwsDueToThreadViolation() {
        // Just to prevent the test to end prematurely
        val countDownLatch = CountDownLatch(1)
        var exception: IllegalStateException? = null

        // It will crash so long we aren't using Dispatchers.Unconfined
        CoroutineScope(Dispatchers.IO).launch {
            assertFailsWith<IllegalStateException> {
                realm.where<SimpleClass>().findAll()
            }.let {
                exception = it
                countDownLatch.countDown()
            }
        }
        TestHelper.awaitOrFail(countDownLatch)

        // Ensure we failed
        assertNotNull(exception)
    }
}
