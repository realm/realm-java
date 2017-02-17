package io.realm;

public interface Observable<T> {
    void addChangeListener(RealmChangeListener<T> listener);
    void removeChangeListener(RealmChangeListener<T> listener);
    void removeAllChangeListeners();
}
