/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.internal;


import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * An ObserverPairList holds a list of ObserverPairs. An {@link ObserverPair} is pair containing an observer and a
 * listener. The observer is the object to react to the changes through the listener. The observer is saved as a weak
 * reference in the pair to control the life cycle of the listener. When the observer gets GCed, the corresponding pair
 * will be removed from the list. So DO NOT keep a strong reference to the observer in the subclass of listener since it
 * will cause leaks!
 * <p>
 * This class is not thread safe and it is not supposed to be.
 *
 * @param <T> the type of {@link ObserverPair}.
 */
public class ObserverPairList<T extends ObserverPairList.ObserverPair> {

    /**
     * @param <T> the type of observer.
     * @param <S> the type of listener.
     */
    public abstract static class ObserverPair<T, S> {
        final WeakReference<T> observerRef;
        protected final S listener;
        // Should only be set by the outer class. To marked it as removed in case it is removed in foreach callback.
        boolean removed = false;

        public ObserverPair(T observer, S listener) {
            this.listener = listener;
            this.observerRef = new WeakReference<T>(observer);
        }

        // The two pairs will be treated as the same only when the observers are the same and the listeners are equal.
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

        @Override
        public int hashCode() {
            T observer = observerRef.get();

            int result = 17;
            result = 31 * result + ((observer != null) ? observer.hashCode() : 0);
            //noinspection ConstantConditions
            result = 31 * result + ((listener != null) ? listener.hashCode() : 0);
            return result;
        }
    }

    /**
     * Callback passed to the {@link #foreach(Callback)} call.
     *
     * @param <T> type of ObserverPair.
     */
    public interface Callback<T extends ObserverPair> {
        void onCalled(T pair, Object observer);
    }

    private List<T> pairs = new CopyOnWriteArrayList<T>();
    // In case the clear() called during the foreach loop.
    private boolean cleared = false;

    /**
     * Iterate every valid pair in the list and call the callback on it. The pair with GCed observer will be removed and
     * callback won't be executed. Before executing the callback, a strong reference to the observer will be kept and
     * passed to the callback in case the observer gets GCed before callback returns.
     *
     * @param callback to be executed on the pair.
     */
    public void foreach(Callback<T> callback) {
        for (T pair : pairs) {
            if (cleared) {
                break;
            } else {
                Object observer = pair.observerRef.get();
                if (observer == null) {
                    pairs.remove(pair);
                } else if (!pair.removed) {
                    callback.onCalled(pair, observer);
                }
            }
        }
    }

    public boolean isEmpty() {
        return pairs.isEmpty();
    }

    public void clear() {
        cleared = true;
        pairs.clear();
    }

    public void add(T pair) {
        if (!pairs.contains(pair)) {
            pairs.add(pair);
            pair.removed = false;
        }
        if (cleared) {
            cleared = false;
        }
    }

    public <S, U> void remove(S observer, U listener) {
        for (T pair : pairs) {
            if (observer == pair.observerRef.get() && listener.equals(pair.listener)) {
                pair.removed = true;
                pairs.remove(pair);
                break;
            }
        }
    }

    void removeByObserver(Object observer) {
        for (T pair : pairs) {
            Object object = pair.observerRef.get();
            if (object == null || object == observer) {
                pair.removed = true;
                pairs.remove(pair);
            }
        }
    }

    public int size() {
        return pairs.size();
    }
}
