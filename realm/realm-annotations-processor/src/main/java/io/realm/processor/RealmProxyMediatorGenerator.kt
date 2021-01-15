/*
 * Copyright 2019 Realm Inc.
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
import io.realm.annotations.RealmModule
import java.io.BufferedWriter
import java.io.IOException
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.tools.JavaFileObject

class RealmProxyMediatorGenerator(private val processingEnvironment: ProcessingEnvironment,
                                  private val className: SimpleClassName,
                                  classesToValidate: Set<ClassMetaData>) {

    private val qualifiedModelClasses = ArrayList<QualifiedClassName>()
    private val qualifiedProxyClasses = ArrayList<QualifiedClassName>()
    private val simpleModelClassNames = ArrayList<SimpleClassName>()
    private val internalClassNames = ArrayList<String>()
    private val embeddedClass = ArrayList<Boolean>()
    private val primaryKeyClasses = mutableListOf<QualifiedClassName>()

    init {
        for (metadata in classesToValidate) {
            qualifiedModelClasses.add(metadata.qualifiedClassName)
            val qualifiedProxyClassName = QualifiedClassName("${Constants.REALM_PACKAGE_NAME}.${Utils.getProxyClassName(metadata.qualifiedClassName)}")
            qualifiedProxyClasses.add(qualifiedProxyClassName)
            simpleModelClassNames.add(metadata.simpleJavaClassName)
            internalClassNames.add(metadata.internalClassName)
            embeddedClass.add(metadata.embedded)
            metadata.primaryKey?.let {
                primaryKeyClasses.add(metadata.qualifiedClassName)
            }
        }
    }

    @Throws(IOException::class)
    fun generate() {
        val qualifiedGeneratedClassName: String = String.format(Locale.US, "%s.%sMediator", Constants.REALM_PACKAGE_NAME, className)
        val sourceFile: JavaFileObject = processingEnvironment.filer.createSourceFile(qualifiedGeneratedClassName)
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

        val writer = JavaWriter(BufferedWriter(sourceFile.openWriter()))
        writer.apply {
            indent = "    "
            emitPackage(Constants.REALM_PACKAGE_NAME)
            emitEmptyLine()
            emitImports(imports)
            emitEmptyLine()
            emitAnnotation(RealmModule::class.java)
            beginType(qualifiedGeneratedClassName,      // full qualified name of the item to generate
                    "class",                      // the type of the item
                    emptySet(),                        // modifiers to apply
                    "RealmProxyMediator")  // class to extend
            emitEmptyLine()
            emitFields(this)
            emitGetExpectedObjectSchemaInfoMap(this)
            emitCreateColumnInfoMethod(this)
            emitGetSimpleClassNameMethod(this)
            emitHasPrimaryKeyMethod(this)
            emitNewInstanceMethod(this)
            emitGetClassModelList(this)
            emitCopyOrUpdateMethod(this)
            emitInsertObjectToRealmMethod(this)
            emitInsertListToRealmMethod(this)
            emitInsertOrUpdateObjectToRealmMethod(this)
            emitInsertOrUpdateListToRealmMethod(this)
            emitCreteOrUpdateUsingJsonObject(this)
            emitCreateUsingJsonStream(this)
            emitCreateDetachedCopyMethod(this)
            emitIsEmbeddedMethod(this)
            emitUpdateEmbeddedObjectMethod(this)
            endType()
            close()
        }
    }

    @Throws(IOException::class)
    private fun emitFields(writer: JavaWriter) {
        writer.apply {
            emitField("Set<Class<? extends RealmModel>>", "MODEL_CLASSES", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL))
            beginInitializer(true)
            emitStatement("Set<Class<? extends RealmModel>> modelClasses = new HashSet<Class<? extends RealmModel>>(%s)", qualifiedModelClasses.size)
            for (clazz in qualifiedModelClasses) {
                emitStatement("modelClasses.add(%s.class)", clazz)
            }
            emitStatement("MODEL_CLASSES = Collections.unmodifiableSet(modelClasses)")
            endInitializer()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitGetExpectedObjectSchemaInfoMap(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod("Map<Class<? extends RealmModel>, OsObjectSchemaInfo>","getExpectedObjectSchemaInfoMap", EnumSet.of(Modifier.PUBLIC))
                emitStatement("Map<Class<? extends RealmModel>, OsObjectSchemaInfo> infoMap = new HashMap<Class<? extends RealmModel>, OsObjectSchemaInfo>(%s)", qualifiedProxyClasses.size)
                for (i in qualifiedProxyClasses.indices) {
                    emitStatement("infoMap.put(%s.class, %s.getExpectedObjectSchemaInfo())", qualifiedModelClasses[i], qualifiedProxyClasses[i])
                }
                emitStatement("return infoMap")
            endMethod()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitCreateColumnInfoMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "ColumnInfo",
                    "createColumnInfo",
                    EnumSet.of(Modifier.PUBLIC),
                    "Class<? extends RealmModel>", "clazz", // Argument type & argument name
                    "OsSchemaInfo", "schemaInfo"
            )
            emitMediatorShortCircuitSwitch(writer, emitStatement = { i: Int ->
                emitStatement("return %s.createColumnInfo(schemaInfo)", qualifiedProxyClasses[i])
            })
            endMethod()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitGetSimpleClassNameMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "String",
                    "getSimpleClassNameImpl",
                    EnumSet.of(Modifier.PUBLIC),
                    "Class<? extends RealmModel>", "clazz"
            )
                emitMediatorShortCircuitSwitch(writer, emitStatement = { i: Int ->
                    emitStatement("return \"%s\"", internalClassNames[i])
                })
            endMethod()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitHasPrimaryKeyMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "boolean",
                    "hasPrimaryKeyImpl",
                    EnumSet.of(Modifier.PUBLIC),
                    "Class<? extends RealmModel>", "clazz"
            )

            if (primaryKeyClasses.isEmpty()) {
                emitStatement("return false")
            } else {
                val primaryKeyCondition = primaryKeyClasses.joinToString(".class.isAssignableFrom(clazz)\n|| ", "", ".class.isAssignableFrom(clazz)")
                emitStatement("return %s", primaryKeyCondition)
            }

            endMethod()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitNewInstanceMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
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
                emitStatement("final BaseRealm.RealmObjectContext objectContext = BaseRealm.objectContext.get()")
                beginControlFlow("try")
                    emitStatement("objectContext.set((BaseRealm) baseRealm, row, columnInfo, acceptDefaultValue, excludeFields)")
                    emitMediatorShortCircuitSwitch(writer, emitStatement = { i: Int ->
                        emitStatement("return clazz.cast(new %s())", qualifiedProxyClasses[i])
                    })
                nextControlFlow("finally")
                    emitStatement("objectContext.clear()")
                endControlFlow()
            endMethod()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitGetClassModelList(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod("Set<Class<? extends RealmModel>>", "getModelClasses", EnumSet.of(Modifier.PUBLIC))
                emitStatement("return MODEL_CLASSES")
            endMethod()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitCopyOrUpdateMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "<E extends RealmModel> E",
                    "copyOrUpdate",
                    EnumSet.of(Modifier.PUBLIC),
                    "Realm", "realm",
                    "E", "obj",
                    "boolean", "update",
                    "Map<RealmModel, RealmObjectProxy>", "cache",
                    "Set<ImportFlag>", "flags"
            )
                emitSingleLineComment("This cast is correct because obj is either")
                emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
                emitStatement("@SuppressWarnings(\"unchecked\") Class<E> clazz = (Class<E>) ((obj instanceof RealmObjectProxy) ? obj.getClass().getSuperclass() : obj.getClass())")
                emitEmptyLine()
                emitMediatorShortCircuitSwitch(writer, false) { i: Int ->
                    emitStatement("%1\$s columnInfo = (%1\$s) realm.getSchema().getColumnInfo(%2\$s.class)", Utils.getSimpleColumnInfoClassName(qualifiedModelClasses[i]), qualifiedModelClasses[i])
                    emitStatement("return clazz.cast(%s.copyOrUpdate(realm, columnInfo, (%s) obj, update, cache, flags))", qualifiedProxyClasses[i], qualifiedModelClasses[i])
                }
            endMethod()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitInsertObjectToRealmMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "void",
                    "insert",
                    EnumSet.of(Modifier.PUBLIC),
                    "Realm", "realm", "RealmModel", "object", "Map<RealmModel, Long>", "cache")

                if (embeddedClass.contains(false)) {
                    emitSingleLineComment("This cast is correct because obj is either")
                    emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
                    emitStatement("@SuppressWarnings(\"unchecked\") Class<RealmModel> clazz = (Class<RealmModel>) ((object instanceof RealmObjectProxy) ? object.getClass().getSuperclass() : object.getClass())")
                    emitEmptyLine()
                    emitMediatorSwitch(writer, false, { i: Int ->
                        if (embeddedClass[i]) {
                            emitEmbeddedObjectsCannotBeCopiedException(writer)
                        } else {
                            emitStatement("%s.insert(realm, (%s) object, cache)", qualifiedProxyClasses[i], qualifiedModelClasses[i])
                        }
                    })
                } else {
                    emitEmbeddedObjectsCannotBeCopiedException(writer)
                }

            endMethod()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitInsertOrUpdateObjectToRealmMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "void",
                    "insertOrUpdate",
                    EnumSet.of(Modifier.PUBLIC),
                    "Realm", "realm", "RealmModel", "obj", "Map<RealmModel, Long>", "cache")

            if (embeddedClass.contains(false)) {
                emitSingleLineComment("This cast is correct because obj is either")
                emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
                emitStatement("@SuppressWarnings(\"unchecked\") Class<RealmModel> clazz = (Class<RealmModel>) ((obj instanceof RealmObjectProxy) ? obj.getClass().getSuperclass() : obj.getClass())")
                emitEmptyLine()
                emitMediatorSwitch(writer, false, { i: Int ->
                    if (embeddedClass[i]) {
                        emitEmbeddedObjectsCannotBeCopiedException(writer)
                    } else {
                        emitStatement("%s.insertOrUpdate(realm, (%s) obj, cache)", qualifiedProxyClasses[i], qualifiedModelClasses[i])
                    }
                })
            } else {
                emitEmbeddedObjectsCannotBeCopiedException(writer)
            }
            endMethod()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitInsertOrUpdateListToRealmMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "void",
                    "insertOrUpdate",
                    EnumSet.of(Modifier.PUBLIC),
                    "Realm", "realm", "Collection<? extends RealmModel>", "objects")

            if (embeddedClass.contains(false)) {
                emitStatement("Iterator<? extends RealmModel> iterator = objects.iterator()")
                emitStatement("RealmModel object = null")
                emitStatement("Map<RealmModel, Long> cache = new HashMap<RealmModel, Long>(objects.size())")

                beginControlFlow("if (iterator.hasNext())")
                emitSingleLineComment(" access the first element to figure out the clazz for the routing below")
                emitStatement("object = iterator.next()")
                emitSingleLineComment("This cast is correct because obj is either")
                emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
                emitStatement("@SuppressWarnings(\"unchecked\") Class<RealmModel> clazz = (Class<RealmModel>) ((object instanceof RealmObjectProxy) ? object.getClass().getSuperclass() : object.getClass())")
                emitEmptyLine()

                emitMediatorSwitch(writer, false) { i: Int ->
                    if (embeddedClass[i]) {
                        emitEmbeddedObjectsCannotBeCopiedException(writer)
                    } else {
                        emitStatement("%s.insertOrUpdate(realm, (%s) object, cache)", qualifiedProxyClasses[i], qualifiedModelClasses[i])
                    }
                }

                beginControlFlow("if (iterator.hasNext())")
                emitMediatorSwitch(writer, false) { i: Int ->
                    if (embeddedClass[i]) {
                        emitEmbeddedObjectsCannotBeCopiedException(writer)
                    } else {
                        emitStatement("%s.insertOrUpdate(realm, iterator, cache)", qualifiedProxyClasses[i])
                    }
                }
                endControlFlow()
                endControlFlow()
            } else {
                emitEmbeddedObjectsCannotBeCopiedException(writer)
            }

            endMethod()
            emitEmptyLine()
        }
    }

    private fun emitEmbeddedObjectsCannotBeCopiedException(writer: JavaWriter) {
        writer.apply {
            emitStatement("throw new IllegalArgumentException(\"Embedded objects cannot be copied into Realm by themselves. They need to be attached to a parent object\")")
        }
    }

    @Throws(IOException::class)
    private fun emitInsertListToRealmMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "void",
                    "insert",
                    EnumSet.of(Modifier.PUBLIC),
                    "Realm", "realm", "Collection<? extends RealmModel>", "objects")

                if (embeddedClass.contains(false)) {
                    emitStatement("Iterator<? extends RealmModel> iterator = objects.iterator()")
                    emitStatement("RealmModel object = null")
                    emitStatement("Map<RealmModel, Long> cache = new HashMap<RealmModel, Long>(objects.size())")

                    beginControlFlow("if (iterator.hasNext())")
                            .emitSingleLineComment(" access the first element to figure out the clazz for the routing below")
                            .emitStatement("object = iterator.next()")
                            .emitSingleLineComment("This cast is correct because obj is either")
                            .emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
                            .emitStatement("@SuppressWarnings(\"unchecked\") Class<RealmModel> clazz = (Class<RealmModel>) ((object instanceof RealmObjectProxy) ? object.getClass().getSuperclass() : object.getClass())")
                            .emitEmptyLine()

                    emitMediatorSwitch(writer, false, { i: Int ->
                        if (embeddedClass[i]) {
                            emitEmbeddedObjectsCannotBeCopiedException(writer)
                        } else {
                            emitStatement("%s.insert(realm, (%s) object, cache)", qualifiedProxyClasses[i], qualifiedModelClasses[i])
                        }
                    })

                    beginControlFlow("if (iterator.hasNext())")
                    emitMediatorSwitch(writer, false, { i: Int ->
                        if (embeddedClass[i]) {
                            emitEmbeddedObjectsCannotBeCopiedException(writer)
                        } else {
                            emitStatement("%s.insert(realm, iterator, cache)", qualifiedProxyClasses[i])
                        }
                    })
                    endControlFlow()
                    endControlFlow()

                } else {
                    emitEmbeddedObjectsCannotBeCopiedException(writer)
                }
            endMethod()
            emitEmptyLine()    
        }
    }

    @Throws(IOException::class)
    private fun emitCreteOrUpdateUsingJsonObject(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "<E extends RealmModel> E",
                    "createOrUpdateUsingJsonObject",
                    EnumSet.of(Modifier.PUBLIC),
                    Arrays.asList("Class<E>", "clazz", "Realm", "realm", "JSONObject", "json", "boolean", "update"),
                    Arrays.asList("JSONException")
            )
                emitMediatorShortCircuitSwitch(writer, emitStatement = { i: Int ->
                    if (!embeddedClass[i]) {
                        emitStatement("return clazz.cast(%s.createOrUpdateUsingJsonObject(realm, json, update))", qualifiedProxyClasses[i])
                    } else {
                        emitStatement("throw new IllegalArgumentException(\"Importing embedded classes from JSON without a parent is not allowed\")")
                    }
                })
            endMethod()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitCreateUsingJsonStream(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "<E extends RealmModel> E",
                    "createUsingJsonStream",
                    EnumSet.of(Modifier.PUBLIC),
                    Arrays.asList("Class<E>", "clazz", "Realm", "realm", "JsonReader", "reader"),
                    Arrays.asList("java.io.IOException")
            )
                emitMediatorShortCircuitSwitch(writer, emitStatement = { i: Int ->
                    if (!embeddedClass[i]) {
                        emitStatement("return clazz.cast(%s.createUsingJsonStream(realm, reader))", qualifiedProxyClasses[i])
                    } else {
                        emitStatement("throw new IllegalArgumentException(\"Importing embedded classes from JSON without a parent is not allowed\")")
                    }
                })
            endMethod()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitCreateDetachedCopyMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "<E extends RealmModel> E",
                    "createDetachedCopy",
                    EnumSet.of(Modifier.PUBLIC),
                    "E", "realmObject", "int", "maxDepth", "Map<RealmModel, RealmObjectProxy.CacheData<RealmModel>>", "cache"
            )
                emitSingleLineComment("This cast is correct because obj is either")
                emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
                emitStatement("@SuppressWarnings(\"unchecked\") Class<E> clazz = (Class<E>) realmObject.getClass().getSuperclass()")
                emitEmptyLine()
                emitMediatorShortCircuitSwitch(writer, false, { i: Int ->
                    emitStatement("return clazz.cast(%s.createDetachedCopy((%s) realmObject, 0, maxDepth, cache))",
                            qualifiedProxyClasses[i], qualifiedModelClasses[i])
                })
            endMethod()
            emitEmptyLine()   
        }
    }

    @Throws(IOException::class)
    private fun emitIsEmbeddedMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "<E extends RealmModel> boolean",
                    "isEmbedded",
                    EnumSet.of(Modifier.PUBLIC),
                    "Class<E>", "clazz"
            )
            emitMediatorShortCircuitSwitch(writer, false, { i: Int ->
                emitStatement("return %s", if (embeddedClass[i]) "true" else "false")
            })
            endMethod()
            emitEmptyLine()
        }
    }

    @Throws(IOException::class)
    private fun emitUpdateEmbeddedObjectMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod(
                    "<E extends RealmModel> void",
                    "updateEmbeddedObject",
                    EnumSet.of(Modifier.PUBLIC),
                    "Realm", "realm",
                    "E", "unmanagedObject",
                    "E", "managedObject",
                    "Map<RealmModel, RealmObjectProxy>", "cache",
                    "Set<ImportFlag>", "flags"
            )

            emitSingleLineComment("This cast is correct because obj is either")
            emitSingleLineComment("generated by RealmProxy or the original type extending directly from RealmObject")
            emitStatement("@SuppressWarnings(\"unchecked\") Class<E> clazz = (Class<E>) managedObject.getClass().getSuperclass()")
            emitEmptyLine()
            emitMediatorSwitch(writer, false) { i: Int ->
                if (embeddedClass[i]) {
                    emitStatement("%1\$s.updateEmbeddedObject(realm, (%2\$s) unmanagedObject, (%2\$s) managedObject, cache, flags)", qualifiedProxyClasses[i], qualifiedModelClasses[i])
                } else {
                    emitStatement("throw getNotEmbeddedClassException(\"%s\")", qualifiedModelClasses[i])
                }
            }
            endMethod()
            emitEmptyLine()
        }
    }


    // Emits the control flow for selecting the appropriate proxy class based on the model class
    // Currently it is just if..else, which is inefficient for large amounts amounts of model classes.
    // Consider switching to HashMap or similar.
    @Throws(IOException::class)
    private fun emitMediatorSwitch(writer: JavaWriter, nullPointerCheck: Boolean, emitStatement: (index: Int) -> Unit) {
        writer.apply {
            if (nullPointerCheck) {
                emitStatement("checkClass(clazz)")
                emitEmptyLine()
            }
            if (qualifiedModelClasses.isEmpty()) {
                emitStatement("throw getMissingProxyClassException(clazz)")
            } else {
                beginControlFlow("if (clazz.equals(%s.class))", qualifiedModelClasses[0])
                emitStatement(0)
                for (i in 1 until qualifiedModelClasses.size) {
                    nextControlFlow("else if (clazz.equals(%s.class))", qualifiedModelClasses[i])
                    emitStatement(i)
                }
                nextControlFlow("else")
                emitStatement("throw getMissingProxyClassException(clazz)")
                endControlFlow()
            }
        }
    }

    @Throws(IOException::class)
    private fun emitMediatorShortCircuitSwitch(writer: JavaWriter, nullPointerCheck: Boolean = true, emitStatement: (index: Int) -> Unit) {
        writer.apply {
            if (nullPointerCheck) {
                emitStatement("checkClass(clazz)")
                emitEmptyLine()
            }
            for (i in qualifiedModelClasses.indices) {
                beginControlFlow("if (clazz.equals(%s.class))", qualifiedModelClasses[i])
                emitStatement(i)
                endControlFlow()
            }
            emitStatement("throw getMissingProxyClassException(clazz)")
        }
    }
}
