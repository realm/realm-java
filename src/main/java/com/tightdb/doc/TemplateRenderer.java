package com.tightdb.doc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

public class TemplateRenderer {

	public void render(String template, Context context, Writer writer) throws IOException {
		InputStream st = getClass().getClassLoader().getResourceAsStream(template);
		InputStreamReader reader = new InputStreamReader(st);
		Velocity.evaluate(context, writer, "", reader);
		writer.flush();
	}

	public String render(String template, Context context) throws IOException {
		ByteArrayOutputStream dd = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(dd);
		render(template, context, writer);
		return dd.toString();
	}

}
