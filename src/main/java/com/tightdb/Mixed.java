package com.tightdb;

import java.nio.ByteBuffer;
import java.util.Date;

public class Mixed {

	public static final int BINARY_TYPE_BYTE_ARRAY = 0;
	public static final int BINARY_TYPE_BYTE_BUFFER = 1;

	public Mixed(long value) {
		this.value = new Long(value);
	}

	public Mixed(ColumnType columnType) {
		assert (columnType == ColumnType.ColumnTypeTable);
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
			return ColumnType.ColumnTypeTable;
		}
		if (value instanceof String)
			return ColumnType.ColumnTypeString;
		else if (value instanceof Long)
			return ColumnType.ColumnTypeInt;
		else if (value instanceof Date)
			return ColumnType.ColumnTypeDate;
		else if (value instanceof Boolean)
			return ColumnType.ColumnTypeBool;
		else if (value instanceof ByteBuffer || (value instanceof byte[])) {
			return ColumnType.ColumnTypeBinary;
		}
		
		throw new IllegalStateException("Unknown column type!");
	}

	public static Mixed mixedValue(Object value) {
		Mixed mixed;
		// TODO: Isn't it a slow way to convert? Can it be done faster?
		if (value instanceof String) {
			mixed = new Mixed((String) value);
		} else if (value instanceof Long) {
			mixed = new Mixed((Long) value);
		} else if (value instanceof Integer) {
			mixed = new Mixed(new Long(((Integer) value).intValue()));
		} else if (value instanceof Boolean) {
			mixed = new Mixed((Boolean) value);
		} else if (value instanceof Date) {
			mixed = new Mixed((Date) value);
		} else if (value instanceof ByteBuffer) {
			mixed = new Mixed((ByteBuffer) value);
		} else if (value instanceof byte[]) {
			mixed = new Mixed((byte[]) value);
			// TODO : cleanup
			// mixed = new Mixed(ByteBuffer.wrap((byte[]) value));
			/*
			 * byte [] array = (byte[] )value; ByteBuffer buffer =
			 * ByteBuffer.allocateDirect(array.length); buffer.put(array); mixed
			 * = new Mixed(buffer);
			 */
		} else {
			throw new IllegalArgumentException("The value is of unsupported type: " + value.getClass());
		}
		return mixed;
	}

	public long getLongValue() throws IllegalAccessException {
		if (!(value instanceof Long)) {
			throw new IllegalAccessException("Tryng to access an different type from mixed");
		}
		return ((Number) value).longValue();
	}

	public boolean getBooleanValue() throws IllegalAccessException {
		if (!(value instanceof Boolean))
			throw new IllegalAccessException("Trying to access an different type from mixed");
		return ((Boolean) value).booleanValue();
	}

	public String getStringValue() throws IllegalAccessException {
		if (!(value instanceof String))
			throw new IllegalAccessException("Trying to access an different type from mixed");
		return (String) value;
	}

	public Date getDateValue() throws IllegalAccessException {
		if (!(value instanceof Date)) {
			throw new IllegalAccessException("Trying to access a different type from mixed");
		}
		return (Date) value;
	}

	protected long getDateTimeValue() throws IllegalAccessException {
		return getDateValue().getTime();
	}

	public ByteBuffer getBinaryValue() throws IllegalAccessException {
		if (!(value instanceof ByteBuffer)) {
			throw new IllegalAccessException("Trying to access a different type from mixed");
		}
		return (ByteBuffer) value;
	}

	public byte[] getBinaryByteArray() throws IllegalAccessException {
		if (!(value instanceof byte[])) {
			throw new IllegalAccessException("Tryng to access a different type from Mixed");
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
}
