/*
 * Copyright 2019 Realm Inc.
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
@file:JvmName("TypeMirrors")
package io.realm.processor

import org.bson.types.Decimal128
import org.bson.types.ObjectId
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

/**
 * This class provides [TypeMirror] instances used in annotation processor.
 *
 * WARNING: Comparing type mirrors using either `==` or `equal()` can break when using incremental
 * annotation processing. Always use `Types.isSameType()` instead when comparing them.
 */
class TypeMirrors(env: ProcessingEnvironment) {

    @JvmField val STRING_MIRROR: TypeMirror
    @JvmField val BINARY_MIRROR: TypeMirror
    @JvmField val BOOLEAN_MIRROR: TypeMirror
    @JvmField val LONG_MIRROR: TypeMirror
    @JvmField val INTEGER_MIRROR: TypeMirror
    @JvmField val SHORT_MIRROR: TypeMirror
    @JvmField val BYTE_MIRROR: TypeMirror
    @JvmField val DOUBLE_MIRROR: TypeMirror
    @JvmField val FLOAT_MIRROR: TypeMirror
    @JvmField val DATE_MIRROR: TypeMirror
    @JvmField val DECIMAL128_MIRROR: TypeMirror
    @JvmField val OBJECT_ID_MIRROR: TypeMirror
    @JvmField val UUID_MIRROR: TypeMirror
    @JvmField val MIXED_MIRROR: TypeMirror

    @JvmField val PRIMITIVE_LONG_MIRROR: TypeMirror
    @JvmField val PRIMITIVE_INT_MIRROR: TypeMirror
    @JvmField val PRIMITIVE_SHORT_MIRROR: TypeMirror
    @JvmField val PRIMITIVE_BYTE_MIRROR: TypeMirror

    init {
        val typeUtils = env.typeUtils
        val elementUtils = env.elementUtils

        STRING_MIRROR = elementUtils.getTypeElement("java.lang.String").asType()
        BINARY_MIRROR = typeUtils.getArrayType(typeUtils.getPrimitiveType(TypeKind.BYTE))
        BOOLEAN_MIRROR = elementUtils.getTypeElement(Boolean::class.javaObjectType.name).asType()
        LONG_MIRROR = elementUtils.getTypeElement(Long::class.javaObjectType.name).asType()
        INTEGER_MIRROR = elementUtils.getTypeElement(Int::class.javaObjectType.name).asType()
        SHORT_MIRROR = elementUtils.getTypeElement(Short::class.javaObjectType.name).asType()
        BYTE_MIRROR = elementUtils.getTypeElement(Byte::class.javaObjectType.name).asType()
        DOUBLE_MIRROR = elementUtils.getTypeElement(Double::class.javaObjectType.name).asType()
        FLOAT_MIRROR = elementUtils.getTypeElement(Float::class.javaObjectType.name).asType()
        DATE_MIRROR = elementUtils.getTypeElement(Date::class.javaObjectType.name).asType()
        DECIMAL128_MIRROR = elementUtils.getTypeElement(Decimal128::class.javaObjectType.name).asType()
        OBJECT_ID_MIRROR = elementUtils.getTypeElement(ObjectId::class.javaObjectType.name).asType()
        UUID_MIRROR = elementUtils.getTypeElement(UUID::class.javaObjectType.name).asType()
        MIXED_MIRROR = elementUtils.getTypeElement("io.realm.Mixed").asType()

        PRIMITIVE_LONG_MIRROR = typeUtils.getPrimitiveType(TypeKind.LONG)
        PRIMITIVE_INT_MIRROR = typeUtils.getPrimitiveType(TypeKind.INT)
        PRIMITIVE_SHORT_MIRROR = typeUtils.getPrimitiveType(TypeKind.SHORT)
        PRIMITIVE_BYTE_MIRROR = typeUtils.getPrimitiveType(TypeKind.BYTE)
    }

    companion object {
        /**
         * @return the [TypeMirror] of the elements in `RealmList`.
         */
        @JvmStatic
        fun getRealmListElementTypeMirror(field: VariableElement): TypeMirror? {
            if (!Utils.isRealmList(field)) {
                return null
            }
            val typeArguments = (field.asType() as DeclaredType).typeArguments
            return if (typeArguments.isNotEmpty()) typeArguments[0] else null
        }
    }
}
