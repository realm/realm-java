package io.realm.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId

class SyncTest: RealmObject() {
    @PrimaryKey
    var _id: ObjectId = ObjectId.get()
    var partition: String = "default"
    var name: String = ""
}