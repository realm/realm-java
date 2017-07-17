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
import io.realm.internal.Table;


/**
 * A MutableRealmInteger is a mutable, {@link java.lang.Long}-like numeric quantity.
 * It behaves almost exactly as a reference to a {@link java.lang.Long}. More specifically:
 * <ul>
 * <li>A MutableRealmInteger may have the value {@code null}.</li>
 * <li>The {@code .equals} operator compares the contained Long values. null-valued MutableRealmInteger are {@code .equals}</li>
 * <li>The {@code .compareTo} operator compares the contained Long values.  It considers {@code null} &lt; any value.</li>
 * <li>The {@code .increment} and {@code .decrement} operators throw {@code IllegalStateException} when applied to a null-valued MutableRealmInteger.</li>
 * </ul>
 * <p>
 *
 * MutableRealmIntegers are most interesting as members of a managed {@code RealmModel} object.
 * When managed, the {@code .increment} and {@code .decrement} operators implement a
 * <a href="https://en.wikipedia.org/wiki/Conflict-free_replicated_data_type">conflict free replicated data type</a>:
 * Simultaneous increments and decrements from multiple distributed clients will be aggregated correctly.
 * For instance, if the value of {@code counter} for the user "Fred" is currently 0, then the following code,
 * executed on two different devices, simultaneously, even if connected by only a slow, unreliable network,
 * will <b>always</b> cause the value of {@code counter} to converge, eventually on the value 2.
 * <pre>
 * {@code
 * MutableRealmInteger counter = realm.where(Users.class)
 *     .equalTo("name", Fred)
 *     .counter.increment(1);
 * }
 * </pre>
 * Note that the {@link #set(Long)} operator must be used with extreme care. It will override any method calls
 * to {@link #increment(long)} or {@link #decrement(long)}. Although the value of a MutableRealmInteger will
 * always converge across devices the specific value will depend on the actual order in which operations took place.
 * Mixing {@link #set(Long)} with {@link #increment(long)} is, therefore, not advised, unless fuzzy counting
 * is acceptable.
 * <p>
 *
 * MutableRealmIntegers may not be primary keys. Their implementations are not thread safe.
 * Like all manage Realm objects, managed MutableRealmIntegers may not be moved across threads.
 * Unmanaged MutableRealmIntegers may be moved across threads but require safe publication.
 * <p>
 *
 * A MutableRealmInteger, in a model class, must always be declared final. For instance:
 * <pre>
 * {@code public final MutableRealmInteger counter = MutableRealmInteger.ofNull(); }
 * </pre>
 * Although this may work under very limited circumstances, developers are advised <b>not</b> to do it:
 * <pre>
 * {@code public final MutableRealmInteger counter = null; }
 * </pre>
 * Also note that when a MutableRealmInteger is {@code @Required}, it is better, though not required,
 * to initialize it with a non-null value.
 * <pre>
 * {@code
 * @Required
 * public final MutableRealmInteger counter = MutableRealmInteger.valueOf(0L);
 * }
 * </pre>
 *
 *<p>
 * A reference to a managed MutableRealmInteger is subject to all of the constraints that apply to the model object
 * from which it was obtained: It can only be mutated within a transaction. It becomes invalid if
 * the Realm backing it is closed. Use the {@link #isManaged}  and {@link #isManaged}  operators to
 * determine whether a MutableRealmInteger is in a consistent state. Note, in particular, that a reference
 * to a managed MutableRealmInteger retains a reference to the model object to which it belongs.
 * For example in this code:
 * <pre>
 * {@code
 * MutableRealmInteger counter = realm.where(Users.class).findFirst().counter;
 * }
 * </pre>
 * {@code counter} holds a reference to the {@code User} model object from which it was obtained.
 * Neither can be GCed until all references to both are unreachable.
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
        public boolean isManaged() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
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
                throw new IllegalStateException("Attempt to increment a null valued MutableRealmInteger");
            }
            value = Long.valueOf(value + inc);
        }

        @Override
        public void decrement(long dec) {
            increment(-dec);
        }
    }


    /**
     * Managed Implementation.
     * Proxies create new subclasses for each MutableRealmInteger field.
     */
    abstract static class Managed<T extends RealmModel> extends MutableRealmInteger {
        protected abstract ProxyState<T> getProxyState();

        protected abstract long getColumnIndex();

        @Override
        public final boolean isManaged() {
            return true;
        }

        @Override
        public final boolean isValid() {
            return !getRealm().isClosed() && getRow().isAttached();
        }

        @Override
        public final Long get() {
            Row row = getRow();
            row.checkIfAttached();
            long columnIndex = getColumnIndex();
            return (row.isNull(columnIndex)) ? null : row .getLong(columnIndex);
        }

        @Override
        public final void set(Long value) {
            ProxyState proxyState = getProxyState();
            proxyState.getRealm$realm().checkIfValidAndInTransaction();

            if (!proxyState.isUnderConstruction()) {
                setValue(value, false);
                return;
            }

            if (!proxyState.getAcceptDefaultValue$realm()) {
                return;
            }

            setValue(value, true);
        }

        @Override
        public final void increment(long inc) {
            getRealm().checkIfValidAndInTransaction();
            Row row = getRow();
            row.getTable().incrementLong(getColumnIndex(), row.getIndex(), inc);
        }

        @Override
        public final void decrement(long dec) {
            increment(-dec);
        }

        private BaseRealm getRealm() {
            return getProxyState().getRealm$realm();
       }

        private Row getRow() {
            return getProxyState().getRow$realm();
        }

        private void setValue(Long value, boolean isDefault) {
            Row row = getRow();
            Table t = row.getTable();
            long rowIndex = row.getIndex();
            long columnIndex = getColumnIndex();
            if (value == null) {
                t.setNull(columnIndex, rowIndex, isDefault);
            } else {
                t.setLong(columnIndex, rowIndex, value, isDefault);
            }
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
        return (thisValue == null)
                ? ((otherValue == null) ? 0 : -1)
                : ((otherValue == null) ? 1 : thisValue.compareTo (otherValue));
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
        return (thisValue == null) ? otherValue == null : thisValue.equals(otherValue);
    }
}
