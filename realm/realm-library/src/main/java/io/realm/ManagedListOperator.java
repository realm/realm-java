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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.realm.internal.OsList;
import io.realm.internal.OsObjectStore;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.Table;
import io.realm.internal.Util;

/**
 * This class provides facade for against {@link OsList}. {@link OsList} is used for both {@link RealmModel}s
 * and values, but there are some subtle differences in actual operation.
 * <p>
 * This class provides common interface for them.
 * <p>
 * You need to use appropriate sub-class for underlying field type.
 *
 * @param <T> class of element which is returned on read operation.
 */
abstract class ManagedListOperator<T> {
    static final String NULL_OBJECTS_NOT_ALLOWED_MESSAGE = "RealmList does not accept null values.";
    static final String INVALID_OBJECT_TYPE_MESSAGE = "Unacceptable value type. Acceptable: %1$s, actual: %2$s .";

    final BaseRealm realm;
    final OsList osList;
    @Nullable
    final Class<T> clazz;

    ManagedListOperator(BaseRealm realm, OsList osList, @Nullable Class<T> clazz) {
        this.realm = realm;
        this.clazz = clazz;
        this.osList = osList;
    }

    public abstract boolean forRealmModel();

    public final OsList getOsList() {
        return osList;
    }

    public final boolean isValid() {
        return osList.isValid();
    }

    public final int size() {
        final long actualSize = osList.size();
        return actualSize < Integer.MAX_VALUE ? (int) actualSize : Integer.MAX_VALUE;
    }

    public final boolean isEmpty() {
        return osList.isEmpty();
    }

    protected abstract void checkValidValue(@Nullable Object value);

    @Nullable
    public abstract T get(int index);

    public final void append(@Nullable Object value) {
        checkValidValue(value);

        if (value == null) {
            appendNull();
        } else {
            appendValue(value);
        }
    }

    private void appendNull() {
        osList.addNull();
    }

    protected abstract void appendValue(Object value);

    public final void insert(int index, @Nullable Object value) {
        checkValidValue(value);

        if (value == null) {
            insertNull(index);
        } else {
            insertValue(index, value);
        }

    }

    protected void insertNull(int index) {
        osList.insertNull(index);
    }

    protected abstract void insertValue(int index, Object value);

    @Nullable
    public final T set(int index, @Nullable Object value) {
        checkValidValue(value);

        //noinspection unchecked
        final T oldObject = get(index);
        if (value == null) {
            setNull(index);
        } else {
            setValue(index, value);
        }
        return oldObject;
    }

    protected void setNull(int index) {
        osList.setNull(index);
    }

    protected abstract void setValue(int index, Object value);

    final void move(int oldPos, int newPos) {
        osList.move(oldPos, newPos);
    }

    final void remove(int index) {
        osList.remove(index);
    }

    final void removeAll() {
        osList.removeAll();
    }

    final void delete(int index) {
        osList.delete(index);
    }

    final void deleteLast() {
        osList.delete(osList.size() - 1);
    }

    final void deleteAll() {
        osList.deleteAll();
    }

}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@link RealmModel} list field.
 */
final class RealmModelListOperator<T> extends ManagedListOperator<T> {

    @Nullable
    private final String className;

    RealmModelListOperator(BaseRealm realm, OsList osList, @Nullable Class<T> clazz, @Nullable String className) {
        super(realm, osList, clazz);
        this.className = className;
    }

    @Override
    public boolean forRealmModel() {
        return true;
    }

