/*
 * Copyright 2018 Realm Inc.
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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Wrapper for {@link javax.lang.model.element.VariableElement} that makes it possible to add
 * additional metadata.
 */
public class RealmFieldElement implements VariableElement {

    private final VariableElement fieldReference;
    private final String internalFieldName; // Name used for this field internally in Realm.
    private final Elements elementsUtils;
    private final Types typeUtils;
    private final List<TypeMirror> validPrimaryKeyTypes;
    private boolean isNullableValueList = false;
    private boolean isNullable = false;
    private boolean isIgnored = false;
    private boolean isIndexed = false;
    private boolean isPrimaryKey = false;
    private Backlink backlinkInfo;
    private ClassMetaData classData;

    public RealmFieldElement(
            VariableElement fieldReference,
            String internalFieldName,
            Types typeUtils,
            Elements elementsUtils,
            List<TypeMirror> validPrimaryKeyTypes) {
        this.fieldReference = fieldReference;
        this.internalFieldName = internalFieldName;
        this.typeUtils = typeUtils;
        this.elementsUtils = elementsUtils;
        this.validPrimaryKeyTypes = validPrimaryKeyTypes;
    }

    public VariableElement getFieldReference() {
        return fieldReference;
    }

    /**
     * Returns the name that Realm Core uses internally when saving data to this field.
     * {@link #getSimpleName()} returns the name in the Java class.
     */
    public String getInternalFieldName() {
        return internalFieldName;
    }

    public Set<Modifier> getModifiers() {
        return fieldReference.getModifiers();
    }

    public TypeMirror asType() {
        return fieldReference.asType();
    }

    @Override
    public ElementKind getKind() {
        return null;
    }

    @Override
    public Object getConstantValue() {
        return fieldReference.getConstantValue();
    }

    /**
     * Returns the name for this field in the Java class.
     * {@link #getInternalFieldName()} returns the name used by Realm Core for the same field.
     */
    @Override
    public Name getSimpleName() {
        return fieldReference.getSimpleName();
    }

    @Override
    public Element getEnclosingElement() {
        return fieldReference.getEnclosingElement();
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return fieldReference.getEnclosedElements();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return fieldReference.getAnnotationMirrors();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> aClass) {
        return fieldReference.getAnnotation(aClass);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> aClass) {
        return fieldReference.getAnnotationsByType(aClass);
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> elementVisitor, P p) {
        return fieldReference.accept(elementVisitor, p);
    }

    @Override
    public String toString() {
        // Mimics the behaviour of the standard implementation of VariableElement `toString()`
        // Some methods in RealmProxyClassGenerator depended on this.
        return getSimpleName().toString();
    }

    public String getJavaName() {
        return getSimpleName().toString();
    }

    public boolean isIndexed() {
        return isIndexed;
    }

    public boolean isIgnored() {
        return isIgnored;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public boolean isNullableValueList() {
        return isNullableValueList;
    }

    public boolean isNullable() {
        return isNullable;
    }

    /**
     * Generates the metadata associated with this field and validates it in the process.
     *
     * @return {@code true} if metadata was succesfully generated, {@code false} if an error was
     * reported while doing so.
     */
    public boolean generateFieldMetaData(
            ClassMetaData classData,
            @Nullable VariableElement existingPrimaryKey,
            boolean ignoreKotlinNullability,
            List<TypeMirror> validPrimaryKeyTypes,
            Types typeUtils) {

        this.classData = classData;

        // completely ignore any static fields
        if (fieldReference.getModifiers().contains(Modifier.STATIC)) {
            isIgnored = true;
            return true;
        }

        // Ignore fields marked with @Ignore or if they are transient
        if (fieldReference.getAnnotation(Ignore.class) != null || fieldReference.getModifiers().contains(Modifier.TRANSIENT)) {
            isIgnored = true;
            return true;
        }

        if (fieldReference.getAnnotation(Index.class) != null) {
            if (!categorizeIndexField(this)) { return false; }
        }

        // @Required annotation of RealmList field only affects its value type, not field itself.
        if (Utils.isRealmList(fieldReference)) {
            boolean hasRequiredAnnotation = hasRequiredAnnotation(fieldReference);
            final List<? extends TypeMirror> listGenericType = ((DeclaredType) fieldReference.asType()).getTypeArguments();
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
                    isNullableValueList = true;
                }
            }
        } else if (isRequiredField(fieldReference, ignoreKotlinNullability)) {
            if (!checkBasicRequiredAnnotationUsage(fieldReference)) {
                return false;
            }
        } else {
            // The field doesn't have the @Required and @org.jetbrains.annotations.NotNull annotation.
            // Without @Required annotation, boxed types/RealmObject/Date/String/bytes should be added to
            // nullableFields.
            // RealmList of models, RealmResults(backlinks) and primitive types are NOT nullable. @Required annotation is not supported.
            if (!Utils.isPrimitiveType(fieldReference) && !Utils.isRealmResults(fieldReference)) {
                isNullable = true;
            }
        }

