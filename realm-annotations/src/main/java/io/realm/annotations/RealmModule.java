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

package io.realm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By default a Realm can stores all classes extending RealmObject in a project. However, if you want to restrict a
 * Realm to only contain a subset of classes or want to share them between a library project and an app project you must
 * use a RealmModule.
 * <p>
 * A RealmModule is a collection of classes extending RealmObject that can be combined with other RealmModules to create
 * the schema for a Realm. This makes it easier to control versioning and migration of those Realms.
 * <p>
 * A RealmModule can either be a library module or an app module. The distinction is made by setting
 * {@code library = true}. Setting {@code library = true} is normally only relevant for library authors. See below for
 * futher details.
 *
 *
 * <h2>RealmModules and libraries</h2>
 *
 * Realms default behavior is to automatically create a RealmModule called {@code DefaultRealmModule} which contains all
 * classes extending RealmObject in a project. This module is automatically known by Realm.
 * <p>
 * This behavior is problematic when combining a library project and an app project that both uses Realm. This is
 * because the {@code DefaultRealmModule} will be created for both the library project and the app project, which will
 * cause the project to fail with duplicate class definition errors.
 * <p>
 * Library authors are responsible for avoiding this conflict by using explicit modules where {@code library = true} is
 * set. This disables the generation of the DefaultRealmModule for the library project and allows the library to be
 * included in the app project that also uses Realm. This means that library projects that uses Realm internally are
 * required to specify a specific module using {@code RealmConfiguration.setModules()}.
 * <p>
 * App developers are not required to specify any modules, as they implicitely use the {@code DefaultRealmModule}, but
 * they now has the option of adding the library project classes to their schema using
 * {@code RealmConfiguration.addModule()}.
 *
 * @see <a href="">TODO Example of a project using modules</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface RealmModule {

    /**
     * Setting this to true will mark this module as a library module. This will prevent Realm from generating the
     * {@code DefaultRealmModule} containing all classes. This is required by libraries so they do notintefer with
     * Realms running in app code, but it also means that all libraries using Realm must explicitly use a module and
     * cannot  rely on the default module being present.
     *
     * Creating library modules and normal modules in the same project is not allowed and will result in the annotation
     * processor throwing an exception.
     */
    boolean library() default false;

    /**
     * Instead of adding all Realm classes manually to a module, set this boolean to true to automatically include all
     * Realm classes in this project. This does not include classes from other libraries which must be exposed using
     * their own module.
     *
     * Setting both {@code allClasses = true} and {@code classes()} will result in the annotation processor throwing
     * an exception.
     */
    boolean allClasses() default false;

    /**
     * Specifies the classes extending RealmObject that should be part of this module. Only classes in this project can
     * be included. Classes from other libraries must be exposed using their own module.
     *
     * Setting both {@code allClasses = true} and {@code classes()} will result in the annotation processor throwing
     * an exception.
     */
    Class<?>[] classes() default {};
}
