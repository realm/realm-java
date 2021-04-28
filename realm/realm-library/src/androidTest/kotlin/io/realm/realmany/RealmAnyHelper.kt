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

package io.realm.realmany

import io.realm.RealmAny
import io.realm.RealmAny.Type
import io.realm.TestHelper
import io.realm.entities.PrimaryKeyAsString
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*

class RealmAnyHelper {
    companion object {
        fun generateRealmAnyValues(): ArrayList<RealmAny> {
            val realmAnyValues = arrayListOf<RealmAny>()

            for (type in RealmAny.Type.values()) {
                when (type) {
                    RealmAny.Type.INTEGER -> {
                        for (i in 0..9) {
                            realmAnyValues.add(RealmAny.valueOf(i.toByte()))
                            realmAnyValues.add(RealmAny.valueOf((i).toShort()))
                            realmAnyValues.add(RealmAny.valueOf((i).toInt()))
                            realmAnyValues.add(RealmAny.valueOf((i).toLong()))
                        }
                    }
                    RealmAny.Type.BOOLEAN -> {
                        realmAnyValues.add(RealmAny.valueOf(false))
                        realmAnyValues.add(RealmAny.valueOf(true))
                    }
                    RealmAny.Type.STRING -> {
                        for (i in 0..4) {
                            realmAnyValues.add(RealmAny.valueOf("hello world $i"))
                        }
                        for (i in 0..4) {
                            realmAnyValues.add(RealmAny.valueOf("HELLO WORLD $i"))
                        }
                    }
                    RealmAny.Type.BINARY -> {
                        realmAnyValues.add(RealmAny.valueOf(byteArrayOf(0, 0, 0)))
                        realmAnyValues.add(RealmAny.valueOf(byteArrayOf(0, 1, 0)))
                        realmAnyValues.add(RealmAny.valueOf(byteArrayOf(0, 1, 1)))
                        realmAnyValues.add(RealmAny.valueOf(byteArrayOf(1, 1, 0)))
                        realmAnyValues.add(RealmAny.valueOf(byteArrayOf(1, 1, 1)))
                    }
                    RealmAny.Type.DATE -> {
                        for (i in 0..4) {
                            realmAnyValues.add(RealmAny.valueOf(Date(i.toLong())))
                        }
                    }
                    RealmAny.Type.FLOAT -> {
                        for (i in 0..9) {
                            realmAnyValues.add(RealmAny.valueOf((4 + i * 0.1).toFloat()))
                        }
                    }
                    RealmAny.Type.DOUBLE -> {
                        for (i in 0..9) {
                            realmAnyValues.add(RealmAny.valueOf((4 + i * 0.1).toDouble()))
                        }
                    }
                    RealmAny.Type.DECIMAL128 -> {
                        for (i in 0..4) {
                            realmAnyValues.add(RealmAny.valueOf(Decimal128(i.toLong())))
                        }
                    }
                    RealmAny.Type.OBJECT_ID -> {
                        for (i in 0..4) {
                            realmAnyValues.add(RealmAny.valueOf(ObjectId(TestHelper.generateObjectIdHexString(i))))
                        }
                    }
                    RealmAny.Type.UUID -> {
                        for (i in 0..4) {
                            realmAnyValues.add(RealmAny.valueOf(UUID.fromString(TestHelper.generateUUIDString(i))))
                        }
                    }
                    RealmAny.Type.OBJECT -> {
                        for (i in 0..5) {
                            realmAnyValues.add(RealmAny.valueOf(PrimaryKeyAsString("item $i")))
                        }
                    }
                    RealmAny.Type.NULL -> {
                        for (i in 1..9) {
                            realmAnyValues.add(RealmAny.nullValue())
                        }
                    }

                    else -> throw AssertionError("Missing case for type: ${type.name}")
                }
            }

            return realmAnyValues
        }
    }
}
