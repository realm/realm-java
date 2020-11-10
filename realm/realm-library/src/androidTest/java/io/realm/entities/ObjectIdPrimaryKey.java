package io.realm.entities;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ObjectIdPrimaryKey extends RealmObject {

    public static final String CLASS_NAME = "ObjectIdPrimaryKey";
    public static final String PROPERTY_OBJECT_ID = "objectId";

    @PrimaryKey
    private ObjectId objectId;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }
}
