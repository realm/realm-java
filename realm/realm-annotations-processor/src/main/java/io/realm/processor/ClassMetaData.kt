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

import io.realm.annotations.*
import io.realm.processor.nameconverter.NameConverter
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * Utility class for holding metadata for RealmProxy classes.
 */
class ClassMetaData(env: ProcessingEnvironment, typeMirrors: TypeMirrors, private val classType: TypeElement /* Reference to model class. */) {

    val simpleJavaClassName = SimpleClassName(classType.simpleName) // Model class simple name as defined in Java.
    val fields = ArrayList<RealmFieldElement>() // List of all fields in the class except those @Ignored.
    private val indexedFields = ArrayList<RealmFieldElement>() // list of all fields marked @Index.
    private val _objectReferenceFields = ArrayList<RealmFieldElement>() // List of all fields that reference a Realm Object either directly or in a List
    private val basicTypeFields = ArrayList<RealmFieldElement>() // List of all fields that reference basic types, i.e. no references to other Realm Objects
    private val backlinks = LinkedHashSet<Backlink>()
    private val nullableFields = LinkedHashSet<RealmFieldElement>() // Set of fields which can be nullable
    private val nullableValueListFields = LinkedHashSet<RealmFieldElement>() // Set of fields whose elements can be nullable
    private val nullableValueMapFields = LinkedHashSet<RealmFieldElement>() // Set of fields whose elements can be nullable

    // package name for model class.
    private lateinit var packageName: String

    // True if model has a public no-arg constructor.
    private var hasDefaultConstructor: Boolean = false

    // Reference to field used as primary key
    var primaryKey: VariableElement? = null
        private set

    private var containsToString: Boolean = false
    private var containsEquals: Boolean = false
    private var containsHashCode: Boolean = false

    // Returns the name that Realm Core uses when saving data from this Java class.
    lateinit var internalClassName: String
        private set

    private val validPrimaryKeyTypes: List<TypeMirror> = listOf(
            typeMirrors.STRING_MIRROR,
            typeMirrors.PRIMITIVE_LONG_MIRROR,
            typeMirrors.PRIMITIVE_INT_MIRROR,
            typeMirrors.PRIMITIVE_SHORT_MIRROR,
            typeMirrors.PRIMITIVE_BYTE_MIRROR,
            typeMirrors.OBJECT_ID_MIRROR,
            typeMirrors.UUID_MIRROR
    )
    private val validListValueTypes: List<TypeMirror> = listOf(
            typeMirrors.STRING_MIRROR,
            typeMirrors.BINARY_MIRROR,
            typeMirrors.BOOLEAN_MIRROR,
            typeMirrors.LONG_MIRROR,
            typeMirrors.INTEGER_MIRROR,
            typeMirrors.SHORT_MIRROR,
            typeMirrors.BYTE_MIRROR,
            typeMirrors.DOUBLE_MIRROR,
            typeMirrors.FLOAT_MIRROR,
            typeMirrors.DATE_MIRROR,
            typeMirrors.DECIMAL128_MIRROR,
            typeMirrors.OBJECT_ID_MIRROR,
            typeMirrors.UUID_MIRROR,
            typeMirrors.MIXED_MIRROR
    )
    private val validDictionaryTypes: List<TypeMirror>  = listOf(
            // TODO: add more ad-hoc
            typeMirrors.MIXED_MIRROR,
            typeMirrors.BOOLEAN_MIRROR,
            typeMirrors.UUID_MIRROR
    )
    private val stringType = typeMirrors.STRING_MIRROR

    private val typeUtils: Types = env.typeUtils
    private val elements: Elements = env.elementUtils
    private lateinit var defaultFieldNameFormatter: NameConverter

    private val ignoreKotlinNullability: Boolean

    val qualifiedClassName: QualifiedClassName
        get() = QualifiedClassName("$packageName.$simpleJavaClassName")

    val backlinkFields: Set<Backlink>
        get() = backlinks.toSet()

    val primaryKeyGetter: String
        get() = getInternalGetter(primaryKey!!.simpleName.toString())

    /**
     * Returns `true if the class is considered to be a valid RealmObject class.
     * RealmObject and Proxy classes also have the @RealmClass annotation but are not considered valid
     * RealmObject classes.
     */
    val isModelClass: Boolean
        get() {
            val type = classType.toString()
            return type != "io.realm.DynamicRealmObject" && !type.endsWith(".RealmObject") && !type.endsWith("RealmProxy")
        }

    var embedded: Boolean = false
        private set

    val classElement: Element
        get() = classType

