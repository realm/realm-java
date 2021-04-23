/*
 * Copyright 2020 Realm Inc.
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

package io.realm;

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.Date;

import javax.annotation.Nullable;

import static io.realm.RealmFieldTypeConstants.MAX_CORE_TYPE_VALUE;


public enum RealmAnyType {
    INTEGER(RealmFieldType.INTEGER, Long.class),
    BOOLEAN(RealmFieldType.BOOLEAN, Boolean.class),
    STRING(RealmFieldType.STRING, String.class),
    BINARY(RealmFieldType.BINARY, Byte[].class),
    DATE(RealmFieldType.DATE, Date.class),
    FLOAT(RealmFieldType.FLOAT, Float.class),
    DOUBLE(RealmFieldType.DOUBLE, Double.class),
    DECIMAL128(RealmFieldType.DECIMAL128, Decimal128.class),
    OBJECT_ID(RealmFieldType.OBJECT_ID, ObjectId.class),
    OBJECT(RealmFieldType.TYPED_LINK, RealmModel.class),
    UUID(RealmFieldType.UUID, java.util.UUID.class),
    NULL(null, null);

    private static final RealmAnyType[] realmFieldToRealmAnyTypeMap = new RealmAnyType[MAX_CORE_TYPE_VALUE + 2];

    static {
        for (RealmAnyType realmAnyType : values()) {
            if (realmAnyType == NULL) { continue; }

            final int nativeValue = realmAnyType.realmFieldType.getNativeValue();
            realmFieldToRealmAnyTypeMap[nativeValue] = realmAnyType;
        }
        // TODO: only used for testing purposes, see https://github.com/realm/realm-java/issues/7385
        // Links Object field type to RealmAny object.
        realmFieldToRealmAnyTypeMap[RealmFieldType.OBJECT.getNativeValue()] = OBJECT;
    }

    public static RealmAnyType fromNativeValue(int realmFieldType) {
        if (realmFieldType == -1) { return NULL; }

        return realmFieldToRealmAnyTypeMap[realmFieldType];
    }

    private final Class<?> clazz;
    private final RealmFieldType realmFieldType;

    RealmAnyType(@Nullable RealmFieldType realmFieldType, @Nullable Class<?> clazz) {
        this.realmFieldType = realmFieldType;
        this.clazz = clazz;
    }

    public Class<?> getTypedClass() {
        return clazz;
    }
}
