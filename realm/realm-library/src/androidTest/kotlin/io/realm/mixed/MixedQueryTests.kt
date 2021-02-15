package io.realm.mixed

import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.entities.*
import io.realm.kotlin.where
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class MixedQueryTests(
        private val testingType: MixedType,
        private val first: Mixed,
        private val second: Mixed,
        private val third: Mixed
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): MutableList<Array<Any>> {
            val list = mutableListOf<Array<Any>>()

            for (type in MixedType.values()) {
                when (type) {
                    MixedType.INTEGER -> {
                        list.add(arrayOf(MixedType.INTEGER,
                                Mixed.valueOf(0.toByte()),
                                Mixed.valueOf(15.toByte()),
                                Mixed.valueOf((-1).toByte())
                        ))
                        list.add(arrayOf(MixedType.INTEGER,
                                Mixed.valueOf(10.toShort()),
                                Mixed.valueOf(25.toShort()),
                                Mixed.valueOf((-30).toByte())
                        ))
                        list.add(arrayOf(MixedType.INTEGER,
                                Mixed.valueOf(25.toInt()),
                                Mixed.valueOf(35.toInt()),
                                Mixed.valueOf((-20).toByte())
                        ))
                        list.add(arrayOf(MixedType.INTEGER,
                                Mixed.valueOf(10.toLong()),
                                Mixed.valueOf(39.toLong()),
                                Mixed.valueOf((-10).toByte())
                        ))
                    }
                    MixedType.BOOLEAN -> list.add(arrayOf(
                            MixedType.BOOLEAN,
                            Mixed.valueOf(false),
                            Mixed.valueOf(true)
                    ))
                    MixedType.STRING -> list.add(arrayOf(
                            MixedType.STRING,
                            Mixed.valueOf("hello world 3"),
                            Mixed.valueOf("hello world 6"),
                            Mixed.valueOf("missing string")
                    ))
                    MixedType.BINARY -> list.add(arrayOf(
                            MixedType.BINARY,
                            Mixed.valueOf(byteArrayOf(0, 0, 0)),
                            Mixed.valueOf(byteArrayOf(0, 1, 1)),
                            Mixed.valueOf(byteArrayOf(1, 1, 1))
                    ))
                    MixedType.DATE -> list.add(arrayOf(
                            MixedType.DATE,
                            Mixed.valueOf(Date(200)),
                            Mixed.valueOf(Date(600)),
                            Mixed.valueOf(Date(100000))
                    ))
                    MixedType.FLOAT -> list.add(arrayOf(
                            MixedType.FLOAT,
                            Mixed.valueOf(0.3.toFloat()),
                            Mixed.valueOf(0.7.toFloat()),
                            Mixed.valueOf(29.toFloat())
                    ))
                    MixedType.DOUBLE -> list.add(arrayOf(
                            MixedType.DOUBLE,
                            Mixed.valueOf(1.3.toDouble()),
                            Mixed.valueOf(1.7.toDouble()),
                            Mixed.valueOf(60.toDouble())
                    ))
                    MixedType.DECIMAL128 -> list.add(arrayOf(
                            MixedType.DECIMAL128,
                            Mixed.valueOf(Decimal128(5)),
                            Mixed.valueOf(Decimal128(7)),
                            Mixed.valueOf(Decimal128(20))
                    ))
                    MixedType.OBJECT_ID -> list.add(arrayOf(
                            MixedType.OBJECT_ID,
                            Mixed.valueOf(ObjectId(Date(200))),
                            Mixed.valueOf(ObjectId(Date(500))),
                            Mixed.valueOf(ObjectId(Date(50000)))
                    ))
                    MixedType.UUID -> list.add(arrayOf(
                            MixedType.UUID,
                            Mixed.valueOf(TestHelper.generateUUIDString(3)),
                            Mixed.valueOf(TestHelper.generateUUIDString(6)),
                            Mixed.valueOf(UUID.randomUUID())
                    ))
                    MixedType.OBJECT,   // Not tested in this test suite
                    MixedType.NULL -> list.add(arrayOf(
                            MixedType.NULL,
                            Mixed.nullValue(),
                            Mixed.nullValue(),
                            Mixed.nullValue()
                    ))
                    else -> throw AssertionError("Missing case for type: ${type.name}")
                }
            }

            return list
        }
    }

    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    private fun initializeTestData() {
        val mixedValues = arrayListOf<Mixed>()

        for (type in MixedType.values()) {
            when (type) {
                MixedType.INTEGER -> {
                    for (i in 0..39) {
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
                    for (i in 0..9) {
                        mixedValues.add(Mixed.valueOf("hello world $i"))
                    }
                }
                MixedType.BINARY -> {
                    mixedValues.add(Mixed.valueOf(byteArrayOf(0, 0, 0)))
                    mixedValues.add(Mixed.valueOf(byteArrayOf(0, 1, 0)))
                    mixedValues.add(Mixed.valueOf(byteArrayOf(0, 1, 1)))
                    mixedValues.add(Mixed.valueOf(byteArrayOf(1, 1, 0)))
                }
                MixedType.DATE -> {
                    for (i in 0..2000 step 200) {
                        mixedValues.add(Mixed.valueOf(Date(i.toLong())))
                    }
                }
                MixedType.FLOAT -> {
                    for (i in 0..10) {
                        mixedValues.add(Mixed.valueOf((1 / 10).toFloat()))
                    }
                }
                MixedType.DOUBLE -> {
                    for (i in 0..10) {
                        mixedValues.add(Mixed.valueOf((1 + (1 / 10)).toDouble()))
                    }
                }
                MixedType.DECIMAL128 -> {
                    for (i in 0..10) {
                        mixedValues.add(Mixed.valueOf(Decimal128(i.toLong())))
                    }
                }
                MixedType.OBJECT_ID -> {
                    for (i in 0..1000 step 100) {
                        mixedValues.add(Mixed.valueOf(ObjectId(Date(i.toLong()))))
                    }
                }
                MixedType.UUID -> {
                    for (i in 0..10) {
                        Mixed.valueOf(TestHelper.generateUUIDString(i))
                    }
                }
                MixedType.OBJECT,   // Not tested in this test suite
                MixedType.NULL -> {
                    for (i in 0..10) {
                        mixedValues.add(Mixed.nullValue())
                    }
                }

                else -> throw AssertionError("Missing case for type: ${type.name}")
            }
        }

        realm.beginTransaction()

        for (value in mixedValues) {
            val mixedObject = MixedNotIndexed(value)
            realm.insert(mixedObject)
        }

        realm.commitTransaction()
    }

    @Rule
    @JvmField
    val folder = TemporaryFolder()

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realmConfiguration = RealmDebugConfigurationBuilder(InstrumentationRegistry.getInstrumentation().targetContext)
                .setSchema(MixedNotIndexed::class.java)
                .directory(folder.newFolder())
                .build()

        realm = Realm.getInstance(realmConfiguration)

        initializeTestData()
    }

    @After
    fun tearDown() {
        realm.close()
    }

