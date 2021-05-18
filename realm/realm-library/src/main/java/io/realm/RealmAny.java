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

import java.util.Date;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static io.realm.RealmFieldTypeConstants.MAX_CORE_TYPE_VALUE;


/**
 * {@link io.realm.RealmAny} is used to represent a polymorphic Realm value.
 * <p>
 * At any particular moment an instance of this class stores a
 * definite value of a definite type. If, for instance, that is an
 * double value, you may call asDouble() to extract that value. You
 * may call getType() to discover what type of value is currently
 * stored. Calling asDouble() on an instance that does not store an
 * double would raise a {@link java.lang.ClassCastException}.
 * <p>
 * RealmAny behaves like a value type on all the supported types except on
 * Realm objects. It means that Realm will not persist any change to the
 * RealmAny value except when the type is Realm object. When a RealmAny
 * holds a Realm object, it just holds the reference to it, not a copy of
 * the object. So modifications to the Realm object are reflected in the
 * RealmAny value, including if the object is deleted. Because RealmAny
 * instances are immutable, a new instance is needed to update a RealmAny
 * attribute.
 * <pre>
 * <code>
 *      anObject.realmAnyAttribute = RealmAny.valueOf(5);
 *      anObject.realmAnyAttribute = RealmAny.valueOf(10.f);
 * </code>
 * </pre>
 * It is crucial to understand that the act of extracting a value of
 * a particular type requires definite knowledge about the stored
 * type. Calling a getter method for any particular type, that is not
 * the same type as the stored value, would raise an exception.
 * <p>
 * Our recommendation to handle the RealmAny polymorphism is to write a
 * switch case around the RealmAny type and its inner value class.
 * <pre>
 * <code>
 *      RealmAny realmAny = aRealmObject.realmAnyAttribute;
 *
 *      switch (realmAny.getType()) {
 *          case OBJECT:
 *              if (realmAny.getValueClass().equals(DogRealmModel.class)) {
 *                  DogRealmModel value = realmAny.asRealmModel(DogRealmModel.class);
 *              }
 *          case INTEGER:
 *              performAction(realmAny.asInteger());
 *              break;
 *          case BOOLEAN:
 *              performAction(realmAny.asBoolean());
 *              break;
 *          case STRING:
 *              performAction(realmAny.asString());
 *              break;
 *          case BINARY:
 *              performAction(realmAny.asBinary());
 *              break;
 *          case DATE:
 *              performAction(realmAny.asDate());
 *              break;
 *          case FLOAT:
 *              performAction(realmAny.asFloat());
 *              break;
 *          case DOUBLE:
 *              performAction(realmAny.asDouble());
 *              break;
 *          case DECIMAL128:
 *              performAction(realmAny.asDecimal128());
 *              break;
 *          case OBJECT_ID:
 *              performAction(realmAny.asObjectId());
 *              break;
 *          case UUID:
 *              performAction(realmAny.asUUID());
 *              break;
 *          case NULL:
 *              performNullAction();
 *              break;
 *      }
 * </code>
 * </pre>
 * <p>
 * getValueClass() returns the Java class that represents the inner
 * value wrapped by the RealmAny instance. If the resulting class is
 * a realization of {@link io.realm.RealmModel} asRealmModel() can be
 * called to cast the RealmAny value to a Realm object reference.
 * <p>
 * RealmAny values can also be sorted. The sorting order used between
 * different RealmAny types, from lowest to highest, is:
 * <ol>
 *     <li>Boolean</li>
 *     <li>Byte/Short/Integer/Long/Float/Double/Decimal128</li>
 *     <li>byte[]/String</li>
 *     <li>Date</li>
 *     <li>ObjectId</li>
 *     <li>UUID</li>
 *     <li>RealmObject</li>
 * </ol>
 * This has implications on how {@link RealmQuery#sort(String)},
 * {@link RealmQuery#minRealmAny(String)} and {@link RealmQuery#maxRealmAny(String)}
 * work. Especially {@code min()} and {@code max()} will not only take
 * numeric fields into account, but will use the sorting order to determine
 * the "largest" or "lowest" value.
 */
