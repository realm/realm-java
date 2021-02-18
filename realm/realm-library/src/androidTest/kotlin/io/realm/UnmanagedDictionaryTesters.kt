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

import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*

/**
 * This file contains all testers for all unmanaged dictionary types.
 *
 * Add new testers ad-hoc whenever we support new types for dictionaries.
 */

class UnmanagedLong(
        private val testKeys: List<String>,
        private val testValues: List<Long?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<Long>()
    override fun isValid() = assertUnmanagedIsValid<Long>()
    override fun isFrozen() = assertUnmanagedIsFrozen<Long>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<Long>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedInteger(
        private val testKeys: List<String>,
        private val testValues: List<Int?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<Int>()
    override fun isValid() = assertUnmanagedIsValid<Int>()
    override fun isFrozen() = assertUnmanagedIsFrozen<Int>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<Int>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedShort(
        private val testKeys: List<String>,
        private val testValues: List<Short?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<Short>()
    override fun isValid() = assertUnmanagedIsValid<Short>()
    override fun isFrozen() = assertUnmanagedIsFrozen<Short>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<Short>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedByte(
        private val testKeys: List<String>,
        private val testValues: List<Byte?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<Byte>()
    override fun isValid() = assertUnmanagedIsValid<Byte>()
    override fun isFrozen() = assertUnmanagedIsFrozen<Byte>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<Byte>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedFloat(
        private val testKeys: List<String>,
        private val testValues: List<Float?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<Float>()
    override fun isValid() = assertUnmanagedIsValid<Float>()
    override fun isFrozen() = assertUnmanagedIsFrozen<Float>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<Float>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedDouble(
        private val testKeys: List<String>,
        private val testValues: List<Double?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<Double>()
    override fun isValid() = assertUnmanagedIsValid<Double>()
    override fun isFrozen() = assertUnmanagedIsFrozen<Double>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<Double>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedString(
        private val testKeys: List<String>,
        private val testValues: List<String?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<String>()
    override fun isValid() = assertUnmanagedIsValid<String>()
    override fun isFrozen() = assertUnmanagedIsFrozen<String>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<String>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedBoolean(
        private val testKeys: List<String>,
        private val testValues: List<Boolean?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<Boolean>()
    override fun isValid() = assertUnmanagedIsValid<Boolean>()
    override fun isFrozen() = assertUnmanagedIsFrozen<Boolean>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<Boolean>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedDate(
        private val testKeys: List<String>,
        private val testValues: List<Date?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<Date>()
    override fun isValid() = assertUnmanagedIsValid<Date>()
    override fun isFrozen() = assertUnmanagedIsFrozen<Date>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<Date>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedDecimal128(
        private val testKeys: List<String>,
        private val testValues: List<Decimal128?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<Decimal128>()
    override fun isValid() = assertUnmanagedIsValid<Decimal128>()
    override fun isFrozen() = assertUnmanagedIsFrozen<Decimal128>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<Decimal128>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedBoxedByteArray(
        private val testKeys: List<String>,
        private val testValues: List<Array<Byte>?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<Array<Byte>>()
    override fun isValid() = assertUnmanagedIsValid<Array<Byte>>()
    override fun isFrozen() = assertUnmanagedIsFrozen<Array<Byte>>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<Array<Byte>>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedByteArray(
        private val testKeys: List<String>,
        private val testValues: List<ByteArray?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<ByteArray>()
    override fun isValid() = assertUnmanagedIsValid<ByteArray>()
    override fun isFrozen() = assertUnmanagedIsFrozen<ByteArray>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<ByteArray>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedObjectId(
        private val testKeys: List<String>,
        private val testValues: List<ObjectId?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<ObjectId>()
    override fun isValid() = assertUnmanagedIsValid<ObjectId>()
    override fun isFrozen() = assertUnmanagedIsFrozen<ObjectId>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<ObjectId>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedUUID(
        private val testKeys: List<String>,
        private val testValues: List<UUID?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<UUID>()
    override fun isValid() = assertUnmanagedIsValid<UUID>()
    override fun isFrozen() = assertUnmanagedIsFrozen<UUID>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<UUID>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedLink(
        private val testKeys: List<String>,
        private val testValues: List<RealmModel?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<RealmModel>()
    override fun isValid() = assertUnmanagedIsValid<RealmModel>()
    override fun isFrozen() = assertUnmanagedIsFrozen<RealmModel>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<RealmModel>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}

class UnmanagedMixed(
        private val type: String,
        private val testKeys: List<String>,
        private val testValues: List<Mixed?>
) : DictionaryTester {

    override fun toString(): String = this.javaClass.simpleName + "-" + type

    override fun addRealmInstance(realm: Realm) = Unit
    override fun isTesterManaged(): Boolean = false
    override fun constructorWithAnotherMap() = assertConstructorWithAnotherMap(testKeys, testValues)
    override fun isManaged() = assertUnmanagedIsManaged<Mixed>()
    override fun isValid() = assertUnmanagedIsValid<Mixed>()
    override fun isFrozen() = assertUnmanagedIsFrozen<Mixed>()
    override fun size() = assertUnmanagedSize(testKeys[0], testValues[0])
    override fun isEmpty() = assertUnmanagedIsEmpty(testKeys[0], testValues[0])
    override fun containsKey() = assertUnmanagedContainsKey(testKeys[0], testValues[0])
    override fun containsValue() = assertUnmanagedContainsValue(testKeys[0], testValues[0], testValues[1])
    override fun get() = assertUnmanagedGet(testKeys[0], testValues[0])
    override fun put() = assertUnmanagedPut(testKeys[0], testValues[0])
    override fun remove() = assertUnmanagedRemove(testKeys[0], testValues[0])
    override fun putAll() = assertUnmanagedPutAll(testKeys, testValues)
    override fun clear() = assertUnmanagedClear(testKeys[0], testValues[0])
    override fun keySet() = assertUnmanagedKeySet(testKeys, testValues)
    override fun values() = assertUnmanagedValues(testKeys, testValues)
    override fun entrySet() = assertUnmanagedEntrySet(testKeys, testValues)
    override fun freeze() = assertUnmanagedFreeze<Mixed>()
    override fun copyToRealm() = Unit
    override fun copyFromRealm() = Unit
}
