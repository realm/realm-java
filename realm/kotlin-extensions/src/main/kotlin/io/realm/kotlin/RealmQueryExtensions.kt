/*
 * Copyright 2017 Realm Inc.
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
package io.realm.kotlin

import io.realm.Case
import io.realm.RealmModel
import io.realm.RealmQuery
import java.util.*


/**
 * In comparison. This allows you to test if objects match any value in an array of values.
 *
 * @param fieldName the field to compare.
 * @param values array of values to compare with and it cannot be null or empty.
 * @param casing how casing is handled. [Case.INSENSITIVE] works only for the Latin-1 characters.
 * @return the query object.
 * @throws java.lang.IllegalArgumentException if the field isn't a String field or `values` is `null` or
 * empty.
 */
fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<String?>,
                                         casing: Case = Case.SENSITIVE): RealmQuery<T> {
    return this.`in`(propertyName, value, casing)
}


/**
 * In comparison. This allows you to test if objects match any value in an array of values.
 *
 * @param fieldName the field to compare.
 * @param values array of values to compare with and it cannot be null or empty.
 * @return the query object.
 * @throws java.lang.IllegalArgumentException if the field isn't a Byte field or `values` is `null` or
 * empty.
 */
fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Byte?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}

/**
 * In comparison. This allows you to test if objects match any value in an array of values.
 *
 * @param fieldName the field to compare.
 * @param values array of values to compare with and it cannot be null or empty.
 * @return the query object.
 * @throws java.lang.IllegalArgumentException if the field isn't a Short field or `values` is `null` or
 * empty.
 */
fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Short?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}

/**
 * In comparison. This allows you to test if objects match any value in an array of values.
 *
 * @param fieldName the field to compare.
 * @param values array of values to compare with and it cannot be null or empty.
 * @return the query object.
 * @throws java.lang.IllegalArgumentException if the field isn't a Integer field or `values` is `null`
 * or empty.
 */
fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Int?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}

/**
 * In comparison. This allows you to test if objects match any value in an array of values.
 *
 * @param fieldName the field to compare.
 * @param values array of values to compare with and it cannot be null or empty.
 * @return the query object.
 * @throws java.lang.IllegalArgumentException if the field isn't a Long field or `values` is `null` or
 * empty.
 */
fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Long?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}

/**
 * In comparison. This allows you to test if objects match any value in an array of values.
 *
 * @param fieldName the field to compare.
 * @param values array of values to compare with and it cannot be null or empty.
 * @return the query object.
 * @throws java.lang.IllegalArgumentException if the field isn't a Double field or `values` is `null` or
 * empty.
 */
fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Double?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}


/**
 * In comparison. This allows you to test if objects match any value in an array of values.
 *
 * @param fieldName the field to compare.
 * @param values array of values to compare with and it cannot be null or empty.
 * @return the query object.
 * @throws java.lang.IllegalArgumentException if the field isn't a Float field or `values` is `null` or
 * empty.
 */
fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Float?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}


/**
 * In comparison. This allows you to test if objects match any value in an array of values.
 *
 * @param fieldName the field to compare.
 * @param values array of values to compare with and it cannot be null or empty.
 * @return the query object.
 * @throws java.lang.IllegalArgumentException if the field isn't a Boolean field or `values` is `null`
 * or empty.
 */
fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Boolean?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}

/**
 * In comparison. This allows you to test if objects match any value in an array of values.
 *
 * @param fieldName the field to compare.
 * @param values array of values to compare with and it cannot be null or empty.
 * @return the query object.
 * @throws java.lang.IllegalArgumentException if the field isn't a Date field or `values` is `null` or
 * empty.
 */
fun <T : RealmModel> RealmQuery<T>.oneOf(propertyName: String,
                                         value: Array<Date?>): RealmQuery<T> {
    return this.`in`(propertyName, value)
}