    init {
        for (element in classType.enclosedElements) {
            if (element is ExecutableElement) {
                val name = element.getSimpleName()
                when {
                    name.contentEquals("toString") -> this.containsToString = true
                    name.contentEquals("equals") -> this.containsEquals = true
                    name.contentEquals("hashCode") -> this.containsHashCode = true
                }
            }
        }

        ignoreKotlinNullability = java.lang.Boolean.valueOf(
                (env.options as MutableMap<String, String>).getOrDefault(OPTION_IGNORE_KOTLIN_NULLABILITY, "false"))
    }

    override fun toString(): String {
        return "class $qualifiedClassName"
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
    val objectReferenceFields: List<RealmFieldElement>
        get() = _objectReferenceFields.toList()

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
     * Checks if the value of a `RealmDictionary` entry designated by `realmDictionaryVariableElement` is nullable.
     *
     * @return `true` if the element is nullable type, `false` otherwise.
     */
    fun isDictionaryValueNullable(realmDictionaryVariableElement: VariableElement): Boolean {
        return nullableValueMapFields.contains(realmDictionaryVariableElement)
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
    fun generate(moduleMetaData: ModuleMetaData): Boolean {
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
        val qualifiedClassName = QualifiedClassName("$packageName.$simpleJavaClassName")
        val moduleClassNameFormatter = moduleMetaData.getClassNameFormatter(qualifiedClassName)
        defaultFieldNameFormatter = moduleMetaData.getFieldNameFormatter(qualifiedClassName)

        val realmClassAnnotation = classType.getAnnotation(RealmClass::class.java)
        // If name has been specifically set, it should override any module policy.
        internalClassName = when {
            realmClassAnnotation.name.isNotEmpty() -> realmClassAnnotation.name
            realmClassAnnotation.value.isNotEmpty() -> realmClassAnnotation.value
            else -> moduleClassNameFormatter.convert(simpleJavaClassName.toString())
        }
        if (internalClassName.length > MAX_CLASSNAME_LENGTH) {
            Utils.error(String.format(Locale.US, "Internal class name is too long. Class '%s' " + "is converted to '%s', which is longer than the maximum allowed of %d characters",
                    simpleJavaClassName, internalClassName, MAX_CLASSNAME_LENGTH))
            return false
        }

        // If field name policy has been explicitly set, override the module field name policy
        if (realmClassAnnotation.fieldNamingPolicy != RealmNamingPolicy.NO_POLICY) {
            defaultFieldNameFormatter = Utils.getNameFormatter(realmClassAnnotation.fieldNamingPolicy)
        }

        embedded = realmClassAnnotation.embedded

        // Categorize and check the rest of the file
        if (!categorizeClassElements()) {
            return false
        }
        if (!checkCollectionTypes()) {
            return false
        }
        if (!checkDictionaryTypes()) {
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
        if (!checkForVolatileFields()) {
            return false
        }

        // Meta data was successfully generated
        return true
    }

    // Iterate through all class elements and add them to the appropriate internal data structures.
    // Returns true if all elements could be categorized and false otherwise.
    private fun categorizeClassElements(): Boolean {
        for (element in classType.enclosedElements) {
            when (element.kind) {
                ElementKind.CONSTRUCTOR -> if (Utils.isDefaultConstructor(element)) {
                    hasDefaultConstructor = true
                }

                ElementKind.FIELD -> if (!categorizeField(element)) {
                    return false
                }
                else -> {
                    /* Ignore */
                }
            }
        }

        if (fields.isEmpty()) {
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

    private fun checkDictionaryTypes(): Boolean {
        for (field in fields) {
            if (Utils.isRealmDictionary(field)) {
                if (!checkDictionaryValuesType(field)) {
                    return false
                }
            }
        }

        return true
    }

    private fun checkDictionaryValuesType(field: VariableElement): Boolean {
        // Check for missing generic (default back to Object)
        if (Utils.getGenericTypeQualifiedName(field) == null) {
            Utils.error(getFieldErrorSuffix(field) + "No generic type supplied for field", field)
            return false
        }

        val elementTypeMirror = checkReferenceIsNotInterface(field) ?: return false
        return checkAcceptableClass(
                field,
                elementTypeMirror,
                validDictionaryTypes,
                "Element type of RealmDictionary must be of type 'Mixed' or any type that can be boxed inside 'Mixed': "
        )
    }

    private fun checkRealmListType(field: VariableElement): Boolean {
        // Check for missing generic (default back to Object)
        if (Utils.getGenericTypeQualifiedName(field) == null) {
            Utils.error(getFieldErrorSuffix(field) + "No generic type supplied for field", field)
            return false
        }

        val elementTypeMirror = checkReferenceIsNotInterface(field) ?: return false
        return checkAcceptableClass(
                field,
                elementTypeMirror,
                validListValueTypes,
                "Element type of RealmList must be a class implementing 'RealmModel' or one of "
        )
    }

    private fun checkAcceptableClass(
            field: VariableElement,
            elementTypeMirror: TypeMirror,
            validTypes: List<TypeMirror>,
            specificFieldTypeMessage: String
    ): Boolean {
        // Check if the actual value class is acceptable
        if (!containsType(validTypes, elementTypeMirror) && !Utils.isRealmModel(elementTypeMirror)) {
            val messageBuilder = StringBuilder(getFieldErrorSuffix(field) + "Type was '$elementTypeMirror'. $specificFieldTypeMessage")
            val separator = ", "
            for (type in validTypes) {
                messageBuilder.append('\'').append(type.toString()).append('\'').append(separator)
            }
            messageBuilder.setLength(messageBuilder.length - separator.length)
            messageBuilder.append('.')
            Utils.error(messageBuilder.toString(), field)
            return false
        }
        return true
    }

    private fun checkReferenceIsNotInterface(field: VariableElement): TypeMirror? {
        // Check that the referenced type is a concrete class and not an interface
        val fieldType = field.asType()
        val elementTypeMirror: TypeMirror = (fieldType as DeclaredType).typeArguments[0]
        if (elementTypeMirror.kind == TypeKind.DECLARED /* class of interface*/) {
            val elementTypeElement = (elementTypeMirror as DeclaredType).asElement() as TypeElement
            if (elementTypeElement.superclass.kind == TypeKind.NONE) {
                Utils.error(
                        getFieldErrorSuffix(field) + "Only concrete Realm classes are allowed in field '$field'. "
                                + "Neither interfaces nor abstract classes are allowed.",
                        field)
                return null
            }
        }
        return elementTypeMirror
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
        return "$simpleJavaClassName.${field.simpleName}: "
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
        return if (!hasDefaultConstructor) {
            Utils.error(String.format(Locale.US,
                    "Class \"%s\" must declare a public constructor with no arguments if it contains custom constructors.",
                    simpleJavaClassName))
            false
        } else {
            true
        }
    }

    private fun checkForFinalFields(): Boolean {
        for (field in fields) {
            if (!field.modifiers.contains(Modifier.FINAL)) {
                continue
            }
            if (Utils.isRealmList(field) || Utils.isMutableRealmInteger(field) ||
                    Utils.isRealmDictionary(field)) {
                continue
            }

            Utils.error(String.format(Locale.US, "Class \"%s\" contains illegal final/immutable field \"%s\".", simpleJavaClassName,
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

        if (field.getAnnotation(Index::class.java) != null) {
            if (!categorizeIndexField(element, field)) {
                return false
            }
        }

        // @Required annotation of RealmList and RealmDictionary field only affects its value type, not field itself.
        if (Utils.isRealmList(field)) {
            val hasRequiredAnnotation = hasRequiredAnnotation(field)
            val listGenericType = (field.asType() as DeclaredType).typeArguments
            val containsRealmModelClasses = (listGenericType.isNotEmpty() && Utils.isRealmModel(listGenericType[0]))

            // @Required not allowed if the list contains Realm model classes
            if (hasRequiredAnnotation && containsRealmModelClasses) {
                Utils.error("@Required not allowed on RealmLists that contain other Realm model classes.")
                return false
            }

            // @Required thus only makes sense for RealmLists with primitive types
            // We only check @Required annotation. @org.jetbrains.annotations.NotNull annotation should not affect nullability of the list values.
            if (!hasRequiredAnnotation) {
                if (!containsRealmModelClasses) {
                    nullableValueListFields.add(field)
                }
            }
        } else if (Utils.isRealmDictionary(field)) {
            // Same as RealmList
            val hasRequiredAnnotation = hasRequiredAnnotation(field)
            val listGenericType = (field.asType() as DeclaredType).typeArguments
            val containsRealmModelClasses = (listGenericType.isNotEmpty() && Utils.isRealmModel(listGenericType[0]))
            val containsMixed = (listGenericType.isNotEmpty() && Utils.isMixed(listGenericType[0]))

            // @Required not allowed if the dictionary contains Realm model classes
            if (hasRequiredAnnotation && (containsRealmModelClasses || containsMixed)) {
                Utils.error("@Required not allowed on RealmDictionaries that contain other Realm model classes and Mixed.")
                return false
            }

            // @Required thus only makes sense for RealmDictionaries with primitive types
            // We only check @Required annotation. @org.jetbrains.annotations.NotNull annotation should not affect nullability of the list values.
            if (!hasRequiredAnnotation) {
                if (!containsRealmModelClasses) {
                    nullableValueMapFields.add(field)
                }
            }
        } else if (isRequiredField(field)) {
            if (!checkBasicRequiredAnnotationUsage(field)) {
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

        if (field.getAnnotation(PrimaryKey::class.java) != null) {
            if (!categorizePrimaryKeyField(field)) {
                return false
            }
        }

        // @LinkingObjects cannot be @PrimaryKey or @Index.
        if (field.getAnnotation(LinkingObjects::class.java) != null) {
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
        if (Utils.isRealmModel(field) || Utils.isRealmModelList(field) || Utils.isMixedList(field) || Utils.isMixed(field) || Utils.isRealmModelDictionary(field)) {
            _objectReferenceFields.add(field)
        } else {
            basicTypeFields.add(field)
        }

        return true
    }

    private fun getInternalFieldName(field: VariableElement, defaultConverter: NameConverter): String {
        val nameAnnotation: RealmField? = field.getAnnotation(RealmField::class.java)
        if (nameAnnotation != null) {
            if (nameAnnotation.name.isNotEmpty()) {
                return nameAnnotation.name
            }
            if (nameAnnotation.value.isNotEmpty()) {
                return nameAnnotation.value
            }
            Utils.note(String.format(("Empty internal name defined on @RealmField. " + "Falling back to named used by Java model class: %s"), field.simpleName), field)
            return field.simpleName.toString()
        } else {
            return defaultConverter.convert(field.simpleName.toString())
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
    // STRING, DATE, INTEGER, BOOLEAN, RealmMutableInteger, OBJECT_ID, UUID and MIXED
    private fun categorizeIndexField(element: Element, fieldElement: RealmFieldElement): Boolean {
        var indexable = false

        if (Utils.isMutableRealmInteger(fieldElement) || Utils.isMixed(fieldElement)) {
            indexable = true
        } else {
            when (Constants.JAVA_TO_REALM_TYPES[fieldElement.asType().toString()]) {
                Constants.RealmFieldType.STRING,
                Constants.RealmFieldType.DATE,
                Constants.RealmFieldType.INTEGER,
                Constants.RealmFieldType.BOOLEAN,
                Constants.RealmFieldType.OBJECT_ID,
                Constants.RealmFieldType.UUID -> { indexable = true }
                else -> { /* Ignore */ }
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
    private fun checkBasicRequiredAnnotationUsage(field: VariableElement): Boolean {
        if (Utils.isPrimitiveType(field)) {
            Utils.error(String.format(Locale.US,
                    "@Required or @NotNull annotation is unnecessary for primitive field \"%s\".", field))
            return false
        }

        if (Utils.isRealmModel(field)) {
            /**
             * Defer checking if @Required usage is valid when checking backlinks. See [categorizeBacklinkField]
             */
            if (!embedded || field.getAnnotation(LinkingObjects::class.java) == null) {
                Utils.error(String.format(Locale.US,
                        "Field \"%s\" with type \"%s\" cannot be @Required or @NotNull.", field, field.asType()))
                return false
            }
        }

        // Should never get here - user should remove @Required
        if (nullableFields.contains(field)) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s\" with type \"%s\" appears to be nullable. Consider removing @Required.",
                    field,
                    field.asType()))

            return false
        }

        return true
    }

    // The field has the @PrimaryKey annotation. It is only valid for
    // String, short, int and long and must only be present one time.
    // From Core 6 String primary keys no longer needs to be indexed, and from Core 10
    // none of the primary key types do.
    private fun categorizePrimaryKeyField(fieldElement: RealmFieldElement): Boolean {
        // Embedded Objects do not support primary keys at all
        if (embedded) {
            Utils.error(String.format(Locale.US,
                    "A model class marked as embedded cannot contain a @PrimaryKey. One was defined for: %s",
                    fieldElement.simpleName.toString()))
            return false
        }

        // Only one primary key pr. class is allowed
        if (primaryKey != null) {
            Utils.error(String.format(Locale.US,
                    "A class cannot have more than one @PrimaryKey. Both \"%s\" and \"%s\" are annotated as @PrimaryKey.",
                    primaryKey!!.simpleName.toString(),
                    fieldElement.simpleName.toString()))
            return false
        }

        // Check that the primary key is defined on a supported field
        val fieldType = fieldElement.asType()
        if (!isValidPrimaryKeyType(fieldType)) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s\" with type \"%s\" cannot be used as primary key. See @PrimaryKey for legal types.",
                    fieldElement.simpleName.toString(),
                    fieldType))
            return false
        }

        primaryKey = fieldElement

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

    private fun isStringPrimaryKeyType(type: TypeMirror): Boolean = typeUtils.isAssignable(type, stringType)

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

