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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
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

import io.realm.annotations.RealmClass;
import io.realm.annotations.RealmField;
import io.realm.annotations.RealmNamingPolicy;
import io.realm.processor.nameconverter.NameConverter;


/**
 * Utility class for holding metadata for RealmProxy classes.
 */
public class ClassMetaData {
    private static final String OPTION_IGNORE_KOTLIN_NULLABILITY = "realm.ignoreKotlinNullability";
    private static final int MAX_CLASSNAME_LENGTH = 57;

    private final TypeElement classType; // Reference to model class.
    private final String javaClassName; // Model class simple name as defined in Java.
    private final Map<String, RealmFieldElement> fields = new LinkedHashMap<>(); // Map of all <fieldName, fieldData> in the class except those @Ignored.
    private final List<RealmFieldElement> indexedFields = new ArrayList<>(); // list of all fields marked @Index.
    private final Set<Backlink> backlinks = new HashSet<>();
    private final Set<RealmFieldElement> nullableFields = new HashSet<>(); // Set of fields which can be nullable
    private final Set<RealmFieldElement> nullableValueListFields = new HashSet<>(); // Set of fields whose elements can be nullable

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
    private final Elements elementUtils;
    private NameConverter defaultFieldNameFormatter;
    private final boolean ignoreKotlinNullability;

