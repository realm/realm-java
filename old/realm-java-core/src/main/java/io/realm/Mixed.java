/*
 * Copyright 2014 Realm Inc.
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

import java.nio.ByteBuffer;
import java.util.Date;

public class Mixed {

    public static final int BINARY_TYPE_BYTE_ARRAY = 0;
    public static final int BINARY_TYPE_BYTE_BUFFER = 1;

    public Mixed(long value) {
        this.value = new Long(value);
    }

    public Mixed(float value) {
        this.value = new Float(value);
    }

    public Mixed(double value) {
        this.value = new Double(value);
    }

    public Mixed(ColumnType columnType) {
        // It's actually ok to call with any columnType - it will however be assumed to be a ColumnTypeTable.
        assert (columnType == null  || columnType == ColumnType.TABLE);
        this.value = null;
    }

    public Mixed(boolean value) {
        this.value = value ? Boolean.TRUE : Boolean.FALSE;
    }

    public Mixed(Date value) {
        assert (value != null);
        this.value = value;
    }

    public Mixed(String value) {
        assert (value != null);
        this.value = value;
    }

    public Mixed(ByteBuffer value) {
        assert (value != null);
        this.value = value;
    }

    public Mixed(byte[] value) {
        assert (value != null);
        this.value = value;
    }

    public boolean equals(Object second) {
        if (second == null)
            return false;
        if (!(second instanceof Mixed))
            return false;
        Mixed secondMixed = (Mixed) second;
        if (value == null) {
            if (secondMixed.value == null) {
                return true;
            } else {
                return false;
            }
        }
        if (!getType().equals(secondMixed.getType())) {
            return false;
        }
        if (value instanceof byte[]) {
            if (!(secondMixed.value instanceof byte[])) {
                return false;
            }
            byte[] firstBytes = (byte[]) value;
            byte[] secondBytes = (byte[]) secondMixed.value;
            if (firstBytes.length != secondBytes.length) {
                return false;
            }
            for (int i = 0; i < firstBytes.length; i++) {
                if (firstBytes[i] != secondBytes[i]) {
                    return false;
                }
            }
            return true;
        }
        if (value instanceof ByteBuffer) {
            ByteBuffer firstByteBuffer = (ByteBuffer) value;
            ByteBuffer secondByteBuffer = (ByteBuffer) secondMixed.value;
            if (firstByteBuffer.capacity() != secondByteBuffer.capacity()) {
                return false;
            }
            for (int i = 0; i < firstByteBuffer.capacity(); i++) {
                if (firstByteBuffer.get(i) != secondByteBuffer.get(i))
                    return false;
            }
            return true;
        }
        return this.value.equals(secondMixed.value);
    }

    public ColumnType getType() {
        if (value == null) {
            return ColumnType.TABLE;
        }
        if (value instanceof String)
            return ColumnType.STRING;
        else if (value instanceof Long)
            return ColumnType.INTEGER;
        else if (value instanceof Float)
            return ColumnType.FLOAT;
        else if (value instanceof Double)
            return ColumnType.DOUBLE;
        else if (value instanceof Date)
            return ColumnType.DATE;
        else if (value instanceof Boolean)
            return ColumnType.BOOLEAN;
        else if (value instanceof ByteBuffer || (value instanceof byte[])) {
            return ColumnType.BINARY;
        }

        throw new IllegalStateException("Unknown column type!");
    }

    public static Mixed mixedValue(Object value) {
        // TODO: Isn't it a slow way to convert? Can it be done faster?
        if (value instanceof String) {
            return new Mixed((String) value);
        } else if (value instanceof Long) {
            return new Mixed((Long) value);
        } else if (value instanceof Integer) {
            return new Mixed(((Integer)value).longValue());
        } else if (value instanceof Boolean) {
            return new Mixed((Boolean) value);
        } else if (value instanceof Float) {
            return new Mixed((Float) value);
        } else if (value instanceof Double) {
            return new Mixed((Double) value);
        } else if (value instanceof Date) {
            return new Mixed((Date) value);
        } else if (value instanceof ByteBuffer) {
            return new Mixed((ByteBuffer) value);
        } else if (value instanceof byte[]) {
            return new Mixed((byte[]) value);
        } else if (value instanceof Mixed) {
            return ((Mixed)(value));
        } else {
            throw new IllegalArgumentException("The value is of unsupported type: " + value.getClass());
        }
    }

    public long getLongValue() {
        if (!(value instanceof Long)) {
            throw new IllegalMixedTypeException("Can't get a long from a Mixed containg a " + getType());
        }
        return ((Long)value).longValue();
    }

    public boolean getBooleanValue() {
        if (!(value instanceof Boolean))
            throw new IllegalMixedTypeException("Can't get a boolean from a Mixed containg a " + getType());
        return ((Boolean) value).booleanValue();
    }

    public float getFloatValue() {
        if (!(value instanceof Float))
            throw new IllegalMixedTypeException("Can't get a float from a Mixed containg a " + getType());
        return ((Float) value).floatValue();
    }

    public double getDoubleValue() {
        if (!(value instanceof Double))
            throw new IllegalMixedTypeException("Can't get a double from a Mixed containg a " + getType());
        return ((Double) value).doubleValue();
    }

    public String getStringValue() {
        if (!(value instanceof String))
            throw new IllegalMixedTypeException("Can't get a String from a Mixed containg a " + getType());
        return (String) value;
    }

    public Date getDateValue() {
        if (!(value instanceof Date)) {
            throw new IllegalMixedTypeException("Can't get a Date from a Mixed containg a " + getType());
        }
        return (Date) value;
    }

    protected long getDateTimeValue() {
        return getDateValue().getTime();
    }

    public ByteBuffer getBinaryValue() {
        if (!(value instanceof ByteBuffer)) {
            throw new IllegalMixedTypeException("Can't get a ByteBuffer from a Mixed containg a " + getType());
        }
        return (ByteBuffer) value;
    }

    public byte[] getBinaryByteArray() {
        if (!(value instanceof byte[])) {
            throw new IllegalMixedTypeException("Can't get a byte[] from a Mixed containg a " + getType());
        }
        return (byte[]) value;
    }

    public int getBinaryType() {
        if (value instanceof byte[]) {
            return BINARY_TYPE_BYTE_ARRAY;
        }
        if (value instanceof ByteBuffer) {
            return BINARY_TYPE_BYTE_BUFFER;
        }
        return -1;
    }

    private Object value;

    public Object getValue() {
        return value;
    }

    public String getReadableValue() {
        ColumnType type = getType();
        try {
            switch (type) {
            case BINARY:
                return "Binary";
            case BOOLEAN:
                return String.valueOf(getBooleanValue());
            case DATE:
                return String.valueOf(getDateValue());
            case DOUBLE:
                return String.valueOf(getDoubleValue());
            case FLOAT:
                return String.valueOf(getFloatValue());
            case INTEGER:
                return String.valueOf(getLongValue());
            case STRING:
                return String.valueOf(getStringValue());
            case TABLE:
                return "Subtable";
            case MIXED:
                break; // error
            }
        } catch (Exception e) {
        }
        return "ERROR";
    }
}
