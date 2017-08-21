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

import javax.annotation.Nullable;

import io.realm.annotations.Beta;
import io.realm.internal.ManagableObject;
import io.realm.internal.Row;
import io.realm.internal.Table;


/**
 * A {@code MutableRealmInteger} is a mutable, {@link Long}-like numeric quantity.
 * It behaves almost exactly as a reference to a {@link Long}. More specifically:
 * <ul>
 * <li>A {@code MutableRealmInteger} may have the value {@code null}.</li>
 * <li>The {@link #equals} operator compares the contained {@link Long} values. {@code null}-valued {@code MutableRealmInteger} are {@code .equals}</li>
 * <li>The {@link #compareTo} operator compares the contained {@link Long} values.  It considers {@code null} &lt; any non-{@code null} value.</li>
 * <li>The {@link #increment} and {@link #decrement} operators throw {@link IllegalStateException} when applied to a {@code null}-valued {@code MutableRealmInteger}.</li>
 * </ul>
 * <p>
 *
 * {@code MutableRealmInteger}s are most interesting as members of a managed {@link RealmModel} object.
 * When managed, the {@link #increment} and {@link #decrement} operators implement a
 * <a href="https://en.wikipedia.org/wiki/Conflict-free_replicated_data_type">conflict free replicated data type</a>:
 * Simultaneous increments and decrements from multiple distributed clients will be aggregated correctly.
 * For instance, if the value of {@code counter} field for the object representing user "Fred" is currently 0,
 * then the following code, executed on two different devices, simultaneously, even if connected by only a slow,
 * unreliable network, will <b>always</b> cause the value of {@code counter} to converge, eventually on the value 2.
 * <pre>
 * <code> MutableRealmInteger counter = realm.where(Users.class)
 *     .equalTo("name", Fred)
 *     .findFirst()
 *     .counter.increment(1);</code>
 * </pre>
 * Note that the {@link #set(Long)} operator must be used with extreme care. It will quash the effects of any prior calls
 * to {@link #increment(long)} or {@link #decrement(long)}. Although the value of a {@code MutableRealmInteger} will
 * always converge across devices, the specific value on which it converges will depend on the actual order in which
 * operations took place. Mixing {@link #set(Long)} with {@link #increment(long)} and {@link #decrement(long)} is,
 * therefore, not advised, unless fuzzy counting is acceptable.
 * <p>
 *
 * {@code MutableRealmInteger}s may not be primary keys. Their implementations are not thread safe.
 * Like all managed Realm objects, managed {@code MutableRealmInteger}s may not be moved across threads.
 * Unmanaged {@code MutableRealmInteger}s may be moved across threads but require safe publication.
 * <p>
 *
 * A {@code MutableRealmInteger}, in a model class, must always be declared {@code final}. For instance:
 * <pre>
 * {@code public final MutableRealmInteger counter = MutableRealmInteger.ofNull(); }
 * </pre>
 * Although initializing the {@code MutableRealmInteger} as {@code null} may work very limited circumstances,
 * developers are advised <b>not</b> to do it:
 * <pre>
 * {@code
 *  public final MutableRealmInteger counter = null; // DO NOT DO THIS! }
 * </pre>
 * Also note that when a {@code MutableRealmInteger} is {@code @Required}, it is better, though not required,
 * to initialize it with a non-null value.
 * <pre>
 * <code>
 * {@literal @}Required
 *  public final MutableRealmInteger counter = MutableRealmInteger.valueOf(0L);</code>
 * </pre>
 *
 *<p>
 * A reference to a managed {@code MutableRealmInteger} is subject to all of the constraints that apply
 * to the model object from which it was obtained: It can only be mutated within a transaction and
 * it becomes invalid if the Realm backing it is closed. Use the {@code isManaged()}
 * and {@code isValid()} operators to determine whether a {@code MutableRealmInteger} is
 * in a consistent state. Note, in particular, that a reference to a managed {@code MutableRealmInteger}
 * retains a reference to the model object to which it belongs. For example in this code:
 * <pre>
 * {@code MutableRealmInteger counter = realm.where(Users.class).findFirst().counter; }
 * </pre>
 * the {@code counter} holds a reference to the {@code User} model object from which it was obtained.
 * Neither can be GCed until all references to both are unreachable.
 */
