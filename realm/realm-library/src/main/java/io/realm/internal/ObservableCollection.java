package io.realm.internal;

import javax.annotation.Nullable;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmChangeListener;

// Helper class for supporting add change listeners on OsResults & OsList.
@Keep
interface ObservableCollection {
    class CollectionObserverPair<T> extends ObserverPairList.ObserverPair<T, Object> {
        public CollectionObserverPair(T observer, Object listener) {
            super(observer, listener);
        }

        public void onChange(T observer, OsCollectionChangeSet changes) {
            if (listener instanceof OrderedRealmCollectionChangeListener) {
                //noinspection unchecked
                ((OrderedRealmCollectionChangeListener<T>) listener).onChange(observer, new StatefulCollectionChangeSet(changes));
            } else if (listener instanceof RealmChangeListener) {
                //noinspection unchecked
                ((RealmChangeListener<T>) listener).onChange(observer);
            } else {
                throw new RuntimeException("Unsupported listener type: " + listener);
            }
        }
    }

    class RealmChangeListenerWrapper<T> implements OrderedRealmCollectionChangeListener<T> {
        private final RealmChangeListener<T> listener;

        RealmChangeListenerWrapper(RealmChangeListener<T> listener) {
            this.listener = listener;
        }

        @Override
        public void onChange(T collection, @Nullable OrderedCollectionChangeSet changes) {
            listener.onChange(collection);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof RealmChangeListenerWrapper &&
                    listener == ((RealmChangeListenerWrapper) obj).listener;
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }

    class Callback implements ObserverPairList.Callback<CollectionObserverPair> {
        private final OsCollectionChangeSet changeSet;

        Callback(OsCollectionChangeSet changeSet) {
            this.changeSet = changeSet;
        }

        @Override
        public void onCalled(CollectionObserverPair pair, Object observer) {
            //noinspection unchecked
            pair.onChange(observer, changeSet);
        }
    }

    // Called by JNI
    @SuppressWarnings("SameParameterValue")
    void notifyChangeListeners(long nativeChangeSetPtr);
}
