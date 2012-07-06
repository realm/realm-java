package com.tightdb.generator;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;

import org.apache.commons.lang.StringUtils;

public abstract class AbstractAnnotationProcessor extends AbstractProcessor {

	private static final String[] SUPPORTED_ANNOTATIONS = { "com.tightdb.lib.Table" };

	protected Messager messager;
	protected Elements elementUtils;
	protected Types typeUtils;
	protected Filer filer;
	protected Map<String, String> options;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		info("Entering annotation processor...");
		if (!env.processingOver()) {
			info("Processing resources...");
			try {
				processAnnotations(annotations, env);
				info("Successfully finished processing.");
			} catch (Exception e) {
				String info = e.getMessage() != null ? e.getMessage() : "";
				String msg = "ERROR: " + info + "\n\n" + StringUtils.join(e.getStackTrace(), "\n");

				Throwable cause = e.getCause();
				while (cause != null) {
					info = cause.getMessage() != null ? cause.getMessage() : "";
					msg += "\n\nCause: " + info + "\n" + StringUtils.join(cause.getStackTrace(), "\n");
					cause = cause.getCause();
				}

				error(msg);
			}
		} else {
			info("Last round, processing is done.");
		}
		return true;
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

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);

		messager = env.getMessager(); // required for logging
		info("Initializing annotation processor...");

		elementUtils = env.getElementUtils();
		typeUtils = env.getTypeUtils();
		filer = env.getFiler();
		options = env.getOptions();

		info("Initialization finished.");
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		info("Specifying supported annotations...");
		return new HashSet<String>(Arrays.asList(SUPPORTED_ANNOTATIONS));
	}

	protected abstract void processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment env) throws Exception;

	protected void writeToFile(String pkg, String filename, String content) {
		final Location location = StandardLocation.SOURCE_OUTPUT;

		Writer writer = null;
		try {
			FileObject fileRes = filer.createResource(location, pkg, filename);
			writer = fileRes.openWriter();
			writer.write(content);
		} catch (IOException e) {
			error("Couldn't write to file: " + filename);
			throw new RuntimeException("Couldn't write to file: " + filename, e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					error("Couldn't write to file: " + filename);
					throw new RuntimeException("Couldn't write to file: " + filename, e);
				}
			}
		}
	}

}
