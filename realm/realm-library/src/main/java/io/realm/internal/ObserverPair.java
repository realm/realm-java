package io.realm.internal;

import java.lang.ref.WeakReference;

public abstract class ObserverPair<T> {
    public final T listener;
    public final WeakReference<Object> observerRef;

    public ObserverPair(T listener, Object objectRef) {
        this.listener = listener;
        this.observerRef = new WeakReference<Object>(objectRef);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ObserverPair) {
            ObserverPair anotherPair = (ObserverPair) obj;
            return listener.equals(anotherPair.listener) &&
                    observerRef.get() == anotherPair.observerRef.get();
        }
        return false;
    }
}
