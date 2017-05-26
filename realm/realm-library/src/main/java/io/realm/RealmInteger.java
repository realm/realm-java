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
import io.realm.internal.counters.UnmanagedRealmInteger;


/**
 * TODO: document the behaviour of this object, as soon as the implementation defines it.
 */
@Beta
public abstract class RealmInteger extends Number implements Comparable<RealmInteger>, ManagableObject {

    /**
     * Creates a new {@code RealmInteger}, with the specified initial value.
     *
     * @param value initial value.
     */
    public static RealmInteger valueOf(long value) {
        return new UnmanagedRealmInteger(value);
    }

    /**
     * Creates a new realm integer, with the specified initial value.
     *
     * @param value initial value: parsed by {@code Long.parseLong}.
     */
    public static RealmInteger valueOf(String value) {
        return new UnmanagedRealmInteger(Long.parseLong(value));
    }

    /**
     * Sets the {@code RealmInteger} value.
     * Calling set() forcibly sets the RealmInteger to the provided value. Doing this means that
     * {@link #increment} and {@link #decrement} changes from other devices might be overridden.
     *
     * @param newValue new value.
     */
    public abstract void set(long newValue);

    /**
     * Increments the {@code RealmInteger}, adding the value of the argument.
     * Increment/decrement from all devices are reflected in the new value, which is guaranteed to converge.
     *
     * @param inc quantity to be added to the counter.
     */
    public abstract void increment(long inc);

    /**
     * Decrements the {@code RealmInteger}, subtracting the value of the argument.
     * Increment/decrement from all devices are reflected in the new value, which is guaranteed to converge.
     *
     * @param dec quantity to be subtracted from the counter.
     */
    public abstract void decrement(long dec);

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.valueOf(longValue());
    }

    /**
     * RealmIntegers compare strictly by their values.
     * Final to ensure contract over all subclasses
     *
     * @param o the compare target
     * @return -1, 0, or 1, depending on whether this object's value is &gt;, =, or &lt; the target's.
     */
    @Override
    public final int compareTo(RealmInteger o) {
        long otherValue = o.longValue();
        long thisValue = longValue();

        return (thisValue == otherValue) ? 0 : ((thisValue > otherValue) ? 1 : -1);
    }

    /**
     * A RealmInteger's hash code depends only on its value.
     * Must be final to ensure contract over all subclasses
     *
     * @return true if the target has the same value.
     */
    @Override
    public final int hashCode() {
        long thisValue = longValue();
        return (int) (thisValue ^ (thisValue >>> 32));
    }

    /**
     * Two RealmIntegers are {@code .equals} if and only if their longValues are equal.
     * Must be final to ensure contract over all subclasses
     *
     * @param o compare target
     * @return true if the target has the same value.
     */
    @Override
    public final boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof RealmInteger)) { return false; }
        return longValue() == ((RealmInteger) o).longValue();
    }
}
