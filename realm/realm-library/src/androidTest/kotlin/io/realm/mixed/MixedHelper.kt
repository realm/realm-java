package io.realm.mixed

import io.realm.Mixed
import io.realm.MixedType
import io.realm.TestHelper
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*
import kotlin.collections.ArrayList

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
                    }   // FIXME: ADD TESTS
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