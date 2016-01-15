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

import io.realm.internal.CheckedRow;
import io.realm.internal.LinkView;
import io.realm.internal.Row;
import io.realm.internal.Table;
import io.realm.internal.UncheckedRow;
import io.realm.internal.android.JsonUtils;

/**
 * Class that wraps a normal RealmObject in order to allow dynamic access instead of a typed interface.
 * Using a DynamicRealmObject is slower than using the regular RealmObject class.
 */
public final class DynamicRealmObject extends RealmObject {
    private String className;

    /**
     * Creates a dynamic Realm object based on an existing object.
     *
     * @param obj the Realm object to convert to a dynamic object. Only objects managed by {@link Realm} can be used.
     * @throws IllegalArgumentException if object isn't managed by Realm or is a {@link DynamicRealmObject} already.
     */
    public DynamicRealmObject(RealmObject obj) {
        if (obj == null) {
            throw new IllegalArgumentException("A non-null object must be provided.");
        }
        if (obj instanceof DynamicRealmObject) {
            throw new IllegalArgumentException("The object is already a DynamicRealmObject: " + obj);
        }

        Row row = obj.row;
        if (!obj.isValid()) {
            throw new IllegalArgumentException("An object managed by Realm must be provided. This " +
                    "is a standalone object or it was deleted.");
        }
        this.realm = obj.realm;
        this.row = ((UncheckedRow) row).convertToChecked();
    }

    // Create a dynamic object. Only used internally
    DynamicRealmObject() {

    }

    DynamicRealmObject(BaseRealm realm, Row row) {
        this.realm = realm;
        this.row = (row instanceof CheckedRow) ? (CheckedRow) row : ((UncheckedRow) row).convertToChecked();
    }

    DynamicRealmObject(String className) {
        this.className = className;
    }

    /**
     * Returns the value for the given field.
     *
     * @param fieldName name of the field.
     * @return the field value.
     * @throws ClassCastException if the field doesn't contain a field of the defined return type.
     */
    @SuppressWarnings("unchecked")
    public <E> E get(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        RealmFieldType type = row.getColumnType(columnIndex);
        switch (type) {
            case BOOLEAN: return (E) Boolean.valueOf(row.getBoolean(columnIndex));
            case INTEGER: return (E) Long.valueOf(row.getLong(columnIndex));
            case FLOAT: return (E) Float.valueOf(row.getFloat(columnIndex));
            case DOUBLE: return (E) Double.valueOf(row.getDouble(columnIndex));
            case STRING: return (E) row.getString(columnIndex);
            case BINARY: return (E) row.getBinaryByteArray(columnIndex);
            case DATE: return (E) row.getDate(columnIndex);
            case OBJECT: return (E) getObject(fieldName);
            case LIST: return (E) getList(fieldName);
            case UNSUPPORTED_TABLE:
            case UNSUPPORTED_MIXED:
            default:
                throw new IllegalStateException("Field type not supported: " + type);
        }
    }

    /**
     * Returns the {@code boolean} value for a given field.
     * If the field is nullable use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the boolean value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain booleans.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public boolean getBoolean(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getBoolean(columnIndex);
    }

    /**
     * Returns the {@code int} value for a given field.
     * If the field is nullable use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the int value. Integer values exceeding {@code Integer.MAX_VALUE} will wrap.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain integers.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public int getInt(String fieldName) {
        return (int) getLong(fieldName);
    }

    /**
     * Returns the {@code short} value for a given field.
     * If the field is nullable use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the short value. Integer values exceeding {@code Short.MAX_VALUE} will wrap.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain integers.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public short getShort(String fieldName) {
        return (short) getLong(fieldName);
    }

    /**
     * Returns the {@code long} value for a given field.
     * If the field is nullable use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the long value. Integer values exceeding {@code Long.MAX_VALUE} will wrap.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain integers.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public long getLong(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getLong(columnIndex);
    }

    /**
     * Returns the {@code byte} value for a given field.
     * If the field is nullable use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the byte value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain integers.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public byte getByte(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return (byte) row.getLong(columnIndex);
    }

    /**
     * Returns the {@code float} value for a given field.
     * If the field is nullable use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the float value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain floats.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public float getFloat(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getFloat(columnIndex);
    }

    /**
     * Returns the {@code double} value for a given field.
     * If the field is nullable use {@link #isNull(String)} to check for {@code null} instead of using
     * this method.
     *
     * @param fieldName the name of the field.
     * @return the double value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain doubles.
     * @throws io.realm.exceptions.RealmException if the return value would be {@code null}.
     */
    public double getDouble(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getDouble(columnIndex);
    }

