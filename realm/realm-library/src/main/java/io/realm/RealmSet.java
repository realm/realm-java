/*
 * Copyright 2021 Realm Inc.
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

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import io.realm.internal.ManageableObject;
import io.realm.internal.OsSet;

/**
 * RealmSet is a collection that contains no duplicate elements.
 * <p>
 * Similarly to {@link RealmList}s, a RealmSet can operate in managed and unmanaged modes. In
 * managed mode a RealmSet persists all its contents inside a Realm whereas in unmanaged mode
 * it functions like a {@link HashSet}.
 * <p>
 * Managed RealmSets can only be created by Realm and will automatically update its content
 * whenever the underlying Realm is updated. Managed RealmSet can only be accessed using the getter
 * that points to a RealmSet field of a {@link RealmObject}.
 * <p>
 * Unmanaged elements in this set can be added to a Realm using the
 * {@link Realm#copyToRealm(Iterable, ImportFlag...)} method.
 * <p>
 * <b>Warning: </b> the following methods are not supported for classes containing set fields yet:
 * <ul>
 * <li>{@link Realm#insert(RealmModel)}</li>
 * <li>{@link Realm#insert(Collection)}</li>
 * <li>{@link Realm#insertOrUpdate(RealmModel)}</li>
 * <li>{@link Realm#insertOrUpdate(Collection)}</li>
 * <li>{@link Realm#createAllFromJson(Class, String)}</li>
 * <li>{@link Realm#createAllFromJson(Class, JSONArray)}</li>
 * <li>{@link Realm#createAllFromJson(Class, InputStream)}</li>
 * <li>{@link Realm#createObjectFromJson(Class, String)}</li>
 * <li>{@link Realm#createObjectFromJson(Class, JSONObject)}}</li>
 * <li>{@link Realm#createObjectFromJson(Class, InputStream)}}</li>
 * <li>{@link Realm#createOrUpdateAllFromJson(Class, String)}</li>
 * <li>{@link Realm#createOrUpdateAllFromJson(Class, JSONArray)}</li>
 * <li>{@link Realm#createOrUpdateAllFromJson(Class, InputStream)}</li>
 * <li>{@link Realm#createOrUpdateObjectFromJson(Class, String)}</li>
 * <li>{@link Realm#createOrUpdateObjectFromJson(Class, JSONObject)}</li>
 * <li>{@link Realm#createOrUpdateObjectFromJson(Class, InputStream)}</li>
 * </ul>
 *
 * @param <E> the type of the values stored in this set
 */
public class RealmSet<E> implements Set<E>, ManageableObject, RealmCollection<E> {

    protected final SetStrategy<E> setStrategy;

    // ------------------------------------------
    // Unmanaged constructors
    // ------------------------------------------

    /**
     * Instantiates a RealmSet in unmanaged mode.
     */
    public RealmSet() {
        this.setStrategy = new UnmanagedSetStrategy<>();
    }

    /**
     * Instantiates a RealmSet in unmanaged mode with another collection.
     *
     * @param collection the collection with which the set will be initially populated.
     */
    public RealmSet(Collection<E> collection) {
        this.setStrategy = new UnmanagedSetStrategy<>(collection);
    }

    // ------------------------------------------
    // Managed constructors
    // ------------------------------------------

    /**
     * Instantiates a RealmSet in managed mode. This constructor is used internally by Realm.
     *
     * @param baseRealm
     * @param osSet
     * @param valueClass
     */
    public RealmSet(BaseRealm baseRealm, OsSet osSet, Class<E> valueClass) {
        this.setStrategy = getStrategy(baseRealm, osSet, valueClass);
    }

    // ------------------------------------------
    // ManageableObject API
    // ------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isManaged() {
        return setStrategy.isManaged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return setStrategy.isValid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFrozen() {
        return setStrategy.isFrozen();
    }

