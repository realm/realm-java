/*
 * Copyright 2015 Realm Inc.
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

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.realm.exceptions.RealmException;
import io.realm.internal.CheckedRow;
import io.realm.internal.OsList;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
import io.realm.internal.android.JsonUtils;


/**
 * Class that wraps a normal RealmObject in order to allow dynamic access instead of a typed interface.
 * Using a DynamicRealmObject is slower than using the regular RealmObject class.
 */
@SuppressWarnings("WeakerAccess")
public class DynamicRealmObject extends RealmObject implements RealmObjectProxy {
    static final String MSG_LINK_QUERY_NOT_SUPPORTED = "Queries across relationships are not supported";

    private final ProxyState<DynamicRealmObject> proxyState = new ProxyState<>(this);

    /**
     * Creates a dynamic Realm object based on an existing object.
     *
     * @param obj the Realm object to convert to a dynamic object. Only objects managed by {@link Realm} can be used.
     * @throws IllegalArgumentException if object isn't managed by Realm or is a {@link DynamicRealmObject} already.
     */
    public DynamicRealmObject(RealmModel obj) {
        //noinspection ConstantConditions
        if (obj == null) {
            throw new IllegalArgumentException("A non-null object must be provided.");
        }
        if (obj instanceof DynamicRealmObject) {
            throw new IllegalArgumentException("The object is already a DynamicRealmObject: " + obj);
        }

        if (!RealmObject.isManaged(obj)) {
            throw new IllegalArgumentException("An object managed by Realm must be provided. This " +
                    "is an unmanaged object.");
        }

        if (!RealmObject.isValid(obj)) {
            throw new IllegalArgumentException("A valid object managed by Realm must be provided. " +
                    "This object was deleted.");
        }

        RealmObjectProxy proxy = (RealmObjectProxy) obj;
        Row row = proxy.realmGet$proxyState().getRow$realm();
        proxyState.setRealm$realm(proxy.realmGet$proxyState().getRealm$realm());
        proxyState.setRow$realm(((UncheckedRow) row).convertToChecked());
        proxyState.setConstructionFinished();
    }

    // row must be an instance of CheckedRow or InvalidRow
    DynamicRealmObject(BaseRealm realm, Row row) {
        proxyState.setRealm$realm(realm);
        proxyState.setRow$realm(row);
        proxyState.setConstructionFinished();
    }

    /**
     * Returns the value for the given field.
     *
     * @param fieldName name of the field.
     * @return the field value.
     * @throws ClassCastException if the field doesn't contain a field of the defined return type.
     */
    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    public <E> E get(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        RealmFieldType type = proxyState.getRow$realm().getColumnType(columnIndex);
        switch (type) {
            case BOOLEAN:
                return (E) Boolean.valueOf(proxyState.getRow$realm().getBoolean(columnIndex));
            case INTEGER:
                return (E) Long.valueOf(proxyState.getRow$realm().getLong(columnIndex));
            case FLOAT:
                return (E) Float.valueOf(proxyState.getRow$realm().getFloat(columnIndex));
            case DOUBLE:
                return (E) Double.valueOf(proxyState.getRow$realm().getDouble(columnIndex));
            case STRING:
                return (E) proxyState.getRow$realm().getString(columnIndex);
            case BINARY:
                return (E) proxyState.getRow$realm().getBinaryByteArray(columnIndex);
            case DATE:
                return (E) proxyState.getRow$realm().getDate(columnIndex);
            case OBJECT:
                return (E) getObject(fieldName);
            case LIST:
                return (E) getList(fieldName);
            default:
                throw new IllegalStateException("Field type not supported: " + type);
        }
    }

