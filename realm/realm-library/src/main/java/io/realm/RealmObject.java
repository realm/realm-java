/*
 * Copyright 2014 Realm Inc.
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

package io.realm;

import java.util.List;

import io.realm.annotations.RealmClass;
import io.realm.internal.InvalidRow;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import rx.Observable;

/**
 * In Realm you define your RealmObject classes by sub-classing RealmObject and adding fields to be persisted. You then 
 * create your objects within a Realm, and use your custom subclasses instead of using the RealmObject class directly.
 * <p>
 * An annotation processor will create a proxy class for your RealmObject subclass.
 * <p>
 * The following field data types are supported:
 * <ul>
 *   <li>boolean/Boolean</li>
 *   <li>short/Short</li>
 *   <li>int/Integer</li>
 *   <li>long/Long</li>
 *   <li>float/Float</li>
 *   <li>double/Double</li>
 *   <li>byte[]</li>
 *   <li>String</li>
 *   <li>Date</li>
 *   <li>Any RealmObject subclass</li>
 *   <li>RealmList</li>
 * </ul>
 * <p>
 * The types <code>short</code>, <code>int</code>, and <code>long</code> are mapped to <code>long</code> when storing
 * within a Realm.
 * <p>
 * The only restriction a RealmObject has is that fields are not allowed to be final, transient' or volatile.
 * Any method as well as public fields are allowed.
 * <p>
 * Fields annotated with {@link io.realm.annotations.Ignore} don't have these restrictions and don't require either a
 * getter or setter.
 * <p>
 * Realm will create indexes for fields annotated with {@link io.realm.annotations.Index}. This will speedup queries but
 * will have a negative impact on inserts and updates.
 * <p>
 * A RealmObject cannot be passed between different threads.
 *
 * @see Realm#createObject(Class)
 * @see Realm#copyToRealm(RealmModel)
 */

@RealmClass
public abstract class RealmObject implements RealmModel {
    /**
     * DEPRECATED: Use {@link #deleteFromRealm()} instead.
     *
     * Removes the object from the Realm it is currently associated to.
     * <p>
     * After this method is called the object will be invalid and any operation (read or write) performed on it will
     * fail with an IllegalStateException
     *
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     */
    @Deprecated
    public final void removeFromRealm() {
        deleteFromRealm();
    }

    /**
     * Deletes the object from the Realm it is currently associated to.
     * <p>
     * After this method is called the object will be invalid and any operation (read or write) performed on it will
     * fail with an IllegalStateException.
     *
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     * @see #isValid()
     */
    public final void deleteFromRealm() {
        deleteFromRealm(this);
    }
    
    /**
     * Deletes the object from the Realm it is currently associated with.
     * <p>
     * After this method is called the object will be invalid and any operation (read or write) performed on it will
     * fail with an IllegalStateException.
     *
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     * @see #isValid()
     */
    public static <E extends RealmModel> void deleteFromRealm(E object) {
        if (!(object instanceof RealmObjectProxy)) {
            // TODO What type of exception IllegalArgument/IllegalState?
            throw new IllegalArgumentException("Object not managed by Realm, so it cannot be removed.");
        }

        RealmObjectProxy proxy = (RealmObjectProxy) object;
        if (proxy.realmGet$proxyState().getRow$realm() == null) {
            throw new IllegalStateException("Object malformed: missing object in Realm. Make sure to instantiate RealmObjects with Realm.createObject()");
        }
        if (proxy.realmGet$proxyState().getRealm$realm() == null) {
            throw new IllegalStateException("Object malformed: missing Realm. Make sure to instantiate RealmObjects with Realm.createObject()");
        }

        proxy.realmGet$proxyState().getRealm$realm().checkIfValid();
        Row row = proxy.realmGet$proxyState().getRow$realm();
        row.getTable().moveLastOver(row.getIndex());
        proxy.realmGet$proxyState().setRow$realm(InvalidRow.INSTANCE);
    }


