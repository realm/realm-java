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
import java.util.List;
import java.util.Locale;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;


public class RealmProxyClassGenerator {
    private static final String OPTION_SUPPRESS_WARNINGS = "realm.suppressWarnings";
    private static final String BACKLINKS_FIELD_EXTENSION = "Backlinks";

    private static final List<String> IMPORTS;
    static {
        List<String> l = Arrays.asList(
            "android.annotation.TargetApi",
            "android.os.Build",
            "android.util.JsonReader",
            "android.util.JsonToken",
            "io.realm.exceptions.RealmMigrationNeededException",
            "io.realm.internal.ColumnInfo",
            "io.realm.internal.OsList",
            "io.realm.internal.OsObject",
            "io.realm.internal.OsSchemaInfo",
            "io.realm.internal.OsObjectSchemaInfo",
            "io.realm.internal.Property",
            "io.realm.ProxyUtils",
            "io.realm.internal.RealmObjectProxy",
            "io.realm.internal.Row",
            "io.realm.internal.Table",
            "io.realm.internal.android.JsonUtils",
            "io.realm.log.RealmLog",
            "java.io.IOException",
            "java.util.ArrayList",
            "java.util.Collections",
            "java.util.List",
            "java.util.Iterator",
            "java.util.Date",
            "java.util.Map",
            "java.util.HashMap",
            "org.json.JSONObject",
            "org.json.JSONException",
            "org.json.JSONArray");
        IMPORTS = Collections.unmodifiableList(l);
    }

    private final ProcessingEnvironment processingEnvironment;
    private final TypeMirrors typeMirrors;
    private final ClassMetaData metadata;
    private final ClassCollection classCollection;
    private final String simpleJavaClassName;
    private final String qualifiedJavaClassName;
    private final String internalClassName;
    private final String interfaceName;
    private final String qualifiedGeneratedClassName;
    private final boolean suppressWarnings;

    public RealmProxyClassGenerator(ProcessingEnvironment processingEnvironment, TypeMirrors typeMirrors, ClassMetaData metadata, ClassCollection classes) {
        this.processingEnvironment = processingEnvironment;
        this.typeMirrors = typeMirrors;
        this.metadata = metadata;
        this.classCollection = classes;
        this.simpleJavaClassName = metadata.getSimpleJavaClassName();
        this.qualifiedJavaClassName = metadata.getFullyQualifiedClassName();
        this.internalClassName = metadata.getInternalClassName();
        this.interfaceName = Utils.getProxyInterfaceName(qualifiedJavaClassName);
        this.qualifiedGeneratedClassName = String.format(Locale.US, "%s.%s",
                Constants.REALM_PACKAGE_NAME, Utils.getProxyClassName(qualifiedJavaClassName));

        // See the configuration for the debug build type,
        //  in the realm-library project, for an example of how to set this flag.
        this.suppressWarnings = !"false".equalsIgnoreCase(processingEnvironment.getOptions().get(OPTION_SUPPRESS_WARNINGS));
    }

    public void generate() throws IOException, UnsupportedOperationException {
        JavaFileObject sourceFile = processingEnvironment.getFiler().createSourceFile(qualifiedGeneratedClassName);
        JavaWriter writer = new JavaWriter(new BufferedWriter(sourceFile.openWriter()));

        // Set source code indent
        writer.setIndent(Constants.INDENT);

        writer.emitPackage(Constants.REALM_PACKAGE_NAME)
                .emitEmptyLine();

        List<String> imports = new ArrayList<String>(IMPORTS);
        if (!metadata.getBacklinkFields().isEmpty()) {
            imports.add("io.realm.internal.UncheckedRow");
        }
        writer.emitImports(imports)
                .emitEmptyLine();

        // Begin the class definition
        if (suppressWarnings) {
            writer.emitAnnotation("SuppressWarnings(\"all\")");
        }
        writer
                .beginType(
                qualifiedGeneratedClassName, // full qualified name of the item to generate
                "class",                     // the type of the item
                EnumSet.of(Modifier.PUBLIC), // modifiers to apply
                qualifiedJavaClassName,          // class to extend
                "RealmObjectProxy",          // interfaces to implement
                interfaceName)
                .emitEmptyLine();

        emitColumnInfoClass(writer);

        emitClassFields(writer);

        emitInstanceFields(writer);
        emitConstructor(writer);

        emitInjectContextMethod(writer);
        emitPersistedFieldAccessors(writer);
        emitBacklinkFieldAccessors(writer);
        emitCreateExpectedObjectSchemaInfo(writer);
        emitGetExpectedObjectSchemaInfo(writer);
        emitCreateColumnInfoMethod(writer);
        emitGetSimpleClassNameMethod(writer);
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
                "OsSchemaInfo", "schemaInfo");
        writer.emitStatement("super(%s)", metadata.getFields().size());
        writer.emitStatement("OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo(\"%1$s\")",
                internalClassName);
        for (RealmFieldElement field : metadata.getFields()) {
            writer.emitStatement(
                    "this.%1$sIndex = addColumnDetails(\"%1$s\", \"%2$s\", objectSchemaInfo)",
                    field.getJavaName(),
                    field.getInternalFieldName());
        }
        for (Backlink backlink : metadata.getBacklinkFields()) {
            writer.emitStatement(
                    "addBacklinkDetails(schemaInfo, \"%s\", \"%s\", \"%s\")",
                    backlink.getTargetField(),
                    classCollection.getClassFromQualifiedName(backlink.getSourceClass()).getInternalClassName(),
                    backlink.getSourceField());
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

        writer.endType();
    }

