package com.tightdb.doc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

public class TemplateRenderer implements RuntimeConstants {

	private static final String RESOURCE_LOADER_CLASS = "file.resource.loader.class";

	private final VelocityEngine engine;

	public TemplateRenderer() {
		this.engine = new VelocityEngine();
	}

	public void init(String templatesPath, boolean debugMode) {
		Properties velocityConfig = new Properties();

		if (templatesPath != null) {
			velocityConfig.setProperty("resource.loader", "file, classpath");

			velocityConfig.setProperty(RESOURCE_LOADER_CLASS, FileResourceLoader.class.getCanonicalName());
			velocityConfig.setProperty(FILE_RESOURCE_LOADER_PATH, templatesPath);

			velocityConfig.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getCanonicalName());
			velocityConfig.setProperty("classpath.resource.loader.cache", "false");
		} else {
			velocityConfig.setProperty(RESOURCE_LOADER_CLASS, ClasspathResourceLoader.class.getCanonicalName());
		}

		velocityConfig.setProperty(VM_MAX_DEPTH, "1000");
		velocityConfig.setProperty(VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL, "true");
		velocityConfig.setProperty(VM_PERM_INLINE_LOCAL, "false");
		velocityConfig.setProperty(VM_PERM_ALLOW_INLINE, "true");

		if (debugMode) {
			velocityConfig.setProperty(VM_LIBRARY_AUTORELOAD, "true");
			velocityConfig.setProperty(FILE_RESOURCE_LOADER_CACHE, "false");
		} else {
			velocityConfig.setProperty(VM_LIBRARY_AUTORELOAD, "false");
			velocityConfig.setProperty(FILE_RESOURCE_LOADER_CACHE, "true");
		}

		engine.setProperty(RUNTIME_LOG_LOGSYSTEM, this);
		engine.init(velocityConfig);
	}

	public void render(String templateName, Context context, Writer writer) throws IOException {
		InputStream st = getClass().getClassLoader().getResourceAsStream(templateName);
		InputStreamReader reader = new InputStreamReader(st);
		engine.evaluate(context, writer, "", reader);
		writer.flush();
	}

	public String render(String templateName, Context context) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(outputStream);
		render(templateName, context, writer);
		return outputStream.toString();
	}

	public String renderFromString(String template, Context context) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(outputStream);
		engine.evaluate(context, writer, "", template);
		return outputStream.toString();
	}

}
