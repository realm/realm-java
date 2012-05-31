package com.tightdb.doc;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

public class DocGenerator {

	private static List<Method> methods = new ArrayList<Method>();

	public static void main(String[] args) throws Exception {
		Velocity.init();
		describeTable();
		generateDoc();
	}

	private static void describeTable() {
		methods.clear();
		TableDesc tableDesc = new TableDesc(methods);
		tableDesc.describe();
	}

	private static void generateDoc() throws Exception {
		Writer writer = new OutputStreamWriter(System.out);
		for (Method method : methods) {
			InputStream st = DocGenerator.class.getClassLoader().getResourceAsStream("method.vm");
			InputStreamReader reader = new InputStreamReader(st);
			Context context = new VelocityContext();
			context.put("ret", method.ret);
			context.put("name", method.name);
			context.put("doc", method.doc);
			context.put("name", method.name);
			context.put("params", method.params);
			Velocity.evaluate(context, writer, "", reader);
		}
		writer.close();
	}

}
