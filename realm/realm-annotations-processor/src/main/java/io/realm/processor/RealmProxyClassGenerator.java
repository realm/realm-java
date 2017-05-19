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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;


public class RealmProxyClassGenerator {
    private static final String BACKLINKS_FIELD_EXTENSION = "Backlinks";

    private final ProcessingEnvironment processingEnvironment;
    private final ClassMetaData metadata;
    private final String simpleClassName;
    private final String qualifiedClassName;
    private final String interfaceName;
    private final String qualifiedGeneratedClassName;

    public RealmProxyClassGenerator(ProcessingEnvironment processingEnvironment, ClassMetaData metadata) {
        this.processingEnvironment = processingEnvironment;
        this.metadata = metadata;
        this.simpleClassName = metadata.getSimpleClassName();
        this.qualifiedClassName = metadata.getFullyQualifiedClassName();
        this.interfaceName = Utils.getProxyInterfaceName(simpleClassName);
        this.qualifiedGeneratedClassName = String.format("%s.%s",
                Constants.REALM_PACKAGE_NAME, Utils.getProxyClassName(simpleClassName));
    }

    public void generate() throws IOException, UnsupportedOperationException {
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));

        // Set source code indent
        writer.setIndent(Constants.INDENT);

        writer.emitPackage(Constants.REALM_PACKAGE_NAME)
                .emitEmptyLine();

        ArrayList<String> imports = new ArrayList<String>();
        imports.add("android.annotation.TargetApi");
        imports.add("android.os.Build");
        imports.add("android.util.JsonReader");
        imports.add("android.util.JsonToken");
        imports.add("io.realm.exceptions.RealmMigrationNeededException");
        imports.add("io.realm.internal.ColumnInfo");
        imports.add("io.realm.internal.RealmObjectProxy");
        imports.add("io.realm.internal.Row");
        imports.add("io.realm.internal.Table");
        imports.add("io.realm.internal.OsObject");
        imports.add("io.realm.internal.OsObjectSchemaInfo");
        imports.add("io.realm.internal.Property");
        imports.add("io.realm.internal.SharedRealm");
        if (!metadata.getBacklinkFields().isEmpty()) {
            imports.add("io.realm.internal.UncheckedRow");
        }
        imports.add("io.realm.internal.LinkView");
        imports.add("io.realm.internal.android.JsonUtils");
        imports.add("io.realm.log.RealmLog");
        imports.add("java.io.IOException");
        imports.add("java.util.ArrayList");
        imports.add("java.util.Collections");
        imports.add("java.util.List");
        imports.add("java.util.Iterator");
        imports.add("java.util.Date");
        imports.add("java.util.Map");
        imports.add("java.util.HashMap");
        imports.add("org.json.JSONObject");
        imports.add("org.json.JSONException");
        imports.add("org.json.JSONArray");

        writer.emitImports(imports)
                .emitEmptyLine();

        // Begin the class definition
        writer.beginType(
                qualifiedGeneratedClassName, // full qualified name of the item to generate
                "class",                     // the type of the item
                EnumSet.of(Modifier.PUBLIC), // modifiers to apply
                qualifiedClassName,          // class to extend
                "RealmObjectProxy",          // interfaces to implement
                interfaceName)
                .emitEmptyLine();

        emitColumnInfoClass(writer);

        emitClassFields(writer);
        emitConstructor(writer);

        emitInjectContextMethod(writer);
        emitPersistedFieldAccessors(writer);
        emitBacklinkFieldAccessors(writer);
        emitCreateExpectedObjectSchemaInfo(writer);
        emitGetExpectedObjectSchemaInfo(writer);
        emitValidateTableMethod(writer);
        emitGetTableNameMethod(writer);
        emitGetFieldNamesMethod(writer);
        emitCreateOrUpdateUsingJsonObject(writer);
        emitCreateUsingJsonStream(writer);
        emitCopyOrUpdateMethod(writer);
        emitCopyMethod(writer);
        emitInsertMethod(writer);
        emitInsertListMethod(writer);
        emitInsertOrUpdateMethod(writer);
        emitInsertOrUpdateListMethod(writer);
        emitCreateDetachedCopyMethod(writer);
        emitUpdateMethod(writer);
        emitToStringMethod(writer);
        emitRealmObjectProxyImplementation(writer);
        emitHashcodeMethod(writer);
        emitEqualsMethod(writer);

        // End the class definition
        writer.endType();
        writer.close();
    }

    private void emitColumnInfoClass(JavaWriter writer) throws IOException {
        writer.beginType(
                columnInfoClassName(),                       // full qualified name of the item to generate
                "class",                                     // the type of the item
                EnumSet.of(Modifier.STATIC, Modifier.FINAL), // modifiers to apply
                "ColumnInfo");                               // base class

        // fields
        for (VariableElement variableElement : metadata.getFields()) {
            writer.emitField("long", columnIndexVarName(variableElement));
        }
        writer.emitEmptyLine();

        // constructor #1
        writer.beginConstructor(
                EnumSet.noneOf(Modifier.class),
                "SharedRealm", "realm", "Table", "table");
        writer.emitStatement("super(%s)", metadata.getFields().size());
        for (VariableElement field : metadata.getFields()) {
            writer.emitStatement(
                    "this.%1$sIndex = addColumnDetails(table, \"%1$s\", %2$s)",
                    field.getSimpleName().toString(), getRealmTypeChecked(field).getRealmType());
        }
        for (Backlink backlink : metadata.getBacklinkFields()) {
            writer.emitStatement(
                    "addBacklinkDetails(realm, \"%1$s\", \"%2$s\", \"%3$s\")",
                    backlink.getTargetField(), Utils.stripPackage(backlink.getSourceClass()), backlink.getSourceField());
        }
        writer.endConstructor()
                .emitEmptyLine();

        // constructor #2
        writer.beginConstructor(
                EnumSet.noneOf(Modifier.class),
                "ColumnInfo", "src", "boolean", "mutable");
        writer.emitStatement("super(src, mutable)")
                .emitStatement("copy(src, this)");
        writer.endConstructor()
                .emitEmptyLine();

        // no-args copy method
        writer.emitAnnotation("Override")
                .beginMethod(
                        "ColumnInfo",                                   // return type
                        "copy",                                         // method name
                        EnumSet.of(Modifier.PROTECTED, Modifier.FINAL), // modifiers
                        "boolean", "mutable");     // parameters
        writer.emitStatement("return new %s(this, mutable)", columnInfoClassName());
        writer.endMethod()
                .emitEmptyLine();

        // copy method
        writer.emitAnnotation("Override")
                .beginMethod(
                        "void",                                          // return type
                        "copy",                                          // method name
                        EnumSet.of(Modifier.PROTECTED, Modifier.FINAL),  // modifiers
                        "ColumnInfo", "rawSrc", "ColumnInfo", "rawDst"); // parameters
        writer.emitStatement("final %1$s src = (%1$s) rawSrc", columnInfoClassName());
        writer.emitStatement("final %1$s dst = (%1$s) rawDst", columnInfoClassName());
        for (VariableElement variableElement : metadata.getFields()) {
            writer.emitStatement("dst.%1$s = src.%1$s", columnIndexVarName(variableElement));
        }
        writer.endMethod();

        writer.endType()
                .emitEmptyLine();
    }

    private void emitClassFields(JavaWriter writer) throws IOException {
        writer.emitField(columnInfoClassName(), "columnInfo", EnumSet.of(Modifier.PRIVATE))
                .emitField("ProxyState<" + qualifiedClassName + ">", "proxyState", EnumSet.of(Modifier.PRIVATE))
                .emitField("OsObjectSchemaInfo", "expectedObjectSchemaInfo",
                        EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
                        "createExpectedObjectSchemaInfo()");


        for (VariableElement variableElement : metadata.getFields()) {
            if (Utils.isRealmList(variableElement)) {
                String genericType = Utils.getGenericTypeQualifiedName(variableElement);
                writer.emitField("RealmList<" + genericType + ">", variableElement.getSimpleName().toString() + "RealmList", EnumSet.of(Modifier.PRIVATE));
            }
        }

        for (Backlink backlink : metadata.getBacklinkFields()) {
            writer.emitField(
                    backlink.getTargetFieldType(),
                    backlink.getTargetField() + BACKLINKS_FIELD_EXTENSION,
                    EnumSet.of(Modifier.PRIVATE));
        }

        //@formatter:off
        writer.emitField("List<String>", "FIELD_NAMES", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL))
                .beginInitializer(true)
                .emitStatement("List<String> fieldNames = new ArrayList<String>()");
        for (VariableElement field : metadata.getFields()) {
            writer.emitStatement("fieldNames.add(\"%s\")", field.getSimpleName().toString());
        }
        writer.emitStatement("FIELD_NAMES = Collections.unmodifiableList(fieldNames)")
                .endInitializer()
                .emitEmptyLine();
        //@formatter:on
    }

    //@formatter:off
    private void emitConstructor(JavaWriter writer) throws IOException {
        // FooRealmProxy(ColumnInfo)
        writer.beginConstructor(EnumSet.noneOf(Modifier.class))
                .emitStatement("proxyState.setConstructionFinished()")
                .endConstructor()
                .emitEmptyLine();
    }
    //@formatter:on

    private void emitPersistedFieldAccessors(final JavaWriter writer) throws IOException {
        for (final VariableElement field : metadata.getFields()) {
            final String fieldName = field.getSimpleName().toString();
            final String fieldTypeCanonicalName = field.asType().toString();

            if (Constants.JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                emitPrimitiveType(writer, field, fieldName, fieldTypeCanonicalName);
            } else if (Utils.isRealmModel(field)) {
                emitRealmModel(writer, field, fieldName, fieldTypeCanonicalName);
            } else if (Utils.isRealmList(field)) {
                emitRealmList(writer, field, fieldName, fieldTypeCanonicalName);
            } else {
                throw new UnsupportedOperationException(String.format(
                        "Field \"%s\" of type \"%s\" is not supported.", fieldName, fieldTypeCanonicalName));
            }

            writer.emitEmptyLine();
        }
    }

    /**
     * Primitives and boxed types
     */
    private void emitPrimitiveType(
            JavaWriter writer,
            final VariableElement field,
            final String fieldName,
            String fieldTypeCanonicalName) throws IOException {

        final String fieldJavaType = getRealmTypeChecked(field).getJavaType();

        // Getter
        //@formatter:off
        writer.emitAnnotation("Override");
        writer.emitAnnotation("SuppressWarnings", "\"cast\"")
                .beginMethod(fieldTypeCanonicalName, metadata.getInternalGetter(fieldName), EnumSet.of(Modifier.PUBLIC))
                .emitStatement("proxyState.getRealm$realm().checkIfValid()");

        // For String and bytes[], null value will be returned by JNI code. Try to save one JNI call here.
        if (metadata.isNullable(field) && !Utils.isString(field) && !Utils.isByteArray(field)) {
            writer.beginControlFlow("if (proxyState.getRow$realm().isNull(%s))", fieldIndexVariableReference(field))
                    .emitStatement("return null")
                    .endControlFlow();
        }
        //@formatter:on

        // For Boxed types, this should be the corresponding primitive types. Others remain the same.
        String castingBackType;
        if (Utils.isBoxedType(fieldTypeCanonicalName)) {
            Types typeUtils = processingEnvironment.getTypeUtils();
            castingBackType = typeUtils.unboxedType(field.asType()).toString();
        } else {
            castingBackType = fieldTypeCanonicalName;
        }
        writer.emitStatement(
                "return (%s) proxyState.getRow$realm().get%s(%s)",
                castingBackType, fieldJavaType, fieldIndexVariableReference(field));
        writer.endMethod()
                .emitEmptyLine();

        // Setter
        writer.emitAnnotation("Override");
        writer.beginMethod("void", metadata.getInternalSetter(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
        emitCodeForUnderConstruction(writer, metadata.isPrimaryKey(field), new CodeEmitter() {
            @Override
            public void emit(JavaWriter writer) throws IOException {
                // set value as default value
                writer.emitStatement("final Row row = proxyState.getRow$realm()");

                //@formatter:off
                if (metadata.isNullable(field)) {
                    writer.beginControlFlow("if (value == null)")
                            .emitStatement("row.getTable().setNull(%s, row.getIndex(), true)",
                                    fieldIndexVariableReference(field))
                            .emitStatement("return")
                            .endControlFlow();
                } else if (!metadata.isNullable(field) && !Utils.isPrimitiveType(field)) {
                    writer.beginControlFlow("if (value == null)")
                            .emitStatement(Constants.STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE, fieldName)
                            .endControlFlow();
                }
                //@formatter:on

                writer.emitStatement(
                        "row.getTable().set%s(%s, row.getIndex(), value, true)",
                        fieldJavaType, fieldIndexVariableReference(field));
                writer.emitStatement("return");
            }
        });
        writer.emitStatement("proxyState.getRealm$realm().checkIfValid()");
        // Although setting null value for String and bytes[] can be handled by the JNI code, we still generate the same code here.
        // Compared with getter, null value won't trigger more native calls in setter which is relatively cheaper.
        if (metadata.isPrimaryKey(field)) {
            // Primary key is not allowed to be changed after object created.
            writer.emitStatement(Constants.STATEMENT_EXCEPTION_PRIMARY_KEY_CANNOT_BE_CHANGED, fieldName);
        } else {
            //@formatter:off
            if (metadata.isNullable(field)) {
                writer.beginControlFlow("if (value == null)")
                        .emitStatement("proxyState.getRow$realm().setNull(%s)", fieldIndexVariableReference(field))
                        .emitStatement("return")
                        .endControlFlow();
            } else if (!metadata.isNullable(field) && !Utils.isPrimitiveType(field)) {
                // Same reason, throw IAE earlier.
                writer
                        .beginControlFlow("if (value == null)")
                        .emitStatement(Constants.STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE, fieldName)
                        .endControlFlow();
            }
            //@formatter:on
            writer.emitStatement(
                    "proxyState.getRow$realm().set%s(%s, value)",
                    fieldJavaType, fieldIndexVariableReference(field));
        }
        writer.endMethod();
    }

    /**
     * Links
     */
    //@formatter:off
    private void emitRealmModel(
            JavaWriter writer,
            final VariableElement field,
            String fieldName,
            String fieldTypeCanonicalName) throws IOException {

        // Getter
        writer.emitAnnotation("Override");
        writer.beginMethod(fieldTypeCanonicalName, metadata.getInternalGetter(fieldName), EnumSet.of(Modifier.PUBLIC))
                .emitStatement("proxyState.getRealm$realm().checkIfValid()")
                .beginControlFlow("if (proxyState.getRow$realm().isNullLink(%s))", fieldIndexVariableReference(field))
                .emitStatement("return null")
                .endControlFlow()
                .emitStatement("return proxyState.getRealm$realm().get(%s.class, proxyState.getRow$realm().getLink(%s), false, Collections.<String>emptyList())",
                        fieldTypeCanonicalName, fieldIndexVariableReference(field))
                .endMethod()
                .emitEmptyLine();

        // Setter
        writer.emitAnnotation("Override");
        writer.beginMethod("void", metadata.getInternalSetter(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
        emitCodeForUnderConstruction(writer, metadata.isPrimaryKey(field), new CodeEmitter() {
            @Override
            public void emit(JavaWriter writer) throws IOException {
                // check excludeFields
                writer.beginControlFlow("if (proxyState.getExcludeFields$realm().contains(\"%1$s\"))",
                        field.getSimpleName().toString())
                        .emitStatement("return")
                        .endControlFlow();
                writer.beginControlFlow("if (value != null && !RealmObject.isManaged(value))")
                        .emitStatement("value = ((Realm) proxyState.getRealm$realm()).copyToRealm(value)")
                        .endControlFlow();

                // set value as default value
                writer.emitStatement("final Row row = proxyState.getRow$realm()");
                writer.beginControlFlow("if (value == null)")
                        .emitSingleLineComment("Table#nullifyLink() does not support default value. Just using Row.")
                        .emitStatement("row.nullifyLink(%s)", fieldIndexVariableReference(field))
                        .emitStatement("return")
                        .endControlFlow();
                writer.beginControlFlow("if (!RealmObject.isValid(value))")
                        .emitStatement("throw new IllegalArgumentException(\"'value' is not a valid managed object.\")")
                        .endControlFlow();
                writer.beginControlFlow("if (((RealmObjectProxy) value).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm())")
                        .emitStatement("throw new IllegalArgumentException(\"'value' belongs to a different Realm.\")")
                        .endControlFlow();
                writer.emitStatement("row.getTable().setLink(%s, row.getIndex(), ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getIndex(), true)",
                        fieldIndexVariableReference(field));
                writer.emitStatement("return");
            }
        });
        writer.emitStatement("proxyState.getRealm$realm().checkIfValid()")
                .beginControlFlow("if (value == null)")
                .emitStatement("proxyState.getRow$realm().nullifyLink(%s)", fieldIndexVariableReference(field))
                .emitStatement("return")
                .endControlFlow()
                .beginControlFlow("if (!(RealmObject.isManaged(value) && RealmObject.isValid(value)))")
                .emitStatement("throw new IllegalArgumentException(\"'value' is not a valid managed object.\")")
                .endControlFlow()
                .beginControlFlow("if (((RealmObjectProxy)value).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm())")
                .emitStatement("throw new IllegalArgumentException(\"'value' belongs to a different Realm.\")")
                .endControlFlow()
                .emitStatement("proxyState.getRow$realm().setLink(%s, ((RealmObjectProxy)value).realmGet$proxyState().getRow$realm().getIndex())", fieldIndexVariableReference(field))
                .endMethod();
    }
    //@formatter:on

    /**
     * LinkLists
     */
    //@formatter:off
    private void emitRealmList(
            JavaWriter writer,
            final VariableElement field,
            String fieldName,
            String fieldTypeCanonicalName) throws IOException {
        String genericType = Utils.getGenericTypeQualifiedName(field);

        // Getter
        writer.emitAnnotation("Override");
        writer.beginMethod(fieldTypeCanonicalName, metadata.getInternalGetter(fieldName), EnumSet.of(Modifier.PUBLIC))
                .emitStatement("proxyState.getRealm$realm().checkIfValid()")
                .emitSingleLineComment("use the cached value if available")
                .beginControlFlow("if (" + fieldName + "RealmList != null)")
                .emitStatement("return " + fieldName + "RealmList")
                .nextControlFlow("else")
                .emitStatement("LinkView linkView = proxyState.getRow$realm().getLinkList(%s)", fieldIndexVariableReference(field))
                .emitStatement(fieldName + "RealmList = new RealmList<%s>(%s.class, linkView, proxyState.getRealm$realm())",
                        genericType, genericType)
                .emitStatement("return " + fieldName + "RealmList")
                .endControlFlow()
                .endMethod()
                .emitEmptyLine();

        // Setter
        writer.emitAnnotation("Override");
        writer.beginMethod("void", metadata.getInternalSetter(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value");
        emitCodeForUnderConstruction(writer, metadata.isPrimaryKey(field), new CodeEmitter() {
            @Override
            public void emit(JavaWriter writer) throws IOException {
                // check excludeFields
                writer.beginControlFlow("if (proxyState.getExcludeFields$realm().contains(\"%1$s\"))",
                        field.getSimpleName().toString())
                        .emitStatement("return")
                        .endControlFlow();
                final String modelFqcn = Utils.getGenericTypeQualifiedName(field);
                writer.beginControlFlow("if (value != null && !value.isManaged())")
                        .emitStatement("final Realm realm = (Realm) proxyState.getRealm$realm()")
                        .emitStatement("final RealmList<%1$s> original = value", modelFqcn)
                        .emitStatement("value = new RealmList<%1$s>()", modelFqcn)
                        .beginControlFlow("for (%1$s item : original)", modelFqcn)
                        .beginControlFlow("if (item == null || RealmObject.isManaged(item))")
                        .emitStatement("value.add(item)")
                        .nextControlFlow("else")
                        .emitStatement("value.add(realm.copyToRealm(item))")
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow();

                // LinkView currently does not support default value feature. Just fallback to normal code.
            }
        });
        writer.emitStatement("proxyState.getRealm$realm().checkIfValid()")
                .emitStatement("LinkView links = proxyState.getRow$realm().getLinkList(%s)", fieldIndexVariableReference(field))
                .emitStatement("links.clear()")
                .beginControlFlow("if (value == null)")
                .emitStatement("return")
                .endControlFlow()
                .beginControlFlow("for (RealmModel linkedObject : (RealmList<? extends RealmModel>) value)")
                .beginControlFlow("if (!(RealmObject.isManaged(linkedObject) && RealmObject.isValid(linkedObject)))")
                .emitStatement("throw new IllegalArgumentException(\"Each element of 'value' must be a valid managed object.\")")
                .endControlFlow()
                .beginControlFlow("if (((RealmObjectProxy)linkedObject).realmGet$proxyState().getRealm$realm() != proxyState.getRealm$realm())")
                .emitStatement("throw new IllegalArgumentException(\"Each element of 'value' must belong to the same Realm.\")")
                .endControlFlow()
                .emitStatement("links.add(((RealmObjectProxy)linkedObject).realmGet$proxyState().getRow$realm().getIndex())")
                .endControlFlow()
                .endMethod();
    }
    //@formatter:on

    private interface CodeEmitter {
        void emit(JavaWriter writer) throws IOException;
    }

    private void emitCodeForUnderConstruction(JavaWriter writer, boolean isPrimaryKey,
            CodeEmitter defaultValueCodeEmitter) throws IOException {
        writer.beginControlFlow("if (proxyState.isUnderConstruction())");
        if (isPrimaryKey) {
            writer.emitSingleLineComment("default value of the primary key is always ignored.")
                    .emitStatement("return");
        } else {
            writer.beginControlFlow("if (!proxyState.getAcceptDefaultValue$realm())")
                    .emitStatement("return")
                    .endControlFlow();
            defaultValueCodeEmitter.emit(writer);
        }
        writer.endControlFlow()
                .emitEmptyLine();
    }

    //@formatter:off
    private void emitInjectContextMethod(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override");
        writer.beginMethod(
                "void", // Return type
                "realm$injectObjectContext", // Method name
                EnumSet.of(Modifier.PUBLIC) // Modifiers
        ); // Argument type & argument name

        writer.beginControlFlow("if (this.proxyState != null)")
                .emitStatement("return")
                .endControlFlow()
                .emitStatement("final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get()")
                .emitStatement("this.columnInfo = (%1$s) context.getColumnInfo()", columnInfoClassName())
                .emitStatement("this.proxyState = new ProxyState<%1$s>(this)", qualifiedClassName)
                .emitStatement("proxyState.setRealm$realm(context.getRealm())")
                .emitStatement("proxyState.setRow$realm(context.getRow())")
                .emitStatement("proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue())")
                .emitStatement("proxyState.setExcludeFields$realm(context.getExcludeFields())");

        writer.endMethod()
                .emitEmptyLine();
    }
    //@formatter:on

    //@formatter:off
    private void emitBacklinkFieldAccessors(JavaWriter writer) throws IOException {
        for (Backlink backlink : metadata.getBacklinkFields()) {
            String cacheFieldName = backlink.getTargetField() + BACKLINKS_FIELD_EXTENSION;
            String realmResultsType = "RealmResults<" + backlink.getSourceClass() + ">";

            // Getter, no setter
            writer.emitAnnotation("Override");
            writer.beginMethod(realmResultsType, metadata.getInternalGetter(backlink.getTargetField()), EnumSet.of(Modifier.PUBLIC))
                    .emitStatement("BaseRealm realm = proxyState.getRealm$realm()")
                    .emitStatement("realm.checkIfValid()")
                    .emitStatement("proxyState.getRow$realm().checkIfAttached()")
                    .beginControlFlow("if (" + cacheFieldName + " == null)")
                    .emitStatement(cacheFieldName + " = RealmResults.createBacklinkResults(realm, proxyState.getRow$realm(), %s.class, \"%s\")",
                            backlink.getSourceClass(), backlink.getSourceField())
                    .endControlFlow()
                    .emitStatement("return " + cacheFieldName)
                    .endMethod()
                    .emitEmptyLine();
        }
    }
    //@formatter:on

    //@formatter:off
    private void emitRealmObjectProxyImplementation(JavaWriter writer) throws IOException {
        writer.emitAnnotation("Override")
                .beginMethod("ProxyState<?>", "realmGet$proxyState", EnumSet.of(Modifier.PUBLIC))
                .emitStatement("return proxyState")
                .endMethod()
                .emitEmptyLine();
    }
    //@formatter:on

    private void emitCreateExpectedObjectSchemaInfo(JavaWriter writer) throws IOException {
        writer.beginMethod(
                "OsObjectSchemaInfo", // Return type
                "createExpectedObjectSchemaInfo", // Method name
                EnumSet.of(Modifier.PRIVATE, Modifier.STATIC)); // Modifiers

        writer.emitStatement(
                "OsObjectSchemaInfo info = new OsObjectSchemaInfo(\"%s\");", this.simpleClassName);

        // For each field generate corresponding table index constant
        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();

            Constants.RealmFieldType fieldType = getRealmType(field);
            switch (fieldType) {
                case NOTYPE:
                    // Perhaps this should fail quickly?
                    break;

                case OBJECT:
                    String fieldTypeSimpleName = Utils.getFieldTypeSimpleName(field);
                    writer.emitStatement("info.add(\"%s\", RealmFieldType.OBJECT, \"%s\")",
                            fieldName, fieldTypeSimpleName);
                    break;

                case LIST:
                    String genericTypeSimpleName = Utils.getGenericTypeSimpleName(field);
                    writer.emitStatement("info.add(\"%s\", RealmFieldType.LIST, \"%s\")",
                            fieldName, genericTypeSimpleName);
                    break;

                default:
                    String nullableFlag = (metadata.isNullable(field) ? "!" : "") + "Property.REQUIRED";
                    String indexedFlag = (metadata.isIndexed(field) ? "" : "!") + "Property.INDEXED";
                    String primaryKeyFlag = (metadata.isPrimaryKey(field) ? "" : "!") + "Property.PRIMARY_KEY";
                    writer.emitStatement("info.add(\"%s\", %s, %s, %s, %s)",
                            fieldName,
                            fieldType.getRealmType(),
                            primaryKeyFlag,
                            indexedFlag,
                            nullableFlag);
            }
        }
        writer.emitStatement("return info");
        writer.endMethod()
                .emitEmptyLine();
    }

    private void emitGetExpectedObjectSchemaInfo(JavaWriter writer) throws IOException {
        writer.beginMethod(
                "OsObjectSchemaInfo", // Return type
                "getExpectedObjectSchemaInfo", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC)); // Modifiers

        writer.emitStatement(" return expectedObjectSchemaInfo");

        writer.endMethod()
                .emitEmptyLine();
    }

    private void emitValidateTableMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                columnInfoClassName(),        // Return type
                "validateTable",              // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "SharedRealm", "sharedRealm", // Argument type & argument name
                "boolean", "allowExtraColumns");

        writer.beginControlFlow(
                "if (!sharedRealm.hasTable(\"" + Constants.TABLE_PREFIX + this.simpleClassName + "\"))");
        emitMigrationNeededException(writer, "\"The '%s' class is missing from the schema for this Realm.\")",
                metadata.getSimpleClassName());
        writer.endControlFlow();

        writer.emitStatement(
                "Table table = sharedRealm.getTable(\"%s%s\")",
                Constants.TABLE_PREFIX,
                this.simpleClassName);

        // verify number of columns
        writer.emitStatement("final long columnCount = table.getColumnCount()");
        writer.beginControlFlow("if (columnCount != %d)", metadata.getFields().size());
        writer.beginControlFlow("if (columnCount < %d)", metadata.getFields().size());
        emitMigrationNeededException(writer, "\"Field count is less than expected - expected %d but was \" + columnCount)",
                metadata.getFields().size());
        writer.endControlFlow();
        writer.beginControlFlow("if (allowExtraColumns)");
        writer.emitStatement(
                "RealmLog.debug(\"Field count is more than expected - expected %d but was %%1$d\", columnCount)",
                metadata.getFields().size());
        writer.nextControlFlow("else");
        emitMigrationNeededException(writer, "\"Field count is more than expected - expected %d but was \" + columnCount)",
                metadata.getFields().size());
        writer.endControlFlow();
        writer.endControlFlow();

        // create type dictionary for lookup
        writer.emitStatement("Map<String, RealmFieldType> columnTypes = new HashMap<String, RealmFieldType>()");
        writer.beginControlFlow("for (long i = 0; i < columnCount; i++)")
                .emitStatement("columnTypes.put(table.getColumnName(i), table.getColumnType(i))")
                .endControlFlow()
                .emitEmptyLine();

        // create an instance of ColumnInfo
        writer.emitStatement("final %1$s columnInfo = new %1$s(sharedRealm, table)", columnInfoClassName())
                .emitEmptyLine();

        // verify primary key definition was not altered
        if (metadata.hasPrimaryKey()) {
            // the current model defines a PK, make sure it's defined in the Realm schema
            String fieldName = metadata.getPrimaryKey().getSimpleName().toString();
            writer.beginControlFlow("if (!table.hasPrimaryKey())");
            emitMigrationNeededException(writer, "\"Primary key not defined for field '%s' in existing Realm file. @PrimaryKey was added.\")",
                    metadata.getPrimaryKey().getSimpleName().toString());
            writer.nextControlFlow("else")
                    .beginControlFlow("if (table.getPrimaryKey() != columnInfo.%sIndex)", fieldName);
            emitMigrationNeededException(writer, "\"Primary Key annotation definition was changed, from field \" + table.getColumnName(table.getPrimaryKey()) + \" to field %s\")",
                    metadata.getPrimaryKey().getSimpleName().toString());
            writer.endControlFlow()
                    .endControlFlow();
        } else {
            // the current model doesn't define a PK, make sure it's not defined in the Realm schema
            writer.beginControlFlow("if (table.hasPrimaryKey())");
            emitMigrationNeededException(writer, "\"Primary Key defined for field \" + table.getColumnName(table.getPrimaryKey()) + \" was removed.\")");
            writer.endControlFlow();
        }
        writer.emitEmptyLine();

        // For each field verify there is a corresponding
        long fieldIndex = -1;
        for (VariableElement field : metadata.getFields()) {
            fieldIndex++;
            String fieldName = field.getSimpleName().toString();
            String fieldTypeQualifiedName = Utils.getFieldTypeQualifiedName(field);
            if (Constants.JAVA_TO_REALM_TYPES.containsKey(fieldTypeQualifiedName)) {
                emitValidateRealmType(writer, field, fieldName, fieldTypeQualifiedName);
            } else if (Utils.isRealmModel(field)) { // Links
                emitValidateRealmModelType(writer, field, fieldIndex, fieldName);
            } else if (Utils.isRealmList(field)) { // Link Lists
                emitValidateRealmListType(writer, field, fieldIndex, fieldName);
            }
        }

        // verify the backlinks
        Set<Backlink> backlinks = metadata.getBacklinkFields();
        if (backlinks.size() > 0) {
            writer.emitEmptyLine()
                    .emitStatement("long backlinkFieldIndex")
                    .emitStatement("Table backlinkSourceTable")
                    .emitStatement("Table backlinkTargetTable")
                    .emitStatement("RealmFieldType backlinkFieldType");
            for (Backlink backlink : metadata.getBacklinkFields()) {
                emitValidateBacklink(writer, backlink);
            }
        }

        writer.emitEmptyLine();
        writer.emitStatement("return %s", "columnInfo");

        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitValidateRealmType(JavaWriter writer, VariableElement field, String fieldName, String fieldTypeQualifiedName)
            throws IOException {

        // make sure types align
        writer.beginControlFlow("if (!columnTypes.containsKey(\"%s\"))", fieldName);
        emitMigrationNeededException(writer, "\"Missing field '%s' in existing Realm file. " +
                "Either remove field or migrate using io.realm.internal.Table.addColumn()." +
                "\")", fieldName);
        writer.endControlFlow();
        writer.beginControlFlow("if (columnTypes.get(\"%s\") != %s)",
                fieldName, getRealmTypeChecked(field).getRealmType());
        emitMigrationNeededException(writer, "\"Invalid type '%s' for field '%s' in existing Realm file.\")",
                Utils.getFieldTypeSimpleName(field), fieldName);
        writer.endControlFlow();

        // make sure that nullability matches
        if (metadata.isNullable(field)) {
            writer.beginControlFlow("if (!table.isColumnNullable(%s))", fieldIndexVariableReference(field));
            // Check if the existing PrimaryKey does support null value for String, Byte, Short, Integer, & Long
            if (metadata.isPrimaryKey(field)) {
                writer.emitStatement("throw new RealmMigrationNeededException(sharedRealm.getPath()," +
                                "\"@PrimaryKey field '%s' does not support null values in the existing Realm file. " +
                                "Migrate using RealmObjectSchema.setNullable(), or mark the field as @Required.\")",
                        fieldName);
                // nullability check for boxed types
            } else if (Utils.isBoxedType(fieldTypeQualifiedName)) {
                writer.emitStatement("throw new RealmMigrationNeededException(sharedRealm.getPath()," +
                                "\"Field '%s' does not support null values in the existing Realm file. " +
                                "Either set @Required, use the primitive type for field '%s' " +
                                "or migrate using RealmObjectSchema.setNullable().\")",
                        fieldName, fieldName);
            } else {
                writer.emitStatement("throw new RealmMigrationNeededException(sharedRealm.getPath()," +
                                " \"Field '%s' is required. Either set @Required to field '%s' " +
                                "or migrate using RealmObjectSchema.setNullable().\")",
                        fieldName, fieldName);
            }
            writer.endControlFlow();
        } else {
            // check before migrating a nullable field containing null value to not-nullable PrimaryKey field for Realm version 0.89+
            if (metadata.isPrimaryKey(field)) {
                writer
                        .beginControlFlow("if (table.isColumnNullable(%s) && table.findFirstNull(%s) != Table.NO_MATCH)",
                                fieldIndexVariableReference(field), fieldIndexVariableReference(field))
                        .emitStatement("throw new IllegalStateException(\"Cannot migrate an object with null value in field '%s'." +
                                        " Either maintain the same type for primary key field '%s', or remove the object with null value before migration.\")",
                                fieldName, fieldName)
                        .endControlFlow();
            } else {
                writer.beginControlFlow("if (table.isColumnNullable(%s))", fieldIndexVariableReference(field));
                if (Utils.isPrimitiveType(fieldTypeQualifiedName)) {
                    writer.emitStatement("throw new RealmMigrationNeededException(sharedRealm.getPath()," +
                                    " \"Field '%s' does support null values in the existing Realm file. " +
                                    "Use corresponding boxed type for field '%s' or migrate using RealmObjectSchema.setNullable().\")",
                            fieldName, fieldName);
                } else {
                    writer.emitStatement("throw new RealmMigrationNeededException(sharedRealm.getPath()," +
                                    " \"Field '%s' does support null values in the existing Realm file. " +
                                    "Remove @Required or @PrimaryKey from field '%s' or migrate using RealmObjectSchema.setNullable().\")",
                            fieldName, fieldName);
                }
                writer.endControlFlow();
            }
        }

        // Validate @Index
        if (metadata.getIndexedFields().contains(field)) {
            writer.beginControlFlow("if (!table.hasSearchIndex(table.getColumnIndex(\"%s\")))", fieldName);
            emitMigrationNeededException(writer, "\"Index not defined for field '%s' in existing Realm file. " +
                    "Either set @Index or migrate using io.realm.internal.Table.removeSearchIndex().\")", fieldName);
            writer.endControlFlow();
        }
    }

    private void emitValidateRealmModelType(JavaWriter writer, VariableElement field, long fieldIndex, String fieldName)
            throws IOException {
        String fieldTypeSimpleName = Utils.getFieldTypeSimpleName(field);

        writer.beginControlFlow("if (!columnTypes.containsKey(\"%s\"))", fieldName);
        emitMigrationNeededException(writer, "\"Missing field '%s' in existing Realm file. " +
                "Either remove field or migrate using io.realm.internal.Table.addColumn().\")", fieldName);
        writer.endControlFlow();
        writer.beginControlFlow("if (columnTypes.get(\"%s\") != RealmFieldType.OBJECT)", fieldName);
        emitMigrationNeededException(writer, "\"Invalid type '%s' for field '%s'\")",
                fieldTypeSimpleName, fieldName);
        writer.endControlFlow();
        writer.beginControlFlow("if (!sharedRealm.hasTable(\"%s%s\"))", Constants.TABLE_PREFIX, fieldTypeSimpleName);
        emitMigrationNeededException(writer, "\"Missing class '%s%s' for field '%s'\")",
                Constants.TABLE_PREFIX, fieldTypeSimpleName, fieldName);
        writer.endControlFlow();

        writer.emitStatement("Table table_%d = sharedRealm.getTable(\"%s%s\")", fieldIndex, Constants.TABLE_PREFIX, fieldTypeSimpleName);
        writer.beginControlFlow("if (!table.getLinkTarget(%s).hasSameSchema(table_%d))",
                fieldIndexVariableReference(field), fieldIndex);
        emitMigrationNeededException(writer, "\"Invalid RealmObject for field '%s': '\" + table.getLinkTarget(%s).getName() + \"' expected - was '\" + table_%d.getName() + \"'\")",
                fieldName, fieldIndexVariableReference(field), fieldIndex);
        writer.endControlFlow();
    }

    private void emitValidateRealmListType(JavaWriter writer, VariableElement field, long fieldIndex, String fieldName)
            throws IOException {
        String genericTypeSimpleName = Utils.getGenericTypeSimpleName(field);
        writer.beginControlFlow("if (!columnTypes.containsKey(\"%s\"))", fieldName);
        emitMigrationNeededException(writer, "\"Missing field '%s'\")", fieldName);
        writer.endControlFlow();
        writer.beginControlFlow("if (columnTypes.get(\"%s\") != RealmFieldType.LIST)", fieldName);
        emitMigrationNeededException(writer, "\"Invalid type '%s' for field '%s'\")",
                genericTypeSimpleName, fieldName);
        writer.endControlFlow();
        writer.beginControlFlow("if (!sharedRealm.hasTable(\"%s%s\"))", Constants.TABLE_PREFIX, genericTypeSimpleName);
        emitMigrationNeededException(writer, "\"Missing class '%s%s' for field '%s'\")",
                Constants.TABLE_PREFIX, genericTypeSimpleName, fieldName);
        writer.endControlFlow();

        writer.emitStatement("Table table_%d = sharedRealm.getTable(\"%s%s\")", fieldIndex, Constants.TABLE_PREFIX, genericTypeSimpleName);
        writer.beginControlFlow("if (!table.getLinkTarget(%s).hasSameSchema(table_%d))",
                fieldIndexVariableReference(field), fieldIndex);
        emitMigrationNeededException(writer, "\"Invalid RealmList type for field '%s': '\" + table.getLinkTarget(%s).getName() + \"' expected - was '\" + table_%d.getName() + \"'\")",
                fieldName, fieldIndexVariableReference(field), fieldIndex);
        writer.endControlFlow();
    }

    private void emitValidateBacklink(JavaWriter writer, Backlink backlink) throws IOException {
        String targetField = backlink.getTargetField();
        String targetClass = backlink.getTargetClass();

        // Preceding code has already verified that the backlink field is not in the table.
        // If it were, either the column count would be wrong, or some field would be missing.

        // verify that the source class exists
        String sourceClass = backlink.getSimpleSourceClass();
        String fullyQualifiedSourceClass = backlink.getSourceClass();
        writer.beginControlFlow("if (!sharedRealm.hasTable(\"%s%s\"))", Constants.TABLE_PREFIX, sourceClass);
        emitMigrationNeededException(writer, "\"Cannot find source class '%s' for @LinkingObjects field '%s.%s'\")",
                fullyQualifiedSourceClass, targetClass, targetField);
        writer.endControlFlow();

        // verify that the source class contains the source field
        String sourceField = backlink.getSourceField();
        writer.emitStatement("backlinkSourceTable = sharedRealm.getTable(\"%s%s\")", Constants.TABLE_PREFIX, sourceClass);
        writer.emitStatement("backlinkFieldIndex = backlinkSourceTable.getColumnIndex(\"%s\")", sourceField);
        writer.beginControlFlow("if (backlinkFieldIndex == Table.NO_MATCH)");
        emitMigrationNeededException(writer, "\"Cannot find source field '%s.%s' for @LinkingObjects field '%s.%s'\")",
                fullyQualifiedSourceClass, sourceField, targetClass, targetField);
        writer.endControlFlow();

        // verify that the source field type is target class
        writer.emitStatement("backlinkFieldType = backlinkSourceTable.getColumnType(backlinkFieldIndex)");
        writer.beginControlFlow("if ((backlinkFieldType != RealmFieldType.OBJECT) && (backlinkFieldType != RealmFieldType.LIST))");
        emitMigrationNeededException(writer, "\"Source field '%s.%s' for @LinkingObjects field '%s.%s' is not a RealmObject type\")",
                fullyQualifiedSourceClass, sourceField, targetClass, targetField);
        writer.endControlFlow();
        writer.emitStatement("backlinkTargetTable = backlinkSourceTable.getLinkTarget(backlinkFieldIndex)");
        writer.beginControlFlow("if (!table.hasSameSchema(backlinkTargetTable))");
        emitMigrationNeededException(writer, "\"Source field '%s.%s' for @LinkingObjects field '%s.%s' has wrong type '\" + backlinkTargetTable.getName() + \"'\")",
                fullyQualifiedSourceClass, sourceField, targetClass, targetField);
        writer.endControlFlow();
    }

    //@formatter:off
    private void emitGetTableNameMethod(JavaWriter writer) throws IOException {
        writer.beginMethod("String", "getTableName", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .emitStatement("return \"%s%s\"", Constants.TABLE_PREFIX, simpleClassName)
                .endMethod()
                .emitEmptyLine();
    }
    //@formatter:on

    //@formatter:off
    private void emitGetFieldNamesMethod(JavaWriter writer) throws IOException {
        writer.beginMethod("List<String>", "getFieldNames", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .emitStatement("return FIELD_NAMES")
                .endMethod()
                .emitEmptyLine();
    }
    //@formatter:on

    //@formatter:off
    private void emitCopyOrUpdateMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                qualifiedClassName, // Return type
                "copyOrUpdate", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", qualifiedClassName, "object", "boolean", "update", "Map<RealmModel,RealmObjectProxy>", "cache" // Argument type & argument name
        );

        writer
                .beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().threadId != realm.threadId)")
                .emitStatement("throw new IllegalArgumentException(\"Objects which belong to Realm instances in other" +
                        " threads cannot be copied into this Realm instance.\")")
                .endControlFlow();

        // If object is already in the Realm there is nothing to update
        writer
                .beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath()))")
                .emitStatement("return object")
                .endControlFlow();

        writer.emitStatement("final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get()");

        writer.emitStatement("RealmObjectProxy cachedRealmObject = cache.get(object)");
        writer.beginControlFlow("if (cachedRealmObject != null)")
                .emitStatement("return (%s) cachedRealmObject", qualifiedClassName)
                .nextControlFlow("else");

        if (!metadata.hasPrimaryKey()) {
            writer.emitStatement("return copy(realm, object, update, cache)");
        } else {
            writer
                    .emitStatement("%s realmObject = null", qualifiedClassName)
                    .emitStatement("boolean canUpdate = update")
                    .beginControlFlow("if (canUpdate)")
                    .emitStatement("Table table = realm.getTable(%s.class)", qualifiedClassName)
                    .emitStatement("long pkColumnIndex = table.getPrimaryKey()");

            String primaryKeyGetter = metadata.getPrimaryKeyGetter();
            VariableElement primaryKeyElement = metadata.getPrimaryKey();
            if (metadata.isNullable(primaryKeyElement)) {
                if (Utils.isString(primaryKeyElement)) {
                    writer
                            .emitStatement("String value = ((%s) object).%s()", interfaceName, primaryKeyGetter)
                            .emitStatement("long rowIndex = Table.NO_MATCH")
                            .beginControlFlow("if (value == null)")
                            .emitStatement("rowIndex = table.findFirstNull(pkColumnIndex)")
                            .nextControlFlow("else")
                            .emitStatement("rowIndex = table.findFirstString(pkColumnIndex, value)")
                            .endControlFlow();
                } else {
                    writer
                            .emitStatement("Number value = ((%s) object).%s()", interfaceName, primaryKeyGetter)
                            .emitStatement("long rowIndex = Table.NO_MATCH")
                            .beginControlFlow("if (value == null)")
                            .emitStatement("rowIndex = table.findFirstNull(pkColumnIndex)")
                            .nextControlFlow("else")
                            .emitStatement("rowIndex = table.findFirstLong(pkColumnIndex, value.longValue())")
                            .endControlFlow();
                }
            } else {
                String pkType = Utils.isString(metadata.getPrimaryKey()) ? "String" : "Long";
                writer.emitStatement("long rowIndex = table.findFirst%s(pkColumnIndex, ((%s) object).%s())",
                        pkType, interfaceName, primaryKeyGetter);
            }

            writer
                    .beginControlFlow("if (rowIndex != Table.NO_MATCH)")
                    .beginControlFlow("try")
                    .emitStatement("objectContext.set(realm, table.getUncheckedRow(rowIndex)," +
                            " realm.schema.getColumnInfo(%s.class)," +
                            " false, Collections.<String> emptyList())", qualifiedClassName)
                    .emitStatement("realmObject = new %s()", qualifiedGeneratedClassName)
                    .emitStatement("cache.put(object, (RealmObjectProxy) realmObject)")
                    .nextControlFlow("finally")
                    .emitStatement("objectContext.clear()")
                    .endControlFlow()

                    .nextControlFlow("else")
                    .emitStatement("canUpdate = false")
                    .endControlFlow();

            writer.endControlFlow();

            writer
                    .emitEmptyLine()
                    .beginControlFlow("if (canUpdate)")
                    .emitStatement("return update(realm, realmObject, object, cache)")
                    .nextControlFlow("else")
                    .emitStatement("return copy(realm, object, update, cache)")
                    .endControlFlow();
        }

        writer.endControlFlow();
        writer.endMethod()
                .emitEmptyLine();
    }
    //@formatter:on

    //@formatter:off
    private void setTableValues(JavaWriter writer, String fieldType, String fieldName, String interfaceName, String getter, boolean isUpdate) throws IOException {
        if ("long".equals(fieldType)
                || "int".equals(fieldType)
                || "short".equals(fieldType)
                || "byte".equals(fieldType)) {
            writer.emitStatement("Table.nativeSetLong(tableNativePtr, columnInfo.%sIndex, rowIndex, ((%s)object).%s(), false)", fieldName, interfaceName, getter);

        } else if ("java.lang.Long".equals(fieldType)
                || "java.lang.Integer".equals(fieldType)
                || "java.lang.Short".equals(fieldType)
                || "java.lang.Byte".equals(fieldType)) {
            writer
                    .emitStatement("Number %s = ((%s)object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetLong(tableNativePtr, columnInfo.%sIndex, rowIndex, %s.longValue(), false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();

        } else if ("double".equals(fieldType)) {
            writer.emitStatement("Table.nativeSetDouble(tableNativePtr, columnInfo.%sIndex, rowIndex, ((%s)object).%s(), false)", fieldName, interfaceName, getter);

        } else if ("java.lang.Double".equals(fieldType)) {
            writer
                    .emitStatement("Double %s = ((%s)object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetDouble(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();

        } else if ("float".equals(fieldType)) {
            writer.emitStatement("Table.nativeSetFloat(tableNativePtr, columnInfo.%sIndex, rowIndex, ((%s)object).%s(), false)", fieldName, interfaceName, getter);

        } else if ("java.lang.Float".equals(fieldType)) {
            writer
                    .emitStatement("Float %s = ((%s)object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetFloat(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();

        } else if ("boolean".equals(fieldType)) {
            writer.emitStatement("Table.nativeSetBoolean(tableNativePtr, columnInfo.%sIndex, rowIndex, ((%s)object).%s(), false)", fieldName, interfaceName, getter);

        } else if ("java.lang.Boolean".equals(fieldType)) {
            writer
                    .emitStatement("Boolean %s = ((%s)object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetBoolean(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();

        } else if ("byte[]".equals(fieldType)) {
            writer
                    .emitStatement("byte[] %s = ((%s)object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetByteArray(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();


        } else if ("java.util.Date".equals(fieldType)) {
            writer
                    .emitStatement("java.util.Date %s = ((%s)object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetTimestamp(tableNativePtr, columnInfo.%sIndex, rowIndex, %s.getTime(), false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();

        } else if ("java.lang.String".equals(fieldType)) {
            writer
                    .emitStatement("String %s = ((%s)object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetString(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();
        } else {
            throw new IllegalStateException("Unsupported type " + fieldType);
        }
    }
    //@formatter:on

    private void emitInsertMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                "long", // Return type
                "insert", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", qualifiedClassName, "object", "Map<RealmModel,Long>", "cache" // Argument type & argument name
        );

        // If object is already in the Realm there is nothing to update
        writer
                .beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath()))")
                .emitStatement("return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex()")
                .endControlFlow();

        writer.emitStatement("Table table = realm.getTable(%s.class)", qualifiedClassName);
        writer.emitStatement("long tableNativePtr = table.getNativePtr()");
        writer.emitStatement("%s columnInfo = (%s) realm.schema.getColumnInfo(%s.class)",
                columnInfoClassName(), columnInfoClassName(), qualifiedClassName);

        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("long pkColumnIndex = table.getPrimaryKey()");
        }
        addPrimaryKeyCheckIfNeeded(metadata, true, writer);

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String fieldType = field.asType().toString();
            String getter = metadata.getInternalGetter(fieldName);

            //@formatter:off
            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitStatement("%s %sObj = ((%s) object).%s()", fieldType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sObj != null)", fieldName)
                        .emitStatement("Long cache%1$s = cache.get(%1$sObj)", fieldName)
                        .beginControlFlow("if (cache%s == null)", fieldName)
                        .emitStatement("cache%s = %s.insert(realm, %sObj, cache)",
                                fieldName,
                                Utils.getProxyClassSimpleName(field),
                                fieldName)
                        .endControlFlow()
                        .emitStatement("Table.nativeSetLink(tableNativePtr, columnInfo.%1$sIndex, rowIndex, cache%1$s, false)", fieldName)
                        .endControlFlow();
            } else if (Utils.isRealmList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                writer
                        .emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("long %1$sNativeLinkViewPtr = Table.nativeGetLinkView(tableNativePtr, columnInfo.%1$sIndex, rowIndex)", fieldName)
                        .beginControlFlow("for (%1$s %2$sItem : %2$sList)", genericType, fieldName)
                        .emitStatement("Long cacheItemIndex%1$s = cache.get(%1$sItem)", fieldName)
                        .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                        .emitStatement("cacheItemIndex%1$s = %2$s.insert(realm, %1$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("LinkView.nativeAdd(%1$sNativeLinkViewPtr, cacheItemIndex%1$s)", fieldName)
                        .endControlFlow()
                        .endControlFlow()
                        .emitEmptyLine();

            } else {
                if (metadata.getPrimaryKey() != field) {
                    setTableValues(writer, fieldType, fieldName, interfaceName, getter, false);
                }
            }
            //@formatter:on
        }

        writer.emitStatement("return rowIndex");
        writer.endMethod()
                .emitEmptyLine();
    }

    private void emitInsertListMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                "void", // Return type
                "insert", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", "Iterator<? extends RealmModel>", "objects", "Map<RealmModel,Long>", "cache" // Argument type & argument name
        );

        writer.emitStatement("Table table = realm.getTable(%s.class)", qualifiedClassName);
        writer.emitStatement("long tableNativePtr = table.getNativePtr()");
        writer.emitStatement("%s columnInfo = (%s) realm.schema.getColumnInfo(%s.class)",
                columnInfoClassName(), columnInfoClassName(), qualifiedClassName);
        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("long pkColumnIndex = table.getPrimaryKey()");
        }
        writer.emitStatement("%s object = null", qualifiedClassName);

        writer.beginControlFlow("while (objects.hasNext())")
                .emitStatement("object = (%s) objects.next()", qualifiedClassName);
        writer.beginControlFlow("if(!cache.containsKey(object))");

        writer.beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath()))");
        writer.emitStatement("cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex())")
                .emitStatement("continue");
        writer.endControlFlow();

        addPrimaryKeyCheckIfNeeded(metadata, true, writer);

        //@formatter:off
        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String fieldType = field.asType().toString();
            String getter = metadata.getInternalGetter(fieldName);

            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitStatement("%s %sObj = ((%s) object).%s()", fieldType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sObj != null)", fieldName)
                        .emitStatement("Long cache%1$s = cache.get(%1$sObj)", fieldName)
                        .beginControlFlow("if (cache%s == null)", fieldName)
                        .emitStatement("cache%s = %s.insert(realm, %sObj, cache)",
                                fieldName,
                                Utils.getProxyClassSimpleName(field),
                                fieldName)
                        .endControlFlow()
                        .emitStatement("table.setLink(columnInfo.%1$sIndex, rowIndex, cache%1$s, false)", fieldName)
                        .endControlFlow();
            } else if (Utils.isRealmList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                writer
                        .emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("long %1$sNativeLinkViewPtr = Table.nativeGetLinkView(tableNativePtr, columnInfo.%1$sIndex, rowIndex)", fieldName)
                        .beginControlFlow("for (%1$s %2$sItem : %2$sList)", genericType, fieldName)
                        .emitStatement("Long cacheItemIndex%1$s = cache.get(%1$sItem)", fieldName)
                        .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                        .emitStatement("cacheItemIndex%1$s = %2$s.insert(realm, %1$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("LinkView.nativeAdd(%1$sNativeLinkViewPtr, cacheItemIndex%1$s)", fieldName)
                        .endControlFlow()
                        .endControlFlow()
                        .emitEmptyLine();

            } else {
                if (metadata.getPrimaryKey() != field) {
                    setTableValues(writer, fieldType, fieldName, interfaceName, getter, false);
                }
            }
        }
        //@formatter:on

        writer.endControlFlow();
        writer.endControlFlow();
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitInsertOrUpdateMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                "long", // Return type
                "insertOrUpdate", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", qualifiedClassName, "object", "Map<RealmModel,Long>", "cache" // Argument type & argument name
        );

        // If object is already in the Realm there is nothing to update
        writer
                .beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath()))")
                .emitStatement("return ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex()")
                .endControlFlow();

        writer.emitStatement("Table table = realm.getTable(%s.class)", qualifiedClassName);
        writer.emitStatement("long tableNativePtr = table.getNativePtr()");
        writer.emitStatement("%s columnInfo = (%s) realm.schema.getColumnInfo(%s.class)",
                columnInfoClassName(), columnInfoClassName(), qualifiedClassName);

        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("long pkColumnIndex = table.getPrimaryKey()");
        }
        addPrimaryKeyCheckIfNeeded(metadata, false, writer);

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String fieldType = field.asType().toString();
            String getter = metadata.getInternalGetter(fieldName);

            //@formatter:off
            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitStatement("%s %sObj = ((%s) object).%s()", fieldType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sObj != null)", fieldName)
                        .emitStatement("Long cache%1$s = cache.get(%1$sObj)", fieldName)
                        .beginControlFlow("if (cache%s == null)", fieldName)
                        .emitStatement("cache%1$s = %2$s.insertOrUpdate(realm, %1$sObj, cache)",
                                fieldName,
                                Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("Table.nativeSetLink(tableNativePtr, columnInfo.%1$sIndex, rowIndex, cache%1$s, false)", fieldName)
                        .nextControlFlow("else")
                        // No need to throw exception here if the field is not nullable. A exception will be thrown in setter.
                        .emitStatement("Table.nativeNullifyLink(tableNativePtr, columnInfo.%sIndex, rowIndex)", fieldName)
                        .endControlFlow();
            } else if (Utils.isRealmList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                writer
                        .emitEmptyLine()
                        .emitStatement("long %1$sNativeLinkViewPtr = Table.nativeGetLinkView(tableNativePtr, columnInfo.%1$sIndex, rowIndex)", fieldName)
                        .emitStatement("LinkView.nativeClear(%sNativeLinkViewPtr)", fieldName)
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .beginControlFlow("for (%1$s %2$sItem : %2$sList)", genericType, fieldName)
                        .emitStatement("Long cacheItemIndex%1$s = cache.get(%1$sItem)", fieldName)
                        .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                        .emitStatement("cacheItemIndex%1$s = %2$s.insertOrUpdate(realm, %1$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("LinkView.nativeAdd(%1$sNativeLinkViewPtr, cacheItemIndex%1$s)", fieldName)
                        .endControlFlow()
                        .endControlFlow()
                        .emitEmptyLine();

            } else {
                if (metadata.getPrimaryKey() != field) {
                    setTableValues(writer, fieldType, fieldName, interfaceName, getter, true);
                }
            }
            //@formatter:on
        }

        writer.emitStatement("return rowIndex");

        writer.endMethod()
                .emitEmptyLine();
    }

    private void emitInsertOrUpdateListMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                "void", // Return type
                "insertOrUpdate", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", "Iterator<? extends RealmModel>", "objects", "Map<RealmModel,Long>", "cache" // Argument type & argument name
        );

        writer.emitStatement("Table table = realm.getTable(%s.class)", qualifiedClassName);
        writer.emitStatement("long tableNativePtr = table.getNativePtr()");
        writer.emitStatement("%s columnInfo = (%s) realm.schema.getColumnInfo(%s.class)",
                columnInfoClassName(), columnInfoClassName(), qualifiedClassName);
        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("long pkColumnIndex = table.getPrimaryKey()");
        }
        writer.emitStatement("%s object = null", qualifiedClassName);

        writer.beginControlFlow("while (objects.hasNext())");
        writer.emitStatement("object = (%s) objects.next()", qualifiedClassName);
        writer.beginControlFlow("if(!cache.containsKey(object))");

        writer.beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy)object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath()))");
        writer.emitStatement("cache.put(object, ((RealmObjectProxy)object).realmGet$proxyState().getRow$realm().getIndex())")
                .emitStatement("continue");
        writer.endControlFlow();
        addPrimaryKeyCheckIfNeeded(metadata, false, writer);

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String fieldType = field.asType().toString();
            String getter = metadata.getInternalGetter(fieldName);

            //@formatter:off
            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitStatement("%s %sObj = ((%s) object).%s()", fieldType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sObj != null)", fieldName)
                        .emitStatement("Long cache%1$s = cache.get(%1$sObj)", fieldName)
                        .beginControlFlow("if (cache%s == null)", fieldName)
                        .emitStatement("cache%1$s = %2$s.insertOrUpdate(realm, %1$sObj, cache)",
                                fieldName,
                                Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("Table.nativeSetLink(tableNativePtr, columnInfo.%1$sIndex, rowIndex, cache%1$s, false)", fieldName)
                        .nextControlFlow("else")
                        // No need to throw exception here if the field is not nullable. A exception will be thrown in setter.
                        .emitStatement("Table.nativeNullifyLink(tableNativePtr, columnInfo.%sIndex, rowIndex)", fieldName)
                        .endControlFlow();
            } else if (Utils.isRealmList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                writer
                        .emitEmptyLine()
                        .emitStatement("long %1$sNativeLinkViewPtr = Table.nativeGetLinkView(tableNativePtr, columnInfo.%1$sIndex, rowIndex)", fieldName)
                        .emitStatement("LinkView.nativeClear(%sNativeLinkViewPtr)", fieldName)
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .beginControlFlow("for (%1$s %2$sItem : %2$sList)", genericType, fieldName)
                        .emitStatement("Long cacheItemIndex%1$s = cache.get(%1$sItem)", fieldName)
                        .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                        .emitStatement("cacheItemIndex%1$s = %2$s.insertOrUpdate(realm, %1$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("LinkView.nativeAdd(%1$sNativeLinkViewPtr, cacheItemIndex%1$s)", fieldName)
                        .endControlFlow()
                        .endControlFlow()
                        .emitEmptyLine();

            } else {
                if (metadata.getPrimaryKey() != field) {
                    setTableValues(writer, fieldType, fieldName, interfaceName, getter, true);
                }
            }
            //@formatter:on
        }
        writer.endControlFlow();
        writer.endControlFlow();

        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void addPrimaryKeyCheckIfNeeded(ClassMetaData metadata, boolean throwIfPrimaryKeyDuplicate, JavaWriter writer) throws IOException {
        if (metadata.hasPrimaryKey()) {
            String primaryKeyGetter = metadata.getPrimaryKeyGetter();
            VariableElement primaryKeyElement = metadata.getPrimaryKey();
            if (metadata.isNullable(primaryKeyElement)) {
                //@formatter:off
                if (Utils.isString(primaryKeyElement)) {
                    writer
                            .emitStatement("String primaryKeyValue = ((%s) object).%s()", interfaceName, primaryKeyGetter)
                            .emitStatement("long rowIndex = Table.NO_MATCH")
                            .beginControlFlow("if (primaryKeyValue == null)")
                            .emitStatement("rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex)")
                            .nextControlFlow("else")
                            .emitStatement("rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue)")
                            .endControlFlow();
                } else {
                    writer
                            .emitStatement("Object primaryKeyValue = ((%s) object).%s()", interfaceName, primaryKeyGetter)
                            .emitStatement("long rowIndex = Table.NO_MATCH")
                            .beginControlFlow("if (primaryKeyValue == null)")
                            .emitStatement("rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex)")
                            .nextControlFlow("else")
                            .emitStatement("rowIndex = Table.nativeFindFirstInt(tableNativePtr, pkColumnIndex, ((%s) object).%s())", interfaceName, primaryKeyGetter)
                            .endControlFlow();
                }
                //@formatter:on
            } else {
                writer.emitStatement("long rowIndex = Table.NO_MATCH");
                writer.emitStatement("Object primaryKeyValue = ((%s) object).%s()", interfaceName, primaryKeyGetter);
                writer.beginControlFlow("if (primaryKeyValue != null)");

                if (Utils.isString(metadata.getPrimaryKey())) {
                    writer.emitStatement("rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, (String)primaryKeyValue)");
                } else {
                    writer.emitStatement("rowIndex = Table.nativeFindFirstInt(tableNativePtr, pkColumnIndex, ((%s) object).%s())", interfaceName, primaryKeyGetter);
                }
                writer.endControlFlow();
            }

            writer.beginControlFlow("if (rowIndex == Table.NO_MATCH)");
            if (Utils.isString(metadata.getPrimaryKey())) {
                writer.emitStatement(
                        "rowIndex = OsObject.createRowWithPrimaryKey(realm.sharedRealm, table, primaryKeyValue)");
            } else {
                writer.emitStatement(
                        "rowIndex = OsObject.createRowWithPrimaryKey(realm.sharedRealm, table, ((%s) object).%s())",
                        interfaceName, primaryKeyGetter);
            }

            if (throwIfPrimaryKeyDuplicate) {
                writer.nextControlFlow("else");
                writer.emitStatement("Table.throwDuplicatePrimaryKeyException(primaryKeyValue)");
            }

            writer.endControlFlow();
            writer.emitStatement("cache.put(object, rowIndex)");
        } else {
            writer.emitStatement("long rowIndex = OsObject.createRow(realm.sharedRealm, table)");
            writer.emitStatement("cache.put(object, rowIndex)");
        }
    }

    private void emitCopyMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                qualifiedClassName, // Return type
                "copy", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", qualifiedClassName, "newObject", "boolean", "update", "Map<RealmModel,RealmObjectProxy>", "cache"); // Argument type & argument name

        writer.emitStatement("RealmObjectProxy cachedRealmObject = cache.get(newObject)");
        writer.beginControlFlow("if (cachedRealmObject != null)")
                .emitStatement("return (%s) cachedRealmObject", qualifiedClassName)
                .nextControlFlow("else");

        writer.emitSingleLineComment("rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.");
        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("%s realmObject = realm.createObjectInternal(%s.class, ((%s) newObject).%s(), false, Collections.<String>emptyList())",
                    qualifiedClassName, qualifiedClassName, interfaceName, metadata.getPrimaryKeyGetter());
        } else {
            writer.emitStatement("%s realmObject = realm.createObjectInternal(%s.class, false, Collections.<String>emptyList())",
                    qualifiedClassName, qualifiedClassName);
        }
        writer.emitStatement("cache.put(newObject, (RealmObjectProxy) realmObject)");
        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String fieldType = field.asType().toString();
            String setter = metadata.getInternalSetter(fieldName);
            String getter = metadata.getInternalGetter(fieldName);

            if (metadata.isPrimaryKey(field)) {
                // PK has been set when creating object.
                continue;
            }

            //@formatter:off
            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitStatement("%s %sObj = ((%s) newObject).%s()", fieldType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sObj != null)", fieldName)
                        .emitStatement("%s cache%s = (%s) cache.get(%sObj)", fieldType, fieldName, fieldType, fieldName)
                        .beginControlFlow("if (cache%s != null)", fieldName)
                        .emitStatement("((%s) realmObject).%s(cache%s)", interfaceName, setter, fieldName)
                        .nextControlFlow("else")
                        .emitStatement("((%s) realmObject).%s(%s.copyOrUpdate(realm, %sObj, update, cache))",
                                interfaceName,
                                setter,
                                Utils.getProxyClassSimpleName(field),
                                fieldName)
                        .endControlFlow()
                        .nextControlFlow("else")
                        // No need to throw exception here if the field is not nullable. A exception will be thrown in setter.
                        .emitStatement("((%s) realmObject).%s(null)", interfaceName, setter)
                        .endControlFlow();
            } else if (Utils.isRealmList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                writer
                        .emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = ((%s) newObject).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("RealmList<%s> %sRealmList = ((%s) realmObject).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("for (int i = 0; i < %sList.size(); i++)", fieldName)
                        .emitStatement("%s %sItem = %sList.get(i)", genericType, fieldName, fieldName)
                        .emitStatement("%s cache%s = (%s) cache.get(%sItem)", genericType, fieldName, genericType, fieldName)
                        .beginControlFlow("if (cache%s != null)", fieldName)
                        .emitStatement("%sRealmList.add(cache%s)", fieldName, fieldName)
                        .nextControlFlow("else")
                        .emitStatement("%sRealmList.add(%s.copyOrUpdate(realm, %sList.get(i), update, cache))", fieldName, Utils.getProxyClassSimpleName(field), fieldName)
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow()
                        .emitEmptyLine();

            } else {
                writer.emitStatement("((%s) realmObject).%s(((%s) newObject).%s())",
                        interfaceName, setter, interfaceName, getter);
            }
            //@formatter:on
        }

        writer.emitStatement("return realmObject");
        writer.endControlFlow();
        writer.endMethod();
        writer.emitEmptyLine();
    }

    //@formatter:off
    private void emitCreateDetachedCopyMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                qualifiedClassName, // Return type
                "createDetachedCopy", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                qualifiedClassName, "realmObject", "int", "currentDepth", "int", "maxDepth", "Map<RealmModel, CacheData<RealmModel>>", "cache");
        writer
                .beginControlFlow("if (currentDepth > maxDepth || realmObject == null)")
                .emitStatement("return null")
                .endControlFlow()
                .emitStatement("CacheData<RealmModel> cachedObject = cache.get(realmObject)")
                .emitStatement("%s unmanagedObject", qualifiedClassName)
                .beginControlFlow("if (cachedObject != null)")
                .emitSingleLineComment("Reuse cached object or recreate it because it was encountered at a lower depth.")
                .beginControlFlow("if (currentDepth >= cachedObject.minDepth)")
                .emitStatement("return (%s)cachedObject.object", qualifiedClassName)
                .nextControlFlow("else")
                .emitStatement("unmanagedObject = (%s)cachedObject.object", qualifiedClassName)
                .emitStatement("cachedObject.minDepth = currentDepth")
                .endControlFlow()
                .nextControlFlow("else")
                .emitStatement("unmanagedObject = new %s()", qualifiedClassName)
                .emitStatement("cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject))")
                .endControlFlow();

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String setter = metadata.getInternalSetter(fieldName);
            String getter = metadata.getInternalGetter(fieldName);

            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitSingleLineComment("Deep copy of %s", fieldName)
                        .emitStatement("((%s) unmanagedObject).%s(%s.createDetachedCopy(((%s) realmObject).%s(), currentDepth + 1, maxDepth, cache))",
                                interfaceName, setter, Utils.getProxyClassSimpleName(field), interfaceName, getter);
            } else if (Utils.isRealmList(field)) {
                writer
                        .emitEmptyLine()
                        .emitSingleLineComment("Deep copy of %s", fieldName)
                        .beginControlFlow("if (currentDepth == maxDepth)")
                        .emitStatement("((%s) unmanagedObject).%s(null)", interfaceName, setter)
                        .nextControlFlow("else")
                        .emitStatement("RealmList<%s> managed%sList = ((%s) realmObject).%s()",
                                Utils.getGenericTypeQualifiedName(field), fieldName, interfaceName, getter)
                        .emitStatement("RealmList<%1$s> unmanaged%2$sList = new RealmList<%1$s>()", Utils.getGenericTypeQualifiedName(field), fieldName)
                        .emitStatement("((%s) unmanagedObject).%s(unmanaged%sList)", interfaceName, setter, fieldName)
                        .emitStatement("int nextDepth = currentDepth + 1")
                        .emitStatement("int size = managed%sList.size()", fieldName)
                        .beginControlFlow("for (int i = 0; i < size; i++)")
                        .emitStatement("%s item = %s.createDetachedCopy(managed%sList.get(i), nextDepth, maxDepth, cache)",
                                Utils.getGenericTypeQualifiedName(field), Utils.getProxyClassSimpleName(field), fieldName)
                        .emitStatement("unmanaged%sList.add(item)", fieldName)
                        .endControlFlow()
                        .endControlFlow();
            } else {
                writer.emitStatement("((%s) unmanagedObject).%s(((%s) realmObject).%s())",
                        interfaceName, setter, interfaceName, getter);
            }
        }

        writer.emitStatement("return unmanagedObject");
        writer.endMethod();
        writer.emitEmptyLine();
    }
    //@formatter:on

    private void emitUpdateMethod(JavaWriter writer) throws IOException {
        if (!metadata.hasPrimaryKey()) {
            return;
        }

        writer.beginMethod(
                qualifiedClassName, // Return type
                "update", // Method name
                EnumSet.of(Modifier.STATIC), // Modifiers
                "Realm", "realm", qualifiedClassName, "realmObject", qualifiedClassName, "newObject", "Map<RealmModel, RealmObjectProxy>", "cache"); // Argument type & argument name

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String setter = metadata.getInternalSetter(fieldName);
            String getter = metadata.getInternalGetter(fieldName);
            //@formatter:off
            if (Utils.isRealmModel(field)) {
                writer
                        .emitStatement("%s %sObj = ((%s) newObject).%s()",
                                Utils.getFieldTypeQualifiedName(field), fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sObj != null)", fieldName)
                        .emitStatement("%s cache%s = (%s) cache.get(%sObj)", Utils.getFieldTypeQualifiedName(field), fieldName, Utils.getFieldTypeQualifiedName(field), fieldName)
                        .beginControlFlow("if (cache%s != null)", fieldName)
                        .emitStatement("((%s) realmObject).%s(cache%s)", interfaceName, setter, fieldName)
                        .nextControlFlow("else")
                        .emitStatement("((%s) realmObject).%s(%s.copyOrUpdate(realm, %sObj, true, cache))",
                                interfaceName,
                                setter,
                                Utils.getProxyClassSimpleName(field),
                                fieldName
                        )
                        .endControlFlow()
                        .nextControlFlow("else")
                        // No need to throw exception here if the field is not nullable. A exception will be thrown in setter.
                        .emitStatement("((%s) realmObject).%s(null)", interfaceName, setter)
                        .endControlFlow();
            } else if (Utils.isRealmList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                writer
                        .emitStatement("RealmList<%s> %sList = ((%s) newObject).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .emitStatement("RealmList<%s> %sRealmList = ((%s) realmObject).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .emitStatement("%sRealmList.clear()", fieldName)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .beginControlFlow("for (int i = 0; i < %sList.size(); i++)", fieldName)
                        .emitStatement("%s %sItem = %sList.get(i)", genericType, fieldName, fieldName)
                        .emitStatement("%s cache%s = (%s) cache.get(%sItem)", genericType, fieldName, genericType, fieldName)
                        .beginControlFlow("if (cache%s != null)", fieldName)
                        .emitStatement("%sRealmList.add(cache%s)", fieldName, fieldName)
                        .nextControlFlow("else")
                        .emitStatement("%sRealmList.add(%s.copyOrUpdate(realm, %sList.get(i), true, cache))", fieldName, Utils.getProxyClassSimpleName(field), fieldName)
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow();

            } else {
                if (field == metadata.getPrimaryKey()) {
                    continue;
                }
                writer.emitStatement("((%s) realmObject).%s(((%s) newObject).%s())",
                        interfaceName, setter, interfaceName, getter);
            }
            //@formatter:on
        }

        writer.emitStatement("return realmObject");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitToStringMethod(JavaWriter writer) throws IOException {
        if (metadata.containsToString()) {
            return;
        }
        writer.emitAnnotation("Override");
        writer.emitAnnotation("SuppressWarnings", "\"ArrayToString\"")
                .beginMethod("String", "toString", EnumSet.of(Modifier.PUBLIC))
                .beginControlFlow("if (!RealmObject.isValid(this))")
                .emitStatement("return \"Invalid object\"")
                .endControlFlow();
        writer.emitStatement("StringBuilder stringBuilder = new StringBuilder(\"%s = proxy[\")", simpleClassName);

        Collection<VariableElement> fields = metadata.getFields();
        int i = fields.size() - 1;
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();

            writer.emitStatement("stringBuilder.append(\"{%s:\")", fieldName);
            if (Utils.isRealmModel(field)) {
                String fieldTypeSimpleName = Utils.getFieldTypeSimpleName(field);
                writer.emitStatement(
                        "stringBuilder.append(%s() != null ? \"%s\" : \"null\")",
                        metadata.getInternalGetter(fieldName),
                        fieldTypeSimpleName
                );
            } else if (Utils.isRealmList(field)) {
                String genericTypeSimpleName = Utils.getGenericTypeSimpleName(field);
                writer.emitStatement("stringBuilder.append(\"RealmList<%s>[\").append(%s().size()).append(\"]\")",
                        genericTypeSimpleName,
                        metadata.getInternalGetter(fieldName));
            } else {
                if (metadata.isNullable(field)) {
                    writer.emitStatement("stringBuilder.append(%s() != null ? %s() : \"null\")",
                            metadata.getInternalGetter(fieldName),
                            metadata.getInternalGetter(fieldName)
                    );
                } else {
                    writer.emitStatement("stringBuilder.append(%s())", metadata.getInternalGetter(fieldName));
                }
            }
            writer.emitStatement("stringBuilder.append(\"}\")");

            if (i-- > 0) {
                writer.emitStatement("stringBuilder.append(\",\")");
            }
        }

        writer.emitStatement("stringBuilder.append(\"]\")");
        writer.emitStatement("return stringBuilder.toString()");
        writer.endMethod()
                .emitEmptyLine();
    }

    /**
     * Currently, the hash value emitted from this could suddenly change as an object's index might
     * alternate due to Realm Java using {@code Table#moveLastOver()}. Hash codes should therefore not
     * be considered stable, i.e. don't save them in a HashSet or use them as a key in a HashMap.
     */
    //@formatter:off
    private void emitHashcodeMethod(JavaWriter writer) throws IOException {
        if (metadata.containsHashCode()) {
            return;
        }
        writer.emitAnnotation("Override")
                .beginMethod("int", "hashCode", EnumSet.of(Modifier.PUBLIC))
                .emitStatement("String realmName = proxyState.getRealm$realm().getPath()")
                .emitStatement("String tableName = proxyState.getRow$realm().getTable().getName()")
                .emitStatement("long rowIndex = proxyState.getRow$realm().getIndex()")
                .emitEmptyLine()
                .emitStatement("int result = 17")
                .emitStatement("result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0)")
                .emitStatement("result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0)")
                .emitStatement("result = 31 * result + (int) (rowIndex ^ (rowIndex >>> 32))")
                .emitStatement("return result")
                .endMethod()
                .emitEmptyLine();
    }
    //@formatter:on

    //@formatter:off
    private void emitEqualsMethod(JavaWriter writer) throws IOException {
        if (metadata.containsEquals()) {
            return;
        }
        String proxyClassName = Utils.getProxyClassName(simpleClassName);
        String otherObjectVarName = "a" + simpleClassName;
        writer.emitAnnotation("Override")
                .beginMethod("boolean", "equals", EnumSet.of(Modifier.PUBLIC), "Object", "o")
                .emitStatement("if (this == o) return true")
                .emitStatement("if (o == null || getClass() != o.getClass()) return false")
                .emitStatement("%s %s = (%s)o", proxyClassName, otherObjectVarName, proxyClassName)  // FooRealmProxy aFoo = (FooRealmProxy)o
                .emitEmptyLine()
                .emitStatement("String path = proxyState.getRealm$realm().getPath()")
                .emitStatement("String otherPath = %s.proxyState.getRealm$realm().getPath()", otherObjectVarName)
                .emitStatement("if (path != null ? !path.equals(otherPath) : otherPath != null) return false")
                .emitEmptyLine()
                .emitStatement("String tableName = proxyState.getRow$realm().getTable().getName()")
                .emitStatement("String otherTableName = %s.proxyState.getRow$realm().getTable().getName()", otherObjectVarName)
                .emitStatement("if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false")
                .emitEmptyLine()
                .emitStatement("if (proxyState.getRow$realm().getIndex() != %s.proxyState.getRow$realm().getIndex()) return false", otherObjectVarName)
                .emitEmptyLine()
                .emitStatement("return true")
                .endMethod()
                .emitEmptyLine();
    }
    //@formatter:on

    private void emitCreateOrUpdateUsingJsonObject(JavaWriter writer) throws IOException {
        writer.emitAnnotation("SuppressWarnings", "\"cast\"");
        writer.beginMethod(
                qualifiedClassName,
                "createOrUpdateUsingJsonObject",
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
                Arrays.asList("Realm", "realm", "JSONObject", "json", "boolean", "update"),
                Collections.singletonList("JSONException"));

        final int modelOrListCount = countModelOrListFields(metadata.getFields());
        if (modelOrListCount == 0) {
            writer.emitStatement("final List<String> excludeFields = Collections.<String> emptyList()");
        } else {
            writer.emitStatement("final List<String> excludeFields = new ArrayList<String>(%1$d)",
                    modelOrListCount);
        }

        //@formatter:off
        if (!metadata.hasPrimaryKey()) {
            buildExcludeFieldsList(writer, metadata.getFields());
            writer.emitStatement("%s obj = realm.createObjectInternal(%s.class, true, excludeFields)",
                    qualifiedClassName, qualifiedClassName);
        } else {
            String pkType = Utils.isString(metadata.getPrimaryKey()) ? "String" : "Long";
            writer
                    .emitStatement("%s obj = null", qualifiedClassName)
                    .beginControlFlow("if (update)")
                    .emitStatement("Table table = realm.getTable(%s.class)", qualifiedClassName)
                    .emitStatement("long pkColumnIndex = table.getPrimaryKey()")
                    .emitStatement("long rowIndex = Table.NO_MATCH");
            if (metadata.isNullable(metadata.getPrimaryKey())) {
                writer
                        .beginControlFlow("if (json.isNull(\"%s\"))", metadata.getPrimaryKey().getSimpleName())
                        .emitStatement("rowIndex = table.findFirstNull(pkColumnIndex)")
                        .nextControlFlow("else")
                        .emitStatement("rowIndex = table.findFirst%s(pkColumnIndex, json.get%s(\"%s\"))",
                                pkType, pkType, metadata.getPrimaryKey().getSimpleName())
                        .endControlFlow();
            } else {
                writer
                        .beginControlFlow("if (!json.isNull(\"%s\"))", metadata.getPrimaryKey().getSimpleName())
                        .emitStatement("rowIndex = table.findFirst%s(pkColumnIndex, json.get%s(\"%s\"))",
                                pkType, pkType, metadata.getPrimaryKey().getSimpleName())
                        .endControlFlow();
            }
            writer
                    .beginControlFlow("if (rowIndex != Table.NO_MATCH)")
                    .emitStatement("final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get()")
                    .beginControlFlow("try")
                    .emitStatement("objectContext.set(realm, table.getUncheckedRow(rowIndex)," +
                            " realm.schema.getColumnInfo(%s.class)," +
                            " false, Collections.<String> emptyList())", qualifiedClassName)
                    .emitStatement("obj = new %s()", qualifiedGeneratedClassName)
                    .nextControlFlow("finally")
                    .emitStatement("objectContext.clear()")
                    .endControlFlow()
                    .endControlFlow()
                    .endControlFlow();

            writer.beginControlFlow("if (obj == null)");
            buildExcludeFieldsList(writer, metadata.getFields());
            String primaryKeyFieldType = metadata.getPrimaryKey().asType().toString();
            String primaryKeyFieldName = metadata.getPrimaryKey().getSimpleName().toString();
            RealmJsonTypeHelper.emitCreateObjectWithPrimaryKeyValue(qualifiedClassName, qualifiedGeneratedClassName,
                    primaryKeyFieldType, primaryKeyFieldName, writer);
            writer.endControlFlow();
        }
        //@formatter:on

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String qualifiedFieldType = field.asType().toString();
            if (metadata.isPrimaryKey(field)) {
                // Primary key has already been set when adding new row or finding the existing row.
                continue;
            }
            if (Utils.isRealmModel(field)) {
                RealmJsonTypeHelper.emitFillRealmObjectWithJsonValue(
                        interfaceName,
                        metadata.getInternalSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        Utils.getProxyClassSimpleName(field),
                        writer
                );

            } else if (Utils.isRealmList(field)) {
                RealmJsonTypeHelper.emitFillRealmListWithJsonValue(
                        interfaceName,
                        metadata.getInternalGetter(fieldName),
                        metadata.getInternalSetter(fieldName),
                        fieldName,
                        ((DeclaredType) field.asType()).getTypeArguments().get(0).toString(),
                        Utils.getProxyClassSimpleName(field),
                        writer);

            } else {
                RealmJsonTypeHelper.emitFillJavaTypeWithJsonValue(
                        interfaceName,
                        metadata.getInternalSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        writer
                );
            }
        }

        writer.emitStatement("return obj");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void buildExcludeFieldsList(JavaWriter writer, Collection<VariableElement> fields) throws IOException {
        for (VariableElement field : fields) {
            if (Utils.isRealmModel(field) || Utils.isRealmList(field)) {
                final String fieldName = field.getSimpleName().toString();
                writer.beginControlFlow("if (json.has(\"%1$s\"))", fieldName)
                        .emitStatement("excludeFields.add(\"%1$s\")", fieldName)
                        .endControlFlow();
            }
        }
    }

    // Since we need to check the PK in stream before creating the object, this is now using copyToRealm
    // instead of createObject() to avoid parsing the stream twice.
    private void emitCreateUsingJsonStream(JavaWriter writer) throws IOException {
        writer.emitAnnotation("SuppressWarnings", "\"cast\"");
        writer.emitAnnotation("TargetApi", "Build.VERSION_CODES.HONEYCOMB");
        writer.beginMethod(
                qualifiedClassName,
                "createUsingJsonStream",
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
                Arrays.asList("Realm", "realm", "JsonReader", "reader"),
                Collections.singletonList("IOException"));

        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("boolean jsonHasPrimaryKey = false");
        }
        writer.emitStatement("%s obj = new %s()", qualifiedClassName, qualifiedClassName);
        writer.emitStatement("reader.beginObject()");
        writer.beginControlFlow("while (reader.hasNext())");
        writer.emitStatement("String name = reader.nextName()");
        writer.beginControlFlow("if (false)");

        Collection<VariableElement> fields = metadata.getFields();
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String qualifiedFieldType = field.asType().toString();
            writer.nextControlFlow("else if (name.equals(\"%s\"))", fieldName);

            if (Utils.isRealmModel(field)) {
                RealmJsonTypeHelper.emitFillRealmObjectFromStream(
                        interfaceName,
                        metadata.getInternalSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        Utils.getProxyClassSimpleName(field),
                        writer
                );

            } else if (Utils.isRealmList(field)) {
                RealmJsonTypeHelper.emitFillRealmListFromStream(
                        interfaceName,
                        metadata.getInternalGetter(fieldName),
                        metadata.getInternalSetter(fieldName),
                        ((DeclaredType) field.asType()).getTypeArguments().get(0).toString(),
                        Utils.getProxyClassSimpleName(field),
                        writer);

            } else {
                RealmJsonTypeHelper.emitFillJavaTypeFromStream(
                        interfaceName,
                        metadata,
                        fieldName,
                        qualifiedFieldType,
                        writer
                );
            }
        }

        writer.nextControlFlow("else");
        writer.emitStatement("reader.skipValue()");
        writer.endControlFlow();

        writer.endControlFlow();
        writer.emitStatement("reader.endObject()");

        if (metadata.hasPrimaryKey()) {
            writer.beginControlFlow("if (!jsonHasPrimaryKey)")
                    .emitStatement(Constants.STATEMENT_EXCEPTION_NO_PRIMARY_KEY_IN_JSON, metadata.getPrimaryKey())
                    .endControlFlow();
        }

        writer.emitStatement("obj = realm.copyToRealm(obj)");
        writer.emitStatement("return obj");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitMigrationNeededException(JavaWriter writer, String message, Object... args) throws IOException {
        writer.emitStatement("throw new RealmMigrationNeededException(sharedRealm.getPath(), " + message, args);
    }

    private String columnInfoClassName() {
        return simpleClassName + "ColumnInfo";
    }

    private String columnIndexVarName(VariableElement variableElement) {
        return variableElement.getSimpleName().toString() + "Index";
    }

    private String fieldIndexVariableReference(VariableElement variableElement) {
        return "columnInfo." + columnIndexVarName(variableElement);
    }

    private static int countModelOrListFields(Collection<VariableElement> fields) {
        int count = 0;
        for (VariableElement f : fields) {
            if (Utils.isRealmModel(f) || Utils.isRealmList(f)) {
                count++;
            }
        }
        return count;
    }

    private Constants.RealmFieldType getRealmType(VariableElement field) {
        String fieldTypeCanonicalName = field.asType().toString();
        Constants.RealmFieldType type = Constants.JAVA_TO_REALM_TYPES.get(fieldTypeCanonicalName);
        if (type != null) {
            return type;
        }
        if (Utils.isRealmModel(field)) {
            return Constants.RealmFieldType.OBJECT;
        }
        if (Utils.isRealmList(field)) {
            return Constants.RealmFieldType.LIST;
        }
        return Constants.RealmFieldType.NOTYPE;
    }

    private Constants.RealmFieldType getRealmTypeChecked(VariableElement field) {
        Constants.RealmFieldType type = getRealmType(field);
        if (type == Constants.RealmFieldType.NOTYPE) {
            throw new IllegalStateException("Unsupported type " + field.asType().toString());
        }
        return type;
    }
}
