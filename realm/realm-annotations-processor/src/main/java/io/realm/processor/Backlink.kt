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

package io.realm.processor

import java.util.Locale

import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

import io.realm.annotations.LinkingObjects
import io.realm.annotations.Required


/**
 * A **Backlink** is an implicit backwards reference.  If field `sourceField` in instance `I`
 * of type `SourceClass` holds a reference to instance `J` of type `TargetClass`,
 * then a "backlink" is the automatically created reference from `J` to `I`.
 * Backlinks are automatically created and destroyed when the forward references to which they correspond are
 * created and destroyed.  This can dramatically reduce the complexity of client code.
 *
 *
 * To expose backlinks for use, create a declaration as follows:
 * `
 * class TargetClass {
 * // ...
 * @LinkingObjects("sourceField")
 * final RealmResults<SourceClass> targetField = null;
 * }
` * .
 *
 *
 * The targetField, the field annotated with the @LinkingObjects annotation must be final.
 * Its type must be `RealmResults` whose generic argument is the `SourceClass`,
 * the class with the `sourceField` that will hold the forward reference to an instance of
 * `TargetClass`
 *
 *
 * The `sourceField` must be either of type `TargetClass`
 * or `RealmList<TargetClass>`
 *
 *
 * In the code link direction is from the perspective of the link, not the backlink: the source is the
 * instance to which the backlink points, the target is the instance holding the pointer.
 * This is consistent with the use of terms in the Realm Core.
 *
 *
 * As should be obvious, from the declaration, backlinks are useful only on managed objects.
 * An unmanaged Model object will have, as the value of its backlink field, the value with which
 * the field is initialized (typically null).
 */
class Backlink(clazz: ClassMetaData?, private val backlinkField: VariableElement) {

    /**
     * The fully-qualified name of the class containing the `targetField`,
     * which is the field annotated with the @LinkingObjects annotation.
     */
    val targetClass: String

    /**
     * The name of the backlink field, in `targetClass`.
     * A `RealmResults<>` field annotated with a @LinkingObjects annotation.
     */
    val targetField: String

    /**
     * The fully-qualified name of the class to which the backlinks, from `targetField`,
     * point.
     */
    val sourceClass: String?

    /**
     * The name of the field, in `SourceClass` that has a normal link to `targetClass`.
     * Making this field, in an instance I of `SourceClass`,
     * a reference to an instance J of `TargetClass`
     * will cause the `targetField` of J to contain a backlink to I.
     */
    val sourceField: String?

    val targetFieldType: String
        get() = backlinkField.asType().toString()


    init {
        if (null == clazz) {
            throw NullPointerException(String.format(Locale.US, "null parameter: %s, %s", clazz, backlinkField))
        }
        this.targetClass = clazz.fullyQualifiedClassName
        this.targetField = backlinkField.simpleName.toString()
        this.sourceClass = Utils.getRealmResultsType(backlinkField)
        this.sourceField = backlinkField.getAnnotation(LinkingObjects::class.java).value
    }

    /**
     * Validate the source side of the backlink.
     *
     * @return true if the backlink source looks good.
     */
    fun validateSource(): Boolean {
        // A @LinkingObjects cannot be @Required
        if (backlinkField.getAnnotation(Required::class.java) != null) {
            Utils.error(String.format(
                    Locale.US,
                    "The @LinkingObjects field \"%s.%s\" cannot be @Required.",
                    targetClass,
                    targetField))
            return false
        }

        // The annotation must have an argument, identifying the linked field
        if (sourceField == null || sourceField == "") {
            Utils.error(String.format(
                    Locale.US,
                    "The @LinkingObjects annotation for the field \"%s.%s\" must have a parameter identifying the link target.",
                    targetClass,
                    targetField))
            return false
        }

        // Using link syntax to try to reference a linked field is not possible.
        if (sourceField.contains(".")) {
            Utils.error(String.format(
                    Locale.US,
                    "The parameter to the @LinkingObjects annotation for the field \"%s.%s\" contains a '.'.  The use of '.' to specify fields in referenced classes is not supported.",
                    targetClass,
                    targetField))
            return false
        }

        // The annotated element must be a RealmResult
        if (!Utils.isRealmResults(backlinkField)) {
            Utils.error(String.format(
                    Locale.US,
                    "The field \"%s.%s\" is a \"%s\". Fields annotated with @LinkingObjects must be RealmResults.",
                    targetClass,
                    targetField,
                    backlinkField.asType()))
            return false
        }

        if (sourceClass == null) {
            Utils.error(String.format(
                    Locale.US,
                    "\"The field \"%s.%s\", annotated with @LinkingObjects, must specify a generic type.",
                    targetClass,
                    targetField))
            return false
        }

        // A @LinkingObjects field must be final
        if (!backlinkField.getModifiers().contains(Modifier.FINAL)) {
            Utils.error(String.format(
                    Locale.US,
                    "A @LinkingObjects field \"%s.%s\" must be final.",
                    targetClass,
                    targetField))
            return false
        }

        return true
    }

    fun validateTarget(clazz: ClassMetaData): Boolean {
        val field = clazz.getDeclaredField(sourceField)

        if (field == null) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s\", the target of the @LinkedObjects annotation on field \"%s.%s\", does not exist in class \"%s\".",
                    sourceField,
                    targetClass,
                    targetField,
                    sourceClass))
            return false
        }

        val fieldType = field.asType().toString()
        if (!(targetClass == fieldType || targetClass == Utils.getRealmListType(field))) {
            Utils.error(String.format(Locale.US,
                    "Field \"%s.%s\", the target of the @LinkedObjects annotation on field \"%s.%s\", has type \"%s\" instead of \"%3\$s\".",
                    sourceClass,
                    sourceField,
                    targetClass,
                    targetField,
                    fieldType))
            return false
        }

        return true
    }

    override fun toString(): String {
        return "Backlink{$sourceClass.$sourceField ==> $targetClass.$targetField}"
    }

    override fun equals(o: Any?): Boolean {
        if (null == o) {
            return false
        }
        if (this === o) {
            return true
        }

        if (o !is Backlink) {
            return false
        }
        val backlink = o as Backlink?

        return (targetClass == backlink!!.targetClass
                && targetField == backlink.targetField
                && sourceClass == backlink.sourceClass
                && sourceField == backlink.sourceField)
    }

    override fun hashCode(): Int {
        var result = targetClass.hashCode()
        result = 31 * result + targetField.hashCode()
        result = 31 * result + sourceClass!!.hashCode()
        result = 31 * result + sourceField!!.hashCode()
        return result
    }
}
