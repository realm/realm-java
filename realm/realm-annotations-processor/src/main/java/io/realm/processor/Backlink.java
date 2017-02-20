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
 * <p>
 * <code>
 * class TargetClass {
 *     // ...
 *     {@literal @}LinkingObjects("sourceField")
 *     RealmResults&lt;SourceClass&gt; targetField;
 * }
 * </code>.
 * <p>
 * The targetField of a managed object cannot be assigned.  It can be queried normally.
 * When an instance X of a class with such a declaration is copied to Realm (`copyToRealm()`),
 * the result will contain in its targetField references to any instances of SourceClass
 * whose sourceField contains a reference to X.  Any previous contents of the targetField are lost.
 * <p>
 * When an instance X is copied from Realm (`copyFromRealm()`) the targetField in the returned object
 * is just another field: it can be set normally. The field is initially set to be `null`.
 * <p>
 * Note that, because subclassing subclasses of RealmObject is forbidden, so are constructs like:
 * <code>RealmResults&lt;? extends Foos&lt;</code>
 * <p>
 * In the code link direction is from the perspective of the link, not the backlink: the source is the
 * instance to which the backlink points, the target is the instance holding the pointer.
 * This is consistent with the use of terms in the Realm Core.
 */
final class Backlink {
    private final VariableElement backlink;

    /**
     * The FQN of the class containing the field <code>targetField</code> that is
     * annotated with the {@literal @}LinkingObjects annotation.
     */
    private final String targetClass;

    /**
     * The name of the backlinked field, in <code>targetClass</code>.
     * A <code>RealmResults&lt;&gt;</code> field annotated with a {@literal @}LinkingObjects annotation.
     */
    private final String targetField;

    /**
     * The FQN of the class to which the backlinks, from <code>targetField</code>, point:
     * The generic argument to the type of the <code>targetField</code> field.
     */
    private final String sourceClass;

    /**
     * The name of the field, in <code>sourceClass</code> that creates the backlink.
     * Making this field, in an instance X of <code>sourceClass</code>,
     * a reference to an instance Y of <code>targetClass</code>
     * will cause the <code>targetField</code> of Y to contain a backlink to X.
     */
    private final String sourceField;


    public Backlink(ClassMetaData klass, VariableElement backlink) {
        if ((null == klass) || (null == backlink)) {
            throw new NullPointerException(String.format("null parameter: %s, %s", klass, backlink));
        }

        this.backlink = backlink;
        this.targetClass = klass.getFullyQualifiedClassName();
        this.targetField = backlink.getSimpleName().toString();
        this.sourceClass = getRealmResultsType(backlink);
        this.sourceField = backlink.getAnnotation(LinkingObjects.class).value();
    }

    public String getTargetClass() { return targetClass; }

    public String getTargetField() { return targetField; }

    public String getSourceClass() { return sourceClass; }

    public String getSourceField() { return sourceField; }

    /**
     * Validate the source side of the backlink.
     *
     * @return true iff the backlink source looks good.
     */
    public boolean validateSource() {
        // A @LinkingObjects cannot be @Required
        if (backlink.getAnnotation(Required.class) != null) {
            Utils.error(String.format(
                "The @LinkingObjects field \"%s.%s\" cannot be @Required.",
                targetClass,
                targetField));
        }

        // The annotation must have an argument, identifying the linked field
        if ((sourceField == null) || sourceField.equals("")) {
            Utils.error(String.format(
                "The @LinkingObjects annotation for the field \"%s.%s\" must have a parameter identifying the link target.",
                targetClass,
                targetField));
            return false;
        }

        // Using link syntax to try to reference a linked field is not possible.
        if (sourceField.contains(".")) {
            Utils.error(String.format(
                "The parameter to the @LinkingObjects annotation for the field \"%s.%s\" contains a '.'.  The use of '.' to specify fields in referenced classes is not supported.",
                targetClass,
                targetField));
            return false;
        }

        // The annotated element must be a RealmResult
        if (!Utils.isRealmResults(backlink)) {
            Utils.error(String.format(
                "The field \"%s.%s\" is a \"%s\". Fields annotated with @LinkingObjects must be RealmResults.",
                targetClass,
                targetField,
                backlink.asType()));
            return false;
        }

        if (sourceClass == null) {
            Utils.error(String.format(
                "\"The field \"%s.%s\", annotated with @LinkingObjects, must specify a generic type.",
                targetClass,
                targetField));
            return false;
        }

        return true;
    }

    public boolean validateTarget(ClassMetaData klass) {
        VariableElement field = klass.getDeclaredField(sourceField);

        if (field == null) {
            Utils.error(String.format(
                "Field \"%s\", the target of the @LinkedObjects annotation on field \"%s.%s\", does not exist in class \"%s\".",
                sourceField,
                targetClass,
                targetField,
                sourceClass));
            return false;
        }

        String fieldType = field.asType().toString();
        if (!(targetClass.equals(fieldType) || targetClass.equals(getRealmListType(field)))) {
            Utils.error(String.format(
                "Field \"%s.%s\", the target of the @LinkedObjects annotation on field \"%s.%s\", has type \"%s\" instead of \"%s\".",
                sourceClass,
                sourceField,
                targetClass,
                targetField,
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

        return targetClass.equals(backlink.targetClass)
            && targetField.equals(backlink.targetField)
            && sourceClass.equals(backlink.sourceClass)
            && sourceField.equals(backlink.sourceField);
    }

    @Override
    public int hashCode() {
        int result = targetClass.hashCode();
        result = 31 * result + targetField.hashCode();
        result = 31 * result + sourceClass.hashCode();
        result = 31 * result + sourceField.hashCode();
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
