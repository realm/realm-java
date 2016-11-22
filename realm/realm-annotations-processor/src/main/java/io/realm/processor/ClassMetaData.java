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
            Utils.error("The RealmClass annotation does not support nested classes", classType);
            return false;
        }

        TypeElement parentElement = (TypeElement) Utils.getSuperClass(classType);
        if (!parentElement.toString().equals("java.lang.Object") && !parentElement.toString().equals("io.realm.RealmObject")) {
                Utils.error("Realm model classes must either extend RealmObject or implement RealmModel to be considered a valid model class", classType);
                return false;
        }

        PackageElement packageElement = (PackageElement) enclosingElement;
        packageName = packageElement.getQualifiedName().toString();

        if (!categorizeClassElements()) return false;
        if (!checkListTypes()) return  false;
        if (!checkReferenceTypes()) return  false;
        if (!checkDefaultConstructor()) return false;
        if (!checkForFinalFields()) return false;
        if (!checkForTransientFields()) return false;
        if (!checkForVolatileFields()) return false;

        return true; // Meta data was successfully generated
    }

    private boolean checkForTransientFields() {
        for (VariableElement field : fields) {
            if (field.getModifiers().contains(Modifier.TRANSIENT)) {
                Utils.error("Transient fields are not allowed. Class: " + className + ", Field: " +
                        field.getSimpleName().toString());
                return false;
            }
        }
        return true;
    }

    private boolean checkForVolatileFields() {
        for (VariableElement field : fields) {
            if (field.getModifiers().contains(Modifier.VOLATILE)) {
                Utils.error("Volatile fields are not allowed. Class: " + className + ", Field: " +
                        field.getSimpleName().toString());
                return false;
            }
        }
        return true;
    }

    private boolean checkForFinalFields() {
        for (VariableElement field : fields) {
            if (field.getModifiers().contains(Modifier.FINAL)) {
                Utils.error("Final fields are not allowed. Class: " + className + ", Field: " +
                        field.getSimpleName().toString());
                return false;
            }
        }
        return true;
    }

    private boolean checkListTypes() {
        for (VariableElement field : fields) {
            if (Utils.isRealmList(field)) {
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
                    Utils.error("Only concrete Realm classes are allowed in RealmLists. Neither " +
                            "interfaces nor abstract classes can be used.", field);
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
                    Utils.error("Only concrete Realm classes can be referenced in model classes. " +
                            "Neither interfaces nor abstract classes can be used.", field);
                    return false;
                }
            }
        }

        return true;
    }



    // Report if the default constructor is missing
    private boolean checkDefaultConstructor() {
        if (!hasDefaultConstructor) {
            Utils.error("A default public constructor with no argument must be declared if a custom constructor is declared.");
            return false;
        } else {
            return true;
        }
    }

    // Iterate through all class elements and add them to the appropriate internal data structures.
    // Returns true if all elements could be false if elements could not be categorized,
    private boolean categorizeClassElements() {
        for (Element element : classType.getEnclosedElements()) {
            ElementKind elementKind = element.getKind();

            if (elementKind.equals(ElementKind.FIELD)) {
                VariableElement variableElement = (VariableElement) element;

                Set<Modifier> modifiers = variableElement.getModifiers();
                if (modifiers.contains(Modifier.STATIC)) {
                    continue; // completely ignore any static fields
                }

                if (variableElement.getAnnotation(Ignore.class) != null) {
                    continue;
                }

                if (variableElement.getAnnotation(Index.class) != null) {
                    // The field has the @Index annotation. It's only valid for column types:
                    // STRING, DATE, INTEGER, BOOLEAN
                    String elementTypeCanonicalName = variableElement.asType().toString();
                    String columnType = Constants.JAVA_TO_COLUMN_TYPES.get(elementTypeCanonicalName);
                    if (columnType != null && (columnType.equals("RealmFieldType.STRING") ||
                            columnType.equals("RealmFieldType.DATE") ||
                            columnType.equals("RealmFieldType.INTEGER") ||
                            columnType.equals("RealmFieldType.BOOLEAN"))) {
                        indexedFields.add(variableElement);
                    } else {
                        Utils.error("@Index is not applicable to this field " + element + ".");
                        return false;
                    }
                }

                if (variableElement.getAnnotation(Required.class) == null) {
                    // The field doesn't have the @Required annotation.
                    // Without @Required annotation, boxed types/RealmObject/Date/String/bytes should be added to
                    // nullableFields.
                    // RealmList and Primitive types are NOT nullable always. @Required annotation is not supported.
                    if (!Utils.isPrimitiveType(variableElement) && !Utils.isRealmList(variableElement)) {
                        nullableFields.add(variableElement);
                    }
                } else {
                    // The field has the @Required annotation
                    if (Utils.isPrimitiveType(variableElement)) {
                        Utils.error("@Required is not needed for field " + element +
                                " with the type " + element.asType());
                    } else if (Utils.isRealmList(variableElement)) {
                        Utils.error("@Required is invalid for field " + element +
                                " with the type " + element.asType());
                    } else if (Utils.isRealmModel(variableElement)) {
                        Utils.error("@Required is invalid for field " + element +
                                " with the type " + element.asType());
                    } else {
                        // Should never get here - user should remove @Required
                        if (nullableFields.contains(variableElement)) {
                            Utils.error("Annotated field " + element + " with type " + element.asType() +
                                    " has been added to the nullableFields before. Consider to remove @Required.");
                        }
                    }
                }

                if (variableElement.getAnnotation(PrimaryKey.class) != null) {
                    // The field has the @PrimaryKey annotation. It is only valid for
                    // String, short, int, long and must only be present one time
                    if (primaryKey != null) {
                        Utils.error(String.format("@PrimaryKey cannot be defined more than once. It was found here \"%s\" and here \"%s\"",
                                primaryKey.getSimpleName().toString(),
                                variableElement.getSimpleName().toString()));
                        return false;
                    }

                    TypeMirror fieldType = variableElement.asType();
                    if (!isValidPrimaryKeyType(fieldType)) {
                        Utils.error("\"" + variableElement.getSimpleName().toString() + "\" is not allowed as primary key. See @PrimaryKey for allowed types.");
                        return false;
                    }

                    primaryKey = variableElement;

                    // Also add as index. All types of primary key can be indexed.
                    if (!indexedFields.contains(variableElement)) {
                        indexedFields.add(variableElement);
                    }
                }

                fields.add(variableElement);
            } else if (elementKind.equals(ElementKind.CONSTRUCTOR)) {
                hasDefaultConstructor =  hasDefaultConstructor || Utils.isDefaultConstructor(element);

            }
        }

        if (fields.size() == 0) {
            Utils.error(className + " must contain at least 1 persistable field");
        }

        return true;
    }

    public String getSimpleClassName() {
        return className;
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

    public String getPackageName() {
        return packageName;
    }

    public String getFullyQualifiedClassName() {
        return packageName + "." + className;
    }

    public List<VariableElement> getFields() {
        return fields;
    }

    public String getGetter(String fieldName) {
        return "realmGet$" + fieldName;
    }

    public String getSetter(String fieldName) {
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
        return getGetter(primaryKey.getSimpleName().toString());
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

    private boolean isValidPrimaryKeyType(TypeMirror type) {
        for (TypeMirror validType : validPrimaryKeyTypes) {
            if (typeUtils.isAssignable(type, validType)) {
                return true;
            }
        }
        return false;
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
}

