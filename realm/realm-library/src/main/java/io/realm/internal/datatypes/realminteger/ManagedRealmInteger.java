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
package io.realm.internal.datatypes.realminteger;

import io.realm.RealmInteger;


/**
 * TODO: document the behaviour of this object, as soon as the implementation defines it.
 *
 * Note that {@link io.realm.RealmInteger#compareTo(io.realm.RealmInteger)},
 * {@link io.realm.RealmInteger#equals} and {@link RealmInteger#hashCode()} are final
 * in the superclass, to assure the reflexive contract.
 */
public final class ManagedRealmInteger extends RealmInteger {

    /**
     * Creates a new {@code ManagedRealmInteger}, with the specified initial value.
     *
     * @param value initial value.
     */
    public ManagedRealmInteger(long value) {

    }

    /**
     * Sets the {@code ManagedRealmInteger} value.
     * Calling set() forcibly sets the ManagedRealmInteger to the provided value. Doing this means that
     * {@link #increment} and {@link #decrement} changes from other devices might be overridden.
     *
     * @param newValue new value.
     */
    @Override
    public void set(long newValue) {

    }

    /**
     * Increments the {@code ManagedRealmInteger}, adding the value of the argument.
     * Increment/decrement from all devices are reflected in the new value, which is guaranteed to converge.
     *
     * @param inc quantity to be added to the counter.
     */
    @Override
    public void increment(long inc) {

    }

    /**
     * Decrements the {@code ManagedRealmInteger}, subtracting the value of the argument.
     * Increment/decrement from all devices are reflected in the new value, which is guaranteed to converge.
     *
     * @param dec quantity to be subtracted from the counter.
     */
    @Override
    public void decrement(long dec) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isManaged() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        return 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int intValue() {
        return (int) 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValue() {
        return (double) 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float floatValue() {
        return (float) 0L;
    }
}
