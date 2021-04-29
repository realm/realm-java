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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.realm.exceptions.RealmException;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Table;
import io.realm.internal.core.NativeRealmAny;


public abstract class RealmAnyOperator {
    static RealmAnyOperator fromNativeRealmAny(BaseRealm realm, NativeRealmAny nativeRealmAny) {
        RealmAny.Type fieldType = nativeRealmAny.getType();

        switch (fieldType) {
            case INTEGER:
                return new IntegerRealmAnyOperator(nativeRealmAny);
            case BOOLEAN:
                return new BooleanRealmAnyOperator(nativeRealmAny);
            case STRING:
                return new StringRealmAnyOperator(nativeRealmAny);
            case BINARY:
                return new BinaryRealmAnyOperator(nativeRealmAny);
            case DATE:
                return new DateRealmAnyOperator(nativeRealmAny);
            case FLOAT:
                return new FloatRealmAnyOperator(nativeRealmAny);
            case DOUBLE:
                return new DoubleRealmAnyOperator(nativeRealmAny);
            case DECIMAL128:
                return new Decimal128RealmAnyOperator(nativeRealmAny);
            case OBJECT_ID:
                return new ObjectIdRealmAnyOperator(nativeRealmAny);
            case UUID:
                return new UUIDRealmAnyOperator(nativeRealmAny);
            case OBJECT:
                if (realm instanceof Realm) {
                    try {
                        Class<RealmModel> clazz = nativeRealmAny.getModelClass(realm.sharedRealm, realm.configuration.getSchemaMediator());
                        return new RealmModelOperator(realm, nativeRealmAny, clazz);
                    } catch (RealmException ignore) {
                        // Fall through to DynamicRealmModelOperator
                    }
                }
                return new DynamicRealmModelRealmAnyOperator(realm, nativeRealmAny);
            case NULL:
                return new NullRealmAnyOperator(nativeRealmAny);
            default:
                throw new ClassCastException("Couldn't cast to " + fieldType);
        }
    }

    @Nullable
    private NativeRealmAny nativeRealmAny;
    private RealmAny.Type type;

    private synchronized NativeRealmAny getNativeRealmAny() {
        if (nativeRealmAny == null) { nativeRealmAny = createNativeRealmAny(); }

        return nativeRealmAny;
    }

    long getNativePtr() {
        return getNativeRealmAny().getNativePtr();
    }

    protected abstract NativeRealmAny createNativeRealmAny();

    protected RealmAnyOperator(RealmAny.Type type) {
        this.type = type;
    }

    protected RealmAnyOperator(RealmAny.Type type, NativeRealmAny nativeRealmAny) {
        this.type = type;
        this.nativeRealmAny = nativeRealmAny;
    }

    abstract <T> T getValue(Class<T> clazz);

    RealmAny.Type getType(){
        return this.type;
    }

    Class<?> getTypedClass() {
        return type.getTypedClass();
    }

    boolean coercedEquals(RealmAnyOperator realmAnyOperator) {
        return getNativeRealmAny().coercedEquals(realmAnyOperator.getNativeRealmAny());
    }

    public void checkValidObject(BaseRealm realm) { }
}

final class NullRealmAnyOperator extends RealmAnyOperator {
    NullRealmAnyOperator() {
        super(RealmAny.Type.NULL);
    }

    NullRealmAnyOperator(NativeRealmAny nativeRealmAny) {
        super(RealmAny.Type.NULL, nativeRealmAny);
    }

    @Override
    protected NativeRealmAny createNativeRealmAny() {
        return new NativeRealmAny();
    }

    @Override
    public <T> T getValue(Class<T> clazz) {
        return null;
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return (other != null) && getClass().equals(other.getClass());
    }
}

abstract class PrimitiveRealmAnyOperator extends RealmAnyOperator {
    @Nullable
    private final Object value;

    PrimitiveRealmAnyOperator(@Nullable Object value, @Nonnull RealmAny.Type type) {
        super(type);
        this.value = value;
    }

    PrimitiveRealmAnyOperator(@Nullable Object value, @Nonnull RealmAny.Type type, @Nonnull NativeRealmAny nativeRealmAny) {
        super(type, nativeRealmAny);
        this.value = value;
    }

    @Override
    <T> T getValue(Class<T> clazz) {
        return clazz.cast(value);
    }

    @Override
    public final int hashCode() {
        return (this.value == null) ? 0 : this.value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if ((other == null) || !getClass().equals(other.getClass())) { return false; }

        PrimitiveRealmAnyOperator otherOperator = (PrimitiveRealmAnyOperator) other;
        return (this.value == null) ? (otherOperator.value == null) : this.value.equals(otherOperator.value);
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}

final class BooleanRealmAnyOperator extends PrimitiveRealmAnyOperator {
    BooleanRealmAnyOperator(Boolean value) {
        super(value, RealmAny.Type.BOOLEAN);
    }

