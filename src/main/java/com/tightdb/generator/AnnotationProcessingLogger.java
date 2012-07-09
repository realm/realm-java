package com.tightdb.generator;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

public class AnnotationProcessingLogger {

	protected Messager messager;

	public AnnotationProcessingLogger(Messager messager) {
		this.messager = messager;
	}

	protected void info(String msg) {
		messager.printMessage(Kind.NOTE, msg);
	}

	protected void warn(String msg) {
		messager.printMessage(Kind.WARNING, msg);
	}

	protected void error(String msg) {
		messager.printMessage(Kind.ERROR, msg);
	}

}
