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

import javax.annotation.Nullable;

import io.realm.internal.NativeContext;
import io.realm.internal.core.NativeMixed;


/**
 * {@link Mixed} is used to represent a polymorphic Realm value.
 * It has two modes: a managed and unmanaged mode. In managed mode contents are persisted inside a Realm, in
 * unmanaged mode contents are persisted in the object instance.
 * <p>
 * Only Realm can create managed Mixed. Managed Mixed will automatically update the content whenever the
 * underlying Realm is updated, and can only be accessed using the getter of a {@link io.realm.Mixed}.
 * <p>
 * Unmanaged Mixed can be created by the user and can contain any Realm value, or both managed and unmanaged RealmObjects.
 */
public class Mixed {

    private final MixedOperator operator;

    Mixed(MixedOperator operator) {
        this.operator = operator;
    }

    long getNativePtr() {
        return this.operator.getNativeMixed(NativeContext.dummyContext).getNativePtr();
    }

    /**
     * Gets the inner type of this Mixed object.
     *
     * @return the inner MixedType
     */
    @Nullable
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
        return this.operator.getType().getTypedClass();
    }
//    /**
//     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
//     * If the value is not null the type will be {@link MixedType#INTEGER}, {@link MixedType#NULL} otherwise.
//     *
//     * @param value initial value
//     * @return a new, unmanaged {@link Mixed} of a Byte
//     */
//    public static Mixed valueOf(@Nullable Byte value) {
//        return new Mixed(value, MixedType.INTEGER);
//    }
//
//    /**
//     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
//     * If the value is not null the type will be {@link MixedType#INTEGER}, {@link MixedType#NULL} otherwise.
//     *
//     * @param value initial value
//     * @return a new, unmanaged {@link Mixed} of a Short
//     */
//    public static Mixed valueOf(@Nullable Short value) {
//        return new Unmanaged(value, MixedType.INTEGER);
//    }
//
//    /**
//     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
//     * If the value is not null the type will be {@link MixedType#INTEGER}, {@link MixedType#NULL} otherwise.
//     *
//     * @param value initial value
//     * @return a new, unmanaged {@link Mixed} of a Integer
//     */
//    public static Mixed valueOf(@Nullable Integer value) {
//        return new Unmanaged(value, MixedType.INTEGER);
//    }
//
//    /**
//     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
//     * If the value is not null the type will be {@link MixedType#INTEGER}, {@link MixedType#NULL} otherwise.
//     *
//     * @param value initial value
//     * @return a new, unmanaged {@link Mixed} of a Long
//     */
//    public static Mixed valueOf(@Nullable Long value) {
//        return new Unmanaged(value, MixedType.INTEGER);
//    }
//

    /**
     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
     * If the value is not null the type will be {@link MixedType#BOOLEAN}, {@link MixedType#NULL} otherwise.
     *
     * @param value initial value
     * @return a new, unmanaged {@link Mixed} of a Boolean
     */
    public static Mixed valueOf(@Nullable Boolean value) {
        return new Mixed((value == null) ? new NullMixedOperator() : new BooleanMixedOperator(value));
    }

