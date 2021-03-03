/*
 * Copyright 2020 Realm Inc.
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

package io.realm

import io.realm.rule.TestRealmConfigurationFactory

/**
// * Contains all the methods to test in RealmDictionary plus some other convenience ones.
 */
interface DictionaryTester : GenericTester {
    override fun setUp(configFactory: TestRealmConfigurationFactory) = Unit     // Not needed here
    fun constructorWithAnotherMap()
    fun isManaged()
    fun isValid()
    fun isFrozen()
    fun size()
    fun isEmpty()
    fun containsKey()
    fun containsValue()
    fun get()
    fun put()
    fun putRequired()
    fun remove()
    fun putAll()
    fun clear()
    fun keySet()
    fun values()
    fun entrySet()
    fun freeze()
    fun copyToRealm()
    fun copyFromRealm()
    fun fieldAccessors()
    fun changeListener()
}
