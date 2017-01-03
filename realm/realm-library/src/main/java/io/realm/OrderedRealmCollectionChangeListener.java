package io.realm;

/**
 * Interface for reporting changes to an ordered collection.
 */
public interface OrderedRealmCollectionChangeListener<T> {
    void onChange(T collection, OrderedCollectionChange changes);
}
