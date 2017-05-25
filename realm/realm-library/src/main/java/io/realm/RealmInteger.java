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


public final class RealmInteger extends Number implements Comparable<RealmInteger> {
    private long value;

    /**
     * Create a new realm integer, with the specified initial value.
     *
     * @param value initial value
     */
    public RealmInteger(long value) {
        this.value = value;
    }

    /**
     * Create a new realm integer, with the specified initial value.
     *
     * @param value initial value: parsed by {@code Long.parseLong}
     */
    public RealmInteger(String value) {
        this.value = Long.parseLong(value);
    }

    /**
     * Set the RealmInteger value.
     * This obliterates any distributed counters and forces the value.
     *
     * @param newValue new value
     */
    public void set(long newValue) {
        value = newValue;
    }

    /**
     * Increment the CRDT counter by adding the value of the argument.
     * Increment/decrement from all devices are reflected in the new value, which is guaranteed to converge.
     *
     * @param inc quantity to be added to the counter
     */
    public void increment(long inc) {
        value += inc;
    }

    /**
     * Decrement the CRDT counter by subtracting the value of the argument.
     * Increment/decrement from all devices are reflected in the new value, which is guaranteed to converge.
     *
     * @param dec quantity to be subtracted from the counter
     */
    public void decrement(long dec) {
        value -= dec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte byteValue() {
        return (byte) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double doubleValue() {
        return (double) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float floatValue() {
        return (float) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short shortValue() {
        return (short) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(RealmInteger o) {
        long otherValue = o.value;
        return (value == otherValue) ? 0 : ((value > otherValue) ? 1 : -1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof RealmInteger)) { return false; }
        RealmInteger other = (RealmInteger) o;
        return other.value == value;
    }
}
