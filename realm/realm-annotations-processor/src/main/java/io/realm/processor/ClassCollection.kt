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
package io.realm.processor

import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.LinkedHashSet

/**
 * Wrapper around all Realm model classes metadata found during processing. It also
 * allows easy lookup for specific class data.
 */
class ClassCollection {

    // These three collections should always stay in sync
    private val qualifiedNameClassMap = LinkedHashMap<String, ClassMetaData>()
    private val classSet = LinkedHashSet<ClassMetaData>()

    val classes: Set<ClassMetaData>
        get() = Collections.unmodifiableSet(classSet)

    fun addClass(metadata: ClassMetaData) {
        classSet.add(metadata)
        qualifiedNameClassMap[metadata.fullyQualifiedClassName] = metadata
    }

    fun getClassFromQualifiedName(qualifiedJavaClassName: String?): ClassMetaData {
        return qualifiedNameClassMap[qualifiedJavaClassName]
                ?: throw IllegalArgumentException("Class $qualifiedJavaClassName was not found")
    }

    fun size(): Int {
        return classSet.size
    }

    fun containsQualifiedClass(qualifiedClassName: String): Boolean {
        return qualifiedNameClassMap.containsKey(qualifiedClassName)
    }
}