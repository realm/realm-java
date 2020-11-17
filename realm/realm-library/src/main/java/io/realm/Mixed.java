package io.realm;

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.Date;

import javax.annotation.Nullable;

import io.realm.annotations.RealmField;
import io.realm.internal.ManageableObject;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;

public abstract class Mixed implements ManageableObject {
    private static final class Unmanaged extends Mixed {
        @Nullable
        private Object value;
        @Nullable
        private RealmFieldType fieldType;

        Unmanaged() {}

        @Override
        public boolean isManaged() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isFrozen() {
            return false;
        }

        @Override
        protected <T> Mixed set(@Nullable T value, RealmFieldType fieldType) {
            this.value = value;
            this.fieldType = fieldType;

            return this;
        }

        @Override
        @Nullable
        protected <T> T get(Class<T> clazz, RealmFieldType fieldType) {
            return clazz.cast(value);
        }

        @Override
        public boolean isNull() {
            return value == null;
        }

        @Override
        public RealmFieldType getType() {
            return fieldType;
        }
    }

    abstract static class Managed<M extends RealmModel> extends Mixed {
        protected abstract ProxyState<M> getProxyState();

        protected abstract long getColumnIndex();

        @Override
        public boolean isManaged() {
            return true;
        }

        @Override
        public final boolean isValid() {
            return !getRealm().isClosed() && getRow().isValid();
        }

        @Override
        public boolean isFrozen() {
            return getRealm().isFrozen();
        }

        @Override
        protected <T> Mixed set(@Nullable T value, RealmFieldType fieldType) {
            // TODO: Shall we check types? Devs could subclass Mixed to get access to this method

            ProxyState<M> proxyState = getProxyState();
            proxyState.getRealm$realm().checkIfValidAndInTransaction();

            if (!proxyState.isUnderConstruction()) {
                setValue(value, fieldType, false);
                return this;
            }

            if (!proxyState.getAcceptDefaultValue$realm()) {
                return this;
            }

            setValue(value, fieldType, true);

            return this;
        }

        @Override
        @Nullable
        protected <T> T get(Class<T> clazz, RealmFieldType fieldType) {
            Row row = getRow();
            Table table = row.getTable();
            long rowIndex = row.getObjectKey();
            long columnIndex = getColumnIndex();

            switch (fieldType){
                case INTEGER:
                    return clazz.cast(table.mixedAsLong(columnIndex, rowIndex));
                case BOOLEAN:
                    return clazz.cast(table.mixedAsBoolean(columnIndex, rowIndex));
                case FLOAT:
                    return clazz.cast(table.mixedAsFloat(columnIndex, rowIndex));
                case DOUBLE:
                    return clazz.cast(table.mixedAsDouble(columnIndex, rowIndex));
                case STRING:
                    return clazz.cast(table.mixedAsString(columnIndex, rowIndex));
                case BINARY:
                    return clazz.cast(table.mixedAsBinaryByteArray(columnIndex, rowIndex));
                case DATE:
                    return clazz.cast(table.mixedAsDate(columnIndex, rowIndex));
                case OBJECT_ID:
                    return clazz.cast(table.mixedAsObjectId(columnIndex, rowIndex));
                case DECIMAL128:
                    return clazz.cast(table.mixedAsDecimal128(columnIndex, rowIndex));
                case OBJECT:
                    return clazz.cast(table.mixedAsLink(columnIndex, rowIndex));
                default:
                    throw new ClassCastException("Couldn't cast to " + fieldType);
            }
        }

        @Override
        public boolean isNull() {
            Row row = getRow();
            Table table = row.getTable();
            long rowIndex = row.getObjectKey();
            long columnIndex = getColumnIndex();

            return table.mixedIsNull(columnIndex, rowIndex);
        }

        private BaseRealm getRealm() {
            return getProxyState().getRealm$realm();
        }

        @Override
        public RealmFieldType getType() {
            Row row = getRow();
            Table table = row.getTable();
            long rowIndex = row.getObjectKey();
            long columnIndex = getColumnIndex();

            return RealmFieldType.fromNativeValue(table.mixedGetType(columnIndex, rowIndex));
        }

        private Row getRow() {
            return getProxyState().getRow$realm();
        }

