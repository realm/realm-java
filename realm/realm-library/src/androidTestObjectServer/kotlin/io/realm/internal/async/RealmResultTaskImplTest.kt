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
import io.realm.TestHelper
import io.realm.mongodb.App
import io.realm.mongodb.AppException
import io.realm.mongodb.RealmResultTask
import io.realm.rule.BlockingLooperThread
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.ThreadPoolExecutor
import kotlin.test.*

private const val OUTPUT = 42
private const val EXCEPTION_REASON = "BOOM"

@RunWith(AndroidJUnit4::class)
class RealmResultTaskImplTest {

    private val looperThread = BlockingLooperThread()
    private val service: ThreadPoolExecutor = App.NETWORK_POOL_EXECUTOR

    @Test
    fun constructor_throwsOnNullArgs() {
        assertFailsWith<IllegalArgumentException> {
            RealmResultTaskImpl<String>(TestHelper.getNull(), object : RealmResultTaskImpl.Executor<String>() {
                override fun run(): String {
                    return "something"
                }
            })
        }

        assertFailsWith<IllegalArgumentException> {
            RealmResultTaskImpl<String>(service, TestHelper.getNull())
        }
    }

    @Test
    fun blockingGet() = RealmResultTaskImpl(
            service,
            object : RealmResultTaskImpl.Executor<Int>() {
                override fun run(): Int {
                    return OUTPUT
                }
            }
    ).let { task -> assertEquals(OUTPUT, task.get()) }

    @Test
    fun blockingGet_fails() {
        val task: RealmResultTask<String> = RealmResultTaskImpl(
                service,
                object : RealmResultTaskImpl.Executor<String>() {
                    override fun run(): String {
                        throw RuntimeException(EXCEPTION_REASON)
                    }
                }
        )
        assertFailsWith<RuntimeException> {
            task.get()
        }.let {
            assertTrue(it.message!!.contains(EXCEPTION_REASON))
        }
    }

    @Test
    fun get_success() = looperThread.runBlocking {
        // Create task inside blocking execution or else the handler won't initialize properly
        // due to not having initialized the looper correctly
        val task: RealmResultTask<Int> = RealmResultTaskImpl(
                service,
                object : RealmResultTaskImpl.Executor<Int>() {
                    override fun run(): Int {
                        return OUTPUT
                    }
                }
        )

        task.getAsync { result ->
            assertEquals(OUTPUT, result.get())
            looperThread.testComplete()
        }
    }

    @Test
    fun get_returnsError() = looperThread.runBlocking {
        // Create task inside blocking execution or else the handler won't initialize properly
        // due to not having initialized the looper correctly
        val task: RealmResultTask<String> = RealmResultTaskImpl(
                service,
                object : RealmResultTaskImpl.Executor<String>() {
                    override fun run(): String {
                        throw RuntimeException(EXCEPTION_REASON)
                    }
                }
        )

        task.getAsync { result ->
            assertNull(result.get())
            assertNotNull(result.error)
            assertEquals(AppException::class.java, result.error::class.java)
            result.error.exception!!.let { exception ->
                assertEquals(exception::class.java, RuntimeException::class.java)
                assertTrue(exception.message.equals(EXCEPTION_REASON))
            }
            looperThread.testComplete()
        }
    }

    @Test
    fun get_throwsDueToNoLooper() {
        val task: RealmResultTask<String> = RealmResultTaskImpl(
                service,
                object : RealmResultTaskImpl.Executor<String>() {
                    override fun run(): String {
                        fail("should fail before returning anything")
                    }
                }
        )
        assertFailsWith<IllegalStateException> {
            task.getAsync {
                fail("should never reach this")
            }
        }
    }
}
