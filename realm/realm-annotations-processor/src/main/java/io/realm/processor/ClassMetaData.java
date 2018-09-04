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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.RealmField;
import io.realm.annotations.RealmNamingPolicy;
import io.realm.annotations.Required;
import io.realm.processor.nameconverter.NameConverter;


/**
 * Utility class for holding metadata for RealmProxy classes.
 */
public class ClassMetaData {
    private static final String OPTION_IGNORE_KOTLIN_NULLABILITY = "realm.ignoreKotlinNullability";
    private static final int MAX_CLASSNAME_LENGTH = 57;

    private final TypeElement classType; // Reference to model class.
    private final String javaClassName; // Model class simple name as defined in Java.
    private final List<RealmFieldElement> fields = new ArrayList<>(); // List of all fields in the class except those @Ignored.
    private final List<RealmFieldElement> indexedFields = new ArrayList<>(); // list of all fields marked @Index.
    private final Set<Backlink> backlinks = new LinkedHashSet<>();
    private final Set<RealmFieldElement> nullableFields = new LinkedHashSet<>(); // Set of fields which can be nullable
    private final Set<RealmFieldElement> nullableValueListFields = new LinkedHashSet<>(); // Set of fields whose elements can be nullable

    private String packageName; // package name for model class.
    private boolean hasDefaultConstructor; // True if model has a public no-arg constructor.
    private VariableElement primaryKey; // Reference to field used as primary key, if any.
    private boolean containsToString;
    private boolean containsEquals;
    private boolean containsHashCode;
    private String internalClassName;

    private final List<TypeMirror> validPrimaryKeyTypes;
    private final List<TypeMirror> validListValueTypes;
    private final Types typeUtils;
    private final Elements elements;
    private NameConverter defaultFieldNameFormatter;

    private final boolean ignoreKotlinNullability;

    public ClassMetaData(ProcessingEnvironment env, TypeMirrors typeMirrors, TypeElement clazz) {
        this.classType = clazz;
        this.javaClassName = clazz.getSimpleName().toString();
        typeUtils = env.getTypeUtils();
        elements = env.getElementUtils();


        validPrimaryKeyTypes = Arrays.asList(
                typeMirrors.STRING_MIRROR,
                typeMirrors.PRIMITIVE_LONG_MIRROR,
                typeMirrors.PRIMITIVE_INT_MIRROR,
                typeMirrors.PRIMITIVE_SHORT_MIRROR,
                typeMirrors.PRIMITIVE_BYTE_MIRROR
        );

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
        );

        for (Element element : classType.getEnclosedElements()) {
            if (element instanceof ExecutableElement) {
                Name name = element.getSimpleName();
                if (name.contentEquals("toString")) {
                    this.containsToString = true;
                } else if (name.contentEquals("equals")) {
                    this.containsEquals = true;
                } else if (name.contentEquals("hashCode")) {
                    this.containsHashCode = true;
                }
            }
        }

