package io.realm;


import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.Date;

import javax.annotation.Nullable;

import static io.realm.RealmFieldTypeConstants.MAX_CORE_TYPE_VALUE;


public enum MixedType {
    INTEGER(RealmFieldType.INTEGER, Long.class.getSimpleName()),
    BOOLEAN(RealmFieldType.BOOLEAN, Boolean.class.getSimpleName()),
    STRING(RealmFieldType.STRING, String.class.getSimpleName()),
    BINARY(RealmFieldType.BINARY, Byte[].class.getSimpleName()),
    DATE(RealmFieldType.DATE, Date.class.getSimpleName()),
    FLOAT(RealmFieldType.FLOAT, Float.class.getSimpleName()),
    DOUBLE(RealmFieldType.DOUBLE, Double.class.getSimpleName()),
    DECIMAL128(RealmFieldType.DECIMAL128, Decimal128.class.getSimpleName()),
    OBJECT_ID(RealmFieldType.OBJECT_ID, ObjectId.class.getSimpleName()),
    OBJECT(RealmFieldType.TYPED_LINK, RealmModel.class.getSimpleName()),
    NULL(null, "null");

    private static final MixedType[] realmFieldToMixedTypeMap = new MixedType[MAX_CORE_TYPE_VALUE + 1];

    static {
        for (MixedType mixedType : values()) {
            if (mixedType == NULL) { continue; }

            final int nativeValue = mixedType.realmFieldType.getNativeValue();
            realmFieldToMixedTypeMap[nativeValue] = mixedType;
        }
    }

    public static MixedType fromNativeValue(int realmFieldType) {
        if (realmFieldType == -1) { return NULL; }

        return realmFieldToMixedTypeMap[realmFieldType];
    }

    private final String simpleClassName;
    private final RealmFieldType realmFieldType;

    MixedType(@Nullable RealmFieldType realmFieldType, String simpleClassName) {
        this.realmFieldType = realmFieldType;
        this.simpleClassName = simpleClassName;
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }
}