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
package io.realm.processor

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ElementVisitor
import javax.lang.model.element.Modifier
import javax.lang.model.element.Name
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

/**
 * Wrapper for [javax.lang.model.element.VariableElement] that makes it possible to add
 * additional metadata.
 */
class RealmFieldElement(val fieldReference: VariableElement,
                        /**
                         * Returns the name that Realm Core uses internally when saving data to this field.
                         * [RealmFieldElement.getSimpleName] returns the name in the Java class.
                         */
                        val internalFieldName: String // Name used for this field internally in Realm.
) : VariableElement {

    val javaName: String
        get() = simpleName.toString()

    override fun getModifiers(): Set<Modifier> {
        return fieldReference.modifiers
    }

    override fun asType(): TypeMirror {
        return fieldReference.asType()
    }

    override fun getKind(): ElementKind? {
        return null
    }

    override fun getConstantValue(): Any {
        return fieldReference.constantValue
    }

    /**
     * Returns the name for this field in the Java class.
     * [RealmFieldElement.internalFieldName] returns the name used by Realm Core for the same field.
     */
    override fun getSimpleName(): Name {
        return fieldReference.simpleName
    }

    override fun getEnclosingElement(): Element {
        return fieldReference.enclosingElement
    }

    override fun getEnclosedElements(): List<Element> {
        return fieldReference.enclosedElements
    }

    override fun getAnnotationMirrors(): List<AnnotationMirror> {
        return fieldReference.annotationMirrors
    }

    override fun <A : Annotation> getAnnotation(aClass: Class<A>): A? {
        return fieldReference.getAnnotation(aClass)
    }

    override fun <A : Annotation> getAnnotationsByType(aClass: Class<A>): Array<A> {
        return fieldReference.getAnnotationsByType(aClass)
    }

    override fun <R, P> accept(elementVisitor: ElementVisitor<R, P>, p: P): R {
        return fieldReference.accept(elementVisitor, p)
    }

    override fun toString(): String {
        // Mimics the behaviour of the standard implementation of VariableElement `toString()`
        // Some methods in RealmProxyClassGenerator depended on this.
        return simpleName.toString()
    }
}
