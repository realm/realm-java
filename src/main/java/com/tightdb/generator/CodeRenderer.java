package com.tightdb.generator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class CodeRenderer {

	private final Configuration cfg;

	public CodeRenderer() {
		cfg = new Configuration();

		try {
			// cfg.setTemplateLoader(loader);
			// TemplateLoader loader = new StringTemplateLoader();
			
			// FIXME: temporary for faster development with auto-loading
			cfg.setDirectoryForTemplateLoading(new File("C:/Users/nikuco/tightdb_java2/src/main/resources/codegen-templates"));
			
			cfg.setObjectWrapper(new DefaultObjectWrapper());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String render(String template, Model model) {
		try {
			Template tmpl = cfg.getTemplate(template);
			// Template tmpl = new Template("name", new
			// StringReader("Test ${user}"), new Configuration());

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Writer writer = new OutputStreamWriter(outputStream);
			tmpl.process(model, writer);

			return outputStream.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
