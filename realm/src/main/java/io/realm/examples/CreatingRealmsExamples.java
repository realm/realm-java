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

package io.realm.examples;

import android.content.Context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.examples.junkyard.AppModule;
import io.realm.examples.junkyard.DefaultRealmConfiguration;
import io.realm.examples.junkyard.LibraryModule;
import io.realm.examples.junkyard.MyDirectRealmMigration;
import io.realm.examples.junkyard.RealmConfiguration;
import io.realm.examples.junkyard.RealmLibraryModule;
import io.realm.examples.junkyard.RealmModule;

/**
 * Problem 1: Using Realm in both a lib and in the app doesn't work. Mostly due to ProGuard.
 * Problem 2: If both Lib and App uses Realm independently changes to either, should not trigger migrations in the other.
 * Problem 3: It is not possible to set the schema version of a Realm in an easy manner.
 * Problem 4: Getting a Realm reference take in some cases a lot of arguments. Constructor parameter explosion!
 * Problem 5: We are about to introduce additional constructor parameters. ReadOnly comes to mind.
 *
 *
 * The following is proposed to fix this (see below for examples):
 *
 * 1) Realm.getInstance() is replaced by two (possible 3) methods:
 * - Realm realm = Realm.getDefaultRealm()
 * - Realm realm = Realm.getNamedRealm(String name)
 *
 * (opt) Realm realm = Realm.getDefaultRealm(getContext())
 *
 * 2) Before opening any Realm it has to be configured in the Application class:
 *
 * - Realm.setDefaultRealm(getContext());
 * - Realm.setDefaultRealm(RealmConfiguration config);
 * - Realm.setNamedRealm(String name, RealmConfiguration config);
 *
 *
 * This is also inline with Cocoa which has the option to configure the default realm. Using a Builder pattern
 * also makes it a lot resilient to future API changes.
 */
public class CreatingRealmsExamples {

    public void appWithSingleRealm() {

        // The dummy case / for examples / making it seem easy
        // This would count as always having version + 1 in case of conflicts and try to migrate seamlessly.
        // and throw an error if that is not possible.
        // It would only make sense for very demo sort of apps, so not sure we should pollute the API with it.
        Realm.getDefaultRealm(getContext());

        // Otherwise easiest realworld setup would be

        // Step A)
        Realm.setDefaultRealm(new DefaultRealmConfiguration(1, getContext(), "otherstuff.realm"));

        // Step B) Any other place
        Realm.getDefaultRealm();
    }

    public void appWithTwoRealms() {

        // Step A)
        Realm.setDefaultRealm(new DefaultRealmConfiguration(1, getContext()));
        Realm.setNamedRealm("otherstuff", new DefaultRealmConfiguration(1, getContext(), "otherstuff.realm"));

        // Step B) Any other place
        Realm defaultRealm = Realm.getDefaultRealm();
        Realm otherStuffRelam = Realm.getNamedRealm("otherstuff");
    }

    public void customConfiguration() {

        // Using the builder pattern it is easy to construct a configuration that allow use to express the construction
        // of a realm instance very intuitively.
        // The below list is just examples to showcase the breadth of options

        // Step A)
        RealmConfiguration customConfig = new RealmConfiguration.Builder()
                .realmDir(getContext().getFilesDir())   // Path
                .realmName("custom-realm.realm")        // Realm name
                .version(1)                             // Spec version
                .encryptionKey(new byte[64])            // Encryption key
                .addClassModule(new LibraryModule())    // add additional module consists of a number of model classes. Make it easy to compose Realms
                .useClassModule(new AppModule())        // Replaces the standard "All"-module with something else
                .migrationCallback(new MyDirectRealmMigration()) // Migration code
                .deletetRealmIfMigrationNeeded(true)    // If a migration is needed. Realm is deleted instead and recreated empty
                .readOnly(true)                         // Read only mode
                .build();

        Realm.setDefaultRealm(customConfig);

        // Step B) Any other place
        Realm defaultRealm = Realm.getDefaultRealm();
    }


