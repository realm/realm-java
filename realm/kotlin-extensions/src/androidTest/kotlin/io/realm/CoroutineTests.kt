package io.realm

import android.util.Log
import io.realm.entities.SimpleClass
import io.realm.kotlin.asFlow
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import io.realm.log.RealmLog
import io.realm.rule.RunInLooperThread
import io.realm.rule.RunTestInLooperThread
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CoroutineTests {

    //    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Suppress("MemberVisibilityCanPrivate")
    @Rule
    @JvmField val looperThread = RunInLooperThread()

    private lateinit var realm: Realm

    @Before
    fun setUp() {
//        Dispatchers.setMain(mainThreadSurrogate)
        realm = looperThread.realm;
    }

    @After
    fun tearDown() {
//        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
//        mainThreadSurrogate.close()
        realm.close()
    }

    @Test
    @RunTestInLooperThread
    fun asFlow() {
        runBlocking {
            realm.where<SimpleClass>().findAllAsync().asFlow()
                    .filter { it.isLoaded }
//                    .filter { it.size > 0 }
                    .take(1)
                    .collect {
                        assertEquals(0, it.size)
                        RealmLog.error(it.toString())
                    }
//            realm.executeTransactionAsync { it.createObject<SimpleClass>().name = "Foo" }
        }
        looperThread.testComplete()
//        var results = realm.where<SimpleClass>().findAllAsync()
//        callbackFlow {
//            val listener: RealmChangeListener<RealmResults<T>> = RealmChangeListener {
//                if (!isClosedForSend) { // Is this needed?
//                    offer(it.freeze())
//                }
//            }
//            addChangeListener(listener)
//            awaitClose {
//                removeChangeListener(listener)
//            }
//        }
//
//
//        val flow = results.asFlow()
//        flow.
//        looperThread.keepStrongReference(results)
//        results.addChangeListener { results ->
//            if (results.isLoaded) {
//                looperThread.testComplete()
//            }
//        }
//        val job = launch {
//            RealmLog.error(Thread.currentThread().name)
//            val flow: Flow<RealmResults<SimpleClass>> = realm.where<SimpleClass>().findAllAsync().asFlow()
//            flow.take(1).collect { value ->
//                RealmLog.error(Thread.currentThread().name)
//            }
//        }
//        job.cancel()
    }
}