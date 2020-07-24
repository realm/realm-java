package io.realm.internal.async

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.internal.objectserver.EventStream
import io.realm.rule.BlockingLooperThread
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

@RunWith(AndroidJUnit4::class)
class RealmStreamTaskImplTest {
    private val looperThread = BlockingLooperThread()

    @Test
    fun syncExclusiveAccess() {
        // Validates that we cannot access asynchronously if we are already
        // accessing the stream synchronously.

        looperThread.runBlocking {
            val lock = ReentrantLock()

            val syncLoadedCondition = lock.newCondition()
            val asyncLoadedCondition = lock.newCondition()

            val task = RealmEventStreamTaskImpl(object : RealmEventStreamTaskImpl.Executor<String>() {
                override fun run(): EventStream<String> {
                    return object : EventStream<String> {
                        var opened: Boolean = true

                        override fun getNextEvent(): String {
                            lock.lock()

                            syncLoadedCondition.signal()
                            asyncLoadedCondition.await()

                            lock.unlock()

                            return ""
                        }

                        override fun close() {
                            opened = false
                        }

                        override fun isOpen(): Boolean {
                            return opened
                        }
                    }
                }
            })

            lock.lock()

            thread {
                task.next
                looperThread.testComplete()
            }

            syncLoadedCondition.await()
            lock.unlock()

            task.getAsync { result ->
                lock.lock()

                if (result.isSuccess) {
                    fail()
                } else {
                    assertEquals(io.realm.mongodb.ErrorCode.RUNTIME_EXCEPTION, result.error.errorCode)
                }

                asyncLoadedCondition.signal()
                lock.unlock()
            }
        }
    }

    @Test
    fun asyncExclusiveAccess() {
        // Validates that we cannot access synchronously if we are already
        // accessing the stream asynchronously.

        looperThread.runBlocking {
            val lock = ReentrantLock()

            val syncLoadedCondition = lock.newCondition()
            val asyncLoadedCondition = lock.newCondition()

            val task = RealmEventStreamTaskImpl(object : RealmEventStreamTaskImpl.Executor<String>() {
                override fun run(): EventStream<String> {
                    return object : EventStream<String> {
                        var opened: Boolean = true

                        override fun getNextEvent(): String {
                            return ""
                        }

                        override fun close() {
                            opened = false
                        }

                        override fun isOpen(): Boolean {
                            return opened
                        }
                    }
                }
            })

            lock.lock()

            task.getAsync { result ->
                lock.lock()

                asyncLoadedCondition.signal()
                syncLoadedCondition.await()

                lock.unlock()

                looperThread.testComplete()
            }

            asyncLoadedCondition.await()

            val exception = kotlin.test.assertFailsWith<RuntimeException> {
                task.next
            }

            assertEquals("Resource already open", exception.message)

            syncLoadedCondition.signal()
            lock.unlock()
        }
    }

    @Test
    fun openClose() {
        val task = RealmEventStreamTaskImpl(object : RealmEventStreamTaskImpl.Executor<String>() {
            override fun run(): EventStream<String> {
                return object : EventStream<String> {
                    var opened: Boolean = false

                    override fun getNextEvent(): String {
                        opened = true
                        return ""
                    }

                    override fun close() {
                        opened = false
                    }

                    override fun isOpen(): Boolean {
                        return opened
                    }
                }
            }
        })

        assertEquals(false, task.isOpen)
        assertEquals(false, task.isCancelled)

        task.next

        assertEquals(true, task.isOpen)
        assertEquals(false, task.isCancelled)

        task.cancel()

        assertEquals(false, task.isOpen)
        assertEquals(true, task.isCancelled)
    }

}