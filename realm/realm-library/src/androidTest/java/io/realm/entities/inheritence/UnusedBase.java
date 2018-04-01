package io.realm.entities.inheritence;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

// Unused base classes will just be ignored by any code generated, but still needs to be valid
public abstract class UnusedBase extends RealmObject {
    @PrimaryKey
    public long id;
}