    BooleanRealmAnyOperator(NativeRealmAny nativeRealmAny) {
        super(nativeRealmAny.asBoolean(), RealmAny.Type.BOOLEAN, nativeRealmAny);
    }

    @Override
    protected NativeRealmAny createNativeRealmAny() {
        return new NativeRealmAny(super.getValue(Boolean.class));
    }
}

final class IntegerRealmAnyOperator extends PrimitiveRealmAnyOperator {
    IntegerRealmAnyOperator(Byte value) {
        super(value, RealmAny.Type.INTEGER);
    }

    IntegerRealmAnyOperator(Short value) {
        super(value, RealmAny.Type.INTEGER);
    }

    IntegerRealmAnyOperator(Integer value) {
        super(value, RealmAny.Type.INTEGER);
    }

    IntegerRealmAnyOperator(Long value) {
        super(value, RealmAny.Type.INTEGER);
    }

    IntegerRealmAnyOperator(NativeRealmAny nativeRealmAny) {
        super(nativeRealmAny.asLong(), RealmAny.Type.INTEGER, nativeRealmAny);
    }

    @Override
    protected NativeRealmAny createNativeRealmAny() {
        return new NativeRealmAny(super.getValue(Number.class));
    }

    @Override
    public boolean equals(Object other) {
        if ((other == null) || !getClass().equals(other.getClass())) { return false; }

        RealmAnyOperator otherOperator = (RealmAnyOperator) other;
        return this.getValue(Number.class).longValue() == otherOperator.getValue(Number.class).longValue();
    }
}

final class FloatRealmAnyOperator extends PrimitiveRealmAnyOperator {
    FloatRealmAnyOperator(Float value) {
        super(value, RealmAny.Type.FLOAT);
    }

    FloatRealmAnyOperator(NativeRealmAny nativeRealmAny) {
        super(nativeRealmAny.asFloat(), RealmAny.Type.FLOAT, nativeRealmAny);
    }

    @Override
    protected NativeRealmAny createNativeRealmAny() {
        return new NativeRealmAny(super.getValue(Float.class));
    }
}

final class DoubleRealmAnyOperator extends PrimitiveRealmAnyOperator {
    DoubleRealmAnyOperator(Double value) {
        super(value, RealmAny.Type.DOUBLE);
    }

    DoubleRealmAnyOperator(NativeRealmAny nativeRealmAny) {
        super(nativeRealmAny.asDouble(), RealmAny.Type.DOUBLE, nativeRealmAny);
    }

    @Override
    protected NativeRealmAny createNativeRealmAny() {
        return new NativeRealmAny(super.getValue(Double.class));
    }
}

final class StringRealmAnyOperator extends PrimitiveRealmAnyOperator {
    StringRealmAnyOperator(String value) {
        super(value, RealmAny.Type.STRING);
    }

    StringRealmAnyOperator(NativeRealmAny nativeRealmAny) {
        super(nativeRealmAny.asString(), RealmAny.Type.STRING, nativeRealmAny);
    }

    @Override
    protected NativeRealmAny createNativeRealmAny() {
        return new NativeRealmAny(super.getValue(String.class));
    }
}

final class BinaryRealmAnyOperator extends PrimitiveRealmAnyOperator {
    BinaryRealmAnyOperator(byte[] value) {
        super(value, RealmAny.Type.BINARY);
    }

    BinaryRealmAnyOperator(NativeRealmAny nativeRealmAny) {
        super(nativeRealmAny.asBinary(), RealmAny.Type.BINARY, nativeRealmAny);
    }

    @Override
    protected NativeRealmAny createNativeRealmAny() {
        return new NativeRealmAny(super.getValue(byte[].class));
    }

    @Override
    public boolean equals(Object other) {
        if ((other == null) || !getClass().equals(other.getClass())) { return false; }

        RealmAnyOperator otherOperator = (RealmAnyOperator) other;
        return Arrays.equals(this.getValue(byte[].class), otherOperator.getValue(byte[].class));
    }
}

final class DateRealmAnyOperator extends PrimitiveRealmAnyOperator {
    DateRealmAnyOperator(Date value) {
        super(value, RealmAny.Type.DATE);
    }

    DateRealmAnyOperator(NativeRealmAny nativeRealmAny) {
        super(nativeRealmAny.asDate(), RealmAny.Type.DATE, nativeRealmAny);
    }

