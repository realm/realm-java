package com.tightdb.generator;

import java.io.ByteArrayOutputStream;
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

public class CodeRenderer {

	public String test() throws Exception {
		Configuration cfg = new Configuration();
		TemplateLoader loader = new StringTemplateLoader();

		// /cfg.setDirectoryForTemplateLoading(new
		// File("C:/Users/nikuco/tightdb_java2/src/main/resources"));
		cfg.setTemplateLoader(loader);
		cfg.setObjectWrapper(new DefaultObjectWrapper());

		Map root = new HashMap();
		root.put("foo", "ABC");

		// Template tmpl = cfg.getTemplate("test.ftl");
		Template tmpl = new Template("name", new StringReader("Test ${user}"), new Configuration());

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(outputStream);
		tmpl.process(root, writer);

		return outputStream.toString();
	}

}
