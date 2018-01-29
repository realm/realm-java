package io.realm.entities

import io.realm.RealmModel
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class AllPropTypesClass : RealmModel {

    var stringVar: String = ""
    var byteVar: Byte = 0
    var shortVar: Short = 0
    var intVar: Int = 0
    var longVar: Long = 0
    var doubleVar: Double = 0.0
    var floatVar: Float = 0.0f
    var booleanVar : Boolean = false
    var dateVar : Date = Date()

    var nullableStringVar: String? = null
    var nullableByteVar: Byte? = null
    var nullableShortVar: Short? = null
    var nullableIntVar: Int? = null
    var nullableLongVar: Long? = null
    var nullableDoubleVar: Double? = null
    var nullableFloatVar: Float? = null
    var nullableBooleanVar : Boolean? = null
    var nullableDateVar : Date? = null
}
