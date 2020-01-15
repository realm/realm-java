package io.realm.entities;

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MongoDBTypes extends RealmObject {

    @PrimaryKey
    public ObjectId id;
    public ObjectId otherId;
    public Decimal128 dec128;
    public RealmList<Decimal128> dec128List = new RealmList<>();
    public RealmList<ObjectId> objectIdList = new RealmList<>();
}