//    /**
//     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
//     * If the value is not null the type will be {@link MixedType#FLOAT}, {@link MixedType#NULL} otherwise.
//     *
//     * @param value initial value
//     * @return a new, unmanaged {@link Mixed} of a Float
//     */
//    public static Mixed valueOf(@Nullable Float value) {
//        return new Unmanaged(value, MixedType.FLOAT);
//    }
//
//    /**
//     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
//     * If the value is not null the type will be {@link MixedType#DOUBLE}, {@link MixedType#NULL} otherwise.
//     *
//     * @param value initial value
//     * @return a new, unmanaged {@link Mixed} of a Double
//     */
//    public static Mixed valueOf(@Nullable Double value) {
//        return new Unmanaged(value, MixedType.DOUBLE);
//    }
//
//    /**
//     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
//     * If the value is not null the type will be {@link MixedType#STRING}, {@link MixedType#NULL} otherwise.
//     *
//     * @param value initial value
//     * @return a new, unmanaged {@link Mixed} of a String
//     */
//    public static Mixed valueOf(@Nullable String value) {
//        return new Unmanaged(value, MixedType.STRING);
//    }
//
//    /**
//     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
//     * If the value is not null the type will be {@link MixedType#BINARY}, {@link MixedType#NULL} otherwise.
//     *
//     * @param value initial value
//     * @return a new, unmanaged {@link Mixed} of a byte[]
//     */
//    public static Mixed valueOf(@Nullable byte[] value) {
//        return new Unmanaged(value, MixedType.BINARY);
//    }
//
//    /**
//     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
//     * If the value is not null the type will be {@link MixedType#DATE}, {@link MixedType#NULL} otherwise.
//     *
//     * @param value initial value
//     * @return a new, unmanaged {@link Mixed} of a Date
//     */
//    public static Mixed valueOf(@Nullable Date value) {
//        return new Unmanaged(value, MixedType.DATE);
//    }
//
//    /**
//     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
//     * If the value is not null the type will be {@link MixedType#OBJECT_ID}, {@link MixedType#NULL} otherwise.
//     *
//     * @param value initial value
//     * @return a new, unmanaged {@link Mixed} of an ObjectId
//     */
//    public static Mixed valueOf(@Nullable ObjectId value) {
//        return new Unmanaged(value, MixedType.OBJECT_ID);
//    }
//
//    /**
//     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
//     * If the value is not null the type will be {@link MixedType#DECIMAL128}, {@link MixedType#NULL} otherwise.
//     *
//     * @param value initial value
//     * @return a new, unmanaged {@link Mixed} of a Decimal128
//     */
//    public static Mixed valueOf(@Nullable Decimal128 value) {
//        return new Unmanaged(value, MixedType.DECIMAL128);
//    }
//
//    /**
//     * Creates a new, unmanaged {@link Mixed} with the specified initial value.
//     * If the value is not null the type will be {@link MixedType#UUID}, {@link MixedType#NULL} otherwise.
//     *
//     * @param value initial value
//     * @return a new, unmanaged {@link Mixed} of an UUID
//     */
//    public static Mixed valueOf(@Nullable UUID value) {
//        return new Unmanaged(value, MixedType.UUID);
//    }
//
//    /**
//     * Creates a new, unmanaged {@link Mixed} of a null value
//     *
//     * @return a new, unmanaged {@link Mixed} instance of a null value
//     */
//    public static Mixed nullValue() {
//        return new Unmanaged();
//    }
//
//    public static Mixed valueOf(@Nullable RealmModel value) {
//        return new Unmanaged(value, MixedType.OBJECT);
//    }
//
//    /**
//     * Returns true if the inner value is null, false otherwise.
//     *
//     * @return true if the inner value is null, false otherwise
//     */
//    public abstract boolean isNull();
//
//    /**
//     * Gets the inner type of this Mixed object.
//     *
//     * @return the inner MixedType
//     */
//    public abstract MixedType getType();
//
//    /**
//     * Gets this value as a Byte if it is one, otherwise throws exception.
//     *
//     * @return a Byte
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public Byte asByte() {
//        Number value = get(Number.class, MixedType.INTEGER);
//        return (value == null) ? null : get(Number.class, MixedType.INTEGER).byteValue();
//    }
//
//    /**
//     * Gets this value as a Short if it is one, otherwise throws exception.
//     *
//     * @return a Short
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public Short asShort() {
//        Number value = get(Number.class, MixedType.INTEGER);
//        return (value == null) ? null : get(Number.class, MixedType.INTEGER).shortValue();
//    }
//
//    /**
//     * Gets this value as a Integer if it is one, otherwise throws exception.
//     *
//     * @return a Integer
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public Integer asInteger() {
//        Number value = get(Number.class, MixedType.INTEGER);
//        return (value == null) ? null : get(Number.class, MixedType.INTEGER).intValue();
//    }
//
//    /**
//     * Gets this value as a Long if it is one, otherwise throws exception.
//     *
//     * @return a Long
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public Long asLong() {
//        Number value = get(Number.class, MixedType.INTEGER);
//        return (value == null) ? null : get(Number.class, MixedType.INTEGER).longValue();
//    }
//

    /**
     * Gets this value as a Boolean if it is one, otherwise throws exception.
     *
     * @return a Boolean
     * @throws java.lang.ClassCastException if this value is not of the expected type
     */
    public Boolean asBoolean() {
        return operator.getValue(Boolean.class);
    }
//
//    /**
//     * Gets this value as a Float if it is one, otherwise throws exception.
//     *
//     * @return a Float
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public Float asFloat() {
//        return get(Float.class, MixedType.FLOAT);
//    }
//
//    /**
//     * Gets this value as a Double if it is one, otherwise throws exception.
//     *
//     * @return a Double
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public Double asDouble() {
//        return get(Double.class, MixedType.DOUBLE);
//    }
//
//    /**
//     * Gets this value as a String if it is one, otherwise throws exception.
//     *
//     * @return a String
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public String asString() {
//        return get(String.class, MixedType.STRING);
//    }
//
//    /**
//     * Gets this value as a byte[] if it is one, otherwise throws exception.
//     *
//     * @return a byte[]
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public byte[] asBinary() {
//        return get(byte[].class, MixedType.BINARY);
//    }
//
//    /**
//     * Gets this value as a Date if it is one, otherwise throws exception.
//     *
//     * @return a Date
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public Date asDate() {
//        return get(Date.class, MixedType.DATE);
//    }
//
//    /**
//     * Gets this value as a ObjectId if it is one, otherwise throws exception.
//     *
//     * @return an ObjectId
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public ObjectId asObjectId() {
//        return get(ObjectId.class, MixedType.OBJECT_ID);
//    }
//
//    /**
//     * Gets this value as a UUID if it is one, otherwise throws exception.
//     *
//     * @return an UUID
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public UUID asUUID() {
//        return get(UUID.class, MixedType.UUID);
//    }
//
//    /**
//     * Gets this value as a Decimal128 if it is one, otherwise throws exception.
//     *
//     * @return a Decimal128
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public Decimal128 asDecimal128() {
//        return get(Decimal128.class, MixedType.DECIMAL128);
//    }
//
//    /**
//     * Gets this value as a RealmModel if it is one, otherwise throws exception.
//     *
//     * @param <T> the RealmModel type to cast the inner value to
//     * @return a RealmModel of the T type
//     * @throws java.lang.ClassCastException if this value is not of the expected type
//     */
//    public <T extends RealmModel> T asRealmModel(Class<T> clazz) {
//        return get(clazz, MixedType.OBJECT);
//    }
}