    @Override
    public T get(int index) {
        //noinspection unchecked
        return (T) realm.get((Class<? extends RealmModel>) clazz, className, osList.getUncheckedRow(index));
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            throw new IllegalArgumentException(NULL_OBJECTS_NOT_ALLOWED_MESSAGE);
        }
        if (!(value instanceof RealmModel)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.lang.String",
                            value.getClass().getName()));
        }
    }

    private void checkInsertIndex(int index) {
        final int size = size();
        if (index < 0 || size < index) {
            throw new IndexOutOfBoundsException("Invalid index " + index + ", size is " + osList.size());
        }
    }

    @Override
    public void appendValue(Object value) {
        RealmModel realmObject = (RealmModel) value;
        boolean copyObject = checkCanObjectBeCopied(realm, realmObject);
        if (isEmbedded((RealmModel) value)) {
            if (value instanceof DynamicRealmObject) {
                throw new IllegalArgumentException("Embedded objects are not supported by RealmLists of DynamicRealmObjects yet.");
            }
            long objKey = osList.createAndAddEmbeddedObject();
            updateEmbeddedObject(realmObject, objKey);
        } else {
            RealmObjectProxy proxy = (RealmObjectProxy) ((copyObject) ? copyToRealm((RealmModel) value) : realmObject);
            osList.addRow(proxy.realmGet$proxyState().getRow$realm().getObjectKey());
        }
    }

    @Override
    protected void insertNull(int index) {
        throw new RuntimeException("Should not reach here.");
    }

    @Override
    public void insertValue(int index, Object value) {
        // need to check in advance to avoid unnecessary copy of unmanaged object into Realm.
        checkInsertIndex(index);
        RealmModel realmObject = (RealmModel) value;
        boolean copyObject = checkCanObjectBeCopied(realm, realmObject);
        if (isEmbedded(realmObject)) {
            if (value instanceof DynamicRealmObject) {
                throw new IllegalArgumentException("Embedded objects are not supported by RealmLists of DynamicRealmObjects yet.");
            }
            long objKey = osList.createAndAddEmbeddedObject(index);
            updateEmbeddedObject(realmObject, objKey);
        } else {
            RealmObjectProxy proxy = (RealmObjectProxy) ((copyObject) ? copyToRealm((RealmModel) value) : realmObject);
            osList.insertRow(index, proxy.realmGet$proxyState().getRow$realm().getObjectKey());
        }
    }

    private boolean isEmbedded(RealmModel value) {
        if (realm instanceof Realm) {
            return realm.getSchema().getSchemaForClass(value.getClass()).isEmbedded();
        } else {
            String objectType = ((DynamicRealmObject) value).getType();
            return realm.getSchema().getSchemaForClass(objectType).isEmbedded();
        }
    }

    @Override
    protected void setNull(int index) {
        throw new RuntimeException("Should not reach here.");
    }

    @Override
    protected void setValue(int index, Object value) {
        RealmModel realmObject = (RealmModel) value;
        boolean copyObject = checkCanObjectBeCopied(realm, realmObject);
        if (isEmbedded(realmObject)) {
            if (value instanceof DynamicRealmObject) {
                throw new IllegalArgumentException("Embedded objects are not supported by RealmLists of DynamicRealmObjects yet.");
            }
            long objKey = osList.createAndSetEmbeddedObject(index);
            updateEmbeddedObject(realmObject, objKey);
        } else {
            RealmObjectProxy proxy = (RealmObjectProxy) ((copyObject) ? copyToRealm((RealmModel) value) : realmObject);
            osList.setRow(index, proxy.realmGet$proxyState().getRow$realm().getObjectKey());
        }
    }

    private boolean checkCanObjectBeCopied(BaseRealm realm, RealmModel object) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;

            if (proxy instanceof DynamicRealmObject) {
                //noinspection ConstantConditions
                @Nonnull
                String listClassName = className;
                if (proxy.realmGet$proxyState().getRealm$realm() == realm) {
                    String objectClassName = ((DynamicRealmObject) object).getType();
                    if (listClassName.equals(objectClassName)) {
                        // Same Realm instance and same target table
                        return false;
                    } else {
                        // Different target table
                        throw new IllegalArgumentException(String.format(Locale.US,
                                "The object has a different type from list's." +
                                        " Type of the list is '%s', type of object is '%s'.", listClassName, objectClassName));
                    }
                } else if (realm.threadId == proxy.realmGet$proxyState().getRealm$realm().threadId) {
                    // We don't support moving DynamicRealmObjects across Realms automatically. The overhead is too big as
                    // you have to run a full schema validation for each object.
                    // And copying from another Realm instance pointed to the same Realm file is not supported as well.
                    throw new IllegalArgumentException("Cannot copy DynamicRealmObject between Realm instances.");
                } else {
                    throw new IllegalStateException("Cannot copy an object to a Realm instance created in another thread.");
                }
            } else {
                // Object is already in this realm
                if (proxy.realmGet$proxyState().getRow$realm() != null && proxy.realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    if (realm != proxy.realmGet$proxyState().getRealm$realm()) {
                        throw new IllegalArgumentException("Cannot copy an object from another Realm instance.");
                    }
                    return false;
                }
            }
        }
        return true;
    }

    // Transparently copies an unmanaged object or managed object from another Realm to the Realm backing this RealmList.
    private <E extends RealmModel> E copyToRealm(E object) {
        // At this point the object can only be a typed object, so the backing Realm cannot be a DynamicRealm.
        Realm realm = (Realm) this.realm;
        if (OsObjectStore.getPrimaryKeyForObject(realm.getSharedRealm(),
                realm.getConfiguration().getSchemaMediator().getSimpleClassName(object.getClass())) != null) {
            return realm.copyToRealmOrUpdate(object);
        } else {
            return realm.copyToRealm(object);
        }
    }

    private void updateEmbeddedObject(RealmModel unmanagedObject, long objKey) {
        RealmProxyMediator schemaMediator = realm.getConfiguration().getSchemaMediator();
        Class<? extends RealmModel> modelClass = Util.getOriginalModelClass(unmanagedObject.getClass());
        Table table = ((Realm) realm).getTable(modelClass);
        RealmModel managedObject = schemaMediator.newInstance(modelClass, realm, table.getUncheckedRow(objKey), realm.getSchema().getColumnInfo(modelClass), true, Collections.EMPTY_LIST);
        schemaMediator.updateEmbeddedObject((Realm) realm, unmanagedObject, managedObject, new HashMap<>(), Collections.EMPTY_SET);
    }

}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@link String} list field.
 */
final class StringListOperator extends ManagedListOperator<String> {

