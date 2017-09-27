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

package io.realm.entities

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.Ignore
import io.realm.annotations.Index
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import java.util.*

open class AllKotlinTypes : RealmObject() {

    @Ignore
    var ignoredString: String = ""
    var nullString: String? = null
    var nonNullString: String = ""
    @Index
    var indexedString: String = ""

    var nullLong: Long? = null
    @PrimaryKey
    var nonNullLong: Long = 0

    var nullShort : Short? = null
    var nonNullShort: Short = 0

    var nullInt: Int? = null
    var nonNullInt: Int = 0

    var nullByte: Byte? = null
    var nonNullByte: Byte = 0

    var nullFloat: Float? = null
    var nonNullFloat: Float = 0F

    var nullDouble: Double? = null
    var nonNullDouble: Double = 0.0 // Double by default

    var nullBoolean: Boolean? = null
    var nonNullBoolean: Boolean = false

    var nullDate: Date? = null
    var nonNullDate: Date = Date()

    var nullBinary: ByteArray? = null
    var nonNullBinary: ByteArray = ByteArray(0)

    // This turns into Byte[] which we dont support for some reason?
    // var nullBoxedBinary: Array<Byte>? = null
    // var nonNullBoxedBinary: Array<Byte> = emptyArray()

    var nullObject: AllKotlinTypes? = null

    // Not-null object references cannot be enforced generically at the schema level, e.g. sync that
    // removes an object reference.
    // If people can maintain the variant themselves they can just expose a custom non-null getter.
    // var nonNullObject: AllKotlinTypes = AllKotlinTypes()

    // This is only possible in unmanaged objects, managed objects are never null
    // For now we allow this anyway.
    var nullList: RealmList<AllKotlinTypes>? = null
    var nonNullList: RealmList<AllKotlinTypes> = RealmList()

    @LinkingObjects("nullObject")
    val objectParents: RealmResults<AllKotlinTypes>? = null;

    @LinkingObjects("nonNullList")
    val listParents: RealmResults<AllKotlinTypes>? = null;
}
