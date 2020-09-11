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

class TraditionalRealmConfigBuilder(
        context: Context
) {

    private var directory: File = context.filesDir
    private var fileName: String = Realm.DEFAULT_REALM_NAME
    private var assetFilePath: String? = null
    private var key: ByteArray? = null
    private var schemaVersion: Long = 0L
    private var migration: RealmMigration? = null
    private var deleteRealmIfMigrationNeeded: Boolean = false
    private var durability: OsRealmConfig.Durability = OsRealmConfig.Durability.FULL
    private var rxFactory: RxObservableFactory? = null
    private var initialDataTransaction: Realm.Transaction? = null
    private var readOnly: Boolean = false
    private var compactOnLaunch: CompactOnLaunchCallback? = null
    private var maxNumberOfActiveVersions: Long = Long.MAX_VALUE

    private val modules: MutableSet<Any> = hashSetOf()
    private val debugSchema: MutableSet<Class<out RealmModel>> = hashSetOf()

    init {
        RealmCore.loadLibrary(context)

        // FIXME: find a less messy way to get the default module
        if (RealmConfiguration.DEFAULT_MODULE != null) {
            this.modules.add(RealmConfiguration.DEFAULT_MODULE)
        }
    }

    fun name(fileName: String): TraditionalRealmConfigBuilder {
        // Recipe: use require to test function arguments
        require(fileName.isNotEmpty()) { "A non-empty filename must be provided" }
        this.fileName = fileName
        return this
    }

    fun directory(directory: File): TraditionalRealmConfigBuilder {
        require(!directory.isFile) { "'dir' is a file, not a directory: ${directory.absolutePath}." }
        require(!(!directory.exists() && !directory.mkdirs())) { "Could not create the specified directory: ${directory.absolutePath}." }
        require(directory.canWrite()) { "Realm directory is not writable: ${directory.absolutePath}." }
        this.directory = directory
        return this
    }

    fun encryptionKey(key: ByteArray): TraditionalRealmConfigBuilder {
        require(key.size == Realm.ENCRYPTION_KEY_LENGTH) { "The provided key must be ${Realm.ENCRYPTION_KEY_LENGTH} bytes. Yours was: ${key.size}" }
        this.key = key.copyOf(key.size)
        return this
    }

    fun schemaVersion(schemaVersion: Long): TraditionalRealmConfigBuilder {
        require(schemaVersion >= 0) { "Realm schema version numbers must be 0 (zero) or higher. Yours was: $schemaVersion" }
        this.schemaVersion = schemaVersion
        return this
    }

    fun migration(migration: RealmMigration): TraditionalRealmConfigBuilder {
        this.migration = migration
        return this
    }

    fun deleteRealmIfMigrationNeeded(): TraditionalRealmConfigBuilder {
        // Recipe: use check to test object state
        check(assetFilePath.isNullOrEmpty()) { "Realm cannot clear its schema when previously configured to use an asset file by calling assetFile()." }
        deleteRealmIfMigrationNeeded = true
        return this
    }

    fun inMemory(): TraditionalRealmConfigBuilder {
        check(assetFilePath.isNullOrEmpty()) { "Realm can not use in-memory configuration if asset file is present." }
        durability = OsRealmConfig.Durability.MEM_ONLY
        return this
    }

    fun modules(baseModule: Any, vararg additionalModules: Any): TraditionalRealmConfigBuilder {
        modules.clear()
        addModule(baseModule)
        for (element: Any in additionalModules) {
            addModule(element)
        }
        return this
    }

    fun rxFactory(rxFactory: RxObservableFactory): TraditionalRealmConfigBuilder {
        this.rxFactory = rxFactory
        return this
    }

    fun initialData(initialDataTransaction: Realm.Transaction): TraditionalRealmConfigBuilder {
        this.initialDataTransaction = initialDataTransaction
        return this
    }

    fun assetFile(assetFile: String): TraditionalRealmConfigBuilder {
        require(assetFile.isNotEmpty()) { "A non-empty asset file path must be provided" }
        if (durability == OsRealmConfig.Durability.MEM_ONLY) {
            throw RealmException("Realm can not use in-memory configuration if asset file is present.")
        }
        check(!deleteRealmIfMigrationNeeded) { "Realm cannot use an asset file when previously configured to clear its schema in migration by calling deleteRealmIfMigrationNeeded()." }
        assetFilePath = assetFile
        return this
    }

    fun readOnly(): TraditionalRealmConfigBuilder {
        readOnly = true
        return this
    }

    fun compactOnLaunch(): TraditionalRealmConfigBuilder {
        return compactOnLaunch(DefaultCompactOnLaunchCallback())
    }

    fun compactOnLaunch(compactOnLaunch: CompactOnLaunchCallback): TraditionalRealmConfigBuilder {
        this.compactOnLaunch = compactOnLaunch
        return this
    }

    fun maxNumberOfActiveVersions(number: Long): TraditionalRealmConfigBuilder {
        require(number >= 1) { "Only positive numbers above 0 are allowed. Yours was: $number" }
        this.maxNumberOfActiveVersions = number
        return this
    }

    fun schema(
            firstClass: Class<out RealmModel>,
            vararg additionalClasses: Class<out RealmModel>
    ): TraditionalRealmConfigBuilder {
        modules.clear()
        modules.add(RealmConfiguration.DEFAULT_MODULE_MEDIATOR)
        debugSchema.add(firstClass)
        debugSchema.addAll(additionalClasses.toCollection(mutableListOf()))
        return this
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
        if (rxFactory == null && Util.isRxJavaAvailable()) {
            rxFactory = RealmObservableFactory(true)
        }
        return RealmConfiguration(File(directory, fileName),
                assetFilePath,
                key,
                schemaVersion,
                migration,
                deleteRealmIfMigrationNeeded,
                durability,
                RealmConfiguration.createSchemaMediator(modules, debugSchema),
                rxFactory,
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

    private fun addModule(module: Any): TraditionalRealmConfigBuilder {
        checkModule(module)
        modules.add(module)
        return this
    }

    private fun checkModule(module: Any) {
        require(module.javaClass.isAnnotationPresent(RealmModule::class.java)) {
            module.javaClass.canonicalName + " is not a RealmModule. " +
                    "Add @RealmModule to the class definition."
        }
    }
}
