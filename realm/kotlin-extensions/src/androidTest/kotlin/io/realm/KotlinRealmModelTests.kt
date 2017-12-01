package io.realm

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.realm.entities.PrimaryKeyClass
import io.realm.entities.SimpleClass
import io.realm.kotlin.*
import io.realm.rule.RunInLooperThread
import io.realm.rule.RunTestInLooperThread
import io.realm.rule.TestRealmConfigurationFactory
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
class KotlinRealmModelTests {

    @Suppress("MemberVisibilityCanPrivate")
    @get:Rule
    val configFactory = TestRealmConfigurationFactory()

    @get:Rule
    val looperThread = RunInLooperThread()

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
    fun deleteFromRealm() {
        // Make sure starting with 0
        Assert.assertEquals(0, realm.where<SimpleClass>().count())

        // Add 1, check count
        realm.executeTransaction { it.createObject<SimpleClass>() }
        Assert.assertEquals(1, realm.where<SimpleClass>().count())

        // Delete the first, check count again.  !! is intentional to make
        // sure we are sure calling deleteFromRealm
        realm.executeTransaction { realm.where<SimpleClass>().findFirst()!!.deleteFromRealm() }
        Assert.assertEquals(0, realm.where<SimpleClass>().count())

    }

    @Test
    fun isValid() {
        realm.executeTransaction {
            val obj = it.createObject<SimpleClass>()
            assertTrue("Expected valid after insert", obj.isValid())

            obj.deleteFromRealm()
            assertFalse("Expected invalid after delete", obj.isValid())
        }
    }

   @Test
    fun isManaged() {
        realm.executeTransaction {
            var obj = SimpleClass()
            assertFalse("Expected not managed until attached", obj.isManaged())

            obj = it.copyToRealm(obj)
            assertTrue("Expected managed after attaching", obj.isManaged())
        }
    }

    @Test
    @RunTestInLooperThread
    fun addChangeListener_RealmObjectChangeListener_addObject() {
        val realm = looperThread.realm
        realm.beginTransaction()
        val obj = realm.createObject<SimpleClass>()
        realm.commitTransaction()

        looperThread.keepStrongReference(obj)
        obj.addChangeListener( RealmObjectChangeListener { updatedObj, changes ->
            assertTrue(changes?.isFieldChanged(SimpleClass::name.name) ?: false)
            assertEquals("simple1", updatedObj.name)
            looperThread.testComplete()
        })

        realm.beginTransaction()
        obj.name = "simple1"
        realm.commitTransaction()
    }

    @Test
    @RunTestInLooperThread
    fun addChangeListener_RealmChangeListener_addObject() {
        val realm = looperThread.realm
        realm.beginTransaction()
        val obj = realm.createObject<SimpleClass>()
        realm.commitTransaction()

        looperThread.keepStrongReference(obj)
        obj.addChangeListener( RealmChangeListener { simpleClass ->
            assertEquals("simple1", simpleClass.name)
            looperThread.testComplete()
        })

        realm.beginTransaction()
        obj.name = "simple1"
        realm.commitTransaction()
    }

    @Test
    @RunTestInLooperThread
    fun removeChangeListener_RealmChangeListener_removeObject() {
        val realm = looperThread.realm
        realm.beginTransaction()
        val obj = realm.createObject<PrimaryKeyClass>(101)
        realm.commitTransaction()

        val listener = RealmChangeListener<PrimaryKeyClass>{
            fail()
        }

        obj.addChangeListener(listener)
        obj.removeChangeListener(listener)

        realm.beginTransaction()
        obj.name = "Bobby Risigliano"
        realm.commitTransaction()

        // Try to trigger the listeners.
        realm.sharedRealm.refresh()
        looperThread.testComplete()
    }

    @Test
    @RunTestInLooperThread
    fun removeChangeListener_RealmObjectChangeListener_removeObject() {
        val realm = looperThread.realm
        realm.beginTransaction()
        val obj = realm.createObject<PrimaryKeyClass>(101)
        realm.commitTransaction()

        val listener = RealmObjectChangeListener<PrimaryKeyClass>{ _,_ ->
            fail()
        }

        obj.addChangeListener(listener)
        obj.removeChangeListener(listener)

        realm.beginTransaction()
        obj.name = "Bobby Risigliano"
        realm.commitTransaction()

        // Try to trigger the listeners.
        realm.sharedRealm.refresh()
        looperThread.testComplete()
    }

    @Test
    @RunTestInLooperThread
    fun removeAllChangeListeners() {
        val realm = looperThread.realm
        realm.beginTransaction()
        val obj = realm.createObject<PrimaryKeyClass>(101)
        realm.commitTransaction()

        val changeListener = RealmChangeListener<PrimaryKeyClass> {
            fail()
        }
        val objectChangeListener = RealmObjectChangeListener<PrimaryKeyClass> { _,_ ->
            fail()
        }

        obj.addChangeListener(changeListener)
        obj.addChangeListener(objectChangeListener)

        obj.removeAllChangeListeners()

        realm.beginTransaction()
        obj.name = "Bobby Risigliano"
        realm.commitTransaction()

        // Try to trigger the listeners.
        realm.sharedRealm.refresh()
        looperThread.testComplete()
    }

    @Test
    @RunTestInLooperThread
    @Throws(Throwable::class)
    fun isLoaded() {
        val realm = looperThread.realm

        realm.executeTransaction { it.createObject<SimpleClass>() }

        val result = realm.where<SimpleClass>().findFirstAsync()
        assertFalse("Expect isLoaded is false just after async call", result.isLoaded())

        looperThread.keepStrongReference(result)

        result.addChangeListener(RealmChangeListener { r ->
            assertTrue("Expected the loading to have completed", r.isLoaded())
            looperThread.testComplete()
        })
    }

    @Test
    @RunTestInLooperThread
    @Throws(Throwable::class)
    fun load() {
        val realm = looperThread.realm

        realm.executeTransaction { it.createObject<SimpleClass>() }

        val result = realm.where<SimpleClass>().findFirstAsync()
        assertFalse("Expect isLoaded is false just after async call", result.isLoaded())

        result.load()

        assertTrue("Expected isLoaded is true after blocking on load()", result.isLoaded())
        looperThread.testComplete()
    }

}
