package com.tightdb.doc;

import java.util.List;

public abstract class AbstractDesc {

    private int constructorsCounter = 0;

    private final String className;
    private final List<Method> methods;
    private final List<Constructor> constructors;

    public AbstractDesc(String className, List<Constructor> constructors, List<Method> methods) {
        this.className = className;
        this.constructors = constructors;
        this.methods = methods;
    }

    protected void constructor(String doc, String... params) {
        constructors.add(new Constructor(className, ++constructorsCounter, doc,
                parameters(params)));
    }

    protected void method(String ret, String name, String doc, String... params) {
        methods.add(new Method(className, ret, name, doc, parameters(params)));
    }

    private Param[] parameters(String[] params) {
        Param[] parameters = new Param[params.length / 2];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = new Param();
            parameters[i].type = params[i * 2];
            parameters[i].name = params[i * 2 + 1];
            parameters[i].desc = null; // FIXME: provide description
        }
        return parameters;
    }

    public abstract void describe();

}
