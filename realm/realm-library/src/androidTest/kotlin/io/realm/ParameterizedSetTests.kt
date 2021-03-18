package io.realm

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.rule.BlockingLooperThread
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * [RealmSet] tests. It uses [Parameterized] tests for all possible combinations of
 * [RealmSet] types (i.e. all primitive Realm types (see [SetSupportedType]) plus
 * [RealmModel] and [Mixed] (and in turn all possible types supported by Mixed) in both `managed`
 * and `unmanaged` modes.
 *
 * In order to streamline the testing for managed dictionaries we use Kotlin's reflection API
 * `KFunction1` and `KFunction2`. These two methods provide access to the Java getters and setters
 * used to work with each dictionary field.
 */
@RunWith(Parameterized::class)
class ParameterizedSetTests(
        private val tester: SetTester
) {

    /**
     * Initializer for parameterized tests
     */
    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun testType(): List<SetTester> {
            return SetMode.values().mapNotNull { type ->
                when (type) {
//                    SetMode.UNMANAGED -> unmanagedSetFactory()
                    SetMode.UNMANAGED -> null
//                    SetMode.MANAGED -> null
                    SetMode.MANAGED -> managedSetFactory()
                }
            }.flatten()
        }
    }

    @Rule
    @JvmField
    val configFactory = TestRealmConfigurationFactory()

    private val looperThread = BlockingLooperThread()

    @Before
    fun setUp() {
        Realm.init(InstrumentationRegistry.getInstrumentation().context)
        tester.setUp(configFactory.createConfiguration(), looperThread)
    }

    @After
    fun tearDown() {
        tester.tearDown()
    }

    @Test
    fun isManaged() {
        tester.isManaged()
    }

    @Test
    fun isValid() {
        tester.isValid()
    }

    @Test
    fun isFrozen() {
        tester.isFrozen()
    }

    @Test
    fun size() {
        tester.size()
    }

    @Test
    fun isEmpty() {
        tester.isEmpty()
    }

    @Test
    fun contains() {
        tester.contains()
    }

    @Test
    fun iterator() {
        tester.iterator()
    }

    @Test
    fun toArray() {
        tester.toArray()
    }

    @Test
    fun toArrayWithParameter() {
        tester.toArrayWithParameter()
    }

    @Test
    fun add() {
        tester.add()
    }

    @Test
    fun remove() {
        tester.remove()
    }

    @Test
    fun containsAll() {
        tester.containsAll()
    }

    @Test
    fun addAll() {
        tester.addAll()
    }

//    @Test
//    fun retainAll() {
//        tester.retainAll()
//    }
//
//    @Test
//    fun removeAll() {
//        tester.removeAll()
//    }

    @Test
    fun clear() {
        tester.clear()
    }

    @Test
    fun freeze() {
        tester.freeze()
    }
}

/**
 * Modes for sets.
 */
enum class SetMode {
    UNMANAGED, MANAGED
}

/**
 * Supported types by sets. Notice that Mixed sets can in turn support all these types internally
 * (except Mixed itself).
 *
 * Add new types ad-hoc here.
 */
enum class SetSupportedType {
    LONG, INTEGER, SHORT, BYTE, FLOAT, DOUBLE, STRING, BOOLEAN, DATE, DECIMAL128, BINARY, OBJECT_ID,
    UUID, LINK, MIXED
}

/**
 *
 */
interface SetTester : GenericTester {
    override fun setUp(configFactory: TestRealmConfigurationFactory) = Unit     // Not needed here
    fun isManaged()
    fun isValid()
    fun isFrozen()
    fun size()
    fun isEmpty()
    fun contains()
    fun iterator()
    fun toArray()
    fun toArrayWithParameter()
    fun add()
    fun remove()
    fun containsAll()
    fun addAll()
    fun retainAll()
    fun removeAll()
    fun clear()
    fun freeze()
}