        ignoreKotlinNullability = Boolean.valueOf(
                env.getOptions().getOrDefault(OPTION_IGNORE_KOTLIN_NULLABILITY, "false"));
    }

    @Override
    public String toString() {
        return "class " + getFullyQualifiedClassName();
    }

    public String getSimpleJavaClassName() {
        return javaClassName;
    }

    /**
     * Returns the name that Realm Core uses when saving data from this Java class.
     */
    public String getInternalClassName() {
        return internalClassName;
    }

    /**
     * Returns the internal field name that matches the one in the Java model class.
     */
    public String getInternalFieldName(String javaFieldName) {
        for (RealmFieldElement field : fields) {
            if (field.getJavaName().equals(javaFieldName)) {
                return field.getInternalFieldName();
            }
        }
        throw new IllegalArgumentException("Could not find fieldname: " + javaFieldName);
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFullyQualifiedClassName() {
        return packageName + "." + javaClassName;
    }

    public List<RealmFieldElement> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public Set<Backlink> getBacklinkFields() {
        return Collections.unmodifiableSet(backlinks);
    }

    public String getInternalGetter(String fieldName) {
        return "realmGet$" + fieldName;
    }

    public String getInternalSetter(String fieldName) {
        return "realmSet$" + fieldName;
    }

    public List<RealmFieldElement> getIndexedFields() {
        return Collections.unmodifiableList(indexedFields);
    }

    public boolean hasPrimaryKey() {
        return primaryKey != null;
    }

    public VariableElement getPrimaryKey() {
        return primaryKey;
    }

    public String getPrimaryKeyGetter() {
        return getInternalGetter(primaryKey.getSimpleName().toString());
    }

    public boolean containsToString() {
        return containsToString;
    }

    public boolean containsEquals() {
        return containsEquals;
    }

    public boolean containsHashCode() {
        return containsHashCode;
    }

    /**
     * Checks if a VariableElement is nullable.
     *
     * @return {@code true} if a VariableElement is nullable type, {@code false} otherwise.
     */
    public boolean isNullable(VariableElement variableElement) {
        return nullableFields.contains(variableElement);
    }

    /**
     * Checks if the element of {@code RealmList} designated by {@code realmListVariableElement} is nullable.
     *
     * @return {@code true} if the element is nullable type, {@code false} otherwise.
     */
    public boolean isElementNullable(VariableElement realmListVariableElement) {
        return nullableValueListFields.contains(realmListVariableElement);
    }

    /**
     * Checks if a VariableElement is indexed.
     *
     * @param variableElement the element/field
     * @return {@code true} if a VariableElement is indexed, {@code false} otherwise.
     */
    public boolean isIndexed(VariableElement variableElement) {
        return indexedFields.contains(variableElement);
    }

    /**
     * Checks if a VariableElement is a primary key.
     *
     * @param variableElement the element/field
     * @return {@code true} if a VariableElement is primary key, {@code false} otherwise.
     */
    public boolean isPrimaryKey(VariableElement variableElement) {
        return primaryKey != null && primaryKey.equals(variableElement);
    }

    /**
     * Returns {@code true} if the class is considered to be a valid RealmObject class.
     * RealmObject and Proxy classes also have the @RealmClass annotation but are not considered valid
     * RealmObject classes.
     */
    public boolean isModelClass() {
        String type = classType.toString();
        return !type.equals("io.realm.DynamicRealmObject") && !type.endsWith(".RealmObject") && !type.endsWith("RealmProxy");
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
    public VariableElement getDeclaredField(String fieldName) {
        if (fieldName == null) { return null; }
        for (VariableElement field : fields) {
            if (field.getSimpleName().toString().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Builds the meta data structures for this class. Any errors or messages will be
     * posted on the provided Messager.
     *
     * @param moduleMetaData pre-processed module meta data.
     * @return True if meta data was correctly created and processing can continue, false otherwise.
     */
    public boolean generate(ModuleMetaData moduleMetaData) {
        // Get the package of the class
        Element enclosingElement = classType.getEnclosingElement();
        if (!enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
            Utils.error("The RealmClass annotation does not support nested classes.", classType);
            return false;
        }

        // Check if the @RealmClass is considered valid with respect to the type hierarchy
        TypeElement parentElement = (TypeElement) Utils.getSuperClass(classType);
        if (!parentElement.toString().equals("java.lang.Object") && !parentElement.toString().equals("io.realm.RealmObject")) {
            Utils.error("Valid model classes must either extend RealmObject or implement RealmModel.", classType);
            return false;
        }

        PackageElement packageElement = (PackageElement) enclosingElement;
        packageName = packageElement.getQualifiedName().toString();

        // Determine naming rules for this class
        String qualifiedClassName = packageName + "." + javaClassName;
        NameConverter moduleClassNameFormatter = moduleMetaData.getClassNameFormatter(qualifiedClassName);
        defaultFieldNameFormatter = moduleMetaData.getFieldNameFormatter(qualifiedClassName);

        RealmClass realmClassAnnotation = classType.getAnnotation(RealmClass.class);
        // If name has been specifically set, it should override any module policy.
        if (!realmClassAnnotation.name().isEmpty()) {
            internalClassName = realmClassAnnotation.name();
        } else if (!realmClassAnnotation.value().isEmpty()) {
            internalClassName = realmClassAnnotation.value();
        } else {
            internalClassName = moduleClassNameFormatter.convert(javaClassName);
        }
        if (internalClassName.length() > MAX_CLASSNAME_LENGTH) {
            Utils.error(String.format(Locale.US, "Internal class name is too long. Class '%s' " +
                    "is converted to '%s', which is longer than the maximum allowed of %d characters",
                    javaClassName, internalClassName, 57));
            return false;
        }

        // If field name policy has been explicitly set, override the module field name policy
        if (realmClassAnnotation.fieldNamingPolicy() != RealmNamingPolicy.NO_POLICY) {
            defaultFieldNameFormatter = Utils.getNameFormatter(realmClassAnnotation.fieldNamingPolicy());
        }

        // Categorize and check the rest of the file
        if (!categorizeClassElements()) { return false; }
        if (!checkCollectionTypes()) { return false; }
        if (!checkReferenceTypes()) { return false; }
        if (!checkDefaultConstructor()) { return false; }
        if (!checkForFinalFields()) { return false; }
        if (!checkForVolatileFields()) { return false; }

        return true; // Meta data was successfully generated
    }

    // Iterate through all class elements and add them to the appropriate internal data structures.
    // Returns true if all elements could be categorized and false otherwise.
    private boolean categorizeClassElements() {
        for (Element element : classType.getEnclosedElements()) {
            ElementKind elementKind = element.getKind();
            switch (elementKind) {
                case CONSTRUCTOR:
                    if (Utils.isDefaultConstructor(element)) { hasDefaultConstructor = true; }
                    break;

                case FIELD:
                    if (!categorizeField(element)) { return false; }
                    break;

                default:
            }
        }

        if (fields.size() == 0) {
            Utils.error(String.format(Locale.US, "Class \"%s\" must contain at least 1 persistable field.", javaClassName));
        }

        return true;
    }

    private boolean checkCollectionTypes() {
        for (VariableElement field : fields) {
            if (Utils.isRealmList(field)) {
                if (!checkRealmListType(field)) {
                    return false;
                }
            } else if (Utils.isRealmResults(field)) {
                if (!checkRealmResultsType(field)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkRealmListType(VariableElement field) {
        // Check for missing generic (default back to Object)
        if (Utils.getGenericTypeQualifiedName(field) == null) {
            Utils.error("No generic type supplied for field", field);
            return false;
        }

        // Check that the referenced type is a concrete class and not an interface
        TypeMirror fieldType = field.asType();
        final TypeMirror elementTypeMirror = ((DeclaredType) fieldType).getTypeArguments().get(0);
        if (elementTypeMirror.getKind() == TypeKind.DECLARED /* class of interface*/) {
            TypeElement elementTypeElement = (TypeElement) ((DeclaredType) elementTypeMirror).asElement();
            if (elementTypeElement.getSuperclass().getKind() == TypeKind.NONE) {
                Utils.error(
                        "Only concrete Realm classes are allowed in RealmLists. "
                                + "Neither interfaces nor abstract classes are allowed.",
                        field);
                return false;
            }
        }

        // Check if the actual value class is acceptable
        if (!validListValueTypes.contains(elementTypeMirror) && !Utils.isRealmModel(elementTypeMirror)) {
            final StringBuilder messageBuilder = new StringBuilder(
                    "Element type of RealmList must be a class implementing 'RealmModel' or one of the ");
            final String separator = ", ";
            for (TypeMirror type : validListValueTypes) {
                messageBuilder.append('\'').append(type.toString()).append('\'').append(separator);
            }
            messageBuilder.setLength(messageBuilder.length() - separator.length());
            messageBuilder.append('.');
            Utils.error(messageBuilder.toString(), field);
            return false;
        }

        return true;
    }

    private boolean checkRealmResultsType(VariableElement field) {
        // Only classes implementing RealmModel are allowed since RealmResults field is used only for backlinks.

        // Check for missing generic (default back to Object)
        if (Utils.getGenericTypeQualifiedName(field) == null) {
            Utils.error("No generic type supplied for field", field);
            return false;
        }

        TypeMirror fieldType = field.asType();
        final TypeMirror elementTypeMirror = ((DeclaredType) fieldType).getTypeArguments().get(0);
        if (elementTypeMirror.getKind() == TypeKind.DECLARED /* class or interface*/) {
            TypeElement elementTypeElement = (TypeElement) ((DeclaredType) elementTypeMirror).asElement();
            if (elementTypeElement.getSuperclass().getKind() == TypeKind.NONE) {
                Utils.error(
                        "Only concrete Realm classes are allowed in RealmResults. "
                                + "Neither interfaces nor abstract classes are allowed.",
                        field);
                return false;
            }
        }

        // Check if the actual value class is acceptable
        if (!Utils.isRealmModel(elementTypeMirror)) {
            Utils.error("Element type of RealmResults must be a class implementing 'RealmModel'.", field);
            return false;
        }

        return true;
    }

    private boolean checkReferenceTypes() {
        for (VariableElement field : fields) {
            if (Utils.isRealmModel(field)) {
                // Check that the referenced type is a concrete class and not an interface
                TypeElement typeElement = elements.getTypeElement(field.asType().toString());
                if (typeElement.getSuperclass().getKind() == TypeKind.NONE) {
                    Utils.error(
                            "Only concrete Realm classes can be referenced from model classes. "
                                    + "Neither interfaces nor abstract classes are allowed.",
                            field);
                    return false;
                }
            }
        }

        return true;
    }

    // Report if the default constructor is missing
    private boolean checkDefaultConstructor() {
        if (!hasDefaultConstructor) {
            Utils.error(String.format(Locale.US,
                    "Class \"%s\" must declare a public constructor with no arguments if it contains custom constructors.",
                    javaClassName));
            return false;
        } else {
            return true;
        }
    }

    private boolean checkForFinalFields() {
        for (VariableElement field : fields) {
            if (!field.getModifiers().contains(Modifier.FINAL)) {
                continue;
            }
            if (Utils.isMutableRealmInteger(field)) {
                continue;
            }

            Utils.error(String.format(Locale.US, "Class \"%s\" contains illegal final field \"%s\".", javaClassName,
                    field.getSimpleName().toString()));

            return false;
        }
        return true;
    }

    private boolean checkForVolatileFields() {
        for (VariableElement field : fields) {
            if (field.getModifiers().contains(Modifier.VOLATILE)) {
                Utils.error(String.format(Locale.US,
                        "Class \"%s\" contains illegal volatile field \"%s\".",
                        javaClassName,
                        field.getSimpleName().toString()));
                return false;
            }
        }
        return true;
    }

    private boolean categorizeField(Element element) {
        VariableElement fieldRef = (VariableElement) element;

        // completely ignore any static fields
        if (fieldRef.getModifiers().contains(Modifier.STATIC)) { return true; }

        // Ignore fields marked with @Ignore or if they are transient
        if (fieldRef.getAnnotation(Ignore.class) != null || fieldRef.getModifiers().contains(Modifier.TRANSIENT)) {
            return true;
        }

        // Determine name for field
        String internalFieldName = getInternalFieldName(fieldRef, defaultFieldNameFormatter);
        RealmFieldElement field = new RealmFieldElement(fieldRef, internalFieldName);

        if (field.getAnnotation(Index.class) != null) {
            if (!categorizeIndexField(element, field)) { return false; }
        }

        // @Required annotation of RealmList field only affects its value type, not field itself.
        if (Utils.isRealmList(field)) {
            boolean hasRequiredAnnotation = hasRequiredAnnotation(field);
            final List<? extends TypeMirror> listGenericType = ((DeclaredType) field.asType()).getTypeArguments();
            boolean containsRealmModelClasses = (!listGenericType.isEmpty() && Utils.isRealmModel(listGenericType.get(0)));

            // @Required not allowed if the list contains Realm model classes
            if (hasRequiredAnnotation && containsRealmModelClasses) {
                Utils.error("@Required not allowed on RealmList's that contain other Realm model classes.");
                return false;
            }

            // @Required thus only makes sense for RealmLists with primitive types
            // We only check @Required annotation. @org.jetbrains.annotations.NotNull annotation should not affect nullability of the list values.
            if (!hasRequiredAnnotation) {
                if (!containsRealmModelClasses) {
                    nullableValueListFields.add(field);
                }
            }
        } else if (isRequiredField(field)) {
            if (!checkBasicRequiredAnnotationUsage(element, field)) {
                return false;
            }
        } else {
            // The field doesn't have the @Required and @org.jetbrains.annotations.NotNull annotation.
            // Without @Required annotation, boxed types/RealmObject/Date/String/bytes should be added to
            // nullableFields.
            // RealmList of models, RealmResults(backlinks) and primitive types are NOT nullable. @Required annotation is not supported.
            if (!Utils.isPrimitiveType(field) && !Utils.isRealmResults(field)) {
                nullableFields.add(field);
            }
        }

        if (field.getAnnotation(PrimaryKey.class) != null) {
            if (!categorizePrimaryKeyField(field)) { return false; }
        }

        // @LinkingObjects cannot be @PrimaryKey or @Index.
        if (field.getAnnotation(LinkingObjects.class) != null) {
            // Do not add backlinks to fields list.
            return categorizeBacklinkField(field);
        }

        // Similarly, a MutableRealmInteger cannot be a @PrimaryKey or @LinkingObject.
        if (Utils.isMutableRealmInteger(field)) {
            if (!categorizeMutableRealmIntegerField(field)) { return false; }
        }

        // Standard field that appears to be valid (more fine grained checks might fail later).
        fields.add(field);

        return true;
    }

    private String getInternalFieldName(VariableElement field, NameConverter defaultConverter) {
        RealmField nameAnnotation = field.getAnnotation(RealmField.class);
        if (nameAnnotation != null) {
            if (!nameAnnotation.name().isEmpty()) {
                return nameAnnotation.name();
            }
            if (!nameAnnotation.value().isEmpty()) {
                return nameAnnotation.value();
            }
            Utils.note(String.format("Empty internal name defined on @RealmField. " +
                    "Falling back to named used by Java model class: %s", field.getSimpleName()), field);
            return field.getSimpleName().toString();
        } else {
            return defaultConverter.convert(field.getSimpleName().toString());
        }
    }

    /**
     * This method only checks if the field has {@code @Required} annotation.
     * In most cases, you should use {@link #isRequiredField(VariableElement)} to take into account
     * Kotlin's annotation as well.
     *
     * @param field target field.
     * @return {@code true} if the field has {@code @Required} annotation, {@code false} otherwise.
     * @see #isRequiredField(VariableElement)
     */
    private boolean hasRequiredAnnotation(VariableElement field) {
        return field.getAnnotation(Required.class) != null;
    }

    /**
     * Checks if the field is annotated as required.
     * @param field target field.
     * @return {@code true} if the field is annotated as required, {@code false} otherwise.
     */
    private boolean isRequiredField(VariableElement field) {
        if (hasRequiredAnnotation(field)) {
            return true;
        }

        if (ignoreKotlinNullability) {
            return false;
        }

        // Kotlin uses the `org.jetbrains.annotations.NotNull` annotation to mark non-null fields.
        // In order to fully support the Kotlin type system we interpret `@NotNull` as an alias
        // for `@Required`
        for (AnnotationMirror annotation : field.getAnnotationMirrors()) {
            if (annotation.getAnnotationType().toString().equals("org.jetbrains.annotations.NotNull")) {
                return true;
            }
        }

        return false;
    }

    // The field has the @Index annotation. It's only valid for column types:
    // STRING, DATE, INTEGER, BOOLEAN, and RealmMutableInteger
    private boolean categorizeIndexField(Element element, RealmFieldElement fieldElement) {
        boolean indexable = false;

        if (Utils.isMutableRealmInteger(fieldElement)) {
            indexable = true;
        } else {
            Constants.RealmFieldType realmType = Constants.JAVA_TO_REALM_TYPES.get(fieldElement.asType().toString());
            if (realmType != null) {
                switch (realmType) {
                    case STRING:
                    case DATE:
                    case INTEGER:
                    case BOOLEAN:
                        indexable = true;
                }
            }
        }

        if (indexable) {
            indexedFields.add(fieldElement);
            return true;
        }

        Utils.error(String.format(Locale.US, "Field \"%s\" of type \"%s\" cannot be an @Index.", element, element.asType()));
        return false;
    }

    // The field has the @Required annotation
    // Returns `true` if the field could be correctly validated, `false` if an error was reported.
    private boolean checkBasicRequiredAnnotationUsage(Element element, VariableElement variableElement) {
        if (Utils.isPrimitiveType(variableElement)) {
            Utils.error(String.format(Locale.US,
                    "@Required or @NotNull annotation is unnecessary for primitive field \"%s\".", element));
            return false;
        }

        if (Utils.isRealmModel(variableElement)) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s\" with type \"%s\" cannot be @Required or @NotNull.", element, element.asType()));
            return false;
        }

        // Should never get here - user should remove @Required
        if (nullableFields.contains(variableElement)) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s\" with type \"%s\" appears to be nullable. Consider removing @Required.",
                    element,
                    element.asType()));

            return false;
        }

        return true;
    }

    // The field has the @PrimaryKey annotation. It is only valid for
    // String, short, int, long and must only be present one time
    private boolean categorizePrimaryKeyField(RealmFieldElement fieldElement) {
        if (primaryKey != null) {
            Utils.error(String.format(Locale.US,
                    "A class cannot have more than one @PrimaryKey. Both \"%s\" and \"%s\" are annotated as @PrimaryKey.",
                    primaryKey.getSimpleName().toString(),
                    fieldElement.getSimpleName().toString()));
            return false;
        }

        TypeMirror fieldType = fieldElement.asType();
        if (!isValidPrimaryKeyType(fieldType)) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s\" with type \"%s\" cannot be used as primary key. See @PrimaryKey for legal types.",
                    fieldElement.getSimpleName().toString(),
                    fieldType));
            return false;
        }

        primaryKey = fieldElement;

        // Also add as index. All types of primary key can be indexed.
        if (!indexedFields.contains(fieldElement)) {
            indexedFields.add(fieldElement);
        }

        return true;
    }

    private boolean categorizeBacklinkField(VariableElement variableElement) {
        Backlink backlink = new Backlink(this, variableElement);
        if (!backlink.validateSource()) { return false; }

        backlinks.add(backlink);

        return true;
    }

    private boolean categorizeMutableRealmIntegerField(VariableElement field) {
        if (field.getModifiers().contains(Modifier.FINAL)) {
            return true;
        }

        Utils.error(String.format(Locale.US,
                "Field \"%s\", a MutableRealmInteger, must be final.",
                field.getSimpleName().toString()));
        return false;
    }

    private boolean isValidPrimaryKeyType(TypeMirror type) {
        for (TypeMirror validType : validPrimaryKeyTypes) {
            if (typeUtils.isAssignable(type, validType)) {
                return true;
            }
        }
        return false;
    }

    public Element getClassElement() {
        return classType;
    }

}

