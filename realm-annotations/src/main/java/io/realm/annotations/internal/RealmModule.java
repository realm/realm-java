/*
 * Copyright 2015 Realm Inc.
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
 * By default a Realm can stores all classes annotated with {@link io.realm.annotations.RealmClass} in a project.
 * However, if you want to restrict a Realm to contain only certain classes or want
 * to share them between a library project and an app project you specify that with
 * a RealmModule.
 * <p>
 * A RealmModule is a collection of annotated {@code RealmClass}'es that can be combined with other
 * RealmModules to create the object schema for a Realm. This makes it easier to
 * control versioning and migration of those Realms.
 * <p>
 * A RealmModule can either be a library module or an app module. This distinction
 * is made by setting {@code library = true}. Creating a library module will prevent
 * Realm from creating the default Realm module, which would otherwise conflict with
 * same class being created by app projects.
 * <p>
 * This means that library projects are <bold>required</bold> to use library modules
 * to allow the library to work seemlessly with app code. App developers can then
 * reuse the modules exposed by the library if they want to use {@code RealmClass}'es from
 * that library.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface RealmModule {

    /**
     * Setting this to {@code true} will mark this module as a library module. This will prevent Realm from generating the
     * default realm module containing all classes. This is required by libraries as not to interfere with Realms running
     * in app code, but also means that all libraries using Realm must explicitly use a module and cannot rely on the
     * default module being present.
     *
     * Creating library modules and normal modules in the same project is not allowed and will result in the annotation
     * processor throwing an error.
     */
    boolean library() default false;

    /**
     * Instead of adding all Realm classes manually to a module, set this boolean to {@code true} to automatically include all
     * Realm classes in this project. This does not include classes from other libraries which must be exposed using
     * their own module.
     *
     * Setting both {@code allClasses = true} and {@code classes()} will result in the annotation processor throwing
     * an error.
     */
    boolean allClasses() default false;

    /**
     * Specify the Realm classes part of this module. Only classes in this project can be included. Realm classes
     * from other libraries must be exposed using their own module.
     *
     * Setting both {@code allClasses = true} and {@code classes()} will result in the annotation processor throwing
     * an error.
     */
    Class<?>[] classes() default { };
}
