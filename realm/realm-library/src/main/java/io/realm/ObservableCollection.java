package io.realm;

/**
 * Interface for collections that are able to tell interested parties how they
 * changed when they where updated.
 */
public interface ObservableCollection<T, S> extends Observable<T> {
    void addChangeListener(S listener);
    void removeChangeListener(S listener);
}
