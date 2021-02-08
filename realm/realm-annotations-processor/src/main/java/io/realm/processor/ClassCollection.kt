/*
 * Copyright 2019 Realm Inc.
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
package io.realm.processor

import java.util.LinkedHashMap
import java.util.LinkedHashSet

/**
 * Wrapper around all Realm model classes metadata found during processing. It also allows easy
 * lookup for specific class data.
 */
class ClassCollection {

    // These three collections should always stay in sync
    private val qualifiedNameClassMap = LinkedHashMap<QualifiedClassName, ClassMetaData>()
    private val classSet = LinkedHashSet<ClassMetaData>()
    val classes: Set<ClassMetaData>
        get() = classSet.toSet()

    fun addClass(metadata: ClassMetaData) {
        classSet.add(metadata)
        qualifiedNameClassMap[metadata.qualifiedClassName] = metadata
    }

    fun getClassFromQualifiedName(className: QualifiedClassName): ClassMetaData {
        return qualifiedNameClassMap[className]
                ?: throw IllegalArgumentException("Class $className was not found")
    }

    fun getClassFromQualifiedNameOrNull(className: QualifiedClassName): ClassMetaData? {
        return qualifiedNameClassMap[className]
    }

    fun size(): Int {
        return classSet.size
    }

    fun containsQualifiedClass(qualifiedClassName: QualifiedClassName?): Boolean {
        return qualifiedNameClassMap.containsKey(qualifiedClassName)
    }
}
