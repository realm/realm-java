package com.tightdb.generator;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class CodeRenderer {

    private final Map<String, Template> templates;

    private final Configuration cfg;

    public CodeRenderer() {

        cfg = new Configuration();
        try {
            templates = new HashMap<String, Template>();
            templates.put("table.ftl", new Template("table.ftl", new StringReader(Templates.TABLE), cfg));
            templates.put("table_add.ftl", new Template("table_add.ftl", new StringReader(Templates.TABLE_ADD), cfg));
            templates.put("table_insert.ftl", new Template("insert.ftl", new StringReader(Templates.TABLE_INSERT), cfg));
            templates.put("query.ftl", new Template("query.ftl", new StringReader(Templates.QUERY), cfg));
            templates.put("cursor.ftl", new Template("cursor.ftl", new StringReader(Templates.CURSOR), cfg));
            templates.put("view.ftl", new Template("view.ftl", new StringReader(Templates.VIEW), cfg));

            // // FIXME: temporary for faster development with auto-loading
            // cfg.setDirectoryForTemplateLoading(new
            // File("C:/Users/nikuco/tightdb_java2/src/main/resources/codegen-templates"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        cfg.setObjectWrapper(new DefaultObjectWrapper());
    }

    public String render(String template, Model model) {
        try {
            // Template tmpl = cfg.getTemplate(template);

            Template tmpl = templates.get(template);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream);
            tmpl.process(model, writer);

            return outputStream.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
