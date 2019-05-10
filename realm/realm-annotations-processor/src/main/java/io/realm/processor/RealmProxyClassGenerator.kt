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

package io.realm.processor

import com.squareup.javawriter.JavaWriter

import java.io.BufferedWriter
import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.EnumSet
import java.util.Locale

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror


class RealmProxyClassGenerator(private val processingEnvironment: ProcessingEnvironment, private val typeMirrors: TypeMirrors, private val metadata: ClassMetaData, private val classCollection: ClassCollection) {
    private val simpleJavaClassName: String
    private val qualifiedJavaClassName: String
    private val internalClassName: String?
    private val interfaceName: String
    private val qualifiedGeneratedClassName: String
    private val suppressWarnings: Boolean

    init {
        this.simpleJavaClassName = metadata.simpleJavaClassName
        this.qualifiedJavaClassName = metadata.fullyQualifiedClassName
        this.internalClassName = metadata.internalClassName
        this.interfaceName = Utils.getProxyInterfaceName(qualifiedJavaClassName)
        this.qualifiedGeneratedClassName = String.format(Locale.US, "%s.%s",
                Constants.REALM_PACKAGE_NAME, Utils.getProxyClassName(qualifiedJavaClassName))

        // See the configuration for the debug build type,
        //  in the realm-library project, for an example of how to set this flag.
        this.suppressWarnings = !"false".equals(processingEnvironment.options[OPTION_SUPPRESS_WARNINGS], ignoreCase = true)
    }

    @Throws(IOException::class, UnsupportedOperationException::class)
    fun generate() {
        val sourceFile = processingEnvironment.filer.createSourceFile(qualifiedGeneratedClassName)
        val writer = JavaWriter(BufferedWriter(sourceFile.openWriter()))

        // Set source code indent
        writer.indent = Constants.INDENT

        writer.emitPackage(Constants.REALM_PACKAGE_NAME)
                .emitEmptyLine()

        val imports = ArrayList(IMPORTS)
        if (!metadata.backlinkFields.isEmpty()) {
            imports.add("io.realm.internal.UncheckedRow")
        }
        writer.emitImports(imports)
                .emitEmptyLine()

        // Begin the class definition
        if (suppressWarnings) {
            writer.emitAnnotation("SuppressWarnings(\"all\")")
        }
        writer
                .beginType(
                        qualifiedGeneratedClassName, // full qualified name of the item to generate
                        "class", // the type of the item
                        EnumSet.of(Modifier.PUBLIC), // modifiers to apply
                        qualifiedJavaClassName, // class to extend
                        "RealmObjectProxy", // interfaces to implement
                        interfaceName)
                .emitEmptyLine()

        emitColumnInfoClass(writer)

        emitClassFields(writer)

        emitInstanceFields(writer)
        emitConstructor(writer)

        emitInjectContextMethod(writer)
        emitPersistedFieldAccessors(writer)
        emitBacklinkFieldAccessors(writer)
        emitCreateExpectedObjectSchemaInfo(writer)
        emitGetExpectedObjectSchemaInfo(writer)
        emitCreateColumnInfoMethod(writer)
        emitGetSimpleClassNameMethod(writer)
        emitCreateOrUpdateUsingJsonObject(writer)
        emitCreateUsingJsonStream(writer)
        emitNewProxyInstance(writer)
        emitCopyOrUpdateMethod(writer)
        emitCopyMethod(writer)
        emitInsertMethod(writer)
        emitInsertListMethod(writer)
        emitInsertOrUpdateMethod(writer)
        emitInsertOrUpdateListMethod(writer)
        emitCreateDetachedCopyMethod(writer)
        emitUpdateMethod(writer)
        emitToStringMethod(writer)
        emitRealmObjectProxyImplementation(writer)
        emitHashcodeMethod(writer)
        emitEqualsMethod(writer)

        // End the class definition
        writer.endType()
        writer.close()
    }

    @Throws(IOException::class)
    private fun emitColumnInfoClass(writer: JavaWriter) {
        writer.beginType(
                columnInfoClassName(), // full qualified name of the item to generate
                "class", // the type of the item
                EnumSet.of(Modifier.STATIC, Modifier.FINAL), // modifiers to apply
                "ColumnInfo")                               // base class

        // fields
        writer.emitField("long", "maxColumnIndexValue") // Must not end with Index as it otherwise could conflict regular fields.
        for (variableElement in metadata.fields) {
            writer.emitField("long", columnIndexVarName(variableElement))
        }
        writer.emitEmptyLine()

        // constructor #1
        writer.beginConstructor(
                EnumSet.noneOf(Modifier::class.java),
                "OsSchemaInfo", "schemaInfo")
        writer.emitStatement("super(%s)", metadata.fields.size)
        writer.emitStatement("OsObjectSchemaInfo objectSchemaInfo = schemaInfo.getObjectSchemaInfo(\"%1\$s\")",
                internalClassName)
        for (field in metadata.fields) {
            writer.emitStatement(
                    "this.%1\$sIndex = addColumnDetails(\"%1\$s\", \"%2\$s\", objectSchemaInfo)",
                    field.javaName,
                    field.internalFieldName)
        }
        for (backlink in metadata.backlinkFields) {
            writer.emitStatement(
                    "addBacklinkDetails(schemaInfo, \"%s\", \"%s\", \"%s\")",
                    backlink.targetField,
                    classCollection.getClassFromQualifiedName(backlink.sourceClass!!).internalClassName,
                    backlink.sourceField)
        }
        writer
                .emitStatement("this.maxColumnIndexValue = objectSchemaInfo.getMaxColumnIndex()")
                .endConstructor()
                .emitEmptyLine()

        // constructor #2
        writer.beginConstructor(
                EnumSet.noneOf(Modifier::class.java),
                "ColumnInfo", "src", "boolean", "mutable")
        writer.emitStatement("super(src, mutable)")
                .emitStatement("copy(src, this)")
        writer.endConstructor()
                .emitEmptyLine()

        // no-args copy method
        writer.emitAnnotation("Override")
                .beginMethod(
                        "ColumnInfo", // return type
                        "copy", // method name
                        EnumSet.of(Modifier.PROTECTED, Modifier.FINAL), // modifiers
                        "boolean", "mutable")     // parameters
        writer.emitStatement("return new %s(this, mutable)", columnInfoClassName())
        writer.endMethod()
                .emitEmptyLine()

        // copy method
        writer.emitAnnotation("Override")
                .beginMethod(
                        "void", // return type
                        "copy", // method name
                        EnumSet.of(Modifier.PROTECTED, Modifier.FINAL), // modifiers
                        "ColumnInfo", "rawSrc", "ColumnInfo", "rawDst") // parameters
        writer.emitStatement("final %1\$s src = (%1\$s) rawSrc", columnInfoClassName())
        writer.emitStatement("final %1\$s dst = (%1\$s) rawDst", columnInfoClassName())
        for (variableElement in metadata.fields) {
            writer.emitStatement("dst.%1\$s = src.%1\$s", columnIndexVarName(variableElement))
        }
        writer.emitStatement("dst.maxColumnIndexValue = src.maxColumnIndexValue")
        writer.endMethod()

        writer.endType()
    }

