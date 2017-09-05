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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


public class TypeMirrors {
    public final TypeMirror STRING_MIRROR;
    public final TypeMirror BINARY_MIRROR;
    public final TypeMirror BOOLEAN_MIRROR;
    public final TypeMirror LONG_MIRROR;
    public final TypeMirror INTEGER_MIRROR;
    public final TypeMirror SHORT_MIRROR;
    public final TypeMirror BYTE_MIRROR;
    public final TypeMirror DOUBLE_MIRROR;
    public final TypeMirror FLOAT_MIRROR;
    public final TypeMirror DATE_MIRROR;

    public final TypeMirror PRIMITIVE_LONG_MIRROR;
    public final TypeMirror PRIMITIVE_INT_MIRROR;
    public final TypeMirror PRIMITIVE_SHORT_MIRROR;
    public final TypeMirror PRIMITIVE_BYTE_MIRROR;

    public TypeMirrors(ProcessingEnvironment env) {
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
}
