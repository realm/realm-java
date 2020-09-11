package io.realm.kotlin.config.builder

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.kotlin.config.builder.factory.TestImmutableNamedParametersRealmConfigurationFactory
import io.realm.kotlin.module.AnimalModule
import io.realm.kotlin.module.HumanModule
import io.realm.rx.RealmObservableFactory
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.security.SecureRandom
import kotlin.test.*

class ImmutableNamedParametersRealmConfigBuilderTest {

    @get:Rule
    val configFactory = TestImmutableNamedParametersRealmConfigurationFactory()

    private lateinit var context: Context
    private lateinit var defaultConfig: RealmConfiguration
    private lateinit var realm: Realm

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        defaultConfig = configFactory.createConfiguration()
    }

    @After
    fun tearDown() {
        if (this::realm.isInitialized) {
            realm.close()
        }
    }

    @Test
    fun getDefaultConfiguration_returnsTheSameObjectThatSetDefaultConfigurationSet() {
        val config = ImmutableNamedParametersRealmConfigBuilder(context).build()
        Realm.setDefaultConfiguration(config)
        assertSame(config, Realm.getDefaultConfiguration())
    }

    @Test
    fun getDefaultConfiguration_returnsNullAfterRemoveDefaultConfiguration() {
        val defaultConfiguration = Realm.getDefaultConfiguration()
        try {
            Realm.removeDefaultConfiguration()
            assertNull(Realm.getDefaultConfiguration())
        } finally {
            Realm.setDefaultConfiguration(defaultConfiguration!!)
        }
    }

    @Test
    fun name_throwsIfEmpty() {
        assertFailsWith<IllegalArgumentException> {
            ImmutableNamedParametersRealmConfigBuilder(
                    context = context,
                    fileName = ""
            )
        }
    }

    @Test
    fun directory_throwsIfIllegalDirectory() {
        assertFailsWith<IllegalArgumentException> {
            ImmutableNamedParametersRealmConfigBuilder(
                    context = context,
                    directory = File("/")
            )
        }
    }

    @Test
    fun directory_throwsIfDirAsFile() {
        val dir = configFactory.root
        val file = File(dir, "dummyfile")

        assertTrue(file.createNewFile())
        assertFailsWith<IllegalArgumentException> {
            ImmutableNamedParametersRealmConfigBuilder(
                    context = context,
                    directory = file
            )
        }
    }

    @Test
    fun key_throwsIfWrongSize() {
        arrayOf(
                ByteArray(0),
                ByteArray(Realm.ENCRYPTION_KEY_LENGTH - 1),
                ByteArray(Realm.ENCRYPTION_KEY_LENGTH + 1)
        ).forEach { key ->
            assertFailsWith<IllegalArgumentException> {
                ImmutableNamedParametersRealmConfigBuilder(
                        context = context,
                        key = key
                )
            }
        }
    }

    @Test
    fun schemaVersion_throwsIfInvalid() {
        assertFailsWith<IllegalArgumentException> {
            ImmutableNamedParametersRealmConfigBuilder(
                    context = context,
                    schemaVersion = -1
            )
        }
    }

    @Test
    fun modules_throwsIfNonRealmModules() {
        assertFailsWith<IllegalArgumentException> {
            ImmutableNamedParametersRealmConfigBuilder(
                    context = context,
                    providedModules = listOf(Any())
            )
        }

        // Test second argument
        assertFailsWith<IllegalArgumentException> {
            ImmutableNamedParametersRealmConfigBuilder(
                    context = context,
                    providedModules = listOf(Realm.getDefaultModule()!!, Any())
            )
        }
    }

    @Test
    fun standardSetup() {
        val config = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root,
                fileName = "foo.realm",
                key = TestHelper.getRandomKey(),
                schemaVersion = 42,
                migration = RealmMigration { _, _, _ -> /* no-op */ },
                deleteRealmIfMigrationNeeded = true
        ).build()

        Realm.deleteRealm(config)
        realm = Realm.getInstance(config)

        assertTrue(realm.path.endsWith("foo.realm"))
        assertEquals(42, realm.version)
    }

    @Test
    fun deleteRealmIfMigrationNeeded_failsWhenAssetFileProvided() {
        assertFailsWith<IllegalStateException> {
            ImmutableNamedParametersRealmConfigBuilder(
                    context = context,
                    assetFilePath = "asset_file.realm",
                    deleteRealmIfMigrationNeeded = true
            )
        }
    }

    @Test
    fun equals() {
        val config1 = ImmutableNamedParametersRealmConfigBuilder(context = context).build()
        val config2 = ImmutableNamedParametersRealmConfigBuilder(context = context).build()
        assertEquals(config1, config2)
    }

    @Test
    fun equals_respectReadOnly() {
        val config1 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                assetFilePath = "foo"
        )

        val config2 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                assetFilePath = "foo",
                readOnly = true
        )

        assertNotEquals(config1, config2)
    }

    @Test
    fun equals_whenRxJavaUnavailable() {
        val config1 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root
        ).build().also {
            emulateRxJavaUnavailable(it)
        }

        val config2 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root
        ).build().also {
            emulateRxJavaUnavailable(it)
        }

        assertEquals(config1, config2)
    }

    @Test
    fun equals_customModules() {
        val config1 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root,
                providedModules = listOf(HumanModule(), AnimalModule())
        ).build()

        val config2 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root,
                providedModules = listOf(AnimalModule(), HumanModule())
        ).build()

        assertEquals(config1, config2)
    }

    @Test
    fun hashCode_test() {
        val config1 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root
        ).build()

        val config2 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root
        ).build()

        assertEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun hashCode_customModules() {
        val config1 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root,
                providedModules = listOf(HumanModule(), AnimalModule())
        ).build()

        val config2 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root,
                providedModules = listOf(AnimalModule(), HumanModule())
        ).build()

        assertEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun hashCode_differentRxObservableFactory() {
        val config1 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root,
                rxFactory = RealmObservableFactory(false)
        ).build()

        val config2 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root,
                rxFactory = object : RealmObservableFactory(false) {
                    override fun hashCode(): Int {
                        return super.hashCode() + 1
                    }
                }
        ).build()

        assertNotEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun equals_configsReturnCachedRealm() {
        val realm1 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root
        ).build().let { Realm.getInstance(it) }

        val realm2 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root
        ).build().let { Realm.getInstance(it) }

        assertEquals(realm1, realm2)

        realm1.close()
        realm2.close()
    }

    @Test
    fun schemaVersion_throwsIfDifferentVersions() {
        val config1 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                schemaVersion = 1
        ).build()
        val config2 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                schemaVersion = 2
        ).build()
        val realm1 = Realm.getInstance(config1)
        assertFailsWith<IllegalArgumentException> {
            Realm.getInstance(config2)
        }
        realm1.close()
    }

    @Test
    fun encryptionKey_throwsIfDifferentEncryptionKeys() {
        val config1 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root,
                key = TestHelper.getRandomKey()
        ).build()
        val config2 = ImmutableNamedParametersRealmConfigBuilder(
                context = context,
                directory = configFactory.root,
                key = TestHelper.getRandomKey()
        ).build()

        val realm1 = Realm.getInstance(config1)
        assertFailsWith<IllegalArgumentException> {
            Realm.getInstance(config2)
        }
        realm1.close()
    }

    private fun emulateRxJavaUnavailable(config: RealmConfiguration) {
        try {
            val field = config.javaClass.getDeclaredField("rxObservableFactory")
            field.isAccessible = true
            field[config] = null
        } catch (e: NoSuchFieldException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
    }
}
