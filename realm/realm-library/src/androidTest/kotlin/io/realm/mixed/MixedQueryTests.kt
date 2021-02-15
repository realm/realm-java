package io.realm.mixed

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.*
import io.realm.entities.MixedNotIndexed
import io.realm.kotlin.where
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.util.*
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class MixedQueryTests {

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

    @Test
    fun sort() {
    }

    @Test
    fun distinct() {
    }

    @Test
    fun equalTo_boolean() {
    }

    @Test
    fun equalTo_byte() {
    }

    @Test
    fun equalTo_short() {
    }

    @Test
    fun equalTo_int() {
    }

    @Test
    fun equalTo_long() {
    }

    @Test
    fun equalTo_float() {
    }

    @Test
    fun equalTo_double() {
    }

    @Test
    fun equalTo_string() {
    }

    @Test
    fun equalTo_binary() {
    }

    @Test
    fun equalTo_date() {
    }

    @Test
    fun equalTo_objectid() {
    }

    @Test
    fun equalTo_decimal() {
    }

    @Test
    fun equalTo_uuid() {
    }

    @Test
    fun notEqualTo_boolean() {
    }

    @Test
    fun notEqualTo_byte() {
    }

    @Test
    fun notEqualTo_short() {
    }

    @Test
    fun notEqualTo_int() {
    }

    @Test
    fun notEqualTo_long() {
    }

    @Test
    fun notEqualTo_float() {
    }

    @Test
    fun notEqualTo_double() {
    }

    @Test
    fun notEqualTo_string() {
    }

    @Test
    fun notEqualTo_binary() {
    }

    @Test
    fun notEqualTo_date() {
    }

    @Test
    fun notEqualTo_objectid() {
    }

    @Test
    fun notEqualTo_decimal() {
    }

    @Test
    fun notEqualTo_uuid() {
    }

    @Test
    fun greaterThanOrEqualTo_byte() {
    }

    @Test
    fun greaterThanOrEqualTo_short() {
    }

    @Test
    fun greaterThanOrEqualTo_int() {
    }

    @Test
    fun greaterThanOrEqualTo_long() {
    }

    @Test
    fun greaterThanOrEqualTo_float() {
    }

    @Test
    fun greaterThanOrEqualTo_double() {
    }

    @Test
    fun greaterThanOrEqualTo_date() {
    }

    @Test
    fun greaterThanOrEqualTo_objectid() {
    }

    @Test
    fun greaterThanOrEqualTo_decimal() {
    }

    @Test
    fun greaterThanOrEqualTo_uuid() {
    }

    @Test
    fun greaterThan_byte() {
    }

    @Test
    fun greaterThan_short() {
    }

    @Test
    fun greaterThan_int() {
    }

    @Test
    fun greaterThan_long() {
    }

    @Test
    fun greaterThan_float() {
    }

    @Test
    fun greaterThan_double() {
    }

    @Test
    fun greaterThan_date() {
    }

    @Test
    fun greaterThan_objectid() {
    }

    @Test
    fun greaterThan_decimal() {
    }

    @Test
    fun greaterThan_uuid() {
    }

    @Test
    fun lessThanOrEqualTo_byte() {
    }

    @Test
    fun lessThanOrEqualTo_short() {
    }

    @Test
    fun lessThanOrEqualTo_int() {
    }

    @Test
    fun lessThanOrEqualTo_long() {
    }

    @Test
    fun lessThanOrEqualTo_float() {
    }

    @Test
    fun lessThanOrEqualTo_double() {
    }

    @Test
    fun lessThanOrEqualTo_date() {
    }

    @Test
    fun lessThanOrEqualTo_objectid() {
    }

    @Test
    fun lessThanOrEqualTo_decimal() {
    }

    @Test
    fun lessThanOrEqualTo_uuid() {
    }

    @Test
    fun lessThan_byte() {
    }

    @Test
    fun lessThan_short() {
    }

    @Test
    fun lessThan_int() {
    }

    @Test
    fun lessThan_long() {
    }

    @Test
    fun lessThan_float() {
    }

    @Test
    fun lessThan_double() {
    }

    @Test
    fun lessThan_date() {
    }

    @Test
    fun lessThan_objectid() {
    }

    @Test
    fun lessThan_decimal() {
    }

    @Test
    fun lessThan_uuid() {
    }

    @Test
    fun in_boolean() {
    }

    @Test
    fun in_byte() {
    }

    @Test
    fun in_short() {
    }

    @Test
    fun in_int() {
    }

    @Test
    fun in_long() {
    }

    @Test
    fun in_float() {
    }

    @Test
    fun in_double() {
    }

    @Test
    fun in_string() {
    }

    @Test
    fun in_binary() {
    }

    @Test
    fun in_date() {
    }

    @Test
    fun between_boolean() {
    }

    @Test
    fun between_byte() {
    }

    @Test
    fun between_short() {
    }

    @Test
    fun between_int() {
    }

    @Test
    fun between_long() {
    }

    @Test
    fun between_float() {
    }

    @Test
    fun between_double() {
    }

    @Test
    fun between_date() {
    }

    @Test
    fun between_decimal() {
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
    fun contains_sensitive() {
        realm.where<MixedNotIndexed>().contains(MixedNotIndexed.FIELD_MIXED, "LLO", Case.SENSITIVE).findAll()
    }

    @Test
    fun contains_insensitive() {
        realm.where<MixedNotIndexed>().contains(MixedNotIndexed.FIELD_MIXED, "LLO", Case.INSENSITIVE).findAll()
    }

    @Test
    fun beginsWith_sensitive() {
        realm.where<MixedNotIndexed>().beginsWith(MixedNotIndexed.FIELD_MIXED, "hell", Case.SENSITIVE).findAll()
    }

    @Test
    fun beginsWith_insensitive() {
        realm.where<MixedNotIndexed>().beginsWith(MixedNotIndexed.FIELD_MIXED, "HELL", Case.INSENSITIVE).findAll()
    }

    @Test
    fun endsWith_sensitive() {
        realm.where<MixedNotIndexed>().endsWith(MixedNotIndexed.FIELD_MIXED, "d 1", Case.SENSITIVE).findAll()
    }

    @Test
    fun endsWith_insensitive() {
        realm.where<MixedNotIndexed>().endsWith(MixedNotIndexed.FIELD_MIXED, "D 1", Case.INSENSITIVE).findAll()
    }

    @Test
    fun like_sensitive() {
        realm.where<MixedNotIndexed>().like(MixedNotIndexed.FIELD_MIXED, "*w?rld*", Case.SENSITIVE).findAll()
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