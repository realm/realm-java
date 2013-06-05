package com.tightdb.doc;

public class Method {

    String className;
    String ret;
    String name;
    String doc;
    Param[] params;

    public Method(String className, String ret, String name, String doc,
            Param[] params) {
        this.className = className;
        this.ret = ret;
        this.name = name;
        this.doc = doc;
        this.params = params;
    }

    public String getId() {
        return className + "-" + name;
    }

}
