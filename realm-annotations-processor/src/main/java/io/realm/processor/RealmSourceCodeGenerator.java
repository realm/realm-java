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
import java.util.List;
import java.util.Locale;

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
        if (typeName.equals("int")) {
            typeName = "Integer";
        } else if (typeName.equals("long") || typeName.equals("float") ||
                typeName.equals("double") || typeName.equals("boolean")) {
            typeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
        }

        return typeName;
    }

    private String convertTypesToColumnType(String typeName) {
        if (typeName.equals("String")) {
            typeName = "ColumnType.STRING";
        } else if (typeName.equals("Long") || typeName.equals("Integer")) {
            typeName = "ColumnType.INTEGER";
        } else if (typeName.equals("Float")) {
            typeName = "ColumnType.FLOAT";
        } else if (typeName.equals("Double")) {
            typeName = "ColumnType.DOUBLE";
        } else if (typeName.equals("Boolean")) {
            typeName = "ColumnType.BOOLEAN";
        } else if (typeName.equals("Date")) {
            typeName = "ColumnType.DATE";
        } else if (typeName.equals("byte[]")) {
            typeName = "ColumnType.BINARY";
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

    private void emitPackage() throws IOException {
        writer.emitPackage(packageName)
                .emitEmptyLine()
                .emitImports(
                        "io.realm.internal.ColumnType",
                        "io.realm.internal.Table",
                        "io.realm.internal.ImplicitTransaction",
                        "io.realm.internal.Row")
                .emitEmptyLine();
    }

    public boolean setClassName(String className) {
        if (!checkState(GeneratorStates.CLASS)) return false;

        this.className = className;

        generatorState = GeneratorStates.METHODS;
        return true;
    }

    private void emitClass() throws IOException {
        writer.beginType(packageName + "." + className + "RealmProxy", "class",
                EnumSet.of(Modifier.PUBLIC, Modifier.FINAL), className).emitEmptyLine();
    }

    public boolean setField(String fieldName, Element fieldElement) {
        if (!checkState(GeneratorStates.METHODS)) return false;

        String shortType = convertSimpleTypesToObject(fieldElement.asType().toString());
        shortType = shortType.substring(shortType.lastIndexOf(".") + 1);

        fields.add(new FieldInfo(fieldName, convertTypesToColumnType(shortType), fieldElement));

        return true;
    }

    public void emitFields() throws IOException {

        int columnIndex = 0;

        for (FieldInfo field : fields) {
            String originalType = field.fieldElement.asType().toString();
            String fullType = convertSimpleTypesToObject(originalType);
            String shortType = fullType.substring(fullType.lastIndexOf(".") + 1);

            String returnCast = "";
            String camelCaseFieldName = Character.toUpperCase(field.fieldName.charAt(0)) + field.fieldName.substring(1);

            if (originalType.equals("int")) {
                fullType = "long";
                shortType = "Long";
                returnCast = "(" + originalType + ")";
            } else if (shortType.equals("Integer")) {
                fullType = "long";
                shortType = "Long";
                returnCast = "(int)";
            } else if (shortType.equals("byte[]")) {
                shortType = "BinaryByteArray";
                returnCast = "(byte[])";
            }

            String getterStmt = "return " + returnCast + "row.get" + shortType + "( " + columnIndex + " )";

            String setterStmt = "row.set" + shortType + "( " + columnIndex + ", value )";

            if (!field.fieldElement.asType().getKind().isPrimitive())
            {
                if (!originalType.equals("java.lang.String") &&
                	!originalType.equals("java.lang.Long") &&
                	!originalType.equals("java.lang.Integer") &&
                	!originalType.equals("java.lang.Float") &&
                	!originalType.equals("java.lang.Double") &&
                	!originalType.equals("java.lang.Boolean") &&
                	!originalType.equals("java.util.Date") &&
                	!originalType.equals("byte[]")) {
                	
                	// We now know this is a type derived from RealmObject - 
                	// this has already been checked in the RealmProcessor
                	setterStmt = String.format("if (value != null) {row.setLink( %d, value.realmGetRow().getIndex() );}", columnIndex);
                	getterStmt = String.format("return realmGetRow().getLink(%d)==-1?null:realm.get(%s.class, realmGetRow().getLink(%d))", columnIndex, fullType, columnIndex);
                    field.columnType = "ColumnType.LINK";
                }
            }
            
            columnIndex++;

            writer.emitAnnotation("Override").beginMethod(originalType, "get" + camelCaseFieldName, EnumSet.of(Modifier.PUBLIC))
                    .emitStatement(getterStmt)
                    .endMethod();

            writer.emitAnnotation("Override").beginMethod("void", "set" + camelCaseFieldName, EnumSet.of(Modifier.PUBLIC),
                    originalType, "value")
                    .emitStatement(setterStmt)
                    .endMethod().emitEmptyLine();
        }
    }


    public boolean generate() throws IOException {

    	// Set source code indent to 4 spaces
        writer.setIndent("    ");

        // Emit java writer code in sections: 
        
        //   1. Package Header and imports
        emitPackage();
        
        //   2. class definition
        emitClass();
        
        //   3. public setters and getters for each field
        emitFields();

        // Generate initTable method, which is used to create the datqbase table

        String tableName = this.className.toLowerCase(Locale.getDefault());

        writer.beginMethod("Table", "initTable", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
                "ImplicitTransaction", "transaction").
                beginControlFlow("if(!transaction.hasTable(\"" + tableName + "\"))").
                emitStatement("Table table = transaction.getTable(\"" + tableName + "\")");

        // For each field generate corresponding table index constant
        for (FieldInfo field : fields) {

            if (field.columnType.equals("ColumnType.LINK")) {
                writer.emitStatement("table.addColumnLink( %s, \"%s\", %s)", field.columnType,
                    field.fieldName.toLowerCase(Locale.getDefault()), "table");
            }
            else {
                writer.emitStatement("table.addColumn( %s, \"%s\" )", field.columnType, field.fieldName.toLowerCase(Locale.getDefault()));
            }
        }

        writer.emitStatement("return table");
        writer.endControlFlow();
        writer.emitStatement("return transaction.getTable(\"" + tableName + "\")");
        writer.endMethod().emitEmptyLine();

        // End the class definition 
        writer.endType();
        writer.close();

        fields.clear();

        generatorState = GeneratorStates.PACKAGE;

        return true;
    }
}
