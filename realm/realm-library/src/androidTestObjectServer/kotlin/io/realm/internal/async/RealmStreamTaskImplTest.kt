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

package io.realm.internal.async

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.realm.internal.objectserver.EventStream
import io.realm.mongodb.mongo.events.BaseChangeEvent
import io.realm.rule.BlockingLooperThread
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.IllegalStateException
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class RealmStreamTaskImplTest {
    private val looperThread = BlockingLooperThread()

    @Test
    fun asyncExclusiveAccess() {
        // Validates that we cannot access synchronously if we are already
        // accessing the stream asynchronously.

        val task = RealmEventStreamAsyncTaskImpl("test", object : RealmEventStreamAsyncTaskImpl.Executor<String>() {
            override fun run(): EventStream<String> {
                return object : EventStream<String> {
                    var opened: Boolean = true

                    override fun getNextEvent(): BaseChangeEvent<String>? {
                        return null
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

        task.get { }

        assertFailsWith<IllegalStateException> {
            task.get { }
        }
    }

    @Test
    fun openClose() {
        val task = RealmEventStreamTaskImpl("test", object : RealmEventStreamTaskImpl.Executor<String>() {
            override fun run(): EventStream<String> {
                return object : EventStream<String> {
                    var opened: Boolean = false

                    override fun getNextEvent(): BaseChangeEvent<String>? {
                        opened = true
                        return null
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

    @Test
    fun openCloseAsync() {
        val task = RealmEventStreamAsyncTaskImpl("test", object : RealmEventStreamAsyncTaskImpl.Executor<String>() {
            override fun run(): EventStream<String> {
                return object : EventStream<String> {
                    var opened: Boolean = false

                    override fun getNextEvent(): BaseChangeEvent<String>? {
                        opened = true
                        return null
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

        looperThread.runBlocking {
            task.get {
                looperThread.testComplete()
            }
        }

        assertEquals(true, task.isOpen)
        assertEquals(false, task.isCancelled)

        task.cancel()

        assertEquals(false, task.isOpen)
        assertEquals(true, task.isCancelled)
    }
}
