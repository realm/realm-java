package com.tightdb;

import java.util.Arrays;
import java.util.Date;

public class Mixed {
	public Mixed(long value){
		this.value = new Long(value);
	}
	
	public Mixed(boolean value){
		this.value = value ? Boolean.TRUE : Boolean.FALSE;
	}
	
	public Mixed(Date value){
		this.value = value;
	}
	
	public Mixed(String value){
		this.value = value;
	}
	
	public Mixed(byte[] value){
		this.value = value;
	}
	
	public boolean equals(Object second){
		if(second == null)
			return false;
		if(!(second instanceof Mixed))
			return false;
		Mixed secondMixed = (Mixed)second;
		if(!getType().equals(secondMixed.getType())){
			return false;
		}
		if(value instanceof byte[]){
			return Arrays.equals((byte[])value, (byte[])secondMixed.value);
		}
		return this.value.equals(secondMixed.value);
	}
	
	public ColumnType getType(){
		if(value instanceof String)
			return ColumnType.ColumnTypeString;
		else if(value instanceof Long)
			return ColumnType.ColumnTypeInt;
		else if(value instanceof Date)
			return ColumnType.ColumnTypeDate;
		else if(value instanceof Boolean)
			return ColumnType.ColumnTypeBool;
		else if(value instanceof byte[]){
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
	
	public byte[] getBinaryValue() throws IllegalAccessException {
		if(!(value instanceof byte[])){
			throw new IllegalAccessException("Trying to access a different type from mixed");
		}
		return (byte[])value;
	}
	private Object value;
}
