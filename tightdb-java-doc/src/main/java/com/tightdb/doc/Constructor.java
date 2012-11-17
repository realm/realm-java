package com.tightdb.doc;

import org.apache.commons.lang.StringUtils;

public class Constructor {

	int order;
	String doc;
	Param[] params;
	String className;

	public Constructor(String className, int order, String doc, Param[] params) {
		this.className = className;
		this.order = order;
		this.doc = doc;
		this.params = params;
	}

	public String getName() {
		return StringUtils.capitalize(className);
	}

	public String getId() {
		return className + "-constructor-" + order;
	}

}
