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
package io.realm.buildtransformer

import java.io.File

/**
 * Custom ClassLoader that can be used to dynamically load a class that have been dynamically modified, i.e. we
 * load it using the ByteArray that represents it.
 */
class DynamicClassLoader(parentLoader: ClassLoader) : ClassLoader(parentLoader) {

    fun loadClass(qualifiedClassName: String, pool: Set<File>): Class<*> {
        val classFile: File = pool.find {
            it.absolutePath.endsWith("${qualifiedClassName.replace(".", "/")}.class")
        } ?: throw IllegalStateException("Class pool does not contain: $qualifiedClassName")

        val classBytes: ByteArray = classFile.readBytes()
        return defineClass(qualifiedClassName, classBytes, 0, classBytes.size)
    }
}