package io.realm.examples.appmodules.model;

import io.realm.RealmDictionary;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmSet;
import io.realm.examples.librarymodules.model.Cat;
import io.realm.examples.librarymodules.model.EmbeddedAnimal;

// Not used by the app, but merely acts as sanity checks that we can make cross module references,
// and generate valid code for it.
public class CrossModuleLinks extends RealmObject {
    public EmbeddedAnimal embeded;
    public Cat link;
    public RealmList<Cat> list;
    public RealmSet<Cat> set;
    public RealmDictionary<Cat> dictionary;
}
