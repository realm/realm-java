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
 * By default a Realm can stores all RealmClasses in a project. However, if you want to restrict a Realm to contain only
 * certain classes or want to share them between a library project and an app project you must use a RealmModule.
 * <p>
 * A RealmModule is a collection of RealmClasses that can be combined with other RealmModules to create the schema for a
 * Realm. This makes it easier to control versioning and migration of those Realms.
 * <p>
 * A RealmModule can either be a library module or an app module. This distinction is made by setting
 * {@code library = true}. As default Realm will automatically create a RealmModule called {@code DefaulRealmModule}
 * that contains all RealmClasses in a project. This module will be an app module and is automatically known by Realm.
 * <p>
 * This behavior is problematic when combining a library project and an app project that both uses Realm as
 * the {@code DefaultRealmModule} will be created for both the library project and the app project causing import
 * conflicts.
 * <p>
 * Library authors are reponsible for avoiding this conflict by creating a explicit library module where
 * {@code library = true} is set. This disable the generation of the DefaultRealmModule for the library project and
 * allows the library to be included in the app project that also uses Realm. This means that library projects that uses
 * Realm internally are required to specify a explicit module using {@code RealmConfiguration.setModule()}.
 * <p>
 * App developers are not required to specify any modules, as they implicitely use the {@code DefaultRealmModule}, but
 * they now has the option of adding the library project classes to their schema using
 * {@code RealmConfiguration.addModule()}.
 *
 * TODO Reference the example project for library modules
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
public @interface RealmModule {

    /**
     * Setting this to true will mark this module as a library module. This will prevent Realm from generating the
     * {@code DefaultRealmModule} containing all classes. This is required by libraries as not to intefer with Realms
     * running in app code, but also means that all libraries using Realm must explicitly use a module and cannot rely
     * on the default module being present.
     *
     * Creating library modules and normal modules in the same project is not allowed and will result in the annotation
     * processor throwing an error.
     */
    boolean library() default false;

    /**
     * Instead of adding all Realm classes manually to a module, set this boolean to true to automatically include all
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
