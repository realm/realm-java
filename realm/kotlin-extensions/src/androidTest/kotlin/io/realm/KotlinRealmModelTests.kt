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
            val simple = it.createObject<SimpleClass>()
            assertTrue(simple.isValid())

            simple.deleteFromRealm()
            assertFalse(simple.isValid())
        }
    }

   @Test
    fun isManaged() {
        realm.executeTransaction {
            var simple = SimpleClass()
            assertFalse(simple.isManaged())

            simple = it.copyToRealm(simple)
            assertTrue(simple.isManaged())
        }
    }

    @Test
    @RunTestInLooperThread
    fun add_realmObjectChangeListener() {
        val realm = looperThread.realm
        realm.beginTransaction()
        val simple = realm.createObject<SimpleClass>()
        realm.commitTransaction()

        looperThread.keepStrongReference(simple)
        simple.addChangeListener(RealmObjectChangeListener{ simpleClass, changes ->
            assert(changes?.isFieldChanged(SimpleClass::name.name) ?: false)
            assertEquals("simple1", simpleClass.name)
            looperThread.testComplete()
        })

        realm.beginTransaction()
        simple.name = "simple1"
        realm.commitTransaction()
    }

    @Test
    @RunTestInLooperThread
    fun add_realmChangeListener() {
        val realm = looperThread.realm
        realm.beginTransaction()
        val simple = realm.createObject<SimpleClass>()
        realm.commitTransaction()

        looperThread.keepStrongReference(simple)
        simple.addChangeListener(RealmChangeListener{ simpleClass ->
            assertEquals("simple1", simpleClass.name)
            looperThread.testComplete()
        })

        realm.beginTransaction()
        simple.name = "simple1"
        realm.commitTransaction()
    }

    @Test
    @RunTestInLooperThread
    fun remove_realmChangeListener() {
        val realm = looperThread.realm
        realm.beginTransaction()
        val model = realm.createObject<PrimaryKeyClass>(101)
        realm.commitTransaction()

        val listener = RealmChangeListener<PrimaryKeyClass>{
            fail()
        }

        model.addChangeListener(listener)
        model.removeChangeListener(listener)

        realm.beginTransaction()
        model.name = "Bobby Risigliano"
        realm.commitTransaction()

        // Try to trigger the listeners.
        realm.sharedRealm.refresh()
        looperThread.testComplete()
    }

    @Test
    @RunTestInLooperThread
    fun remove_realmObjectChangeListener() {
        val realm = looperThread.realm
        realm.beginTransaction()
        val model = realm.createObject<PrimaryKeyClass>(101)
        realm.commitTransaction()

        val listener = RealmObjectChangeListener<PrimaryKeyClass>{ _,_ ->
            fail()
        }

        model.addChangeListener(listener)
        model.removeChangeListener(listener)

        realm.beginTransaction()
        model.name = "Bobby Risigliano"
        realm.commitTransaction()

        // Try to trigger the listeners.
        realm.sharedRealm.refresh()
        looperThread.testComplete()
    }

    @Test
    @RunTestInLooperThread
    fun remove_allChangeListeners() {
        val realm = looperThread.realm
        realm.beginTransaction()
        val model = realm.createObject<PrimaryKeyClass>(101)
        realm.commitTransaction()

        val changeListener = RealmChangeListener<PrimaryKeyClass> {
            fail()
        }
        val objectChangeListener = RealmObjectChangeListener<PrimaryKeyClass> { _,_ ->
            fail()
        }

        model.addChangeListener(changeListener)
        model.addChangeListener(objectChangeListener)

        model.removeAllChangeListeners()

        realm.beginTransaction()
        model.name = "Bobby Risigliano"
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
        assertFalse(result.isLoaded())

        looperThread.keepStrongReference(result)

        result.addChangeListener(RealmChangeListener { r ->
            assertTrue(r.isLoaded())
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
        assertFalse(result.isLoaded())

        result.load()

        assertTrue(result.isLoaded())
        looperThread.testComplete()
    }

}