    // ------------------------------------------
    // Set API
    // ------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return setStrategy.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return setStrategy.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(@Nullable Object o) {
        return setStrategy.contains(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<E> iterator() {
        return setStrategy.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return setStrategy.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return setStrategy.toArray(a);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(@Nullable E e) {
        return setStrategy.add(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(@Nullable Object o) {
        return setStrategy.remove(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return setStrategy.containsAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        return setStrategy.addAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return setStrategy.retainAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return setStrategy.removeAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        setStrategy.clear();
    }

    // ------------------------------------------
    // Freezable API
    // ------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public RealmSet<E> freeze() {
        return setStrategy.freeze();
    }

    // ------------------------------------------
    // RealmSet API
    // ------------------------------------------

    /**
     * Adds a change listener to this {@link RealmSet}.
     * <p>
     * Registering a change listener will not prevent the underlying RealmSet from being garbage
     * collected. If the RealmSet is garbage collected, the change listener will stop being
     * triggered. To avoid this, keep a strong reference for as long as appropriate e.g. in a class
     * variable.
     * <p>
     * <pre>
     * {@code
     * public class MyActivity extends Activity {
     *
     *     private RealmSet<Dog> dogs; // Strong reference to keep listeners alive
     *
     *     \@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *       super.onCreate(savedInstanceState);
     *       dogs = realm.where(Person.class).findFirst().getDogs();
     *       dogs.addChangeListener(new RealmChangeListener<RealmSet<Dog>>() {
     *           \@Override
     *           public void onChange(RealmSet<Dog> map) {
     *               // React to change
     *           }
     *       });
     *     }
     * }
     * }
     * </pre>
     *
     * @param listener the listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException    if you try to add a listener from a non-Looper or
     *                                  {@link android.app.IntentService} thread.
     */
    public void addChangeListener(RealmChangeListener<RealmSet<E>> listener) {
        setStrategy.addChangeListener(this, listener);
    }

    /**
     * Adds a change listener to this {@link RealmSet}.
     * <p>
     * Registering a change listener will not prevent the underlying RealmSet from being garbage
     * collected. If the RealmSet is garbage collected, the change listener will stop being
     * triggered. To avoid this, keep a strong reference for as long as appropriate e.g. in a class
     * variable.
     * <p>
     * <pre>
     * {@code
     * public class MyActivity extends Activity {
     *
     *     private RealmSet<Dog> dogs; // Strong reference to keep listeners alive
     *
     *     \@Override
     *     protected void onCreate(Bundle savedInstanceState) {
     *       super.onCreate(savedInstanceState);
     *       dogs = realm.where(Person.class).findFirst().getDogs();
     *       dogs.addChangeListener(new SetChangeListener<Dog>() {
     *           \@Override
     *           public void onChange(RealmSet<Dog> set, SetChangeSet changeSet) {
     *               // React to change
     *           }
     *       });
     *     }
     * }
     * }
     * </pre>
     *
     * @param listener the listener to be notified.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException    if you try to add a listener from a non-Looper or
     *                                  {@link android.app.IntentService} thread.
     */
    public void addChangeListener(SetChangeListener<E> listener) {
        setStrategy.addChangeListener(this, listener);
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException    if you try to remove a listener from a non-Looper Thread.
     */
    public void removeChangeListener(RealmChangeListener<RealmSet<E>> listener) {
        setStrategy.removeChangeListener(this, listener);
    }

    /**
     * Removes the specified change listener.
     *
     * @param listener the change listener to be removed.
     * @throws IllegalArgumentException if the change listener is {@code null}.
     * @throws IllegalStateException    if you try to remove a listener from a non-Looper Thread.
     */
    public void removeChangeListener(SetChangeListener<E> listener) {
        setStrategy.removeChangeListener(this, listener);
    }

    /**
     * Removes all user-defined change listeners.
     *
     * @throws IllegalStateException if you try to remove listeners from a non-Looper Thread.
     * @see io.realm.RealmChangeListener
     */
    public void removeAllChangeListeners() {
        setStrategy.removeAllChangeListeners();
    }

    /**
     * Indicates whether a set has any listeners attached to it.
     *
     * @return {@code true} if any listeners have been added, {@code false} otherwise.
     */
    public boolean hasListeners() {
        return setStrategy.hasListeners();
    }

    // ------------------------------------------
    // RealmCollection API
    // ------------------------------------------

    /**
     * Returns a RealmQuery, which can be used to query for specific objects of this class.
     *
     * @return a RealmQuery object.
     * @throws IllegalStateException if Realm instance has been closed or parent object has been removed.
     * @see io.realm.RealmQuery
     */
    @Override
    public RealmQuery<E> where() {
        return setStrategy.where();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Number min(String fieldName) {
        return setStrategy.min(fieldName);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Number max(String fieldName) {
        return setStrategy.max(fieldName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number sum(String fieldName) {
        return setStrategy.sum(fieldName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double average(String fieldName) {
        return setStrategy.average(fieldName);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Date maxDate(String fieldName) {
        return setStrategy.maxDate(fieldName);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Date minDate(String fieldName) {
        return setStrategy.minDate(fieldName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteAllFromRealm() {
        return setStrategy.deleteAllFromRealm();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoaded() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean load() {
        return true;
    }

    // ------------------------------------------
    // Private stuff
    // ------------------------------------------

    OsSet getOsSet() {
        return setStrategy.getOsSet();
    }

    @SuppressWarnings("unchecked")
    private static <T> ManagedSetStrategy<T> getStrategy(BaseRealm baseRealm,
                                                         OsSet osSet,
                                                         Class<T> valueClass) {
        if (CollectionUtils.isClassForRealmModel(valueClass)) {
            Class<? extends RealmModel> typeCastClass = (Class<? extends RealmModel>) valueClass;
            return new ManagedSetStrategy<>((SetValueOperator<T>) new RealmModelSetOperator<>(baseRealm, osSet, typeCastClass), valueClass);
        }

        SetValueOperator<T> operator;
        if (valueClass == Boolean.class) {
            operator = (SetValueOperator<T>) new BooleanOperator(baseRealm, osSet, Boolean.class);
        } else if (valueClass == String.class) {
            operator = (SetValueOperator<T>) new StringOperator(baseRealm, osSet, String.class);
        } else if (valueClass == Integer.class) {
            operator = (SetValueOperator<T>) new IntegerOperator(baseRealm, osSet, Integer.class);
        } else if (valueClass == Long.class) {
            operator = (SetValueOperator<T>) new LongOperator(baseRealm, osSet, Long.class);
        } else if (valueClass == Short.class) {
            operator = (SetValueOperator<T>) new ShortOperator(baseRealm, osSet, Short.class);
        } else if (valueClass == Byte.class) {
            operator = (SetValueOperator<T>) new ByteOperator(baseRealm, osSet, Byte.class);
        } else if (valueClass == Float.class) {
            operator = (SetValueOperator<T>) new FloatOperator(baseRealm, osSet, Float.class);
        } else if (valueClass == Double.class) {
            operator = (SetValueOperator<T>) new DoubleOperator(baseRealm, osSet, Double.class);
        } else if (valueClass == byte[].class) {
            operator = (SetValueOperator<T>) new BinaryOperator(baseRealm, osSet, byte[].class);
        } else if (valueClass == Date.class) {
            operator = (SetValueOperator<T>) new DateOperator(baseRealm, osSet, Date.class);
        } else if (valueClass == Decimal128.class) {
            operator = (SetValueOperator<T>) new Decimal128Operator(baseRealm, osSet, Decimal128.class);
        } else if (valueClass == ObjectId.class) {
            operator = (SetValueOperator<T>) new ObjectIdOperator(baseRealm, osSet, ObjectId.class);
        } else if (valueClass == UUID.class) {
            operator = (SetValueOperator<T>) new UUIDOperator(baseRealm, osSet, UUID.class);
        } else if (valueClass == RealmAny.class) {
            operator = (SetValueOperator<T>) new RealmAnySetOperator(baseRealm, osSet, RealmAny.class);
        } else {
            throw new UnsupportedOperationException("getStrategy: missing class '" + valueClass.getSimpleName() + "'");
        }

        return new ManagedSetStrategy<>(operator, valueClass);
    }

    /**
     * Strategy responsible for abstracting the managed/unmanaged logic for sets.
     *
     * @param <E> the type of the values stored in this set
     */
    private abstract static class SetStrategy<E> implements Set<E>, ManageableObject, RealmCollection<E> {
        abstract OsSet getOsSet();

        abstract void addChangeListener(RealmSet<E> set, RealmChangeListener<RealmSet<E>> listener);

        abstract void addChangeListener(RealmSet<E> set, SetChangeListener<E> listener);

        abstract void removeChangeListener(RealmSet<E> set, RealmChangeListener<RealmSet<E>> listener);

        abstract void removeChangeListener(RealmSet<E> set, SetChangeListener<E> listener);

        abstract void removeAllChangeListeners();

        abstract boolean hasListeners();

        @Override
        public abstract RealmSet<E> freeze();
    }

    /**
     * Concrete {@link RealmSet.SetStrategy} that works for managed {@link io.realm.RealmSet}s.
     *
     * @param <E> the value type
     */
    private static class ManagedSetStrategy<E> extends SetStrategy<E> {

        private final SetValueOperator<E> setValueOperator;
        private final Class<E> valueClass;

        ManagedSetStrategy(SetValueOperator<E> setValueOperator, Class<E> valueClass) {
            this.setValueOperator = setValueOperator;
            this.valueClass = valueClass;
        }

        // ------------------------------------------
        // ManageableObject API
        // ------------------------------------------

        @Override
        public boolean isManaged() {
            return true;
        }

        // ------------------------------------------
        // RealmCollection API
        // ------------------------------------------

        @Override
        public RealmQuery<E> where() {
            return setValueOperator.where();
        }

        @Nullable
        @Override
        public Number min(String fieldName) {
            return where().min(fieldName);
        }

        @Nullable
        @Override
        public Number max(String fieldName) {
            return where().max(fieldName);
        }

        @Override
        public Number sum(String fieldName) {
            return where().sum(fieldName);
        }

        @Override
        public double average(String fieldName) {
            return where().average(fieldName);
        }

        @Nullable
        @Override
        public Date maxDate(String fieldName) {
            return where().maximumDate(fieldName);
        }

        @Nullable
        @Override
        public Date minDate(String fieldName) {
            return where().minimumDate(fieldName);
        }

        @Override
        public boolean deleteAllFromRealm() {
            setValueOperator.baseRealm.checkIfValid();
            if (!setValueOperator.isEmpty()) {
                setValueOperator.deleteAll();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public boolean load() {
            return true;
        }

        @Override
        public boolean isValid() {
            return setValueOperator.isValid();
        }

        @Override
        public boolean isFrozen() {
            return setValueOperator.isFrozen();
        }

        // ------------------------------------------
        // Set API
        // ------------------------------------------

        @Override
        public int size() {
            return setValueOperator.size();
        }

        @Override
        public boolean isEmpty() {
            return setValueOperator.isEmpty();
        }

        @Override
        public boolean contains(@Nullable Object o) {
            return setValueOperator.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return setValueOperator.iterator();
        }

        @Override
        public Object[] toArray() {
            Object[] array = new Object[size()];
            int i = 0;
            for (E value : this) {
                array[i] = value;
                i++;
            }
            return array;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            checkValidArray(a);

            T[] array;
            long setSize = size();

            // From docs:
            // If the set fits in the specified array, it is returned therein.
            // Otherwise, a new array is allocated with the runtime type of the
            // specified array and the size of this set.
            if (a.length == setSize || a.length > setSize) {
                array = a;
            } else {
                array = (T[]) Array.newInstance(valueClass, (int) setSize);
            }

            int i = 0;
            for (E value : this) {
                if (value == null) {
                    array[i] = null;
                } else {
                    array[i] = (T) value;
                }
                i++;
            }

            // From docs:
            // If this set fits in the specified array with room to spare
            // (i.e., the array has more elements than this set), the element in
            // the array immediately following the end of the set is set to null.
            if (a.length > setSize) {
                array[i] = null;
            }

            return array;
        }

        @Override
        public boolean add(@Nullable E e) {
            return setValueOperator.add(e);
        }

        @Override
        public boolean remove(@Nullable Object o) {
            return setValueOperator.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            checkValidCollection(c);
            return setValueOperator.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            checkValidCollection(c);
            return setValueOperator.addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            checkValidCollection(c);
            return setValueOperator.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            checkValidCollection(c);
            return setValueOperator.removeAll(c);
        }

        @Override
        public void clear() {
            setValueOperator.clear();
        }

        // ------------------------------------------
        // Freezable API
        // ------------------------------------------

        @Override
        public RealmSet<E> freeze() {
            return setValueOperator.freeze();
        }

        @Override
        OsSet getOsSet() {
            return setValueOperator.getOsSet();
        }

        // ------------------------------------------
        // RealmSet API
        // ------------------------------------------

        @Override
        void addChangeListener(RealmSet<E> set, RealmChangeListener<RealmSet<E>> listener) {
            setValueOperator.addChangeListener(set, listener);
        }

        @Override
        void addChangeListener(RealmSet<E> set, SetChangeListener<E> listener) {
            setValueOperator.addChangeListener(set, listener);
        }

        @Override
        void removeChangeListener(RealmSet<E> set, RealmChangeListener<RealmSet<E>> listener) {
            setValueOperator.removeChangeListener(set, listener);
        }

        @Override
        void removeChangeListener(RealmSet<E> set, SetChangeListener<E> listener) {
            setValueOperator.removeChangeListener(set, listener);
        }

        @Override
        void removeAllChangeListeners() {
            setValueOperator.removeAllChangeListeners();
        }

        @Override
        boolean hasListeners() {
            return setValueOperator.hasListeners();
        }

        // ------------------------------------------
        // Private stuff
        // ------------------------------------------

        private <T> void checkValidArray(T[] array) {
            if (array == null) {
                // According to Java Set documentation
                throw new NullPointerException("Cannot pass a null array when calling 'toArray'.");
            }

            String valueClassSimpleName = valueClass.getSimpleName();
            String arrayTypeSimpleName = array.getClass().getComponentType().getSimpleName();

            // According to Java Set documentation
            if (!valueClassSimpleName.equals(arrayTypeSimpleName)) {
                throw new ArrayStoreException("Array type must be of type '" + valueClassSimpleName +
                        "' but it was of type '" + arrayTypeSimpleName + "'.");
            }
        }

        private void checkValidCollection(Collection<?> collection) {
            if (collection == null) {
                throw new NullPointerException("Collection must not be null.");
            }
        }
    }

    /**
     * Concrete {@link RealmSet.SetStrategy} that works for unmanaged {@link io.realm.RealmSet}s.
     *
     * @param <E> the value type
     */
    private static class UnmanagedSetStrategy<E> extends SetStrategy<E> {

        private static final String ONLY_IN_MANAGED_MODE_MESSAGE = "This method is only available in managed mode.";

        private final Set<E> unmanagedSet;

        UnmanagedSetStrategy() {
            unmanagedSet = new HashSet<>();
        }

        UnmanagedSetStrategy(Collection<E> collection) {
            this();
            unmanagedSet.addAll(collection);
        }

        // ------------------------------------------
        // ManageableObject API
        // ------------------------------------------

        @Override
        public boolean isManaged() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isFrozen() {
            return false;
        }

        // ------------------------------------------
        // ManageableObject API
        // ------------------------------------------

        @Override
        public RealmQuery<E> where() {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }

        @Nullable
        @Override
        public Number min(String fieldName) {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }

        @Nullable
        @Override
        public Number max(String fieldName) {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }

        @Override
        public Number sum(String fieldName) {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }

        @Override
        public double average(String fieldName) {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }

        @Nullable
        @Override
        public Date maxDate(String fieldName) {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }

        @Nullable
        @Override
        public Date minDate(String fieldName) {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }

        @Override
        public boolean deleteAllFromRealm() {
            throw new UnsupportedOperationException(ONLY_IN_MANAGED_MODE_MESSAGE);
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public boolean load() {
            return true;
        }

        // ------------------------------------------
        // Set API
        // ------------------------------------------

        @Override
        public int size() {
            return unmanagedSet.size();
        }

        @Override
        public boolean isEmpty() {
            return unmanagedSet.isEmpty();
        }

        @Override
        public boolean contains(@Nullable Object o) {
            return unmanagedSet.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return unmanagedSet.iterator();
        }

        @Override
        public Object[] toArray() {
            return unmanagedSet.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return unmanagedSet.toArray(a);
        }

        @Override
        public boolean add(@Nullable E e) {
            return unmanagedSet.add(e);
        }

        @Override
        public boolean remove(@Nullable Object o) {
            return unmanagedSet.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return unmanagedSet.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return unmanagedSet.addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return unmanagedSet.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return unmanagedSet.removeAll(c);
        }

        @Override
        public void clear() {
            unmanagedSet.clear();
        }

        // ------------------------------------------
        // Freezable API
        // ------------------------------------------

        @Override
        public RealmSet<E> freeze() {
            throw new UnsupportedOperationException("Unmanaged RealmSets cannot be frozen.");
        }

        @Override
        OsSet getOsSet() {
            throw new UnsupportedOperationException("Unmanaged RealmSets do not have a representation in native code.");
        }

        // ------------------------------------------
        // RealmSet API
        // ------------------------------------------

        @Override
        void addChangeListener(RealmSet<E> set, RealmChangeListener<RealmSet<E>> listener) {
            throw new UnsupportedOperationException("Unmanaged RealmSets do not support change listeners.");
        }

        @Override
        void addChangeListener(RealmSet<E> set, SetChangeListener<E> listener) {
            throw new UnsupportedOperationException("Unmanaged RealmSets do not support change listeners.");
        }

        @Override
        void removeChangeListener(RealmSet<E> set, RealmChangeListener<RealmSet<E>> listener) {
            throw new UnsupportedOperationException("Cannot remove change listener because unmanaged RealmSets do not support change listeners.");
        }

        @Override
        void removeChangeListener(RealmSet<E> set, SetChangeListener<E> listener) {
            throw new UnsupportedOperationException("Cannot remove change listener because unmanaged RealmSets do not support change listeners.");
        }

        @Override
        void removeAllChangeListeners() {
            throw new UnsupportedOperationException("Cannot remove change listeners because unmanaged RealmSets do not support change listeners.");
        }

        @Override
        boolean hasListeners() {
            return false;
        }
    }
}
