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

import android.app.IntentService;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.realm.annotations.RealmClass;
import io.realm.internal.InvalidRow;
import io.realm.internal.ManagableObject;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.log.RealmLog;
import io.realm.rx.ObjectChange;

/**
 * In Realm you define your RealmObject classes by sub-classing RealmObject and adding fields to be persisted. You then
 * create your objects within a Realm, and use your custom subclasses instead of using the RealmObject class directly.
 * <p>
 * An annotation processor will create a proxy class for your RealmObject subclass.
 * <p>
 * The following field data types are supported:
 * <ul>
 * <li>boolean/Boolean</li>
 * <li>short/Short</li>
 * <li>int/Integer</li>
 * <li>long/Long</li>
 * <li>float/Float</li>
 * <li>double/Double</li>
 * <li>byte[]</li>
 * <li>String</li>
 * <li>Date</li>
 * <li>Any RealmObject subclass</li>
 * <li>RealmList</li>
 * </ul>
 * <p>
 * The types <code>short</code>, <code>int</code>, and <code>long</code> are mapped to <code>long</code> when storing
 * within a Realm.
 * <p>
 * The only restriction a RealmObject has is that fields are not allowed to be final or volatile.
 * Any method as well as public fields are allowed. When providing custom constructors, a public constructor with
 * no arguments must be declared.
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
public abstract class RealmObject implements RealmModel, ManagableObject {
    static final String MSG_NULL_OBJECT = "'model' is null.";
    static final String MSG_DELETED_OBJECT = "the object is already deleted.";
    static final String MSG_DYNAMIC_OBJECT = "the object is an instance of DynamicRealmObject. Use DynamicRealmObject.getDynamicRealm() instead.";

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
     * Checks if the RealmObject is still valid to use i.e., the RealmObject hasn't been deleted nor has the
     * {@link io.realm.Realm} been closed. It will always return {@code true} for unmanaged objects.
     * <p>
     * Note that this can be used to check the validity of certain conditions such as being {@code null}
     * when observed.
     * <pre>
     * {@code
     * realm.where(BannerRealm.class).equalTo("type", type).findFirstAsync().asFlowable()
     *      .filter(result.isLoaded() && result.isValid())
     *      .first()
     * }
     * </pre>
     *
     * @return {@code true} if the object is still accessible or an unmanaged object, {@code false} otherwise.
     * @see <a href="https://github.com/realm/realm-java/tree/master/examples/rxJavaExample">Examples using Realm with RxJava</a>
     */
    @Override
    public final boolean isValid() {
        return RealmObject.isValid(this);
    }

    /**
     * Checks if the RealmObject is still valid to use i.e., the RealmObject hasn't been deleted nor has the
     * {@link io.realm.Realm} been closed. It will always return {@code true} for unmanaged objects.
     *
     * @param object RealmObject to check validity for.
     * @return {@code true} if the object is still accessible or an unmanaged object, {@code false} otherwise.
     */
    public static <E extends RealmModel> boolean isValid(E object) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            Row row = proxy.realmGet$proxyState().getRow$realm();
            return row != null && row.isAttached();
        } else {
            //noinspection ConstantConditions
            return object != null;
        }
    }

    /**
     * Checks if the query used to find this RealmObject has completed.
     * <p>
     * Async methods like {@link RealmQuery#findFirstAsync()} return an {@link RealmObject} that represents the future
     * result of the {@link RealmQuery}. It can be considered similar to a {@link java.util.concurrent.Future} in this
     * regard.
     * <p>
     * Once {@code isLoaded()} returns {@code true}, the object represents the query result even if the query
     * didn't find any object matching the query parameters. In this case the {@link RealmObject} will
     * become a "null" object.
     * <p>
     * "Null" objects represents {@code null}.  An exception is throw if any accessor is called, so it is important to
     * also check {@link #isValid()} before calling any methods. A common pattern is:
     * <p>
     * <pre>
     * {@code
     * Person person = realm.where(Person.class).findFirstAsync();
     * person.isLoaded(); // == false
     * person.addChangeListener(new RealmChangeListener() {
     *      \@Override
     *      public void onChange(Person person) {
     *          person.isLoaded(); // Always true here
     *          if (person.isValid()) {
     *              // It is safe to access the person.
     *          }
     *      }
     * });
     * }
     * </pre>
     * <p>
     * Synchronous RealmObjects are by definition blocking hence this method will always return {@code true} for them.
     * This method will return {@code true} if called on an unmanaged object (created outside of Realm).
     *
     * @return {@code true} if the query has completed, {@code false} if the query is in
     * progress.
     * @see #isValid()
     */
    public final boolean isLoaded() {
        return RealmObject.isLoaded(this);
    }


    /**
     * Checks if the query used to find this RealmObject has completed.
     * <p>
     * Async methods like {@link RealmQuery#findFirstAsync()} return an {@link RealmObject} that represents the future result
     * of the {@link RealmQuery}. It can be considered similar to a {@link java.util.concurrent.Future} in this regard.
     * <p>
     * Once {@code isLoaded()} returns {@code true}, the object represents the query result even if the query
     * didn't find any object matching the query parameters. In this case the {@link RealmObject} will
     * become a "null" object.
     * <p>
     * "Null" objects represents {@code null}.  An exception is throw if any accessor is called, so it is important to also
     * check {@link #isValid()} before calling any methods. A common pattern is:
     * <p>
     * <pre>
     * {@code
     * Person person = realm.where(Person.class).findFirstAsync();
     * RealmObject.isLoaded(person); // == false
     * RealmObject.addChangeListener(person, new RealmChangeListener() {
     *      \@Override
     *      public void onChange(Person person) {
     *          RealmObject.isLoaded(person); // always true here
     *          if (RealmObject.isValid(person)) {
     *              // It is safe to access the person.
     *          }
     *      }
     * });
     * }
     * </pre>
     * <p>
     * Synchronous RealmObjects are by definition blocking hence this method will always return {@code true} for them.
     * This method will return {@code true} if called on an unmanaged object (created outside of Realm).
     *
     * @param object RealmObject to check.
     * @return {@code true} if the query has completed, {@code false} if the query is in
     * progress.
     * @see #isValid(RealmModel)
     */
    public static <E extends RealmModel> boolean isLoaded(E object) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            proxy.realmGet$proxyState().getRealm$realm().checkIfValid();
            return proxy.realmGet$proxyState().isLoaded();
        }
        return true;
    }

    /**
     * Checks if this object is managed by Realm. A managed object is just a wrapper around the data in the underlying
     * Realm file. On Looper threads, a managed object will be live-updated so it always points to the latest data. It
     * is possible to register a change listener using {@link #addChangeListener(RealmChangeListener)} to be notified
     * when changes happen. Managed objects are thread confined so that they cannot be accessed from other threads than
     * the one that created them.
     * <p>
     * <p>
     * If this method returns {@code false}, the object is unmanaged. An unmanaged object is just a normal Java object,
     * so it can be parsed freely across threads, but the data in the object is not connected to the underlying Realm,
     * so it will not be live updated.
     * <p>
     * <p>
     * It is possible to create a managed object from an unmanaged object by using
     * {@link Realm#copyToRealm(RealmModel)}. An unmanaged object can be created from a managed object by using
     * {@link Realm#copyFromRealm(RealmModel)}.
     *
     * @return {@code true} if the object is managed, {@code false} if it is unmanaged.
     */
    @Override
    public boolean isManaged() {
        return RealmObject.isManaged(this);
    }

    /**
     * Checks if this object is managed by Realm. A managed object is just a wrapper around the data in the underlying
     * Realm file. On Looper threads, a managed object will be live-updated so it always points to the latest data. It
     * is possible to register a change listener using {@link #addChangeListener(RealmModel, RealmChangeListener)} to be
     * notified when changes happen. Managed objects are thread confined so that they cannot be accessed from other threads
     * than the one that created them.
     * <p>
     * <p>
     * If this method returns {@code false}, the object is unmanaged. An unmanaged object is just a normal Java object,
     * so it can be parsed freely across threads, but the data in the object is not connected to the underlying Realm,
     * so it will not be live updated.
     * <p>
     * <p>
     * It is possible to create a managed object from an unmanaged object by using
     * {@link Realm#copyToRealm(RealmModel)}. An unmanaged object can be created from a managed object by using
     * {@link Realm#copyFromRealm(RealmModel)}.
     *
     * @return {@code true} if the object is managed, {@code false} if it is unmanaged.
     */
    public static <E extends RealmModel> boolean isManaged(E object) {
        return object instanceof RealmObjectProxy;
    }

    /**
     * Returns {@link Realm} instance where this {@link RealmObject} belongs.
     * <p>
     * You <b>must not</b> call {@link Realm#close()} against returned instance.
     *
     * @return {@link Realm} instance where this object belongs to or {@code null} if this object is unmanaged.
     * @throws IllegalStateException if this object is an instance of {@link DynamicRealmObject}
     * or this object was already deleted or the corresponding {@link Realm} was already closed.
     */
    public Realm getRealm() {
        return getRealm(this);
    }

    /**
     * returns {@link Realm} instance where the {@code model} belongs.
     * <p>
     * You <b>must not</b> call {@link Realm#close()} against returned instance.
     *
     * @param model an {@link RealmModel} instance other than {@link DynamicRealmObject}.
     * @return {@link Realm} instance where the {@code model} belongs or {@code null} if the {@code model} is unmanaged.
     * @throws IllegalArgumentException if the {@code model} is {@code null}.
     * @throws IllegalStateException if the {@code model}  is an instance of {@link DynamicRealmObject}
     * or this object was already deleted or the corresponding {@link Realm} was already closed.
     */
    public static Realm getRealm(RealmModel model) {
        if (model == null) {
            throw new IllegalArgumentException(MSG_NULL_OBJECT);
        }
        if (model instanceof DynamicRealmObject) {
            throw new IllegalStateException(MSG_DYNAMIC_OBJECT);
        }
        if (!(model instanceof RealmObjectProxy)) {
            return null;
        }
        final BaseRealm realm = ((RealmObjectProxy) model).realmGet$proxyState().getRealm$realm();
        realm.checkIfValid();
        if (!RealmObject.isValid(model)) {
            throw new IllegalStateException(MSG_DELETED_OBJECT);
        }

        return (Realm) realm;
    }

    /**
     * Makes an asynchronous query blocking. This will also trigger any registered listeners.
     * <p>
     * Note: This will return {@code true} if called for an unmanaged object (created outside of Realm).
     *
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    public final boolean load() {
        return RealmObject.load(this);
    }

    /**
     * Makes an asynchronous query blocking. This will also trigger any registered listeners.
     * <p>
     * Note: This will return {@code true} if called for an unmanaged object (created outside of Realm).
     *
     * @param object RealmObject to force load.
     * @return {@code true} if it successfully completed the query, {@code false} otherwise.
     */
    public static <E extends RealmModel> boolean load(E object) {
        if (RealmObject.isLoaded(object)) {
            return true;
        } else if (object instanceof RealmObjectProxy) {
            ((RealmObjectProxy) object).realmGet$proxyState().load();
            return true;
        }
        return false;
    }

    /**
     * Adds a change listener to this RealmObject to get detailed information about changes. The listener will be
     * triggered if any value field or referenced RealmObject field is changed, or the RealmList field itself is
     * changed.
     * <p>
     * Registering a change listener will not prevent the underlying RealmObject from being garbage collected.
     * If the RealmObject is garbage collected, the change listener will stop being triggered. To avoid this, keep a
     * strong reference for as long as appropriate e.g. in a class variable.
     * <p>
     * <pre>
     * {@code
     * public class MyActivity extends Activity {
     *
     *     private Person person; // Strong reference to keep listeners alive
     *
     *     \@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *       super.onCreate(savedInstanceState);
     *       person = realm.where(Person.class).findFirst();
     *       person.addChangeListener(new RealmObjectChangeListener<Person>() {
     *           \@Override
     *           public void onChange(Person person, ObjectChangeSet changeSet) {
     *               // React to change
     *           }
     *       });
     *     }
     * }
     * }
     * </pre>
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null} or the object is an unmanaged object.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or {@link IntentService} thread.
     * @throws IllegalStateException if you try to add a listener inside a transaction.
     */
    public final <E extends RealmModel> void addChangeListener(RealmObjectChangeListener<E> listener) {
        //noinspection unchecked
        RealmObject.addChangeListener((E) this, listener);
    }

    /**
     * Adds a change listener to this RealmObject that will be triggered if any value field or referenced RealmObject
     * field is changed, or the RealmList field itself is changed.
     * <p>
     * Registering a change listener will not prevent the underlying RealmObject from being garbage collected.
     * If the RealmObject is garbage collected, the change listener will stop being triggered. To avoid this, keep a
     * strong reference for as long as appropriate e.g. in a class variable.
     * <p>
     * <pre>
     * {@code
     * public class MyActivity extends Activity {
     *
     *     private Person person; // Strong reference to keep listeners alive
     *
     *     \@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *       super.onCreate(savedInstanceState);
     *       person = realm.where(Person.class).findFirst();
     *       person.addChangeListener(new RealmChangeListener<Person>() {
     *           \@Override
     *           public void onChange(Person person) {
     *               // React to change
     *           }
     *       });
     *     }
     * }
     * }
     * </pre>
     *
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null} or the object is an unmanaged object.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or {@link IntentService} thread.
     * @throws IllegalStateException if you try to add a listener inside a transaction.
     */
    public final <E extends RealmModel> void addChangeListener(RealmChangeListener<E> listener) {
        //noinspection unchecked
        RealmObject.addChangeListener((E) this, listener);
    }

    /**
     * Adds a change listener to a RealmObject to get detailed information about the changes. The listener will be
     * triggered if any value field or referenced RealmObject field is changed, or the RealmList field itself is
     * changed.
     * <p>
     * Registering a change listener will not prevent the underlying RealmObject from being garbage collected.
     * If the RealmObject is garbage collected, the change listener will stop being triggered. To avoid this, keep a
     * strong reference for as long as appropriate e.g. in a class variable.
     * <p>
     * <pre>
     * {@code
     * public class MyActivity extends Activity {
     *
     *     private Person person; // Strong reference to keep listeners alive
     *
     *     \@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *       super.onCreate(savedInstanceState);
     *       person = realm.where(Person.class).findFirst();
     *       person.addChangeListener(new RealmObjectChangeListener<Person>() {
     *           \@Override
     *           public void onChange(Person person, ObjectChangeSet changeSet) {
     *               // React to change
     *           }
     *       });
     *     }
     * }
     * }
     * </pre>
     *
     *
     * @param object RealmObject to add listener to.
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the {@code object} is {@code null} or an unmanaged object, or the change
     * listener is {@code null}.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or {@link IntentService} thread.
     * @throws IllegalStateException if you try to add a listener inside a transaction.
     */
    public static <E extends RealmModel> void addChangeListener(E object, RealmObjectChangeListener<E> listener) {
        //noinspection ConstantConditions
        if (object == null) {
            throw new IllegalArgumentException("Object should not be null");
        }
        //noinspection ConstantConditions
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            BaseRealm realm = proxy.realmGet$proxyState().getRealm$realm();
            realm.checkIfValid();
            realm.sharedRealm.capabilities.checkCanDeliverNotification(BaseRealm.LISTENER_NOT_ALLOWED_MESSAGE);
            //noinspection unchecked
            proxy.realmGet$proxyState().addChangeListener(listener);
        } else {
            throw new IllegalArgumentException("Cannot add listener from this unmanaged RealmObject (created outside of Realm)");
        }
    }

    /**
     * Adds a change listener to a RealmObject that will be triggered if any value field or referenced RealmObject field
     * is changed, or the RealmList field itself is changed.
     * <p>
     * Registering a change listener will not prevent the underlying RealmObject from being garbage collected.
     * If the RealmObject is garbage collected, the change listener will stop being triggered. To avoid this, keep a
     * strong reference for as long as appropriate e.g. in a class variable.
     * <p>
     * <pre>
     * {@code
     * public class MyActivity extends Activity {
     *
     *     private Person person; // Strong reference to keep listeners alive
     *
     *     \@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *       super.onCreate(savedInstanceState);
     *       person = realm.where(Person.class).findFirst();
     *       person.addChangeListener(new RealmChangeListener<Person>() {
     *           \@Override
     *           public void onChange(Person person) {
     *               // React to change
     *           }
     *       });
     *     }
     * }
     * }
     * </pre>
     *
     * @param object RealmObject to add listener to.
     * @param listener the change listener to be notified.
     * @throws IllegalArgumentException if the {@code object} is {@code null} or an unmanaged object, or the change
     * listener is {@code null}.
     * @throws IllegalStateException if you try to add a listener from a non-Looper or {@link IntentService} thread.
     * @throws IllegalStateException if you try to add a listener inside a transaction.
     */
    public static <E extends RealmModel> void addChangeListener(E object, RealmChangeListener<E> listener) {
        addChangeListener(object, new ProxyState.RealmChangeListenerWrapper<>(listener));
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener the instance to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null} or the object is an unmanaged object.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     */
    public final void removeChangeListener(RealmObjectChangeListener listener) {
        RealmObject.removeChangeListener(this, listener);
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener the instance to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null} or the object is an unmanaged object.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     */
    public final void removeChangeListener(RealmChangeListener listener) {
        RealmObject.removeChangeListener(this, listener);
    }

    /**
     * Removes a previously registered listener on the given RealmObject.
     *
     * @param object RealmObject to remove listener from.
     * @param listener the instance to be removed.
     * @throws IllegalArgumentException if the {@code object} or the change listener is {@code null}.
     * @throws IllegalArgumentException if object is an unmanaged RealmObject.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     */
    public static <E extends RealmModel> void removeChangeListener(E object, RealmObjectChangeListener listener) {
        //noinspection ConstantConditions
        if (object == null) {
            throw new IllegalArgumentException("Object should not be null");
        }
        //noinspection ConstantConditions
        if (listener == null) {
            throw new IllegalArgumentException("Listener should not be null");
        }
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            BaseRealm realm = proxy.realmGet$proxyState().getRealm$realm();
            if (realm.isClosed()) {
                RealmLog.warn("Calling removeChangeListener on a closed Realm %s, " +
                        "make sure to close all listeners before closing the Realm.", realm.configuration.getPath());
            }
            //noinspection unchecked
            proxy.realmGet$proxyState().removeChangeListener(listener);
        } else {
            throw new IllegalArgumentException("Cannot remove listener from this unmanaged RealmObject (created outside of Realm)");
        }
    }

    /**
     * Removes a previously registered listener on the given RealmObject.
     *
     * @param object RealmObject to remove listener from.
     * @param listener the instance to be removed.
     * @throws IllegalArgumentException if the {@code object} or the change listener is {@code null}.
     * @throws IllegalArgumentException if object is an unmanaged RealmObject.
     * @throws IllegalStateException if you try to remove a listener from a non-Looper Thread.
     */
    public static <E extends RealmModel> void removeChangeListener(E object, RealmChangeListener<E> listener) {
        removeChangeListener(object, new ProxyState.RealmChangeListenerWrapper<>(listener));
    }

    /**
     * Removes all registered listeners.
     */
    public final void removeAllChangeListeners() {
        RealmObject.removeAllChangeListeners(this);
    }

    /**
     * Removes all registered listeners from the given RealmObject.
     *
     * @param object RealmObject to remove all listeners from.
     * @throws IllegalArgumentException if object is {@code null} or isn't managed by Realm.
     */
    public static <E extends RealmModel> void removeAllChangeListeners(E object) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            BaseRealm realm = proxy.realmGet$proxyState().getRealm$realm();
            if (realm.isClosed()) {
                RealmLog.warn("Calling removeChangeListener on a closed Realm %s, " +
                        "make sure to close all listeners before closing the Realm.", realm.configuration.getPath());
            }
            proxy.realmGet$proxyState().removeAllChangeListeners();
        } else {
            throw new IllegalArgumentException("Cannot remove listeners from this unmanaged RealmObject (created outside of Realm)");
        }
    }

    /**
     * Returns an RxJava Flowable that monitors changes to this RealmObject. It will emit the current object when
     * subscribed to. Object updates will continually be emitted as the RealmObject is updated -
     * {@code onComplete} will never be called.
     * <p>
     * When chaining a RealmObject flowable use {@code obj.<MyRealmObjectClass>asFlowable()} to pass on
     * type information, otherwise the type of the following observables will be {@code RealmObject}.
     * <p>
     * If you would like the {@code asFlowable()} to stop emitting items you can instruct RxJava to
     * only emit only the first item by using the {@code first()} operator:
     * <p>
     * <pre>
     * {@code
     * obj.asFlowable()
     *      .filter(obj -> obj.isLoaded())
     *      .first()
     *      .subscribe( ... ) // You only get the object once
     * }
     * </pre>
     * <p>
     * <p>
     * Note that when the {@link Realm} is accessed from threads other than where it was created,
     * {@link IllegalStateException} will be thrown. Care should be taken when using different schedulers
     * with {@code subscribeOn()} and {@code observeOn()}. Consider using {@code Realm.where().find*Async()}
     * instead.
     *
     * @param <E> RealmObject class that is being observed. Must be this class or its super types.
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath or the
     * corresponding Realm instance doesn't support RxJava.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    public final <E extends RealmObject> Flowable<E> asFlowable() {
        //noinspection unchecked
        return (Flowable<E>) RealmObject.asFlowable(this);
    }

    /**
     * Returns an Rx Observable that monitors changes to this RealmObject. It will emit the current RealmObject when
     * subscribed to. For each update to the RealmObject a pair consisting of the RealmObject and the
     * {@link ObjectChangeSet} will be sent. The changeset will be {@code null} the first
     * time the RealmObject is emitted.
     * <p>
     * The RealmObject will continually be emitted as it is updated - {@code onComplete} will never be called.
     * <p>
     * Note that when the {@link Realm} is accessed from threads other than where it was created,
     * {@link IllegalStateException} will be thrown. Care should be taken when using different schedulers
     * with {@code subscribeOn()} and {@code observeOn()}. Consider using {@code Realm.where().find*Async()}
     * instead.
     *
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath or the
     * corresponding Realm instance doesn't support RxJava.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    public final <E extends RealmObject> Observable<ObjectChange<E>> asChangesetObservable() {
        return (Observable) RealmObject.asChangesetObservable(this);
    }

    /**
     * Returns an RxJava Flowable that monitors changes to this RealmObject. It will emit the current object when
     * subscribed to. Object updates will continuously be emitted as the RealmObject is updated -
     * {@code onComplete} will never be called.
     * <p>
     * When chaining a RealmObject observable use {@code obj.<MyRealmObjectClass>asFlowable()} to pass on
     * type information, otherwise the type of the following observables will be {@code RealmObject}.
     * <p>
     * If you would like the {@code asFlowable()} to stop emitting items you can instruct RxJava to
     * emit only the first item by using the {@code first()} operator:
     * <p>
     * <pre>
     * {@code
     * obj.asFlowable()
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
    public static <E extends RealmModel> Flowable<E> asFlowable(E object) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            BaseRealm realm = proxy.realmGet$proxyState().getRealm$realm();
            if (realm instanceof Realm) {
                return realm.configuration.getRxFactory().from((Realm) realm, object);
            } else if (realm instanceof DynamicRealm) {
                DynamicRealm dynamicRealm = (DynamicRealm) realm;
                DynamicRealmObject dynamicObject = (DynamicRealmObject) object;
                @SuppressWarnings("unchecked")
                Flowable<E> observable = (Flowable<E>) realm.configuration.getRxFactory().from(dynamicRealm, dynamicObject);
                return observable;
            } else {
                throw new UnsupportedOperationException(realm.getClass() + " does not support RxJava." +
                        " See https://realm.io/docs/java/latest/#rxjava for more details.");
            }
        } else {
            // TODO Is this true? Should we just return Observable.just(object) ?
            throw new IllegalArgumentException("Cannot create Observables from unmanaged RealmObjects");
        }
    }


    /**
     * Returns an Rx Observable that monitors changes to this RealmObject. It will emit the current RealmObject when
     * subscribed to. For each update to the RealmObject a pair consisting of the RealmObject and the
     * {@link ObjectChangeSet} will be sent. The changeset will be {@code null} the first
     * time the RealmObject is emitted.
     * <p>
     * The RealmObject will continually be emitted as it is updated - {@code onComplete} will never be called.
     * <p>
     * Note that when the {@link Realm} is accessed from threads other than where it was created,
     * {@link IllegalStateException} will be thrown. Care should be taken when using different schedulers
     * with {@code subscribeOn()} and {@code observeOn()}. Consider using {@code Realm.where().find*Async()}
     * instead.
     *
     * @param object RealmObject class that is being observed. Must be this class or its super types.
     * @return RxJava Observable that only calls {@code onNext}. It will never call {@code onComplete} or {@code OnError}.
     * @throws UnsupportedOperationException if the required RxJava framework is not on the classpath or the
     * corresponding Realm instance doesn't support RxJava.
     * @see <a href="https://realm.io/docs/java/latest/#rxjava">RxJava and Realm</a>
     */
    public static <E extends RealmModel> Observable<ObjectChange<E>> asChangesetObservable(E object) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;
            BaseRealm realm = proxy.realmGet$proxyState().getRealm$realm();
            if (realm instanceof Realm) {
                return realm.configuration.getRxFactory().changesetsFrom((Realm) realm, object);
            } else if (realm instanceof DynamicRealm) {
                DynamicRealm dynamicRealm = (DynamicRealm) realm;
                DynamicRealmObject dynamicObject = (DynamicRealmObject) object;
                return (Observable) realm.configuration.getRxFactory().changesetsFrom(dynamicRealm, dynamicObject);
            } else {
                throw new UnsupportedOperationException(realm.getClass() + " does not support RxJava." +
                        " See https://realm.io/docs/java/latest/#rxjava for more details.");
            }
        } else {
            // TODO Is this true? Should we just return Observable.just(object) ?
            throw new IllegalArgumentException("Cannot create Observables from unmanaged RealmObjects");
        }
    }
}
