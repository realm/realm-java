package io.realm.internal.sync;

import io.realm.annotations.RealmModule;

// Workaround preventing `io.realm.DefaultRealmModuleMediator` being generated in the
// Realm JAR. Related to `https://github.com/realm/realm-java/issues/5799
@RealmModule(library = true, allClasses = true)
public class BaseModule {
}
