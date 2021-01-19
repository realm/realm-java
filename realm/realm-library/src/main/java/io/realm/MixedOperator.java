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

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.internal.NativeContext;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.core.NativeMixed;


public abstract class MixedOperator {
    static MixedOperator fromNativeMixed(BaseRealm realm, NativeMixed nativeMixed) {
        MixedType fieldType = nativeMixed.getType();

        switch (fieldType) {
            case INTEGER:
                return new IntegerMixedOperator(nativeMixed);
            case BOOLEAN:
                return new BooleanMixedOperator(nativeMixed);
            case STRING:
                return new StringMixedOperator(nativeMixed);
            case BINARY:
                return new BinaryMixedOperator(nativeMixed);
            case DATE:
                return new DateMixedOperator(nativeMixed);
            case FLOAT:
                return new FloatMixedOperator(nativeMixed);
            case DOUBLE:
                return new DoubleMixedOperator(nativeMixed);
            case DECIMAL128:
                return new Decimal128MixedOperator(nativeMixed);
            case OBJECT_ID:
                return new ObjectIdMixedOperator(nativeMixed);
            case UUID:
                return new UUIDMixedOperator(nativeMixed);
            case OBJECT:
                if (realm instanceof DynamicRealm) {
                    return new DynamicRealmModelMixedOperator(realm, nativeMixed);
                } else {
                    return new RealmModelOperator(realm, nativeMixed);
                }
            case NULL:
                return new NullMixedOperator(nativeMixed);
            default:
                throw new ClassCastException("Couldn't cast to " + fieldType);
        }
    }

    @Nullable
    private NativeMixed nativeMixed;

    @Nullable
    private final Object value;

    @Nullable
    private final MixedType type;

    private synchronized NativeMixed getNativeMixed() {
        if (nativeMixed == null) { nativeMixed = createNativeMixed(); }

        return nativeMixed;
    }

    long getNativePtr() {
        return getNativeMixed().getNativePtr();
    }

    protected abstract NativeMixed createNativeMixed();

    protected MixedOperator(@Nullable Object value, @Nullable MixedType type, @Nullable NativeMixed nativeMixed) {
        this.value = value;
        this.type = type;
        this.nativeMixed = nativeMixed;
    }

    protected MixedOperator(Object value, MixedType type) {
        this(value, type, null);
    }

    protected MixedOperator(NativeMixed nativeMixed) {
        this(null, null, nativeMixed);
    }

    protected MixedOperator(MixedType mixedType) {
        this(null, mixedType, null);
    }

    protected MixedOperator() {
        this(null, null, null);
    }

    <T> T getValue(Class<T> clazz) {
        return clazz.cast(value);
    }

    MixedType getType() {
        return type;
    }

    Class<?> getTypedClass() {
        return type.getTypedClass();
    }
}

final class BooleanMixedOperator extends MixedOperator {
    BooleanMixedOperator(Boolean value) {
        super(value, MixedType.BOOLEAN);
    }

