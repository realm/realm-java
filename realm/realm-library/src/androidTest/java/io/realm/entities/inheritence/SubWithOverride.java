package io.realm.entities.inheritence;

// Subclass which override a base class field
public class SubWithOverride extends ObjectBase {
    // Not marked with primary key which base field is. Field should respect this.
    public long id;
}
