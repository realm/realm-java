package io.realm.kotlin.config

import io.realm.*
import io.realm.exceptions.RealmException
import io.realm.internal.OsRealmConfig
import io.realm.internal.RealmProxyMediator
import io.realm.internal.Util
import io.realm.internal.modules.CompositeMediator
import io.realm.internal.modules.FilterableMediator
import io.realm.rx.RealmObservableFactory
import io.realm.rx.RxObservableFactory
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * TODO: not finished, just to give a taste on how it would look like
 */
data class RealmConfig(
        val realmPath: File,
        val assetFilePath: String? = null,
        val key: ByteArray? = null,
        val schemaVersion: Long = 0L,
        val migration: RealmMigration? = null,
        val deleteRealmIfMigrationNeeded: Boolean = false,
        val durability: OsRealmConfig.Durability = OsRealmConfig.Durability.FULL,
        val modules: HashSet<Any> = hashSetOf(),
        val debugSchema: HashSet<Class<out RealmModel>> = hashSetOf(),
        val rxObservableFactory: RxObservableFactory?,
        val initialDataTransaction: Realm.Transaction? = null,
        val readOnly: Boolean = false,
        val inMemory: Boolean = false,
        val compactOnLaunch: CompactOnLaunchCallback?,
        val isRecoveryConfiguration: Boolean = false,
        val maxNumberOfActiveVersions: Long = Long.MAX_VALUE
) {

    private val schemaMediator: RealmProxyMediator

    init {
        if (inMemory) {
            check(assetFilePath.isNullOrEmpty()) { "Realm can not use in-memory configuration if asset file is present." }
        }

        // Check that readOnly() was applied to legal configuration. Right now it should only be allowed if
        // an assetFile is configured
        if (readOnly) {
            check(initialDataTransaction == null) { "This Realm is marked as read-only. Read-only Realms cannot use initialData(Realm.Transaction)." }
            checkNotNull(assetFilePath) { "Only Realms provided using 'assetFile(path)' can be marked read-only. No such Realm was provided." }
            check(!deleteRealmIfMigrationNeeded) { "'deleteRealmIfMigrationNeeded()' and read-only Realms cannot be combined" }
            check(compactOnLaunch == null) { "'compactOnLaunch()' and read-only Realms cannot be combined" }
        }

        schemaMediator = createSchemaMediator(modules, debugSchema)

        // TODO: more validation
    }


}

/**
 * TODO
 */
object DefaultModuleStore {

    val defaultModule = Realm.getDefaultModule()
    val defaultModuleMediator: RealmProxyMediator?

    init {
        defaultModuleMediator = if (defaultModule != null) {
            val mediator = getModuleMediator(defaultModule.javaClass.canonicalName!!)
            if (!mediator.transformerApplied()) {
                throw ExceptionInInitializerError("RealmTransformer doesn't seem to be applied." +
                        " Please update the project configuration to use the Realm Gradle plugin." +
                        " See https://realm.io/news/android-installation-change/")
            }
            mediator
        } else {
            null
        }
    }
}

private fun getModuleMediator(fullyQualifiedModuleClassName: String): RealmProxyMediator {
    val moduleNameParts = fullyQualifiedModuleClassName.split("\\.".toRegex()).toTypedArray()
    val moduleSimpleName = moduleNameParts[moduleNameParts.size - 1]
    val mediatorName = String.format(Locale.US, "io.realm.%s%s", moduleSimpleName, "Mediator")
    return try {
        val clazz = Class.forName(mediatorName)
        val constructor = clazz.declaredConstructors[0]
        constructor.isAccessible = true
        constructor.newInstance() as RealmProxyMediator
    } catch (e: ClassNotFoundException) {
        throw RealmException("Could not find $mediatorName", e)
    } catch (e: InvocationTargetException) {
        throw RealmException("Could not create an instance of $mediatorName", e)
    } catch (e: InstantiationException) {
        throw RealmException("Could not create an instance of $mediatorName", e)
    } catch (e: IllegalAccessException) {
        throw RealmException("Could not create an instance of $mediatorName", e)
    }
}

private fun createSchemaMediator(
        modules: Set<Any>,
        debugSchema: Set<Class<out RealmModel>>
): RealmProxyMediator {
    // If using debug schema, uses special mediator.
    if (debugSchema.isNotEmpty()) {
        return FilterableMediator(RealmConfiguration.DEFAULT_MODULE_MEDIATOR, debugSchema)
    }

    // If only one module, uses that mediator directly.
    if (modules.size == 1) {
        return getModuleMediator(modules.iterator().next().javaClass.canonicalName!!)
    }

    // Otherwise combines all mediators.
    val mediators = arrayOfNulls<RealmProxyMediator>(modules.size)
    for ((i, module) in modules.withIndex()) {
        mediators[i] = getModuleMediator(module.javaClass.canonicalName!!)
    }
    return CompositeMediator(*mediators)
}

private fun getRxFactory(): RxObservableFactory? = when {
    Util.isRxJavaAvailable() -> RealmObservableFactory(true)
    else -> null
}
