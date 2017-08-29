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
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.realm.exceptions.RealmException;
import io.realm.internal.CheckedRow;
import io.realm.internal.LinkView;
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
            case UNSUPPORTED_TABLE:
            case UNSUPPORTED_MIXED:
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
     * Returns the {@link RealmList} of objects being linked to from this field.
     *
     * @param fieldName the name of the field.
     * @return the {@link RealmList} data for this field.
     * @throws IllegalArgumentException if field name doesn't exist or it doesn't contain a list of links.
     */
    public RealmList<DynamicRealmObject> getList(String fieldName) {
        proxyState.getRealm$realm().checkIfValid();

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        try {
            LinkView linkView = proxyState.getRow$realm().getLinkList(columnIndex);
            //noinspection ConstantConditions
            @Nonnull
            String className = linkView.getTargetTable().getClassName();
            return new RealmList<>(className, linkView, proxyState.getRealm$realm());
        } catch (IllegalArgumentException e) {
            checkFieldType(fieldName, columnIndex, RealmFieldType.LIST);
            throw e;
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
            case UNSUPPORTED_TABLE:
            case UNSUPPORTED_MIXED:
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
            @SuppressWarnings("unchecked")
            RealmList<DynamicRealmObject> list = (RealmList<DynamicRealmObject>) value;
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
     * @param list list of references.
     * @throws IllegalArgumentException if field name doesn't exist, it is not a list field, the type
     * of the object represented by the DynamicRealmObject doesn't match or any element in the list belongs to a
     * different Realm.
     */
    public void setList(String fieldName, RealmList<DynamicRealmObject> list) {
        proxyState.getRealm$realm().checkIfValid();

        //noinspection ConstantConditions
        if (list == null) {
            throw new IllegalArgumentException("Null values not allowed for lists");
        }

        long columnIndex = proxyState.getRow$realm().getColumnIndex(fieldName);
        LinkView links = proxyState.getRow$realm().getLinkList(columnIndex);
        Table linkTargetTable = links.getTargetTable();
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

        links.clear();
        for (int i = 0; i < listLength; i++) {
            links.add(indices[i]);
        }
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
                    sb.append(String.format(Locale.US, "RealmList<%s>[%s]", targetClassName, proxyState.getRow$realm().getLinkList(columnIndex).size()));
                    break;
                case UNSUPPORTED_TABLE:
                case UNSUPPORTED_MIXED:
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
