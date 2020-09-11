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

class MutableNamedParametersRealmConfigBuilder private constructor(
        context: Context,
        private var directory: File = context.filesDir,
        private var fileName: String = Realm.DEFAULT_REALM_NAME,
        private var assetFilePath: String? = null,
        private var key: ByteArray? = null,
        private var schemaVersion: Long = 0L,
        private var migration: RealmMigration? = null,
        private var deleteRealmIfMigrationNeeded: Boolean = false,
        private var initialDataTransaction: Realm.Transaction? = null,
        private var readOnly: Boolean = false,
        private var compactOnLaunch: CompactOnLaunchCallback? = null,
        private var maxNumberOfActiveVersions: Long = Long.MAX_VALUE,
        private var inMemory: Boolean = false,
        private var rxFactory: RxObservableFactory? = null,
        private var schema: List<Class<out RealmModel>> = listOf(),
        private var providedModules: List<Any> = listOf()
) {

    private val modules: MutableSet<Any> = hashSetOf()
    private val debugSchema: MutableSet<Class<out RealmModel>> = hashSetOf()

    private lateinit var durability: OsRealmConfig.Durability

    init {
        RealmCore.loadLibrary(context)
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
            require(key!!.size == Realm.ENCRYPTION_KEY_LENGTH) { "The provided key must be ${Realm.ENCRYPTION_KEY_LENGTH} bytes. Yours was: ${key!!.size}" }
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
            require(assetFilePath!!.isNotEmpty()) { "A non-empty asset file path must be provided" }

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
        initializeBuilder()

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

    fun directory(block: () -> File) { directory = block() }
    fun fileName(block: () -> String) { fileName = block() }
    fun assetFilePath(block: () -> String) { assetFilePath = block() }
    fun key(block: () -> ByteArray) { key = block() }
    fun schemaVersion(block: () -> Long) { schemaVersion = block() }
    fun migration(block: () -> RealmMigration) { migration = block() }
    fun deleteRealmIfMigrationNeeded(block: () -> Boolean ) { deleteRealmIfMigrationNeeded = block() }
    fun initialDataTransaction(block: () -> Realm.Transaction) { initialDataTransaction = block() }
    fun readOnly(block: () -> Boolean) { readOnly = block() }
    fun compactOnLaunch(block: () -> CompactOnLaunchCallback) { compactOnLaunch = block() }
    fun maxNumberOfActiveVersions(block: () -> Long) { maxNumberOfActiveVersions = block() }
    fun inMemory(block: () -> Boolean) { inMemory = block() }
    fun rxFactory(block: () -> RxObservableFactory) { rxFactory = block() }
    fun schema(block: () -> List<Class<out RealmModel>>) { schema = block() }
    fun providedModules(block: () -> List<Any>) { providedModules = block() }


    //-----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------

    private fun checkModule(module: Any) {
        require(module.javaClass.isAnnotationPresent(RealmModule::class.java)) {
            "${module.javaClass.canonicalName} is not a RealmModule. Add @RealmModule to the class definition."
        }
    }

    companion object {
        fun mutableRealmConfigBuilder(
                context: Context,
                block: MutableNamedParametersRealmConfigBuilder.() -> Unit = {}
        ): RealmConfiguration {
            return MutableNamedParametersRealmConfigBuilder(context)
                    .apply(block)
                    .build()
        }
    }
}