public class RealmAny {
    @Nonnull
    private final RealmAnyOperator operator;

    RealmAny(@Nonnull RealmAnyOperator operator) {
        this.operator = operator;
    }

    final long getNativePtr() {
        return this.operator.getNativePtr();
    }

    /**
     * Gets the inner type of this RealmAny object.
     *
     * @return the inner RealmAny.Type
     */
    public RealmAny.Type getType() {
        return this.operator.getType();
    }

    /**
     * Returns the Java class that represents the inner value wrapped by this RealmAny value.
     *
     * @return the class that represents the inner value wrapped by this RealmAny value.
     */
    @Nullable
    public Class<?> getValueClass() {
        return this.operator.getTypedClass();
    }

    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#INTEGER}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny containing a Byte value.
     */
    public static RealmAny valueOf(@Nullable Byte value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new IntegerRealmAnyOperator(value));
    }

    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#INTEGER}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of a Short.
     */
    public static RealmAny valueOf(@Nullable Short value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new IntegerRealmAnyOperator(value));
    }

    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#INTEGER}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of a Integer.
     */
    public static RealmAny valueOf(@Nullable Integer value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new IntegerRealmAnyOperator(value));
    }

    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#INTEGER}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of a Long.
     */
    public static RealmAny valueOf(@Nullable Long value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new IntegerRealmAnyOperator(value));
    }


    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#BOOLEAN}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of a Boolean.
     */
    public static RealmAny valueOf(@Nullable Boolean value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new BooleanRealmAnyOperator(value));
    }

    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#FLOAT}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of a Float.
     */
    public static RealmAny valueOf(@Nullable Float value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new FloatRealmAnyOperator(value));
    }

    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#DOUBLE}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of a Double.
     */
    public static RealmAny valueOf(@Nullable Double value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new DoubleRealmAnyOperator(value));
    }

    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#STRING}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of a String.
     */
    public static RealmAny valueOf(@Nullable String value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new StringRealmAnyOperator(value));
    }

    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#BINARY}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of a byte[].
     */
    public static RealmAny valueOf(@Nullable byte[] value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new BinaryRealmAnyOperator(value));
    }

    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#DATE}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of a Date.
     */
    public static RealmAny valueOf(@Nullable Date value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new DateRealmAnyOperator(value));
    }

    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#OBJECT_ID}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of an ObjectId.
     */
    public static RealmAny valueOf(@Nullable ObjectId value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new ObjectIdRealmAnyOperator(value));
    }

    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#DECIMAL128}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of a Decimal128.
     */
    public static RealmAny valueOf(@Nullable Decimal128 value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new Decimal128RealmAnyOperator(value));
    }

    /**
     * Creates a new RealmAny with the specified value.
     * If the value is not null the type will be {@link RealmAny.Type#UUID}, {@link RealmAny.Type#NULL} otherwise.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of an UUID.
     */
    public static RealmAny valueOf(@Nullable UUID value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new UUIDRealmAnyOperator(value));
    }

    /**
     * Creates a new RealmAny of a null value.
     *
     * @return a new RealmAny instance of a null value.
     */
    public static RealmAny nullValue() {
        return new RealmAny(new NullRealmAnyOperator());
    }

    /**
     * Creates a new RealmAny with the specified value.
     *
     * @param value the RealmAny value.
     * @return a new RealmAny of a RealmModel.
     */
    public static RealmAny valueOf(@Nullable RealmModel value) {
        return new RealmAny((value == null) ? new NullRealmAnyOperator() : new RealmModelOperator(value));
    }

    /**
     * Returns true if the inner value is null, false otherwise.
     *
     * @return true if the inner value is null, false otherwise.
     */
    public boolean isNull() {
        return this.getType() == RealmAny.Type.NULL;
    }

    /**
     * Gets this value as a Byte if it is one, otherwise throws exception.
     *
     * @return a Byte.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public Byte asByte() {
        Number value = operator.getValue(Number.class);
        return (value == null) ? null : value.byteValue();
    }

    /**
     * Gets this value as a Short if it is one, otherwise throws exception.
     *
     * @return a Short.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public Short asShort() {
        Number value = operator.getValue(Number.class);
        return (value == null) ? null : value.shortValue();
    }

    /**
     * Gets this value as a Integer if it is one, otherwise throws exception.
     *
     * @return an Integer.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public Integer asInteger() {
        Number value = operator.getValue(Number.class);
        return (value == null) ? null : value.intValue();
    }

    /**
     * Gets this value as a Long if it is one, otherwise throws exception.
     *
     * @return a Long.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public Long asLong() {
        Number value = operator.getValue(Number.class);
        return (value == null) ? null : value.longValue();
    }

    /**
     * Gets this value as a Boolean if it is one, otherwise throws exception.
     *
     * @return a Boolean.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public Boolean asBoolean() {
        return operator.getValue(Boolean.class);
    }

    /**
     * Gets this value as a Float if it is one, otherwise throws exception.
     *
     * @return a Float.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public Float asFloat() {
        return operator.getValue(Float.class);
    }

    /**
     * Gets this value as a Double if it is one, otherwise throws exception.
     *
     * @return a Double.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public Double asDouble() {
        return operator.getValue(Double.class);
    }

    /**
     * Gets this value as a String if it is one, otherwise throws exception.
     *
     * @return a String.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public String asString() {
        return operator.getValue(String.class);
    }

    /**
     * Gets this value as a byte[] if it is one, otherwise throws exception.
     *
     * @return a byte[].
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public byte[] asBinary() {
        return operator.getValue(byte[].class);
    }

    /**
     * Gets this value as a Date if it is one, otherwise throws exception.
     *
     * @return a Date.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public Date asDate() {
        return operator.getValue(Date.class);
    }

    /**
     * Gets this value as a ObjectId if it is one, otherwise throws exception.
     *
     * @return an ObjectId.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public ObjectId asObjectId() {
        return operator.getValue(ObjectId.class);
    }

    /**
     * Gets this value as a UUID if it is one, otherwise throws exception.
     *
     * @return an UUID.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public UUID asUUID() {
        return operator.getValue(UUID.class);
    }

    /**
     * Gets this value as a Decimal128 if it is one, otherwise throws exception.
     *
     * @return a Decimal128.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public Decimal128 asDecimal128() {
        return operator.getValue(Decimal128.class);
    }

    /**
     * Gets this value as a RealmModel if it is one, otherwise throws exception.
     *
     * @param <T> the RealmModel type to cast the inner value to.
     * @return a RealmModel of the T type.
     * @throws java.lang.ClassCastException if this value is not of the expected type.
     */
    public <T extends RealmModel> T asRealmModel(Class<T> clazz) {
        return operator.getValue(clazz);
    }

    /**
     * Creates a new RealmAny out of an Object.
     *
     * @param value initial value.
     * @return a new RealmAny wrapping the object.
     */
    static RealmAny valueOf(@Nullable Object value) {
        if (value == null) {
            return RealmAny.nullValue();
        } else if (value instanceof Boolean) {
            return RealmAny.valueOf((Boolean) value);
        } else if (value instanceof Byte) {
            return RealmAny.valueOf((Byte) value);
        } else if (value instanceof Short) {
            return RealmAny.valueOf((Short) value);
        } else if (value instanceof Integer) {
            return RealmAny.valueOf((Integer) value);
        } else if (value instanceof Long) {
            return RealmAny.valueOf((Long) value);
        } else if (value instanceof Float) {
            return RealmAny.valueOf((Float) value);
        } else if (value instanceof Double) {
            return RealmAny.valueOf((Double) value);
        } else if (value instanceof Decimal128) {
            return RealmAny.valueOf((Decimal128) value);
        } else if (value instanceof String) {
            return RealmAny.valueOf((String) value);
        } else if (value instanceof byte[]) {
            return RealmAny.valueOf((byte[]) value);
        } else if (value instanceof Date) {
            return RealmAny.valueOf((Date) value);
        } else if (value instanceof ObjectId) {
            return RealmAny.valueOf((ObjectId) value);
        } else if (value instanceof UUID) {
            return RealmAny.valueOf((UUID) value);
        } else if (value instanceof RealmAny) {
            return (RealmAny) value;
        } else if (RealmModel.class.isAssignableFrom(value.getClass())) {
            RealmModel model = (RealmModel) value;

            if (!RealmObject.isValid(model) || !RealmObject.isManaged(model)) {
                throw new IllegalArgumentException("RealmObject is not a valid managed object.");
            }

            return RealmAny.valueOf((RealmModel) model);
        } else {
            throw new IllegalArgumentException("Type not supported on RealmAny: " + value.getClass().getSimpleName());
        }
    }

    /**
     * A {@code RealmAny}'s hash code is, exactly, the hash code of its value.
     *
     * @return true if the target has the same value
     * @throws NullPointerException if the inner value is null
     */
    @Override
    public final int hashCode() {
        return this.operator.hashCode();
    }

    /**
     * Two {@code RealmAny}s are {@code .equals} if and only if their contents are equal.
     *
     * @param other compare target
     * @return true if the target has the same value
     */
    @Override
    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
    public final boolean equals(@Nullable Object other) {
        if (other == this) { return true; }
        if (!(other instanceof RealmAny)) { return false; }
        RealmAny otherRealmAny = ((RealmAny) other);
        return this.operator.equals(otherRealmAny.operator);
    }

    public final boolean coercedEquals(@Nullable RealmAny other) {
        if (other == null) { return false; }
        return this.operator.coercedEquals(other.operator);
    }

    @Override
    public String toString() {
        return this.operator.toString();
    }

    void checkValidObject(BaseRealm realm) {
        operator.checkValidObject(realm);
    }

    /**
     * Enum describing all the types supported by RealmAny.
     */
    public enum Type {
        INTEGER(RealmFieldType.INTEGER, Long.class),
        BOOLEAN(RealmFieldType.BOOLEAN, Boolean.class),
        STRING(RealmFieldType.STRING, String.class),
        BINARY(RealmFieldType.BINARY, Byte[].class),
        DATE(RealmFieldType.DATE, Date.class),
        FLOAT(RealmFieldType.FLOAT, Float.class),
        DOUBLE(RealmFieldType.DOUBLE, Double.class),
        DECIMAL128(RealmFieldType.DECIMAL128, Decimal128.class),
        OBJECT_ID(RealmFieldType.OBJECT_ID, ObjectId.class),
        OBJECT(RealmFieldType.TYPED_LINK, RealmModel.class),
        UUID(RealmFieldType.UUID, java.util.UUID.class),
        NULL(null, null);

        private static final Type[] realmFieldToRealmAnyTypeMap = new Type[MAX_CORE_TYPE_VALUE + 2];

        static {
            for (Type realmAnyType : values()) {
                if (realmAnyType == NULL) { continue; }

                final int nativeValue = realmAnyType.realmFieldType.getNativeValue();
                realmFieldToRealmAnyTypeMap[nativeValue] = realmAnyType;
            }
            // TODO: only used for testing purposes, see https://github.com/realm/realm-java/issues/7385
            // Links Object field type to RealmAny object.
            realmFieldToRealmAnyTypeMap[RealmFieldType.OBJECT.getNativeValue()] = OBJECT;
        }

        public static Type fromNativeValue(int realmFieldType) {
            if (realmFieldType == -1) { return NULL; }

            return realmFieldToRealmAnyTypeMap[realmFieldType];
        }

        private final Class<?> clazz;
        private final RealmFieldType realmFieldType;

        Type(@Nullable RealmFieldType realmFieldType, @Nullable Class<?> clazz) {
            this.realmFieldType = realmFieldType;
            this.clazz = clazz;
        }

        public Class<?> getTypedClass() {
            return clazz;
        }
    }
}
