package com.realm.typed;

/**
 * Common operations of the fields of any type, that represent a column in the
 * generated XyzView and XyzTable classes for the Xyz entity.
 */
public interface TableOrViewColumn<Type> {

    Type[] getAll();

    void setAll(Type value);

}
