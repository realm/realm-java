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

package io.realm.entities

import io.realm.RealmAny
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmSet
import io.realm.annotations.PrimaryKey
import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*

open class SetContainerClass : RealmObject() {

    val myRealmModelNoPkSet = RealmSet<Owner>()
    val myRealmModelSet = RealmSet<DogPrimaryKey>()
    val myBooleanSet = RealmSet<Boolean>()
    val myStringSet = RealmSet<String>()
    val myIntSet = RealmSet<Int>()
    val myFloatSet = RealmSet<Float>()
    val myLongSet = RealmSet<Long>()
    val myShortSet = RealmSet<Short>()
    val myDoubleSet = RealmSet<Double>()
    val myByteSet = RealmSet<Byte>()
    val myBinarySet = RealmSet<ByteArray>()
    val myDateSet = RealmSet<Date>()
    val myObjectIdSet = RealmSet<ObjectId>()
    val myUUIDSet = RealmSet<UUID>()
    val myDecimal128Set = RealmSet<Decimal128>()
    val myRealmAnySet = RealmSet<RealmAny>()

    val myRealmModelNoPkList = RealmList<Owner>()
    val myRealmModelList = RealmList<DogPrimaryKey>()
    val myBooleanList = RealmList<Boolean>()
    val myStringList = RealmList<String>()
    val myIntList = RealmList<Int>()
    val myFloatList = RealmList<Float>()
    val myLongList = RealmList<Long>()
    val myShortList = RealmList<Short>()
    val myDoubleList = RealmList<Double>()
    val myByteList = RealmList<Byte>()
    val myBinaryList = RealmList<ByteArray>()
    val myDateList = RealmList<Date>()
    val myObjectIdList = RealmList<ObjectId>()
    val myUUIDList = RealmList<UUID>()
    val myDecimal128List = RealmList<Decimal128>()
    val myRealmAnyList = RealmList<RealmAny>()

    companion object {
        const val CLASS_NAME = "SetContainerClass"
    }
}

open class SetContainerMigrationClass : RealmObject() {

    @PrimaryKey
    var id: String? = ""

    val myRealmModelSet = RealmSet<StringOnly>()
    val myBooleanSet = RealmSet<Boolean>()
    val myStringSet = RealmSet<String>()
    val myIntSet = RealmSet<Int>()
    val myFloatSet = RealmSet<Float>()
    val myLongSet = RealmSet<Long>()
    val myShortSet = RealmSet<Short>()
    val myDoubleSet = RealmSet<Double>()
    val myByteSet = RealmSet<Byte>()
    val myBinarySet = RealmSet<ByteArray>()
    val myDateSet = RealmSet<Date>()
    val myObjectIdSet = RealmSet<ObjectId>()
    val myUUIDSet = RealmSet<UUID>()
    val myDecimal128Set = RealmSet<Decimal128>()
    val myRealmAnySet = RealmSet<RealmAny>()

    companion object {
        const val CLASS_NAME = "SetContainerMigrationClass"
    }
}

open class SetContainerAfterMigrationClass : RealmObject() {

    @PrimaryKey
    var id: String? = ""

    companion object {
        const val CLASS_NAME = "SetContainerAfterMigrationClass"
    }
}
