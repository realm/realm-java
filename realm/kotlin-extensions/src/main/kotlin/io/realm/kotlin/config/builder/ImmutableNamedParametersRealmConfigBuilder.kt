package io.realm.kotlin.config.builder

import android.content.Context
import io.realm.*
import io.realm.annotations.RealmModule
import io.realm.exceptions.RealmException
import io.realm.internal.OsRealmConfig
import io.realm.internal.RealmCore
import io.realm.internal.Util
import io.realm.rx.RealmObservableFactory
import io.realm.rx.RxObservableFactory
import java.io.File

class ImmutableNamedParametersRealmConfigBuilder(
        private val context: Context,
        private val directory: File = context.filesDir,
        private val fileName: String = Realm.DEFAULT_REALM_NAME,
        private val assetFilePath: String? = null,
        private val key: ByteArray? = null,
        private val schemaVersion: Long = 0L,
        private val migration: RealmMigration? = null,
        private val deleteRealmIfMigrationNeeded: Boolean = false,
        private val initialDataTransaction: Realm.Transaction? = null,
        private val readOnly: Boolean = false,
        private val compactOnLaunch: CompactOnLaunchCallback? = null,
        private val maxNumberOfActiveVersions: Long = Long.MAX_VALUE,
        private val inMemory: Boolean = false,
        private val rxFactory: RxObservableFactory? = null,
        private val schema: List<Class<out RealmModel>> = listOf(),
        private val providedModules: List<Any> = listOf()
) {

    private val modules: MutableSet<Any> = hashSetOf()
    private val debugSchema: MutableSet<Class<out RealmModel>> = hashSetOf()

    private lateinit var durability: OsRealmConfig.Durability

    init {
        RealmCore.loadLibrary(context)
        initializeBuilder()
    }

    private fun initializeBuilder() {
        // fileName
        require(fileName.isNotEmpty()) { "A non-empty filename must be provided" }

        // directory
        require(!directory.isFile) { "'dir' is a file, not a directory: ${directory.absolutePath}." }
        require(!(!directory.exists() && !directory.mkdirs())) { "Could not create the specified directory: ${directory.absolutePath}." }
        require(directory.canWrite()) { "Realm directory is not writable: ${directory.absolutePath}." }

        // key
        if (key != null) {
            require(key.size == Realm.ENCRYPTION_KEY_LENGTH) { "The provided key must be ${Realm.ENCRYPTION_KEY_LENGTH} bytes. Yours was: ${key.size}" }
        }

        // schemaVersion
        require(schemaVersion >= 0) { "Realm schema version numbers must be 0 (zero) or higher. Yours was: $schemaVersion" }

        // deleteRealmIfMigrationNeeded
        if (deleteRealmIfMigrationNeeded) {
            check(assetFilePath.isNullOrEmpty()) { "Realm cannot clear its schema when previously configured to use an asset file by calling assetFile()." }
        }

        // durability/inMemory
        durability = if (inMemory) {
            check(assetFilePath.isNullOrEmpty()) { "Realm can not use in-memory configuration if asset file is present." }
            OsRealmConfig.Durability.MEM_ONLY
        } else {
            OsRealmConfig.Durability.FULL
        }

        // assetFilePath
        if (assetFilePath != null) {
            require(assetFilePath.isNotEmpty()) { "A non-empty asset file path must be provided" }

            if (durability == OsRealmConfig.Durability.MEM_ONLY) {
                throw RealmException("Realm can not use in-memory configuration if asset file is present.")
            }
            check(!deleteRealmIfMigrationNeeded) { "Realm cannot use an asset file when previously configured to clear its schema in migration by calling deleteRealmIfMigrationNeeded()." }
        }

        // maxNumberOfActiveVersions
        require(maxNumberOfActiveVersions >= 1) { "Only positive numbers above 0 are allowed. Yours was: $maxNumberOfActiveVersions" }

        // schema
        if (schema.isNotEmpty()) {
            modules.clear()
        }
        schema.forEach { clazz ->
            debugSchema.add(clazz)
        }

        // modules
        // FIXME: find a less messy way to get the default module
        if (RealmConfiguration.DEFAULT_MODULE != null) {
            modules.add(RealmConfiguration.DEFAULT_MODULE)
        }
        providedModules.forEach { providedModule ->
            checkModule(providedModule)
            modules.add(providedModule)
        }
    }

    fun build(): RealmConfiguration {
        // Check that readOnly() was applied to legal configuration. Right now it should only be allowed if
        // an assetFile is configured
        if (readOnly) {
            check(initialDataTransaction == null) { "This Realm is marked as read-only. Read-only Realms cannot use initialData(Realm.Transaction)." }
            checkNotNull(assetFilePath) { "Only Realms provided using 'assetFile(path)' can be marked read-only. No such Realm was provided." }
            check(!deleteRealmIfMigrationNeeded) { "'deleteRealmIfMigrationNeeded()' and read-only Realms cannot be combined" }
            check(compactOnLaunch == null) { "'compactOnLaunch()' and read-only Realms cannot be combined" }
        }

        val selectedRxFactory = when {
            rxFactory == null && Util.isRxJavaAvailable() -> RealmObservableFactory(true)
            else -> rxFactory
        }

        return RealmConfiguration(File(directory, fileName),
                assetFilePath,
                key,
                schemaVersion,
                migration,
                deleteRealmIfMigrationNeeded,
                durability,
                RealmConfiguration.createSchemaMediator(modules, debugSchema),
                selectedRxFactory,
                initialDataTransaction,
                readOnly,
                compactOnLaunch,
                false,
                maxNumberOfActiveVersions
        )
    }

    //-----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------

    private fun checkModule(module: Any) {
        require(module.javaClass.isAnnotationPresent(RealmModule::class.java)) {
            "${module.javaClass.canonicalName} is not a RealmModule. Add @RealmModule to the class definition."
        }
    }
}