    //@formatter:off
    private void emitClassFields(JavaWriter writer) throws IOException {
        writer.emitEmptyLine()
                .emitField("OsObjectSchemaInfo", "expectedObjectSchemaInfo",
                EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL), "createExpectedObjectSchemaInfo()");
    }
    //@formatter:on

    //@formatter:off
    private void emitInstanceFields(JavaWriter writer) throws IOException {
        writer.emitEmptyLine()
                .emitField(columnInfoClassName(), "columnInfo", EnumSet.of(Modifier.PRIVATE))
                .emitField("ProxyState<" + qualifiedJavaClassName + ">", "proxyState", EnumSet.of(Modifier.PRIVATE));

        for (VariableElement variableElement : metadata.getFields()) {
            if (Utils.isMutableRealmInteger(variableElement)) {
                emitMutableRealmIntegerField(writer, variableElement);
            } else if (Utils.isRealmList(variableElement)) {
                String genericType = Utils.getGenericTypeQualifiedName(variableElement);
                writer.emitField("RealmList<" + genericType + ">", variableElement.getSimpleName().toString() + "RealmList", EnumSet.of(Modifier.PRIVATE));
            }
        }

        for (Backlink backlink : metadata.getBacklinkFields()) {
            writer.emitField(backlink.getTargetFieldType(), backlink.getTargetField() + BACKLINKS_FIELD_EXTENSION,
                    EnumSet.of(Modifier.PRIVATE));
        }
    }
    //@formatter:on

    // The anonymous subclass of MutableRealmInteger.Managed holds a reference to this proxy.
    // Even if all other references to the proxy are dropped, the proxy will not be GCed until
    // the MutableInteger that it owns, also becomes unreachable.
    //@formatter:off
    private void emitMutableRealmIntegerField(JavaWriter writer, VariableElement variableElement) throws IOException{
        writer.emitField("MutableRealmInteger.Managed",
                mutableRealmIntegerFieldName(variableElement),
                EnumSet.of(Modifier.PRIVATE, Modifier.FINAL),
                String.format(
                        "new MutableRealmInteger.Managed<%1$s>() {\n"
                                + "    @Override protected ProxyState<%1$s> getProxyState() { return proxyState; }\n"
                                + "    @Override protected long getColumnIndex() { return columnInfo.%2$s; }\n"
                                + "}",
                        qualifiedJavaClassName, columnIndexVarName(variableElement)));
    }
    //@formatter:on

    //@formatter:off
    private void emitConstructor(JavaWriter writer) throws IOException {
        // FooRealmProxy(ColumnInfo)
        writer.emitEmptyLine()
                .beginConstructor(EnumSet.noneOf(Modifier.class))
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
            } else if (Utils.isMutableRealmInteger(field)) {
                emitMutableRealmInteger(writer, field, fieldName, fieldTypeCanonicalName);
            } else if (Utils.isRealmModel(field)) {
                emitRealmModel(writer, field, fieldName, fieldTypeCanonicalName);
            } else if (Utils.isRealmList(field)) {
                final TypeMirror elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field);
                emitRealmList(writer, field, fieldName, fieldTypeCanonicalName, elementTypeMirror);
            } else {
                throw new UnsupportedOperationException(String.format(Locale.US,
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

    //@formatter:off
    private void emitMutableRealmInteger(JavaWriter writer, VariableElement field, String fieldName, String fieldTypeCanonicalName) throws IOException {
        writer.emitAnnotation("Override")
            .beginMethod(fieldTypeCanonicalName, metadata.getInternalGetter(fieldName), EnumSet.of(Modifier.PUBLIC))
                .emitStatement("proxyState.getRealm$realm().checkIfValid()")
                .emitStatement("return this.%s", mutableRealmIntegerFieldName(field))
            .endMethod();
    }
    //@formatter:on

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
                writer.emitStatement("proxyState.checkValidObject(value)");
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
                .emitStatement("proxyState.checkValidObject(value)")
                .emitStatement("proxyState.getRow$realm().setLink(%s, ((RealmObjectProxy) value).realmGet$proxyState().getRow$realm().getIndex())", fieldIndexVariableReference(field))
                .endMethod();
    }
    //@formatter:on

    /**
     * ModelList, ValueList
     */
    //@formatter:off
    private void emitRealmList(
            JavaWriter writer,
            final VariableElement field,
            String fieldName,
            String fieldTypeCanonicalName,
            final TypeMirror elementTypeMirror) throws IOException {
        final String genericType = Utils.getGenericTypeQualifiedName(field);
        final boolean forRealmModel = Utils.isRealmModel(elementTypeMirror);

        // Getter
        writer.emitAnnotation("Override");
        writer.beginMethod(fieldTypeCanonicalName, metadata.getInternalGetter(fieldName), EnumSet.of(Modifier.PUBLIC))
                .emitStatement("proxyState.getRealm$realm().checkIfValid()")
                .emitSingleLineComment("use the cached value if available")
                .beginControlFlow("if (" + fieldName + "RealmList != null)")
                .emitStatement("return " + fieldName + "RealmList")
                .nextControlFlow("else");
                if (Utils.isRealmModelList(field)) {
                    writer.emitStatement("OsList osList = proxyState.getRow$realm().getModelList(%s)",
                            fieldIndexVariableReference(field));
                } else {
                    writer.emitStatement("OsList osList = proxyState.getRow$realm().getValueList(%1$s, RealmFieldType.%2$s)",
                            fieldIndexVariableReference(field), Utils.getValueListFieldType(field).name());
                }
                writer.emitStatement(fieldName + "RealmList = new RealmList<%s>(%s.class, osList, proxyState.getRealm$realm())",
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

                if (!forRealmModel) {
                    return;
                }

                writer.emitSingleLineComment("if the list contains unmanaged RealmObjects, convert them to managed.")
                        .beginControlFlow("if (value != null && !value.isManaged())")
                        .emitStatement("final Realm realm = (Realm) proxyState.getRealm$realm()")
                        .emitStatement("final RealmList<%1$s> original = value", genericType)
                        .emitStatement("value = new RealmList<%1$s>()", genericType)
                        .beginControlFlow("for (%1$s item : original)", genericType)
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

        writer.emitStatement("proxyState.getRealm$realm().checkIfValid()");
        if (Utils.isRealmModelList(field)) {
            writer.emitStatement("OsList osList = proxyState.getRow$realm().getModelList(%s)",
                    fieldIndexVariableReference(field));
        } else {
            writer.emitStatement("OsList osList = proxyState.getRow$realm().getValueList(%1$s, RealmFieldType.%2$s)",
                    fieldIndexVariableReference(field), Utils.getValueListFieldType(field).name());
        }
        if (forRealmModel) {
            // Model lists.
            writer
                .emitSingleLineComment("For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.")
                .beginControlFlow("if (value != null && value.size() == osList.size())")
                    .emitStatement("int objects = value.size()")
                    .beginControlFlow("for (int i = 0; i < objects; i++)")
                        .emitStatement("%s linkedObject = value.get(i)", genericType)
                        .emitStatement("proxyState.checkValidObject(linkedObject)")
                        .emitStatement("osList.setRow(i, ((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getIndex())")
                    .endControlFlow()
                .nextControlFlow("else")
                    .emitStatement("osList.removeAll()")
                    .beginControlFlow("if (value == null)")
                        .emitStatement("return")
                    .endControlFlow()
                    .emitStatement("int objects = value.size()")
                    .beginControlFlow("for (int i = 0; i < objects; i++)")
                        .emitStatement("%s linkedObject = value.get(i)", genericType)
                        .emitStatement("proxyState.checkValidObject(linkedObject)")
                        .emitStatement("osList.addRow(((RealmObjectProxy) linkedObject).realmGet$proxyState().getRow$realm().getIndex())")
                    .endControlFlow()
                .endControlFlow();
        } else {
            // Value lists
            writer
                .emitStatement("osList.removeAll()")
                .beginControlFlow("if (value == null)")
                    .emitStatement("return")
                .endControlFlow()
                .beginControlFlow("for (%1$s item : value)", genericType)
                    .beginControlFlow("if (item == null)")
                        .emitStatement(metadata.isElementNullable(field) ? "osList.addNull()" : "throw new IllegalArgumentException(\"Storing 'null' into " + fieldName + "' is not allowed by the schema.\")")
                    .nextControlFlow("else")
                        .emitStatement(getStatementForAppendingValueToOsList("osList", "item", elementTypeMirror))
                    .endControlFlow()
                .endControlFlow();
        }
        writer.endMethod();

    }
    //@formatter:on

    private String getStatementForAppendingValueToOsList(
            @SuppressWarnings("SameParameterValue") String osListVariableName,
            @SuppressWarnings("SameParameterValue") String valueVariableName,
            TypeMirror elementTypeMirror) {
        if (elementTypeMirror == typeMirrors.STRING_MIRROR) {
            return osListVariableName + ".addString(" + valueVariableName + ")";
        }
        if (elementTypeMirror == typeMirrors.LONG_MIRROR || elementTypeMirror == typeMirrors.INTEGER_MIRROR
                || elementTypeMirror == typeMirrors.SHORT_MIRROR || elementTypeMirror == typeMirrors.BYTE_MIRROR) {
            return osListVariableName + ".addLong(" + valueVariableName + ".longValue())";
        }
        if (elementTypeMirror.equals(typeMirrors.BINARY_MIRROR)) {
            return osListVariableName + ".addBinary(" + valueVariableName + ")";
        }
        if (elementTypeMirror == typeMirrors.DATE_MIRROR) {
            return osListVariableName + ".addDate(" + valueVariableName + ")";
        }
        if (elementTypeMirror == typeMirrors.BOOLEAN_MIRROR) {
            return osListVariableName + ".addBoolean(" + valueVariableName + ")";
        }
        if (elementTypeMirror == typeMirrors.DOUBLE_MIRROR) {
            return osListVariableName + ".addDouble(" + valueVariableName + ".doubleValue())";
        }
        if (elementTypeMirror == typeMirrors.FLOAT_MIRROR) {
            return osListVariableName + ".addFloat(" + valueVariableName + ".floatValue())";
        }
        throw new RuntimeException("unexpected element type: " + elementTypeMirror.toString());
    }

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

    // Note that because of bytecode hackery, this method may run before the constructor!
    // It may even run before fields have been initialized.
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
                .emitStatement("this.proxyState = new ProxyState<%1$s>(this)", qualifiedJavaClassName)
                .emitStatement("proxyState.setRealm$realm(context.getRealm())")
                .emitStatement("proxyState.setRow$realm(context.getRow())")
                .emitStatement("proxyState.setAcceptDefaultValue$realm(context.getAcceptDefaultValue())")
                .emitStatement("proxyState.setExcludeFields$realm(context.getExcludeFields())")
                .endMethod()
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

        // Guess capacity for Arrays used by OsObjectSchemaInfo.
        // Used to prevent array resizing at runtime
        int persistedFields = metadata.getFields().size();
        int computedFields = metadata.getBacklinkFields().size();

        writer.emitStatement(
                "OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder(\"%s\", %s, %s)",
                internalClassName, persistedFields, computedFields);

        // For each field generate corresponding table index constant
        for (RealmFieldElement field : metadata.getFields()) {
            String fieldName = field.getInternalFieldName();

            Constants.RealmFieldType fieldType = getRealmTypeChecked(field);
            switch (fieldType) {
                case NOTYPE: {
                    // Perhaps this should fail quickly?
                    break;
                }
                case OBJECT: {
                    String fieldTypeQualifiedName = Utils.getFieldTypeQualifiedName(field);
                    String internalClassName = Utils.getReferencedTypeInternalClassNameStatement(fieldTypeQualifiedName, classCollection);
                    writer.emitStatement("builder.addPersistedLinkProperty(\"%s\", RealmFieldType.OBJECT, %s)",
                            fieldName, internalClassName);
                    break;
                }
                case LIST: {
                    String genericTypeQualifiedName = Utils.getGenericTypeQualifiedName(field);
                    String internalClassName = Utils.getReferencedTypeInternalClassNameStatement(genericTypeQualifiedName, classCollection);
                    writer.emitStatement("builder.addPersistedLinkProperty(\"%s\", RealmFieldType.LIST, %s)",
                            fieldName, internalClassName);
                    break;
                }
                case INTEGER_LIST:
                case BOOLEAN_LIST:
                case STRING_LIST:
                case BINARY_LIST:
                case DATE_LIST:
                case FLOAT_LIST:
                case DOUBLE_LIST:
                    writer.emitStatement("builder.addPersistedValueListProperty(\"%s\", %s, %s)",
                            fieldName, fieldType.getRealmType(), metadata.isElementNullable(field) ? "!Property.REQUIRED" : "Property.REQUIRED");
                    break;

                case BACKLINK:
                    throw new IllegalArgumentException("LinkingObject field should not be added to metadata");

                case INTEGER:
                case FLOAT:
                case DOUBLE:
                case BOOLEAN:
                case STRING:
                case DATE:
                case BINARY:
                case REALM_INTEGER:
                    String nullableFlag = (metadata.isNullable(field) ? "!" : "") + "Property.REQUIRED";
                    String indexedFlag = (metadata.isIndexed(field) ? "" : "!") + "Property.INDEXED";
                    String primaryKeyFlag = (metadata.isPrimaryKey(field) ? "" : "!") + "Property.PRIMARY_KEY";
                    writer.emitStatement("builder.addPersistedProperty(\"%s\", %s, %s, %s, %s)",
                            fieldName,
                            fieldType.getRealmType(),
                            primaryKeyFlag,
                            indexedFlag,
                            nullableFlag);
                    break;

                default:
                    throw new IllegalArgumentException("'fieldType' " + fieldName + " is not handled");
            }
        }
        for (Backlink backlink: metadata.getBacklinkFields()) {
            // Backlinks can only be created between classes in the current round of annotation processing
            // as the forward link cannot be created unless you know the type already.
            ClassMetaData sourceClass = classCollection.getClassFromQualifiedName(backlink.getSourceClass());
            String targetField = backlink.getTargetField(); // Only in the model, so no internal name exists
            String internalSourceField = sourceClass.getInternalFieldName(backlink.getSourceField());
            writer.emitStatement("builder.addComputedLinkProperty(\"%s\", \"%s\", \"%s\")",
                    targetField, sourceClass.getInternalClassName(), internalSourceField);
        }
        writer.emitStatement("return builder.build()");
        writer.endMethod()
                .emitEmptyLine();
    }

    private void emitGetExpectedObjectSchemaInfo(JavaWriter writer) throws IOException {
        writer.beginMethod(
                "OsObjectSchemaInfo", // Return type
                "getExpectedObjectSchemaInfo", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC)); // Modifiers

        writer.emitStatement("return expectedObjectSchemaInfo");

        writer.endMethod()
                .emitEmptyLine();
    }

    private void emitCreateColumnInfoMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                columnInfoClassName(),        // Return type
                "createColumnInfo",              // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "OsSchemaInfo", "schemaInfo"); // Argument type & argument name

        // create an instance of ColumnInfo
        writer.emitStatement("return new %1$s(schemaInfo)", columnInfoClassName());

        writer.endMethod();
        writer.emitEmptyLine();
    }

    //@formatter:off
    private void emitGetSimpleClassNameMethod(JavaWriter writer) throws IOException {
        writer.beginMethod("String", "getSimpleClassName", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .emitStatement("return \"%s\"", internalClassName)
                .endMethod()
                .emitEmptyLine();

        // Helper class for the annotation processor so it can access the internal class name
        // without needing to load the parent class (which we cannot do as it transitively loads
        // native code, which cannot be loaded on the JVM).
        writer.beginType(
                "ClassNameHelper",                       // full qualified name of the item to generate
                "class",                                                  // the type of the item
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)); // modifiers to apply
        writer.emitField("String", "INTERNAL_CLASS_NAME", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL), "\""+ internalClassName+"\"");
        writer.endType();
        writer.emitEmptyLine();
    }
    //@formatter:on

    //@formatter:off
    private void emitCopyOrUpdateMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                qualifiedJavaClassName, // Return type
                "copyOrUpdate", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", qualifiedJavaClassName, "object", "boolean", "update", "Map<RealmModel,RealmObjectProxy>", "cache" // Argument type & argument name
        );

        writer
                .beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null)")
                    .emitStatement("final BaseRealm otherRealm = ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm()")
                    .beginControlFlow("if (otherRealm.threadId != realm.threadId)")
                        .emitStatement("throw new IllegalArgumentException(\"Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.\")")
                    .endControlFlow()

                    // If object is already in the Realm there is nothing to update
                    .beginControlFlow("if (otherRealm.getPath().equals(realm.getPath()))")
                        .emitStatement("return object")
                    .endControlFlow()
                .endControlFlow();


        writer.emitStatement("final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get()");

        writer.emitStatement("RealmObjectProxy cachedRealmObject = cache.get(object)")
                .beginControlFlow("if (cachedRealmObject != null)")
                    .emitStatement("return (%s) cachedRealmObject", qualifiedJavaClassName)
                .endControlFlow()
                .emitEmptyLine();

        if (!metadata.hasPrimaryKey()) {
            writer.emitStatement("return copy(realm, object, update, cache)");
        } else {
            writer
                    .emitStatement("%s realmObject = null", qualifiedJavaClassName)
                    .emitStatement("boolean canUpdate = update")
                    .beginControlFlow("if (canUpdate)")
                    .emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName)
                    .emitStatement("%s columnInfo = (%s) realm.getSchema().getColumnInfo(%s.class)",
                        columnInfoClassName(), columnInfoClassName(), qualifiedJavaClassName)
                    .emitStatement("long pkColumnIndex = %s", fieldIndexVariableReference(metadata.getPrimaryKey()));

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
                    .beginControlFlow("if (rowIndex == Table.NO_MATCH)")
                        .emitStatement("canUpdate = false")
                    .nextControlFlow("else")
                        .beginControlFlow("try")
                            .emitStatement(
                                "objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.getSchema().getColumnInfo(%s.class), false, Collections.<String> emptyList())",
                                    qualifiedJavaClassName)
                            .emitStatement("realmObject = new %s()", qualifiedGeneratedClassName)
                            .emitStatement("cache.put(object, (RealmObjectProxy) realmObject)")
                        .nextControlFlow("finally")
                            .emitStatement("objectContext.clear()")
                        .endControlFlow()
                    .endControlFlow();

            writer.endControlFlow();

            writer
                    .emitEmptyLine()
                       .emitStatement("return (canUpdate) ? update(realm, realmObject, object, cache) : copy(realm, object, update, cache)");
        }

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
            writer.emitStatement("Table.nativeSetLong(tableNativePtr, columnInfo.%sIndex, rowIndex, ((%s) object).%s(), false)", fieldName, interfaceName, getter);

        } else if ("java.lang.Long".equals(fieldType)
                || "java.lang.Integer".equals(fieldType)
                || "java.lang.Short".equals(fieldType)
                || "java.lang.Byte".equals(fieldType)) {
            writer
                    .emitStatement("Number %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetLong(tableNativePtr, columnInfo.%sIndex, rowIndex, %s.longValue(), false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();

        } else if ("io.realm.MutableRealmInteger".equals(fieldType)) {
            writer
                    .emitStatement("Long %s = ((%s) object).%s().get()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetLong(tableNativePtr, columnInfo.%sIndex, rowIndex, %s.longValue(), false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();

        } else if ("double".equals(fieldType)) {
            writer.emitStatement("Table.nativeSetDouble(tableNativePtr, columnInfo.%sIndex, rowIndex, ((%s) object).%s(), false)", fieldName, interfaceName, getter);

        } else if ("java.lang.Double".equals(fieldType)) {
            writer
                    .emitStatement("Double %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetDouble(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();

        } else if ("float".equals(fieldType)) {
            writer.emitStatement("Table.nativeSetFloat(tableNativePtr, columnInfo.%sIndex, rowIndex, ((%s) object).%s(), false)", fieldName, interfaceName, getter);

        } else if ("java.lang.Float".equals(fieldType)) {
            writer
                    .emitStatement("Float %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetFloat(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();

        } else if ("boolean".equals(fieldType)) {
            writer.emitStatement("Table.nativeSetBoolean(tableNativePtr, columnInfo.%sIndex, rowIndex, ((%s) object).%s(), false)", fieldName, interfaceName, getter);

        } else if ("java.lang.Boolean".equals(fieldType)) {
            writer
                    .emitStatement("Boolean %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetBoolean(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();

        } else if ("byte[]".equals(fieldType)) {
            writer
                    .emitStatement("byte[] %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetByteArray(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();


        } else if ("java.util.Date".equals(fieldType)) {
            writer
                    .emitStatement("java.util.Date %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetTimestamp(tableNativePtr, columnInfo.%sIndex, rowIndex, %s.getTime(), false)", fieldName, getter);
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName);
            }
            writer.endControlFlow();

        } else if ("java.lang.String".equals(fieldType)) {
            writer
                    .emitStatement("String %s = ((%s) object).%s()", getter, interfaceName, getter)
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
                "Realm", "realm", qualifiedJavaClassName, "object", "Map<RealmModel,Long>", "cache" // Argument type & argument name
        );

        // If object is already in the Realm there is nothing to update
        writer
                .beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath()))")
                .emitStatement("return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex()")
                .endControlFlow();

        writer.emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName);
        writer.emitStatement("long tableNativePtr = table.getNativePtr()");
        writer.emitStatement("%s columnInfo = (%s) realm.getSchema().getColumnInfo(%s.class)",
                columnInfoClassName(), columnInfoClassName(), qualifiedJavaClassName);

        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("long pkColumnIndex = %s", fieldIndexVariableReference(metadata.getPrimaryKey()));
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
            } else if (Utils.isRealmModelList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                writer
                        .emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("OsList %1$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1$sIndex)", fieldName)
                        .beginControlFlow("for (%1$s %2$sItem : %2$sList)", genericType, fieldName)
                        .emitStatement("Long cacheItemIndex%1$s = cache.get(%1$sItem)", fieldName)
                        .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                        .emitStatement("cacheItemIndex%1$s = %2$s.insert(realm, %1$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("%1$sOsList.addRow(cacheItemIndex%1$s)", fieldName)
                        .endControlFlow()
                        .endControlFlow();
            } else if (Utils.isRealmValueList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                final TypeMirror elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field);
                writer
                        .emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("OsList %1$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1$sIndex)", fieldName)
                        .beginControlFlow("for (%1$s %2$sItem : %2$sList)", genericType, fieldName)
                        .beginControlFlow("if (%1$sItem == null)", fieldName)
                        .emitStatement(fieldName + "OsList.addNull()")
                        .nextControlFlow("else")
                        .emitStatement(getStatementForAppendingValueToOsList(fieldName + "OsList", fieldName + "Item", elementTypeMirror))
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow();
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

        writer.emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName);
        writer.emitStatement("long tableNativePtr = table.getNativePtr()");
        writer.emitStatement("%s columnInfo = (%s) realm.getSchema().getColumnInfo(%s.class)",
                columnInfoClassName(), columnInfoClassName(), qualifiedJavaClassName);
        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("long pkColumnIndex = %s", fieldIndexVariableReference(metadata.getPrimaryKey()));
        }
        writer.emitStatement("%s object = null", qualifiedJavaClassName);

        writer.beginControlFlow("while (objects.hasNext())")
                .emitStatement("object = (%s) objects.next()", qualifiedJavaClassName);
        writer.beginControlFlow("if (cache.containsKey(object))")
                .emitStatement("continue")
                .endControlFlow();

        writer.beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath()))");
        writer.emitStatement("cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex())")
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
            } else if (Utils.isRealmModelList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                writer
                        .emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("OsList %1$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1$sIndex)", fieldName)
                        .beginControlFlow("for (%1$s %2$sItem : %2$sList)", genericType, fieldName)
                        .emitStatement("Long cacheItemIndex%1$s = cache.get(%1$sItem)", fieldName)
                        .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                        .emitStatement("cacheItemIndex%1$s = %2$s.insert(realm, %1$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("%1$sOsList.addRow(cacheItemIndex%1$s)", fieldName)
                        .endControlFlow()
                        .endControlFlow();

            } else if (Utils.isRealmValueList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                final TypeMirror elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field);
                writer
                        .emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("OsList %1$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1$sIndex)", fieldName)
                        .beginControlFlow("for (%1$s %2$sItem : %2$sList)", genericType, fieldName)
                        .beginControlFlow("if (%1$sItem == null)", fieldName)
                        .emitStatement("%1$sOsList.addNull()", fieldName)
                        .nextControlFlow("else")
                        .emitStatement(getStatementForAppendingValueToOsList(fieldName + "OsList", fieldName + "Item", elementTypeMirror))
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow();
            } else {
                if (metadata.getPrimaryKey() != field) {
                    setTableValues(writer, fieldType, fieldName, interfaceName, getter, false);
                }
            }
        }
        //@formatter:on

        writer.endControlFlow();
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private void emitInsertOrUpdateMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                "long", // Return type
                "insertOrUpdate", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", qualifiedJavaClassName, "object", "Map<RealmModel,Long>", "cache" // Argument type & argument name
        );

        // If object is already in the Realm there is nothing to update
        writer
                .beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath()))")
                .emitStatement("return ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex()")
                .endControlFlow();

        writer.emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName);
        writer.emitStatement("long tableNativePtr = table.getNativePtr()");
        writer.emitStatement("%s columnInfo = (%s) realm.getSchema().getColumnInfo(%s.class)",
                columnInfoClassName(), columnInfoClassName(), qualifiedJavaClassName);

        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("long pkColumnIndex = %s", fieldIndexVariableReference(metadata.getPrimaryKey()));
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
            } else if (Utils.isRealmModelList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                writer
                    .emitEmptyLine()
                    .emitStatement("OsList %1$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1$sIndex)", fieldName)
                    .emitStatement("RealmList<%s> %sList = ((%s) object).%s()", genericType, fieldName, interfaceName, getter)
                    .beginControlFlow("if (%1$sList != null && %1$sList.size() == %1$sOsList.size())", fieldName)
                        .emitSingleLineComment("For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.")
                        .emitStatement("int objects = %1$sList.size()", fieldName)
                        .beginControlFlow("for (int i = 0; i < objects; i++)")
                            .emitStatement("%1$s %2$sItem = %2$sList.get(i)", genericType, fieldName)
                            .emitStatement("Long cacheItemIndex%1$s = cache.get(%1$sItem)", fieldName)
                            .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                                .emitStatement("cacheItemIndex%1$s = %2$s.insertOrUpdate(realm, %1$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                            .endControlFlow()
                            .emitStatement("%1$sOsList.setRow(i, cacheItemIndex%1$s)", fieldName)
                    .endControlFlow()
                    .nextControlFlow("else")
                        .emitStatement("%1$sOsList.removeAll()", fieldName)
                        .beginControlFlow("if (%sList != null)", fieldName)
                            .beginControlFlow("for (%1$s %2$sItem : %2$sList)", genericType, fieldName)
                                .emitStatement("Long cacheItemIndex%1$s = cache.get(%1$sItem)", fieldName)
                                .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                                    .emitStatement("cacheItemIndex%1$s = %2$s.insertOrUpdate(realm, %1$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                                .endControlFlow()
                                .emitStatement("%1$sOsList.addRow(cacheItemIndex%1$s)", fieldName)
                            .endControlFlow()
                        .endControlFlow()
                    .endControlFlow()
                    .emitEmptyLine();

            } else if (Utils.isRealmValueList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                final TypeMirror elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field);
                writer
                        .emitEmptyLine()
                        .emitStatement("OsList %1$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1$sIndex)", fieldName)
                        .emitStatement("%1$sOsList.removeAll()", fieldName)
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .beginControlFlow("for (%1$s %2$sItem : %2$sList)", genericType, fieldName)
                        .beginControlFlow("if (%1$sItem == null)", fieldName)
                        .emitStatement("%1$sOsList.addNull()", fieldName)
                        .nextControlFlow("else")
                        .emitStatement(getStatementForAppendingValueToOsList(fieldName + "OsList", fieldName + "Item", elementTypeMirror))
                        .endControlFlow()
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

        writer.emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName);
        writer.emitStatement("long tableNativePtr = table.getNativePtr()");
        writer.emitStatement("%s columnInfo = (%s) realm.getSchema().getColumnInfo(%s.class)",
                columnInfoClassName(), columnInfoClassName(), qualifiedJavaClassName);
        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("long pkColumnIndex = %s", fieldIndexVariableReference(metadata.getPrimaryKey()));
        }
        writer.emitStatement("%s object = null", qualifiedJavaClassName);

        writer.beginControlFlow("while (objects.hasNext())");
        writer.emitStatement("object = (%s) objects.next()", qualifiedJavaClassName);
        writer.beginControlFlow("if (cache.containsKey(object))")
                .emitStatement("continue")
                .endControlFlow();

        writer.beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm() != null && ((RealmObjectProxy) object).realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath()))");
        writer.emitStatement("cache.put(object, ((RealmObjectProxy) object).realmGet$proxyState().getRow$realm().getIndex())")
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
            } else if (Utils.isRealmModelList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                writer
                    .emitEmptyLine()
                    .emitStatement("OsList %1$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1$sIndex)", fieldName)
                    .emitStatement("RealmList<%s> %sList = ((%s) object).%s()", genericType, fieldName, interfaceName, getter)
                    .beginControlFlow("if (%1$sList != null && %1$sList.size() == %1$sOsList.size())", fieldName)
                        .emitSingleLineComment("For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.")
                        .emitStatement("int objectCount = %1$sList.size()", fieldName)
                        .beginControlFlow("for (int i = 0; i < objectCount; i++)")
                            .emitStatement("%1$s %2$sItem = %2$sList.get(i)", genericType, fieldName)
                            .emitStatement("Long cacheItemIndex%1$s = cache.get(%1$sItem)", fieldName)
                            .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                                .emitStatement("cacheItemIndex%1$s = %2$s.insertOrUpdate(realm, %1$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                            .endControlFlow()
                            .emitStatement("%1$sOsList.setRow(i, cacheItemIndex%1$s)", fieldName)
                        .endControlFlow()
                    .nextControlFlow("else")
                        .emitStatement("%1$sOsList.removeAll()", fieldName)
                        .beginControlFlow("if (%sList != null)", fieldName)
                            .beginControlFlow("for (%1$s %2$sItem : %2$sList)", genericType, fieldName)
                                .emitStatement("Long cacheItemIndex%1$s = cache.get(%1$sItem)", fieldName)
                                .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                                    .emitStatement("cacheItemIndex%1$s = %2$s.insertOrUpdate(realm, %1$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                                .endControlFlow()
                                .emitStatement("%1$sOsList.addRow(cacheItemIndex%1$s)", fieldName)
                            .endControlFlow()
                        .endControlFlow()
                    .endControlFlow()
                    .emitEmptyLine();

            } else if (Utils.isRealmValueList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                final TypeMirror elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field);
                writer
                        .emitEmptyLine()
                        .emitStatement("OsList %1$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1$sIndex)", fieldName)
                        .emitStatement("%1$sOsList.removeAll()", fieldName)
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .beginControlFlow("for (%1$s %2$sItem : %2$sList)", genericType, fieldName)
                        .beginControlFlow("if (%1$sItem == null)", fieldName)
                        .emitStatement("%1$sOsList.addNull()", fieldName)
                        .nextControlFlow("else")
                        .emitStatement(getStatementForAppendingValueToOsList(fieldName + "OsList",
                                fieldName + "Item", elementTypeMirror))
                        .endControlFlow()
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
                        "rowIndex = OsObject.createRowWithPrimaryKey(table, pkColumnIndex, primaryKeyValue)");
            } else {
                writer.emitStatement(
                        "rowIndex = OsObject.createRowWithPrimaryKey(table, pkColumnIndex, ((%s) object).%s())",
                        interfaceName, primaryKeyGetter);
            }

            if (throwIfPrimaryKeyDuplicate) {
                writer.nextControlFlow("else");
                writer.emitStatement("Table.throwDuplicatePrimaryKeyException(primaryKeyValue)");
            }

            writer.endControlFlow();
            writer.emitStatement("cache.put(object, rowIndex)");
        } else {
            writer.emitStatement("long rowIndex = OsObject.createRow(table)");
            writer.emitStatement("cache.put(object, rowIndex)");
        }
    }

    private void emitCopyMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                qualifiedJavaClassName, // Return type
                "copy", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", qualifiedJavaClassName, "newObject", "boolean", "update", "Map<RealmModel,RealmObjectProxy>", "cache"); // Argument type & argument name

        writer.emitStatement("RealmObjectProxy cachedRealmObject = cache.get(newObject)");
        writer.beginControlFlow("if (cachedRealmObject != null)")
                .emitStatement("return (%s) cachedRealmObject", qualifiedJavaClassName)
                .endControlFlow();


        writer.emitEmptyLine()
                .emitSingleLineComment("rejecting default values to avoid creating unexpected objects from RealmModel/RealmList fields.");
        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("%s realmObject = realm.createObjectInternal(%s.class, ((%s) newObject).%s(), false, Collections.<String>emptyList())",
                    qualifiedJavaClassName, qualifiedJavaClassName, interfaceName, metadata.getPrimaryKeyGetter());
        } else {
            writer.emitStatement("%s realmObject = realm.createObjectInternal(%s.class, false, Collections.<String>emptyList())",
                    qualifiedJavaClassName, qualifiedJavaClassName);
        }
        writer.emitStatement("cache.put(newObject, (RealmObjectProxy) realmObject)");

        writer.emitEmptyLine()
                .emitStatement("%1$s realmObjectSource = (%1$s) newObject", interfaceName)
                .emitStatement("%1$s realmObjectCopy = (%1$s) realmObject", interfaceName);

        writer.emitEmptyLine();
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
                writer.emitEmptyLine()
                        .emitStatement("%s %sObj = realmObjectSource.%s()", fieldType, fieldName, getter)
                        .beginControlFlow("if (%sObj == null)", fieldName)
                            .emitStatement("realmObjectCopy.%s(null)", setter)
                        .nextControlFlow("else")
                            .emitStatement("%s cache%s = (%s) cache.get(%sObj)", fieldType, fieldName, fieldType, fieldName)
                            .beginControlFlow("if (cache%s != null)", fieldName)
                                .emitStatement("realmObjectCopy.%s(cache%s)", setter, fieldName)
                            .nextControlFlow("else")
                                .emitStatement("realmObjectCopy.%s(%s.copyOrUpdate(realm, %sObj, update, cache))",
                                    setter, Utils.getProxyClassSimpleName(field), fieldName)
                            .endControlFlow()
                        // No need to throw exception here if the field is not nullable. A exception will be thrown in setter.
                        .endControlFlow();
            } else if (Utils.isRealmModelList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                writer.emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = realmObjectSource.%s()", genericType, fieldName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                            .emitStatement("RealmList<%s> %sRealmList = realmObjectCopy.%s()",
                                genericType, fieldName, getter)
                             // Clear is needed. See bug https://github.com/realm/realm-java/issues/4957
                            .emitStatement("%sRealmList.clear()", fieldName)
                            .beginControlFlow("for (int i = 0; i < %sList.size(); i++)", fieldName)
                                .emitStatement("%1$s %2$sItem = %2$sList.get(i)", genericType, fieldName)
                                .emitStatement("%1$s cache%2$s = (%1$s) cache.get(%2$sItem)", genericType, fieldName)
                                .beginControlFlow("if (cache%s != null)", fieldName)
                                    .emitStatement("%1$sRealmList.add(cache%1$s)", fieldName)
                                .nextControlFlow("else")
                                    .emitStatement("%1$sRealmList.add(%2$s.copyOrUpdate(realm, %1$sItem, update, cache))",
                                        fieldName, Utils.getProxyClassSimpleName(field))
                                .endControlFlow()
                            .endControlFlow()
                        .endControlFlow()
                        .emitEmptyLine();

            } else if (Utils.isRealmValueList(field)) {
                writer.emitStatement("realmObjectCopy.%s(realmObjectSource.%s())", setter, getter);
            } else if (Utils.isMutableRealmInteger(field)) {
                writer.emitEmptyLine()
                        .emitStatement("realmObjectCopy.%1$s().set(realmObjectSource.%1$s().get())", getter);
            } else {
                writer.emitStatement("realmObjectCopy.%s(realmObjectSource.%s())", setter, getter);
            }
            //@formatter:on
        }

        writer.emitStatement("return realmObject");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    //@formatter:off
    private void emitCreateDetachedCopyMethod(JavaWriter writer) throws IOException {
        writer.beginMethod(
                qualifiedJavaClassName, // Return type
                "createDetachedCopy", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                qualifiedJavaClassName, "realmObject", "int", "currentDepth", "int", "maxDepth", "Map<RealmModel, CacheData<RealmModel>>", "cache");
        writer
                .beginControlFlow("if (currentDepth > maxDepth || realmObject == null)")
                .emitStatement("return null")
                .endControlFlow()
                .emitStatement("CacheData<RealmModel> cachedObject = cache.get(realmObject)")
                .emitStatement("%s unmanagedObject", qualifiedJavaClassName)
                .beginControlFlow("if (cachedObject == null)")
                .emitStatement("unmanagedObject = new %s()", qualifiedJavaClassName)
                .emitStatement("cache.put(realmObject, new RealmObjectProxy.CacheData<RealmModel>(currentDepth, unmanagedObject))")
                .nextControlFlow("else")
                .emitSingleLineComment("Reuse cached object or recreate it because it was encountered at a lower depth.")
                .beginControlFlow("if (currentDepth >= cachedObject.minDepth)")
                .emitStatement("return (%s) cachedObject.object", qualifiedJavaClassName)
                .endControlFlow()
                .emitStatement("unmanagedObject = (%s) cachedObject.object", qualifiedJavaClassName)
                .emitStatement("cachedObject.minDepth = currentDepth")
                .endControlFlow();

        // may cause an unused variable warning if the object contains only null lists
        writer.emitStatement("%1$s unmanagedCopy = (%1$s) unmanagedObject", interfaceName)
            .emitStatement("%1$s realmSource = (%1$s) realmObject", interfaceName);

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String setter = metadata.getInternalSetter(fieldName);
            String getter = metadata.getInternalGetter(fieldName);

            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitSingleLineComment("Deep copy of %s", fieldName)
                        .emitStatement("unmanagedCopy.%s(%s.createDetachedCopy(realmSource.%s(), currentDepth + 1, maxDepth, cache))",
                                setter, Utils.getProxyClassSimpleName(field), getter);
            } else if (Utils.isRealmModelList(field)) {
                writer
                        .emitEmptyLine()
                        .emitSingleLineComment("Deep copy of %s", fieldName)
                        .beginControlFlow("if (currentDepth == maxDepth)")
                        .emitStatement("unmanagedCopy.%s(null)", setter)
                        .nextControlFlow("else")
                        .emitStatement("RealmList<%s> managed%sList = realmSource.%s()",
                                Utils.getGenericTypeQualifiedName(field), fieldName, getter)
                        .emitStatement("RealmList<%1$s> unmanaged%2$sList = new RealmList<%1$s>()", Utils.getGenericTypeQualifiedName(field), fieldName)
                        .emitStatement("unmanagedCopy.%s(unmanaged%sList)", setter, fieldName)
                        .emitStatement("int nextDepth = currentDepth + 1")
                        .emitStatement("int size = managed%sList.size()", fieldName)
                        .beginControlFlow("for (int i = 0; i < size; i++)")
                        .emitStatement("%s item = %s.createDetachedCopy(managed%sList.get(i), nextDepth, maxDepth, cache)",
                                Utils.getGenericTypeQualifiedName(field), Utils.getProxyClassSimpleName(field), fieldName)
                        .emitStatement("unmanaged%sList.add(item)", fieldName)
                        .endControlFlow()
                        .endControlFlow();
            } else if (Utils.isRealmValueList(field)) {
                writer
                        .emitEmptyLine()
                        .emitStatement("unmanagedCopy.%1$s(new RealmList<%2$s>())", setter, Utils.getGenericTypeQualifiedName(field))
                        .emitStatement("unmanagedCopy.%1$s().addAll(realmSource.%1$s())", getter);
            } else if (Utils.isMutableRealmInteger(field)) {
                // If the user initializes the unmanaged MutableRealmInteger to null, this will fail mysteriously.
                writer.emitStatement("unmanagedCopy.%s().set(realmSource.%s().get())", getter, getter);
            } else {
                writer.emitStatement("unmanagedCopy.%s(realmSource.%s())", setter, getter);
            }
        }

        writer.emitEmptyLine();
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
                qualifiedJavaClassName, // Return type
                "update", // Method name
                EnumSet.of(Modifier.STATIC), // Modifiers
                "Realm", "realm", qualifiedJavaClassName, "realmObject", qualifiedJavaClassName, "newObject", "Map<RealmModel, RealmObjectProxy>", "cache"); // Argument type & argument name

        writer
                .emitStatement("%1$s realmObjectTarget = (%1$s) realmObject", interfaceName)
                .emitStatement("%1$s realmObjectSource = (%1$s) newObject", interfaceName);

        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String setter = metadata.getInternalSetter(fieldName);
            String getter = metadata.getInternalGetter(fieldName);
            //@formatter:off
            if (Utils.isRealmModel(field)) {
                writer
                        .emitStatement("%s %sObj = realmObjectSource.%s()",
                                Utils.getFieldTypeQualifiedName(field), fieldName, getter)
                        .beginControlFlow("if (%sObj == null)", fieldName)
                        .emitStatement("realmObjectTarget.%s(null)", setter)
                        .nextControlFlow("else")
                        .emitStatement("%1$s cache%2$s = (%1$s) cache.get(%2$sObj)",
                                Utils.getFieldTypeQualifiedName(field), fieldName)
                        .beginControlFlow("if (cache%s != null)", fieldName)
                        .emitStatement("realmObjectTarget.%s(cache%s)", setter, fieldName)
                        .nextControlFlow("else")
                        .emitStatement("realmObjectTarget.%s(%s.copyOrUpdate(realm, %sObj, true, cache))",
                                setter, Utils.getProxyClassSimpleName(field), fieldName)
                        .endControlFlow()
                        // No need to throw exception here if the field is not nullable. A exception will be thrown in setter.
                        .endControlFlow();
            } else if (Utils.isRealmModelList(field)) {
                final String genericType = Utils.getGenericTypeQualifiedName(field);
                writer
                    .emitStatement("RealmList<%s> %sList = realmObjectSource.%s()", genericType, fieldName, getter)
                    .emitStatement("RealmList<%s> %sRealmList = realmObjectTarget.%s()", genericType, fieldName, getter)
                    .beginControlFlow("if (%1$sList != null && %1$sList.size() == %1$sRealmList.size())", fieldName)
                        .emitSingleLineComment("For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.")
                        .emitStatement("int objects = %sList.size()", fieldName)
                        .beginControlFlow("for (int i = 0; i < objects; i++)")
                            .emitStatement("%1$s %2$sItem = %2$sList.get(i)", genericType, fieldName)
                            .emitStatement("%1$s cache%2$s = (%1$s) cache.get(%2$sItem)", genericType, fieldName)
                            .beginControlFlow("if (cache%s != null)", fieldName)
                                .emitStatement("%1$sRealmList.set(i, cache%1$s)", fieldName)
                            .nextControlFlow("else")
                                .emitStatement("%1$sRealmList.set(i, %2$s.copyOrUpdate(realm, %1$sItem, true, cache))", fieldName, Utils.getProxyClassSimpleName(field))
                            .endControlFlow()
                        .endControlFlow()
                    .nextControlFlow("else")
                        .emitStatement("%sRealmList.clear()", fieldName)
                        .beginControlFlow("if (%sList != null)", fieldName)
                            .beginControlFlow("for (int i = 0; i < %sList.size(); i++)", fieldName)
                                .emitStatement("%1$s %2$sItem = %2$sList.get(i)", genericType, fieldName)
                                .emitStatement("%1$s cache%2$s = (%1$s) cache.get(%2$sItem)", genericType, fieldName)
                                .beginControlFlow("if (cache%s != null)", fieldName)
                                    .emitStatement("%1$sRealmList.add(cache%1$s)", fieldName)
                                .nextControlFlow("else")
                                    .emitStatement("%1$sRealmList.add(%2$s.copyOrUpdate(realm, %1$sItem, true, cache))", fieldName, Utils.getProxyClassSimpleName(field))
                                .endControlFlow()
                            .endControlFlow()
                        .endControlFlow()
                    .endControlFlow();
            } else if (Utils.isRealmValueList(field)) {
                writer.emitStatement("realmObjectTarget.%s(realmObjectSource.%s())", setter, getter);
            } else if (Utils.isMutableRealmInteger(field)) {
                writer.emitStatement("realmObjectTarget.%s().set(realmObjectSource.%s().get())", getter, getter);
            } else {
                if (field != metadata.getPrimaryKey()) {
                    writer.emitStatement("realmObjectTarget.%s(realmObjectSource.%s())", setter, getter);
                }
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
        writer.emitStatement("StringBuilder stringBuilder = new StringBuilder(\"%s = proxy[\")", simpleJavaClassName);

        Collection<RealmFieldElement> fields = metadata.getFields();
        int i = fields.size() - 1;
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();

            writer.emitStatement("stringBuilder.append(\"{%s:\")", fieldName);
            if (Utils.isRealmModel(field)) {
                String fieldTypeSimpleName = Utils.stripPackage(Utils.getFieldTypeQualifiedName(field));
                writer.emitStatement(
                        "stringBuilder.append(%s() != null ? \"%s\" : \"null\")",
                        metadata.getInternalGetter(fieldName),
                        fieldTypeSimpleName
                );
            } else if (Utils.isRealmList(field)) {
                String genericTypeSimpleName = Utils.stripPackage(Utils.getGenericTypeQualifiedName(field));
                writer.emitStatement("stringBuilder.append(\"RealmList<%s>[\").append(%s().size()).append(\"]\")",
                        genericTypeSimpleName,
                        metadata.getInternalGetter(fieldName));
            } else if (Utils.isMutableRealmInteger(field)) {
                writer.emitStatement("stringBuilder.append(%s().get())", metadata.getInternalGetter(fieldName));
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
        String proxyClassName = Utils.getProxyClassName(qualifiedJavaClassName);
        String otherObjectVarName = "a" + simpleJavaClassName;
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
                .endMethod();
    }
    //@formatter:on

    private void emitCreateOrUpdateUsingJsonObject(JavaWriter writer) throws IOException {
        writer.emitAnnotation("SuppressWarnings", "\"cast\"");
        writer.beginMethod(
                qualifiedJavaClassName,
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
                    qualifiedJavaClassName, qualifiedJavaClassName);
        } else {
            String pkType = Utils.isString(metadata.getPrimaryKey()) ? "String" : "Long";
            writer
                .emitStatement("%s obj = null", qualifiedJavaClassName)
                .beginControlFlow("if (update)")
                    .emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName)
                    .emitStatement("%s columnInfo = (%s) realm.getSchema().getColumnInfo(%s.class)",
                        columnInfoClassName(), columnInfoClassName(), qualifiedJavaClassName)
                    .emitStatement("long pkColumnIndex = %s", fieldIndexVariableReference(metadata.getPrimaryKey()))
                    .emitStatement("long rowIndex = Table.NO_MATCH");
            if (metadata.isNullable(metadata.getPrimaryKey())) {
                writer
                    .beginControlFlow("if (json.isNull(\"%s\"))", metadata.getPrimaryKey().getSimpleName())
                        .emitStatement("rowIndex = table.findFirstNull(pkColumnIndex)")
                    .nextControlFlow("else")
                        .emitStatement(
                                "rowIndex = table.findFirst%s(pkColumnIndex, json.get%s(\"%s\"))",
                                pkType, pkType, metadata.getPrimaryKey().getSimpleName())
                    .endControlFlow();
            } else {
                writer
                    .beginControlFlow("if (!json.isNull(\"%s\"))", metadata.getPrimaryKey().getSimpleName())
                        .emitStatement(
                                "rowIndex = table.findFirst%s(pkColumnIndex, json.get%s(\"%s\"))",
                                pkType, pkType, metadata.getPrimaryKey().getSimpleName())
                    .endControlFlow();
            }
            writer
                .beginControlFlow("if (rowIndex != Table.NO_MATCH)")
                    .emitStatement("final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get()")
                    .beginControlFlow("try")
                        .emitStatement(
                                "objectContext.set(realm, table.getUncheckedRow(rowIndex), realm.getSchema().getColumnInfo(%s.class), false, Collections.<String> emptyList())",
                                qualifiedJavaClassName)
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
            RealmJsonTypeHelper.emitCreateObjectWithPrimaryKeyValue(
                    qualifiedJavaClassName, qualifiedGeneratedClassName, primaryKeyFieldType, primaryKeyFieldName, writer);
            writer.endControlFlow();
        }
        //@formatter:on

        writer
                .emitEmptyLine()
                .emitStatement("final %1$s objProxy = (%1$s) obj", interfaceName);
        for (VariableElement field : metadata.getFields()) {
            String fieldName = field.getSimpleName().toString();
            String qualifiedFieldType = field.asType().toString();
            if (metadata.isPrimaryKey(field)) {
                // Primary key has already been set when adding new row or finding the existing row.
                continue;
            }
            if (Utils.isRealmModel(field)) {
                RealmJsonTypeHelper.emitFillRealmObjectWithJsonValue(
                        "objProxy",
                        metadata.getInternalSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        Utils.getProxyClassSimpleName(field),
                        writer
                );

            } else if (Utils.isRealmModelList(field)) {
                RealmJsonTypeHelper.emitFillRealmListWithJsonValue(
                        "objProxy",
                        metadata.getInternalGetter(fieldName),
                        metadata.getInternalSetter(fieldName),
                        fieldName,
                        ((DeclaredType) field.asType()).getTypeArguments().get(0).toString(),
                        Utils.getProxyClassSimpleName(field),
                        writer);

            } else if (Utils.isRealmValueList(field)) {
                writer.emitStatement("ProxyUtils.setRealmListWithJsonObject(objProxy.%1$s(), json, \"%2$s\")",
                        metadata.getInternalGetter(fieldName), fieldName);
            } else if (Utils.isMutableRealmInteger(field)) {
                RealmJsonTypeHelper.emitFillJavaTypeWithJsonValue(
                        "objProxy",
                        metadata.getInternalGetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        writer);

            } else {
                RealmJsonTypeHelper.emitFillJavaTypeWithJsonValue(
                        "objProxy",
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

    private void buildExcludeFieldsList(JavaWriter writer, Collection<RealmFieldElement> fields) throws IOException {
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
                qualifiedJavaClassName,
                "createUsingJsonStream",
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
                Arrays.asList("Realm", "realm", "JsonReader", "reader"),
                Collections.singletonList("IOException"));

        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("boolean jsonHasPrimaryKey = false");
        }
        writer.emitStatement("final %s obj = new %s()", qualifiedJavaClassName, qualifiedJavaClassName);
        writer.emitStatement("final %1$s objProxy = (%1$s) obj", interfaceName);
        writer.emitStatement("reader.beginObject()");
        writer.beginControlFlow("while (reader.hasNext())");
        writer.emitStatement("String name = reader.nextName()");
        writer.beginControlFlow("if (false)");
        Collection<RealmFieldElement> fields = metadata.getFields();
        for (VariableElement field : fields) {
            String fieldName = field.getSimpleName().toString();
            String qualifiedFieldType = field.asType().toString();
            writer.nextControlFlow("else if (name.equals(\"%s\"))", fieldName);

            if (Utils.isRealmModel(field)) {
                RealmJsonTypeHelper.emitFillRealmObjectFromStream(
                        "objProxy",
                        metadata.getInternalSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        Utils.getProxyClassSimpleName(field),
                        writer
                );

            } else if (Utils.isRealmModelList(field)) {
                RealmJsonTypeHelper.emitFillRealmListFromStream(
                        "objProxy",
                        metadata.getInternalGetter(fieldName),
                        metadata.getInternalSetter(fieldName),
                        ((DeclaredType) field.asType()).getTypeArguments().get(0).toString(),
                        Utils.getProxyClassSimpleName(field),
                        writer);

            } else if (Utils.isRealmValueList(field)) {
                writer.emitStatement("objProxy.%1$s(ProxyUtils.createRealmListWithJsonStream(%2$s.class, reader))",
                        metadata.getInternalSetter(fieldName),
                        Utils.getRealmListType(field));
            } else if (Utils.isMutableRealmInteger(field)) {
                RealmJsonTypeHelper.emitFillJavaTypeFromStream(
                        "objProxy",
                        metadata,
                        metadata.getInternalGetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        writer
                );
            } else {
                RealmJsonTypeHelper.emitFillJavaTypeFromStream(
                        "objProxy",
                        metadata,
                        metadata.getInternalSetter(fieldName),
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

        writer.emitStatement("return realm.copyToRealm(obj)");
        writer.endMethod();
        writer.emitEmptyLine();
    }

    private String columnInfoClassName() {
        return simpleJavaClassName + "ColumnInfo";
    }

    private String columnIndexVarName(VariableElement variableElement) {
        return variableElement.getSimpleName().toString() + "Index";
    }

    private String mutableRealmIntegerFieldName(VariableElement variableElement) {
        return variableElement.getSimpleName().toString() + "MutableRealmInteger";
    }

    private String fieldIndexVariableReference(VariableElement variableElement) {
        return "columnInfo." + columnIndexVarName(variableElement);
    }

    private static int countModelOrListFields(Collection<RealmFieldElement> fields) {
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
        if (Utils.isMutableRealmInteger(field)) {
            return Constants.RealmFieldType.REALM_INTEGER;
        }
        if (Utils.isRealmModel(field)) {
            return Constants.RealmFieldType.OBJECT;
        }
        if (Utils.isRealmModelList(field)) {
            return Constants.RealmFieldType.LIST;
        }
        if (Utils.isRealmValueList(field)) {
            final Constants.RealmFieldType fieldType = Utils.getValueListFieldType(field);
            if (fieldType == null) {
                return Constants.RealmFieldType.NOTYPE;
            }
            return fieldType;
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
