package com.tightdb.generator;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class ResourceGenerator {

    public static void main(String[] args) throws IOException {
        generate("table", "table_add", "table_insert", "cursor", "view", "query");
    }

    private static void generate(String... names) throws IOException {
        String frm = "    public static final String %s = \"%s\";";
        List<String> lines = new LinkedList<String>();

        lines.add("package com.tightdb.generator;");
        lines.add("");
        lines.add("/* This class is automatically generated from the .ftl templates */");
        lines.add("public class Templates {");

        for (String name : names) {
            File file = new File("src/main/resources/codegen-templates/" + name + ".ftl");
            if (file.exists()) {
                String content = FileUtils.readFileToString(file);
                content = StringUtils.escapeJava(content);
                content = content.replaceAll("\\\\/", "/");
                String line = String.format(frm, name.toUpperCase(), content);
                lines.add(line);
            } else {
                System.err.println("No such file: " + file);
            }
        }

        lines.add("}");

        File output = new File("src/main/java/com/tightdb/generator/Templates.java");
        FileUtils.writeLines(output, lines);
    }
}
