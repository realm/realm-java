package io.realm.internal;

import java.lang.ref.WeakReference;

public abstract class ObserverPair<T, S> {
    public final WeakReference<T> observerRef;
    public final S listener;

    public ObserverPair(T observer, S listener) {
        this.listener = listener;
        this.observerRef = new WeakReference<T>(observer);
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
