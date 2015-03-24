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

package io.realm.annotations.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * As a default a Realm can store all RealmObjects in a project. However, if you want to restrict a Realm to contain only
 * certain RealmObjects or want to share RealmObjects between a library project and a app project you will have to do so
 * using a Realm module.
 *
 * A Realm module is a collection of RealmObjects that can be combined with other modules to create the object schema
 * for an Realm. This makes it easier to control versioning and migration of those Realms.
 *
 * A Realm module can either be a library module or an app module. This distinction is made by setting
 * {@code library = true}. Creating a library module will prevent Realm from creating the default Realm module, which
 * would otherwise conflict with same class being created by app projects.
 *
 * This means that library projects are <bold>required</bold> to use library modules to allow the library to work
 * seemlessly with app code. App developers can then reuse the modules exposed by the library if they want to use RealmObjects
 * from that library.
 *
 * TODO Add example once RealmConfiguration gets merged.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface RealmModule {

    /**
     * Setting this to true will mark this module as a library module. This will prevent Realm from generating the
     * default realm module containing all classes. This is required by libraries as not to intefer with Realms running
     * in app code, but also means that all libraries using Realm must explicitly use a module and cannot rely on the
     * default module being present.
     *
     * Creating library modules and normal modules in the same project is not allowed and will result in the annotation
     * processor throwing an error.
     */
    boolean library() default false;

    /**
     * Instead of adding all RealmObjects manually to a module, set this boolean to true to automatically
     * include all RealmObject classes in this project. This does not include classes from other libraries
     * which must be exposed using their own module.
     *
     * Setting both {@code allClasses = true} and {@code classes()} will result in the annotation processor throwing
     * an error.
     */
    boolean allClasses() default false;

    /**
     * Specify the RealmObject classes part of this module. Only classes in this project can be included. RealmObjects
     * from other libraries must be exposed using their own module.
     *
     * Setting both {@code allClasses = true} and {@code classes()} will result in the annotation processor throwing
     * an error.
     */
    Class<?>[] classes() default { };
}
