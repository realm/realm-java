package com.tightdb.generator;

import java.util.Map;

public class Field {

	private final String fieldType;
	private final String fieldName;
	private final Map<String, Object> attrs;

	public Field(String fieldType, String fieldName, Map<String, Object> attrs) {
		this.fieldType = fieldType;
		this.fieldName = fieldName;
		this.attrs = attrs;
	}

	public String getFieldType() {
		return fieldType;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Map<String, Object> getAttrs() {
		return attrs;
	}

}
