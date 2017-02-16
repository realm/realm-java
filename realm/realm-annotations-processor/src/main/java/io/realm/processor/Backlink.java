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

/**
 * Wrapper class for representing backlinks.
 */
class Backlink {

    /** The class containing the field <code>sourceField</code> with the backlink annotation */
    public final String sourceClass;

    /**
     * The name of the backlinked field, in sourceClass.
     * The <code>RealmResults</code> field annotated with @LinkingObjects.
     */
    public final String sourceField;

    /**
     * The generic type of the RealmResults that is the type of <code>sourceField</code>.
     * Not necessarily <code>targetClass</code>
     */
    public final String sourceFieldType;

    /** The class to which the backlink, from sourceField, points */
    public final String targetClass;

    /**
     * The name of the field, in <code>targetClass</code> that creates the backlink.
     * Making this field, in A, a reference to B will cause the <code>sourceField</code> of B
     * (the <code>sourceClass</code>) to contain a backlink to A.
     */
    public final String targetField;

    public Backlink(String srcClass, String srcField, String srcType, String targetClass, String targetField) {
        if ((null == srcClass) || (null == srcField) || (null == srcType) || (null == targetClass) || (null == targetField)) {
            throw new NullPointerException(
                String.format("null parameter: %s, %s, %s, %s, %s ", srcClass, srcField, srcType, targetClass, targetField));
        }

        this.sourceClass = srcClass;
        this.sourceField = srcField;
        this.sourceFieldType = srcType;
        this.targetClass = targetClass;
        this.targetField = targetField;
    }

    @Override
    public String toString() {
        return "Backlink{" + sourceFieldType + " " + sourceClass + "." + sourceField + " ==> " + targetClass + "." + targetField + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) { return false; }
        if (this == o) { return true; }

        if (!(o instanceof Backlink)) { return false; }
        Backlink backlink = (Backlink) o;

        return sourceClass.equals(backlink.sourceClass)
            && sourceField.equals(backlink.sourceField)
            && sourceFieldType.equals(backlink.sourceFieldType)
            && targetClass.equals(backlink.targetClass)
            && targetField.equals(backlink.targetField);
    }

    @Override
    public int hashCode() {
        int result = sourceClass.hashCode();
        result = 31 * result + sourceField.hashCode();
        result = 31 * result + sourceFieldType.hashCode();
        result = 31 * result + targetClass.hashCode();
        result = 31 * result + targetField.hashCode();
        return result;
    }
}
