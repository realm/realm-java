/*
 * Copyright 2017 Realm Inc.
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

import java.util.Date;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


/**
 * This class provides {@link TypeMirror} instances used in annotation processor.
 */
class TypeMirrors {
    final TypeMirror STRING_MIRROR;
    final TypeMirror BINARY_MIRROR;
    final TypeMirror BOOLEAN_MIRROR;
    final TypeMirror LONG_MIRROR;
    final TypeMirror INTEGER_MIRROR;
    final TypeMirror SHORT_MIRROR;
    final TypeMirror BYTE_MIRROR;
    final TypeMirror DOUBLE_MIRROR;
    final TypeMirror FLOAT_MIRROR;
    final TypeMirror DATE_MIRROR;

    final TypeMirror PRIMITIVE_LONG_MIRROR;
    final TypeMirror PRIMITIVE_INT_MIRROR;
    final TypeMirror PRIMITIVE_SHORT_MIRROR;
    final TypeMirror PRIMITIVE_BYTE_MIRROR;

    TypeMirrors(ProcessingEnvironment env) {
        final Types typeUtils = env.getTypeUtils();
        final Elements elementUtils = env.getElementUtils();

        STRING_MIRROR = elementUtils.getTypeElement("java.lang.String").asType();
        BINARY_MIRROR = typeUtils.getArrayType(typeUtils.getPrimitiveType(TypeKind.BYTE));
        BOOLEAN_MIRROR = elementUtils.getTypeElement(Boolean.class.getName()).asType();
        LONG_MIRROR = elementUtils.getTypeElement(Long.class.getName()).asType();
        INTEGER_MIRROR = elementUtils.getTypeElement(Integer.class.getName()).asType();
        SHORT_MIRROR = elementUtils.getTypeElement(Short.class.getName()).asType();
        BYTE_MIRROR = elementUtils.getTypeElement(Byte.class.getName()).asType();
        DOUBLE_MIRROR = elementUtils.getTypeElement(Double.class.getName()).asType();
        FLOAT_MIRROR = elementUtils.getTypeElement(Float.class.getName()).asType();
        DATE_MIRROR = elementUtils.getTypeElement(Date.class.getName()).asType();

        PRIMITIVE_LONG_MIRROR = typeUtils.getPrimitiveType(TypeKind.LONG);
        PRIMITIVE_INT_MIRROR = typeUtils.getPrimitiveType(TypeKind.INT);
        PRIMITIVE_SHORT_MIRROR = typeUtils.getPrimitiveType(TypeKind.SHORT);
        PRIMITIVE_BYTE_MIRROR = typeUtils.getPrimitiveType(TypeKind.BYTE);
    }

    /**
     * @return the {@link TypeMirror} of the elements in {@code RealmList}.
     */
    public static TypeMirror getRealmListElementTypeMirror(VariableElement field) {
        if (!Utils.isRealmList(field)) {
            return null;
        }
        return ((DeclaredType) field.asType()).getTypeArguments().get(0);
    }
}