    public void modules() {
        /*
            Modules is a way to define "schemas" and makes it possible to modularize your
            schemas.

            1) The key design is this: library projects should always use custom schemas while app developers
            should be free to use the implicit "all" module

            2) Modules should be the only classes not being able to be proguarded, this ensures that only one class exists.
               and is our way of "binding" to the proxy classes.

            3) For each module a <Name>ReamModuleMediator is created. This module will contain mapping
               between all RealmObject classes and their static methods.

            4) If extending RealmLibraryModule instead of RealmModule, the "all" default schema will not
               be created. This means that only custom schemas are allowed. This is needed so the default schema
               doesn't get class conflicts.

            1) If not told otherwise Realm always create a DefaultRealmModule that contains all known classes
               We need a way to prevent library projects from creating this as

         */


    }

    private Context getContext() {
        return null; // Application class is a context
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
    OPTION 1
    Each module returns their list of valid classes. We generate a mapper class for each module but
    because the annotation processor cannot know which classes are returned, they both contain mappings
    to all classes, ie. you got two copies of the same code. It is up to the binding to ensure that only
    classes in getClasses() are valid.

    Library developers has to know to extend RealmLibraryModule instead of RealmModule

    Extending RealmLibraryModule disables the generation of the DefaultRealmModuleMapper.

    Advantages
        - Simple code

    Disadvantages
        - Duplicate code generation. Alle modules from the same lib will contain the same code
        - You have to know to extend RealmLibraryModule instead of RealmModule

    Suggested fix (below is the autogenerated code):
    ie. module classes just point to the same instance of an autogenerated proxymediator that contains
    all classes.

    public class MyLibraryModuleRealmModule implements RealmProxyBinder {
        public RealmProxyMediator getMediator() {
            return <UUUID>LibraryProxyMediator.getInstance();
        }
    }

    public class AnotherLibraryModuleRealmModule implements RealmProxyBinder {
        public RealmProxyMediator getMediator() {
            return <UUUID>LibraryProxyMediator.getInstance();
        }
    }

    public class <UUID>LibraryProxyMediator implements RealmProxyMediator {

        // Singleton
        public static RealmProxyMediator getInstance() {

        }

        ... Autogenerated code like from the ProGuard PR but only for library classes
    }


    // If no RealmLibraryModule is found, we do this instead

    public class DefaultAppRealmModule implements RealmProxyMediator {
        public RealmProxyMediator getMediator() {
            return DefaultRealmProxyMediator.getInstance();
        }
    }

    public class <UUID>LibraryProxyMediator implements RealmProxyMediator {

        // Singleton
        public static RealmProxyMediator getInstance() {

        }

        ... Autogenerated code like from the ProGuard PR
    }




    */
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class MyLibraryModule implements RealmLibraryModule {
        @Override
        public Set<Class<? extends RealmObject>> getClasses() {
            return new HashSet<Class<? extends RealmObject>>(Arrays.asList(
                    RealmObject.class
            ));
        }
    }

    public class AnotherLibraryModule implements RealmLibraryModule {
        @Override
        public Set<Class<? extends RealmObject>> getClasses() {
            return new HashSet<Class<? extends RealmObject>>(Arrays.asList(
                    RealmObject.class
            ));
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
    OPTION 2
    Each module is annotated with the relevant classes. We then create a Mapper class only containing those

    Advantages
        - Only necessary code is generated

    Disadvantages
        - No module type safety
        - Creating a no-op class just for the sake of having a annotation seems weird.
    */
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @RealmLibraryModule(classes = {
        RealmObject.class
    })
    public class MyLibraryModule {


    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
    OPTION 3
    Instead of annotating the Module, annotate the RealmObjects instead

    Advantages
        - All code is near the RealmObjects (mostly)

    Disadvantages
        - It is not easy to see what objects are in a module
        - Noop class has to be made

    */
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class FooModule implements RealmLibraryModule {

        // Any method here would make it seems more

    }

    @RealmModule(FooModule.class)
    public class MyObject extends RealmObject {

    }
}
