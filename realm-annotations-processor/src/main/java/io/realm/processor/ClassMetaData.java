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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

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
    private List<String> fieldNames = new ArrayList<String>();
    private List<String> ignoreFieldNames = new ArrayList<String>();
    private List<VariableElement> indexedFields = new ArrayList<VariableElement>(); // list of all fields marked @Index.
    private Set<String> expectedGetters = new HashSet<String>(); // Set of fieldnames that are expected to have a getter
    private Set<String> expectedSetters = new HashSet<String>(); // Set of fieldnames that are expected to have a setter
    private Set<ExecutableElement> methods = new HashSet<ExecutableElement>(); // List of all methods in the model class
    private Map<String, String> getters = new HashMap<String, String>(); // Map between fieldnames and their getters
    private Map<String, String> setters = new HashMap<String, String>(); // Map between fieldname and their setters

    private final List<TypeMirror> validPrimaryKeyTypes;
    private final Types typeUtils;
    private DeclaredType realmList;

    public ClassMetaData(ProcessingEnvironment env, TypeElement clazz) {
        this.classType = clazz;
        this.className = clazz.getSimpleName().toString();
        typeUtils = env.getTypeUtils();
        TypeMirror stringType = env.getElementUtils().getTypeElement("java.lang.String").asType();
        realmList = typeUtils.getDeclaredType(env.getElementUtils().getTypeElement("io.realm.RealmList"), typeUtils.getWildcardType(null, null));
        validPrimaryKeyTypes = Arrays.asList(
                stringType,
                typeUtils.getPrimitiveType(TypeKind.SHORT),
                typeUtils.getPrimitiveType(TypeKind.INT),
                typeUtils.getPrimitiveType(TypeKind.LONG)
        );
    }

    /**
     * Build the meta data structures for this class. Any errors or messages will be
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
        if (!parentElement.toString().endsWith(".RealmObject")) {
            Utils.error("A RealmClass annotated object must be derived from RealmObject", classType);
            return false;
        }

        PackageElement packageElement = (PackageElement) enclosingElement;
        packageName = packageElement.getQualifiedName().toString();

        if (!categorizeClassElements()) return false;
        if (!checkListTypes()) return  false;
        if (!checkMethods()) return false;
        if (!checkDefaultConstructor()) return false;
        if (!checkRequiredGetters()) return false;
        if (!checkRequireSetters()) return false;

        return true; // Meta data was successfully generated
    }

    // Check that only allowed methods are present in the model class
    private boolean checkMethods() {
        for (ExecutableElement executableElement : methods) {
            String methodName = executableElement.getSimpleName().toString();

            // Check the modifiers of the method
            Set<Modifier> modifiers = executableElement.getModifiers();
            if (modifiers.contains(Modifier.STATIC)) {
                continue; // We're cool with static methods. Move along!
            } else if (!modifiers.contains(Modifier.PUBLIC)) {
                Utils.error("The methods of the model must be public", executableElement);
                return false;
            }

            // Check that getters and setters are valid
            if (methodName.startsWith("get") || methodName.startsWith("is")) {
                if (!checkGetterMethod(methodName)) {
                    Utils.error(String.format("Getter %s is not associated to any field", methodName), executableElement);
                    return false;
                }
            } else if (methodName.startsWith("set")) {
                if (!checkSetterMethod(methodName)) {
                    Utils.error(String.format("Setter %s is not associated to any field", methodName), executableElement);
                    return false;
                }
            } else {
                Utils.error("Only getters and setters should be defined in model classes", executableElement);
                return false;
            }
        }

        return true;
    }

    private boolean checkListTypes() {
        for (VariableElement field : fields) {
            if (typeUtils.isAssignable(field.asType(), realmList)) {
                if (Utils.getGenericType(field) == null) {
                    Utils.error("No generic type supplied for field", field);
                    return false;
                }
            }
        }
        return true;
    }

    // Verify that a setter is used to set a field in the model class.
    // Note: This is done heuristically by comparing the name of setter with the name of the field.
    // Annotation processors does not allow us to inspect individual statements.
    private boolean checkSetterMethod(String methodName) {
        boolean found = false;

        String methodMinusSet = methodName.substring(3);
        String methodMinusSetCapitalised = Utils.lowerFirstChar(methodMinusSet);
        String methodMenusSetPlusIs = "is" + methodMinusSet;

        if (fieldNames.contains(methodMinusSet)) { // mPerson -> setmPerson
            expectedSetters.remove(methodMinusSet);
            if (!ignoreFieldNames.contains(methodMinusSet)) {
                setters.put(methodMinusSet, methodName);
            }
            found = true;
        } else if (fieldNames.contains(methodMinusSetCapitalised)) { // person -> setPerson
            expectedSetters.remove(methodMinusSetCapitalised);
            if (!ignoreFieldNames.contains(methodMinusSetCapitalised)) {
                setters.put(methodMinusSetCapitalised, methodName);
            }
            found = true;
        } else if (fieldNames.contains(methodMenusSetPlusIs)) { // isReady -> setReady
            expectedSetters.remove(methodMenusSetPlusIs);
            if (!ignoreFieldNames.contains(methodMenusSetPlusIs)) {
                setters.put(methodMenusSetPlusIs, methodName);
            }
            found = true;
        }

        return found;
    }

    // Verify that a getter is used to get a field in the model class.
    // Note: This is done heuristically by comparing the name of getter with the name of the field.
    // Annotation processors does not allow us to inspect individual statements.
    private boolean checkGetterMethod(String methodName) {
        boolean found = false;

        if (methodName.startsWith("is")) {
            String methodMinusIs = methodName.substring(2);
            String methodMinusIsCapitalised = Utils.lowerFirstChar(methodMinusIs);
            if (fieldNames.contains(methodName)) { // isDone -> isDone
                expectedGetters.remove(methodName);
                if (!ignoreFieldNames.contains(methodName)) {
                    getters.put(methodName, methodName);
                }
                found = true;
            } else if (fieldNames.contains(methodMinusIs)) {  // mDone -> ismDone
                expectedGetters.remove(methodMinusIs);
                if (!ignoreFieldNames.contains(methodMinusIs)) {
                    getters.put(methodMinusIs, methodName);
                }
                found = true;
            } else if (fieldNames.contains(methodMinusIsCapitalised)) { // done -> isDone
                expectedGetters.remove(methodMinusIsCapitalised);
                if (!ignoreFieldNames.contains(methodMinusIsCapitalised)) {
                    getters.put(methodMinusIsCapitalised, methodName);
                }
                found = true;
            }
        }

        if (!found && methodName.startsWith("get")) {
            String methodMinusGet = methodName.substring(3);
            String methodMinusGetCapitalised = Utils.lowerFirstChar(methodMinusGet);
            if (fieldNames.contains(methodMinusGet)) { // mPerson -> getmPerson
                expectedGetters.remove(methodMinusGet);
                if (!ignoreFieldNames.contains(methodMinusGet)) {
                    getters.put(methodMinusGet, methodName);
                }
                found = true;
            } else if (fieldNames.contains(methodMinusGetCapitalised)) { // person -> getPerson
                expectedGetters.remove(methodMinusGetCapitalised);
                if (!ignoreFieldNames.contains(methodMinusGetCapitalised)) {
                    getters.put(methodMinusGetCapitalised, methodName);
                }
                found = true;
            }
        }

        return found;
    }

    // Report any setters that are missing
    private boolean checkRequireSetters() {
        for (String expectedSetter : expectedSetters) {
            Utils.error("No setter found for field " + expectedSetter);
        }
        return expectedSetters.size() == 0;
    }

    // Report any getters that are missing
    private boolean checkRequiredGetters() {
        for (String expectedGetter : expectedGetters) {
            Utils.error("No getter found for field " + expectedGetter);
        }
        return expectedGetters.size() == 0;
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
                String fieldName = variableElement.getSimpleName().toString();

                Set<Modifier> modifiers = variableElement.getModifiers();
                if (modifiers.contains(Modifier.STATIC)) {
                    continue; // completely ignore any static fields
                }

                if (variableElement.getAnnotation(Ignore.class) != null) {
                    // The field has the @Ignore annotation. No need to go any further.
                    String ignoredFieldName = variableElement.getSimpleName().toString();
                    fieldNames.add(ignoredFieldName);
                    ignoreFieldNames.add(ignoredFieldName);
                    continue;
                }

                if (variableElement.getAnnotation(Index.class) != null) {
                    // The field has the @Index annotation. It's only valid for column types:
                    // STRING, DATE, INTEGER, BOOLEAN
                    String elementTypeCanonicalName = variableElement.asType().toString();
                    String columnType = Constants.JAVA_TO_COLUMN_TYPES.get(elementTypeCanonicalName);
                    if (columnType != null && (columnType.equals("ColumnType.STRING") ||
                            columnType.equals("ColumnType.DATE") ||
                            columnType.equals("ColumnType.INTEGER") ||
                            columnType.equals("ColumnType.BOOLEAN"))) {
                        indexedFields.add(variableElement);
                    } else {
                        Utils.error("@Index is not applicable to this field " + element + ".");
                        return false;
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

                if (!variableElement.getModifiers().contains(Modifier.PRIVATE)) {
                    Utils.error("The fields of the model must be private", variableElement);
                    return false;
                }

                fields.add(variableElement);
                expectedGetters.add(fieldName);
                expectedSetters.add(fieldName);
            } else if (elementKind.equals(ElementKind.CONSTRUCTOR)) {
                hasDefaultConstructor =  hasDefaultConstructor || Utils.isDefaultConstructor(element);

            } else if (elementKind.equals(ElementKind.METHOD)) {
                ExecutableElement executableElement = (ExecutableElement) element;
                methods.add(executableElement);
            }
        }

        for (VariableElement field : fields) {
            fieldNames.add(field.getSimpleName().toString());
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
     * Returns true if the model class is considered to be a true model class.
     * RealmObject and Proxy classes also has the the @RealmClass annotation but is not considered true
     * model classes.
     */
    public boolean isModelClass() {
        String type = classType.toString();
        if (type.equals("io.realm.dynamic.DynamicRealmObject")) {
            return false;
        }
        return (!type.endsWith(".RealmObject") && !type.endsWith("RealmProxy"));
    }

    public String getFullyQualifiedClassName() {
        return packageName + "." + className;
    }

    public List<VariableElement> getFields() {
        return fields;
    }

    public String getGetter(String fieldName) {
        return getters.get(fieldName);
    }

    public String getSetter(String fieldName) {
        return setters.get(fieldName);
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
        return getters.get(primaryKey.getSimpleName().toString());
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

