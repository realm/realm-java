/*
 * Copyright 2020 Realm Inc.
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
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * {@link io.realm.Mixed} is used to represent a polymorphic Realm value.
 * <p>
 * At any particular moment an instance of this class stores a
 * definite value of a definite type. If, for instance, that is an
 * double value, you may call asDouble() to extract that value. You
 * may call getType() to discover what type of value is currently
 * stored. Calling asDouble() on an instance that does not store an
 * integer would raise a {@link java.lang.ClassCastException}.
 * <p>
 * It is crucial to understand that the act of extracting a value of
 * a particular type requires definite knowledge about the stored
 * type. Calling a getter method for any particular type, that is not
 * the same type as the stored value, would raise an exception.
 * <p>
 * getValueClass() returns the Java class that represents the inner
 * value wrapped by the Mixed instance. It is useful to know what
 * {@link io.realm.RealmModel} to cast to when calling asRealmModel().
 */

public class Mixed {
    @Nonnull
    private final MixedOperator operator;

    Mixed(@Nonnull MixedOperator operator) {
        this.operator = operator;
    }

    long getNativePtr() {
        return this.operator.getNativePtr();
    }

    /**
     * Gets the inner type of this Mixed object.
     *
     * @return the inner MixedType
     */
    public MixedType getType() {
        return this.operator.getType();
    }