    StringListOperator(BaseRealm realm, OsList osList, Class<String> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public String get(int index) {
        return (String) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.lang.String",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addString((String) value);
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertString(index, (String) value);
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setString(index, (String) value);
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@code long} list field.
 */
final class LongListOperator<T> extends ManagedListOperator<T> {

    LongListOperator(BaseRealm realm, OsList osList, Class<T> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public T get(int index) {
        final Long value = (Long) osList.getValue(index);
        if (value == null) {
            return null;
        }
        if (clazz == Long.class) {
            //noinspection unchecked
            return (T) value;
        }
        if (clazz == Integer.class) {
            //noinspection unchecked,UnnecessaryBoxing,ConstantConditions
            return clazz.cast(Integer.valueOf(value.intValue()));
        }
        if (clazz == Short.class) {
            //noinspection unchecked,UnnecessaryBoxing,ConstantConditions
            return clazz.cast(Short.valueOf(value.shortValue()));
        }
        if (clazz == Byte.class) {
            //noinspection unchecked,UnnecessaryBoxing,ConstantConditions
            return clazz.cast(Byte.valueOf(value.byteValue()));
        }
        //noinspection ConstantConditions
        throw new IllegalStateException("Unexpected element type: " + clazz.getName());
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.lang.Long, java.lang.Integer, java.lang.Short, java.lang.Byte",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addLong(((Number) value).longValue());
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertLong(index, ((Number) value).longValue());
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setLong(index, ((Number) value).longValue());
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@code boolean} list field.
 */
final class BooleanListOperator extends ManagedListOperator<Boolean> {

    BooleanListOperator(BaseRealm realm, OsList osList, Class<Boolean> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public Boolean get(int index) {
        return (Boolean) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.lang.Boolean",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addBoolean((Boolean) value);
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertBoolean(index, (Boolean) value);
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setBoolean(index, (Boolean) value);
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@code byte[]} list field.
 */
final class BinaryListOperator extends ManagedListOperator<byte[]> {

    BinaryListOperator(BaseRealm realm, OsList osList, Class<byte[]> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public byte[] get(int index) {
        return (byte[]) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof byte[])) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "byte[]",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addBinary((byte[]) value);
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertBinary(index, (byte[]) value);
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setBinary(index, (byte[]) value);
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@code double} list field.
 */
final class DoubleListOperator extends ManagedListOperator<Double> {

    DoubleListOperator(BaseRealm realm, OsList osList, Class<Double> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public Double get(int index) {
        return (Double) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.lang.Number",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addDouble(((Number) value).doubleValue());
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertDouble(index, ((Number) value).doubleValue());
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setDouble(index, ((Number) value).doubleValue());
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@code float} list field.
 */
final class FloatListOperator extends ManagedListOperator<Float> {

    FloatListOperator(BaseRealm realm, OsList osList, Class<Float> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public Float get(int index) {
        return (Float) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.lang.Number",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addFloat(((Number) value).floatValue());
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertFloat(index, ((Number) value).floatValue());
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setFloat(index, ((Number) value).floatValue());
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@link Date} list field.
 */
final class DateListOperator extends ManagedListOperator<Date> {

    DateListOperator(BaseRealm realm, OsList osList, Class<Date> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public Date get(int index) {
        return (Date) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof Date)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "java.util.Date",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addDate((Date) value);
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertDate(index, (Date) value);
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setDate(index, (Date) value);
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@link Decimal128} list field.
 */
final class Decimal128ListOperator extends ManagedListOperator<Decimal128> {

    Decimal128ListOperator(BaseRealm realm, OsList osList, Class<Decimal128> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public Decimal128 get(int index) {
        return (Decimal128) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof Decimal128)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "org.bson.types.Decimal128",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addDecimal128((Decimal128) value);
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertDecimal128(index, (Decimal128) value);
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setDecimal128(index, (Decimal128) value);
    }
}

/**
 * A subclass of {@link ManagedListOperator} that deal with {@link ObjectId} list field.
 */
final class ObjectIdListOperator extends ManagedListOperator<ObjectId> {

    ObjectIdListOperator(BaseRealm realm, OsList osList, Class<ObjectId> clazz) {
        super(realm, osList, clazz);
    }

    @Override
    public boolean forRealmModel() {
        return false;
    }

    @Nullable
    @Override
    public ObjectId get(int index) {
        return (ObjectId) osList.getValue(index);
    }

    @Override
    protected void checkValidValue(@Nullable Object value) {
        if (value == null) {
            // null is always valid (but schema may reject null on insertion).
            return;
        }
        if (!(value instanceof ObjectId)) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, INVALID_OBJECT_TYPE_MESSAGE,
                            "org.bson.types.ObjectId",
                            value.getClass().getName()));
        }
    }

    @Override
    public void appendValue(Object value) {
        osList.addObjectId((ObjectId) value);
    }

    @Override
    public void insertValue(int index, Object value) {
        osList.insertObjectId(index, (ObjectId) value);
    }

    @Override
    protected void setValue(int index, Object value) {
        osList.setObjectId(index, (ObjectId) value);
    }
}
