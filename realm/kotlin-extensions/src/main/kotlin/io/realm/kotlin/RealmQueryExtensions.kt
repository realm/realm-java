@file:Suppress("unused")

package io.realm.kotlin

import io.realm.*
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KMutableProperty1

fun <T : RealmModel> RealmQuery<T>.isNull(property: KMutableProperty1<T, *>): RealmQuery<T> {
    return this.isNull(property.name)
}

fun <T : RealmModel> RealmQuery<T>.isNotNull(property: KMutableProperty1<T, *>): RealmQuery<T> {
    return this.isNotNull(property.name)
}

// equalTo

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out String?>,
                                           value: String?, case: Case = Case.SENSITIVE): RealmQuery<T> {
    return this.equalTo(property.name, value, case)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Byte?>,
                                           value: Byte?): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out ByteArray?>,
                                           value: ByteArray?): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Short?>,
                                           value: Short?): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Int?>,
                                           value: Int?): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Long?>,
                                           value: Long?): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Double?>,
                                           value: Double?): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Float?>,
                                           value: Float?): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Boolean?>,
                                           value: Boolean?): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.equalTo(property: KMutableProperty1<T, out Date?>,
                                           value: Date?): RealmQuery<T> {
    return this.equalTo(property.name, value)
}

// in

fun <T : RealmModel> RealmQuery<T>.oneOf(property: KMutableProperty1<T, out String?>,
                                        value: Array<String?>, casing: Case = Case.SENSITIVE): RealmQuery<T> {
    return this.`in`(property.name, value, casing)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(property: KMutableProperty1<T, out Byte?>,
                                        value: Array<Byte?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(property: KMutableProperty1<T, out Short?>,
                                        value: Array<Short?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(property: KMutableProperty1<T, out Int?>,
                                        value: Array<Int?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(property: KMutableProperty1<T, out Long?>,
                                        value: Array<Long?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(property: KMutableProperty1<T, out Double?>,
                                        value: Array<Double?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(property: KMutableProperty1<T, out Float?>,
                                        value: Array<Float?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(property: KMutableProperty1<T, out Boolean?>,
                                        value: Array<Boolean?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.oneOf(property: KMutableProperty1<T, out Date?>,
                                        value: Array<Date?>): RealmQuery<T> {
    return this.`in`(property.name, value)
}

// notEqualTo

fun <T : RealmModel> RealmQuery<T>.notEqualTo(property: KMutableProperty1<T, out String?>,
                                              value: String?, case: Case = Case.SENSITIVE): RealmQuery<T> {
    return this.notEqualTo(property.name, value, case)
}

fun <T : RealmModel> RealmQuery<T>.notEqualTo(property: KMutableProperty1<T, out Byte?>,
                                              value: Byte?): RealmQuery<T> {
    return this.notEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.notEqualTo(property: KMutableProperty1<T, out ByteArray?>,
                                              value: ByteArray?): RealmQuery<T> {
    return this.notEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.notEqualTo(property: KMutableProperty1<T, out Short?>,
                                              value: Short?): RealmQuery<T> {
    return this.notEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.notEqualTo(property: KMutableProperty1<T, out Int?>,
                                              value: Int?): RealmQuery<T> {
    return this.notEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.notEqualTo(property: KMutableProperty1<T, out Long?>,
                                              value: Long?): RealmQuery<T> {
    return this.notEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.notEqualTo(property: KMutableProperty1<T, out Double?>,
                                              value: Double?): RealmQuery<T> {
    return this.notEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.notEqualTo(property: KMutableProperty1<T, out Float?>,
                                              value: Float?): RealmQuery<T> {
    return this.notEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.notEqualTo(property: KMutableProperty1<T, out Boolean?>,
                                              value: Boolean?): RealmQuery<T> {
    return this.notEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.notEqualTo(property: KMutableProperty1<T, out Date?>,
                                              value: Date?): RealmQuery<T> {
    return this.notEqualTo(property.name, value)
}

// greaterThan

fun <T : RealmModel> RealmQuery<T>.greaterThan(property: KMutableProperty1<T, out Byte?>,
                                               value: Byte): RealmQuery<T> {
    return this.greaterThan(property.name, value.toInt())
}

fun <T : RealmModel> RealmQuery<T>.greaterThan(property: KMutableProperty1<T, out Short?>,
                                               value: Short): RealmQuery<T> {
    return this.greaterThan(property.name, value.toInt())
}

fun <T : RealmModel> RealmQuery<T>.greaterThan(property: KMutableProperty1<T, out Int?>,
                                               value: Int): RealmQuery<T> {
    return this.greaterThan(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.greaterThan(property: KMutableProperty1<T, out Long?>,
                                               value: Long): RealmQuery<T> {
    return this.greaterThan(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.greaterThan(property: KMutableProperty1<T, out Double?>,
                                               value: Double): RealmQuery<T> {
    return this.greaterThan(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.greaterThan(property: KMutableProperty1<T, out Float?>,
                                               value: Float): RealmQuery<T> {
    return this.greaterThan(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.greaterThan(property: KMutableProperty1<T, out Date?>,
                                               value: Date): RealmQuery<T> {
    return this.greaterThan(property.name, value)
}

// greaterThanOrEqualTo

fun <T : RealmModel> RealmQuery<T>.greaterThanOrEqualTo(property: KMutableProperty1<T, out Byte?>,
                                                        value: Byte): RealmQuery<T> {
    return this.greaterThanOrEqualTo(property.name, value.toInt())
}

fun <T : RealmModel> RealmQuery<T>.greaterThanOrEqualTo(property: KMutableProperty1<T, out Short?>,
                                                        value: Short): RealmQuery<T> {
    return this.greaterThanOrEqualTo(property.name, value.toInt())
}

fun <T : RealmModel> RealmQuery<T>.greaterThanOrEqualTo(property: KMutableProperty1<T, out Int?>,
                                                        value: Int): RealmQuery<T> {
    return this.greaterThanOrEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.greaterThanOrEqualTo(property: KMutableProperty1<T, out Long?>,
                                                        value: Long): RealmQuery<T> {
    return this.greaterThanOrEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.greaterThanOrEqualTo(property: KMutableProperty1<T, out Double?>,
                                                        value: Double): RealmQuery<T> {
    return this.greaterThanOrEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.greaterThanOrEqualTo(property: KMutableProperty1<T, out Float?>,
                                                        value: Float): RealmQuery<T> {
    return this.greaterThanOrEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.greaterThanOrEqualTo(property: KMutableProperty1<T, out Date?>,
                                                        value: Date): RealmQuery<T> {
    return this.greaterThanOrEqualTo(property.name, value)
}

// lessThan

fun <T : RealmModel> RealmQuery<T>.lessThan(property: KMutableProperty1<T, out Byte?>,
                                            value: Byte): RealmQuery<T> {
    return this.lessThan(property.name, value.toInt())
}

fun <T : RealmModel> RealmQuery<T>.lessThan(property: KMutableProperty1<T, out Short?>,
                                            value: Short): RealmQuery<T> {
    return this.lessThan(property.name, value.toInt())
}

fun <T : RealmModel> RealmQuery<T>.lessThan(property: KMutableProperty1<T, out Int?>,
                                            value: Int): RealmQuery<T> {
    return this.lessThan(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.lessThan(property: KMutableProperty1<T, out Long?>,
                                            value: Long): RealmQuery<T> {
    return this.lessThan(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.lessThan(property: KMutableProperty1<T, out Double?>,
                                            value: Double): RealmQuery<T> {
    return this.lessThan(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.lessThan(property: KMutableProperty1<T, out Float?>,
                                            value: Float): RealmQuery<T> {
    return this.lessThan(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.lessThan(property: KMutableProperty1<T, out Date?>,
                                            value: Date): RealmQuery<T> {
    return this.lessThan(property.name, value)
}

// lessThanOrEqualTo

fun <T : RealmModel> RealmQuery<T>.lessThanOrEqualTo(property: KMutableProperty1<T, out Byte?>,
                                                     value: Byte): RealmQuery<T> {
    return this.lessThanOrEqualTo(property.name, value.toInt())
}

fun <T : RealmModel> RealmQuery<T>.lessThanOrEqualTo(property: KMutableProperty1<T, out Short?>,
                                                     value: Short): RealmQuery<T> {
    return this.lessThanOrEqualTo(property.name, value.toInt())
}

fun <T : RealmModel> RealmQuery<T>.lessThanOrEqualTo(property: KMutableProperty1<T, out Int?>,
                                                     value: Int): RealmQuery<T> {
    return this.lessThanOrEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.lessThanOrEqualTo(property: KMutableProperty1<T, out Long?>,
                                                     value: Long): RealmQuery<T> {
    return this.lessThanOrEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.lessThanOrEqualTo(property: KMutableProperty1<T, out Double?>,
                                                     value: Double): RealmQuery<T> {
    return this.lessThanOrEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.lessThanOrEqualTo(property: KMutableProperty1<T, out Float?>,
                                                     value: Float): RealmQuery<T> {
    return this.lessThanOrEqualTo(property.name, value)
}

fun <T : RealmModel> RealmQuery<T>.lessThanOrEqualTo(property: KMutableProperty1<T, out Date?>,
                                                     value: Date): RealmQuery<T> {
    return this.lessThanOrEqualTo(property.name, value)
}

// between

fun <T : RealmModel> RealmQuery<T>.between(property: KMutableProperty1<T, out Byte?>,
                                           from: Byte, to: Byte): RealmQuery<T> {
    return this.between(property.name, from.toInt(), to.toInt())
}

fun <T : RealmModel> RealmQuery<T>.between(property: KMutableProperty1<T, out Short?>,
                                           from: Short, to: Short): RealmQuery<T> {
    return this.between(property.name, from.toInt(), to.toInt())
}

fun <T : RealmModel> RealmQuery<T>.between(property: KMutableProperty1<T, out Int?>,
                                           from: Int, to: Int): RealmQuery<T> {
    return this.between(property.name, from, to)
}

fun <T : RealmModel> RealmQuery<T>.between(property: KMutableProperty1<T, out Long?>,
                                           from: Long, to: Long): RealmQuery<T> {
    return this.between(property.name, from, to)
}

fun <T : RealmModel> RealmQuery<T>.between(property: KMutableProperty1<T, out Double?>,
                                           from: Double, to: Double): RealmQuery<T> {
    return this.between(property.name, from, to)
}

fun <T : RealmModel> RealmQuery<T>.between(property: KMutableProperty1<T, out Float?>,
                                           from: Float, to: Float): RealmQuery<T> {
    return this.between(property.name, from, to)
}

fun <T : RealmModel> RealmQuery<T>.between(property: KMutableProperty1<T, out Date?>,
                                           from: Date, to: Date): RealmQuery<T> {
    return this.between(property.name, from, to)
}

// contains

fun <T : RealmModel> RealmQuery<T>.contains(property: KMutableProperty1<T, out String?>,
                                            value: String, case: Case = Case.SENSITIVE): RealmQuery<T> {
    return this.contains(property.name, value, case)
}

// beginsWith

fun <T : RealmModel> RealmQuery<T>.beginsWith(property: KMutableProperty1<T, out String?>,
                                            value: String, case: Case = Case.SENSITIVE): RealmQuery<T> {
    return this.beginsWith(property.name, value, case)
}

// endsWith

fun <T : RealmModel> RealmQuery<T>.endsWith(property: KMutableProperty1<T, out String?>,
                                            value: String, case: Case = Case.SENSITIVE): RealmQuery<T> {
    return this.endsWith(property.name, value, case)
}

// like

fun <T : RealmModel> RealmQuery<T>.like(property: KMutableProperty1<T, out String?>,
                                            value: String, case: Case = Case.SENSITIVE): RealmQuery<T> {
    return this.like(property.name, value, case)
}

// beginGroup, endGroup

fun <T : RealmModel> RealmQuery<T>.group(body: RealmQuery<T>.() -> Unit): RealmQuery<T> {
    beginGroup()
    body()
    return endGroup()
}

// isEmpty

fun <T : RealmModel> RealmQuery<T>.isEmpty(property: KMutableProperty1<T, *>): RealmQuery<T> {
    return this.isEmpty(property.name)
}

// isNotEmpty

fun <T : RealmModel> RealmQuery<T>.isNotEmpty(property: KMutableProperty1<T, *>): RealmQuery<T> {
    return this.isNotEmpty(property.name)
}

// distinct

fun <T : RealmModel> RealmQuery<T>.distinct(property: KMutableProperty1<T, *>): RealmResults<T> {
    return this.distinct(property.name)
}

fun <T : RealmModel> RealmQuery<T>.distinct(firstField: KMutableProperty1<T, *>, vararg remainingFields: KMutableProperty1<T, *>): RealmResults<T> {
    return this.distinct(firstField.name, *remainingFields.map { it.name }.toTypedArray())
}

// distinctAsync

fun <T : RealmModel> RealmQuery<T>.distinctAsync(property: KMutableProperty1<T, *>): RealmResults<T> {
    return this.distinctAsync(property.name)
}

// sum

fun <T : RealmModel> RealmQuery<T>.sum(property: KMutableProperty1<T, out Number?>): Long {
    return this.sum(property.name).toLong()
}

fun <T : RealmModel> RealmQuery<T>.sum(property: KMutableProperty1<T, out Double?>): Double {
    return this.sum(property.name).toDouble()
}

fun <T : RealmModel> RealmQuery<T>.sum(property: KMutableProperty1<T, out Float?>): Float {
    return this.sum(property.name).toFloat()
}

// average

fun <T : RealmModel> RealmQuery<T>.average(property: KMutableProperty1<T, out Number?>): Double {
    return this.average(property.name)
}

// min

fun <T : RealmModel> RealmQuery<T>.min(property: KMutableProperty1<T, out Number?>): Long? {
    return this.min(property.name)?.toLong()
}

fun <T : RealmModel> RealmQuery<T>.min(property: KMutableProperty1<T, out Double?>): Double? {
    return this.min(property.name)?.toDouble()
}

fun <T : RealmModel> RealmQuery<T>.min(property: KMutableProperty1<T, out Float?>): Float? {
    return this.min(property.name)?.toFloat()
}

// max

fun <T : RealmModel> RealmQuery<T>.max(property: KMutableProperty1<T, out Number?>): Long? {
    return this.max(property.name)?.toLong()
}

fun <T : RealmModel> RealmQuery<T>.max(property: KMutableProperty1<T, out Double?>): Double? {
    return this.max(property.name)?.toDouble()
}

fun <T : RealmModel> RealmQuery<T>.max(property: KMutableProperty1<T, out Float?>): Float? {
    return this.max(property.name)?.toFloat()
}

// minimumDate

fun <T : RealmModel> RealmQuery<T>.minimumDate(property: KMutableProperty1<T, out Date?>): Date? {
    return this.minimumDate(property.name)
}

// maximumDate

fun <T : RealmModel> RealmQuery<T>.maximumDate(property: KMutableProperty1<T, out Date?>): Date? {
    return this.maximumDate(property.name)
}

// findAllSorted

fun <T : RealmModel> RealmQuery<T>.findAllSorted(field: KMutableProperty1<T, out Any?>,
                                                 sortOrder: Sort = Sort.ASCENDING): RealmResults<T> {
    return this.findAllSorted(field.name, sortOrder)
}

fun <T : RealmModel> RealmQuery<T>.findAllSorted(field1: KMutableProperty1<T, out Any?>,
                                                 sortOrder1: Sort = Sort.ASCENDING,
                                                 field2: KMutableProperty1<T, out Any?>,
                                                 sortOrder2: Sort = Sort.ASCENDING): RealmResults<T> {
    return this.findAllSorted(field1.name, sortOrder1, field2.name, sortOrder2)
}

fun <T : RealmModel> RealmQuery<T>.findAllSorted(fields: Array<KMutableProperty1<T, out Any?>>,
                                                 sortOrders: Array<Sort>): RealmResults<T> {
    return this.findAllSorted(fields.map {it.name}.toTypedArray(), sortOrders)
}

// findAllSortedAsync

fun <T : RealmModel> RealmQuery<T>.findAllSortedAsync(property: KMutableProperty1<T, out Any?>,
                                                      sortOrder: Sort = Sort.ASCENDING): RealmResults<T> {
    return this.findAllSortedAsync(property.name, sortOrder)
}

fun <T : RealmModel> RealmQuery<T>.findAllSortedAsync(field1: KMutableProperty1<T, out Any?>,
                                                      sortOrder1: Sort = Sort.ASCENDING,
                                                      field2: KMutableProperty1<T, out Any?>,
                                                      sortOrder2: Sort = Sort.ASCENDING): RealmResults<T> {
    return this.findAllSortedAsync(field1.name, sortOrder1, field2.name, sortOrder2)
}

fun <T : RealmModel> RealmQuery<T>.findAllSortedAsync(fields: Array<KMutableProperty1<T, out Any?>>,
                                                 sortOrders: Array<Sort>): RealmResults<T> {
    return this.findAllSortedAsync(fields.map {it.name}.toTypedArray(), sortOrders)
}
