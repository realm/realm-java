package com.tightdb.doc;

import java.util.List;

public abstract class AbstractDesc {

	private final List<Method> methods;

	public AbstractDesc(List<Method> methods) {
		this.methods = methods;
	}

	protected void method(String ret, String name, String doc, String... params) {
		methods.add(new Method(ret, name, doc, parameters(params)));
	}

	private Param[] parameters(String[] params) {
		Param[] parameters = new Param[params.length / 2];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = new Param();
			parameters[i].type = params[i * 2];
			parameters[i].name = params[i * 2 + 1];
		}
		return parameters;
	}

	public abstract void describe();
	
}
