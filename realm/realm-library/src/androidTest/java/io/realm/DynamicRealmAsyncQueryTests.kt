/*
 * Copyright 2020 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.DynamicRealm
import io.realm.TestHelper.TestLogger
import io.realm.entities.AllTypes
import io.realm.entities.Owner
import io.realm.internal.async.RealmThreadPoolExecutor
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.rule.BlockingLooperThread
import io.realm.rule.TestRealmConfigurationFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

@RunWith(AndroidJUnit4::class)
class DynamicRealmAsyncQueryTests {

    @get:Rule
    val configFactory = TestRealmConfigurationFactory()
    @get:Rule
    val thrown: ExpectedException = ExpectedException.none()

    private val looperThread = BlockingLooperThread()

    private lateinit var config: RealmConfiguration

    @Before
    fun setUp() {
        config = configFactory.createConfiguration()

        // Initializes schema. DynamicRealm will not do that, so let a normal Realm create the file first.
        Realm.getInstance(config).close()
    }

    // ****************************
    // ****  Async transaction  ***
    // ****************************
    // Starts asynchronously a transaction to insert one element.
    @Test
    fun executeTransactionAsync() = looperThread.runBlocking {
        val realm = DynamicRealm.getInstance(config)
                .also { looperThread.closeAfterTest(it) }

        assertEquals(0, realm.where(Owner.CLASS_NAME).count())

        realm.executeTransactionAsync({ transactionRealm ->
            val owner = transactionRealm.createObject(Owner.CLASS_NAME)
            owner.setString(Owner.FIELD_NAME, "Owner")
        }, {
            assertEquals(1, realm.where(Owner.CLASS_NAME).count())
            assertEquals("Owner", realm.where(Owner.CLASS_NAME).findFirst()!!.getString(Owner.FIELD_NAME))
            looperThread.testComplete()
        }) { error ->
            fail(error.message)
        }
    }

    @Test
    fun executeTransactionAsync_onSuccess() = looperThread.runBlocking {
        val realm = DynamicRealm.getInstance(config)
                .also { looperThread.closeAfterTest(it) }

        assertEquals(0, realm.where(Owner.CLASS_NAME).count())

        realm.executeTransactionAsync(DynamicRealm.Transaction { transactionRealm ->
            val owner = transactionRealm.createObject(Owner.CLASS_NAME)
            owner.setString(Owner.FIELD_NAME, "Owner")
        }, DynamicRealm.Transaction.OnSuccess {
            assertEquals(1, realm.where(Owner.CLASS_NAME).count())
            assertEquals("Owner", realm.where(Owner.CLASS_NAME).findFirst()!!.getString(Owner.FIELD_NAME))
            looperThread.testComplete()
        })
    }

    @Test
    fun executeTransactionAsync_onSuccessCallerRealmClosed() = looperThread.runBlocking {
        val realm = DynamicRealm.getInstance(config)

        assertEquals(0, realm.where(Owner.CLASS_NAME).count())
        realm.executeTransactionAsync(DynamicRealm.Transaction { transactionRealm ->
            val owner = transactionRealm.createObject(Owner.CLASS_NAME)
            owner.setString(Owner.FIELD_NAME, "Owner")
        }, DynamicRealm.Transaction.OnSuccess {
            assertTrue(realm.isClosed)

            DynamicRealm.getInstance(config).use { newRealm ->
                assertEquals(1, newRealm.where(Owner.CLASS_NAME).count())
                assertEquals("Owner", newRealm.where(Owner.CLASS_NAME).findFirst()!!.getString(Owner.FIELD_NAME))
            }

            looperThread.testComplete()
        })
        realm.close()
    }

    @Test
    fun executeTransactionAsync_onError() = looperThread.runBlocking {
        val realm = DynamicRealm.getInstance(config)
                .also { looperThread.closeAfterTest(it) }

        val runtimeException = RuntimeException("Oh! What a Terrible Failure")

        assertEquals(0, realm.where(Owner.CLASS_NAME).count())

        realm.executeTransactionAsync({
            throw runtimeException
        }) { error ->
            assertEquals(0, realm.where(Owner.CLASS_NAME).count())
            assertNull(realm.where(Owner.CLASS_NAME).findFirst())
            assertEquals(runtimeException, error)

            looperThread.testComplete()
        }
    }

    @Test
    fun executeTransactionAsync_onErrorCallerRealmClosed() = looperThread.runBlocking {
        val realm = DynamicRealm.getInstance(config)
        val runtimeException = RuntimeException("Oh! What a Terrible Failure")

        assertEquals(0, realm.where(Owner.CLASS_NAME).count())

        realm.executeTransactionAsync({
            throw runtimeException
        }) { error ->
            assertTrue(realm.isClosed)

            DynamicRealm.getInstance(config).use { newRealm ->
                assertEquals(0, newRealm.where(Owner.CLASS_NAME).count())
                assertNull(newRealm.where(Owner.CLASS_NAME).findFirst())
                assertEquals(runtimeException, error)
            }

            looperThread.testComplete()
        }
        realm.close()
    }

    @Test
    fun executeTransactionAsync_NoCallbacks() = looperThread.runBlocking {
        val realm = DynamicRealm.getInstance(config)
                .also { looperThread.closeAfterTest(it) }

        assertEquals(0, realm.where(Owner.CLASS_NAME).count())

        realm.executeTransactionAsync { transactionRealm ->
            val owner = transactionRealm.createObject(Owner.CLASS_NAME)
            owner.setString(Owner.FIELD_NAME, "Owner")
        }

        realm.addChangeListener { listenerRealm ->
            assertEquals("Owner", listenerRealm.where(Owner.CLASS_NAME).findFirst()!!.getString(Owner.FIELD_NAME))

            looperThread.testComplete()
        }
    }

    // Tests that an async transaction that throws when call cancelTransaction manually.
    @Test
    fun executeTransactionAsync_cancelTransactionInside() = looperThread.runBlocking {
        val testLogger = TestLogger(LogLevel.DEBUG)
        RealmLog.add(testLogger)

        val realm = DynamicRealm.getInstance(config)
                .also { looperThread.closeAfterTest(it) }

        assertEquals(0, realm.where(Owner.CLASS_NAME).count())

        realm.executeTransactionAsync({ transactionRealm ->
            val owner = transactionRealm.createObject(Owner.CLASS_NAME)
            owner.setString(Owner.FIELD_NAME, "Owner")
            transactionRealm.cancelTransaction()
        }, {
            fail("Should not reach success if runtime exception is thrown in callback.")
        }) { error ->
            // Ensure we are giving developers quality messages in the logs.
            assertTrue(testLogger.message.contains("Exception has been thrown: Can't commit a non-existing write transaction"))
            assertTrue(error is java.lang.IllegalStateException)

            RealmLog.remove(testLogger)

            looperThread.testComplete()
        }
    }

    // Tests if the background Realm is closed when transaction success returned.
    @Test
    fun executeTransactionAsync_realmClosedOnSuccess() = looperThread.runBlocking {
        val realm = DynamicRealm.getInstance(config)
                .also { looperThread.closeAfterTest(it) }

        val counter = AtomicInteger(100)

        val cacheCallback = RealmCache.Callback { count ->
            assertEquals(1, count.toLong())
            if (counter.decrementAndGet() == 0) {
                looperThread.testComplete()
            }
        }

        val onSuccessCallback = object : DynamicRealm.Transaction.OnSuccess {
            override fun onSuccess() {
                RealmCache.invokeWithGlobalRefCount(realm.getConfiguration(), cacheCallback)
                if (counter.get() == 0) {
                    // Finishes testing.
                    return
                }
                realm.executeTransactionAsync(DynamicRealm.Transaction {
                    // no-op
                }, this)
            }
        }
        realm.executeTransactionAsync(DynamicRealm.Transaction {
            // no-op
        }, onSuccessCallback)
    }

    // Tests if the background Realm is closed when transaction error returned.
    @Test
    fun executeTransaction_async_realmClosedOnError() = looperThread.runBlocking {
        val realm = DynamicRealm.getInstance(config)
                .also { looperThread.closeAfterTest(it) }

        val counter = AtomicInteger(100)

        val cacheCallback = RealmCache.Callback { count ->
            assertEquals(1, count.toLong())
            if (counter.decrementAndGet() == 0) {
                looperThread.testComplete()
            }
        }

        val onErrorCallback = object : DynamicRealm.Transaction.OnError {
            override fun onError(error: Throwable) {
                RealmCache.invokeWithGlobalRefCount(realm.getConfiguration(), cacheCallback)
                if (counter.get() == 0) {
                    // Finishes testing.
                    return
                }
                realm.executeTransactionAsync(DynamicRealm.Transaction { throw RuntimeException("Dummy exception") }, this)
            }
        }

        realm.executeTransactionAsync(DynamicRealm.Transaction {
            throw RuntimeException("Dummy exception")
        }, onErrorCallback)
    }

    // Test case for https://github.com/realm/realm-java/issues/1893
    // Ensures that onSuccess is called with the correct Realm version for async transaction.
    @Test
    fun executeTransactionAsync_asyncQuery() = looperThread.runBlocking {
        val realm = DynamicRealm.getInstance(config)
                .also { looperThread.closeAfterTest(it) }

        val results = realm.where(AllTypes.CLASS_NAME).findAllAsync()

        assertEquals(0, results.size.toLong())

        realm.executeTransactionAsync({ transactionRealm ->
            transactionRealm.createObject(AllTypes.CLASS_NAME)
        }, {
            assertEquals(1, realm.where(AllTypes.CLASS_NAME).count())

            // We cannot guarantee the async results get delivered from OS.
            if (results.isLoaded) {
                assertEquals(1, results.size.toLong())
            } else {
                assertEquals(0, results.size.toLong())
            }

            looperThread.testComplete()
        }) {
            fail(it.message)
        }
    }

    @Test
    fun executeTransactionAsync_onSuccessOnNonLooperThreadThrows() {
        DynamicRealm.getInstance(config).use { realm ->
            thrown.expect(IllegalStateException::class.java)
            realm.executeTransactionAsync(DynamicRealm.Transaction {
                // no-op
            }, DynamicRealm.Transaction.OnSuccess {
                // no-op
            })
        }
    }

    @Test
    fun executeTransactionAsync_onErrorOnNonLooperThreadThrows() {
        DynamicRealm.getInstance(config).use { realm ->
            thrown.expect(IllegalStateException::class.java)
            realm.executeTransactionAsync(DynamicRealm.Transaction {
                // no-op
            }, DynamicRealm.Transaction.OnError {
                // no-op
            })
        }
    }

    // https://github.com/realm/realm-java/issues/4595#issuecomment-298830411
    // onSuccess might commit another transaction which will call didChange. So before calling async transaction
    // callbacks, the callback should be cleared.
    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun executeTransactionAsync_callbacksShouldBeClearedBeforeCalling() = looperThread.runBlocking {
        val callbackCounter = AtomicInteger(0)
        val foregroundRealm = DynamicRealm.getInstance(config)
                .also { looperThread.closeAfterTest(it) }

        // Use single thread executor
        TestHelper.replaceRealmThreadExecutor(RealmThreadPoolExecutor.newSingleThreadExecutor())

        // To reproduce the issue, the posted callback needs to arrived before the Object Store did_change called.
        // We just disable the auto refresh here then the did_change won't be called.
        foregroundRealm.isAutoRefresh = false
        foregroundRealm.executeTransactionAsync(DynamicRealm.Transaction { transactionRealm ->
            transactionRealm.createObject(AllTypes.CLASS_NAME)
        }, DynamicRealm.Transaction.OnSuccess {
            // This will be called first and only once
            assertEquals(0, callbackCounter.getAndIncrement().toLong())

            // This transaction should never trigger the onSuccess.
            foregroundRealm.beginTransaction()
            foregroundRealm.createObject(AllTypes.CLASS_NAME)
            foregroundRealm.commitTransaction()
        })

        foregroundRealm.executeTransactionAsync(DynamicRealm.Transaction { transactionRealm ->
            transactionRealm.createObject(AllTypes.CLASS_NAME)
        }, DynamicRealm.Transaction.OnSuccess {
            // This will be called 2nd and only once
            assertEquals(1, callbackCounter.getAndIncrement().toLong())

            looperThread.testComplete()
        })

        // Wait for all async tasks finish to ensure the async transaction posted callback will arrive first.
        TestHelper.resetRealmThreadExecutor()
        looperThread.postRunnable(Runnable {
            // Manually call refresh, so the did_change will be triggered.
            foregroundRealm.sharedRealm.refresh()
        })
    }
}