    //@formatter:off
    @Throws(IOException::class)
    private fun emitClassFields(writer: JavaWriter) {
        writer.emitEmptyLine()
                .emitField("OsObjectSchemaInfo", "expectedObjectSchemaInfo",
                        EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL), "createExpectedObjectSchemaInfo()")
    }
    //@formatter:on

    //@formatter:off
    @Throws(IOException::class)
    private fun emitInstanceFields(writer: JavaWriter) {
        writer.emitEmptyLine()
                .emitField(columnInfoClassName(), "columnInfo", EnumSet.of(Modifier.PRIVATE))
                .emitField("ProxyState<$qualifiedJavaClassName>", "proxyState", EnumSet.of(Modifier.PRIVATE))

        for (variableElement in metadata.fields) {
            if (Utils.isMutableRealmInteger(variableElement)) {
                emitMutableRealmIntegerField(writer, variableElement)
            } else if (Utils.isRealmList(variableElement)) {
                val genericType = Utils.getGenericTypeQualifiedName(variableElement)
                writer.emitField("RealmList<$genericType>", variableElement.simpleName.toString() + "RealmList", EnumSet.of(Modifier.PRIVATE))
            }
        }

        for (backlink in metadata.backlinkFields) {
            writer.emitField(backlink.targetFieldType, backlink.targetField + BACKLINKS_FIELD_EXTENSION,
                    EnumSet.of(Modifier.PRIVATE))
        }
    }
    //@formatter:on

    // The anonymous subclass of MutableRealmInteger.Managed holds a reference to this proxy.
    // Even if all other references to the proxy are dropped, the proxy will not be GCed until
    // the MutableInteger that it owns, also becomes unreachable.
    //@formatter:off
    @Throws(IOException::class)
    private fun emitMutableRealmIntegerField(writer: JavaWriter, variableElement: VariableElement) {
        writer.emitField("MutableRealmInteger.Managed",
                mutableRealmIntegerFieldName(variableElement),
                EnumSet.of(Modifier.PRIVATE, Modifier.FINAL),
                String.format(
                        "new MutableRealmInteger.Managed<%1\$s>() {\n"
                                + "    @Override protected ProxyState<%1\$s> getProxyState() { return proxyState; }\n"
                                + "    @Override protected long getColumnIndex() { return columnInfo.%2\$s; }\n"
                                + "}",
                        qualifiedJavaClassName, columnIndexVarName(variableElement)))
    }
    //@formatter:on

    //@formatter:off
    @Throws(IOException::class)
    private fun emitConstructor(writer: JavaWriter) {
        // FooRealmProxy(ColumnInfo)
        writer.emitEmptyLine()
                .beginConstructor(EnumSet.noneOf(Modifier::class.java))
                .emitStatement("proxyState.setConstructionFinished()")
                .endConstructor()
                .emitEmptyLine()
    }
    //@formatter:on

    @Throws(IOException::class)
    private fun emitPersistedFieldAccessors(writer: JavaWriter) {
        for (field in metadata.fields) {
            val fieldName = field.simpleName.toString()
            val fieldTypeCanonicalName = field.asType().toString()

            if (Constants.JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName)) {
                emitPrimitiveType(writer, field, fieldName, fieldTypeCanonicalName)
            } else if (Utils.isMutableRealmInteger(field)) {
                emitMutableRealmInteger(writer, field, fieldName, fieldTypeCanonicalName)
            } else if (Utils.isRealmModel(field)) {
                emitRealmModel(writer, field, fieldName, fieldTypeCanonicalName)
            } else if (Utils.isRealmList(field)) {
                val elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field)
                emitRealmList(writer, field, fieldName, fieldTypeCanonicalName, elementTypeMirror)
            } else {
                throw UnsupportedOperationException(String.format(Locale.US,
                        "Field \"%s\" of type \"%s\" is not supported.", fieldName, fieldTypeCanonicalName))
            }

            writer.emitEmptyLine()
        }
    }

    /**
     * Primitives and boxed types
     */
    @Throws(IOException::class)
    private fun emitPrimitiveType(
            writer: JavaWriter,
            field: VariableElement,
            fieldName: String,
            fieldTypeCanonicalName: String) {

        val fieldJavaType = getRealmTypeChecked(field).javaType

        // Getter
        //@formatter:off
        writer.emitAnnotation("Override")
        writer.emitAnnotation("SuppressWarnings", "\"cast\"")
                .beginMethod(fieldTypeCanonicalName, metadata.getInternalGetter(fieldName), EnumSet.of(Modifier.PUBLIC))
                .emitStatement("proxyState.getRealm\$realm().checkIfValid()")

        // For String and bytes[], null value will be returned by JNI code. Try to save one JNI call here.
        if (metadata.isNullable(field) && !Utils.isString(field) && !Utils.isByteArray(field)) {
            writer.beginControlFlow("if (proxyState.getRow\$realm().isNull(%s))", fieldIndexVariableReference(field))
                    .emitStatement("return null")
                    .endControlFlow()
        }
        //@formatter:on

        // For Boxed types, this should be the corresponding primitive types. Others remain the same.
        val castingBackType: String
        if (Utils.isBoxedType(fieldTypeCanonicalName)) {
            val typeUtils = processingEnvironment.typeUtils
            castingBackType = typeUtils.unboxedType(field.asType()).toString()
        } else {
            castingBackType = fieldTypeCanonicalName
        }
        writer.emitStatement(
                "return (%s) proxyState.getRow\$realm().get%s(%s)",
                castingBackType, fieldJavaType, fieldIndexVariableReference(field))
        writer.endMethod()
                .emitEmptyLine()

        // Setter
        writer.emitAnnotation("Override")
        writer.beginMethod("void", metadata.getInternalSetter(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value")
        emitCodeForUnderConstruction(writer, metadata.isPrimaryKey(field), object : CodeEmitter {
            @Throws(IOException::class)
            override fun emit(writer: JavaWriter) {
                // set value as default value
                writer.emitStatement("final Row row = proxyState.getRow\$realm()")

                //@formatter:off
                if (metadata.isNullable(field)) {
                    writer.beginControlFlow("if (value == null)")
                            .emitStatement("row.getTable().setNull(%s, row.getIndex(), true)",
                                    fieldIndexVariableReference(field))
                            .emitStatement("return")
                            .endControlFlow()
                } else if (!metadata.isNullable(field) && !Utils.isPrimitiveType(field)) {
                    writer.beginControlFlow("if (value == null)")
                            .emitStatement(Constants.STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE, fieldName)
                            .endControlFlow()
                }
                //@formatter:on

                writer.emitStatement(
                        "row.getTable().set%s(%s, row.getIndex(), value, true)",
                        fieldJavaType, fieldIndexVariableReference(field))
                writer.emitStatement("return")
            }
        })
        writer.emitStatement("proxyState.getRealm\$realm().checkIfValid()")
        // Although setting null value for String and bytes[] can be handled by the JNI code, we still generate the same code here.
        // Compared with getter, null value won't trigger more native calls in setter which is relatively cheaper.
        if (metadata.isPrimaryKey(field)) {
            // Primary key is not allowed to be changed after object created.
            writer.emitStatement(Constants.STATEMENT_EXCEPTION_PRIMARY_KEY_CANNOT_BE_CHANGED, fieldName)
        } else {
            //@formatter:off
            if (metadata.isNullable(field)) {
                writer.beginControlFlow("if (value == null)")
                        .emitStatement("proxyState.getRow\$realm().setNull(%s)", fieldIndexVariableReference(field))
                        .emitStatement("return")
                        .endControlFlow()
            } else if (!metadata.isNullable(field) && !Utils.isPrimitiveType(field)) {
                // Same reason, throw IAE earlier.
                writer
                        .beginControlFlow("if (value == null)")
                        .emitStatement(Constants.STATEMENT_EXCEPTION_ILLEGAL_NULL_VALUE, fieldName)
                        .endControlFlow()
            }
            //@formatter:on
            writer.emitStatement(
                    "proxyState.getRow\$realm().set%s(%s, value)",
                    fieldJavaType, fieldIndexVariableReference(field))
        }
        writer.endMethod()
    }

    //@formatter:off
    @Throws(IOException::class)
    private fun emitMutableRealmInteger(writer: JavaWriter, field: VariableElement, fieldName: String, fieldTypeCanonicalName: String) {
        writer.emitAnnotation("Override")
                .beginMethod(fieldTypeCanonicalName, metadata.getInternalGetter(fieldName), EnumSet.of(Modifier.PUBLIC))
                .emitStatement("proxyState.getRealm\$realm().checkIfValid()")
                .emitStatement("return this.%s", mutableRealmIntegerFieldName(field))
                .endMethod()
    }
    //@formatter:on

    /**
     * Links
     */
    //@formatter:off
    @Throws(IOException::class)
    private fun emitRealmModel(
            writer: JavaWriter,
            field: VariableElement,
            fieldName: String,
            fieldTypeCanonicalName: String) {

        // Getter
        writer.emitAnnotation("Override")
        writer.beginMethod(fieldTypeCanonicalName, metadata.getInternalGetter(fieldName), EnumSet.of(Modifier.PUBLIC))
                .emitStatement("proxyState.getRealm\$realm().checkIfValid()")
                .beginControlFlow("if (proxyState.getRow\$realm().isNullLink(%s))", fieldIndexVariableReference(field))
                .emitStatement("return null")
                .endControlFlow()
                .emitStatement("return proxyState.getRealm\$realm().get(%s.class, proxyState.getRow\$realm().getLink(%s), false, Collections.<String>emptyList())",
                        fieldTypeCanonicalName, fieldIndexVariableReference(field))
                .endMethod()
                .emitEmptyLine()

        // Setter
        writer.emitAnnotation("Override")
        writer.beginMethod("void", metadata.getInternalSetter(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value")
        emitCodeForUnderConstruction(writer, metadata.isPrimaryKey(field), object : CodeEmitter {
            @Throws(IOException::class)
            override fun emit(writer: JavaWriter) {
                // check excludeFields
                writer.beginControlFlow("if (proxyState.getExcludeFields\$realm().contains(\"%1\$s\"))",
                        field.simpleName.toString())
                        .emitStatement("return")
                        .endControlFlow()
                writer.beginControlFlow("if (value != null && !RealmObject.isManaged(value))")
                        .emitStatement("value = ((Realm) proxyState.getRealm\$realm()).copyToRealm(value)")
                        .endControlFlow()

                // set value as default value
                writer.emitStatement("final Row row = proxyState.getRow\$realm()")
                writer.beginControlFlow("if (value == null)")
                        .emitSingleLineComment("Table#nullifyLink() does not support default value. Just using Row.")
                        .emitStatement("row.nullifyLink(%s)", fieldIndexVariableReference(field))
                        .emitStatement("return")
                        .endControlFlow()
                writer.emitStatement("proxyState.checkValidObject(value)")
                writer.emitStatement("row.getTable().setLink(%s, row.getIndex(), ((RealmObjectProxy) value).realmGet\$proxyState().getRow\$realm().getIndex(), true)",
                        fieldIndexVariableReference(field))
                writer.emitStatement("return")
            }
        })
        writer.emitStatement("proxyState.getRealm\$realm().checkIfValid()")
                .beginControlFlow("if (value == null)")
                .emitStatement("proxyState.getRow\$realm().nullifyLink(%s)", fieldIndexVariableReference(field))
                .emitStatement("return")
                .endControlFlow()
                .emitStatement("proxyState.checkValidObject(value)")
                .emitStatement("proxyState.getRow\$realm().setLink(%s, ((RealmObjectProxy) value).realmGet\$proxyState().getRow\$realm().getIndex())", fieldIndexVariableReference(field))
                .endMethod()
    }
    //@formatter:on

    /**
     * ModelList, ValueList
     */
    //@formatter:off
    @Throws(IOException::class)
    private fun emitRealmList(
            writer: JavaWriter,
            field: VariableElement,
            fieldName: String,
            fieldTypeCanonicalName: String,
            elementTypeMirror: TypeMirror?) {
        val genericType = Utils.getGenericTypeQualifiedName(field)
        val forRealmModel = Utils.isRealmModel(elementTypeMirror)

        // Getter
        writer.emitAnnotation("Override")
        writer.beginMethod(fieldTypeCanonicalName, metadata.getInternalGetter(fieldName), EnumSet.of(Modifier.PUBLIC))
                .emitStatement("proxyState.getRealm\$realm().checkIfValid()")
                .emitSingleLineComment("use the cached value if available")
                .beginControlFlow("if (" + fieldName + "RealmList != null)")
                .emitStatement("return " + fieldName + "RealmList")
                .nextControlFlow("else")
        if (Utils.isRealmModelList(field)) {
            writer.emitStatement("OsList osList = proxyState.getRow\$realm().getModelList(%s)",
                    fieldIndexVariableReference(field))
        } else {
            writer.emitStatement("OsList osList = proxyState.getRow\$realm().getValueList(%1\$s, RealmFieldType.%2\$s)",
                    fieldIndexVariableReference(field), Utils.getValueListFieldType(field).name)
        }
        writer.emitStatement(fieldName + "RealmList = new RealmList<%s>(%s.class, osList, proxyState.getRealm\$realm())",
                genericType, genericType)
                .emitStatement("return " + fieldName + "RealmList")
                .endControlFlow()
                .endMethod()
                .emitEmptyLine()

        // Setter
        writer.emitAnnotation("Override")
        writer.beginMethod("void", metadata.getInternalSetter(fieldName), EnumSet.of(Modifier.PUBLIC), fieldTypeCanonicalName, "value")
        emitCodeForUnderConstruction(writer, metadata.isPrimaryKey(field), object : CodeEmitter {
            @Throws(IOException::class)
            override fun emit(writer: JavaWriter) {
                // check excludeFields
                writer.beginControlFlow("if (proxyState.getExcludeFields\$realm().contains(\"%1\$s\"))",
                        field.simpleName.toString())
                        .emitStatement("return")
                        .endControlFlow()

                if (!forRealmModel) {
                    return
                }

                writer.emitSingleLineComment("if the list contains unmanaged RealmObjects, convert them to managed.")
                        .beginControlFlow("if (value != null && !value.isManaged())")
                        .emitStatement("final Realm realm = (Realm) proxyState.getRealm\$realm()")
                        .emitStatement("final RealmList<%1\$s> original = value", genericType)
                        .emitStatement("value = new RealmList<%1\$s>()", genericType)
                        .beginControlFlow("for (%1\$s item : original)", genericType)
                        .beginControlFlow("if (item == null || RealmObject.isManaged(item))")
                        .emitStatement("value.add(item)")
                        .nextControlFlow("else")
                        .emitStatement("value.add(realm.copyToRealm(item))")
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow()

                // LinkView currently does not support default value feature. Just fallback to normal code.
            }
        })

        writer.emitStatement("proxyState.getRealm\$realm().checkIfValid()")
        if (Utils.isRealmModelList(field)) {
            writer.emitStatement("OsList osList = proxyState.getRow\$realm().getModelList(%s)",
                    fieldIndexVariableReference(field))
        } else {
            writer.emitStatement("OsList osList = proxyState.getRow\$realm().getValueList(%1\$s, RealmFieldType.%2\$s)",
                    fieldIndexVariableReference(field), Utils.getValueListFieldType(field).name)
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
                    .emitStatement("osList.setRow(i, ((RealmObjectProxy) linkedObject).realmGet\$proxyState().getRow\$realm().getIndex())")
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
                    .emitStatement("osList.addRow(((RealmObjectProxy) linkedObject).realmGet\$proxyState().getRow\$realm().getIndex())")
                    .endControlFlow()
                    .endControlFlow()
        } else {
            // Value lists
            writer
                    .emitStatement("osList.removeAll()")
                    .beginControlFlow("if (value == null)")
                    .emitStatement("return")
                    .endControlFlow()
                    .beginControlFlow("for (%1\$s item : value)", genericType)
                    .beginControlFlow("if (item == null)")
                    .emitStatement(if (metadata.isElementNullable(field)) "osList.addNull()" else "throw new IllegalArgumentException(\"Storing 'null' into $fieldName' is not allowed by the schema.\")")
                    .nextControlFlow("else")
                    .emitStatement(getStatementForAppendingValueToOsList("osList", "item", elementTypeMirror))
                    .endControlFlow()
                    .endControlFlow()
        }
        writer.endMethod()

    }
    //@formatter:on

    private fun getStatementForAppendingValueToOsList(
            osListVariableName: String,
            valueVariableName: String,
            elementTypeMirror: TypeMirror?): String {

        val typeUtils = processingEnvironment.typeUtils
        if (typeUtils.isSameType(elementTypeMirror, typeMirrors.STRING_MIRROR)) {
            return "$osListVariableName.addString($valueVariableName)"
        }
        if ((typeUtils.isSameType(elementTypeMirror, typeMirrors.LONG_MIRROR)
                        || typeUtils.isSameType(elementTypeMirror, typeMirrors.INTEGER_MIRROR)
                        || typeUtils.isSameType(elementTypeMirror, typeMirrors.SHORT_MIRROR)
                        || typeUtils.isSameType(elementTypeMirror, typeMirrors.BYTE_MIRROR))) {
            return "$osListVariableName.addLong($valueVariableName.longValue())"
        }
        if (typeUtils.isSameType(elementTypeMirror, typeMirrors.BINARY_MIRROR)) {
            return "$osListVariableName.addBinary($valueVariableName)"
        }
        if (typeUtils.isSameType(elementTypeMirror, typeMirrors.DATE_MIRROR)) {
            return "$osListVariableName.addDate($valueVariableName)"
        }
        if (typeUtils.isSameType(elementTypeMirror, typeMirrors.BOOLEAN_MIRROR)) {
            return "$osListVariableName.addBoolean($valueVariableName)"
        }
        if (typeUtils.isSameType(elementTypeMirror, typeMirrors.DOUBLE_MIRROR)) {
            return "$osListVariableName.addDouble($valueVariableName.doubleValue())"
        }
        if (typeUtils.isSameType(elementTypeMirror, typeMirrors.FLOAT_MIRROR)) {
            return "$osListVariableName.addFloat($valueVariableName.floatValue())"
        }
        throw RuntimeException("unexpected element type: " + elementTypeMirror!!.toString())
    }

    private interface CodeEmitter {
        @Throws(IOException::class)
        fun emit(writer: JavaWriter)
    }

    @Throws(IOException::class)
    private fun emitCodeForUnderConstruction(writer: JavaWriter, isPrimaryKey: Boolean,
                                             defaultValueCodeEmitter: CodeEmitter) {
        writer.beginControlFlow("if (proxyState.isUnderConstruction())")
        if (isPrimaryKey) {
            writer.emitSingleLineComment("default value of the primary key is always ignored.")
                    .emitStatement("return")
        } else {
            writer.beginControlFlow("if (!proxyState.getAcceptDefaultValue\$realm())")
                    .emitStatement("return")
                    .endControlFlow()
            defaultValueCodeEmitter.emit(writer)
        }
        writer.endControlFlow()
                .emitEmptyLine()
    }

    // Note that because of bytecode hackery, this method may run before the constructor!
    // It may even run before fields have been initialized.
    //@formatter:off
    @Throws(IOException::class)
    private fun emitInjectContextMethod(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "void", // Return type
                "realm\$injectObjectContext", // Method name
                EnumSet.of(Modifier.PUBLIC) // Modifiers
        ) // Argument type & argument name

        writer.beginControlFlow("if (this.proxyState != null)")
                .emitStatement("return")
                .endControlFlow()
                .emitStatement("final BaseRealm.RealmObjectContext context = BaseRealm.objectContext.get()")
                .emitStatement("this.columnInfo = (%1\$s) context.getColumnInfo()", columnInfoClassName())
                .emitStatement("this.proxyState = new ProxyState<%1\$s>(this)", qualifiedJavaClassName)
                .emitStatement("proxyState.setRealm\$realm(context.getRealm())")
                .emitStatement("proxyState.setRow\$realm(context.getRow())")
                .emitStatement("proxyState.setAcceptDefaultValue\$realm(context.getAcceptDefaultValue())")
                .emitStatement("proxyState.setExcludeFields\$realm(context.getExcludeFields())")
                .endMethod()
                .emitEmptyLine()
    }
    //@formatter:on

    //@formatter:off
    @Throws(IOException::class)
    private fun emitBacklinkFieldAccessors(writer: JavaWriter) {
        for (backlink in metadata.backlinkFields) {
            val cacheFieldName = backlink.targetField + BACKLINKS_FIELD_EXTENSION
            val realmResultsType = "RealmResults<" + backlink.sourceClass + ">"

            // Getter, no setter
            writer.emitAnnotation("Override")
            writer.beginMethod(realmResultsType, metadata.getInternalGetter(backlink.targetField), EnumSet.of(Modifier.PUBLIC))
                    .emitStatement("BaseRealm realm = proxyState.getRealm\$realm()")
                    .emitStatement("realm.checkIfValid()")
                    .emitStatement("proxyState.getRow\$realm().checkIfAttached()")
                    .beginControlFlow("if ($cacheFieldName == null)")
                    .emitStatement("$cacheFieldName = RealmResults.createBacklinkResults(realm, proxyState.getRow\$realm(), %s.class, \"%s\")",
                            backlink.sourceClass, backlink.sourceField)
                    .endControlFlow()
                    .emitStatement("return $cacheFieldName")
                    .endMethod()
                    .emitEmptyLine()
        }
    }
    //@formatter:on

    //@formatter:off
    @Throws(IOException::class)
    private fun emitRealmObjectProxyImplementation(writer: JavaWriter) {
        writer.emitAnnotation("Override")
                .beginMethod("ProxyState<?>", "realmGet\$proxyState", EnumSet.of(Modifier.PUBLIC))
                .emitStatement("return proxyState")
                .endMethod()
                .emitEmptyLine()
    }
    //@formatter:on

    @Throws(IOException::class)
    private fun emitCreateExpectedObjectSchemaInfo(writer: JavaWriter) {
        writer.beginMethod(
                "OsObjectSchemaInfo", // Return type
                "createExpectedObjectSchemaInfo", // Method name
                EnumSet.of(Modifier.PRIVATE, Modifier.STATIC)) // Modifiers

        // Guess capacity for Arrays used by OsObjectSchemaInfo.
        // Used to prevent array resizing at runtime
        val persistedFields = metadata.fields.size
        val computedFields = metadata.backlinkFields.size

        writer.emitStatement(
                "OsObjectSchemaInfo.Builder builder = new OsObjectSchemaInfo.Builder(\"%s\", %s, %s)",
                internalClassName, persistedFields, computedFields)

        // For each field generate corresponding table index constant
        for (field in metadata.fields) {
            val fieldName = field.internalFieldName

            val fieldType = getRealmTypeChecked(field)
            when (fieldType) {
                Constants.RealmFieldType.NOTYPE -> {
                }// Perhaps this should fail quickly?
                Constants.RealmFieldType.OBJECT -> {
                    val fieldTypeQualifiedName = Utils.getFieldTypeQualifiedName(field)
                    val internalClassName = Utils.getReferencedTypeInternalClassNameStatement(fieldTypeQualifiedName, classCollection)
                    writer.emitStatement("builder.addPersistedLinkProperty(\"%s\", RealmFieldType.OBJECT, %s)",
                            fieldName, internalClassName)
                }
                Constants.RealmFieldType.LIST -> {
                    val genericTypeQualifiedName = Utils.getGenericTypeQualifiedName(field)
                    val internalClassName = Utils.getReferencedTypeInternalClassNameStatement(genericTypeQualifiedName, classCollection)
                    writer.emitStatement("builder.addPersistedLinkProperty(\"%s\", RealmFieldType.LIST, %s)",
                            fieldName, internalClassName)
                }
                Constants.RealmFieldType.INTEGER_LIST, Constants.RealmFieldType.BOOLEAN_LIST, Constants.RealmFieldType.STRING_LIST, Constants.RealmFieldType.BINARY_LIST, Constants.RealmFieldType.DATE_LIST, Constants.RealmFieldType.FLOAT_LIST, Constants.RealmFieldType.DOUBLE_LIST -> writer.emitStatement("builder.addPersistedValueListProperty(\"%s\", %s, %s)",
                        fieldName, fieldType.realmType, if (metadata.isElementNullable(field)) "!Property.REQUIRED" else "Property.REQUIRED")

                Constants.RealmFieldType.BACKLINK -> throw IllegalArgumentException("LinkingObject field should not be added to metadata")

                Constants.RealmFieldType.INTEGER, Constants.RealmFieldType.FLOAT, Constants.RealmFieldType.DOUBLE, Constants.RealmFieldType.BOOLEAN, Constants.RealmFieldType.STRING, Constants.RealmFieldType.DATE, Constants.RealmFieldType.BINARY, Constants.RealmFieldType.REALM_INTEGER -> {
                    val nullableFlag = (if (metadata.isNullable(field)) "!" else "") + "Property.REQUIRED"
                    val indexedFlag = (if (metadata.isIndexed(field)) "" else "!") + "Property.INDEXED"
                    val primaryKeyFlag = (if (metadata.isPrimaryKey(field)) "" else "!") + "Property.PRIMARY_KEY"
                    writer.emitStatement("builder.addPersistedProperty(\"%s\", %s, %s, %s, %s)",
                            fieldName,
                            fieldType.realmType,
                            primaryKeyFlag,
                            indexedFlag,
                            nullableFlag)
                }

                else -> throw IllegalArgumentException("'fieldType' $fieldName is not handled")
            }
        }
        for (backlink in metadata.backlinkFields) {
            // Backlinks can only be created between classes in the current round of annotation processing
            // as the forward link cannot be created unless you know the type already.
            val sourceClass = classCollection.getClassFromQualifiedName(backlink.sourceClass!!)
            val targetField = backlink.targetField // Only in the model, so no internal name exists
            val internalSourceField = sourceClass.getInternalFieldName(backlink.sourceField!!)
            writer.emitStatement("builder.addComputedLinkProperty(\"%s\", \"%s\", \"%s\")",
                    targetField, sourceClass.internalClassName, internalSourceField)
        }
        writer.emitStatement("return builder.build()")
        writer.endMethod()
                .emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitGetExpectedObjectSchemaInfo(writer: JavaWriter) {
        writer.beginMethod(
                "OsObjectSchemaInfo", // Return type
                "getExpectedObjectSchemaInfo", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC)) // Modifiers

        writer.emitStatement("return expectedObjectSchemaInfo")

        writer.endMethod()
                .emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitCreateColumnInfoMethod(writer: JavaWriter) {
        writer.beginMethod(
                columnInfoClassName(), // Return type
                "createColumnInfo", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "OsSchemaInfo", "schemaInfo") // Argument type & argument name

        // create an instance of ColumnInfo
        writer.emitStatement("return new %1\$s(schemaInfo)", columnInfoClassName())

        writer.endMethod()
        writer.emitEmptyLine()
    }

    //@formatter:off
    @Throws(IOException::class)
    private fun emitGetSimpleClassNameMethod(writer: JavaWriter) {
        writer.beginMethod("String", "getSimpleClassName", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .emitStatement("return \"%s\"", internalClassName)
                .endMethod()
                .emitEmptyLine()

        // Helper class for the annotation processor so it can access the internal class name
        // without needing to load the parent class (which we cannot do as it transitively loads
        // native code, which cannot be loaded on the JVM).
        writer.beginType(
                "ClassNameHelper", // full qualified name of the item to generate
                "class", // the type of the item
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)) // modifiers to apply
        writer.emitField("String", "INTERNAL_CLASS_NAME", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL), "\"" + internalClassName + "\"")
        writer.endType()
        writer.emitEmptyLine()
    }
    //@formatter:on

    //@formatter:off
    @Throws(IOException::class)
    private fun emitNewProxyInstance(writer: JavaWriter) {
        writer
                .beginMethod(qualifiedGeneratedClassName,
                        "newProxyInstance",
                        EnumSet.of(Modifier.PRIVATE, Modifier.STATIC),
                        "BaseRealm", "realm",
                        "Row", "row")
                .emitSingleLineComment("Ignore default values to avoid creating uexpected objects from RealmModel/RealmList fields")
                .emitStatement("final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get()")
                .emitStatement("objectContext.set(realm, row, realm.getSchema().getColumnInfo(%s.class), false, Collections.<String>emptyList())", qualifiedJavaClassName)
                .emitStatement("%1\$s obj = new %1\$s()", qualifiedGeneratedClassName)
                .emitStatement("objectContext.clear()")
                .emitStatement("return obj")
                .endMethod()
                .emitEmptyLine()
    }
    //@formatter:on

    //@formatter:off
    @Throws(IOException::class)
    private fun emitCopyOrUpdateMethod(writer: JavaWriter) {
        writer.beginMethod(
                qualifiedJavaClassName, // Return type
                "copyOrUpdate", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", // Argument type & argument name
                columnInfoClassName(), "columnInfo",
                qualifiedJavaClassName, "object",
                "boolean", "update",
                "Map<RealmModel,RealmObjectProxy>", "cache",
                "Set<ImportFlag>", "flags"
        )

        writer
                .beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet\$proxyState().getRealm\$realm() != null)")
                .emitStatement("final BaseRealm otherRealm = ((RealmObjectProxy) object).realmGet\$proxyState().getRealm\$realm()")
                .beginControlFlow("if (otherRealm.threadId != realm.threadId)")
                .emitStatement("throw new IllegalArgumentException(\"Objects which belong to Realm instances in other threads cannot be copied into this Realm instance.\")")
                .endControlFlow()

                // If object is already in the Realm there is nothing to update
                .beginControlFlow("if (otherRealm.getPath().equals(realm.getPath()))")
                .emitStatement("return object")
                .endControlFlow()
                .endControlFlow()


        writer.emitStatement("final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get()")

        writer.emitStatement("RealmObjectProxy cachedRealmObject = cache.get(object)")
                .beginControlFlow("if (cachedRealmObject != null)")
                .emitStatement("return (%s) cachedRealmObject", qualifiedJavaClassName)
                .endControlFlow()
                .emitEmptyLine()

        if (!metadata.hasPrimaryKey()) {
            writer.emitStatement("return copy(realm, columnInfo, object, update, cache, flags)")
        } else {
            writer
                    .emitStatement("%s realmObject = null", qualifiedJavaClassName)
                    .emitStatement("boolean canUpdate = update")
                    .beginControlFlow("if (canUpdate)")
                    .emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName)
                    .emitStatement("long pkColumnIndex = %s", fieldIndexVariableReference(metadata.primaryKey))

            val primaryKeyGetter = metadata.primaryKeyGetter
            val primaryKeyElement = metadata.primaryKey
            if (metadata.isNullable(primaryKeyElement!!)) {
                if (Utils.isString(primaryKeyElement)) {
                    writer
                            .emitStatement("String value = ((%s) object).%s()", interfaceName, primaryKeyGetter)
                            .emitStatement("long rowIndex = Table.NO_MATCH")
                            .beginControlFlow("if (value == null)")
                            .emitStatement("rowIndex = table.findFirstNull(pkColumnIndex)")
                            .nextControlFlow("else")
                            .emitStatement("rowIndex = table.findFirstString(pkColumnIndex, value)")
                            .endControlFlow()
                } else {
                    writer
                            .emitStatement("Number value = ((%s) object).%s()", interfaceName, primaryKeyGetter)
                            .emitStatement("long rowIndex = Table.NO_MATCH")
                            .beginControlFlow("if (value == null)")
                            .emitStatement("rowIndex = table.findFirstNull(pkColumnIndex)")
                            .nextControlFlow("else")
                            .emitStatement("rowIndex = table.findFirstLong(pkColumnIndex, value.longValue())")
                            .endControlFlow()
                }
            } else {
                val pkType = if (Utils.isString(metadata.primaryKey)) "String" else "Long"
                writer.emitStatement("long rowIndex = table.findFirst%s(pkColumnIndex, ((%s) object).%s())",
                        pkType, interfaceName, primaryKeyGetter)
            }

            writer
                    .beginControlFlow("if (rowIndex == Table.NO_MATCH)")
                    .emitStatement("canUpdate = false")
                    .nextControlFlow("else")
                    .beginControlFlow("try")
                    .emitStatement("objectContext.set(realm, table.getUncheckedRow(rowIndex), columnInfo, false, Collections.<String> emptyList())")
                    .emitStatement("realmObject = new %s()", qualifiedGeneratedClassName)
                    .emitStatement("cache.put(object, (RealmObjectProxy) realmObject)")
                    .nextControlFlow("finally")
                    .emitStatement("objectContext.clear()")
                    .endControlFlow()
                    .endControlFlow()

            writer.endControlFlow()

            writer
                    .emitEmptyLine()
                    .emitStatement("return (canUpdate) ? update(realm, columnInfo, realmObject, object, cache, flags) : copy(realm, columnInfo, object, update, cache, flags)")
        }

        writer.endMethod()
                .emitEmptyLine()
    }
    //@formatter:on

    //@formatter:off
    @Throws(IOException::class)
    private fun setTableValues(writer: JavaWriter, fieldType: String, fieldName: String, interfaceName: String, getter: String, isUpdate: Boolean) {
        if (("long" == fieldType
                        || "int" == fieldType
                        || "short" == fieldType
                        || "byte" == fieldType)) {
            writer.emitStatement("Table.nativeSetLong(tableNativePtr, columnInfo.%sIndex, rowIndex, ((%s) object).%s(), false)", fieldName, interfaceName, getter)

        } else if (("java.lang.Long" == fieldType
                        || "java.lang.Integer" == fieldType
                        || "java.lang.Short" == fieldType
                        || "java.lang.Byte" == fieldType)) {
            writer
                    .emitStatement("Number %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetLong(tableNativePtr, columnInfo.%sIndex, rowIndex, %s.longValue(), false)", fieldName, getter)
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName)
            }
            writer.endControlFlow()

        } else if ("io.realm.MutableRealmInteger" == fieldType) {
            writer
                    .emitStatement("Long %s = ((%s) object).%s().get()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetLong(tableNativePtr, columnInfo.%sIndex, rowIndex, %s.longValue(), false)", fieldName, getter)
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName)
            }
            writer.endControlFlow()

        } else if ("double" == fieldType) {
            writer.emitStatement("Table.nativeSetDouble(tableNativePtr, columnInfo.%sIndex, rowIndex, ((%s) object).%s(), false)", fieldName, interfaceName, getter)

        } else if ("java.lang.Double" == fieldType) {
            writer
                    .emitStatement("Double %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetDouble(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter)
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName)
            }
            writer.endControlFlow()

        } else if ("float" == fieldType) {
            writer.emitStatement("Table.nativeSetFloat(tableNativePtr, columnInfo.%sIndex, rowIndex, ((%s) object).%s(), false)", fieldName, interfaceName, getter)

        } else if ("java.lang.Float" == fieldType) {
            writer
                    .emitStatement("Float %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetFloat(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter)
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName)
            }
            writer.endControlFlow()

        } else if ("boolean" == fieldType) {
            writer.emitStatement("Table.nativeSetBoolean(tableNativePtr, columnInfo.%sIndex, rowIndex, ((%s) object).%s(), false)", fieldName, interfaceName, getter)

        } else if ("java.lang.Boolean" == fieldType) {
            writer
                    .emitStatement("Boolean %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetBoolean(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter)
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName)
            }
            writer.endControlFlow()

        } else if ("byte[]" == fieldType) {
            writer
                    .emitStatement("byte[] %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetByteArray(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter)
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName)
            }
            writer.endControlFlow()


        } else if ("java.util.Date" == fieldType) {
            writer
                    .emitStatement("java.util.Date %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetTimestamp(tableNativePtr, columnInfo.%sIndex, rowIndex, %s.getTime(), false)", fieldName, getter)
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName)
            }
            writer.endControlFlow()

        } else if ("java.lang.String" == fieldType) {
            writer
                    .emitStatement("String %s = ((%s) object).%s()", getter, interfaceName, getter)
                    .beginControlFlow("if (%s != null)", getter)
                    .emitStatement("Table.nativeSetString(tableNativePtr, columnInfo.%sIndex, rowIndex, %s, false)", fieldName, getter)
            if (isUpdate) {
                writer.nextControlFlow("else")
                        .emitStatement("Table.nativeSetNull(tableNativePtr, columnInfo.%sIndex, rowIndex, false)", fieldName)
            }
            writer.endControlFlow()
        } else {
            throw IllegalStateException("Unsupported type $fieldType")
        }
    }
    //@formatter:on

    @Throws(IOException::class)
    private fun emitInsertMethod(writer: JavaWriter) {
        writer.beginMethod(
                "long", // Return type
                "insert", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", qualifiedJavaClassName, "object", "Map<RealmModel,Long>", "cache" // Argument type & argument name
        )

        // If object is already in the Realm there is nothing to update
        writer
                .beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet\$proxyState().getRealm\$realm() != null && ((RealmObjectProxy) object).realmGet\$proxyState().getRealm\$realm().getPath().equals(realm.getPath()))")
                .emitStatement("return ((RealmObjectProxy) object).realmGet\$proxyState().getRow\$realm().getIndex()")
                .endControlFlow()

        writer.emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName)
        writer.emitStatement("long tableNativePtr = table.getNativePtr()")
        writer.emitStatement("%s columnInfo = (%s) realm.getSchema().getColumnInfo(%s.class)",
                columnInfoClassName(), columnInfoClassName(), qualifiedJavaClassName)

        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("long pkColumnIndex = %s", fieldIndexVariableReference(metadata.primaryKey))
        }
        addPrimaryKeyCheckIfNeeded(metadata, true, writer)

        for (field in metadata.fields) {
            val fieldName = field.simpleName.toString()
            val fieldType = field.asType().toString()
            val getter = metadata.getInternalGetter(fieldName)

            //@formatter:off
            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitStatement("%s %sObj = ((%s) object).%s()", fieldType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sObj != null)", fieldName)
                        .emitStatement("Long cache%1\$s = cache.get(%1\$sObj)", fieldName)
                        .beginControlFlow("if (cache%s == null)", fieldName)
                        .emitStatement("cache%s = %s.insert(realm, %sObj, cache)",
                                fieldName,
                                Utils.getProxyClassSimpleName(field),
                                fieldName)
                        .endControlFlow()
                        .emitStatement("Table.nativeSetLink(tableNativePtr, columnInfo.%1\$sIndex, rowIndex, cache%1\$s, false)", fieldName)
                        .endControlFlow()
            } else if (Utils.isRealmModelList(field)) {
                val genericType = Utils.getGenericTypeQualifiedName(field)
                writer
                        .emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("OsList %1\$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1\$sIndex)", fieldName)
                        .beginControlFlow("for (%1\$s %2\$sItem : %2\$sList)", genericType, fieldName)
                        .emitStatement("Long cacheItemIndex%1\$s = cache.get(%1\$sItem)", fieldName)
                        .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                        .emitStatement("cacheItemIndex%1\$s = %2\$s.insert(realm, %1\$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("%1\$sOsList.addRow(cacheItemIndex%1\$s)", fieldName)
                        .endControlFlow()
                        .endControlFlow()
            } else if (Utils.isRealmValueList(field)) {
                val genericType = Utils.getGenericTypeQualifiedName(field)
                val elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field)
                writer
                        .emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("OsList %1\$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1\$sIndex)", fieldName)
                        .beginControlFlow("for (%1\$s %2\$sItem : %2\$sList)", genericType, fieldName)
                        .beginControlFlow("if (%1\$sItem == null)", fieldName)
                        .emitStatement(fieldName + "OsList.addNull()")
                        .nextControlFlow("else")
                        .emitStatement(getStatementForAppendingValueToOsList(fieldName + "OsList", fieldName + "Item", elementTypeMirror))
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow()
            } else {
                if (metadata.primaryKey !== field) {
                    setTableValues(writer, fieldType, fieldName, interfaceName, getter, false)
                }
            }
            //@formatter:on
        }

        writer.emitStatement("return rowIndex")
        writer.endMethod()
                .emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitInsertListMethod(writer: JavaWriter) {
        writer.beginMethod(
                "void", // Return type
                "insert", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", "Iterator<? extends RealmModel>", "objects", "Map<RealmModel,Long>", "cache" // Argument type & argument name
        )

        writer.emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName)
        writer.emitStatement("long tableNativePtr = table.getNativePtr()")
        writer.emitStatement("%s columnInfo = (%s) realm.getSchema().getColumnInfo(%s.class)",
                columnInfoClassName(), columnInfoClassName(), qualifiedJavaClassName)
        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("long pkColumnIndex = %s", fieldIndexVariableReference(metadata.primaryKey))
        }
        writer.emitStatement("%s object = null", qualifiedJavaClassName)

        writer.beginControlFlow("while (objects.hasNext())")
                .emitStatement("object = (%s) objects.next()", qualifiedJavaClassName)
        writer.beginControlFlow("if (cache.containsKey(object))")
                .emitStatement("continue")
                .endControlFlow()

        writer.beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet\$proxyState().getRealm\$realm() != null && ((RealmObjectProxy) object).realmGet\$proxyState().getRealm\$realm().getPath().equals(realm.getPath()))")
        writer.emitStatement("cache.put(object, ((RealmObjectProxy) object).realmGet\$proxyState().getRow\$realm().getIndex())")
                .emitStatement("continue")
        writer.endControlFlow()

        addPrimaryKeyCheckIfNeeded(metadata, true, writer)

        //@formatter:off
        for (field in metadata.fields) {
            val fieldName = field.simpleName.toString()
            val fieldType = field.asType().toString()
            val getter = metadata.getInternalGetter(fieldName)

            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitStatement("%s %sObj = ((%s) object).%s()", fieldType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sObj != null)", fieldName)
                        .emitStatement("Long cache%1\$s = cache.get(%1\$sObj)", fieldName)
                        .beginControlFlow("if (cache%s == null)", fieldName)
                        .emitStatement("cache%s = %s.insert(realm, %sObj, cache)",
                                fieldName,
                                Utils.getProxyClassSimpleName(field),
                                fieldName)
                        .endControlFlow()
                        .emitStatement("table.setLink(columnInfo.%1\$sIndex, rowIndex, cache%1\$s, false)", fieldName)
                        .endControlFlow()
            } else if (Utils.isRealmModelList(field)) {
                val genericType = Utils.getGenericTypeQualifiedName(field)
                writer
                        .emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("OsList %1\$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1\$sIndex)", fieldName)
                        .beginControlFlow("for (%1\$s %2\$sItem : %2\$sList)", genericType, fieldName)
                        .emitStatement("Long cacheItemIndex%1\$s = cache.get(%1\$sItem)", fieldName)
                        .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                        .emitStatement("cacheItemIndex%1\$s = %2\$s.insert(realm, %1\$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("%1\$sOsList.addRow(cacheItemIndex%1\$s)", fieldName)
                        .endControlFlow()
                        .endControlFlow()

            } else if (Utils.isRealmValueList(field)) {
                val genericType = Utils.getGenericTypeQualifiedName(field)
                val elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field)
                writer
                        .emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("OsList %1\$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1\$sIndex)", fieldName)
                        .beginControlFlow("for (%1\$s %2\$sItem : %2\$sList)", genericType, fieldName)
                        .beginControlFlow("if (%1\$sItem == null)", fieldName)
                        .emitStatement("%1\$sOsList.addNull()", fieldName)
                        .nextControlFlow("else")
                        .emitStatement(getStatementForAppendingValueToOsList(fieldName + "OsList", fieldName + "Item", elementTypeMirror))
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow()
            } else {
                if (metadata.primaryKey !== field) {
                    setTableValues(writer, fieldType, fieldName, interfaceName, getter, false)
                }
            }
        }
        //@formatter:on

        writer.endControlFlow()
        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitInsertOrUpdateMethod(writer: JavaWriter) {
        writer.beginMethod(
                "long", // Return type
                "insertOrUpdate", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", qualifiedJavaClassName, "object", "Map<RealmModel,Long>", "cache" // Argument type & argument name
        )

        // If object is already in the Realm there is nothing to update
        writer
                .beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet\$proxyState().getRealm\$realm() != null && ((RealmObjectProxy) object).realmGet\$proxyState().getRealm\$realm().getPath().equals(realm.getPath()))")
                .emitStatement("return ((RealmObjectProxy) object).realmGet\$proxyState().getRow\$realm().getIndex()")
                .endControlFlow()

        writer.emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName)
        writer.emitStatement("long tableNativePtr = table.getNativePtr()")
        writer.emitStatement("%s columnInfo = (%s) realm.getSchema().getColumnInfo(%s.class)",
                columnInfoClassName(), columnInfoClassName(), qualifiedJavaClassName)

        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("long pkColumnIndex = %s", fieldIndexVariableReference(metadata.primaryKey))
        }
        addPrimaryKeyCheckIfNeeded(metadata, false, writer)

        for (field in metadata.fields) {
            val fieldName = field.simpleName.toString()
            val fieldType = field.asType().toString()
            val getter = metadata.getInternalGetter(fieldName)

            //@formatter:off
            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitStatement("%s %sObj = ((%s) object).%s()", fieldType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sObj != null)", fieldName)
                        .emitStatement("Long cache%1\$s = cache.get(%1\$sObj)", fieldName)
                        .beginControlFlow("if (cache%s == null)", fieldName)
                        .emitStatement("cache%1\$s = %2\$s.insertOrUpdate(realm, %1\$sObj, cache)",
                                fieldName,
                                Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("Table.nativeSetLink(tableNativePtr, columnInfo.%1\$sIndex, rowIndex, cache%1\$s, false)", fieldName)
                        .nextControlFlow("else")
                        // No need to throw exception here if the field is not nullable. A exception will be thrown in setter.
                        .emitStatement("Table.nativeNullifyLink(tableNativePtr, columnInfo.%sIndex, rowIndex)", fieldName)
                        .endControlFlow()
            } else if (Utils.isRealmModelList(field)) {
                val genericType = Utils.getGenericTypeQualifiedName(field)
                writer
                        .emitEmptyLine()
                        .emitStatement("OsList %1\$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1\$sIndex)", fieldName)
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()", genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%1\$sList != null && %1\$sList.size() == %1\$sOsList.size())", fieldName)
                        .emitSingleLineComment("For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.")
                        .emitStatement("int objects = %1\$sList.size()", fieldName)
                        .beginControlFlow("for (int i = 0; i < objects; i++)")
                        .emitStatement("%1\$s %2\$sItem = %2\$sList.get(i)", genericType, fieldName)
                        .emitStatement("Long cacheItemIndex%1\$s = cache.get(%1\$sItem)", fieldName)
                        .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                        .emitStatement("cacheItemIndex%1\$s = %2\$s.insertOrUpdate(realm, %1\$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("%1\$sOsList.setRow(i, cacheItemIndex%1\$s)", fieldName)
                        .endControlFlow()
                        .nextControlFlow("else")
                        .emitStatement("%1\$sOsList.removeAll()", fieldName)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .beginControlFlow("for (%1\$s %2\$sItem : %2\$sList)", genericType, fieldName)
                        .emitStatement("Long cacheItemIndex%1\$s = cache.get(%1\$sItem)", fieldName)
                        .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                        .emitStatement("cacheItemIndex%1\$s = %2\$s.insertOrUpdate(realm, %1\$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("%1\$sOsList.addRow(cacheItemIndex%1\$s)", fieldName)
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow()
                        .emitEmptyLine()

            } else if (Utils.isRealmValueList(field)) {
                val genericType = Utils.getGenericTypeQualifiedName(field)
                val elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field)
                writer
                        .emitEmptyLine()
                        .emitStatement("OsList %1\$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1\$sIndex)", fieldName)
                        .emitStatement("%1\$sOsList.removeAll()", fieldName)
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .beginControlFlow("for (%1\$s %2\$sItem : %2\$sList)", genericType, fieldName)
                        .beginControlFlow("if (%1\$sItem == null)", fieldName)
                        .emitStatement("%1\$sOsList.addNull()", fieldName)
                        .nextControlFlow("else")
                        .emitStatement(getStatementForAppendingValueToOsList(fieldName + "OsList", fieldName + "Item", elementTypeMirror))
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow()
                        .emitEmptyLine()
            } else {
                if (metadata.primaryKey !== field) {
                    setTableValues(writer, fieldType, fieldName, interfaceName, getter, true)
                }
            }
            //@formatter:on
        }

        writer.emitStatement("return rowIndex")

        writer.endMethod()
                .emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitInsertOrUpdateListMethod(writer: JavaWriter) {
        writer.beginMethod(
                "void", // Return type
                "insertOrUpdate", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                "Realm", "realm", "Iterator<? extends RealmModel>", "objects", "Map<RealmModel,Long>", "cache" // Argument type & argument name
        )

        writer.emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName)
        writer.emitStatement("long tableNativePtr = table.getNativePtr()")
        writer.emitStatement("%s columnInfo = (%s) realm.getSchema().getColumnInfo(%s.class)",
                columnInfoClassName(), columnInfoClassName(), qualifiedJavaClassName)
        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("long pkColumnIndex = %s", fieldIndexVariableReference(metadata.primaryKey))
        }
        writer.emitStatement("%s object = null", qualifiedJavaClassName)

        writer.beginControlFlow("while (objects.hasNext())")
        writer.emitStatement("object = (%s) objects.next()", qualifiedJavaClassName)
        writer.beginControlFlow("if (cache.containsKey(object))")
                .emitStatement("continue")
                .endControlFlow()

        writer.beginControlFlow("if (object instanceof RealmObjectProxy && ((RealmObjectProxy) object).realmGet\$proxyState().getRealm\$realm() != null && ((RealmObjectProxy) object).realmGet\$proxyState().getRealm\$realm().getPath().equals(realm.getPath()))")
        writer.emitStatement("cache.put(object, ((RealmObjectProxy) object).realmGet\$proxyState().getRow\$realm().getIndex())")
                .emitStatement("continue")
        writer.endControlFlow()
        addPrimaryKeyCheckIfNeeded(metadata, false, writer)

        for (field in metadata.fields) {
            val fieldName = field.simpleName.toString()
            val fieldType = field.asType().toString()
            val getter = metadata.getInternalGetter(fieldName)

            //@formatter:off
            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitStatement("%s %sObj = ((%s) object).%s()", fieldType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sObj != null)", fieldName)
                        .emitStatement("Long cache%1\$s = cache.get(%1\$sObj)", fieldName)
                        .beginControlFlow("if (cache%s == null)", fieldName)
                        .emitStatement("cache%1\$s = %2\$s.insertOrUpdate(realm, %1\$sObj, cache)",
                                fieldName,
                                Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("Table.nativeSetLink(tableNativePtr, columnInfo.%1\$sIndex, rowIndex, cache%1\$s, false)", fieldName)
                        .nextControlFlow("else")
                        // No need to throw exception here if the field is not nullable. A exception will be thrown in setter.
                        .emitStatement("Table.nativeNullifyLink(tableNativePtr, columnInfo.%sIndex, rowIndex)", fieldName)
                        .endControlFlow()
            } else if (Utils.isRealmModelList(field)) {
                val genericType = Utils.getGenericTypeQualifiedName(field)
                writer
                        .emitEmptyLine()
                        .emitStatement("OsList %1\$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1\$sIndex)", fieldName)
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()", genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%1\$sList != null && %1\$sList.size() == %1\$sOsList.size())", fieldName)
                        .emitSingleLineComment("For lists of equal lengths, we need to set each element directly as clearing the receiver list can be wrong if the input and target list are the same.")
                        .emitStatement("int objectCount = %1\$sList.size()", fieldName)
                        .beginControlFlow("for (int i = 0; i < objectCount; i++)")
                        .emitStatement("%1\$s %2\$sItem = %2\$sList.get(i)", genericType, fieldName)
                        .emitStatement("Long cacheItemIndex%1\$s = cache.get(%1\$sItem)", fieldName)
                        .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                        .emitStatement("cacheItemIndex%1\$s = %2\$s.insertOrUpdate(realm, %1\$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("%1\$sOsList.setRow(i, cacheItemIndex%1\$s)", fieldName)
                        .endControlFlow()
                        .nextControlFlow("else")
                        .emitStatement("%1\$sOsList.removeAll()", fieldName)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .beginControlFlow("for (%1\$s %2\$sItem : %2\$sList)", genericType, fieldName)
                        .emitStatement("Long cacheItemIndex%1\$s = cache.get(%1\$sItem)", fieldName)
                        .beginControlFlow("if (cacheItemIndex%s == null)", fieldName)
                        .emitStatement("cacheItemIndex%1\$s = %2\$s.insertOrUpdate(realm, %1\$sItem, cache)", fieldName, Utils.getProxyClassSimpleName(field))
                        .endControlFlow()
                        .emitStatement("%1\$sOsList.addRow(cacheItemIndex%1\$s)", fieldName)
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow()
                        .emitEmptyLine()

            } else if (Utils.isRealmValueList(field)) {
                val genericType = Utils.getGenericTypeQualifiedName(field)
                val elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field)
                writer
                        .emitEmptyLine()
                        .emitStatement("OsList %1\$sOsList = new OsList(table.getUncheckedRow(rowIndex), columnInfo.%1\$sIndex)", fieldName)
                        .emitStatement("%1\$sOsList.removeAll()", fieldName)
                        .emitStatement("RealmList<%s> %sList = ((%s) object).%s()",
                                genericType, fieldName, interfaceName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .beginControlFlow("for (%1\$s %2\$sItem : %2\$sList)", genericType, fieldName)
                        .beginControlFlow("if (%1\$sItem == null)", fieldName)
                        .emitStatement("%1\$sOsList.addNull()", fieldName)
                        .nextControlFlow("else")
                        .emitStatement(getStatementForAppendingValueToOsList(fieldName + "OsList",
                                fieldName + "Item", elementTypeMirror))
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow()
                        .emitEmptyLine()
            } else {
                if (metadata.primaryKey !== field) {
                    setTableValues(writer, fieldType, fieldName, interfaceName, getter, true)
                }
            }
            //@formatter:on
        }
        writer.endControlFlow()

        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun addPrimaryKeyCheckIfNeeded(metadata: ClassMetaData, throwIfPrimaryKeyDuplicate: Boolean, writer: JavaWriter) {
        if (metadata.hasPrimaryKey()) {
            val primaryKeyGetter = metadata.primaryKeyGetter
            val primaryKeyElement = metadata.primaryKey
            if (metadata.isNullable(primaryKeyElement!!)) {
                //@formatter:off
                if (Utils.isString(primaryKeyElement)) {
                    writer
                            .emitStatement("String primaryKeyValue = ((%s) object).%s()", interfaceName, primaryKeyGetter)
                            .emitStatement("long rowIndex = Table.NO_MATCH")
                            .beginControlFlow("if (primaryKeyValue == null)")
                            .emitStatement("rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex)")
                            .nextControlFlow("else")
                            .emitStatement("rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, primaryKeyValue)")
                            .endControlFlow()
                } else {
                    writer
                            .emitStatement("Object primaryKeyValue = ((%s) object).%s()", interfaceName, primaryKeyGetter)
                            .emitStatement("long rowIndex = Table.NO_MATCH")
                            .beginControlFlow("if (primaryKeyValue == null)")
                            .emitStatement("rowIndex = Table.nativeFindFirstNull(tableNativePtr, pkColumnIndex)")
                            .nextControlFlow("else")
                            .emitStatement("rowIndex = Table.nativeFindFirstInt(tableNativePtr, pkColumnIndex, ((%s) object).%s())", interfaceName, primaryKeyGetter)
                            .endControlFlow()
                }
                //@formatter:on
            } else {
                writer.emitStatement("long rowIndex = Table.NO_MATCH")
                writer.emitStatement("Object primaryKeyValue = ((%s) object).%s()", interfaceName, primaryKeyGetter)
                writer.beginControlFlow("if (primaryKeyValue != null)")

                if (Utils.isString(metadata.primaryKey)) {
                    writer.emitStatement("rowIndex = Table.nativeFindFirstString(tableNativePtr, pkColumnIndex, (String)primaryKeyValue)")
                } else {
                    writer.emitStatement("rowIndex = Table.nativeFindFirstInt(tableNativePtr, pkColumnIndex, ((%s) object).%s())", interfaceName, primaryKeyGetter)
                }
                writer.endControlFlow()
            }

            writer.beginControlFlow("if (rowIndex == Table.NO_MATCH)")
            if (Utils.isString(metadata.primaryKey)) {
                writer.emitStatement(
                        "rowIndex = OsObject.createRowWithPrimaryKey(table, pkColumnIndex, primaryKeyValue)")
            } else {
                writer.emitStatement(
                        "rowIndex = OsObject.createRowWithPrimaryKey(table, pkColumnIndex, ((%s) object).%s())",
                        interfaceName, primaryKeyGetter)
            }

            if (throwIfPrimaryKeyDuplicate) {
                writer.nextControlFlow("else")
                writer.emitStatement("Table.throwDuplicatePrimaryKeyException(primaryKeyValue)")
            }

            writer.endControlFlow()
            writer.emitStatement("cache.put(object, rowIndex)")
        } else {
            writer.emitStatement("long rowIndex = OsObject.createRow(table)")
            writer.emitStatement("cache.put(object, rowIndex)")
        }
    }

    @Throws(IOException::class)
    private fun emitCopyMethod(writer: JavaWriter) {
        writer
                .beginMethod(
                        qualifiedJavaClassName, // Return type
                        "copy", // Method name
                        EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                        "Realm", "realm",
                        columnInfoClassName(), "columnInfo",
                        qualifiedJavaClassName, "newObject",
                        "boolean", "update",
                        "Map<RealmModel,RealmObjectProxy>", "cache",
                        "Set<ImportFlag>", "flags"

                ) // Argument type & argument name

        writer
                .emitStatement("RealmObjectProxy cachedRealmObject = cache.get(newObject)")
                .beginControlFlow("if (cachedRealmObject != null)")
                .emitStatement("return (%s) cachedRealmObject", qualifiedJavaClassName)
                .endControlFlow()
                .emitEmptyLine()

        writer
                .emitStatement("%1\$s realmObjectSource = (%1\$s) newObject", interfaceName)
                .emitEmptyLine()
                .emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName)
                .emitStatement("OsObjectBuilder builder = new OsObjectBuilder(table, columnInfo.maxColumnIndexValue, flags)")

        // Copy basic types
        writer
                .emitEmptyLine()
                .emitSingleLineComment("Add all non-\"object reference\" fields")
        for (field in metadata.getBasicTypeFields()) {
            val fieldIndex = fieldIndexVariableReference(field)
            val fieldName = field.simpleName.toString()
            val getter = metadata.getInternalGetter(fieldName)
            writer.emitStatement("builder.%s(%s, realmObjectSource.%s())", OsObjectBuilderTypeHelper.getOsObjectBuilderName(field), fieldIndex, getter)
        }

        // Create the underlying object
        writer
                .emitEmptyLine()
                .emitSingleLineComment("Create the underlying object and cache it before setting any object/objectlist references")
                .emitSingleLineComment("This will allow us to break any circular dependencies by using the object cache.")
                .emitStatement("Row row = builder.createNewObject()")
                .emitStatement("%s realmObjectCopy = newProxyInstance(realm, row)", qualifiedGeneratedClassName)
                .emitStatement("cache.put(newObject, realmObjectCopy)")

        // Copy all object references or lists-of-objects
        writer.emitEmptyLine()
        if (!metadata.getObjectReferenceFields().isEmpty()) {
            writer.emitSingleLineComment("Finally add all fields that reference other Realm Objects, either directly or through a list")
        }
        for (field in metadata.getObjectReferenceFields()) {
            val fieldType = field.asType().toString()
            val fieldName = field.simpleName.toString()
            val getter = metadata.getInternalGetter(fieldName)
            val setter = metadata.getInternalSetter(fieldName)

            if (Utils.isRealmModel(field)) {
                writer
                        .emitStatement("%s %sObj = realmObjectSource.%s()", fieldType, fieldName, getter)
                        .beginControlFlow("if (%sObj == null)", fieldName)
                        .emitStatement("realmObjectCopy.%s(null)", setter)
                        .nextControlFlow("else")
                        .emitStatement("%s cache%s = (%s) cache.get(%sObj)", fieldType, fieldName, fieldType, fieldName)
                        .beginControlFlow("if (cache%s != null)", fieldName)
                        .emitStatement("realmObjectCopy.%s(cache%s)", setter, fieldName)
                        .nextControlFlow("else")
                        .emitStatement("realmObjectCopy.%s(%s.copyOrUpdate(realm, (%s) realm.getSchema().getColumnInfo(%s.class), %sObj, update, cache, flags))",
                                setter, Utils.getProxyClassSimpleName(field), columnInfoClassName(field), Utils.getFieldTypeQualifiedName(field), fieldName)
                        .endControlFlow()
                        // No need to throw exception here if the field is not nullable. A exception will be thrown in setter.
                        .endControlFlow()
                        .emitEmptyLine()

            } else if (Utils.isRealmModelList(field)) {
                val genericType = Utils.getGenericTypeQualifiedName(field)
                writer
                        .emitStatement("RealmList<%s> %sList = realmObjectSource.%s()", genericType, fieldName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("RealmList<%s> %sRealmList = realmObjectCopy.%s()",
                                genericType, fieldName, getter)
                        // Clear is needed. See bug https://github.com/realm/realm-java/issues/4957
                        .emitStatement("%sRealmList.clear()", fieldName)
                        .beginControlFlow("for (int i = 0; i < %sList.size(); i++)", fieldName)
                        .emitStatement("%1\$s %2\$sItem = %2\$sList.get(i)", genericType, fieldName)
                        .emitStatement("%1\$s cache%2\$s = (%1\$s) cache.get(%2\$sItem)", genericType, fieldName)
                        .beginControlFlow("if (cache%s != null)", fieldName)
                        .emitStatement("%1\$sRealmList.add(cache%1\$s)", fieldName)
                        .nextControlFlow("else")
                        .emitStatement("%1\$sRealmList.add(%2\$s.copyOrUpdate(realm, (%3\$s) realm.getSchema().getColumnInfo(%4\$s.class), %1\$sItem, update, cache, flags))",
                                fieldName, Utils.getProxyClassSimpleName(field), columnInfoClassName(field), Utils.getGenericTypeQualifiedName(field))
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow()
                        .emitEmptyLine()
            } else {
                throw IllegalStateException("Unsupported field: $field")
            }
        }

        writer
                .emitStatement("return realmObjectCopy")
                .endMethod()
                .emitEmptyLine()
    }

    //@formatter:off
    @Throws(IOException::class)
    private fun emitCreateDetachedCopyMethod(writer: JavaWriter) {
        writer.beginMethod(
                qualifiedJavaClassName, // Return type
                "createDetachedCopy", // Method name
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), // Modifiers
                qualifiedJavaClassName, "realmObject", "int", "currentDepth", "int", "maxDepth", "Map<RealmModel, CacheData<RealmModel>>", "cache")
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
                .endControlFlow()

        // may cause an unused variable warning if the object contains only null lists
        writer.emitStatement("%1\$s unmanagedCopy = (%1\$s) unmanagedObject", interfaceName)
                .emitStatement("%1\$s realmSource = (%1\$s) realmObject", interfaceName)

        for (field in metadata.fields) {
            val fieldName = field.simpleName.toString()
            val setter = metadata.getInternalSetter(fieldName)
            val getter = metadata.getInternalGetter(fieldName)

            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitSingleLineComment("Deep copy of %s", fieldName)
                        .emitStatement("unmanagedCopy.%s(%s.createDetachedCopy(realmSource.%s(), currentDepth + 1, maxDepth, cache))",
                                setter, Utils.getProxyClassSimpleName(field), getter)
            } else if (Utils.isRealmModelList(field)) {
                writer
                        .emitEmptyLine()
                        .emitSingleLineComment("Deep copy of %s", fieldName)
                        .beginControlFlow("if (currentDepth == maxDepth)")
                        .emitStatement("unmanagedCopy.%s(null)", setter)
                        .nextControlFlow("else")
                        .emitStatement("RealmList<%s> managed%sList = realmSource.%s()",
                                Utils.getGenericTypeQualifiedName(field), fieldName, getter)
                        .emitStatement("RealmList<%1\$s> unmanaged%2\$sList = new RealmList<%1\$s>()", Utils.getGenericTypeQualifiedName(field), fieldName)
                        .emitStatement("unmanagedCopy.%s(unmanaged%sList)", setter, fieldName)
                        .emitStatement("int nextDepth = currentDepth + 1")
                        .emitStatement("int size = managed%sList.size()", fieldName)
                        .beginControlFlow("for (int i = 0; i < size; i++)")
                        .emitStatement("%s item = %s.createDetachedCopy(managed%sList.get(i), nextDepth, maxDepth, cache)",
                                Utils.getGenericTypeQualifiedName(field), Utils.getProxyClassSimpleName(field), fieldName)
                        .emitStatement("unmanaged%sList.add(item)", fieldName)
                        .endControlFlow()
                        .endControlFlow()
            } else if (Utils.isRealmValueList(field)) {
                writer
                        .emitEmptyLine()
                        .emitStatement("unmanagedCopy.%1\$s(new RealmList<%2\$s>())", setter, Utils.getGenericTypeQualifiedName(field))
                        .emitStatement("unmanagedCopy.%1\$s().addAll(realmSource.%1\$s())", getter)
            } else if (Utils.isMutableRealmInteger(field)) {
                // If the user initializes the unmanaged MutableRealmInteger to null, this will fail mysteriously.
                writer.emitStatement("unmanagedCopy.%s().set(realmSource.%s().get())", getter, getter)
            } else {
                writer.emitStatement("unmanagedCopy.%s(realmSource.%s())", setter, getter)
            }
        }

        writer.emitEmptyLine()
        writer.emitStatement("return unmanagedObject")
        writer.endMethod()
        writer.emitEmptyLine()
    }
    //@formatter:on

    @Throws(IOException::class)
    private fun emitUpdateMethod(writer: JavaWriter) {
        if (!metadata.hasPrimaryKey()) {
            return
        }

        writer.beginMethod(
                qualifiedJavaClassName, // Return type
                "update", // Method name
                EnumSet.of(Modifier.STATIC), // Modifiers
                "Realm", "realm", // Argument type & argument name
                columnInfoClassName(), "columnInfo",
                qualifiedJavaClassName, "realmObject",
                qualifiedJavaClassName, "newObject",
                "Map<RealmModel, RealmObjectProxy>", "cache",
                "Set<ImportFlag>", "flags"
        )

        writer
                .emitStatement("%1\$s realmObjectTarget = (%1\$s) realmObject", interfaceName)
                .emitStatement("%1\$s realmObjectSource = (%1\$s) newObject", interfaceName)
                .emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName)
                .emitStatement("OsObjectBuilder builder = new OsObjectBuilder(table, columnInfo.maxColumnIndexValue, flags)")


        for (field in metadata.fields) {
            val fieldType = field.asType().toString()
            val fieldName = field.simpleName.toString()
            val getter = metadata.getInternalGetter(fieldName)
            val fieldIndex = fieldIndexVariableReference(field)

            if (Utils.isRealmModel(field)) {
                writer
                        .emitEmptyLine()
                        .emitStatement("%s %sObj = realmObjectSource.%s()", fieldType, fieldName, getter)
                        .beginControlFlow("if (%sObj == null)", fieldName)
                        .emitStatement("builder.addNull(%s)", fieldIndexVariableReference(field))
                        .nextControlFlow("else")
                        .emitStatement("%s cache%s = (%s) cache.get(%sObj)", fieldType, fieldName, fieldType, fieldName)
                        .beginControlFlow("if (cache%s != null)", fieldName)
                        .emitStatement("builder.addObject(%s, cache%s)", fieldIndex, fieldName)
                        .nextControlFlow("else")
                        .emitStatement("builder.addObject(%s, %s.copyOrUpdate(realm, (%s) realm.getSchema().getColumnInfo(%s.class), %sObj, true, cache, flags))",
                                fieldIndex, Utils.getProxyClassSimpleName(field), columnInfoClassName(field), Utils.getFieldTypeQualifiedName(field), fieldName)
                        .endControlFlow()
                        // No need to throw exception here if the field is not nullable. A exception will be thrown in setter.
                        .endControlFlow()
            } else if (Utils.isRealmModelList(field)) {
                val genericType = Utils.getGenericTypeQualifiedName(field)
                writer
                        .emitEmptyLine()
                        .emitStatement("RealmList<%s> %sList = realmObjectSource.%s()", genericType, fieldName, getter)
                        .beginControlFlow("if (%sList != null)", fieldName)
                        .emitStatement("RealmList<%s> %sManagedCopy = new RealmList<%s>()", genericType, fieldName, genericType)
                        .beginControlFlow("for (int i = 0; i < %sList.size(); i++)", fieldName)
                        .emitStatement("%1\$s %2\$sItem = %2\$sList.get(i)", genericType, fieldName)
                        .emitStatement("%1\$s cache%2\$s = (%1\$s) cache.get(%2\$sItem)", genericType, fieldName)
                        .beginControlFlow("if (cache%s != null)", fieldName)
                        .emitStatement("%1\$sManagedCopy.add(cache%1\$s)", fieldName)
                        .nextControlFlow("else")
                        .emitStatement("%1\$sManagedCopy.add(%2\$s.copyOrUpdate(realm, (%3\$s) realm.getSchema().getColumnInfo(%4\$s.class), %1\$sItem, true, cache, flags))",
                                fieldName, Utils.getProxyClassSimpleName(field), columnInfoClassName(field), Utils.getGenericTypeQualifiedName(field))
                        .endControlFlow()
                        .endControlFlow()
                        .emitStatement("builder.addObjectList(%s, %sManagedCopy)", fieldIndex, fieldName)
                        .nextControlFlow("else")
                        .emitStatement("builder.addObjectList(%s, new RealmList<%s>())", fieldIndex, genericType)
                        .endControlFlow()
            } else {
                writer
                        .emitStatement("builder.%s(%s, realmObjectSource.%s())", OsObjectBuilderTypeHelper.getOsObjectBuilderName(field), fieldIndex, getter)
            }
        }

        writer
                .emitEmptyLine()
                .emitStatement("builder.updateExistingObject()")
                .emitStatement("return realmObject")

        writer
                .endMethod()
                .emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitToStringMethod(writer: JavaWriter) {
        if (metadata.containsToString()) {
            return
        }
        writer.emitAnnotation("Override")
        writer.emitAnnotation("SuppressWarnings", "\"ArrayToString\"")
                .beginMethod("String", "toString", EnumSet.of(Modifier.PUBLIC))
                .beginControlFlow("if (!RealmObject.isValid(this))")
                .emitStatement("return \"Invalid object\"")
                .endControlFlow()
        writer.emitStatement("StringBuilder stringBuilder = new StringBuilder(\"%s = proxy[\")", simpleJavaClassName)

        val fields = metadata.fields
        var i = fields.size - 1
        for (field in fields) {
            val fieldName = field.simpleName.toString()

            writer.emitStatement("stringBuilder.append(\"{%s:\")", fieldName)
            if (Utils.isRealmModel(field)) {
                val fieldTypeSimpleName = Utils.stripPackage(Utils.getFieldTypeQualifiedName(field))
                writer.emitStatement(
                        "stringBuilder.append(%s() != null ? \"%s\" : \"null\")",
                        metadata.getInternalGetter(fieldName),
                        fieldTypeSimpleName
                )
            } else if (Utils.isRealmList(field)) {
                val genericTypeSimpleName = Utils.stripPackage(Utils.getGenericTypeQualifiedName(field)!!)
                writer.emitStatement("stringBuilder.append(\"RealmList<%s>[\").append(%s().size()).append(\"]\")",
                        genericTypeSimpleName,
                        metadata.getInternalGetter(fieldName))
            } else if (Utils.isMutableRealmInteger(field)) {
                writer.emitStatement("stringBuilder.append(%s().get())", metadata.getInternalGetter(fieldName))
            } else {
                if (metadata.isNullable(field)) {
                    writer.emitStatement("stringBuilder.append(%s() != null ? %s() : \"null\")",
                            metadata.getInternalGetter(fieldName),
                            metadata.getInternalGetter(fieldName)
                    )
                } else {
                    writer.emitStatement("stringBuilder.append(%s())", metadata.getInternalGetter(fieldName))
                }
            }
            writer.emitStatement("stringBuilder.append(\"}\")")

            if (i-- > 0) {
                writer.emitStatement("stringBuilder.append(\",\")")
            }
        }

        writer.emitStatement("stringBuilder.append(\"]\")")
        writer.emitStatement("return stringBuilder.toString()")
        writer.endMethod()
                .emitEmptyLine()
    }

    /**
     * Currently, the hash value emitted from this could suddenly change as an object's index might
     * alternate due to Realm Java using `Table#moveLastOver()`. Hash codes should therefore not
     * be considered stable, i.e. don't save them in a HashSet or use them as a key in a HashMap.
     */
    //@formatter:off
    @Throws(IOException::class)
    private fun emitHashcodeMethod(writer: JavaWriter) {
        if (metadata.containsHashCode()) {
            return
        }
        writer.emitAnnotation("Override")
                .beginMethod("int", "hashCode", EnumSet.of(Modifier.PUBLIC))
                .emitStatement("String realmName = proxyState.getRealm\$realm().getPath()")
                .emitStatement("String tableName = proxyState.getRow\$realm().getTable().getName()")
                .emitStatement("long rowIndex = proxyState.getRow\$realm().getIndex()")
                .emitEmptyLine()
                .emitStatement("int result = 17")
                .emitStatement("result = 31 * result + ((realmName != null) ? realmName.hashCode() : 0)")
                .emitStatement("result = 31 * result + ((tableName != null) ? tableName.hashCode() : 0)")
                .emitStatement("result = 31 * result + (int) (rowIndex ^ (rowIndex >>> 32))")
                .emitStatement("return result")
                .endMethod()
                .emitEmptyLine()
    }
    //@formatter:on

    //@formatter:off
    @Throws(IOException::class)
    private fun emitEqualsMethod(writer: JavaWriter) {
        if (metadata.containsEquals()) {
            return
        }
        val proxyClassName = Utils.getProxyClassName(qualifiedJavaClassName)
        val otherObjectVarName = "a$simpleJavaClassName"
        writer.emitAnnotation("Override")
                .beginMethod("boolean", "equals", EnumSet.of(Modifier.PUBLIC), "Object", "o")
                .emitStatement("if (this == o) return true")
                .emitStatement("if (o == null || getClass() != o.getClass()) return false")
                .emitStatement("%s %s = (%s)o", proxyClassName, otherObjectVarName, proxyClassName)  // FooRealmProxy aFoo = (FooRealmProxy)o
                .emitEmptyLine()
                .emitStatement("String path = proxyState.getRealm\$realm().getPath()")
                .emitStatement("String otherPath = %s.proxyState.getRealm\$realm().getPath()", otherObjectVarName)
                .emitStatement("if (path != null ? !path.equals(otherPath) : otherPath != null) return false")
                .emitEmptyLine()
                .emitStatement("String tableName = proxyState.getRow\$realm().getTable().getName()")
                .emitStatement("String otherTableName = %s.proxyState.getRow\$realm().getTable().getName()", otherObjectVarName)
                .emitStatement("if (tableName != null ? !tableName.equals(otherTableName) : otherTableName != null) return false")
                .emitEmptyLine()
                .emitStatement("if (proxyState.getRow\$realm().getIndex() != %s.proxyState.getRow\$realm().getIndex()) return false", otherObjectVarName)
                .emitEmptyLine()
                .emitStatement("return true")
                .endMethod()
    }
    //@formatter:on

    @Throws(IOException::class)
    private fun emitCreateOrUpdateUsingJsonObject(writer: JavaWriter) {
        writer.emitAnnotation("SuppressWarnings", "\"cast\"")
        writer.beginMethod(
                qualifiedJavaClassName,
                "createOrUpdateUsingJsonObject",
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
                Arrays.asList("Realm", "realm", "JSONObject", "json", "boolean", "update"),
                listOf("JSONException"))

        val modelOrListCount = countModelOrListFields(metadata.fields)
        if (modelOrListCount == 0) {
            writer.emitStatement("final List<String> excludeFields = Collections.<String> emptyList()")
        } else {
            writer.emitStatement("final List<String> excludeFields = new ArrayList<String>(%1\$d)",
                    modelOrListCount)
        }

        //@formatter:off
        if (!metadata.hasPrimaryKey()) {
            buildExcludeFieldsList(writer, metadata.fields)
            writer.emitStatement("%s obj = realm.createObjectInternal(%s.class, true, excludeFields)",
                    qualifiedJavaClassName, qualifiedJavaClassName)
        } else {
            val pkType = if (Utils.isString(metadata.primaryKey)) "String" else "Long"
            writer
                    .emitStatement("%s obj = null", qualifiedJavaClassName)
                    .beginControlFlow("if (update)")
                    .emitStatement("Table table = realm.getTable(%s.class)", qualifiedJavaClassName)
                    .emitStatement("%s columnInfo = (%s) realm.getSchema().getColumnInfo(%s.class)",
                            columnInfoClassName(), columnInfoClassName(), qualifiedJavaClassName)
                    .emitStatement("long pkColumnIndex = %s", fieldIndexVariableReference(metadata.primaryKey))
                    .emitStatement("long rowIndex = Table.NO_MATCH")
            if (metadata.isNullable(metadata.primaryKey!!)) {
                writer
                        .beginControlFlow("if (json.isNull(\"%s\"))", metadata.primaryKey!!.simpleName)
                        .emitStatement("rowIndex = table.findFirstNull(pkColumnIndex)")
                        .nextControlFlow("else")
                        .emitStatement(
                                "rowIndex = table.findFirst%s(pkColumnIndex, json.get%s(\"%s\"))",
                                pkType, pkType, metadata.primaryKey!!.simpleName)
                        .endControlFlow()
            } else {
                writer
                        .beginControlFlow("if (!json.isNull(\"%s\"))", metadata.primaryKey!!.simpleName)
                        .emitStatement(
                                "rowIndex = table.findFirst%s(pkColumnIndex, json.get%s(\"%s\"))",
                                pkType, pkType, metadata.primaryKey!!.simpleName)
                        .endControlFlow()
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
                    .endControlFlow()

            writer.beginControlFlow("if (obj == null)")
            buildExcludeFieldsList(writer, metadata.fields)
            val primaryKeyFieldType = metadata.primaryKey!!.asType().toString()
            val primaryKeyFieldName = metadata.primaryKey!!.simpleName.toString()
            RealmJsonTypeHelper.emitCreateObjectWithPrimaryKeyValue(
                    qualifiedJavaClassName, qualifiedGeneratedClassName, primaryKeyFieldType, primaryKeyFieldName, writer)
            writer.endControlFlow()
        }
        //@formatter:on

        writer
                .emitEmptyLine()
                .emitStatement("final %1\$s objProxy = (%1\$s) obj", interfaceName)
        for (field in metadata.fields) {
            val fieldName = field.simpleName.toString()
            val qualifiedFieldType = field.asType().toString()
            if (metadata.isPrimaryKey(field)) {
                // Primary key has already been set when adding new row or finding the existing row.
                continue
            }
            if (Utils.isRealmModel(field)) {
                RealmJsonTypeHelper.emitFillRealmObjectWithJsonValue(
                        "objProxy",
                        metadata.getInternalSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        Utils.getProxyClassSimpleName(field),
                        writer
                )

            } else if (Utils.isRealmModelList(field)) {
                RealmJsonTypeHelper.emitFillRealmListWithJsonValue(
                        "objProxy",
                        metadata.getInternalGetter(fieldName),
                        metadata.getInternalSetter(fieldName),
                        fieldName,
                        (field.asType() as DeclaredType).typeArguments[0].toString(),
                        Utils.getProxyClassSimpleName(field),
                        writer)

            } else if (Utils.isRealmValueList(field)) {
                writer.emitStatement("ProxyUtils.setRealmListWithJsonObject(objProxy.%1\$s(), json, \"%2\$s\")",
                        metadata.getInternalGetter(fieldName), fieldName)
            } else if (Utils.isMutableRealmInteger(field)) {
                RealmJsonTypeHelper.emitFillJavaTypeWithJsonValue(
                        "objProxy",
                        metadata.getInternalGetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        writer)

            } else {
                RealmJsonTypeHelper.emitFillJavaTypeWithJsonValue(
                        "objProxy",
                        metadata.getInternalSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        writer
                )
            }
        }

        writer.emitStatement("return obj")
        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun buildExcludeFieldsList(writer: JavaWriter, fields: Collection<RealmFieldElement>) {
        for (field in fields) {
            if (Utils.isRealmModel(field) || Utils.isRealmList(field)) {
                val fieldName = field.simpleName.toString()
                writer.beginControlFlow("if (json.has(\"%1\$s\"))", fieldName)
                        .emitStatement("excludeFields.add(\"%1\$s\")", fieldName)
                        .endControlFlow()
            }
        }
    }

    // Since we need to check the PK in stream before creating the object, this is now using copyToRealm
    // instead of createObject() to avoid parsing the stream twice.
    @Throws(IOException::class)
    private fun emitCreateUsingJsonStream(writer: JavaWriter) {
        writer.emitAnnotation("SuppressWarnings", "\"cast\"")
        writer.emitAnnotation("TargetApi", "Build.VERSION_CODES.HONEYCOMB")
        writer.beginMethod(
                qualifiedJavaClassName,
                "createUsingJsonStream",
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
                Arrays.asList("Realm", "realm", "JsonReader", "reader"),
                listOf("IOException"))

        if (metadata.hasPrimaryKey()) {
            writer.emitStatement("boolean jsonHasPrimaryKey = false")
        }
        writer.emitStatement("final %s obj = new %s()", qualifiedJavaClassName, qualifiedJavaClassName)
        writer.emitStatement("final %1\$s objProxy = (%1\$s) obj", interfaceName)
        writer.emitStatement("reader.beginObject()")
        writer.beginControlFlow("while (reader.hasNext())")
        writer.emitStatement("String name = reader.nextName()")
        writer.beginControlFlow("if (false)")
        val fields = metadata.fields
        for (field in fields) {
            val fieldName = field.simpleName.toString()
            val qualifiedFieldType = field.asType().toString()
            writer.nextControlFlow("else if (name.equals(\"%s\"))", fieldName)

            if (Utils.isRealmModel(field)) {
                RealmJsonTypeHelper.emitFillRealmObjectFromStream(
                        "objProxy",
                        metadata.getInternalSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        Utils.getProxyClassSimpleName(field),
                        writer
                )

            } else if (Utils.isRealmModelList(field)) {
                RealmJsonTypeHelper.emitFillRealmListFromStream(
                        "objProxy",
                        metadata.getInternalGetter(fieldName),
                        metadata.getInternalSetter(fieldName),
                        (field.asType() as DeclaredType).typeArguments[0].toString(),
                        Utils.getProxyClassSimpleName(field),
                        writer)

            } else if (Utils.isRealmValueList(field)) {
                writer.emitStatement("objProxy.%1\$s(ProxyUtils.createRealmListWithJsonStream(%2\$s.class, reader))",
                        metadata.getInternalSetter(fieldName),
                        Utils.getRealmListType(field))
            } else if (Utils.isMutableRealmInteger(field)) {
                RealmJsonTypeHelper.emitFillJavaTypeFromStream(
                        "objProxy",
                        metadata,
                        metadata.getInternalGetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        writer
                )
            } else {
                RealmJsonTypeHelper.emitFillJavaTypeFromStream(
                        "objProxy",
                        metadata,
                        metadata.getInternalSetter(fieldName),
                        fieldName,
                        qualifiedFieldType,
                        writer
                )
            }
        }

        writer.nextControlFlow("else")
        writer.emitStatement("reader.skipValue()")
        writer.endControlFlow()

        writer.endControlFlow()
        writer.emitStatement("reader.endObject()")

        if (metadata.hasPrimaryKey()) {
            writer.beginControlFlow("if (!jsonHasPrimaryKey)")
                    .emitStatement(Constants.STATEMENT_EXCEPTION_NO_PRIMARY_KEY_IN_JSON, metadata.primaryKey)
                    .endControlFlow()
        }

        writer.emitStatement("return realm.copyToRealm(obj)")
        writer.endMethod()
        writer.emitEmptyLine()
    }

    private fun columnInfoClassName(): String {
        return simpleJavaClassName + "ColumnInfo"
    }

    /**
     * Returns the name of the ColumnInfo class for the model class referenced in the field.
     * I.e. for `com.test.Person`, it returns `Person.PersonColumnInfo`
     */
    private fun columnInfoClassName(field: VariableElement): String {
        val qualfiedModelClassName = Utils.getModelClassQualifiedName(field)
        return Utils.getSimpleColumnInfoClassName(qualfiedModelClassName)
    }

    private fun columnIndexVarName(variableElement: VariableElement): String {
        return variableElement.simpleName.toString() + "Index"
    }

    private fun mutableRealmIntegerFieldName(variableElement: VariableElement): String {
        return variableElement.simpleName.toString() + "MutableRealmInteger"
    }

    private fun fieldIndexVariableReference(variableElement: VariableElement?): String {
        return "columnInfo." + columnIndexVarName(variableElement!!)
    }

    private fun getRealmType(field: VariableElement): Constants.RealmFieldType {
        val fieldTypeCanonicalName = field.asType().toString()
        val type = Constants.JAVA_TO_REALM_TYPES[fieldTypeCanonicalName]
        if (type != null) {
            return type
        }
        if (Utils.isMutableRealmInteger(field)) {
            return Constants.RealmFieldType.REALM_INTEGER
        }
        if (Utils.isRealmModel(field)) {
            return Constants.RealmFieldType.OBJECT
        }
        if (Utils.isRealmModelList(field)) {
            return Constants.RealmFieldType.LIST
        }
        if (Utils.isRealmValueList(field)) {
            val fieldType = Utils.getValueListFieldType(field)
                    ?: return Constants.RealmFieldType.NOTYPE
            return fieldType
        }
        return Constants.RealmFieldType.NOTYPE
    }

    private fun getRealmTypeChecked(field: VariableElement): Constants.RealmFieldType {
        val type = getRealmType(field)
        if (type === Constants.RealmFieldType.NOTYPE) {
            throw IllegalStateException("Unsupported type " + field.asType().toString())
        }
        return type
    }

    companion object {
        private val OPTION_SUPPRESS_WARNINGS = "realm.suppressWarnings"
        private val BACKLINKS_FIELD_EXTENSION = "Backlinks"

        private val IMPORTS: List<String>

        init {
            val l = Arrays.asList(
                    "android.annotation.TargetApi",
                    "android.os.Build",
                    "android.util.JsonReader",
                    "android.util.JsonToken",
                    "io.realm.ImportFlag",
                    "io.realm.exceptions.RealmMigrationNeededException",
                    "io.realm.internal.ColumnInfo",
                    "io.realm.internal.OsList",
                    "io.realm.internal.OsObject",
                    "io.realm.internal.OsSchemaInfo",
                    "io.realm.internal.OsObjectSchemaInfo",
                    "io.realm.internal.Property",
                    "io.realm.internal.objectstore.OsObjectBuilder",
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
                    "java.util.Set",
                    "org.json.JSONObject",
                    "org.json.JSONException",
                    "org.json.JSONArray")
            IMPORTS = Collections.unmodifiableList(l)
        }

        private fun countModelOrListFields(fields: Collection<RealmFieldElement>): Int {
            var count = 0
            for (f in fields) {
                if (Utils.isRealmModel(f) || Utils.isRealmList(f)) {
                    count++
                }
            }
            return count
        }
    }
}