    BooleanMixedOperator(NativeMixed nativeMixed) {
        super(nativeMixed.asBoolean(), MixedType.BOOLEAN, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed() {
        return new NativeMixed(super.getValue(Boolean.class));
    }
}

final class IntegerMixedOperator extends MixedOperator {
    IntegerMixedOperator(Byte value) {
        super(value, MixedType.INTEGER);
    }

    IntegerMixedOperator(Short value) {
        super(value, MixedType.INTEGER);
    }

    IntegerMixedOperator(Integer value) {
        super(value, MixedType.INTEGER);
    }

    IntegerMixedOperator(Long value) {
        super(value, MixedType.INTEGER);
    }

    IntegerMixedOperator(NativeMixed nativeMixed) {
        super(nativeMixed.asLong(), MixedType.INTEGER, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed() {
        return new NativeMixed(super.getValue(Number.class));
    }
}

final class FloatMixedOperator extends MixedOperator {
    FloatMixedOperator(Float value) {
        super(value, MixedType.FLOAT);
    }

    FloatMixedOperator(NativeMixed nativeMixed) {
        super(nativeMixed.asFloat(), MixedType.FLOAT, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed() {
        return new NativeMixed(super.getValue(Float.class));
    }
}

final class DoubleMixedOperator extends MixedOperator {
    DoubleMixedOperator(Double value) {
        super(value, MixedType.DOUBLE);
    }

    DoubleMixedOperator(NativeMixed nativeMixed) {
        super(nativeMixed.asDouble(), MixedType.DOUBLE, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed() {
        return new NativeMixed(super.getValue(Double.class));
    }
}

final class StringMixedOperator extends MixedOperator {
    StringMixedOperator(String value) {
        super(value, MixedType.STRING);
    }

    StringMixedOperator(NativeMixed nativeMixed) {
        super(nativeMixed.asString(), MixedType.STRING, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed() {
        return new NativeMixed(super.getValue(String.class));
    }
}

final class BinaryMixedOperator extends MixedOperator {
    BinaryMixedOperator(byte[] value) {
        super(value, MixedType.BINARY);
    }

    BinaryMixedOperator(NativeMixed nativeMixed) {
        super(nativeMixed.asBinary(), MixedType.BINARY, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed() {
        return new NativeMixed(super.getValue(byte[].class));
    }
}

final class DateMixedOperator extends MixedOperator {
    DateMixedOperator(Date value) {
        super(value, MixedType.DATE);
    }

    DateMixedOperator(NativeMixed nativeMixed) {
        super(nativeMixed.asDate(), MixedType.DATE, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed() {
        return new NativeMixed(super.getValue(Date.class));
    }
}

final class ObjectIdMixedOperator extends MixedOperator {
    ObjectIdMixedOperator(ObjectId value) {
        super(value, MixedType.OBJECT_ID);
    }

    ObjectIdMixedOperator(NativeMixed nativeMixed) {
        super(nativeMixed.asObjectId(), MixedType.OBJECT_ID, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed() {
        return new NativeMixed(super.getValue(ObjectId.class));
    }
}

final class Decimal128MixedOperator extends MixedOperator {
    Decimal128MixedOperator(Decimal128 value) {
        super(value, MixedType.DECIMAL128);
    }

    Decimal128MixedOperator(NativeMixed nativeMixed) {
        super(nativeMixed.asDecimal128(), MixedType.DECIMAL128, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed() {
        return new NativeMixed(super.getValue(Decimal128.class));
    }
}

final class UUIDMixedOperator extends MixedOperator {
    UUIDMixedOperator(UUID value) {
        super(value, MixedType.UUID);
    }

    UUIDMixedOperator(NativeMixed nativeMixed) {
        super(nativeMixed.asUUID(), MixedType.UUID, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed() {
        return new NativeMixed(super.getValue(UUID.class));
    }
}

final class NullMixedOperator extends MixedOperator {
    NullMixedOperator() {
        super(MixedType.NULL);
    }

    NullMixedOperator(NativeMixed nativeMixed) {
        super(null, MixedType.NULL, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed() {
        return new NativeMixed();
    }

    @Override
    public <T> T getValue(Class<T> clazz) {
        return null;
    }
}

class RealmModelOperator extends MixedOperator {
    private static <T extends RealmModel> Class<T> getModelClass(BaseRealm realm, NativeMixed nativeMixed) {
        OsSharedRealm sharedRealm = realm
                .getSharedRealm();

        String className = Table.getClassNameForTable(nativeMixed.getRealmModelTableName(sharedRealm));

        return realm
                .getConfiguration()
                .getSchemaMediator()
                .getClazz(className);
    }

    private static <T extends RealmModel> T getRealmModel(BaseRealm realm, Class<T> clazz, NativeMixed nativeMixed) {
        return realm
                .get(clazz, nativeMixed.getRealmModelRowKey(), false, Collections.emptyList());
    }

    private final Class<? extends RealmModel> clazz;
    private final RealmModel value;

    RealmModelOperator(RealmModel realmModel) {
        this.value = realmModel;
        this.clazz = realmModel.getClass();
    }

    <T extends RealmModel> RealmModelOperator(BaseRealm realm, NativeMixed nativeMixed) {
        super(nativeMixed);

        Class<T> clazz = getModelClass(realm, nativeMixed);
        this.clazz = clazz;

        this.value = getRealmModel(realm, clazz, nativeMixed);
    }

    @Override
    protected NativeMixed createNativeMixed() {
        return new NativeMixed(getValue(RealmObjectProxy.class));
    }

    @Override
    <T> T getValue(Class<T> clazz) {
        return clazz.cast(value);
    }

    @Override
    MixedType getType() {
        return MixedType.OBJECT;
    }

    @Override
    Class<?> getTypedClass() {
        return clazz;
    }
}

final class DynamicRealmModelMixedOperator extends RealmModelOperator {
    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    private static <T extends RealmModel> T getRealmModel(BaseRealm realm, NativeMixed nativeMixed) {
        OsSharedRealm sharedRealm = realm.getSharedRealm();

        String className = Table.getClassNameForTable(nativeMixed.getRealmModelTableName(sharedRealm));

        return realm
                .get((Class<T>) DynamicRealmObject.class, className, nativeMixed.getRealmModelRowKey());
    }

    DynamicRealmModelMixedOperator(BaseRealm realm, NativeMixed nativeMixed) {
        super(getRealmModel(realm, nativeMixed));
    }

    @Override
    Class<?> getTypedClass() {
        return DynamicRealmObject.class;
    }
}
