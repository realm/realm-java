package io.realm;


import static io.realm.RealmFieldTypeConstants.MAX_CORE_TYPE_VALUE;


public enum MixedType {
    INTEGER(RealmFieldType.INTEGER),
    BOOLEAN(RealmFieldType.BOOLEAN),
    STRING(RealmFieldType.STRING),
    BINARY(RealmFieldType.BINARY),
    DATE(RealmFieldType.DATE),
    FLOAT(RealmFieldType.FLOAT),
    DOUBLE(RealmFieldType.DOUBLE),
    OBJECT(RealmFieldType.OBJECT),
    DECIMAL128(RealmFieldType.DECIMAL128),
    OBJECT_ID(RealmFieldType.OBJECT_ID),
    NULL(null);

    private static final MixedType[] realmFieldToMixedTypeMap = new MixedType[MAX_CORE_TYPE_VALUE + 1];

    static {
        for (MixedType mixedType : values()) {
            if(mixedType == NULL)
                continue;

            final int nativeValue = mixedType.realmFieldType.getNativeValue();
            realmFieldToMixedTypeMap[nativeValue] = mixedType;
        }
    }

    public static MixedType fromNativeValue(int realmFieldType) {
        if(realmFieldType == -1)
            return NULL;

        return realmFieldToMixedTypeMap[realmFieldType];
    }

    private final RealmFieldType realmFieldType;

    MixedType(RealmFieldType realmFieldType) {
        this.realmFieldType = realmFieldType;
    }

    RealmFieldType getRealmFieldType() {
        return this.realmFieldType;
    }
}