package com.tightdb.doc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

public class DocGenerator {

    private static List<Constructor> constructors = new ArrayList<Constructor>();
    private static List<Method> methods = new ArrayList<Method>();

    private static TemplateRenderer renderer = new TemplateRenderer();

    public static void main(String[] args) throws Exception {
        Velocity.init();
        Context context = new VelocityContext();

        describeAndGen(new TableDesc(constructors, methods), "Table", context);
        describeAndGen(new RowDesc(constructors, methods), "Row", context);
        describeAndGen(new QueryDesc(constructors, methods), "Query", context);
        describeAndGen(new ViewDesc(constructors, methods), "View", context);
        describeAndGen(new GroupDesc(constructors, methods), "Group", context);

        // String docs = renderer.render("reference.vm", context);
        // writeFile("reference", docs);
        System.out.println("Documentation updated.");
    }

    private static void describeAndGen(AbstractDesc desc, String cls,
            Context context) throws Exception {
        constructors.clear();
        methods.clear();
        desc.describe();

        context.put("class", cls);
        context.put("method_list", generateMethodList(cls));
        context.put("constructor_overview", generateConstructorOverview(cls));
        context.put("method_overview", generateMethodOverview(cls));
        context.put("constructor_details", generateConstructorsDetails(cls));
        context.put("method_details", generateMethodsDetails(cls));

        if (cls.equalsIgnoreCase("table") || cls.equalsIgnoreCase("view")) {
            context.put("columns", generateTableOrViewColumns());
        } else if (cls.equalsIgnoreCase("query")) {
            context.put("columns", generateQueryColumns());
        }

        String doc = renderer.render(cls.toLowerCase() + "-ref.vm", context);
        writeFile("method-ref/" + cls.toLowerCase(), doc);
    }

    private static String generateMethodList(String cls) throws Exception {
        StringBuilder sb = new StringBuilder();

        for (Constructor constructor : constructors) {
            Context context = new VelocityContext();
            context.put("id", constructor.getId());
            context.put("name", "constructor");
            sb.append(renderer.render("method-list.vm", context));
        }

        for (Method method : methods) {
            Context context = new VelocityContext();
            context.put("id", method.getId());
            context.put("name", method.name);
            sb.append(renderer.render("method-list.vm", context));
        }

        return sb.toString();
    }

    private static String generateConstructorOverview(String cls)
            throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Constructor constructor : constructors) {
            Context context = new VelocityContext();
            context.put("class", cls);
            context.put("id", constructor.getId());
            context.put("order", constructor.order);
            context.put("name", constructor.getName());
            context.put("doc", constructor.doc);
            context.put("params", constructor.params);
            sb.append(renderer.render("constructor-overview.vm", context));
        }
        return sb.toString();
    }

    private static String generateMethodOverview(String cls) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (Method method : methods) {
            Context context = new VelocityContext();
            context.put("class", cls);
            context.put("ret", method.ret);
            context.put("name", method.name);
            context.put("doc", method.doc);
            context.put("params", method.params);
            sb.append(renderer.render("method-overview.vm", context));
        }
        return sb.toString();
    }

    private static String generateConstructorsDetails(String cls)
            throws Exception {
        StringBuilder sb = new StringBuilder();
        ExampleReader exampleReader = new ExampleReader(cls + "Examples.java");

        for (Constructor constructor : constructors) {
            String doc = generateConstructorDetails(cls, exampleReader,
                    constructor);
            writeFile("method-ref/" + constructor.getId(), doc);
            sb.append(doc);
        }

        return sb.toString();
    }

    private static String generateConstructorDetails(String cls,
            ExampleReader exampleReader, Constructor constructor)
            throws IOException {
        Context context = new VelocityContext();
        context.put("class", cls);
        context.put("id", constructor.getId());
        context.put("order", constructor.order);
        context.put("name", constructor.getName());
        context.put("doc", constructor.doc);
        context.put("params", constructor.params);
        context.put("example",
                exampleReader.getExample("constructor-" + constructor.order));
        String doc = renderer.render("constructor-ref.vm", context);
        return doc;
    }

    private static String generateMethodsDetails(String cls) throws Exception {
        StringBuilder sb = new StringBuilder();
        ExampleReader exampleReader = new ExampleReader(cls + "Examples.java");

        for (Method method : methods) {
            String doc = generateMethodDetails(cls, exampleReader, method);
            writeFile("method-ref/" + method.getId(), doc);
            sb.append(doc);
        }

        return sb.toString();
    }

    private static String generateMethodDetails(String cls,
            ExampleReader exampleReader, Method method) throws IOException {
        Context context = new VelocityContext();
        context.put("class", cls);
        context.put("ret", method.ret);
        context.put("name", method.name);
        context.put("doc", method.doc);
        context.put("params", method.params);
        context.put("example", exampleReader.getExample(method.name));
        return renderer.render("method-ref.vm", context);
    }

    private static String generateTableOrViewColumns() throws Exception {
        StringBuilder sb = new StringBuilder();

        Context context = new VelocityContext();
        context.put("class", "table");
        sb.append(renderer.render("columns.vm", context));

        return sb.toString();
    }

    private static String generateQueryColumns() throws Exception {
        StringBuilder sb = new StringBuilder();

        Context context = new VelocityContext();
        context.put("class", "query");
        sb.append(renderer.render("columns.vm", context));

        return sb.toString();
    }

    private static void writeFile(String fileName, String content)
            throws Exception {
        FileUtils.writeStringToFile(new File("doc/reference/" + fileName
                + ".html"), content);
    }

}
