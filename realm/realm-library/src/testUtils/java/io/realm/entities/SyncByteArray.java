package io.realm.entities;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class SyncByteArray extends RealmObject {
    @PrimaryKey
    public ObjectId _id;
    @Required
    public byte[] columnBinary = new byte[0];
}
