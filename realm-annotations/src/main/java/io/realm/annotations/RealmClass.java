/*
 * Copyright 2014 Realm Inc.
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


import java.lang.annotation.Inherited;

/**
 * Annotation that marks a class as RealmObject. Proxy classes will be generated for these classes.
 * All subclasses that extend RealmObject will automatically have this annotation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.CLASS)
@Inherited
public @interface RealmClass {
    String name() default null; // Default is the simple name of the class. Use this to set a manual name.
    boolean export() default false; // Set to true to make the model available to other jars/aars.
}
