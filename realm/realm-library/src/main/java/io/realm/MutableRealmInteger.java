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
 * managed MutableRealmIntegers may not be moved across threads.  Unmanaged MutableRealmIntegers
 * may be moved across threads but require safe publication.
 * <p>
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
    private static final class Unmanaged extends MutableRealmInteger {
        private Long value;

        Unmanaged(Long value) {
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
            if (value == null) {
                throw new NullPointerException("Attempt to increment a null valued MutableRealmInteger");
            }
            value = Long.valueOf(value + inc);
        }

        @Override
        public void decrement(long dec) {
            if (value == null) {
                throw new NullPointerException("Attempt to decrement a null valued MutableRealmInteger");
            }
            value = Long.valueOf(value - dec);
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
    // FIXME MutableRealmIntegers: wire up to native increment method
    static class Managed extends MutableRealmInteger {
        private final ProxyState<?> proxyState;
        private final BaseRealm realm;
        private final Row row;
        private final long columnIndex;
        private Long value; // FIXME MutableRealmIntegers: remove!

        /**
         * Inject the proxy state into this managed MutableRealmInteger.
         *
         * @param proxyState Proxy state object.  Contains refs to Realm and Row.
         * @param columnIndex The index of the column that contains the MutableRealmInteger.
         * @return a managed MutableRealmInteger.
         */
        Managed(ProxyState<? extends RealmObject> proxyState, long columnIndex) {
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
            return value;
        }


//        // Template code:
//        if (proxyState.isUnderConstruction()) {
//            if (!proxyState.getAcceptDefaultValue$realm()) {  // Wat?
//                return;
//            }
//
//            row.getTable().setLong(columnIndex, row.getIndex(), value, true);
//            return;
//        }
//        row.setLong(columnIndex,value);
        @Override
        public void set(Long value) {
            realm.checkIfValidAndInTransaction();
            this.value = value;
        }

        @Override
        public void increment(long inc) {
            Long val = get();
            if (val == null) {
                throw new NullPointerException("Attempt to increment a null valued MutableRealmInteger");
            }
            set(val + inc);
        }

        @Override
        public void decrement(long dec) {
            Long val = get();
            if (val == null) {
                throw new NullPointerException("Attempt to decrement a null valued MutableRealmInteger");
            }
            set(val - dec);
        }
    }

    /**
     * Creates a new, unmanaged {@code MutableRealmInteger} with the specified initial value.
     *
     * @param value initial value.
     */
    public static MutableRealmInteger valueOf(Long value) {
        return new MutableRealmInteger.Unmanaged(value);
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
     * @return a managed MutableRealmInteger.
     */
    static MutableRealmInteger.Managed getManaged(ProxyState<? extends RealmObject> proxyState, long columnIndex) {
        return new Managed(proxyState, columnIndex);
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
     * @param inc quantity to be added to the MutableRealmInteger.
     */
    public abstract void increment(long inc);

    /**
     * Decrements the {@code MutableRealmInteger}, subtracting the value of the argument.
     * Increment/decrement from all devices are reflected in the new value, which is guaranteed to converge.
     *
     * @param dec quantity to be subtracted from the MutableRealmInteger.
     */
    public abstract void decrement(long dec);


    /**
     * @return true if and only if {@code get()} will return {@code null}.
     */
    public final boolean isNull() {
        return get() == null;
    }


    /**
     * MutableRealmIntegers compare strictly by their values.
     * Null is a legal value for a MutableRealmInteger: null &lt; non-null
     *
     * @param o the compare target
     * @return -1, 0, or 1, depending on whether this object's value is &lt;, =, or &gt; the target's.
     */
    @Override
    public final int compareTo(MutableRealmInteger o) {
        Long thisValue = get();
        Long otherValue = o.get();
        return (thisValue == null) ? ((otherValue == null) ? 0 : -1)
                : (otherValue == null) ? 1 : thisValue.compareTo(otherValue);
    }

    /**
     * A MutableRealmInteger's hash code depends only on its value.
     *
     * @return true if the target has the same value.
     */
    @Override
    public final int hashCode() {
        Long thisValue = get();
        return (thisValue == null) ? 0 : thisValue.hashCode();
    }

    /**
     * Two MutableRealmIntegers are {@code .equals} if and only if their longValues are equal.
     *
     * @param o compare target
     * @return true if the target has the same value.
     */
    @Override
    public final boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof MutableRealmInteger)) { return false; }
        Long thisValue = get();
        Long otherValue = ((MutableRealmInteger) o).get();
        return  (thisValue == null) ? otherValue == null : thisValue.equals(otherValue);
    }
}
