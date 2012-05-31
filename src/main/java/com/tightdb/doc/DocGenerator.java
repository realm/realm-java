package com.tightdb.doc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

public class DocGenerator {

	private static List<Method> methods = new ArrayList<Method>();

	private static TemplateRenderer renderer = new TemplateRenderer();

	public static void main(String[] args) throws Exception {
		Velocity.init();
		Context context = new VelocityContext();
		
		describeAndGen(new TableDesc(methods), "Table", context);
		describeAndGen(new ViewDesc(methods), "Row", context);
		describeAndGen(new ViewDesc(methods), "Query", context);
		describeAndGen(new ViewDesc(methods), "View", context);
		describeAndGen(new ViewDesc(methods), "Group", context);
		
		String docs = renderer.render("reference.vm", context);
		// FIXME: hard-coded path (temporary)
		FileUtils.writeStringToFile(new File("D:/docs/reference/reference.html"), docs);
	}

	private static void describeAndGen(AbstractDesc desc, String cls, Context context) throws Exception {
		methods.clear();
		desc.describe(); 
		context.put(cls.toLowerCase() + "_method_overview", generateDocOverview(cls));
		context.put(cls.toLowerCase() + "_method_details", generateExtendedDoc(cls));
	}

	private static String generateDocOverview(String cls) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (Method method : methods) {
			Context context = new VelocityContext();
			context.put("class", cls);
			context.put("ret", method.ret);
			context.put("name", method.name);
			context.put("doc", method.doc);
			context.put("name", method.name);
			context.put("params", method.params);
			sb.append(renderer.render("method.vm", context));
		}
		return sb.toString();
	}

	private static String generateExtendedDoc(String cls) throws Exception {
		StringBuilder sb = new StringBuilder();
		ExampleReader exampleReader = new ExampleReader(cls + "Examples.java");

		for (Method method : methods) {
			Context context = new VelocityContext();
			context.put("class", cls);
			context.put("ret", method.ret);
			context.put("name", method.name);
			context.put("doc", method.doc);
			context.put("name", method.name);
			context.put("params", method.params);
			context.put("example", exampleReader.getExample(method.name));
			sb.append(renderer.render("method-details.vm", context));
		}

		return sb.toString();
	}
}