    /**
     * Checks if the RealmObject is still valid to use i.e. the RealmObject hasn't been deleted nor has the
     * {@link io.realm.Realm} been closed. It will always return false for stand alone objects.
     *
     * @return {@code true} if the object is still accessible, {@code false} otherwise or if it is a standalone object.
     */
    public final boolean isValid() {
        return RealmObject.isValid(this);
    }

    /**
     * Checks if the RealmObject is still valid to use i.e. the RealmObject hasn't been deleted nor has the
     * {@link io.realm.Realm} been closed. It will always return false for stand alone objects.
     *
     * @param object RealmObject to check validity for.
     * @return {@code true} if the object is still accessible, {@code false} otherwise or if it is a standalone object.
     */
    public static <E extends RealmModel> boolean isValid(E object) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            Row row = proxy.realmGet$proxyState().getRow$realm();
            return row != null && row.isAttached();
        } else {
            return false;
        }
    }

    /**
     * Determines if the current RealmObject is obtained synchronously or asynchronously (from a worker thread).
     * Synchronous RealmObjects are by definition blocking hence this method will always return {@code true} for them.
     * This will return {@code true} if called for a standalone object (created outside of Realm).
     *
     * @return {@code true} if the query has completed and the data is available {@code false} if the query is in
     * progress.
     */
    public final boolean isLoaded() {
        return RealmObject.isLoaded(this);
    }

    /**
     * Determines if the RealmObject is obtained synchronously or asynchronously (from a worker thread).
     * Synchronous RealmObjects are by definition blocking hence this method will always return {@code true} for them.
     * This will return {@code true} if called for a standalone object (created outside of Realm).
     *
     * @param object RealmObject to check.
     * @return {@code true} if the query has completed and the data is available {@code false} if the query is in
     * progress.
     */
    public static <E extends RealmModel> boolean isLoaded(E object) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            proxy.realmGet$proxyState().getRealm$realm().checkIfValid();
            return proxy.realmGet$proxyState().getPendingQuery$realm() == null || proxy.realmGet$proxyState().isCompleted$realm();
        } else {
            return true;
        }
    }

    /**
     * Makes an asynchronous query blocking. This will also trigger any registered listeners.
     * Note: This will return {@code true} if called for a standalone object (created outside of Realm).
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    public final boolean load() {
        return RealmObject.load(this);
    }

    /**
     * Makes an asynchronous query blocking. This will also trigger any registered listeners.
     * Note: This will return {@code true} if called for a standalone object (created outside of Realm).
     *
     * @param object RealmObject to force load.
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    public static <E extends RealmModel> boolean load(E object) {
        if (RealmObject.isLoaded(object)) {
            return true;
        } else {
            if (object instanceof RealmObjectProxy) {
                // doesn't guarantee to import correctly the result (because the user may have advanced)
                // in this case the Realm#handler will be responsible of retrying
                return ((RealmObjectProxy) object).realmGet$proxyState().onCompleted$realm();
            } else {
                return false;
            }
        }
    }

    /**
     * Adds a change listener to this RealmObject.
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if object is an un-managed RealmObject.
     */
    public final <E extends RealmModel> void addChangeListener(RealmChangeListener<E> listener) {
        RealmObject.addChangeListener((E) this, listener);
    }

    /**
     * Adds a change listener to a RealmObject.
     *
     * @param object RealmObject to add listener to.
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if object is an un-managed RealmObject.
     */
    public static <E extends RealmModel> void addChangeListener(E object, RealmChangeListener<E> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            BaseRealm realm = proxy.realmGet$proxyState().getRealm$realm();
            realm.checkIfValid();
            if (realm.handler == null) {
                throw new IllegalStateException("You can't register a listener from a non-Looper thread ");
            }
            List<RealmChangeListener> listeners = proxy.realmGet$proxyState().getListeners$realm();
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        } else {
            throw new IllegalArgumentException("Cannot add listener from this unmanaged RealmObject (created outside of Realm)");
        }
    }


    /**
     * Removes a previously registered listener.
     *
     * @param listener the instance to be removed.
     */
    public final void removeChangeListener(RealmChangeListener listener) {
        RealmObject.removeChangeListener(this, listener);
    }

    /**
     * Removes a previously registered listener on the given RealmObject.
     *
     * @param object RealmObject to remove listener from.
     * @param listener the instance to be removed.
     */
    public static <E extends RealmModel> void removeChangeListener(E object, RealmChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            proxy.realmGet$proxyState().getRealm$realm().checkIfValid();
            proxy.realmGet$proxyState().getListeners$realm().remove(listener);
        } else {
            throw new IllegalArgumentException("Cannot remove listener from this unmanaged RealmObject (created outside of Realm)");
        }
    }

    /**
     * Removes all registered listeners.
     */
    public final void removeChangeListeners() {
        RealmObject.removeChangeListeners(this);
    }

    /**
     * Removes all registered listeners from the given RealmObject.
     *
     * @param object RealmObject to remove all listeners from.
     * @throws IllegalArgumentException if object is {@code null} or isn't managed by Realm.
     */
    public static <E extends RealmModel> void removeChangeListeners(E object) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            proxy.realmGet$proxyState().getRealm$realm().checkIfValid();
            proxy.realmGet$proxyState().getListeners$realm().clear();
        } else {
            throw new IllegalArgumentException("Cannot remove listeners from this un-managed RealmObject (created outside of Realm)");
        }
    }

    /**
     * Returns an RxJava Observable that monitors changes to this RealmObject. It will emit the current object when
     * subscribed to. Object updates will continually be emitted as the RealmObject is updated -
     * {@code onComplete} will never be called.
     *
     * If chaining a RealmObject observable use {@code obj.<MyRealmObjectClass>asObservable()} to pass on
     * type information, otherwise the type of the following observables will be {@code RealmObject}.
     *
     * If you would like the {@code asObservable()} to stop emitting items you can instruct RxJava to
     * only emit only the first item by using the {@code first()} operator:
     *
     * <pre>
     * {@code
     * obj.asObservable()
     *      .filter(obj -> obj.isLoaded())
     *      .first()
     *      .subscribe( ... ) // You only get the object once
     * }
     * </pre>
     *
     * @param <E> RealmObject class that is being observed. Must be this class or its super types.
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath or the
     * corresponding Realm instance doesn't support RxJava.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    public final <E extends RealmObject> Observable<E> asObservable() {
        return (Observable<E>) RealmObject.asObservable(this);
    }

    /**
     * Returns an RxJava Observable that monitors changes to this RealmObject. It will emit the current object when
     * subscribed to. Object updates will continuously be emitted as the RealmObject is updated -
     * {@code onComplete} will never be called.
     *
     * If chaining a RealmObject observable use {@code obj.<MyRealmObjectClass>asObservable()} to pass on
     * type information, otherwise the type of the following observables will be {@code RealmObject}.
     *
     * If you would like the {@code asObservable()} to stop emitting items you can instruct RxJava to
     * emit only the first item by using the {@code first()} operator:
     *
     * <pre>
     * {@code
     * obj.asObservable()
     *      .filter(obj -> obj.isLoaded())
     *      .first()
     *      .subscribe( ... ) // You only get the object once
     * }
     * </pre>
     *
     * @param object RealmObject class that is being observed. Must be this class or its super types.
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    public static <E extends RealmModel> Observable<E> asObservable(E object) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            BaseRealm realm = proxy.realmGet$proxyState().getRealm$realm();
            if (realm instanceof Realm) {
                return realm.configuration.getRxFactory().from((Realm) realm, object);
            } else if (realm instanceof DynamicRealm) {
                DynamicRealm dynamicRealm = (DynamicRealm) realm;
                DynamicRealmObject dynamicObject = (DynamicRealmObject) object;
                @SuppressWarnings("unchecked")
                Observable<E> observable = (Observable<E>) realm.configuration.getRxFactory().from(dynamicRealm, dynamicObject);
                return observable;
            } else {
                throw new UnsupportedOperationException(realm.getClass() + " does not support RxJava." +
                        " See https://realm.io/docs/java/latest/#rxjava for more details.");
            }
        } else {
            // TODO Is this true? Should we just return Observable.just(object) ?
            throw new IllegalArgumentException("Cannot create Observables from un-managed RealmObjects");
        }
    }
}
