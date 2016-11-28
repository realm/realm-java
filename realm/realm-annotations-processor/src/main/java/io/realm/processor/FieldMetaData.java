/*
 * Copyright 2016 Realm Inc.
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

import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import lombok.Getter;


/**
 * Utility class for holding metadata for RealmProxy fields.
 */
@Getter
public class FieldMetaData {

    private final VariableElement variableElement;
    private boolean indexed;
    private boolean nullable;
    private boolean primaryKey;

    private final List<TypeMirror> validPrimaryKeyTypes;

    private final Types typeUtils;

    FieldMetaData(ProcessingEnvironment env, VariableElement variableElement) throws InvalidFieldException {
        this.variableElement = variableElement;
        this.typeUtils = env.getTypeUtils();

        TypeMirror stringType = env.getElementUtils().getTypeElement("java.lang.String").asType();
        validPrimaryKeyTypes = Arrays.asList(
                stringType,
                typeUtils.getPrimitiveType(TypeKind.SHORT),
                typeUtils.getPrimitiveType(TypeKind.INT),
                typeUtils.getPrimitiveType(TypeKind.LONG),
                typeUtils.getPrimitiveType(TypeKind.BYTE)
        );

        if (variableElement.getAnnotation(Index.class) != null) {
            // The field has the @Index annotation. It's only valid for column types:
            // STRING, DATE, INTEGER, BOOLEAN
            String elementTypeCanonicalName = variableElement.asType().toString();
            String columnType = Constants.JAVA_TO_COLUMN_TYPES.get(elementTypeCanonicalName);
            if (columnType != null && (columnType.equals("RealmFieldType.STRING") ||
                    columnType.equals("RealmFieldType.DATE") ||
                    columnType.equals("RealmFieldType.INTEGER") ||
                    columnType.equals("RealmFieldType.BOOLEAN"))) {
                indexed = true;
            } else {
                throw new InvalidFieldException("@Index is not applicable to this field " + variableElement + ".");
            }
        } else {
            indexed = false;
        }

        if (variableElement.getAnnotation(Required.class) == null) {
            // The field doesn't have the @Required annotation.
            // Without @Required annotation, boxed types/RealmObject/Date/String/bytes should be added to
            // nullableFields.
            // RealmList and Primitive types are NOT nullable always. @Required annotation is not supported.
            if (!isPrimitiveType() && !isRealmList()) {
                nullable = true;
            }
        } else {
            // The field has the @Required annotation
            if (isPrimitiveType()) {
                throw new InvalidFieldException("@Required is not needed for field " + variableElement +
                        " with the type " + variableElement.asType());
            } else if (isRealmList()) {
                throw new InvalidFieldException("@Required is invalid for field " + variableElement +
                        " with the type " + variableElement.asType());
            } else if (isRealmModel()) {
                throw new InvalidFieldException("@Required is invalid for field " + variableElement +
                        " with the type " + variableElement.asType());
            } else {
                // Should never get here - user should remove @Required
                if (nullable) {
                    Utils.error("Annotated field " + variableElement + " with type " + variableElement.asType() +
                            " has been added to the nullableFields before. Consider to remove @Required.");
                }
            }
        }

        if (variableElement.getAnnotation(PrimaryKey.class) != null) {
            // The field has the @PrimaryKey annotation. It is only valid for
            // String, short, int, long and must only be present one time


            TypeMirror fieldType = variableElement.asType();
            if (!isValidPrimaryKeyType(fieldType)) {
                throw new InvalidFieldException("\"" + variableElement.getSimpleName().toString() + "\" is not allowed as primary key. See @PrimaryKey for allowed types.");
            }

            primaryKey = true;

            // Also add as index. All types of primary key can be indexed.
            indexed = true;
        }
    }

    public boolean isRealmModel() {
        return Utils.isRealmModel(variableElement);
    }

    public boolean isRealmList() {
        return Utils.isRealmList(variableElement);
    }

    public String getName() {
        return variableElement.getSimpleName().toString();
    }

    private boolean isValidPrimaryKeyType(TypeMirror type) {
        for (TypeMirror validType : validPrimaryKeyTypes) {
            if (typeUtils.isAssignable(type, validType)) {
                return true;
            }
        }
        return false;
    }

    public String getProxyClassSimpleName() {
        return Utils.getProxyClassSimpleName(getVariableElement());
    }

    public String getTypeName() {
        return getVariableElement().asType().toString();
    }

    public String getGetter() {
        return "realmGet$" + getName();
    }

    public String getSetter() {
        return "realmSet$" + getName();
    }

    public boolean isString() {
        if (variableElement == null) {
            throw new IllegalArgumentException("Argument 'field' cannot be null.");
        }
        return Utils.getFieldTypeSimpleName(variableElement).equals("String");
    }

    public boolean isByteArray() {
        if (variableElement == null) {
            throw new IllegalArgumentException("Argument 'field' cannot be null.");
        }
        return Utils.getFieldTypeSimpleName(variableElement).equals("byte[]");
    }

    public boolean isPrimitiveType() {
        if (variableElement == null) {
            throw new IllegalArgumentException("Argument 'field' cannot be null.");
        }
        return variableElement.asType().getKind().isPrimitive();
    }

    public String getGenericTypeQualifiedName() {
        return Utils.getGenericTypeQualifiedName(variableElement);
    }

    public String getFieldTypeSimpleName() {
        return Utils.getFieldTypeSimpleName(variableElement);
    }

    public String getGenericTypeSimpleName() {
        return Utils.getGenericTypeSimpleName(variableElement);
    }

    public String getFieldTypeQualifiedName() {
        return Utils.getFieldTypeQualifiedName(variableElement);
    }
}
