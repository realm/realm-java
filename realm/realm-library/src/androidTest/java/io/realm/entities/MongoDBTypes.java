package io.realm.entities;

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import io.realm.RealmObject;

public class MongoDBTypes extends RealmObject {
    public ObjectId id = ObjectId.get();
    public Decimal128 dec128 = new Decimal128()
}