    /**
     * Returns the {@code byte[]} value for a given field.
     *
     * @param fieldName the name of the field.
     * @return the byte[] value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain binary data.
     */
    public byte[] getBlob(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getBinaryByteArray(columnIndex);
    }

    /**
     * Returns the {@code String} value for a given field.
     *
     * @param fieldName the name of the field.
     * @return the String value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain Strings.
     */
    public String getString(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getString(columnIndex);
    }

    /**
     * Returns the {@code Date} value for a given field.
     *
     * @param fieldName the name of the field.
     * @return the Date value.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain Dates.
     */
    public Date getDate(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        if (row.isNull(columnIndex)) {
            return null;
        } else {
            return row.getDate(columnIndex);
        }
    }

    /**
     * Returns the object being linked to from this field.
     *
     * @param fieldName the name of the field.
     * @return the {@link DynamicRealmObject} representation of the linked object or {@code null} if no object is linked.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain links to other objects.
     */
    public DynamicRealmObject getObject(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        if (row.isNullLink(columnIndex)) {
            return null;
        } else {
            long linkRowIndex = row.getLink(columnIndex);
            CheckedRow linkRow = row.getTable().getLinkTarget(columnIndex).getCheckedRow(linkRowIndex);
            return new DynamicRealmObject(realm, linkRow);
        }
    }

    /**
     * Returns the {@link RealmList} of objects being linked to from this field.
     *
     * @param fieldName the name of the field.
     * @return the {@link RealmList} data for this field.
     * @throws IllegalArgumentException if field name doesn't exists or it doesn't contain a list of links.
     */
    public RealmList<DynamicRealmObject> getList(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        LinkView linkView = row.getLinkList(columnIndex);
        String className = linkView.getTable().getName().substring(Table.TABLE_PREFIX.length());
        return new RealmList<DynamicRealmObject>(className, linkView, realm);
    }