    /**
     * Returns the {@code boolean} value for a given field.
     * <p>
     * If the field is nullable, use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the boolean value.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain booleans.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public boolean getBoolean(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        try {
            return proxyState.getRow$realm().getBoolean(columnIndex);
        } catch (IllegalArgumentException e) {
            checkFieldType(fieldName, columnIndex, RealmFieldType.BOOLEAN);
            throw e;
        }
    }

    /**
     * Returns the {@code int} value for a given field.
     * <p>
     * If the field is nullable, use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the int value. Integer values exceeding {@code Integer.MAX_VALUE} will wrap.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain integers.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public int getInt(String fieldName) {
        return (int) getLong(fieldName);
    }

    /**
     * Returns the {@code short} value for a given field.
     * <p>
     * If the field is nullable, use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the short value. Integer values exceeding {@code Short.MAX_VALUE} will wrap.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain integers.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public short getShort(String fieldName) {
        return (short) getLong(fieldName);
    }

    /**
     * Returns the {@code long} value for a given field.
     * <p>
     * If the field is nullable, use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the long value. Integer values exceeding {@code Long.MAX_VALUE} will wrap.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain integers.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public long getLong(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        try {
            return proxyState.getRow$realm().getLong(columnIndex);
        } catch (IllegalArgumentException e) {
            checkFieldType(fieldName, columnIndex, RealmFieldType.INTEGER);
            throw e;
        }
    }

    /**
     * Returns the {@code byte} value for a given field.
     * <p>
     * If the field is nullable, use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the byte value.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain integers.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public byte getByte(String fieldName) {
        return (byte) getLong(fieldName);
    }

    /**
     * Returns the {@code float} value for a given field.
     * <p>
     * If the field is nullable, use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the float value.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain floats.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public float getFloat(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        try {
            return proxyState.getRow$realm().getFloat(columnIndex);
        } catch (IllegalArgumentException e) {
            checkFieldType(fieldName, columnIndex, RealmFieldType.FLOAT);
            throw e;
        }
    }

    /**
     * Returns the {@code double} value for a given field.
     * <p>
     * If the field is nullable, use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the double value.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain doubles.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public double getDouble(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        try {
            return proxyState.getRow$realm().getDouble(columnIndex);
        } catch (IllegalArgumentException e) {
            checkFieldType(fieldName, columnIndex, RealmFieldType.DOUBLE);
            throw e;
        }
    }

    /**
     * Returns the {@code byte[]} value for a given field.
     *
     * @param fieldName the name of the field.
     * @return the byte[] value.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain binary data.
     */
    public byte[] getBlob(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        try {
            return proxyState.getRow$realm().getBinaryByteArray(columnIndex);
        } catch (IllegalArgumentException e) {
            checkFieldType(fieldName, columnIndex, RealmFieldType.BINARY);
            throw e;
        }
    }

    /**
     * Returns the {@code String} value for a given field.
     *
     * @param fieldName the name of the field.
     * @return the String value.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain Strings.
     */
    public String getString(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        try {
            return proxyState.getRow$realm().getString(columnIndex);
        } catch (IllegalArgumentException e) {
            checkFieldType(fieldName, columnIndex, RealmFieldType.STRING);
            throw e;
        }
    }

    /**
     * Returns the {@code Date} value for a given field.
     *
     * @param fieldName the name of the field.
     * @return the Date value.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain Dates.
     */
    public Date getDate(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        checkFieldType(fieldName, columnIndex, RealmFieldType.DATE);
        if (proxyState.getRow$realm().isNull(columnIndex)) {
            return null;
        } else {
            return proxyState.getRow$realm().getDate(columnIndex);
        }
    }

    /**
     * Returns the object being linked to from this field.
     *
     * @param fieldName the name of the field.
     * @return the {@link DynamicRealmObject} representation of the linked object or {@code null} if no object is linked.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain links to other objects.
     */
    @Nullable
    public DynamicRealmObject getObject(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        checkFieldType(fieldName, columnIndex, RealmFieldType.OBJECT);
        if (proxyState.getRow$realm().isNullLink(columnIndex)) {
            return null;
        } else {
            long linkRowIndex = proxyState.getRow$realm().getLink(columnIndex);
            CheckedRow linkRow = proxyState.getRow$realm().getTable().getLinkTarget(columnIndex).getCheckedRow(linkRowIndex);
            return new DynamicRealmObject(proxyState.getRealm$realm(), linkRow);
        }
    }

    /**
     * Returns the {@link RealmList} of {@link DynamicRealmObject}s being linked from the given field.
     * <p>
     * If the list contains primitive types, use {@link #getList(String, Class)} instead.
     *
     * @param fieldName the name of the field.
     * @return the {@link RealmList} data for this field.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain a list of objects.
     */
    public RealmList<DynamicRealmObject> getList(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        try {
            OsList osList = proxyState.getRow$realm().getModelList(columnIndex);
            //noinspection ConstantConditions
            @Nonnull
            String className = osList.getTargetTable().getClassName();
            return new RealmList<>(className, osList, proxyState.getRealm$realm());
        } catch (IllegalArgumentException e) {
            checkFieldType(fieldName, columnIndex, RealmFieldType.LIST);
            throw e;
        }
    }

    /**
     * Returns the {@link RealmList} containing only primitive values.
     *
     * <p>
     * If the list contains references to other Realm objects, use {@link #getList(String)} instead.
     *
     * @param fieldName the name of the field.
     * @param primitiveType the type of elements in the list. Only primitive types are supported.
     * @return the {@link RealmList} data for this field.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain a list of primitive objects.
     */
    public <E> RealmList<E> getList(String fieldName, Class<E> primitiveType) {
        proxyState.getRealm$realm().checkIfValid();

        if (primitiveType == null) {
            throw new IllegalArgumentException("Non-null 'primitiveType' required.");
        }
        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        RealmFieldType realmType = classToRealmType(primitiveType);
        try {
            OsList osList = proxyState.getRow$realm().getValueList(columnIndex, realmType);
            return new RealmList<>(primitiveType, osList, proxyState.getRealm$realm());
        } catch (IllegalArgumentException e) {
            checkFieldType(fieldName, columnIndex, realmType);
            throw e;
        }
    }

