/*
 * Copyright 2015 Realm Inc.
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
import javax.tools.JavaFileObject

import io.realm.annotations.RealmModule

class RealmProxyMediatorGenerator(private val processingEnvironment: ProcessingEnvironment,
                                  private val className: String, classesToValidate: Set<ClassMetaData>) {
    private val qualifiedModelClasses = ArrayList<String>()
    private val qualifiedProxyClasses = ArrayList<String>()
    private val simpleModelClassNames = ArrayList<String>()
    private val internalClassNames = ArrayList<String>()


    init {

        for (metadata in classesToValidate) {
            qualifiedModelClasses.add(metadata.fullyQualifiedClassName)
            val qualifiedProxyClassName = Constants.REALM_PACKAGE_NAME + "." + Utils.getProxyClassName(metadata.fullyQualifiedClassName)
            qualifiedProxyClasses.add(qualifiedProxyClassName)
            simpleModelClassNames.add(metadata.simpleJavaClassName)
            internalClassNames.add(metadata.internalClassName!!)
        }
    }

    @Throws(IOException::class)
    fun generate() {
        val qualifiedGeneratedClassName = String.format(Locale.US, "%s.%sMediator", Constants.REALM_PACKAGE_NAME, className)
        val sourceFile = processingEnvironment.filer.createSourceFile(qualifiedGeneratedClassName)
        val writer = JavaWriter(BufferedWriter(sourceFile.openWriter()))
        writer.indent = "    "

        writer.emitPackage(Constants.REALM_PACKAGE_NAME)
        writer.emitEmptyLine()

        val imports = ArrayList(Arrays.asList("android.util.JsonReader",
                "java.io.IOException",
                "java.util.Collections",
                "java.util.HashSet",
                "java.util.List",
                "java.util.Map",
                "java.util.HashMap",
                "java.util.Set",
                "java.util.Iterator",
                "java.util.Collection",
                "io.realm.ImportFlag",
                "io.realm.internal.ColumnInfo",
                "io.realm.internal.RealmObjectProxy",
                "io.realm.internal.RealmProxyMediator",
                "io.realm.internal.Row",
                "io.realm.internal.OsSchemaInfo",
                "io.realm.internal.OsObjectSchemaInfo",
                "org.json.JSONException",
                "org.json.JSONObject"))

        writer.emitImports(imports)
        writer.emitEmptyLine()

        writer.emitAnnotation(RealmModule::class.java)
        writer.beginType(
                qualifiedGeneratedClassName, // full qualified name of the item to generate
                "class", // the type of the item
                emptySet(), // modifiers to apply
                "RealmProxyMediator")              // class to extend
        writer.emitEmptyLine()

        emitFields(writer)
        emitGetExpectedObjectSchemaInfoMap(writer)
        emitCreateColumnInfoMethod(writer)
        emitGetSimpleClassNameMethod(writer)
        emitNewInstanceMethod(writer)
        emitGetClassModelList(writer)
        emitCopyOrUpdateMethod(writer)
        emitInsertObjectToRealmMethod(writer)
        emitInsertListToRealmMethod(writer)
        emitInsertOrUpdateObjectToRealmMethod(writer)
        emitInsertOrUpdateListToRealmMethod(writer)
        emitCreteOrUpdateUsingJsonObject(writer)
        emitCreateUsingJsonStream(writer)
        emitCreateDetachedCopyMethod(writer)
        writer.endType()
        writer.close()
    }

    @Throws(IOException::class)
    private fun emitFields(writer: JavaWriter) {
        writer.emitField("Set<Class<? extends RealmModel>>", "MODEL_CLASSES", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL))
        writer.beginInitializer(true)
        writer.emitStatement("Set<Class<? extends RealmModel>> modelClasses = new HashSet<Class<? extends RealmModel>>(%s)", qualifiedModelClasses.size)
        for (clazz in qualifiedModelClasses) {
            writer.emitStatement("modelClasses.add(%s.class)", clazz)
        }
        writer.emitStatement("MODEL_CLASSES = Collections.unmodifiableSet(modelClasses)")
        writer.endInitializer()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitGetExpectedObjectSchemaInfoMap(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "Map<Class<? extends RealmModel>, OsObjectSchemaInfo>",
                "getExpectedObjectSchemaInfoMap",
                EnumSet.of(Modifier.PUBLIC))

        writer.emitStatement(
                "Map<Class<? extends RealmModel>, OsObjectSchemaInfo> infoMap = " + "new HashMap<Class<? extends RealmModel>, OsObjectSchemaInfo>(%s)", qualifiedProxyClasses.size)
        for (i in qualifiedProxyClasses.indices) {
            writer.emitStatement("infoMap.put(%s.class, %s.getExpectedObjectSchemaInfo())",
                    qualifiedModelClasses[i], qualifiedProxyClasses[i])
        }
        writer.emitStatement("return infoMap")

        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitCreateColumnInfoMethod(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "ColumnInfo",
                "createColumnInfo",
                EnumSet.of(Modifier.PUBLIC),
                "Class<? extends RealmModel>", "clazz", // Argument type & argument name
                "OsSchemaInfo", "schemaInfo"
        )

        emitMediatorShortCircuitSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("return %s.createColumnInfo(schemaInfo)",
                        qualifiedProxyClasses[i])
            }
        }, writer)
        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitGetSimpleClassNameMethod(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "String",
                "getSimpleClassNameImpl",
                EnumSet.of(Modifier.PUBLIC),
                "Class<? extends RealmModel>", "clazz"
        )
        emitMediatorShortCircuitSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("return \"%s\"", internalClassNames[i])
            }
        }, writer)
        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitNewInstanceMethod(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "<E extends RealmModel> E",
                "newInstance",
                EnumSet.of(Modifier.PUBLIC),
                "Class<E>", "clazz",
                "Object", "baseRealm",
                "Row", "row",
                "ColumnInfo", "columnInfo",
                "boolean", "acceptDefaultValue",
                "List<String>", "excludeFields"
        )
        writer.emitStatement("final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get()")
        writer.beginControlFlow("try")
                .emitStatement("objectContext.set((BaseRealm) baseRealm, row, columnInfo, acceptDefaultValue, excludeFields)")
        emitMediatorShortCircuitSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("return clazz.cast(new %s())", qualifiedProxyClasses[i])
            }
        }, writer)
        writer.nextControlFlow("finally")
                .emitStatement("objectContext.clear()")
                .endControlFlow()
        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitGetClassModelList(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod("Set<Class<? extends RealmModel>>", "getModelClasses", EnumSet.of(Modifier.PUBLIC))
        writer.emitStatement("return MODEL_CLASSES")
        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitCopyOrUpdateMethod(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "<E extends RealmModel> E",
                "copyOrUpdate",
                EnumSet.of(Modifier.PUBLIC),
                "Realm", "realm",
                "E", "obj",
                "boolean", "update",
                "Map<RealmModel, RealmObjectProxy>", "cache",
                "Set<ImportFlag>", "flags"
        )
        writer.emitSingleLineComment("This cast is correct because obj is either")
        writer.emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
        writer.emitStatement("@SuppressWarnings(\"unchecked\") Class<E> clazz = (Class<E>) ((obj instanceof RealmObjectProxy) ? obj.getClass().getSuperclass() : obj.getClass())")
        writer.emitEmptyLine()
        emitMediatorShortCircuitSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("%1\$s columnInfo = (%1\$s) realm.getSchema().getColumnInfo(%2\$s.class)", Utils.getSimpleColumnInfoClassName(qualifiedModelClasses[i]), qualifiedModelClasses[i])
                writer.emitStatement("return clazz.cast(%s.copyOrUpdate(realm, columnInfo, (%s) obj, update, cache, flags))", qualifiedProxyClasses[i], qualifiedModelClasses[i])
            }
        }, writer, false)
        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitInsertObjectToRealmMethod(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "void",
                "insert",
                EnumSet.of(Modifier.PUBLIC),
                "Realm", "realm", "RealmModel", "object", "Map<RealmModel, Long>", "cache")
        writer.emitSingleLineComment("This cast is correct because obj is either")
        writer.emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
        writer.emitStatement("@SuppressWarnings(\"unchecked\") Class<RealmModel> clazz = (Class<RealmModel>) ((object instanceof RealmObjectProxy) ? object.getClass().getSuperclass() : object.getClass())")
        writer.emitEmptyLine()
        emitMediatorSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("%s.insert(realm, (%s) object, cache)", qualifiedProxyClasses[i], qualifiedModelClasses[i])
            }
        }, writer, false)
        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitInsertOrUpdateObjectToRealmMethod(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "void",
                "insertOrUpdate",
                EnumSet.of(Modifier.PUBLIC),
                "Realm", "realm", "RealmModel", "obj", "Map<RealmModel, Long>", "cache")
        writer.emitSingleLineComment("This cast is correct because obj is either")
        writer.emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
        writer.emitStatement("@SuppressWarnings(\"unchecked\") Class<RealmModel> clazz = (Class<RealmModel>) ((obj instanceof RealmObjectProxy) ? obj.getClass().getSuperclass() : obj.getClass())")
        writer.emitEmptyLine()
        emitMediatorSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("%s.insertOrUpdate(realm, (%s) obj, cache)", qualifiedProxyClasses[i], qualifiedModelClasses[i])
            }
        }, writer, false)
        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitInsertOrUpdateListToRealmMethod(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "void",
                "insertOrUpdate",
                EnumSet.of(Modifier.PUBLIC),
                "Realm", "realm", "Collection<? extends RealmModel>", "objects")

        writer.emitStatement("Iterator<? extends RealmModel> iterator = objects.iterator()")
        writer.emitStatement("RealmModel object = null")
        writer.emitStatement("Map<RealmModel, Long> cache = new HashMap<RealmModel, Long>(objects.size())")

        writer.beginControlFlow("if (iterator.hasNext())")
                .emitSingleLineComment(" access the first element to figure out the clazz for the routing below")
                .emitStatement("object = iterator.next()")
                .emitSingleLineComment("This cast is correct because obj is either")
                .emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
                .emitStatement("@SuppressWarnings(\"unchecked\") Class<RealmModel> clazz = (Class<RealmModel>) ((object instanceof RealmObjectProxy) ? object.getClass().getSuperclass() : object.getClass())")
                .emitEmptyLine()

        emitMediatorSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("%s.insertOrUpdate(realm, (%s) object, cache)", qualifiedProxyClasses[i], qualifiedModelClasses[i])
            }
        }, writer, false)

        writer.beginControlFlow("if (iterator.hasNext())")
        emitMediatorSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("%s.insertOrUpdate(realm, iterator, cache)", qualifiedProxyClasses[i])
            }
        }, writer, false)
        writer.endControlFlow()
        writer.endControlFlow()

        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitInsertListToRealmMethod(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "void",
                "insert",
                EnumSet.of(Modifier.PUBLIC),
                "Realm", "realm", "Collection<? extends RealmModel>", "objects")

        writer.emitStatement("Iterator<? extends RealmModel> iterator = objects.iterator()")
        writer.emitStatement("RealmModel object = null")
        writer.emitStatement("Map<RealmModel, Long> cache = new HashMap<RealmModel, Long>(objects.size())")

        writer.beginControlFlow("if (iterator.hasNext())")
                .emitSingleLineComment(" access the first element to figure out the clazz for the routing below")
                .emitStatement("object = iterator.next()")
                .emitSingleLineComment("This cast is correct because obj is either")
                .emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
                .emitStatement("@SuppressWarnings(\"unchecked\") Class<RealmModel> clazz = (Class<RealmModel>) ((object instanceof RealmObjectProxy) ? object.getClass().getSuperclass() : object.getClass())")
                .emitEmptyLine()

        emitMediatorSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("%s.insert(realm, (%s) object, cache)", qualifiedProxyClasses[i], qualifiedModelClasses[i])
            }
        }, writer, false)

        writer.beginControlFlow("if (iterator.hasNext())")
        emitMediatorSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("%s.insert(realm, iterator, cache)", qualifiedProxyClasses[i])
            }
        }, writer, false)
        writer.endControlFlow()
        writer.endControlFlow()

        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitCreteOrUpdateUsingJsonObject(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "<E extends RealmModel> E",
                "createOrUpdateUsingJsonObject",
                EnumSet.of(Modifier.PUBLIC),
                Arrays.asList("Class<E>", "clazz", "Realm", "realm", "JSONObject", "json", "boolean", "update"),
                Arrays.asList("JSONException")
        )
        emitMediatorShortCircuitSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("return clazz.cast(%s.createOrUpdateUsingJsonObject(realm, json, update))", qualifiedProxyClasses[i])
            }
        }, writer)
        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitCreateUsingJsonStream(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "<E extends RealmModel> E",
                "createUsingJsonStream",
                EnumSet.of(Modifier.PUBLIC),
                Arrays.asList("Class<E>", "clazz", "Realm", "realm", "JsonReader", "reader"),
                Arrays.asList("java.io.IOException")
        )
        emitMediatorShortCircuitSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("return clazz.cast(%s.createUsingJsonStream(realm, reader))", qualifiedProxyClasses[i])
            }
        }, writer)
        writer.endMethod()
        writer.emitEmptyLine()
    }

    @Throws(IOException::class)
    private fun emitCreateDetachedCopyMethod(writer: JavaWriter) {
        writer.emitAnnotation("Override")
        writer.beginMethod(
                "<E extends RealmModel> E",
                "createDetachedCopy",
                EnumSet.of(Modifier.PUBLIC),
                "E", "realmObject", "int", "maxDepth", "Map<RealmModel, RealmObjectProxy.CacheData<RealmModel>>", "cache"
        )
        writer.emitSingleLineComment("This cast is correct because obj is either")
        writer.emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
        writer.emitStatement("@SuppressWarnings(\"unchecked\") Class<E> clazz = (Class<E>) realmObject.getClass().getSuperclass()")
        writer.emitEmptyLine()
        emitMediatorShortCircuitSwitch(object : ProxySwitchStatement {
            @Throws(IOException::class)
            override fun emitStatement(i: Int, writer: JavaWriter) {
                writer.emitStatement("return clazz.cast(%s.createDetachedCopy((%s) realmObject, 0, maxDepth, cache))",
                        qualifiedProxyClasses[i], qualifiedModelClasses[i])
            }
        }, writer, false)
        writer.endMethod()
        writer.emitEmptyLine()
    }

    // Emits the control flow for selecting the appropriate proxy class based on the model class
    // Currently it is just if..else, which is inefficient for large amounts amounts of model classes.
    // Consider switching to HashMap or similar.
    @Throws(IOException::class)
    private fun emitMediatorSwitch(statement: ProxySwitchStatement, writer: JavaWriter, nullPointerCheck: Boolean) {
        if (nullPointerCheck) {
            writer.emitStatement("checkClass(clazz)")
            writer.emitEmptyLine()
        }
        if (qualifiedModelClasses.size == 0) {
            writer.emitStatement("throw getMissingProxyClassException(clazz)")
        } else {
            writer.beginControlFlow("if (clazz.equals(%s.class))", qualifiedModelClasses[0])
            statement.emitStatement(0, writer)
            for (i in 1 until qualifiedModelClasses.size) {
                writer.nextControlFlow("else if (clazz.equals(%s.class))", qualifiedModelClasses[i])
                statement.emitStatement(i, writer)
            }
            writer.nextControlFlow("else")
            writer.emitStatement("throw getMissingProxyClassException(clazz)")
            writer.endControlFlow()
        }
    }

    @Throws(IOException::class)
    private fun emitMediatorShortCircuitSwitch(statement: ProxySwitchStatement, writer: JavaWriter, nullPointerCheck: Boolean = true) {
        if (nullPointerCheck) {
            writer.emitStatement("checkClass(clazz)")
            writer.emitEmptyLine()
        }
        for (i in qualifiedModelClasses.indices) {
            writer.beginControlFlow("if (clazz.equals(%s.class))", qualifiedModelClasses[i])
            statement.emitStatement(i, writer)
            writer.endControlFlow()
        }
        writer.emitStatement("throw getMissingProxyClassException(clazz)")
    }


    private interface ProxySwitchStatement {
        @Throws(IOException::class)
        fun emitStatement(i: Int, writer: JavaWriter)
    }
}// Identical to the above, but eliminates the un-needed "else" clauses for, e.g., return statements
