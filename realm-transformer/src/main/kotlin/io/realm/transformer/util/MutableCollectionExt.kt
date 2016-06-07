/*
 * Copyright 2016 Realm Inc.
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

package io.realm.transformer.util

import com.android.SdkConstants
import com.android.build.api.transform.TransformInput
import javassist.ClassPool
import java.io.File
import java.util.jar.JarFile

// Find all classes and append their names to the given String collection
fun MutableCollection<TransformInput>.appendThisToClassNames(classNames: MutableCollection<String>) {
    for (item: TransformInput in this) {
        for (input in item.directoryInputs) {
            val dirPath = input.file.absolutePath
            input.file.walkTopDown().filter { it.isFile && it.endsWith(SdkConstants.DOT_CLASS) }.forEach {
                val qualifiedClassName = it.absolutePath.substring(
                        dirPath.length + 1,
                        input.file.absolutePath.length - SdkConstants.DOT_CLASS.length)
                        .replace(File.separatorChar, '.')
                classNames.add(qualifiedClassName)
            }
        }

        for (input in item.jarInputs) {
            val jarFile = JarFile(input.file)
            jarFile.entries().asSequence().filter { it.name.endsWith(SdkConstants.DOT_CLASS) }.forEach {
                val path = it.name
                // The jar might not using File.separatorChar as the path separator. So we just replace both `\` and
                // `/`. It depends on how the jar file was created.
                // See http://stackoverflow.com/questions/13846000/file-separators-of-path-name-of-zipentry
                val qualifiedClassName = path.substring(0, path.length - SdkConstants.DOT_CLASS.length)
                        .replace('/', '.')
                        .replace('\\', '.')
                classNames.add(qualifiedClassName)
            }
        }
    }
}

// Add all the classes in this collection to the given ClassPool
fun MutableCollection<TransformInput>.appendThisToClassPool(classPool: ClassPool) {
    this.forEach {
        it.directoryInputs.forEach { classPool.appendClassPath(it.file.absolutePath) }
        it.jarInputs.forEach { classPool.appendClassPath(it.file.absolutePath) }
    }
}
