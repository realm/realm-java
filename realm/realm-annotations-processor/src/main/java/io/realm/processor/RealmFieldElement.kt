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
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Wrapper for {@link javax.lang.model.element.VariableElement} that makes it possible to add
 * additional metadata.
 */
public class RealmFieldElement implements VariableElement {

    private final VariableElement fieldReference;
    private final String internalFieldName; // Name used for this field internally in Realm.

    public RealmFieldElement(VariableElement fieldReference, String internalFieldName) {
        this.fieldReference = fieldReference;
        this.internalFieldName = internalFieldName;
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
}