@Beta
public abstract class MutableRealmInteger implements Comparable<MutableRealmInteger>, ManagableObject {

    /**
     * Unmanaged Implementation.
     */
    private static final class Unmanaged extends MutableRealmInteger {
        @Nullable
        private Long value;

        Unmanaged(@Nullable Long value) {
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
        public void set(@Nullable Long newValue) {
            value = newValue;
        }

        @Override
        @Nullable
        public Long get() {
            return value;
        }

        @Override
        public void increment(long inc) {
            if (value == null) {
                throw new IllegalStateException("Cannot increment a MutableRealmInteger whose value is null. Set its value first.");
            }
            //noinspection UnnecessaryBoxing
            value = Long.valueOf(value + inc);
        }

        @Override
        public void decrement(long dec) {
            increment(-dec);
        }
    }


    /**
     * Managed Implementation.
     * Proxies create new subclasses for each {@code MutableRealmInteger} field.
     */
    @SuppressWarnings("unused")
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
        public final void set(@Nullable Long value) {
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

        private void setValue(@Nullable Long value, boolean isDefault) {
            Row row = getRow();
            Table table = row.getTable();
            long rowIndex = row.getIndex();
            long columnIndex = getColumnIndex();
            if (value == null) {
                table.setNull(columnIndex, rowIndex, isDefault);
            } else {
                table.setLong(columnIndex, rowIndex, value, isDefault);
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
     * Creates a new, unmanaged {@code MutableRealmInteger} whose value is {@code null}.
     */
    public static MutableRealmInteger ofNull() {
        return new MutableRealmInteger.Unmanaged(null);
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
     * Creates a new, unmanaged {@code MutableRealmInteger} with the specified initial value.
     *
     * @param value initial value: parsed by {@link Long#parseLong}.
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
    @Nullable
    public abstract Long get();

    /**
     * Sets the {@code MutableRealmInteger} value.
     * Calling {@code set} forcibly sets the {@code MutableRealmInteger} to the provided value.
     * Doing this obliterates the effects of any calls to {@link #increment} and {@link #decrement} perceived
     * before the call to {@code set}.
     *
     * @param newValue new value.
     */
    public abstract void set(@Nullable Long newValue);

    /**
     * Sets the {@code MutableRealmInteger} value.
     * Calling {@link #set} forcibly sets the {@code MutableRealmInteger} to the provided value.
     * Doing this obliterates the effects of any calls to {@link #increment} and {@link #decrement} perceived
     * before the call to {@link #set}.
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
     * @param inc quantity to be added to the {@code MutableRealmInteger}.
     */
    public abstract void increment(long inc);

    /**
     * Decrements the {@code MutableRealmInteger}, subtracting the value of the argument.
     * Increment/decrement from all devices are reflected in the new value, which is guaranteed to converge.
     *
     * @param dec quantity to be subtracted from the {@code MutableRealmInteger}.
     */
    public abstract void decrement(long dec);

    /**
     * @return true if and only if {@link #get} will return {@code null}.
     */
    public final boolean isNull() {
        return get() == null;
    }

    /**
     * {@code MutableRealmInteger}s compare strictly by their values.
     * Null is a legal value for a {@code MutableRealmInteger} and {@code null} &lt; any non-{@code null} value
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
     * A {@code MutableRealmInteger}'s hash code is, exactly, the hash code of its value.
     *
     * @return true if the target has the same value.
     */
    @Override
    public final int hashCode() {
        Long thisValue = get();
        return (thisValue == null) ? 0 : thisValue.hashCode();
    }

    /**
     * Two {@code MutableRealmInteger}s are {@code .equals} if and only if their {@code longValues} are equal.
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
