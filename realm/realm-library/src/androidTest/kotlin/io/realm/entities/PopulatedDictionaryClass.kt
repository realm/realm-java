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

package io.realm.entities

import io.realm.*
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*

open class PopulatedDictionaryClass : RealmObject() {

    val populatedBooleanDictionary: RealmDictionary<Boolean> =
            RealmDictionary<Boolean>().init(listOf(KEY_HELLO to VALUE_BOOLEAN_HELLO, KEY_BYE to VALUE_BOOLEAN_BYE, KEY_NULL to null))
    val populatedStringDictionary: RealmDictionary<String> =
            RealmDictionary<String>().init(listOf(KEY_HELLO to VALUE_STRING_HELLO, KEY_BYE to VALUE_STRING_BYE, KEY_NULL to null))
    val populatedIntDictionary: RealmDictionary<Int> =
            RealmDictionary<Int>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO, KEY_BYE to VALUE_NUMERIC_BYE, KEY_NULL to null))
    val populatedFloatDictionary: RealmDictionary<Float> =
            RealmDictionary<Float>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toFloat(), KEY_BYE to VALUE_NUMERIC_BYE.toFloat(), KEY_NULL to null))
    val populatedLongDictionary: RealmDictionary<Long> =
            RealmDictionary<Long>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toLong(), KEY_BYE to VALUE_NUMERIC_BYE.toLong(), KEY_NULL to null))
    val populatedShortDictionary: RealmDictionary<Short> =
            RealmDictionary<Short>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toShort(), KEY_BYE to VALUE_NUMERIC_BYE.toShort(), KEY_NULL to null))
    val populatedDoubleDictionary: RealmDictionary<Double> =
            RealmDictionary<Double>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toDouble(), KEY_BYE to VALUE_NUMERIC_BYE.toDouble(), KEY_NULL to null))
    val populatedByteDictionary: RealmDictionary<Byte> =
            RealmDictionary<Byte>().init(listOf(KEY_HELLO to VALUE_NUMERIC_HELLO.toByte(), KEY_BYE to VALUE_NUMERIC_BYE.toByte(), KEY_NULL to null))
    val populatedBinaryDictionary: RealmDictionary<ByteArray> =
            RealmDictionary<ByteArray>().init(listOf(KEY_HELLO to VALUE_BINARY_HELLO, KEY_BYE to VALUE_BINARY_BYE, KEY_NULL to null))
    val populatedDateDictionary: RealmDictionary<Date> =
            RealmDictionary<Date>().init(listOf(KEY_HELLO to VALUE_DATE_HELLO, KEY_BYE to VALUE_DATE_BYE, KEY_NULL to null))
    val populatedObjectIdDictionary: RealmDictionary<ObjectId> =
            RealmDictionary<ObjectId>().init(listOf(KEY_HELLO to VALUE_OBJECT_ID_HELLO, KEY_BYE to VALUE_OBJECT_ID_BYE, KEY_NULL to null))
    val populatedUUIDDictionary: RealmDictionary<UUID> =
            RealmDictionary<UUID>().init(listOf(KEY_HELLO to VALUE_UUID_HELLO, KEY_BYE to VALUE_UUID_BYE, KEY_NULL to null))
    val populatedDecimal128Dictionary: RealmDictionary<Decimal128> =
            RealmDictionary<Decimal128>().init(listOf(KEY_HELLO to VALUE_DECIMAL128_HELLO, KEY_BYE to VALUE_DECIMAL128_BYE, KEY_NULL to null))
    val populatedRealmModelDictionary: RealmDictionary<DogPrimaryKey> =
            RealmDictionary<DogPrimaryKey>().init(listOf(KEY_HELLO to VALUE_LINK_HELLO, KEY_BYE to VALUE_LINK_BYE, KEY_NULL to null))
    val populatedMixedDictionary: RealmDictionary<Mixed> =
            MixedType.values()
                    .map { mixedType ->
                        DictionaryKey.values().map { key ->
                            Pair("${key.name}_${mixedType.name}", getMixedForType(mixedType, key))
                        }
                    }
                    .flatten()
                    .let { keyValuePairs ->
                        RealmDictionary<Mixed>().init(keyValuePairs)
                    }

    companion object {
        const val CLASS_NAME = "PopulatedDictionaryClass"
    }
}
