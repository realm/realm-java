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
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;

import org.apache.commons.lang.StringUtils;

public abstract class AbstractAnnotationProcessor extends AbstractProcessor {

	private static final String[] SUPPORTED_ANNOTATIONS = { "com.tightdb.lib.Table" };

	protected Elements elementUtils;
	protected Types typeUtils;
	protected Filer filer;
	protected Map<String, String> options;
	protected AnnotationProcessingLogger logger;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		logger.info("Entering annotation processor...");
		if (!env.processingOver()) {
			logger.info("Processing resources...");
			try {
				processAnnotations(annotations, env);
				logger.info("Successfully finished processing.");
			} catch (Exception e) {
				String info = e.getMessage() != null ? "(" + e.getMessage() + ")" : "";
				String msg = e.getClass().getCanonicalName() + " " + info + "\n\n" + StringUtils.join(e.getStackTrace(), "\n");

				Throwable cause = e.getCause();
				while (cause != null) {
					info = cause.getMessage() != null ? cause.getMessage() : "";
					msg += "\n\nCause: " + info + "\n" + StringUtils.join(cause.getStackTrace(), "\n");
					cause = cause.getCause();
				}

				logger.error(msg);
			}
		} else {
			logger.info("Last round, processing is done.");
		}
		return true;
	}

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);

		Messager messager = env.getMessager(); // required for logging
		logger = new AnnotationProcessingLogger(messager);
		logger.info("Initializing annotation processor...");

		elementUtils = env.getElementUtils();
		typeUtils = env.getTypeUtils();
		filer = env.getFiler();
		options = env.getOptions();

		logger.info("Initialization finished.");
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		logger.info("Specifying supported annotations...");
		return new HashSet<String>(Arrays.asList(SUPPORTED_ANNOTATIONS));
	}

	protected abstract void processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment env) throws Exception;

	protected void writeToSourceFile(String pkg, String filename, String content, Element... originatingElements) {
		Writer writer = null;
		try {
			String name = pkg + "/" + filename;
			logger.info("Writing source file: " + name);
			FileObject fileRes = filer.createSourceFile(name, originatingElements);
			writer = fileRes.openWriter();
			writer.write(content);
		} catch (IOException e) {
			logger.error("Couldn't write to file: " + filename);
			throw new RuntimeException("Couldn't write to file: " + filename, e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("Couldn't write to file: " + filename);
					throw new RuntimeException("Couldn't write to file: " + filename, e);
				}
			}
		}
	}

}
