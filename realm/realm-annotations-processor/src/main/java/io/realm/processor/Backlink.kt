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

import java.util.Locale;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import io.realm.annotations.LinkingObjects;
import io.realm.annotations.Required;


/**
 * A <b>Backlink</b> is an implicit backwards reference.  If field <code>sourceField</code> in instance <code>I</code>
 * of type <code>SourceClass</code> holds a reference to instance <code>J</code> of type <code>TargetClass</code>,
 * then a "backlink" is the automatically created reference from <code>J</code> to <code>I</code>.
 * Backlinks are automatically created and destroyed when the forward references to which they correspond are
 * created and destroyed.  This can dramatically reduce the complexity of client code.
 * <p>
 * To expose backlinks for use, create a declaration as follows:
 * <code>
 * class TargetClass {
 * // ...
 * {@literal @}LinkingObjects("sourceField")
 * final RealmResults&lt;SourceClass&gt; targetField = null;
 * }
 * </code>.
 * <p>
 * The targetField, the field annotated with the {@literal @}LinkingObjects annotation must be final.
 * Its type must be <code>RealmResults</code> whose generic argument is the <code>SourceClass</code>,
 * the class with the <code>sourceField</code> that will hold the forward reference to an instance of
 * <code>TargetClass</code>
 * <p>
 * The <code>sourceField</code> must be either of type <code>TargetClass</code>
 * or <code>RealmList&lt;TargetClass&gt;</code>
 * <p>
 * In the code link direction is from the perspective of the link, not the backlink: the source is the
 * instance to which the backlink points, the target is the instance holding the pointer.
 * This is consistent with the use of terms in the Realm Core.
 * <p>
 * As should be obvious, from the declaration, backlinks are useful only on managed objects.
 * An unmanaged Model object will have, as the value of its backlink field, the value with which
 * the field is initialized (typically null).
 */
final class Backlink {
    private final VariableElement backlinkField;

    /**
     * The fully-qualified name of the class containing the <code>targetField</code>,
     * which is the field annotated with the {@literal @}LinkingObjects annotation.
     */
    private final String targetClass;

    /**
     * The name of the backlink field, in <code>targetClass</code>.
     * A <code>RealmResults&lt;&gt;</code> field annotated with a {@literal @}LinkingObjects annotation.
     */
    private final String targetField;

    /**
     * The fully-qualified name of the class to which the backlinks, from <code>targetField</code>,
     * point.
     */
    private final String sourceClass;

    /**
     * The name of the field, in <code>SourceClass</code> that has a normal link to <code>targetClass</code>.
     * Making this field, in an instance I of <code>SourceClass</code>,
     * a reference to an instance J of <code>TargetClass</code>
     * will cause the <code>targetField</code> of J to contain a backlink to I.
     */
    private final String sourceField;


    public Backlink(ClassMetaData clazz, VariableElement backlinkField) {
        if ((null == clazz) || (null == backlinkField)) {
            throw new NullPointerException(String.format(Locale.US, "null parameter: %s, %s", clazz, backlinkField));
        }

        this.backlinkField = backlinkField;
        this.targetClass = clazz.getFullyQualifiedClassName();
        this.targetField = backlinkField.getSimpleName().toString();
        this.sourceClass = Utils.getRealmResultsType(backlinkField);
        this.sourceField = backlinkField.getAnnotation(LinkingObjects.class).value();
    }

    public String getTargetClass() {
        return targetClass;
    }

    public String getTargetField() {
        return targetField;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public String getSourceField() {
        return sourceField;
    }

    public String getTargetFieldType() {
        return backlinkField.asType().toString();
    }

    /**
     * Validate the source side of the backlink.
     *
     * @return true if the backlink source looks good.
     */
    public boolean validateSource() {
        // A @LinkingObjects cannot be @Required
        if (backlinkField.getAnnotation(Required.class) != null) {
            Utils.error(String.format(
                    Locale.US,
                    "The @LinkingObjects field \"%s.%s\" cannot be @Required.",
                    targetClass,
                    targetField));
            return false;
        }

        // The annotation must have an argument, identifying the linked field
        if ((sourceField == null) || sourceField.equals("")) {
            Utils.error(String.format(
                    Locale.US,
                    "The @LinkingObjects annotation for the field \"%s.%s\" must have a parameter identifying the link target.",
                    targetClass,
                    targetField));
            return false;
        }

        // Using link syntax to try to reference a linked field is not possible.
        if (sourceField.contains(".")) {
            Utils.error(String.format(
                    Locale.US,
                    "The parameter to the @LinkingObjects annotation for the field \"%s.%s\" contains a '.'.  The use of '.' to specify fields in referenced classes is not supported.",
                    targetClass,
                    targetField));
            return false;
        }

        // The annotated element must be a RealmResult
        if (!Utils.isRealmResults(backlinkField)) {
            Utils.error(String.format(
                    Locale.US,
                    "The field \"%s.%s\" is a \"%s\". Fields annotated with @LinkingObjects must be RealmResults.",
                    targetClass,
                    targetField,
                    backlinkField.asType()));
            return false;
        }

        if (sourceClass == null) {
            Utils.error(String.format(
                    Locale.US,
                    "\"The field \"%s.%s\", annotated with @LinkingObjects, must specify a generic type.",
                    targetClass,
                    targetField));
            return false;
        }

        // A @LinkingObjects field must be final
        if (!backlinkField.getModifiers().contains(Modifier.FINAL)) {
            Utils.error(String.format(
                    Locale.US,
                    "A @LinkingObjects field \"%s.%s\" must be final.",
                    targetClass,
                    targetField));
            return false;
        }

        return true;
    }

    public boolean validateTarget(ClassMetaData clazz) {
        VariableElement field = clazz.getDeclaredField(sourceField);

        if (field == null) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s\", the target of the @LinkedObjects annotation on field \"%s.%s\", does not exist in class \"%s\".",
                    sourceField,
                    targetClass,
                    targetField,
                    sourceClass));
            return false;
        }

        String fieldType = field.asType().toString();
        if (!(targetClass.equals(fieldType) || targetClass.equals(Utils.getRealmListType(field)))) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s.%s\", the target of the @LinkedObjects annotation on field \"%s.%s\", has type \"%s\" instead of \"%3$s\".",
                    sourceClass,
                    sourceField,
                    targetClass,
                    targetField,
                    fieldType));
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
}
