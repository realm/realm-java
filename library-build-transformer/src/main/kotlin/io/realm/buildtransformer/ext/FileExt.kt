/*
 * Copyright 2018 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