        private <T> void setValue(@Nullable T value, RealmFieldType fieldType, boolean isDefault) {
            Row row = getRow();
            Table table = row.getTable();
            long rowIndex = row.getObjectKey();
            long columnIndex = getColumnIndex();

            if (value == null) {
                table.mixedSetNull(columnIndex, rowIndex, isDefault);
            } else {
                switch (fieldType) {
                    case INTEGER:
                        table.mixedSetLong(columnIndex, rowIndex, (Long) value, isDefault);
                        break;
                    case BOOLEAN:
                        table.mixedSetBoolean(columnIndex, rowIndex, (Boolean) value, isDefault);
                        break;
                    case FLOAT:
                        table.mixedSetFloat(columnIndex, rowIndex, (Float) value, isDefault);
                        break;
                    case DOUBLE:
                        table.mixedSetDouble(columnIndex, rowIndex, (Double) value, isDefault);
                        break;
                    case STRING:
                        table.mixedSetString(columnIndex, rowIndex, (String) value, isDefault);
                        break;
                    case BINARY:
                        table.mixedSetBinaryByteArray(columnIndex, rowIndex, (byte[]) value, isDefault);
                        break;
                    case DATE:
                        table.mixedSetDate(columnIndex, rowIndex, (Date) value, isDefault);
                        break;
                    case OBJECT_ID:
                        table.mixedSetObjectId(columnIndex, rowIndex, (ObjectId) value, isDefault);
                        break;
                    case DECIMAL128:
                        table.mixedSetDecimal128(columnIndex, rowIndex, (Decimal128) value, isDefault);
                        break;
                    case OBJECT:
                        table.mixedSetLink(columnIndex, rowIndex, ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getObjectKey(), isDefault);
                        break;
                    default:
                        //TODO: throw exception
                }
            }
        }
    }

    protected abstract <T> Mixed set(@Nullable T value, RealmFieldType fieldType);

    @Nullable
    protected abstract <T> T get(Class<T> clazz, RealmFieldType fieldType);

    public static Mixed valueOf(Long value) {
        return new Unmanaged().set(value, RealmFieldType.INTEGER);
    }

    public abstract boolean isNull();

    public abstract RealmFieldType getType();

    public void set(Integer value) {
        set(value, RealmFieldType.INTEGER);
    }

    public void set(Boolean value) {
        set(value, RealmFieldType.BOOLEAN);
    }

    public void set(Float value) {
        set(value, RealmFieldType.FLOAT);
    }

    public void set(Double value) {
        set(value, RealmFieldType.DOUBLE);
    }

    public void set(String value) {
        set(value, RealmFieldType.STRING);
    }

    public void set(byte[] value) {
        set(value, RealmFieldType.BINARY);
    }

    public void set(Date value) {
        set(value, RealmFieldType.DATE);
    }

    public void set(ObjectId value) {
        set(value, RealmFieldType.OBJECT_ID);
    }

    public void set(Decimal128 value) {
        set(value, RealmFieldType.DECIMAL128);
    }

    public void set(RealmModel value) {
        set(value, RealmFieldType.OBJECT);
    }

    // TODO: Should be able to copy mixed into mixed?
    //    public void set(Mixed value) {
    //        set(value, Mixed.class);
    //    }

    public Long asInteger() {
        return get(Long.class, RealmFieldType.INTEGER);
    }

    public Boolean asBoolean() {
        return get(Boolean.class, RealmFieldType.BOOLEAN);
    }

    public Float asFloat() {
        return get(Float.class, RealmFieldType.FLOAT);
    }

    public Double asDouble() {
        return get(Double.class, RealmFieldType.DOUBLE);
    }

    public String asString() {
        return get(String.class, RealmFieldType.STRING);
    }

    public byte[] asBinary() {
        return get(byte[].class, RealmFieldType.BINARY);
    }

    public Date asDate() {
        return get(Date.class, RealmFieldType.DATE);
    }

    public ObjectId asObjectId() {
        return get(ObjectId.class, RealmFieldType.OBJECT_ID);
    }

    public Decimal128 asDecimal128() {
        return get(Decimal128.class, RealmFieldType.DECIMAL128);
    }

    public <T extends RealmModel> T asRealmModel(Class<T> clazz) {
        return get(clazz, RealmFieldType.OBJECT);
    }
}