    public ClassMetaData(ProcessingEnvironment env, TypeMirrors typeMirrors, TypeElement clazz) {
        this.classType = clazz;
        this.javaClassName = clazz.getSimpleName().toString();
        typeUtils = env.getTypeUtils();
        elementUtils = env.getElementUtils();

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
        RealmFieldElement field = fields.get(javaFieldName);
        if (field == null) {
            throw new IllegalArgumentException("Could not find fieldname: " + javaFieldName);
        } else {
            return field.getInternalFieldName();
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFullyQualifiedClassName() {
        return packageName + "." + javaClassName;
    }

    public RealmFieldElement getFieldByName(String name) {
        for (int i = 0; i < fields.size(); i++) {
            RealmFieldElement field = fields.get(i);
            if (field.getJavaName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public List<RealmFieldElement> getFields() {
        ArrayList<RealmFieldElement> fieldList = new ArrayList<>();
        fieldList.addAll(fields.values());
        return Collections.unmodifiableList(fieldList);
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

        // The `DynamicRealmObject` itself also extends `RealmObject`, but it still isn't a
        // real model class.
        if (type.equals("io.realm.DynamicRealmObject")) {
            return false;
        }

        // The `RealmObject` class itself isn't a model class.
        if (type.endsWith(".RealmObject")) {
            return false;
        }

        // All proxies should not be considered model classes, they are only created from one.
        if (type.endsWith("RealmProxy")) {
            return false;
        }

        return true;
    }

    /**
     * Returns {@code true} if this class is a super model class, i.e. one that hold properties that
     * can be shared, but cannot be instantiated by itself (yet).
     */
    public boolean isSuperClass() {
        // Abstract classes are not considered real Model classes, they are only used
        // as a mean to share fields between between different other model classes
        // i.e. non-polymorphic inheritance.
        return classType.getModifiers().contains(Modifier.ABSTRACT);
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
        return fields.get(fieldName);
    }

    /**
     * Builds the meta data structures for this class. Any errors or messages will be
     * posted on the provided Messager.
     *
     * @param moduleMetaData pre-processed module meta data.
     * @param superClassData set of all known super classes. Precondition: Should already contain all
     *                       super class info related to the class being processed.
     * @return True if meta data was correctly created and processing can continue, false otherwise.
     */
    public boolean generate(ModuleMetaData moduleMetaData, ClassCollection superClassData ) {
        // Get the package of the class
        Element enclosingElement = classType.getEnclosingElement();
        if (!enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
            Utils.error("The RealmClass annotation does not support nested classes.", classType);
            return false;
        }

        // Check if the @RealmClass is considered valid with respect to the type hierarchy
        if (!Utils.isImplementingMarkerInterface(classType)) {
            Utils.error("Valid model classes must either extend RealmObject, implement RealmModel or extend an abstract classes that do.", classType);
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
        if (!realmClassAnnotation.name().equals("")) {
            internalClassName = realmClassAnnotation.name();
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

        // Combine with super class and validate that no illegal combinations exists
        TypeElement superClass = Utils.getSuperClass(classType);
        if (superClass != null
                && !superClass.asType().toString().equals("java.lang.Object")
                && !superClass.asType().toString().equals("io.realm.RealmObject")) {
            String qualifiedSuperClassName = superClass.getQualifiedName().toString();
            if (!superClassData.containsQualifiedClass(qualifiedSuperClassName)) {
                throw new IllegalStateException(qualifiedSuperClassName + " has not been processed yet.");
            }
            if (!combineWithSuperClassData(superClassData.getClassFromQualifiedName(qualifiedSuperClassName))) {
                return false;
            }
        }

        return true; // Meta data was successfully generated
    }

    /*
     * Add all super class data to this class metadata. Returns {@code true} if succesful,
     * {@code false} if something went wrong
     */
    private boolean combineWithSuperClassData(ClassMetaData superClass) {

        // Normally in Java, a subclass with a field that has the same name as a field in the super
        // class is just hiding the super class field. We adopt the same semantics, so it is
        // allowed for a subclass to redefine a named field type without an error being thrown.
        // With one exception: If a primary key is defined in both the super class and sub class
        // they have to match with regard to type and name, otherwise an error is thrown.
        VariableElement superClassPrimaryKey = superClass.getPrimaryKey();
        if (superClassPrimaryKey != null) {
            if (primaryKey == null) {
                primaryKey = superClassPrimaryKey;
            } else {
                if (!primaryKey.getSimpleName().equals(superClassPrimaryKey.getSimpleName())) {
                    Utils.error(String.format("The classes %s and %s have both defined a primary key with " +
                                    "different names: %s vs %s", getFullyQualifiedClassName(), superClass.getFullyQualifiedClassName(),
                            primaryKey.getSimpleName().toString(), superClassPrimaryKey.getSimpleName().toString()), primaryKey);
                    return false;
                }

                if (!primaryKey.asType().equals(superClassPrimaryKey.asType())) {
                    Utils.error(String.format("The classes %s and %s have both defined a primary key, but with" +
                                    "different types: %s vs %s", getFullyQualifiedClassName(), superClass.getFullyQualifiedClassName(),
                            primaryKey.getSimpleName(), superClassPrimaryKey.getSimpleName().toString()), primaryKey);
                    return false;
                }
            }
        }

        // If equals()/hashCode()/toString() are set in the superclass, do not auto generate
        // them in the base class. This is following normal inheritance rules in the Java language.
        containsEquals = containsEquals || superClass.containsEquals();
        containsHashCode = containsHashCode || superClass.containsHashCode();
        containsToString = containsToString || superClass.containsToString();

        // Combine internal data structures that track the various fields.
        // Ignore any fields in super classes that are defined in the subclass, even if they have
        // different types or annotations (except @PrimaryKey).
        for (RealmFieldElement field : superClass.getFields()) {
            if (!fields.containsKey(field.getJavaName())) {
                if (field.isNullableValueList()) {
                    nullableValueListFields.add(field);
                } else if (field.isNullable()) {
                    nullableFields.add(field);
                }

                if (field.isIndexed()) {
                    indexedFields.add(field);
                }

                if (field.isBacklink()) {
                    backlinks.add(field.getBacklinkInfo());
                } else {
                    fields.put(field.getJavaName(), field);
                }
            }
        }

        return true;
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

        if (fields.size() == 0 && !isSuperClass()) {
            Utils.error(String.format(Locale.US, "Class \"%s\" must contain at least 1 persistable field.", javaClassName));
        }

        return true;
    }

    private boolean checkCollectionTypes() {
        for (VariableElement field : fields.values()) {
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

        // Check that abstract base classes are not used
        TypeElement classType = (TypeElement) typeUtils.asElement(elementTypeMirror);
        if (classType != null && Utils.isRealmModelSuperClass(classType)) {
            Utils.error("Only concrete Realm model classes can be used as a generic argument", field);
            return false;
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
        for (VariableElement field : fields.values()) {
            if (Utils.isRealmModel(field)) {
                // Check that the referenced type is a concrete class and not an interface
                TypeElement typeElement = elementUtils.getTypeElement(field.asType().toString());
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
        for (VariableElement field : fields.values()) {
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
        for (VariableElement field : fields.values()) {
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

        // Determine name for field
        String internalFieldName = getInternalFieldName(fieldRef, defaultFieldNameFormatter);
        RealmFieldElement field = new RealmFieldElement(fieldRef, internalFieldName, typeUtils, elementUtils, validPrimaryKeyTypes);

        if (!field.generateFieldMetaData(
                this,
                primaryKey,
                ignoreKotlinNullability,
                validPrimaryKeyTypes,
                typeUtils
        )) {
            return false;
        }

        if (field.isIgnored()) {
            return true;
        }

        if (field.isPrimaryKey()) {
            primaryKey = field;
        }

        if (field.isIndexed()) {
            indexedFields.add(field);
        }

        if (field.isNullable()) {
            nullableFields.add(field);
        } else if (field.isNullableValueList()) {
            nullableValueListFields.add(field);
        }

        if (field.isBacklink()) {
            backlinks.add(field.getBacklinkInfo());
        } else {
            fields.put(field.getJavaName(), field);
        }

        return true;
    }

    private String getInternalFieldName(VariableElement field, NameConverter defaultConverter) {
        RealmField nameAnnotation = field.getAnnotation(RealmField.class);
        if (nameAnnotation != null) {
            String declaredName = nameAnnotation.name();
            if (!declaredName.equals("")) {
                return declaredName;
            } else {
                Utils.note(String.format("Empty internal name defined on @RealmField. " +
                        "Falling back to named used by Java model class: %s", field.getSimpleName()), field);
                return field.getSimpleName().toString();
            }
        } else {
            return defaultConverter.convert(field.getSimpleName().toString());
        }
    }

    public Element getClassElement() {
        return classType;
    }

}

