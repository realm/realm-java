package com.tightdb.generator;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

public class AnnotationProcessingLogger {

    protected Messager messager;

    public AnnotationProcessingLogger(Messager messager) {
        this.messager = messager;
    }

    public void debug(String msg) {
        messager.printMessage(Kind.NOTE, msg);
    }

    protected void info(String msg) {
        messager.printMessage(Kind.NOTE, msg);
    }

    protected void info(String msg, Element element) {
        messager.printMessage(Kind.NOTE, msg, element);
    }

    protected void warn(String msg) {
        messager.printMessage(Kind.WARNING, msg);
    }

    protected void warn(String msg, Element element) {
        messager.printMessage(Kind.WARNING, msg, element);
    }

    protected void error(String msg) {
        messager.printMessage(Kind.ERROR, msg);
    }

    protected void error(String msg, Element element) {
        messager.printMessage(Kind.ERROR, msg, element);
    }

}