    private <E> RealmFieldType classToRealmType(Class<E> primitiveType) {
        if (primitiveType.equals(Integer.class)
                || primitiveType.equals(Long.class)
                || primitiveType.equals(Short.class)
                || primitiveType.equals(Byte.class)) {
            return RealmFieldType.INTEGER_LIST;
        } else if (primitiveType.equals(Boolean.class)) {
            return RealmFieldType.BOOLEAN_LIST;
        } else if (primitiveType.equals(String.class)) {
            return RealmFieldType.STRING_LIST;
        } else if (primitiveType.equals(byte[].class)) {
            return RealmFieldType.BINARY_LIST;
        } else if (primitiveType.equals(Date.class)) {
            return RealmFieldType.DATE_LIST;
        } else if (primitiveType.equals(Float.class)) {
            return RealmFieldType.FLOAT_LIST;
        } else if (primitiveType.equals(Double.class)) {
            return RealmFieldType.DOUBLE_LIST;
        } else {
            throw new IllegalArgumentException("Unsupported element type. Only primitive types supported. Yours was: " + primitiveType);
        }
    }

    /**
     * Checks if the value of a given field is {@code null}.
     *
     * @param fieldName the name of the field.
     * @return {@code true} if field value is null, {@code false} otherwise.
     * @throws IllegalArgumentException if field name doesn't exist.
     */
    public boolean isNull(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        RealmFieldType type = proxyState.getRow$realm().getColumnType(columnIndex);
        switch (type) {
            case OBJECT:
                return proxyState.getRow$realm().isNullLink(columnIndex);
            case BOOLEAN:
            case INTEGER:
            case FLOAT:
            case DOUBLE:
            case STRING:
            case BINARY:
            case DATE:
                return proxyState.getRow$realm().isNull(columnIndex);
            case LIST:
            case LINKING_OBJECTS:
            case INTEGER_LIST:
            case BOOLEAN_LIST:
            case STRING_LIST:
            case BINARY_LIST:
            case DATE_LIST:
            case FLOAT_LIST:
            case DOUBLE_LIST:
                // fall through
            default:
                return false;
        }
    }

    /**
     * Checks whether an object has the given field or not.
     *
     * @param fieldName field name to check.
     * @return {@code true} if the object has a field with the given name, {@code false} otherwise.
     */
    public boolean hasField(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        //noinspection SimplifiableIfStatement,ConstantConditions
        if (fieldName == null || fieldName.isEmpty()) {
            return false;
        }
        return proxyState.getRow$realm().hasColumn(fieldName);
    }

    /**
     * Returns the list of field names on this object.
     *
     * @return list of field names on this objects or the empty list if the object doesn't have any fields.
     */
    public String[] getFieldNames() {
        proxyState.getRealm$realm().checkIfValid();

        String[] keys = new String[(int) proxyState.getRow$realm().getColumnCount()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = proxyState.getRow$realm().getColumnName(i);
        }
        return keys;
    }

