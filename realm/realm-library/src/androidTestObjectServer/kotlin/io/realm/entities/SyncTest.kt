package io.realm.entities

import android.graphics.Color
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmField
import org.bson.types.ObjectId

open class SyncTest: RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId.get()
    @RealmField(name = "realm_id")
    var realmId: String = "\"foobar\""
    var color: String = Color.RED.toString()
}