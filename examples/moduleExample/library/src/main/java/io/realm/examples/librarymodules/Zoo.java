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

package io.realm.examples.librarymodules;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.examples.librarymodules.model.Cat;
import io.realm.examples.librarymodules.modules.AllAnimalsModule;

/**
 * Library projects can also use Realms, but some configuration options are mandatory to avoid clashing with Realms used
 * in the app code.
 */
public class Zoo {

    private final RealmConfiguration realmConfig;
    private Realm realm;

    public Zoo(Context context) {
        realmConfig = new RealmConfiguration.Builder(context) // Beware this is the app context
                .name("library.zoo.realm")                    // So always use a unique name
                .setModules(new AllAnimalsModule())           // Always use explicit modules in library projects
                .build();

        // Reset Realm
        Realm.deleteRealm(realmConfig);
    }

    public void open() {
        // Don't use Realm.setDefaultInstance() in library projects. It is unsafe as app developers can override the
        // default configuration. So always use explicit configurations in library projects.
        realm = Realm.getInstance(realmConfig);
    }

    public long getNoOfAnimals() {
        return realm.where(Cat.class).count();
    }

    public void addAnimals(int count) {
        realm.beginTransaction();
        for (int i = 0; i < count; i++) {
            Cat cat = realm.createObject(Cat.class);
            cat.setName("Cat " + i);
        }
        realm.commitTransaction();
    }

    public void close() {
        realm.close();
    }
}
