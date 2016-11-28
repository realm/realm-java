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

import io.realm.annotations.Ignore;

/**
 * Utility class for holding metadata for RealmProxy classes.
 */
public class ClassMetaData {

    private final TypeElement classType; // Reference to model class.
    private String className; // Model class simple name.
    private String packageName; // package name for model class.
    private boolean hasDefaultConstructor; // True if model has a public no-arg constructor.
    private FieldMetaData primaryKey; // Reference to field used as primary key, if any.
    private List<FieldMetaData> fields = new ArrayList<FieldMetaData>(); // List of all fields in the class except those @Ignored.
    private List<FieldMetaData> indexedFields = new ArrayList<FieldMetaData>(); // list of all fields marked @Index.
    private boolean containsToString;
    private boolean containsEquals;
    private boolean containsHashCode;

    private final Elements elements;
    private final ProcessingEnvironment env;

    public ClassMetaData(ProcessingEnvironment env, TypeElement clazz) {
        this.classType = clazz;
        this.className = clazz.getSimpleName().toString();
        this.env = env;
        elements = env.getElementUtils();

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
        for (FieldMetaData field : fields) {
            if (field.getVariableElement().getModifiers().contains(Modifier.TRANSIENT)) {
                Utils.error("Transient fields are not allowed. Class: " + className + ", Field: " +
                        field.getVariableElement().getSimpleName().toString());
                return false;
            }
        }
        return true;
    }

    private boolean checkForVolatileFields() {
        for (FieldMetaData field : fields) {
            if (field.getVariableElement().getModifiers().contains(Modifier.VOLATILE)) {
                Utils.error("Volatile fields are not allowed. Class: " + className + ", Field: " +
                        field.getVariableElement().getSimpleName().toString());
                return false;
            }
        }
        return true;
    }

    private boolean checkForFinalFields() {
        for (FieldMetaData field : fields) {
            if (field.getVariableElement().getModifiers().contains(Modifier.FINAL)) {
                Utils.error("Final fields are not allowed. Class: " + className + ", Field: " +
                        field.getVariableElement().getSimpleName().toString());
                return false;
            }
        }
        return true;
    }

    private boolean checkListTypes() {
        for (FieldMetaData field : fields) {
            if (field.isRealmList()) {
                // Check for missing generic (default back to Object)
                if (field.getGenericTypeQualifiedName() == null) {
                    Utils.error("No generic type supplied for field", field.getVariableElement());
                    return false;
                }

                // Check that the referenced type is a concrete class and not an interface
                TypeMirror fieldType = field.getVariableElement().asType();
                List<? extends TypeMirror> typeArguments = ((DeclaredType) fieldType).getTypeArguments();
                String genericCanonicalType = typeArguments.get(0).toString();
                TypeElement typeElement = elements.getTypeElement(genericCanonicalType);
                if (typeElement.getSuperclass().getKind() == TypeKind.NONE) {
                    Utils.error("Only concrete Realm classes are allowed in RealmLists. Neither " +
                            "interfaces nor abstract classes can be used.", field.getVariableElement());
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkReferenceTypes() {
        for (FieldMetaData field : fields) {
            if (field.isRealmModel()) {
                // Check that the referenced type is a concrete class and not an interface
                TypeElement typeElement = elements.getTypeElement(field.getVariableElement().asType().toString());
                if (typeElement.getSuperclass().getKind() == TypeKind.NONE) {
                    Utils.error("Only concrete Realm classes can be referenced in model classes. " +
                            "Neither interfaces nor abstract classes can be used.", field.getVariableElement());
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

                try {
                    FieldMetaData field = new FieldMetaData(env, variableElement);
                    fields.add(field);

                    if(field.isIndexed()) {
                        indexedFields.add(field);
                    }

                    if(field.isPrimaryKey()) {
                        if (primaryKey != null) {
                            throw new InvalidFieldException(String.format("@PrimaryKey cannot be defined more than once. It was found here \"%s\" and here \"%s\"",
                                    primaryKey.getName(),
                                    variableElement.getSimpleName().toString()));
                        }

                        primaryKey = field;
                    }

                } catch (InvalidFieldException e) {
                    Utils.error(e.getMessage());
                    return false;
                }

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

    public List<FieldMetaData> getFields() {
        return fields;
    }

    public List<FieldMetaData> getIndexedFields() {
        return indexedFields;
    }

    public boolean hasPrimaryKey() {
        return primaryKey != null;
    }

    public FieldMetaData getPrimaryKey() {
        return primaryKey;
    }

    public String getPrimaryKeyGetter() {
        return primaryKey.getGetter();
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

