/*
 * Copyright 2021 Realm Inc.
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

package io.realm.mixed

import io.realm.Mixed
import io.realm.MixedType
import io.realm.TestHelper
import io.realm.entities.PrimaryKeyAsString
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*

class MixedHelper {
    companion object {
        fun generateMixedValues(): ArrayList<Mixed> {
            val mixedValues = arrayListOf<Mixed>()

            for (type in MixedType.values()) {
                when (type) {
                    MixedType.INTEGER -> {
                        for (i in 0..9) {
                            mixedValues.add(Mixed.valueOf(i.toByte()))
                            mixedValues.add(Mixed.valueOf((i).toShort()))
                            mixedValues.add(Mixed.valueOf((i).toInt()))
                            mixedValues.add(Mixed.valueOf((i).toLong()))
                        }
                    }
                    MixedType.BOOLEAN -> {
                        mixedValues.add(Mixed.valueOf(false))
                        mixedValues.add(Mixed.valueOf(true))
                    }
                    MixedType.STRING -> {
                        for (i in 0..4) {
                            mixedValues.add(Mixed.valueOf("hello world $i"))
                        }
                        for (i in 0..4) {
                            mixedValues.add(Mixed.valueOf("HELLO WORLD $i"))
                        }
                    }
                    MixedType.BINARY -> {
                        mixedValues.add(Mixed.valueOf(byteArrayOf(0, 0, 0)))
                        mixedValues.add(Mixed.valueOf(byteArrayOf(0, 1, 0)))
                        mixedValues.add(Mixed.valueOf(byteArrayOf(0, 1, 1)))
                        mixedValues.add(Mixed.valueOf(byteArrayOf(1, 1, 0)))
                        mixedValues.add(Mixed.valueOf(byteArrayOf(1, 1, 1)))
                    }
                    MixedType.DATE -> {
                        for (i in 0..4) {
                            mixedValues.add(Mixed.valueOf(Date(i.toLong())))
                        }
                    }
                    MixedType.FLOAT -> {
                        for (i in 0..9) {
                            mixedValues.add(Mixed.valueOf((4 + i * 0.1).toFloat()))
                        }
                    }
                    MixedType.DOUBLE -> {
                        for (i in 0..9) {
                            mixedValues.add(Mixed.valueOf((4 + i * 0.1).toDouble()))
                        }
                    }
                    MixedType.DECIMAL128 -> {
                        for (i in 0..4) {
                            mixedValues.add(Mixed.valueOf(Decimal128(i.toLong())))
                        }
                    }
                    MixedType.OBJECT_ID -> {
                        for (i in 0..4) {
                            mixedValues.add(Mixed.valueOf(ObjectId(TestHelper.generateObjectIdHexString(i))))
                        }
                    }
                    MixedType.UUID -> {
                        for (i in 0..4) {
                            mixedValues.add(Mixed.valueOf(UUID.fromString(TestHelper.generateUUIDString(i))))
                        }
                    }
                    MixedType.OBJECT -> {
                        for (i in 0..5) {
                            mixedValues.add(Mixed.valueOf(PrimaryKeyAsString("item $i")))
                        }
                    }
                    MixedType.NULL -> {
                        for (i in 1..9) {
                            mixedValues.add(Mixed.nullValue())
                        }
                    }

                    else -> throw AssertionError("Missing case for type: ${type.name}")
                }
            }

            return mixedValues
        }
    }
}
