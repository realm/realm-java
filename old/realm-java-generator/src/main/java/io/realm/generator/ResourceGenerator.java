/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.generator;

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

        lines.add("package io.realm.generator;");
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

        File output = new File("src/main/java/io/realm/generator/Templates.java");
        FileUtils.writeLines(output, lines);
    }
}
