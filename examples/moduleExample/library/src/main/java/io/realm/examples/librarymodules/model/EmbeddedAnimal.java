package io.realm.examples.librarymodules.model;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

// Not used by the app, but merely acts as sanity checks that we can make cross module references,
// and generate valid code for it.
@RealmClass(embedded = true)
public class EmbeddedAnimal extends RealmObject {
    public String name;
}