        if (fieldReference.getAnnotation(PrimaryKey.class) != null) {
            if (!categorizePrimaryKeyField(this, existingPrimaryKey)) { return false; }
        }

        // @LinkingObjects cannot be @PrimaryKey or @Index.
        if (fieldReference.getAnnotation(LinkingObjects.class) != null) {
            // Do not add backlinks to fields list.
            return categorizeBacklinkField(fieldReference);
        }

        // Similarly, a MutableRealmInteger cannot be a @PrimaryKey or @LinkingObject.
        if (Utils.isMutableRealmInteger(fieldReference)) {
            if (!categorizeMutableRealmIntegerField(fieldReference)) { return false; }
        }

        return true;
    }

    public boolean isBacklink() {
        return backlinkInfo != null;
    }

    @Nullable
    public Backlink getBacklinkInfo() {
        return backlinkInfo;
    }

    /**
     * This method only checks if the field has {@code @Required} annotation.
     * In most cases, you should use {@link #isRequiredField(VariableElement, boolean)} to take into account
     * Kotlin's annotation as well.
     *
     * @param field target field.
     * @return {@code true} if the field has {@code @Required} annotation, {@code false} otherwise.
     * @see #isRequiredField(VariableElement, boolean)
     */
    private boolean hasRequiredAnnotation(VariableElement field) {
        return field.getAnnotation(Required.class) != null;
    }

    /**
     * Checks if the field is annotated as required.
     * @param field target field.
     * @return {@code true} if the field is annotated as required, {@code false} otherwise.
     */
    private boolean isRequiredField(VariableElement field, boolean ignoreKotlinNullability) {
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
    private boolean categorizeIndexField(VariableElement field) {
        boolean indexable = false;

        if (Utils.isMutableRealmInteger(field)) {
            indexable = true;
        } else {
            Constants.RealmFieldType realmType = Constants.JAVA_TO_REALM_TYPES.get(field.asType().toString());
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
            isIndexed = true;
            return true;
        }

        Utils.error(String.format(Locale.US, "Field \"%s\" of type \"%s\" cannot be an @Index.", field, field.asType()));
        return false;
    }

    // The field has the @Required annotation
    // Returns `true` if the field could be correctly validated, `false` if an error was reported.
    private boolean checkBasicRequiredAnnotationUsage(VariableElement field) {
        if (Utils.isPrimitiveType(field)) {
            Utils.error(String.format(Locale.US,
                    "@Required or @NotNull annotation is unnecessary for primitive field \"%s\".", field));
            return false;
        }

        if (Utils.isRealmModel(field)) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s\" with type \"%s\" cannot be @Required or @NotNull.", field, field.asType()));
            return false;
        }

        // Should never get here - user should remove @Required
        if (isNullable()) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s\" with type \"%s\" appears to be nullable. Consider removing @Required.",
                    field,
                    field.asType()));

            return false;
        }

        return true;
    }

    // The field has the @PrimaryKey annotation. It is only valid for
    // String, short, int, long and must only be present one time
    private boolean categorizePrimaryKeyField(RealmFieldElement fieldElement, @Nullable VariableElement existingPrimaryKey) {
        if (existingPrimaryKey != null) {
            Utils.error(String.format(Locale.US,
                    "A class cannot have more than one @PrimaryKey. Both \"%s\" and \"%s\" are annotated as @PrimaryKey.",
                    existingPrimaryKey.getSimpleName().toString(),
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

        isPrimaryKey = true;
        isIndexed = true; // Also add as index. All types of primary key can be indexed.

        return true;
    }

    private boolean categorizeBacklinkField(VariableElement field) {
        Backlink backlink = new Backlink(classData, field, elementsUtils);
        if (!backlink.validateSource()) { return false; }
        this.backlinkInfo = backlink;
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

}
