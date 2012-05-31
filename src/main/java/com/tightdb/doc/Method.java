package com.tightdb.doc;

public class Method {
	String ret;
	String name;
	String doc;
	Param[] params;

	public Method(String ret, String name, String doc, Param[] params) {
		this.ret = ret;
		this.name = name;
		this.doc = doc;
		this.params = params;
	}
}
