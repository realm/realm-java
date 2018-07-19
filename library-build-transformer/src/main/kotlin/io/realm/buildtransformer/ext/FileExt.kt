package io.realm.buildtransformer.ext

import java.io.File
import java.util.*
import kotlin.reflect.KProperty

// Add support for extension properties
// Credit: https://stackoverflow.com/questions/36502413/extension-fields-in-kotlin
class FieldProperty<R, T : Any>(val initializer: (R) -> T = { throw IllegalStateException("Field Property not initialized.") }) {
    private val map = WeakHashMap<R, T>()

    operator fun getValue(thisRef: R, property: KProperty<*>): T =
            map[thisRef] ?: setValue(thisRef, property, initializer(thisRef))

    operator fun setValue(thisRef: R, property: KProperty<*>, value: T): T {
        map[thisRef] = value
        return value
    }
}

/**
 * If the file points to a compiled `.class` file, this property stores the path leading to the root folder of the
 * package hierarchy.
 */
var File.packageHierarchyRootDir: String by FieldProperty<File, String>()

/**
 * `true` if the file is marked for deletion after being processed.
 */
var File.shouldBeDeleted: Boolean by FieldProperty<File, Boolean>()