    @Override
    protected NativeRealmAny createNativeRealmAny() {
        return new NativeRealmAny(super.getValue(Date.class));
    }
}

final class ObjectIdRealmAnyOperator extends PrimitiveRealmAnyOperator {
    ObjectIdRealmAnyOperator(ObjectId value) {
        super(value, RealmAny.Type.OBJECT_ID);
    }

    ObjectIdRealmAnyOperator(NativeRealmAny nativeRealmAny) {
        super(nativeRealmAny.asObjectId(), RealmAny.Type.OBJECT_ID, nativeRealmAny);
    }

    @Override
    protected NativeRealmAny createNativeRealmAny() {
        return new NativeRealmAny(super.getValue(ObjectId.class));
    }
}

final class Decimal128RealmAnyOperator extends PrimitiveRealmAnyOperator {
    Decimal128RealmAnyOperator(Decimal128 value) {
        super(value, RealmAny.Type.DECIMAL128);
    }

    Decimal128RealmAnyOperator(NativeRealmAny nativeRealmAny) {
        super(nativeRealmAny.asDecimal128(), RealmAny.Type.DECIMAL128, nativeRealmAny);
    }

    @Override
    protected NativeRealmAny createNativeRealmAny() {
        return new NativeRealmAny(super.getValue(Decimal128.class));
    }
}

final class UUIDRealmAnyOperator extends PrimitiveRealmAnyOperator {
    UUIDRealmAnyOperator(UUID value) {
        super(value, RealmAny.Type.UUID);
    }

    UUIDRealmAnyOperator(NativeRealmAny nativeRealmAny) {
        super(nativeRealmAny.asUUID(), RealmAny.Type.UUID, nativeRealmAny);
    }

    @Override
    protected NativeRealmAny createNativeRealmAny() {
        return new NativeRealmAny(super.getValue(UUID.class));
    }
}

class RealmModelOperator extends RealmAnyOperator {
    private static <T extends RealmModel> T getRealmModel(BaseRealm realm, Class<T> clazz, NativeRealmAny nativeRealmAny) {
        return realm
                .get(clazz, nativeRealmAny.getRealmModelRowKey(), false, Collections.emptyList());
    }

    private final Class<? extends RealmModel> clazz;
    private final RealmModel value;

    RealmModelOperator(RealmModel realmModel) {
        super(RealmAny.Type.OBJECT);
        this.value = realmModel;
        this.clazz = realmModel.getClass();
    }

    <T extends RealmModel> RealmModelOperator(BaseRealm realm, NativeRealmAny nativeRealmAny, Class<T> clazz) {
        super(RealmAny.Type.OBJECT, nativeRealmAny);

        this.clazz = clazz;
        this.value = getRealmModel(realm, clazz, nativeRealmAny);
    }

    @Override
    protected NativeRealmAny createNativeRealmAny() {
        if (!(value instanceof RealmObjectProxy)) {
            throw new IllegalStateException("Native RealmAny instances only allow managed Realm objects or primitives");
        }
        return new NativeRealmAny(getValue(RealmObjectProxy.class));
    }

    @Override
    <T> T getValue(Class<T> clazz) {
        return clazz.cast(value);
    }

    @Override
    Class<?> getTypedClass() {
        return clazz;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if ((other == null) || !getClass().equals(other.getClass())) { return false; }

        RealmModelOperator otherOperator = (RealmModelOperator) other;
        return (this.value == null) ? (otherOperator.value == null) : this.value.equals(otherOperator.value);
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    @Override
    public void checkValidObject(BaseRealm realm) {
        if (!RealmObject.isValid(value) || !RealmObject.isManaged(value)) {
            throw new IllegalArgumentException("Realm object is not a valid managed object.");
        }
        if (((RealmObjectProxy) value).realmGet$proxyState().getRealm$realm() != realm) {
            throw new IllegalArgumentException("Realm object belongs to a different Realm.");
        }
    }
}

final class DynamicRealmModelRealmAnyOperator extends RealmModelOperator {
    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    private static <T extends RealmModel> T getRealmModel(BaseRealm realm, NativeRealmAny nativeRealmAny) {
        OsSharedRealm sharedRealm = realm.getSharedRealm();

        String className = Table.getClassNameForTable(nativeRealmAny.getRealmModelTableName(sharedRealm));

        return realm.get((Class<T>) DynamicRealmObject.class, className, nativeRealmAny.getRealmModelRowKey());
    }

    DynamicRealmModelRealmAnyOperator(BaseRealm realm, NativeRealmAny nativeRealmAny) {
        super(getRealmModel(realm, nativeRealmAny));
    }

    @Override
    Class<?> getTypedClass() {
        return DynamicRealmObject.class;
    }
}
