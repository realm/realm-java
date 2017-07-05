/*
 * Copyright 2017 Realm Inc.
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

import io.realm.annotations.Beta;
import io.realm.internal.ManagableObject;
import io.realm.internal.Row;


/**
 * A MutableRealmInteger is a mutable, {@link java.lang.Long}-like numeric quantity.
 * MutableRealmInteger implementations are not thread safe.  Like all Realm objects,
 * managed RealmIntegers may not be moved across threads.  Unmanaged RealmObject
 * may be moved across threads but require safe publication.
 * <p>
 * It wraps an internal CRDT counter:
 *
 * @see <a href="https://en.wikipedia.org/wiki/Conflict-free_replicated_data_type"></a>
 * <p>
 * TODO: More complete docs, including examples of use.
 */
@Beta
public abstract class MutableRealmInteger implements Comparable<MutableRealmInteger>, ManagableObject {

    /**
     * Unmanaged Implementation.
     */
    private static final class UnmanagedMutableRealmInteger extends MutableRealmInteger {
        private Long value;

        UnmanagedMutableRealmInteger(long value) {
            this.value = value;
        }

        @Override
        public void set(Long newValue) {
            value = newValue;
        }

        @Override
        public Long get() {
            return value;
        }

        @Override
        public void increment(long inc) {
            if (value != null) {
                value = Long.valueOf(value + inc);
            }
        }

        @Override
        public void decrement(long dec) {
            if (value != null) {
                value = Long.valueOf(value - dec);
            }
        }

        @Override
        public boolean isManaged() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }


    /**
     * Managed Implementation.
     */
    static class ManagedMutableRealmInteger extends MutableRealmInteger {
        private final ProxyState<?> proxyState;
        private final BaseRealm realm;
        private final Row row;
        private final long columnIndex;

        ManagedMutableRealmInteger(ProxyState<? extends RealmObject> proxyState, long columnIndex) {
            this.proxyState = proxyState;
            this.realm = proxyState.getRealm$realm();
            this.row = proxyState.getRow$realm();
            this.columnIndex = columnIndex;
        }

        @Override
        public boolean isManaged() {
            return true;
        }

        @Override
        public boolean isValid() {
            return !realm.isClosed() && row.isAttached();
        }

        @Override
        public Long get() {
            return null;
        }

        @Override
        public void set(Long value) {
            // FIXME Counters: wire up to native increment method

// Template code:
//            if (proxyState.isUnderConstruction()) {
//                if (!proxyState.getAcceptDefaultValue$realm()) {  // Wat?
//                    return;
//                }
//
//                row.getTable().setLong(columnIndex, row.getIndex(), value, true);
//                return;
//            }
//
//            realm.checkIfValidAndInTransaction();
//            row.setLong(columnIndex, value);
        }

        @Override
        public void increment(long inc) {
            realm.checkIfValidAndInTransaction();
            // FIXME Counters: wire up to native increment method
        }

        @Override
        public void decrement(long dec) {
            realm.checkIfValidAndInTransaction();
            // FIXME Counters: wire up to native increment method
        }

    }

    /**
     * Creates a new, unmanaged {@code MutableRealmInteger} with the specified initial value.
     *
     * @param value initial value.
     */
    public static MutableRealmInteger valueOf(Long value) {
        return new UnmanagedMutableRealmInteger(value);
    }

    /**
     * Creates a new, unmanaged {@code MutableRealmInteger} whose value is null.
     *
     */
    public static MutableRealmInteger ofNull() {
        return valueOf((Long) null);
    }

    /**
     * Creates a new, unmanaged {@code MutableRealmInteger} with the specified initial value.
     *
     * @param value initial value.
     */
    public static MutableRealmInteger valueOf(long value) {
        return valueOf(Long.valueOf(value));
    }

    /**
     * Creates a new, unmmanaged {@code MutableRealmInteger} with the specified initial value.
     *
     * @param value initial value: parsed by {@code Long.parseLong}.
     */
    public static MutableRealmInteger valueOf(String value) {
        return valueOf(Long.parseLong(value));
    }

    /**
     * Creates a new, managed {@code MutableRealmInteger}.
     *
     * @param proxyState Proxy state object.  Contains refs to Realm and Row.
     * @param columnIndex The index of the column that contains the MutableRealmInteger.
     * @return a managed MutableRealmInteger.
     */
    static MutableRealmInteger managedRealmInteger(ProxyState<? extends RealmObject> proxyState, long columnIndex) {
        return new ManagedMutableRealmInteger(proxyState, columnIndex);
    }

    /**
     * Seal the class.
     * In fact, this allows subclasses inside the package "realm.io".
     * Because it eliminates the synthetic constructor, though, we can live with that.
     * Don't make subclasses.
     */
    MutableRealmInteger() {}

    /**
     * Gets the {@code MutableRealmInteger} value.
     * The value may be null.
     *
     * @return the value.
     */
    public abstract Long get();

    /**
     * Sets the {@code MutableRealmInteger} value.
     * Calling set() forcibly sets the MutableRealmInteger to the provided value. Doing this means that
     * {@link #increment} and {@link #decrement} changes from other devices might be overridden.
     *
     * @param newValue new value.
     */
    public abstract void set(Long newValue);

    /**
     * Sets the {@code MutableRealmInteger} value.
     * Calling set() forcibly sets the MutableRealmInteger to the provided value. Doing this means that
     * {@link #increment} and {@link #decrement} changes from other devices might be overridden.
     *
     * @param newValue new value.
     */
    public final void set(long newValue) {
        set(Long.valueOf(newValue));
    }

    /**
     * Increments the {@code MutableRealmInteger}, adding the value of the argument.
     * Increment/decrement from all devices are reflected in the new value, which is guaranteed to converge.
     *
     * @param inc quantity to be added to the counter.
     */
    public abstract void increment(long inc);

    /**
     * Decrements the {@code MutableRealmInteger}, subtracting the value of the argument.
     * Increment/decrement from all devices are reflected in the new value, which is guaranteed to converge.
     *
     * @param dec quantity to be subtracted from the counter.
     */
    public abstract void decrement(long dec);


    /**
     * @return true if and only iff {@code get()} will return {@code null}.
     */
    public final boolean isNull() {
        return get() == null;
    }


    /**
     * RealmIntegers compare strictly by their values.
     * Will NPE if either value is null
     *
     * @param o the compare target
     * @return -1, 0, or 1, depending on whether this object's value is &gt;, =, or &lt; the target's.
     */
    @Override
    public final int compareTo(MutableRealmInteger o) {
        Long otherValue = o.get();
        Long thisValue = get();

        return thisValue.compareTo(otherValue);
    }

    /**
     * A MutableRealmInteger's hash code depends only on its value.
     * Will NPE if either value is null
     *
     * @return true if the target has the same value.
     */
    @Override
    public final int hashCode() {
        return get().hashCode();
    }

    /**
     * Two RealmIntegers are {@code .equals} if and only if their longValues are equal.
     * Will NPE if either value is null
     *
     * @param o compare target
     * @return true if the target has the same value.
     */
    @Override
    public final boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof MutableRealmInteger)) { return false; }
        return get().equals(((MutableRealmInteger) o).get());
    }
}
