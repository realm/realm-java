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

package io.realm.processor;

import com.squareup.javawriter.JavaWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

public class RealmSourceCodeGenerator {

    private class FieldInfo {
        public String fieldName;
        public String columnType;
        public Element fieldElement;

        public FieldInfo(String fieldName, String columnType, Element fieldElement) {
            this.columnType = columnType;
            this.fieldElement = fieldElement;
            this.fieldName = fieldName;
        }
    }

    private enum GeneratorStates {
        PACKAGE,
        CLASS,
        METHODS,
    }

    private JavaWriter writer = null;
    private String className = null;
    private String packageName = null;
    private GeneratorStates generatorState = GeneratorStates.PACKAGE;
    private String errorMessage = "";
    private List<FieldInfo> fields = new ArrayList<FieldInfo>();

    private void setError(String message) {
        errorMessage = message;
    }

    public String getError() {
        return errorMessage;
    }

    private String convertSimpleTypesToObject(String typeName) {
        if (typeName.compareTo("int") == 0) {
            typeName = "Integer";
        } else if (typeName.compareTo("long") == 0 || typeName.compareTo("float") == 0 ||
                typeName.compareTo("double") == 0 || typeName.compareTo("boolean") == 0) {
            typeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
        }

        return typeName;
    }

    private String convertTypesToColumnType(String typeName) {
        if (typeName.compareTo("String") == 0) {
            typeName = "ColumnType.STRING";
        } else if (typeName.compareTo("Long") == 0 || typeName.compareTo("Integer") == 0) {
            typeName = "ColumnType.INTEGER";
        } else if (typeName.compareTo("Float") == 0) {
            typeName = "ColumnType.FLOAT";
        } else if (typeName.compareTo("Double") == 0) {
            typeName = "ColumnType.DOUBLE";
        } else if (typeName.compareTo("Boolean") == 0) {
            typeName = "ColumnType.BOOLEAN";
        } else if (typeName.compareTo("Date") == 0) {
            typeName = "ColumnType.DATE";
        }

        return typeName;
    }

    private boolean checkState(GeneratorStates checkState) {
        if (writer == null) {
            setError("No output writer has been defined");
            return false;
        }

        if (generatorState != checkState) {
            setError("Annotations received in wrong order");
            return false;
        }

        return true;
    }

    public boolean setBufferedWriter(BufferedWriter bw) {
        writer = new JavaWriter(bw);

        return true;
    }

    public boolean setPackageName(String packageName) {
        if (!checkState(GeneratorStates.PACKAGE)) return false;

        this.packageName = packageName;

        generatorState = GeneratorStates.CLASS;
        return true;
    }

    private boolean emitPackage() throws IOException {
        writer.emitPackage(packageName)
                .emitEmptyLine()
                .emitImports(
                        "io.realm.internal.ColumnType",
                        "io.realm.internal.Table",
                        "io.realm.internal.ImplicitTransaction")
                .emitEmptyLine();

        return true;
    }

    public boolean setClassName(String className) {
        if (!checkState(GeneratorStates.CLASS)) return false;

        this.className = className;

        generatorState = GeneratorStates.METHODS;
        return true;
    }

    private boolean emitClass() throws IOException {
        writer.beginType(packageName + "." + className + "RealmProxy", "class",
                EnumSet.of(Modifier.PUBLIC, Modifier.FINAL), className).emitEmptyLine();

        return true;
    }

    public boolean setField(String fieldName, Element fieldElement) {
        if (!checkState(GeneratorStates.METHODS)) return false;

        String shortType = convertSimpleTypesToObject(fieldElement.asType().toString());
        shortType = shortType.substring(shortType.lastIndexOf(".") + 1);

        fields.add(new FieldInfo(fieldName, convertTypesToColumnType(shortType), fieldElement));

        return true;
    }

    public boolean emitFields() throws IOException {

    	int columnIndex = 0;
    	
        for (FieldInfo field : fields) {
            String originalType = field.fieldElement.asType().toString();
            String fullType = convertSimpleTypesToObject(originalType);
            String shortType = fullType.substring(fullType.lastIndexOf(".") + 1);

            String returnCast = "";
            String camelCaseFieldName = Character.toUpperCase(field.fieldName.charAt(0)) + field.fieldName.substring(1);

            if (originalType.compareTo("int") == 0) {
                fullType = "long";
                shortType = "Long";
                returnCast = "(" + originalType + ")";
            }

            if (shortType.compareTo("Integer") == 0) {
                fullType = "long";
                shortType = "Long";
                returnCast = "(int)";
            }

            String getterStmt = "return " + returnCast + "row.get" + shortType + "( " + columnIndex + " )";

            String setterStmt = "row.set" + shortType + "( " + columnIndex + ", value )";

            columnIndex++;

            writer.emitAnnotation("Override").beginMethod(originalType, "get" + camelCaseFieldName, EnumSet.of(Modifier.PUBLIC))
                    .emitStatement(getterStmt)
                    .endMethod();

            writer.emitAnnotation("Override").beginMethod("void", "set" + camelCaseFieldName, EnumSet.of(Modifier.PUBLIC),
                    originalType, "value")
                    .emitStatement(setterStmt)
                    .endMethod().emitEmptyLine();
        }

        return true;
    }

    public boolean generate() throws IOException {

        writer.setIndent("    ");

        if (!emitPackage()) return false;
        if (!emitClass()) return false;
        if (!emitFields()) return false;

        writer.beginMethod("Table", "initTable", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
                "ImplicitTransaction", "transaction").
                beginControlFlow("if(!transaction.hasTable(\"" + this.className + "\"))").
                emitStatement("Table table = transaction.getTable(\"" + this.className + "\")");

        for (int index = 0; index < fields.size(); ++index) {
            FieldInfo field = fields.get(index);
            writer.emitStatement("table.addColumn( %s, \"%s\" )", field.columnType, field.fieldName.toLowerCase(Locale.getDefault()));
        }

        writer.emitStatement("return table");
        writer.endControlFlow();
        writer.emitStatement("return transaction.getTable(\"" + this.className + "\")");
        writer.endMethod().emitEmptyLine();

        writer.endType();
        writer.close();

        fields.clear();

        generatorState = GeneratorStates.PACKAGE;

        return true;
    }
}