    /**
     * Returns the Java class that represents the inner value wrapped by this Mixed value.
     *
     * @return the class that represents the inner value wrapped by this Mixed value.
     */
    @Nullable
    public Class<?> getValueClass() {
        return this.operator.getTypedClass();
    }

    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#INTEGER}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of a Byte
     */
    public static Mixed valueOf(@Nullable Byte value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new IntegerMixedOperator(value));
    }

    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#INTEGER}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of a Short
     */
    public static Mixed valueOf(@Nullable Short value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new IntegerMixedOperator(value));
    }

    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#INTEGER}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of a Integer
     */
    public static Mixed valueOf(@Nullable Integer value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new IntegerMixedOperator(value));
    }

    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#INTEGER}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of a Long
     */
    public static Mixed valueOf(@Nullable Long value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new IntegerMixedOperator(value));
    }


    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#BOOLEAN}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of a Boolean
     */
    public static Mixed valueOf(@Nullable Boolean value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new BooleanMixedOperator(value));
    }

    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#FLOAT}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of a Float
     */
    public static Mixed valueOf(@Nullable Float value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new FloatMixedOperator(value));
    }

    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#DOUBLE}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of a Double
     */
    public static Mixed valueOf(@Nullable Double value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new DoubleMixedOperator(value));
    }

    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#STRING}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of a String
     */
    public static Mixed valueOf(@Nullable String value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new StringMixedOperator(value));
    }

    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#BINARY}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of a byte[]
     */
    public static Mixed valueOf(@Nullable byte[] value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new BinaryMixedOperator(value));
    }

    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#DATE}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of a Date
     */
    public static Mixed valueOf(@Nullable Date value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new DateMixedOperator(value));
    }

    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#OBJECT_ID}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of an ObjectId
     */
    public static Mixed valueOf(@Nullable ObjectId value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new ObjectIdMixedOperator(value));
    }

    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#DECIMAL128}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of a Decimal128
     */
    public static Mixed valueOf(@Nullable Decimal128 value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new Decimal128MixedOperator(value));
    }

    /**
     * Creates a new Mixed with the specified initial value.
     * If the value is not null the type will be {@link MixedType#UUID}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new Mixed of an UUID
     */
    public static Mixed valueOf(@Nullable UUID value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new UUIDMixedOperator(value));
    }

    /**
     * Creates a new Mixed of a null value
     *
     * @return a new Mixed instance of a null value
     */
    public static Mixed nullValue() {
        return new Mixed(new NullMixedOperator());
    }

    /**
     * Creates a new Mixed with the specified initial value.
     *
     * @param value initial value
     * @return a new Mixed of a RealmModel
     */
    public static Mixed valueOf(@Nullable RealmModel value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new RealmModelOperator(value));
    }

    /**
     * Returns true if the inner value is null, false otherwise.
     *
     * @return true if the inner value is null, false otherwise
     */
    public boolean isNull() {
        return this.getType() == MixedType.NULL;
    }

    /**
     * Gets this value as a Byte if it is one, otherwise throws exception.
     *
     * @return a Byte
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public Byte asByte() {
        Number value = operator.getValue(Number.class);
        return (value == null) ? null : value.byteValue();
    }

    /**
     * Gets this value as a Short if it is one, otherwise throws exception.
     *
     * @return a Short
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public Short asShort() {
        Number value = operator.getValue(Number.class);
        return (value == null) ? null : value.shortValue();
    }

    /**
     * Gets this value as a Integer if it is one, otherwise throws exception.
     *
     * @return a Integer
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public Integer asInteger() {
        Number value = operator.getValue(Number.class);
        return (value == null) ? null : value.intValue();
    }

    /**
     * Gets this value as a Long if it is one, otherwise throws exception.
     *
     * @return a Long
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public Long asLong() {
        Number value = operator.getValue(Number.class);
        return (value == null) ? null : value.longValue();
    }

    /**
     * Gets this value as a Boolean if it is one, otherwise throws exception.
     *
     * @return a Boolean
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public Boolean asBoolean() {
        return operator.getValue(Boolean.class);
    }

    /**
     * Gets this value as a Float if it is one, otherwise throws exception.
     *
     * @return a Float
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public Float asFloat() {
        return operator.getValue(Float.class);
    }

    /**
     * Gets this value as a Double if it is one, otherwise throws exception.
     *
     * @return a Double
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public Double asDouble() {
        return operator.getValue(Double.class);
    }

    /**
     * Gets this value as a String if it is one, otherwise throws exception.
     *
     * @return a String
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public String asString() {
        return operator.getValue(String.class);
    }

    /**
     * Gets this value as a byte[] if it is one, otherwise throws exception.
     *
     * @return a byte[]
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public byte[] asBinary() {
        return operator.getValue(byte[].class);
    }

    /**
     * Gets this value as a Date if it is one, otherwise throws exception.
     *
     * @return a Date
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public Date asDate() {
        return operator.getValue(Date.class);
    }

    /**
     * Gets this value as a ObjectId if it is one, otherwise throws exception.
     *
     * @return an ObjectId
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public ObjectId asObjectId() {
        return operator.getValue(ObjectId.class);
    }

    /**
     * Gets this value as a UUID if it is one, otherwise throws exception.
     *
     * @return an UUID
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public UUID asUUID() {
        return operator.getValue(UUID.class);
    }

    /**
     * Gets this value as a Decimal128 if it is one, otherwise throws exception.
     *
     * @return a Decimal128
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public Decimal128 asDecimal128() {
        return operator.getValue(Decimal128.class);
    }


    /**
     * Gets this value as a RealmModel if it is one, otherwise throws exception.
     *
     * @param <T> the RealmModel type to cast the inner value to
     * @return a RealmModel of the T type
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public <T extends RealmModel> T asRealmModel(Class<T> clazz) {
        return operator.getValue(clazz);
    }


    /**
     * A {@code Mixed}'s hash code is, exactly, the hash code of its value.
     *
     * @return true if the target has the same value
     * @throws NullPointerException if the inner value is null
     */
    @Override
    public final int hashCode() {
        return this.operator.hashCode();
    }

    /**
     * Two {@code Mixed}s are {@code .equals} if and only if their contents are equal.
     *
     * @param other compare target
     * @return true if the target has the same value
     */
    @Override
    public final boolean equals(Object other) {
        if (other == this) { return true; }
        if (other == null) { return this.operator instanceof NullMixedOperator; }
        if (!(other instanceof Mixed)) { return false; }
        Mixed otherMixed = ((Mixed) other);
        return this.operator.equals(otherMixed.operator);
    }
}