//    @Test
//    fun sort() {
//        val results = realm.where<MixedNotIndexed>().sort("mixed").findAll()
//
//        // FIXME: MIXED - VALIDATE ORDER IN RESULTS
//    }

//    @Test
//    fun distinct() {
//        assertEquals(20, realm.where<MixedNotIndexed>().findAll().size)
//        assertEquals(10, realm.where<MixedNotIndexed>().distinct("mixed").findAll().size)
//    }


    @Test
    fun equalTo() {
        val results = realm.where<MixedNotIndexed>().equalTo(MixedNotIndexed.FIELD_MIXED, first).findAll()

        if (testingType == MixedType.INTEGER){
            assertEquals(4, results.size)
        } else {
            assertEquals(1, results.size)
        }
    }

    @Test
    fun notEqualTo() {
        val results = realm.where<MixedNotIndexed>().equalTo(MixedNotIndexed.FIELD_MIXED, first).findAll()

        if (testingType == MixedType.INTEGER){
            assertEquals(4, results.size)
        } else {
            assertEquals(1, results.size)
        }
    }

    @Test
    fun greaterThanOrEqualTo() {
        // greaterThanOrEqualTo on Mixed fields would compare bool, int, float, double and decimal.

        val results = realm.where<MixedNotIndexed>().greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, first).findAll()

        assertEquals(6, realm.where<MixedNotIndexed>().greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, 1).findAll().size)
        assertEquals(1, realm.where<MixedNotIndexed>().greaterThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, 6).findAll().size)

        // FIXME: MIXED - VALIDATE OBJECT_ID BEHAVIOUR
    }

    @Test
    fun greaterThan() {
        // greaterThan on Mixed fields would compare int, float, double, bool and decimal.
        val results = realm.where<MixedNotIndexed>().greaterThan(MixedNotIndexed.FIELD_MIXED, first).findAll()

        assertEquals(5, realm.where<MixedNotIndexed>().greaterThan(MixedNotIndexed.FIELD_MIXED, 1).findAll().size)
        assertEquals(0, realm.where<MixedNotIndexed>().greaterThan(MixedNotIndexed.FIELD_MIXED, 6).findAll().size)

        // FIXME: MIXED - VALIDATE OBJECT_ID BEHAVIOUR
    }

    @Test
    fun lessThanOrEqualTo() {
        // lessThanOrEqualTo on Mixed fields would compare int, float, double, bool and decimal.
        val results = realm.where<MixedNotIndexed>().lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, first).findAll()

        assertEquals(6, realm.where<MixedNotIndexed>().lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, 6).findAll().size)
        assertEquals(1, realm.where<MixedNotIndexed>().lessThanOrEqualTo(MixedNotIndexed.FIELD_MIXED, 1).findAll().size)

        // FIXME: MIXED - VALIDATE OBJECT_ID BEHAVIOUR
    }

    @Test
    fun lessThan() {
        // lessThan on Mixed fields would compare int, float, double, bool and decimal.
        val results = realm.where<MixedNotIndexed>().lessThan(MixedNotIndexed.FIELD_MIXED, first).findAll()

        assertEquals(5, realm.where<MixedNotIndexed>().lessThan(MixedNotIndexed.FIELD_MIXED, 6).findAll().size)
        assertEquals(0, realm.where<MixedNotIndexed>().lessThan(MixedNotIndexed.FIELD_MIXED, 1).findAll().size)

        // FIXME: MIXED - VALIDATE OBJECT_ID BEHAVIOUR
    }

    @Test
    fun isNull() {
        val results = realm.where<MixedNotIndexed>().isNull(MixedNotIndexed.FIELD_MIXED).findAll()
        assertEquals(1, realm.where<MixedNotIndexed>().isNull(MixedNotIndexed.FIELD_MIXED).findAll().size)
    }

    @Test
    fun isNotNull() {
        val results = realm.where<MixedNotIndexed>().isNotNull(MixedNotIndexed.FIELD_MIXED).findAll()
        assertEquals(9, realm.where<MixedNotIndexed>().isNotNull(MixedNotIndexed.FIELD_MIXED).findAll().size)
    }

    @Test
    fun isEmpty() {
        val results = realm.where<MixedNotIndexed>().isEmpty(MixedNotIndexed.FIELD_MIXED).findAll()
    }

    @Test
    fun average() {
        val average = realm.where<MixedNotIndexed>().average(MixedNotIndexed.FIELD_MIXED)
    }

    @Test
    fun sum() {
        val sum = realm.where<MixedNotIndexed>().sum(MixedNotIndexed.FIELD_MIXED)
    }

    @Test
    fun `in`() {
        realm.where<MixedNotIndexed>().`in`(MixedNotIndexed.FIELD_MIXED, arrayOf(first, second, third)).findAll()
    }

    @Test
    fun between() {
        realm.where<MixedNotIndexed>().between(MixedNotIndexed.FIELD_MIXED, first, second).findAll()
    }

    @Test
    fun contains_sensitive() {
        realm.where<MixedNotIndexed>().contains(MixedNotIndexed.FIELD_MIXED, first, Case.SENSITIVE).findAll()
    }

    @Test
    fun contains_insensitive() {
        realm.where<MixedNotIndexed>().contains(MixedNotIndexed.FIELD_MIXED, first, Case.INSENSITIVE).findAll()
    }

    @Test
    fun beginsWith_sensitive() {
        realm.where<MixedNotIndexed>().beginsWith(MixedNotIndexed.FIELD_MIXED, "HELL", Case.SENSITIVE).findAll()
    }

    @Test
    fun beginsWith_insensitive() {
        realm.where<MixedNotIndexed>().beginsWith(MixedNotIndexed.FIELD_MIXED, "HELL", Case.INSENSITIVE).findAll()
    }

    @Test
    fun endsWith_sensitive() {
        realm.where<MixedNotIndexed>().endsWith(MixedNotIndexed.FIELD_MIXED, "1", Case.SENSITIVE).findAll()
    }

    @Test
    fun endsWith_insensitive() {
        realm.where<MixedNotIndexed>().endsWith(MixedNotIndexed.FIELD_MIXED, "1", Case.INSENSITIVE).findAll()
    }

    @Test
    fun like_sensitive() {
        realm.where<MixedNotIndexed>().like(MixedNotIndexed.FIELD_MIXED, "*W?RLD*", Case.SENSITIVE).findAll()
    }

    @Test
    fun like_insensitive() {
        realm.where<MixedNotIndexed>().like(MixedNotIndexed.FIELD_MIXED, "*W?RLD*", Case.INSENSITIVE).findAll()
    }

    @Test
    fun min() {
        val value = realm.where<MixedNotIndexed>().min(MixedNotIndexed.FIELD_MIXED)
    }

    @Test
    fun max() {
        val value = realm.where<MixedNotIndexed>().max(MixedNotIndexed.FIELD_MIXED)
    }

    @Test
    fun minDate() {
        val date = realm.where<MixedNotIndexed>().minimumDate(MixedNotIndexed.FIELD_MIXED)
    }

    @Test
    fun maxDate() {
        val date = realm.where<MixedNotIndexed>().maximumDate(MixedNotIndexed.FIELD_MIXED)
    }

    @Test
    fun count() {
        val count = realm.where<MixedNotIndexed>().count()
    }

}