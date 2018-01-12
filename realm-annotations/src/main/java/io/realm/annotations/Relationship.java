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

package io.realm.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to describe the relationship between two objects through either a object or list
 * reference. See {@link RelationshipType} for a detailed description of the semantics of setting this.
 * <p>
 * This annotation can only be applied to fields that reference either an object or a RealmList with
 * model classes:
 * {@code
 * public class Person extends RealmObject {
 *
 *     public String name;
 *
 *     \@Relationship(RelationshipType.STRONG)
 *     public ContactInfo contactInfo;
 *
 *     \@Relationship(RelationshipType.WEAK)
 *     public RealmList<Dog> dogs = new RealmList<>();
 * }
 * }
 *
 * In all other cases the annotation processor will throw an error.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Relationship {

    /**
     * Set the type of the relationship. See {@link RelationshipType} for more information about the
     * semantics.
     */
    RelationshipType value() default RelationshipType.WEAK;
}