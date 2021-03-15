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

import io.realm.MutableRealmInteger
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.TestHelper
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmField
import io.realm.annotations.Required
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.util.*

open class SyncAllTypes : RealmObject() {

    companion object {
        const val CLASS_NAME = "SyncAllTypes"
        const val FIELD_STRING = "columnString"
        const val FIELD_LONG = "columnLong"
        const val FIELD_FLOAT = "columnFloat"
        const val FIELD_DOUBLE = "columnDouble"
        const val FIELD_BOOLEAN = "columnBoolean"
        const val FIELD_DATE = "columnDate"
        const val FIELD_BINARY = "columnBinary"
        const val FIELD_MUTABLEREALMINTEGER = "columnMutableRealmInteger"
        const val FIELD_DECIMAL128 = "columnDecimal128"
        const val FIELD_OBJECT_ID = "columnObjectId"
        const val FIELD_REALMOBJECT = "columnRealmObject"
        const val FIELD_REALMLIST = "columnRealmList"
        const val FIELD_STRING_LIST = "columnStringList"
        const val FIELD_BINARY_LIST = "columnBinaryList"
        const val FIELD_BOOLEAN_LIST = "columnBooleanList"
        const val FIELD_LONG_LIST = "columnLongList"
        const val FIELD_DOUBLE_LIST = "columnDoubleList"
        const val FIELD_FLOAT_LIST = "columnFloatList"
        const val FIELD_DATE_LIST = "columnDateList"
    }

    @PrimaryKey
    @RealmField(name = "_id")
    var id = ObjectId()

    @Required
    var columnString = ""
    var columnLong: Long = 0

    // FIXME Float ruins initial upload of scheme, works if added to schema later
//    var columnFloat = 0f

    var columnDouble = 0.0
    var isColumnBoolean = false

    @Required
    var columnDate = Date(0)

    @Required
    var columnBinary = ByteArray(0)

    @Required
    var columnDecimal128 = Decimal128(BigDecimal.ZERO)

    @Required
    var columnObjectId = ObjectId(TestHelper.randomObjectIdHexString())

    @Required
    var columnUUID: UUID = UUID.randomUUID()

    val columnRealmInteger: MutableRealmInteger = MutableRealmInteger.ofNull()

    var columnRealmObject: SyncDog? = null

    var columnRealmList: RealmList<SyncDog> = RealmList()
    @Required
    var columnStringList: RealmList<String> = RealmList()
    @Required
    var columnBinaryList: RealmList<ByteArray> = RealmList()
    @Required
    var columnBooleanList: RealmList<Boolean> = RealmList()
    @Required
    var columnLongList: RealmList<Long> = RealmList()
    @Required
    var columnDoubleList: RealmList<Double> = RealmList()

    // FIXME Float ruins initial upload of scheme, works if added to schema later
//    @Required
//    var columnFloatList: RealmList<Float>? = null

    @Required
    var columnDateList: RealmList<Date> = RealmList()
    @Required
    var columnDecimal128List: RealmList<Decimal128> = RealmList()

    @Required
    var columnObjectIdList: RealmList<ObjectId> = RealmList()
    @Required
    var columnUUIDList: RealmList<UUID> = RealmList()

    fun setColumnMutableRealmInteger(value: Int) {
        columnRealmInteger.set(value.toLong())
    }

}