    /**
     * Checks if the value of a given field is {@code null}.
     *
     * @param fieldName name of field.
     * @param fieldName the name of the field.
     * @return {@code true} if field value is null, {@code false} otherwise.
     * @throws IllegalArgumentException if field name doesn't exists.
     */
    public boolean isNull(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        RealmFieldType type = row.getColumnType(columnIndex);
        switch (type) {
            case OBJECT:
                return row.isNullLink(columnIndex);
            case BOOLEAN:
            case INTEGER:
            case FLOAT:
            case DOUBLE:
            case STRING:
            case BINARY:
            case DATE:
                return row.isNull(columnIndex);
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
        //noinspection SimplifiableIfStatement
        if (fieldName == null || fieldName.isEmpty()) {
            return false;
        }
        return row.hasColumn(fieldName);
    }

    /**
     * Returns the list of field names on this object.
     *
     * @return list of field names on this objects or the empty list if the object doesn't have any fields.
     */
    public String[] getFieldNames() {
        String[] keys = new String[(int) row.getColumnCount()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = row.getColumnName(i);
        }
        return keys;
    }

    /**
     * Set the value for the given field. This method will automatically try to convert numbers and
     * booleans that are given as {@code String} to their appropriate type. E.g. {@code "10"} will be
     * converted to {@code 10} if the field type is {@code int}.
     *
     * Using the typed setters will be faster than using this method.
     *
     * @throws IllegalArgumentException if field name doesn't exists or if the input value cannot be converted
     * to the appropriate input type.
     * @throws NumberFormatException if a String based number cannot be converted properly.
     */
    @SuppressWarnings("unchecked")
    public void set(String fieldName, Object value) {
        boolean isString = (value instanceof String);
        String strValue = isString ? (String) value : null;

        // Do implicit conversion if needed
        long columnIndex = row.getColumnIndex(fieldName);
        RealmFieldType type = row.getColumnType(columnIndex);
        if (isString && type != RealmFieldType.STRING) {
            switch(type) {
                case BOOLEAN: value = Boolean.parseBoolean(strValue); break;
                case INTEGER: value = Long.parseLong(strValue); break;
                case FLOAT: value = Float.parseFloat(strValue); break;
                case DOUBLE: value = Double.parseDouble(strValue); break;
                case DATE: value = JsonUtils.stringToDate(strValue); break;
                default:
                    throw new IllegalArgumentException(String.format("Field %s is not a String field, " +
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

    // Automatically finds the appropriate setter based on the objects type
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
            RealmList<RealmObject> list = (RealmList<RealmObject>) value;
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
     * @throws IllegalArgumentException if field name doesn't exists or isn't a boolean field.
     */
    public void setBoolean(String fieldName, boolean value) {
        long columnIndex = row.getColumnIndex(fieldName);
        row.setBoolean(columnIndex, value);
    }

    /**
     * Sets the {@code short} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't an integer field.
     */
    public void setShort(String fieldName, short value) {
        long columnIndex = row.getColumnIndex(fieldName);
        row.setLong(columnIndex, value);
    }

    /**
     * Sets the {@code int} value of the given field.
     *
     * @param fieldName field name to update.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't an integer field.
     */
    public void setInt(String fieldName, int value) {
        long columnIndex = row.getColumnIndex(fieldName);
        row.setLong(columnIndex, value);
    }

    /**
     * Sets the {@code long} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't an integer field.
     */
    public void setLong(String fieldName, long value) {
        long columnIndex = row.getColumnIndex(fieldName);
        row.setLong(columnIndex, value);
    }

    /**
     * Sets the {@code byte} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't an integer field.
     */
    public void setByte(String fieldName, byte value) {
        long columnIndex = row.getColumnIndex(fieldName);
        row.setLong(columnIndex, value);
    }

    /**
     * Sets the {@code float} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't an integer field.
     */
    public void setFloat(String fieldName, float value) {
        long columnIndex = row.getColumnIndex(fieldName);
        row.setFloat(columnIndex, value);
    }

    /**
     * Sets the {@code double} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't a double field.
     */
    public void setDouble(String fieldName, double value) {
        long columnIndex = row.getColumnIndex(fieldName);
        row.setDouble(columnIndex, value);
    }

    /**
     * Sets the {@code String} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't a String field.
     */
    public void setString(String fieldName, String value) {
        long columnIndex = row.getColumnIndex(fieldName);
        row.setString(columnIndex, value);
    }

    /**
     * Sets the binary value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't a binary field.
     */
    public void setBlob(String fieldName, byte[] value) {
        long columnIndex = row.getColumnIndex(fieldName);
        row.setBinaryByteArray(columnIndex, value);
    }

    /**
     * Sets the {@code Date} value of the given field.
     *
     * @param fieldName field name.
     * @param value value to insert.
     * @throws IllegalArgumentException if field name doesn't exists or isn't a Date field.
     */
    public void setDate(String fieldName, Date value) {
        long columnIndex = row.getColumnIndex(fieldName);
        if (value == null) {
            row.setNull(columnIndex);
        } else {
            row.setDate(columnIndex, value);
        }
    }

    /**
     * Sets a reference to another object on the given field.
     *
     * @param fieldName field name.
     * @param value object to link to.
     * @throws IllegalArgumentException if field name doesn't exists, it doesn't link to other Realm objects, or the type
     * of DynamicRealmObject doesn't match.
     */
    public void setObject(String fieldName, DynamicRealmObject value) {
        long columnIndex = row.getColumnIndex(fieldName);
        if (value == null) {
            row.nullifyLink(columnIndex);
        } else {
            if (value.realm == null || value.row == null) {
                throw new IllegalArgumentException("Cannot link to objects that are not part of the Realm.");
            }
            if (!realm.getConfiguration().equals(value.realm.getConfiguration())) {
                throw new IllegalArgumentException("Cannot add an object from another Realm");
            }
            Table table = row.getTable().getLinkTarget(columnIndex);
            Table inputTable = value.row.getTable();
            if (!table.hasSameSchema(inputTable)) {
                throw new IllegalArgumentException(String.format("Type of object is wrong. Was %s, expected %s",
                        inputTable.getName(), table.getName()));
            }
            row.setLink(columnIndex, value.row.getIndex());
        }
    }

    /**
     * Sets the reference to a {@link RealmList} on the given field.
     *
     * @param fieldName field name.
     * @param list list of references.
     * @throws IllegalArgumentException if field name doesn't exists, it is not a list field or the type
     * of the object represented by the DynamicRealmObject doesn't match.
     */
    public void setList(String fieldName, RealmList<? extends RealmObject> list) {
        if (list == null) {
            throw new IllegalArgumentException("Null values not allowed for lists");
        }

        String tableName = row.getTable().getName();
        boolean typeValidated;
        if (list.className == null && list.clazz == null) {
            // Standalone lists don't know anything about the types they contain. They might even hold objects of
            // multiple types :(, so we have to check each item in the list.
            typeValidated = false;
        } else {
            String listType = list.className != null ? list.className : realm.schema.getTable(list.clazz).getName();
            if (!tableName.equals(listType)) {
                throw new IllegalArgumentException(String.format("The elements in the list is not the proper type. " +
                        "Was %s expected %s.", listType, tableName));
            }
            typeValidated = true;
        }

        long columnIndex = row.getColumnIndex(fieldName);
        LinkView links = row.getLinkList(columnIndex);
        links.clear();
        for (int i = 0; i < list.size(); i++) {
            RealmObject obj = list.get(i);
            if (!typeValidated) {
                String elementType = obj.row.getTable().getName();
                if (!tableName.equals(elementType)) {
                    throw new IllegalArgumentException(String.format("Element at index %d is not the proper type. " +
                            "Was %s expected %s.", i, elementType, tableName));
                }
            }
            links.add(obj.row.getIndex());
        }
    }

    /**
     * Sets the value to {@code null} for the given field.
     *
     * @param fieldName field name.
     * @throws IllegalArgumentException if field name doesn't exists, or the field isn't nullable.
     */
    public void setNull(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        RealmFieldType type = row.getColumnType(columnIndex);
        if (type == RealmFieldType.OBJECT) {
            row.nullifyLink(columnIndex);
        } else {
            row.setNull(columnIndex);
        }
    }

    /**
     * Return the type of object. This will normally correspond to the name of a class that is extending
     * {@link RealmObject}.
     *
     * @return this objects type.
     */
    public String getType() {
        return row.getTable().getName().substring(Table.TABLE_PREFIX.length());
    }

    /**
     * Returns the type used by the underlying storage engine to represent this field.
     *
     * @return the underlying type used by Realm to represent this field.
     */
    public RealmFieldType getFieldType(String fieldName) {
        long columnIndex = row.getColumnIndex(fieldName);
        return row.getColumnType(columnIndex);
    }

    @Override
    public int hashCode() {
        String realmName = realm.getPath();
        String tableName = row.getTable().getName();
        long rowIndex = row.getIndex();

        int result = 17;
        result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0);
        result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0);
        result = 31 * result + (int) (rowIndex ^ (rowIndex >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DynamicRealmObject other = (DynamicRealmObject) o;

        String path = realm.getPath();
        String otherPath = other.realm.getPath();
        if (path != null ? !path.equals(otherPath) : otherPath != null) {
            return false;
        }

        String tableName = row.getTable().getName();
        String otherTableName = other.row.getTable().getName();
        //noinspection SimplifiableIfStatement
        if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) {
            return false;
        }

        return row.getIndex() == other.row.getIndex();
    }

    @Override
    public String toString() {
        if (row == null || !row.isAttached()) {
            return "Invalid object";
        }
        StringBuilder sb = new StringBuilder(row.getTable().getName() + " = [");
        String[] fields = getFieldNames();
        for (String field : fields) {
            long columnIndex = row.getColumnIndex(field);
            RealmFieldType type = row.getColumnType(columnIndex);
            sb.append("{");
            switch (type) {
                case BOOLEAN: sb.append(field).append(": ").append(row.getBoolean(columnIndex)); break;
                case INTEGER: sb.append(field).append(": ").append(row.getLong(columnIndex)); break;
                case FLOAT: sb.append(field).append(": ").append(row.getFloat(columnIndex)); break;
                case DOUBLE: sb.append(field).append(": ").append(row.getDouble(columnIndex)); break;
                case STRING: sb.append(field).append(": ").append(row.getString(columnIndex)); break;
                case BINARY: sb.append(field).append(": ").append(Arrays.toString(row.getBinaryByteArray(columnIndex))); break;
                case DATE: sb.append(field).append(": ").append(row.getDate(columnIndex)); break;
                case OBJECT:
                    if (row.isNullLink(columnIndex)) {
                        sb.append("null");
                    } else {
                        sb.append(field).append(": ").append(row.getTable().getLinkTarget(columnIndex).getName());
                    }
                    break;
                case LIST:
                    String targetType = row.getTable().getLinkTarget(columnIndex).getName();
                    sb.append(String.format("%s: RealmList<%s>[%s]", field, targetType, row.getLinkList(columnIndex).size()));
                    break;
                case UNSUPPORTED_TABLE:
                case UNSUPPORTED_MIXED:
                default:
                    sb.append(field).append(": ?");
            }
            sb.append("}, ");
        }
        sb.replace(sb.length() - 2, sb.length(), "");
        sb.append("]");
        return sb.toString();
    }

    @Override
    protected Table getTable() {
        if (className != null) {
            return realm.schema.getTable(className);
        }
        return super.getTable();
    }
}
