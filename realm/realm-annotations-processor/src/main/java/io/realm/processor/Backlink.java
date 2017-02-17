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

import java.util.List;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import io.realm.annotations.LinkingObjects;
import io.realm.annotations.Required;

/**
 * A Backlink is:
 *
 * <code>
 * class SourceClass {
 *     // ...
 *
 *     &at;LinkingObjects("targetField")
 *     RealmResults&lt;TargetClass&gt; sourceField;
 * }
 * </code>.
 *
 * When managed, an instance X of a class with such a declaration will contain in the sourceField
 * references to any instances of TargetClass whose targetField contains a reference to X.
 * When managed, the sourceField cannot be assigned.  It can be queried normally.
 *
 * When unmanaged, the sourceField is just another field: it can be set normally.  Managing an unmanaged
 * instance of SourceClass destroys any previous contents of the backlinked field and replaces it with
 * backlinks.  Unmanagning a managed instance of SourceClass nulls the backlinks field.
 *
 * Note that, because subclassing subclasses of RealmObject is forbidden, so are constructs like:
 * <code>RealmResults&lt;? extends Foos&lt;</code>
 */
final class Backlink {
    private final VariableElement backlink;

    /**
     * The class containing the field <code>sourceField</code> with the backlink annotation
     */
    private final String sourceClass;

    /**
     * The name of the backlinked field, in sourceClass.
     * The <code>RealmResults</code> field annotated with @LinkingObjects.
     */
    private final String sourceField;

    /**
     * The class to which the backlink, from sourceField, points
     */
    private final String targetClass;

    /**
     * The name of the field, in <code>targetClass</code> that creates the backlink.
     * Making this field, in A, a reference to B will cause the <code>sourceField</code> of B
     * (the <code>sourceClass</code>) to contain a backlink to A.
     */
    private final String targetField;


    public Backlink(ClassMetaData klass, VariableElement backlink) {
        if ((null == klass) || (null == backlink)) {
            throw new NullPointerException(String.format("null parameter: %s, %s", klass, backlink));
        }

        this.backlink = backlink;
        this.sourceClass = klass.getFullyQualifiedClassName();
        this.sourceField = backlink.getSimpleName().toString();
        this.targetClass = getRealmResultsType(backlink);
        this.targetField = backlink.getAnnotation(LinkingObjects.class).value();
    }

    public String getSourceClass() { return sourceClass; }

    public String getSourceField() { return sourceField; }

    public String getTargetClass() { return targetClass; }

    public String getTargetField() { return targetField; }

    /**
     * Validate the source side of the backlink.
     *
     * @return true iff the backlink source looks good.
     */
    public boolean validateSource() {
        // A @LinkingObjects cannot be @Required
        if (backlink.getAnnotation(Required.class) != null) {
            Utils.error(String.format("The @LinkingObjects field \"%s\" cannot be @Required.", sourceField));
        }

        // The annotation must have an argument, identifying the linked field
        if (targetField == null || targetField.equals("")) {
            Utils.error(String.format(
                "@LinkingObjects annotation for field \"%s\" requires a parameter identifying the link target.",
                sourceField));
            return false;
        }

        // Using link syntax to try to reference a linked field is not possible.
        if (targetField.contains(".")) {
            Utils.error("@LinkingObjects must refer to a field of the linked class. "
                + "Using '.' to specify fields in referenced classes is not supported.");
            return false;
        }

        // The annotated element must be a RealmResult
        if (!Utils.isRealmResults(backlink)) {
            Utils.error(String.format("Only RealmResults can be @LinkingObjects. Field \"%s\" is a \"%s\".",
                sourceField, sourceClass));
            return false;
        }

        if (targetClass == null) {
            Utils.error("A declaration annotated with @LinkingObjects must be a generically typed RealmResults.");
            return false;
        }

        return true;
    }

    public boolean validateTarget(ClassMetaData klass) {
        VariableElement field = klass.getDeclaredField(targetField);

        if (field == null) {
            Utils.error(String.format(
                "\"%s\", the target of the @LinkedObjects annotation on field \"%s\" in class \"%s\", does not exist in class \"%s\".",
                targetField,
                sourceField,
                sourceClass,
                klass.getFullyQualifiedClassName()));
            return false;
        }

        String fieldType = field.asType().toString();
        if (!(targetClass.equals(fieldType) || targetClass.equals(getRealmListType(field)))) {
            Utils.error(String.format(
                "\"%s\", the target of the @LinkedObjects annotation on field \"%s\" in class \"%s\", has type \"%s\" instead of \"%s\".",
                targetField,
                sourceField,
                sourceClass,
                fieldType,
                targetClass));
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Backlink{" + sourceClass + "." + sourceField + " ==> " + targetClass + "." + targetField + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) { return false; }
        if (this == o) { return true; }

        if (!(o instanceof Backlink)) { return false; }
        Backlink backlink = (Backlink) o;

        return sourceClass.equals(backlink.sourceClass)
            && sourceField.equals(backlink.sourceField)
            && targetClass.equals(backlink.targetClass)
            && targetField.equals(backlink.targetField);
    }

    @Override
    public int hashCode() {
        int result = sourceClass.hashCode();
        result = 31 * result + sourceField.hashCode();
        result = 31 * result + targetClass.hashCode();
        result = 31 * result + targetField.hashCode();
        return result;
    }

    private String getRealmResultsType(VariableElement field) {
        if (!Utils.isRealmResults(field)) { return null; }
        DeclaredType type = getGenericTypeForContainer(field);
        if (null == type) { return null; }
        return type.toString();
    }

    private String getRealmListType(VariableElement field) {
        if (!Utils.isRealmList(field)) { return null; }
        DeclaredType type = getGenericTypeForContainer(field);
        if (null == type) { return null; }
        return type.toString();
    }

    private DeclaredType getGenericTypeForContainer(VariableElement field) {
        TypeMirror fieldType = field.asType();
        TypeKind kind = fieldType.getKind();
        if (kind != TypeKind.DECLARED) { return null; }

        List<? extends TypeMirror> args = ((DeclaredType) fieldType).getTypeArguments();
        if (args.size() <= 0) { return null; }

        fieldType = args.get(0);
        kind = fieldType.getKind();
        if (kind != TypeKind.DECLARED) { return null; }

        return (DeclaredType) fieldType;
    }
}