    /**
     * Sets the value for the given field. This method will automatically try to convert numbers and
     * booleans that are given as {@code String} to their appropriate type. For example {@code "10"}
     * will be converted to {@code 10} if the field type is {@code int}.
     * <p>
     * Using the typed setters will be faster than using this method.
     *
     * @throws IllegalArgumentException if field name doesn't exist or if the input value cannot be converted
     * to the appropriate input type.
     * @throws NumberFormatException if a String based number cannot be converted properly.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    @SuppressWarnings("unchecked")
    public void set(String fieldName, Object value) {
        proxyState.getRealm$realm().checkIfValid();

        boolean isString = (value instanceof String);
        String strValue = isString ? (String) value : null;

        // Does implicit conversion if needed.
        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        RealmFieldType type = proxyState.getRow$realm().getColumnType(columnIndex);
        if (isString && type != RealmFieldType.STRING) {
            switch (type) {
                case BOOLEAN:
                    value = Boolean.parseBoolean(strValue);
                    break;
                case INTEGER:
                    value = Long.parseLong(strValue);
                    break;
                case FLOAT:
                    value = Float.parseFloat(strValue);
                    break;
                case DOUBLE:
                    value = Double.parseDouble(strValue);
                    break;
                case DATE:
                    value = JsonUtils.stringToDate(strValue);
                    break;
                default:
                    throw new IllegalArgumentException(String.format(Locale.US,
                            "Field %s is not a String field, " +
                            "and the provide value could not be automatically converted: %s. Use a typed" +
                            "setter instead", fieldName, value));
            }
        }

        if (value == null) {
            setNull(fieldName);
        } else {
            setValue(fieldName, value);
        }
    }

    // Automatically finds the appropriate setter based on the objects type.
    private void setValue(String fieldName, Object value) {
        Class<?> valueClass = value.getClass();
        if (valueClass == Boolean.class) {
            setBoolean(fieldName, (Boolean) value);
        } else if (valueClass == Short.class) {
            setShort(fieldName, (Short) value);
        } else if (valueClass == Integer.class) {
            setInt(fieldName, (Integer) value);
        } else if (valueClass == Long.class) {
            setLong(fieldName, (Long) value);
        } else if (valueClass == Byte.class) {
            setByte(fieldName, (Byte) value);
        } else if (valueClass == Float.class) {
            setFloat(fieldName, (Float) value);
        } else if (valueClass == Double.class) {
            setDouble(fieldName, (Double) value);
        } else if (valueClass == String.class) {
            setString(fieldName, (String) value);
        } else if (value instanceof Date) {
            setDate(fieldName, (Date) value);
        } else if (value instanceof byte[]) {
            setBlob(fieldName, (byte[]) value);
        } else if (valueClass == DynamicRealmObject.class) {
            setObject(fieldName, (DynamicRealmObject) value);
        } else if (valueClass == RealmList.class) {
            RealmList<?> list = (RealmList<?>) value;
            setList(fieldName, list);
        } else {
            throw new IllegalArgumentException("Value is of an type not supported: " + value.getClass());
        }
    }

    /**
     * Sets the {@code boolean} value of the given field.
     *
     * @param fieldName field name to update.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't a boolean field.
     */
    public void setBoolean(String fieldName, boolean value) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        proxyState.getRow$realm().setBoolean(columnIndex, value);
    }

    /**
     * Sets the {@code short} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't an integer field.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    public void setShort(String fieldName, short value) {
        proxyState.getRealm$realm().checkIfValid();

        checkIsPrimaryKey(fieldName);
        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        proxyState.getRow$realm().setLong(columnIndex, value);
    }

    /**
     * Sets the {@code int} value of the given field.
     *
     * @param fieldName field name to update.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't an integer field.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    public void setInt(String fieldName, int value) {
        proxyState.getRealm$realm().checkIfValid();

        checkIsPrimaryKey(fieldName);
        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        proxyState.getRow$realm().setLong(columnIndex, value);
    }

    /**
     * Sets the {@code long} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't an integer field.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    public void setLong(String fieldName, long value) {
        proxyState.getRealm$realm().checkIfValid();

        checkIsPrimaryKey(fieldName);
        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        proxyState.getRow$realm().setLong(columnIndex, value);
    }

    /**
     * Sets the {@code byte} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't an integer field.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    public void setByte(String fieldName, byte value) {
        proxyState.getRealm$realm().checkIfValid();

        checkIsPrimaryKey(fieldName);
        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        proxyState.getRow$realm().setLong(columnIndex, value);
    }

    /**
     * Sets the {@code float} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't a float field.
     */
    public void setFloat(String fieldName, float value) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        proxyState.getRow$realm().setFloat(columnIndex, value);
    }

    /**
     * Sets the {@code double} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't a double field.
     */
    public void setDouble(String fieldName, double value) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        proxyState.getRow$realm().setDouble(columnIndex, value);
    }

    /**
     * Sets the {@code String} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't a String field.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    public void setString(String fieldName, @Nullable String value) {
        proxyState.getRealm$realm().checkIfValid();

        checkIsPrimaryKey(fieldName);
        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        proxyState.getRow$realm().setString(columnIndex, value);
    }

    /**
     * Sets the binary value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't a binary field.
     */
    public void setBlob(String fieldName, @Nullable byte[] value) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        proxyState.getRow$realm().setBinaryByteArray(columnIndex, value);
    }

    /**
     * Sets the {@code Date} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exist or field isn't a Date field.
     */
    public void setDate(String fieldName, @Nullable Date value) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        if (value == null) {
            proxyState.getRow$realm().setNull(columnIndex);
        } else {
            proxyState.getRow$realm().setDate(columnIndex, value);
        }
    }

    /**
     * Sets a reference to another object on the given field.
     *
     * @param fieldName field name.
     * @param value object to link to.
     * @throws IllegalArgumentException if field name doesn't exist, it doesn't link to other Realm objects, the type
     * of DynamicRealmObject doesn't match or it belongs to a different Realm.
     */
    public void setObject(String fieldName, @Nullable DynamicRealmObject value) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        if (value == null) {
            proxyState.getRow$realm().nullifyLink(columnIndex);
        } else {
            if (value.proxyState.getRealm$realm() == null || value.proxyState.getRow$realm() == null) {
                throw new IllegalArgumentException("Cannot link to objects that are not part of the Realm.");
            }
            if (proxyState.getRealm$realm() != value.proxyState.getRealm$realm()) {
                throw new IllegalArgumentException("Cannot add an object from another Realm instance.");
            }
            Table table = proxyState.getRow$realm().getTable().getLinkTarget(columnIndex);
            Table inputTable = value.proxyState.getRow$realm().getTable();
            if (!table.hasSameSchema(inputTable)) {
                throw new IllegalArgumentException(String.format(Locale.US,
                        "Type of object is wrong. Was %s, expected %s",
                        inputTable.getName(), table.getName()));
            }
            proxyState.getRow$realm().setLink(columnIndex, value.proxyState.getRow$realm().getIndex());
        }
    }

    /**
     * Sets the reference to a {@link RealmList} on the given field.
     *
     * @param fieldName field name.
     * @param list list of objects. Must either be primitive types or {@link DynamicRealmObject}s.
     * @throws IllegalArgumentException if field name doesn't exist, it is not a list field, the objects in the
     * list doesn't match the expected type or any Realm object in the list belongs to a different Realm.
     */
    public <E> void setList(String fieldName, RealmList<E> list) {
        proxyState.getRealm$realm().checkIfValid();

        //noinspection ConstantConditions
        if (list == null) {
            throw new IllegalArgumentException("Non-null 'list' required");
        }

        // Find type of list in Realm
        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        final RealmFieldType columnType = proxyState.getRow$realm().getColumnType(columnIndex);

        switch (columnType) {
            case LIST:
                // Due to type erasure it is not possible to check the generic parameter,
                // instead we try to see if the first element is of the wrong type in order
                // to throw a better error message.
                // Primitive types are checked inside `setModelList`
                if (!list.isEmpty()) {
                    E element = list.first();
                    if (!(element instanceof DynamicRealmObject) && RealmModel.class.isAssignableFrom(element.getClass())) {
                        throw new IllegalArgumentException("RealmList must contain `DynamicRealmObject's, not Java model classes.");
                    }
                }
                //noinspection unchecked
                setModelList(fieldName, (RealmList<DynamicRealmObject>) list);
                break;
            case INTEGER_LIST:
            case BOOLEAN_LIST:
            case STRING_LIST:
            case BINARY_LIST:
            case DATE_LIST:
            case FLOAT_LIST:
            case DOUBLE_LIST:
                setValueList(fieldName, list, columnType);
                break;
            default:
                throw new IllegalArgumentException(String.format("Field '%s' is not a list but a %s", fieldName, columnType));
        }
    }

    private void setModelList(String fieldName, RealmList<DynamicRealmObject> list) {
        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        OsList osList = proxyState.getRow$realm().getModelList(columnIndex);
        Table linkTargetTable = osList.getTargetTable();
        //noinspection ConstantConditions
        @Nonnull
        final String linkTargetTableName = linkTargetTable.getClassName();

        boolean typeValidated;
        if (list.className == null && list.clazz == null) {
            // Unmanaged lists don't know anything about the types they contain. They might even hold objects of
            // multiple types :(, so we have to check each item in the list.
            typeValidated = false;
        } else {
            String listType = list.className != null ? list.className
                    : proxyState.getRealm$realm().getSchema().getTable(list.clazz).getClassName();
            if (!linkTargetTableName.equals(listType)) {
                throw new IllegalArgumentException(String.format(Locale.US,
                        "The elements in the list are not the proper type. " +
                                "Was %s expected %s.", listType, linkTargetTableName));
            }
            typeValidated = true;
        }

        final int listLength = list.size();
        final long[] indices = new long[listLength];

        for (int i = 0; i < listLength; i++) {
            RealmObjectProxy obj = list.get(i);
            if (obj.realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm()) {
                throw new IllegalArgumentException("Each element in 'list' must belong to the same Realm instance.");
            }
            if (!typeValidated && !linkTargetTable.hasSameSchema(obj.realmGet$proxyState().getRow$realm().getTable())) {
                throw new IllegalArgumentException(String.format(Locale.US,
                        "Element at index %d is not the proper type. " +
                                "Was '%s' expected '%s'.",
                        i,
                        obj.realmGet$proxyState().getRow$realm().getTable().getClassName(),
                        linkTargetTableName));
            }
            indices[i] = obj.realmGet$proxyState().getRow$realm().getIndex();
        }

        osList.removeAll();
        for (int i = 0; i < listLength; i++) {
            osList.addRow(indices[i]);
        }
    }

    @SuppressWarnings("unchecked")
    private <E> void setValueList(String fieldName, RealmList<E> list, RealmFieldType primitiveType) {
        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        OsList osList = proxyState.getRow$realm().getValueList(columnIndex, primitiveType);

        Class<E> elementClass;
        switch(primitiveType) {
            case INTEGER_LIST: elementClass = (Class<E>) Long.class; break;
            case BOOLEAN_LIST: elementClass = (Class<E>) Boolean.class; break;
            case STRING_LIST: elementClass = (Class<E>) String.class; break;
            case BINARY_LIST: elementClass = (Class<E>) byte[].class; break;
            case DATE_LIST: elementClass = (Class<E>) Date.class; break;
            case FLOAT_LIST: elementClass = (Class<E>) Float.class; break;
            case DOUBLE_LIST: elementClass = (Class<E>) Double.class; break;
            default:
                throw new IllegalArgumentException("Unsupported type: " + primitiveType);
        }
        final ManagedListOperator<?> operator = getOperator(proxyState.getRealm$realm(), osList, primitiveType, elementClass);

        if (list.isManaged() && osList.size() == list.size()) {
            // There is a chance that the source list and the target list are the same list in the same object.
            // In this case, we can't use removeAll().
            final int size = list.size();
            final Iterator<?> iterator = list.iterator();
            for (int i = 0; i < size; i++) {
                @Nullable
                final Object value = iterator.next();
                operator.set(i, value);
            }
        }  else {
            osList.removeAll();
            for (Object value : list) {
                operator.append(value);
            }
        }
    }

    private <E> ManagedListOperator<E> getOperator(BaseRealm realm, OsList osList, RealmFieldType valueListType, Class<E> valueClass) {
        if (valueListType == RealmFieldType.STRING_LIST) {
            //noinspection unchecked
            return (ManagedListOperator<E>) new StringListOperator(realm, osList, (Class<String>) valueClass);
        }
        if (valueListType == RealmFieldType.INTEGER_LIST) {
            return new LongListOperator<>(realm, osList, valueClass);
        }
        if (valueListType == RealmFieldType.BOOLEAN_LIST) {
            //noinspection unchecked
            return (ManagedListOperator<E>) new BooleanListOperator(realm, osList, (Class<Boolean>) valueClass);
        }
        if (valueListType == RealmFieldType.BINARY_LIST) {
            //noinspection unchecked
            return (ManagedListOperator<E>) new BinaryListOperator(realm, osList, (Class<byte[]>) valueClass);
        }
        if (valueListType == RealmFieldType.DOUBLE_LIST) {
            //noinspection unchecked
            return (ManagedListOperator<E>) new DoubleListOperator(realm, osList, (Class<Double>) valueClass);
        }
        if (valueListType == RealmFieldType.FLOAT_LIST) {
            //noinspection unchecked
            return (ManagedListOperator<E>) new FloatListOperator(realm, osList, (Class<Float>) valueClass);
        }
        if (valueListType == RealmFieldType.DATE_LIST) {
            //noinspection unchecked
            return (ManagedListOperator<E>) new DateListOperator(realm, osList, (Class<Date>) valueClass);
        }
        throw new IllegalArgumentException("Unexpected list type: " + valueListType.name());
    }

    /**
     * Sets the value to {@code null} for the given field.
     *
     * @param fieldName field name.
     * @throws IllegalArgumentException if field name doesn't exist, or the field isn't nullable.
     * @throws RealmException if the field is a {@link io.realm.annotations.PrimaryKey} field.
     */
    public void setNull(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        RealmFieldType type = proxyState.getRow$realm().getColumnType(columnIndex);
        if (type == RealmFieldType.OBJECT) {
            proxyState.getRow$realm().nullifyLink(columnIndex);
        } else {
            checkIsPrimaryKey(fieldName);
            proxyState.getRow$realm().setNull(columnIndex);
        }
    }

    /**
     * Returns the type of object. This will normally correspond to the name of a class that is extending
     * {@link RealmObject}.
     *
     * @return this objects type.
     */
    public String getType() {
        proxyState.getRealm$realm().checkIfValid();

        return proxyState.getRow$realm().getTable().getClassName();
    }

    /**
     * Returns the type used by the underlying storage engine to represent this field.
     *
     * @return the underlying type used by Realm to represent this field.
     */
    public RealmFieldType getFieldType(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        return proxyState.getRow$realm().getColumnType(columnIndex);
    }

    private void checkFieldType(String fieldName, long columnIndex, RealmFieldType expectedType) {
        RealmFieldType columnType = proxyState.getRow$realm().getColumnType(columnIndex);
        if (columnType != expectedType) {
            String expectedIndefiniteVowel = "";
            if (expectedType == RealmFieldType.INTEGER || expectedType == RealmFieldType.OBJECT) {
                expectedIndefiniteVowel = "n";
            }
            String columnTypeIndefiniteVowel = "";
            if (columnType == RealmFieldType.INTEGER || columnType == RealmFieldType.OBJECT) {
                columnTypeIndefiniteVowel = "n";
            }
            throw new IllegalArgumentException(String.format(Locale.US,
                    "'%s' is not a%s '%s', but a%s '%s'.",
                    fieldName, expectedIndefiniteVowel, expectedType, columnTypeIndefiniteVowel, columnType));
        }
    }

    /**
     * Returns a hash code value for the {@link DynamicRealmObject} object.
     * <p>
     * By the general contract of {@link Object#hashCode()}, any two objects for which {@link #equals}
     * returns {@code true} must return the same hash code value.
     * <p>
     * Note that a {@link RealmObject} is a live object, and it might be updated by changes from
     * other threads. This means that a hash code value of the object is not stable, and the value
     * should be neither used as a key in HashMap nor saved in HashSet.
     *
     * @return a hash code value for the object.
     * @see #equals
     */
    @Override
    public int hashCode() {
        proxyState.getRealm$realm().checkIfValid();

        String realmName = proxyState.getRealm$realm().getPath();
        String tableName = proxyState.getRow$realm().getTable().getName();
        long rowIndex = proxyState.getRow$realm().getIndex();

        int result = 17;
        result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0);
        result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0);
        result = 31 * result + (int) (rowIndex ^ (rowIndex >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        proxyState.getRealm$realm().checkIfValid();

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DynamicRealmObject other = (DynamicRealmObject) o;

        String path = proxyState.getRealm$realm().getPath();
        String otherPath = other.proxyState.getRealm$realm().getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) {
            return false;
        }

        String tableName = proxyState.getRow$realm().getTable().getName();
        String otherTableName = other.proxyState.getRow$realm().getTable().getName();
        //noinspection SimplifiableIfStatement
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) {
            return false;
        }

        return proxyState.getRow$realm().getIndex() == other.proxyState.getRow$realm().getIndex();
    }

    @Override
    public String toString() {
        proxyState.getRealm$realm().checkIfValid();

        if (!proxyState.getRow$realm().isAttached()) {
            return "Invalid object";
        }

        final String className = proxyState.getRow$realm().getTable().getClassName();
        StringBuilder sb = new StringBuilder(className + " = dynamic[");
        String[] fields = getFieldNames();
        for (String field : fields) {
            long columnIndex = proxyState.getRow$realm().getColumnIndex(field);
            RealmFieldType type = proxyState.getRow$realm().getColumnType(columnIndex);
            sb.append("{");
            sb.append(field).append(":");
            switch (type) {
                case BOOLEAN:
                    sb.append(proxyState.getRow$realm().isNull(columnIndex) ? "null" : proxyState.getRow$realm().getBoolean(columnIndex));
                    break;
                case INTEGER:
                    sb.append(proxyState.getRow$realm().isNull(columnIndex) ? "null" : proxyState.getRow$realm().getLong(columnIndex));
                    break;
                case FLOAT:
                    sb.append(proxyState.getRow$realm().isNull(columnIndex) ? "null" : proxyState.getRow$realm().getFloat(columnIndex));
                    break;
                case DOUBLE:
                    sb.append(proxyState.getRow$realm().isNull(columnIndex) ? "null" : proxyState.getRow$realm().getDouble(columnIndex));
                    break;
                case STRING:
                    sb.append(proxyState.getRow$realm().getString(columnIndex));
                    break;
                case BINARY:
                    sb.append(Arrays.toString(proxyState.getRow$realm().getBinaryByteArray(columnIndex)));
                    break;
                case DATE:
                    sb.append(proxyState.getRow$realm().isNull(columnIndex) ? "null" : proxyState.getRow$realm().getDate(columnIndex));
                    break;
                case OBJECT:
                    sb.append(proxyState.getRow$realm().isNullLink(columnIndex)
                            ? "null"
                            : proxyState.getRow$realm().getTable().getLinkTarget(columnIndex).getClassName());
                    break;
                case LIST:
                    String targetClassName = proxyState.getRow$realm().getTable().getLinkTarget(columnIndex).getClassName();
                    sb.append(String.format(Locale.US, "RealmList<%s>[%s]", targetClassName, proxyState.getRow$realm().getModelList(columnIndex).size()));
                    break;
                case INTEGER_LIST:
                    sb.append(String.format(Locale.US, "RealmList<Long>[%s]", proxyState.getRow$realm().getValueList(columnIndex, type).size()));
                    break;
                case BOOLEAN_LIST:
                    sb.append(String.format(Locale.US, "RealmList<Boolean>[%s]", proxyState.getRow$realm().getValueList(columnIndex, type).size()));
                    break;
                case STRING_LIST:
                    sb.append(String.format(Locale.US, "RealmList<String>[%s]", proxyState.getRow$realm().getValueList(columnIndex, type).size()));
                    break;
                case BINARY_LIST:
                    sb.append(String.format(Locale.US, "RealmList<byte[]>[%s]", proxyState.getRow$realm().getValueList(columnIndex, type).size()));
                    break;
                case DATE_LIST:
                    sb.append(String.format(Locale.US, "RealmList<Date>[%s]", proxyState.getRow$realm().getValueList(columnIndex, type).size()));
                    break;
                case FLOAT_LIST:
                    sb.append(String.format(Locale.US, "RealmList<Float>[%s]", proxyState.getRow$realm().getValueList(columnIndex, type).size()));
                    break;
                case DOUBLE_LIST:
                    sb.append(String.format(Locale.US, "RealmList<Double>[%s]", proxyState.getRow$realm().getValueList(columnIndex, type).size()));
                    break;
                default:
                    sb.append("?");
                    break;
            }
            sb.append("},");
        }
        sb.replace(sb.length() - 1, sb.length(), "");
        sb.append("]");
        return sb.toString();
    }

    /**
     * Returns {@link RealmResults} containing all {@code srcClassName} class objects that have a relationship
     * to this object from {@code srcFieldName} field.
     * <p>
     * An entry is added for each reference, e.g. if the same reference is in a list multiple times,
     * the src object will show up here multiple times.
     *
     * @param srcClassName name of the class returned objects belong to.
     * @param srcFieldName name of the field in the source class that holds a reference to this object.
     *                    Field type must be either {@code io.realm.RealmFieldType.OBJECT} or {@code io.realm.RealmFieldType.LIST}.
     * @return the result.
     * @throws IllegalArgumentException if the {@code srcClassName} is {@code null} or does not exist,
     * the {@code srcFieldName} is {@code null} or does not exist,
     * type of the source field is not supported.
     */
    public RealmResults<DynamicRealmObject> linkingObjects(String srcClassName, String srcFieldName) {
        final DynamicRealm realm = (DynamicRealm) proxyState.getRealm$realm();
        realm.checkIfValid();
        proxyState.getRow$realm().checkIfAttached();

        final RealmSchema schema = realm.getSchema();
        final RealmObjectSchema realmObjectSchema = schema.get(srcClassName);
        if (realmObjectSchema == null) {
            throw new IllegalArgumentException("Class not found: " + srcClassName);
        }

        //noinspection ConstantConditions
        if (srcFieldName == null) {
            throw new IllegalArgumentException("Non-null 'srcFieldName' required.");
        }
        if (srcFieldName.contains(".")) {
            throw new IllegalArgumentException(MSG_LINK_QUERY_NOT_SUPPORTED);
        }

        final RealmFieldType fieldType = realmObjectSchema.getFieldType(srcFieldName); // throws IAE if not found
        if (fieldType != RealmFieldType.OBJECT && fieldType != RealmFieldType.LIST) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "Unexpected field type: %1$s. Field type should be either %2$s.%3$s or %2$s.%4$s.",
                    fieldType.name(),
                    RealmFieldType.class.getSimpleName(),
                    RealmFieldType.OBJECT.name(), RealmFieldType.LIST.name()));
        }

        return RealmResults.createDynamicBacklinkResults(realm, (CheckedRow) proxyState.getRow$realm(), realmObjectSchema.getTable(), srcFieldName);
    }

    /**
     * Returns {@link DynamicRealm} instance where this {@link DynamicRealmObject} belongs.
     * <p>
     * You <b>must not</b> call {@link DynamicRealm#close()} against returned instance.
     *
     * @return {@link DynamicRealm} instance where this object belongs.
     * @throws IllegalStateException if this object was deleted or the corresponding {@link DynamicRealm} was already closed.
     */
    public DynamicRealm getDynamicRealm() {
        final BaseRealm realm = realmGet$proxyState().getRealm$realm();
        realm.checkIfValid();
        if (!isValid()) {
            throw new IllegalStateException(MSG_DELETED_OBJECT);
        }
        return (DynamicRealm) realm;
    }

    @Override
    public void realm$injectObjectContext() {
        // nothing to do for DynamicRealmObject
    }

    @Override
    public ProxyState realmGet$proxyState() {
        return proxyState;
    }

    // Checks if the given field is primary key field. Throws if it is a PK field.
    private void checkIsPrimaryKey(String fieldName) {
        RealmObjectSchema objectSchema = proxyState.getRealm$realm().getSchema().getSchemaForClass(getType());
        if (objectSchema.hasPrimaryKey() && objectSchema.getPrimaryKey().equals(fieldName)) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "Primary key field '%s' cannot be changed after object was created.", fieldName));
        }
    }
}
