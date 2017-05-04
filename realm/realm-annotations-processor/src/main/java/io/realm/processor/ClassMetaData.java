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
import java.util.List;
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

import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


/**
 * Utility class for holding metadata for RealmProxy classes.
 */
public class ClassMetaData {

    private final TypeElement classType; // Reference to model class.
    private String className; // Model class simple name.
    private String packageName; // package name for model class.
    private boolean hasDefaultConstructor; // True if model has a public no-arg constructor.
    private VariableElement primaryKey; // Reference to field used as primary key, if any.
    private List<VariableElement> fields = new ArrayList<VariableElement>(); // List of all fields in the class except those @Ignored.
    private List<VariableElement> indexedFields = new ArrayList<VariableElement>(); // list of all fields marked @Index.
    private Set<Backlink> backlinks = new HashSet<Backlink>();
    private Set<VariableElement> nullableFields = new HashSet<VariableElement>(); // Set of fields which can be nullable
    private boolean containsToString;
    private boolean containsEquals;
    private boolean containsHashCode;

    private final List<TypeMirror> validPrimaryKeyTypes;
    private final Types typeUtils;
    private final Elements elements;

    public ClassMetaData(ProcessingEnvironment env, TypeElement clazz) {
        this.classType = clazz;
        this.className = clazz.getSimpleName().toString();
        typeUtils = env.getTypeUtils();
        elements = env.getElementUtils();
        TypeMirror stringType = env.getElementUtils().getTypeElement("java.lang.String").asType();
        validPrimaryKeyTypes = Arrays.asList(
                stringType,
                typeUtils.getPrimitiveType(TypeKind.SHORT),
                typeUtils.getPrimitiveType(TypeKind.INT),
                typeUtils.getPrimitiveType(TypeKind.LONG),
                typeUtils.getPrimitiveType(TypeKind.BYTE)
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
    }

    @Override
    public String toString() {
        return "class " + getFullyQualifiedClassName();
    }

    public String getSimpleClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFullyQualifiedClassName() {
        return packageName + "." + className;
    }

    public List<VariableElement> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public Set<Backlink> getBacklinkFields() {
        return backlinks;
    }

    public String getInternalGetter(String fieldName) {
        return "realmGet$" + fieldName;
    }

    public String getInternalSetter(String fieldName) {
        return "realmSet$" + fieldName;
    }

    public List<VariableElement> getIndexedFields() {
        return indexedFields;
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
        if (primaryKey == null) {
            return false;
        }
        return primaryKey.equals(variableElement);
    }

    /**
     * Returns {@code true} if the class is considered to be a valid RealmObject class.
     * RealmObject and Proxy classes also have the @RealmClass annotation but are not considered valid
     * RealmObject classes.
     */
    public boolean isModelClass() {
        String type = classType.toString();
        if (type.equals("io.realm.DynamicRealmObject")) {
            return false;
        }
        return (!type.endsWith(".RealmObject") && !type.endsWith("RealmProxy"));
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
     * @return True if meta data was correctly created and processing can continue, false otherwise.
     */
    public boolean generate() {
        // Get the package of the class
        Element enclosingElement = classType.getEnclosingElement();
        if (!enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
            Utils.error("The RealmClass annotation does not support nested classes.", classType);
            return false;
        }

        TypeElement parentElement = (TypeElement) Utils.getSuperClass(classType);
        if (!parentElement.toString().equals("java.lang.Object") && !parentElement.toString().equals("io.realm.RealmObject")) {
            Utils.error("Valid model classes must either extend RealmObject or implement RealmModel.", classType);
            return false;
        }

        PackageElement packageElement = (PackageElement) enclosingElement;
        packageName = packageElement.getQualifiedName().toString();

        if (!categorizeClassElements()) { return false; }
        if (!checkListTypes()) { return false; }
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
            Utils.error(String.format("Class \"%s\" must contain at least 1 persistable field.", className));
        }

        return true;
    }

    private boolean checkListTypes() {
        for (VariableElement field : fields) {
            if (Utils.isRealmList(field) || Utils.isRealmResults(field)) {
                // Check for missing generic (default back to Object)
                if (Utils.getGenericTypeQualifiedName(field) == null) {
                    Utils.error("No generic type supplied for field", field);
                    return false;
                }

                // Check that the referenced type is a concrete class and not an interface
                TypeMirror fieldType = field.asType();
                List<? extends TypeMirror> typeArguments = ((DeclaredType) fieldType).getTypeArguments();
                String genericCanonicalType = typeArguments.get(0).toString();
                TypeElement typeElement = elements.getTypeElement(genericCanonicalType);
                if (typeElement.getSuperclass().getKind() == TypeKind.NONE) {
                    Utils.error(
                            "Only concrete Realm classes are allowed in RealmLists. "
                                    + "Neither interfaces nor abstract classes are allowed.",
                            field);
                    return false;
                }
            }
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
            Utils.error(String.format(
                    "Class \"%s\" must declare a public constructor with no arguments if it contains custom constructors.",
                    className));
            return false;
        } else {
            return true;
        }
    }

    private boolean checkForFinalFields() {
        for (VariableElement field : fields) {
            if (field.getModifiers().contains(Modifier.FINAL)) {
                Utils.error(String.format(
                        "Class \"%s\" contains illegal final field \"%s\".", className, field.getSimpleName().toString()));
                return false;
            }
        }
        return true;
    }

    private boolean checkForVolatileFields() {
        for (VariableElement field : fields) {
            if (field.getModifiers().contains(Modifier.VOLATILE)) {
                Utils.error(String.format(
                        "Class \"%s\" contains illegal volatile field \"%s\".",
                        className,
                        field.getSimpleName().toString()));
                return false;
            }
        }
        return true;
    }

    private boolean categorizeField(Element element) {
        VariableElement field = (VariableElement) element;

        // completely ignore any static fields
        if (field.getModifiers().contains(Modifier.STATIC)) { return true; }

        // Ignore fields marked with @Ignore or if they are transient
        if (field.getAnnotation(Ignore.class) != null || field.getModifiers().contains(Modifier.TRANSIENT)) {
            return true;
        }

        if (field.getAnnotation(Index.class) != null) {
            if (!categorizeIndexField(element, field)) { return false; }
        }

        if (field.getAnnotation(Required.class) != null) {
            categorizeRequiredField(element, field);
        } else {
            // The field doesn't have the @Required annotation.
            // Without @Required annotation, boxed types/RealmObject/Date/String/bytes should be added to
            // nullableFields.
            // RealmList and Primitive types are NOT nullable always. @Required annotation is not supported.
            if (!Utils.isPrimitiveType(field) && !Utils.isRealmList(field)) {
                nullableFields.add(field);
            }
        }

        if (field.getAnnotation(PrimaryKey.class) != null) {
            if (!categorizePrimaryKeyField(field)) { return false; }
        }

        // Check @LinkingObjects last since it is not allowed to be either @Index, @Required or @PrimaryKey
        if (field.getAnnotation(LinkingObjects.class) != null) {
            return categorizeBacklinkField(field);
        }

        // Standard field that appear valid (more fine grained checks might fail later).
        fields.add(field);

        return true;
    }

    private boolean categorizeIndexField(Element element, VariableElement variableElement) {
        // The field has the @Index annotation. It's only valid for column types:
        // STRING, DATE, INTEGER, BOOLEAN
        String elementTypeCanonicalName = variableElement.asType().toString();
        String columnType = Constants.JAVA_TO_COLUMN_TYPES.get(elementTypeCanonicalName);
        if (columnType != null &&
                (columnType.equals("RealmFieldType.STRING") ||
                        columnType.equals("RealmFieldType.DATE") ||
                        columnType.equals("RealmFieldType.INTEGER") ||
                        columnType.equals("RealmFieldType.BOOLEAN"))) {
            indexedFields.add(variableElement);
        } else {
            Utils.error(String.format("Field \"%s\" of type \"%s\" cannot be an @Index.", element, element.asType()));
            return false;
        }

        return true;
    }

    // The field has the @Required annotation
    private void categorizeRequiredField(Element element, VariableElement variableElement) {
        if (Utils.isPrimitiveType(variableElement)) {
            Utils.error(String.format(
                    "@Required annotation is unnecessary for primitive field \"%s\".", element));
        } else if (Utils.isRealmList(variableElement) || Utils.isRealmModel(variableElement)) {
            Utils.error(String.format(
                    "Field \"%s\" with type \"%s\" cannot be @Required.", element, element.asType()));
        } else {
            // Should never get here - user should remove @Required
            if (nullableFields.contains(variableElement)) {
                Utils.error(String.format(
                        "Field \"%s\" with type \"%s\" appears to be nullable. Consider removing @Required.",
                        element,
                        element.asType()));
            }
        }
    }

    // The field has the @PrimaryKey annotation. It is only valid for
    // String, short, int, long and must only be present one time
    private boolean categorizePrimaryKeyField(VariableElement variableElement) {
        if (primaryKey != null) {
            Utils.error(String.format(
                    "A class cannot have more than one @PrimaryKey. Both \"%s\" and \"%s\" are annotated as @PrimaryKey.",
                    primaryKey.getSimpleName().toString(),
                    variableElement.getSimpleName().toString()));
            return false;
        }

        TypeMirror fieldType = variableElement.asType();
        if (!isValidPrimaryKeyType(fieldType)) {
            Utils.error(String.format(
                    "Field \"%s\" with type \"%s\" cannot be used as primary key. See @PrimaryKey for legal types.",
                    variableElement.getSimpleName().toString(),
                    fieldType));
            return false;
        }

        primaryKey = variableElement;

        // Also add as index. All types of primary key can be indexed.
        if (!indexedFields.contains(variableElement)) {
            indexedFields.add(variableElement);
        }

        return true;
    }

    private boolean categorizeBacklinkField(VariableElement variableElement) {
        Backlink backlink = new Backlink(this, variableElement);
        if (!backlink.validateSource()) { return false; }

        backlinks.add(backlink);

        return true;
    }

    private boolean isValidPrimaryKeyType(TypeMirror type) {
        for (TypeMirror validType : validPrimaryKeyTypes) {
            if (typeUtils.isAssignable(type, validType)) {
                return true;
            }
        }
        return false;
    }
}

