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

import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.LinkedHashSet
import java.util.Locale

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

import io.realm.annotations.Ignore
import io.realm.annotations.Index
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.RealmField
import io.realm.annotations.RealmNamingPolicy
import io.realm.annotations.Required
import io.realm.processor.nameconverter.NameConverter


/**
 * Utility class for holding metadata for RealmProxy classes.
 */
class ClassMetaData(env: ProcessingEnvironment, typeMirrors: TypeMirrors, private val classType: TypeElement // Reference to model class.
) {
    val simpleJavaClassName: String // Model class simple name as defined in Java.
    val fields = ArrayList<RealmFieldElement>() // List of all fields in the class except those @Ignored.
    private val indexedFields = ArrayList<RealmFieldElement>() // list of all fields marked @Index.
    private val objectReferenceFields = ArrayList<RealmFieldElement>() // List of all fields that reference a Realm Object either directly or in a List
    private val basicTypeFields = ArrayList<RealmFieldElement>() // List of all fields that reference basic types, i.e. no references to other Realm Objects
    private val backlinks = LinkedHashSet<Backlink>()
    private val nullableFields = LinkedHashSet<RealmFieldElement>() // Set of fields which can be nullable
    private val nullableValueListFields = LinkedHashSet<RealmFieldElement>() // Set of fields whose elements can be nullable

    var packageName: String? = null
        private set // package name for model class.
    private var hasDefaultConstructor: Boolean = false // True if model has a public no-arg constructor.
    var primaryKey: VariableElement? = null
        private set // Reference to field used as primary key, if any.
    private var containsToString: Boolean = false
    private var containsEquals: Boolean = false
    private var containsHashCode: Boolean = false
    /**
     * Returns the name that Realm Core uses when saving data from this Java class.
     */
    var internalClassName: String? = null
        private set

    private val validPrimaryKeyTypes: List<TypeMirror>
    private val validListValueTypes: List<TypeMirror>
    private val typeUtils: Types
    private val elements: Elements
    private var defaultFieldNameFormatter: NameConverter? = null

    private val ignoreKotlinNullability: Boolean

    val fullyQualifiedClassName: String
        get() = "$packageName.$simpleJavaClassName"

    val backlinkFields: Set<Backlink>
        get() = Collections.unmodifiableSet(backlinks)

    val primaryKeyGetter: String
        get() = getInternalGetter(primaryKey!!.simpleName.toString())

    /**
     * Returns `true` if the class is considered to be a valid RealmObject class.
     * RealmObject and Proxy classes also have the @RealmClass annotation but are not considered valid
     * RealmObject classes.
     */
    val isModelClass: Boolean
        get() {
            val type = classType.toString()
            return type != "io.realm.DynamicRealmObject" && !type.endsWith(".RealmObject") && !type.endsWith("RealmProxy")
        }

    val classElement: Element
        get() = classType

    init {
        this.simpleJavaClassName = classType.simpleName.toString()
        typeUtils = env.typeUtils
        elements = env.elementUtils


        validPrimaryKeyTypes = Arrays.asList(
                typeMirrors.STRING_MIRROR,
                typeMirrors.PRIMITIVE_LONG_MIRROR,
                typeMirrors.PRIMITIVE_INT_MIRROR,
                typeMirrors.PRIMITIVE_SHORT_MIRROR,
                typeMirrors.PRIMITIVE_BYTE_MIRROR
        )

        validListValueTypes = Arrays.asList(
                typeMirrors.STRING_MIRROR,
                typeMirrors.BINARY_MIRROR,
                typeMirrors.BOOLEAN_MIRROR,
                typeMirrors.LONG_MIRROR,
                typeMirrors.INTEGER_MIRROR,
                typeMirrors.SHORT_MIRROR,
                typeMirrors.BYTE_MIRROR,
                typeMirrors.DOUBLE_MIRROR,
                typeMirrors.FLOAT_MIRROR,
                typeMirrors.DATE_MIRROR
        )

        for (element in classType.enclosedElements) {
            if (element is ExecutableElement) {
                val name = element.getSimpleName()
                if (name.contentEquals("toString")) {
                    this.containsToString = true
                } else if (name.contentEquals("equals")) {
                    this.containsEquals = true
                } else if (name.contentEquals("hashCode")) {
                    this.containsHashCode = true
                }
            }
        }

        ignoreKotlinNullability = java.lang.Boolean.valueOf(
                (env.options as java.util.Map<String, String>).getOrDefault(OPTION_IGNORE_KOTLIN_NULLABILITY, "false"))
    }

    override fun toString(): String {
        return "class $fullyQualifiedClassName"
    }

    /**
     * Returns the internal field name that matches the one in the Java model class.
     */
    fun getInternalFieldName(javaFieldName: String): String {
        for (field in fields) {
            if (field.javaName == javaFieldName) {
                return field.internalFieldName
            }
        }
        throw IllegalArgumentException("Could not find fieldname: $javaFieldName")
    }

    /**
     * Returns all persistable fields that reference other Realm objects.
     */
    fun getObjectReferenceFields(): List<RealmFieldElement> {
        return Collections.unmodifiableList(objectReferenceFields)
    }

    /**
     * Returns all persistable fields that contain a basic type, this include lists of primitives.
     */
    fun getBasicTypeFields(): List<RealmFieldElement> {
        return Collections.unmodifiableList(basicTypeFields)
    }

    fun getInternalGetter(fieldName: String): String {
        return "realmGet$$fieldName"
    }

    fun getInternalSetter(fieldName: String): String {
        return "realmSet$$fieldName"
    }

    fun getIndexedFields(): List<RealmFieldElement> {
        return Collections.unmodifiableList(indexedFields)
    }

    fun hasPrimaryKey(): Boolean {
        return primaryKey != null
    }

    fun containsToString(): Boolean {
        return containsToString
    }

    fun containsEquals(): Boolean {
        return containsEquals
    }

    fun containsHashCode(): Boolean {
        return containsHashCode
    }

    /**
     * Checks if a VariableElement is nullable.
     *
     * @return `true` if a VariableElement is nullable type, `false` otherwise.
     */
    fun isNullable(variableElement: VariableElement): Boolean {
        return nullableFields.contains(variableElement)
    }

    /**
     * Checks if the element of `RealmList` designated by `realmListVariableElement` is nullable.
     *
     * @return `true` if the element is nullable type, `false` otherwise.
     */
    fun isElementNullable(realmListVariableElement: VariableElement): Boolean {
        return nullableValueListFields.contains(realmListVariableElement)
    }

    /**
     * Checks if a VariableElement is indexed.
     *
     * @param variableElement the element/field
     * @return `true` if a VariableElement is indexed, `false` otherwise.
     */
    fun isIndexed(variableElement: VariableElement): Boolean {
        return indexedFields.contains(variableElement)
    }

    /**
     * Checks if a VariableElement is a primary key.
     *
     * @param variableElement the element/field
     * @return `true` if a VariableElement is primary key, `false` otherwise.
     */
    fun isPrimaryKey(variableElement: VariableElement): Boolean {
        return primaryKey != null && primaryKey == variableElement
    }

    /**
     * Find the named field in this classes list of fields.
     * This method is called only during backlink checking,
     * so creating a map, even lazily, doesn't seem like a worthwhile optimization.
     * If it gets used more widely, that decision should be revisited.
     *
     * @param fieldName The name of the sought field
     * @return the named field's VariableElement, or null if not found
     */
    fun getDeclaredField(fieldName: String?): VariableElement? {
        if (fieldName == null) {
            return null
        }
        for (field in fields) {
            if (field.simpleName.toString() == fieldName) {
                return field
            }
        }
        return null
    }

    /**
     * Builds the meta data structures for this class. Any errors or messages will be
     * posted on the provided Messager.
     *
     * @param moduleMetaData pre-processed module meta data.
     * @return True if meta data was correctly created and processing can continue, false otherwise.
     */
    fun generate(moduleMetaData: ModuleMetaData?): Boolean {
        // Get the package of the class
        val enclosingElement = classType.enclosingElement
        if (enclosingElement.kind != ElementKind.PACKAGE) {
            Utils.error("The RealmClass annotation does not support nested classes.", classType)
            return false
        }

        // Check if the @RealmClass is considered valid with respect to the type hierarchy
        val parentElement = Utils.getSuperClass(classType) as TypeElement
        if (parentElement.toString() != "java.lang.Object" && parentElement.toString() != "io.realm.RealmObject") {
            Utils.error("Valid model classes must either extend RealmObject or implement RealmModel.", classType)
            return false
        }

        val packageElement = enclosingElement as PackageElement
        packageName = packageElement.qualifiedName.toString()

        // Determine naming rules for this class
        val qualifiedClassName = "$packageName.$simpleJavaClassName"
        val moduleClassNameFormatter = moduleMetaData!!.getClassNameFormatter(qualifiedClassName)
        defaultFieldNameFormatter = moduleMetaData!!.getFieldNameFormatter(qualifiedClassName)

        val realmClassAnnotation = classType.getAnnotation(RealmClass::class.java)
        // If name has been specifically set, it should override any module policy.
        if (!realmClassAnnotation.name.isEmpty()) {
            internalClassName = realmClassAnnotation.name
        } else if (!realmClassAnnotation.value.isEmpty()) {
            internalClassName = realmClassAnnotation.value
        } else {
            internalClassName = moduleClassNameFormatter.convert(simpleJavaClassName)
        }
        if (internalClassName!!.length > MAX_CLASSNAME_LENGTH) {
            Utils.error(String.format(Locale.US, "Internal class name is too long. Class '%s' " + "is converted to '%s', which is longer than the maximum allowed of %d characters",
                    simpleJavaClassName, internalClassName, 57))
            return false
        }

        // If field name policy has been explicitly set, override the module field name policy
        if (realmClassAnnotation.fieldNamingPolicy != RealmNamingPolicy.NO_POLICY) {
            defaultFieldNameFormatter = Utils.getNameFormatter(realmClassAnnotation.fieldNamingPolicy)
        }

        // Categorize and check the rest of the file
        if (!categorizeClassElements()) {
            return false
        }
        if (!checkCollectionTypes()) {
            return false
        }
        if (!checkReferenceTypes()) {
            return false
        }
        if (!checkDefaultConstructor()) {
            return false
        }
        if (!checkForFinalFields()) {
            return false
        }
        return if (!checkForVolatileFields()) {
            false
        } else true

// Meta data was successfully generated
    }

    // Iterate through all class elements and add them to the appropriate internal data structures.
    // Returns true if all elements could be categorized and false otherwise.
    private fun categorizeClassElements(): Boolean {
        for (element in classType.enclosedElements) {
            val elementKind = element.kind
            when (elementKind) {
                ElementKind.CONSTRUCTOR -> if (Utils.isDefaultConstructor(element)) {
                    hasDefaultConstructor = true
                }

                ElementKind.FIELD -> if (!categorizeField(element)) {
                    return false
                }
            }
        }

        if (fields.size == 0) {
            Utils.error(String.format(Locale.US, "Class \"%s\" must contain at least 1 persistable field.", simpleJavaClassName))
        }

        return true
    }

    private fun checkCollectionTypes(): Boolean {
        for (field in fields) {
            if (Utils.isRealmList(field)) {
                if (!checkRealmListType(field)) {
                    return false
                }
            } else if (Utils.isRealmResults(field)) {
                if (!checkRealmResultsType(field)) {
                    return false
                }
            }
        }

        return true
    }

    private fun checkRealmListType(field: VariableElement): Boolean {
        // Check for missing generic (default back to Object)
        if (Utils.getGenericTypeQualifiedName(field) == null) {
            Utils.error(getFieldErrorSuffix(field) + "No generic type supplied for field", field)
            return false
        }

        // Check that the referenced type is a concrete class and not an interface
        val fieldType = field.asType()
        val elementTypeMirror = (fieldType as DeclaredType).typeArguments[0]
        if (elementTypeMirror.kind == TypeKind.DECLARED /* class of interface*/) {
            val elementTypeElement = (elementTypeMirror as DeclaredType).asElement() as TypeElement
            if (elementTypeElement.superclass.kind == TypeKind.NONE) {
                Utils.error(
                        getFieldErrorSuffix(field) + "Only concrete Realm classes are allowed in RealmLists. "
                                + "Neither interfaces nor abstract classes are allowed.",
                        field)
                return false
            }
        }

        // Check if the actual value class is acceptable
        if (!containsType(validListValueTypes, elementTypeMirror) && !Utils.isRealmModel(elementTypeMirror)) {
            val messageBuilder = StringBuilder(
                    getFieldErrorSuffix(field) + "Element type of RealmList must be a class implementing 'RealmModel' or one of ")
            val separator = ", "
            for (type in validListValueTypes) {
                messageBuilder.append('\'').append(type.toString()).append('\'').append(separator)
            }
            messageBuilder.setLength(messageBuilder.length - separator.length)
            messageBuilder.append('.')
            Utils.error(messageBuilder.toString(), field)
            return false
        }

        return true
    }

    private fun checkRealmResultsType(field: VariableElement): Boolean {
        // Only classes implementing RealmModel are allowed since RealmResults field is used only for backlinks.

        // Check for missing generic (default back to Object)
        if (Utils.getGenericTypeQualifiedName(field) == null) {
            Utils.error(getFieldErrorSuffix(field) + "No generic type supplied for field", field)
            return false
        }

        val fieldType = field.asType()
        val elementTypeMirror = (fieldType as DeclaredType).typeArguments[0]
        if (elementTypeMirror.kind == TypeKind.DECLARED /* class or interface*/) {
            val elementTypeElement = (elementTypeMirror as DeclaredType).asElement() as TypeElement
            if (elementTypeElement.superclass.kind == TypeKind.NONE) {
                Utils.error(
                        ("Only concrete Realm classes are allowed in RealmResults. " + "Neither interfaces nor abstract classes are allowed."),
                        field)
                return false
            }
        }

        // Check if the actual value class is acceptable
        if (!Utils.isRealmModel(elementTypeMirror)) {
            Utils.error(getFieldErrorSuffix(field) + "Element type of RealmResults must be a class implementing 'RealmModel'.", field)
            return false
        }

        return true
    }

    private fun getFieldErrorSuffix(field: VariableElement): String {
        return simpleJavaClassName + "." + field.simpleName + ": "
    }

    private fun checkReferenceTypes(): Boolean {
        for (field in fields) {
            if (Utils.isRealmModel(field)) {
                // Check that the referenced type is a concrete class and not an interface
                val typeElement = elements.getTypeElement(field.asType().toString())
                if (typeElement.superclass.kind == TypeKind.NONE) {
                    Utils.error(
                            ("Only concrete Realm classes can be referenced from model classes. " + "Neither interfaces nor abstract classes are allowed."),
                            field)
                    return false
                }
            }
        }

        return true
    }

    // Report if the default constructor is missing
    private fun checkDefaultConstructor(): Boolean {
        if (!hasDefaultConstructor) {
            Utils.error(String.format(Locale.US,
                    "Class \"%s\" must declare a public constructor with no arguments if it contains custom constructors.",
                    simpleJavaClassName))
            return false
        } else {
            return true
        }
    }

    private fun checkForFinalFields(): Boolean {
        for (field in fields) {
            if (!field.modifiers.contains(Modifier.FINAL)) {
                continue
            }
            if (Utils.isMutableRealmInteger(field)) {
                continue
            }

            Utils.error(String.format(Locale.US, "Class \"%s\" contains illegal final field \"%s\".", simpleJavaClassName,
                    field.simpleName.toString()))

            return false
        }
        return true
    }

    private fun checkForVolatileFields(): Boolean {
        for (field in fields) {
            if (field.modifiers.contains(Modifier.VOLATILE)) {
                Utils.error(String.format(Locale.US,
                        "Class \"%s\" contains illegal volatile field \"%s\".",
                        simpleJavaClassName,
                        field.simpleName.toString()))
                return false
            }
        }
        return true
    }

    private fun categorizeField(element: Element): Boolean {
        val fieldRef = element as VariableElement

        // completely ignore any static fields
        if (fieldRef.modifiers.contains(Modifier.STATIC)) {
            return true
        }

        // Ignore fields marked with @Ignore or if they are transient
        if (fieldRef.getAnnotation(Ignore::class.java) != null || fieldRef.modifiers.contains(Modifier.TRANSIENT)) {
            return true
        }

        // Determine name for field
        val internalFieldName = getInternalFieldName(fieldRef, defaultFieldNameFormatter)
        val field = RealmFieldElement(fieldRef, internalFieldName)

        if (field.getAnnotation(Index::class.java!!) != null) {
            if (!categorizeIndexField(element, field)) {
                return false
            }
        }

        // @Required annotation of RealmList field only affects its value type, not field itself.
        if (Utils.isRealmList(field)) {
            val hasRequiredAnnotation = hasRequiredAnnotation(field)
            val listGenericType = (field.asType() as DeclaredType).typeArguments
            val containsRealmModelClasses = (!listGenericType.isEmpty() && Utils.isRealmModel(listGenericType[0]))

            // @Required not allowed if the list contains Realm model classes
            if (hasRequiredAnnotation && containsRealmModelClasses) {
                Utils.error("@Required not allowed on RealmList's that contain other Realm model classes.")
                return false
            }

            // @Required thus only makes sense for RealmLists with primitive types
            // We only check @Required annotation. @org.jetbrains.annotations.NotNull annotation should not affect nullability of the list values.
            if (!hasRequiredAnnotation) {
                if (!containsRealmModelClasses) {
                    nullableValueListFields.add(field)
                }
            }
        } else if (isRequiredField(field)) {
            if (!checkBasicRequiredAnnotationUsage(element, field)) {
                return false
            }
        } else {
            // The field doesn't have the @Required and @org.jetbrains.annotations.NotNull annotation.
            // Without @Required annotation, boxed types/RealmObject/Date/String/bytes should be added to
            // nullableFields.
            // RealmList of models, RealmResults(backlinks) and primitive types are NOT nullable. @Required annotation is not supported.
            if (!Utils.isPrimitiveType(field) && !Utils.isRealmResults(field)) {
                nullableFields.add(field)
            }
        }

        if (field.getAnnotation(PrimaryKey::class.java!!) != null) {
            if (!categorizePrimaryKeyField(field)) {
                return false
            }
        }

        // @LinkingObjects cannot be @PrimaryKey or @Index.
        if (field.getAnnotation(LinkingObjects::class.java!!) != null) {
            // Do not add backlinks to fields list.
            return categorizeBacklinkField(field)
        }

        // Similarly, a MutableRealmInteger cannot be a @PrimaryKey or @LinkingObject.
        if (Utils.isMutableRealmInteger(field)) {
            if (!categorizeMutableRealmIntegerField(field)) {
                return false
            }
        }

        // Standard field that appears to be valid (more fine grained checks might fail later).
        fields.add(field)
        if (Utils.isRealmModel(field) || Utils.isRealmModelList(field)) {
            objectReferenceFields.add(field)
        } else {
            basicTypeFields.add(field)
        }

        return true
    }

    private fun getInternalFieldName(field: VariableElement, defaultConverter: NameConverter?): String {
        val nameAnnotation = field.getAnnotation(RealmField::class.java)
        if (nameAnnotation != null) {
            if (!nameAnnotation!!.name.isEmpty()) {
                return nameAnnotation!!.name
            }
            if (!nameAnnotation!!.value.isEmpty()) {
                return nameAnnotation!!.value
            }
            Utils.note(String.format(("Empty internal name defined on @RealmField. " + "Falling back to named used by Java model class: %s"), field.simpleName), field)
            return field.simpleName.toString()
        } else {
            return defaultConverter!!.convert(field.simpleName.toString())
        }
    }

    /**
     * This method only checks if the field has `@Required` annotation.
     * In most cases, you should use [.isRequiredField] to take into account
     * Kotlin's annotation as well.
     *
     * @param field target field.
     * @return `true` if the field has `@Required` annotation, `false` otherwise.
     * @see .isRequiredField
     */
    private fun hasRequiredAnnotation(field: VariableElement): Boolean {
        return field.getAnnotation(Required::class.java) != null
    }

    /**
     * Checks if the field is annotated as required.
     * @param field target field.
     * @return `true` if the field is annotated as required, `false` otherwise.
     */
    private fun isRequiredField(field: VariableElement): Boolean {
        if (hasRequiredAnnotation(field)) {
            return true
        }

        if (ignoreKotlinNullability) {
            return false
        }

        // Kotlin uses the `org.jetbrains.annotations.NotNull` annotation to mark non-null fields.
        // In order to fully support the Kotlin type system we interpret `@NotNull` as an alias
        // for `@Required`
        for (annotation in field.annotationMirrors) {
            if (annotation.annotationType.toString() == "org.jetbrains.annotations.NotNull") {
                return true
            }
        }

        return false
    }

    // The field has the @Index annotation. It's only valid for column types:
    // STRING, DATE, INTEGER, BOOLEAN, and RealmMutableInteger
    private fun categorizeIndexField(element: Element, fieldElement: RealmFieldElement): Boolean {
        var indexable = false

        if (Utils.isMutableRealmInteger(fieldElement)) {
            indexable = true
        } else {
            val realmType = Constants.JAVA_TO_REALM_TYPES[fieldElement.asType().toString()]
            if (realmType != null) {
                when (realmType) {
                    Constants.RealmFieldType.STRING, Constants.RealmFieldType.DATE, Constants.RealmFieldType.INTEGER, Constants.RealmFieldType.BOOLEAN -> indexable = true
                }
            }
        }

        if (indexable) {
            indexedFields.add(fieldElement)
            return true
        }

        Utils.error(String.format(Locale.US, "Field \"%s\" of type \"%s\" cannot be an @Index.", element, element.asType()))
        return false
    }

    // The field has the @Required annotation
    // Returns `true` if the field could be correctly validated, `false` if an error was reported.
    private fun checkBasicRequiredAnnotationUsage(element: Element, variableElement: VariableElement): Boolean {
        if (Utils.isPrimitiveType(variableElement)) {
            Utils.error(String.format(Locale.US,
                    "@Required or @NotNull annotation is unnecessary for primitive field \"%s\".", element))
            return false
        }

        if (Utils.isRealmModel(variableElement)) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s\" with type \"%s\" cannot be @Required or @NotNull.", element, element.asType()))
            return false
        }

        // Should never get here - user should remove @Required
        if (nullableFields.contains(variableElement)) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s\" with type \"%s\" appears to be nullable. Consider removing @Required.",
                    element,
                    element.asType()))

            return false
        }

        return true
    }

    // The field has the @PrimaryKey annotation. It is only valid for
    // String, short, int, long and must only be present one time
    private fun categorizePrimaryKeyField(fieldElement: RealmFieldElement): Boolean {
        if (primaryKey != null) {
            Utils.error(String.format(Locale.US,
                    "A class cannot have more than one @PrimaryKey. Both \"%s\" and \"%s\" are annotated as @PrimaryKey.",
                    primaryKey!!.simpleName.toString(),
                    fieldElement.simpleName.toString()))
            return false
        }

        val fieldType = fieldElement.asType()
        if (!isValidPrimaryKeyType(fieldType)) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s\" with type \"%s\" cannot be used as primary key. See @PrimaryKey for legal types.",
                    fieldElement.simpleName.toString(),
                    fieldType))
            return false
        }

        primaryKey = fieldElement

        // Also add as index. All types of primary key can be indexed.
        if (!indexedFields.contains(fieldElement)) {
            indexedFields.add(fieldElement)
        }

        return true
    }

    private fun categorizeBacklinkField(variableElement: VariableElement): Boolean {
        val backlink = Backlink(this, variableElement)
        if (!backlink.validateSource()) {
            return false
        }

        backlinks.add(backlink)

        return true
    }

    private fun categorizeMutableRealmIntegerField(field: VariableElement): Boolean {
        if (field.modifiers.contains(Modifier.FINAL)) {
            return true
        }

        Utils.error(String.format(Locale.US,
                "Field \"%s\", a MutableRealmInteger, must be final.",
                field.simpleName.toString()))
        return false
    }

    private fun isValidPrimaryKeyType(type: TypeMirror): Boolean {
        for (validType in validPrimaryKeyTypes) {
            if (typeUtils.isAssignable(type, validType)) {
                return true
            }
        }
        return false
    }

    private fun containsType(listOfTypes: List<TypeMirror>, type: TypeMirror): Boolean {
        for (i in listOfTypes.indices) {
            // Comparing TypeMirror's using `equals()` breaks when using incremental annotation processing.
            if (typeUtils.isSameType(listOfTypes[i], type)) {
                return true
            }
        }
        return false
    }

    companion object {
        private val OPTION_IGNORE_KOTLIN_NULLABILITY = "realm.ignoreKotlinNullability"
        private val MAX_CLASSNAME_LENGTH = 57
    }

}
