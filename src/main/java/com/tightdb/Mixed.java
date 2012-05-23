package com.tightdb;

import java.nio.ByteBuffer;
import java.util.Date;

public class Mixed {
	public Mixed(long value){
		this.value = new Long(value);
	}
	
	public Mixed(ColumnType columnType){
		assert(columnType == ColumnType.ColumnTypeTable);
		this.value = null;
	}
	
	public Mixed(boolean value){
		this.value = value ? Boolean.TRUE : Boolean.FALSE;
	}
	
	public Mixed(Date value){
		assert(value != null);
		this.value = value;
	}
	
	public Mixed(String value){
		assert(value != null);
		this.value = value;
	}
	
	public Mixed(ByteBuffer value){
		assert(value != null);
		this.value = value;
	}
	
	public boolean equals(Object second){
		if(second == null)
			return false;
		if(!(second instanceof Mixed))
			return false;
		Mixed secondMixed = (Mixed)second;
		if(value == null){
			if(secondMixed.value == null){
				return true;
			}else{
				return false;
			}
		}
		if(!getType().equals(secondMixed.getType())){
			return false;
		}
		if(value instanceof ByteBuffer){
			ByteBuffer firstByteBuffer = (ByteBuffer)value;
			ByteBuffer secondByteBuffer = (ByteBuffer)secondMixed.value;
			if(firstByteBuffer.capacity() != secondByteBuffer.capacity()){
				return false;
			}
			for(int i=0; i < firstByteBuffer.capacity(); i++){
				byte firstByte = firstByteBuffer.get(i);
				byte secondByte = secondByteBuffer.get(i);
				if(firstByte != secondByte)
					return false;
			}
			return true;
		}
		return this.value.equals(secondMixed.value);
	}
	
	public ColumnType getType(){
		if(value == null){
			return ColumnType.ColumnTypeTable;
		}
		if(value instanceof String)
			return ColumnType.ColumnTypeString;
		else if(value instanceof Long)
			return ColumnType.ColumnTypeInt;
		else if(value instanceof Date)
			return ColumnType.ColumnTypeDate;
		else if(value instanceof Boolean)
			return ColumnType.ColumnTypeBool;
		else if(value instanceof ByteBuffer){
			return ColumnType.ColumnTypeBinary;
		}
		return null;
	}
	
	public long getLongValue() throws IllegalAccessException {
		if(!(value instanceof Long)){
			throw new IllegalAccessException("Tryng to access an different type from mixed");
		}
		return ((Number)value).longValue();
	}
	
	public boolean getBooleanValue() throws IllegalAccessException {
		if(!(value instanceof Boolean))
			throw new IllegalAccessException("Trying to access an different type from mixed");
		return ((Boolean)value).booleanValue();
	}
	
	public String getStringValue() throws IllegalAccessException {
		if(!(value instanceof String))
			throw new IllegalAccessException("Trying to access an different type from mixed");
		return (String)value;
	}
	
	public Date getDateValue() throws IllegalAccessException {
		if(!(value instanceof Date)){
			throw new IllegalAccessException("Trying to access a different type from mixed");
		}
		return (Date)value;
	}
	
	protected long getDateTimeValue() throws IllegalAccessException {
		return getDateValue().getTime();
	}
	
	public ByteBuffer getBinaryValue() throws IllegalAccessException {
		if(!(value instanceof ByteBuffer)){
			throw new IllegalAccessException("Trying to access a different type from mixed");
		}
		return (ByteBuffer)value;
	}
	private Object value;
	
	public Object getValue() {
		return value;
	}
}
