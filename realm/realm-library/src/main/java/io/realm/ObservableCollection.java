package io.realm;

/**
 * Interface for collections that are able to tell interested parties how they
 * changed when they where updated.
 */
public interface ObservableCollection<T, L> extends Observable<T> {
    void addChangeListener(L listener);
    void removeChangeListener(L listener);
}
