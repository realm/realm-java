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

import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.realm.internal.OsList;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.android.JsonUtils;


class ProxyUtils {

    /**
     * Called by proxy to set the managed {@link RealmList} according to the given {@link JSONObject}.
     *
     * @param realmList  the managed {@link RealmList}.
     * @param jsonObject the {@link JSONObject} which may contain the data of the list to be set.
     * @param fieldName  the field name of the {@link RealmList}.
     * @param <E>        type of the {@link RealmList}.
     * @throws JSONException if it fails to parse JSON.
     */
    static <E> void setRealmListWithJsonObject(Realm realm,
            RealmList<E> realmList, JSONObject jsonObject, String fieldName, boolean update) throws JSONException {
        if (!jsonObject.has(fieldName)) {
            return;
        }

        OsList osList = realmList.getOsList();
        if (jsonObject.isNull(fieldName)) {
            osList.removeAll();
            return;
        }

        JSONArray jsonArray = jsonObject.getJSONArray(fieldName);
        osList.removeAll();
        int arraySize = jsonArray.length();

        if (realmList.clazz == Boolean.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                } else {
                    osList.addBoolean(jsonArray.getBoolean(i));
                }
            }
        } else if (realmList.clazz == Float.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                } else {
                    osList.addFloat((float) jsonArray.getDouble(i));
                }
            }
        } else if (realmList.clazz == Double.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                } else {
                    osList.addDouble(jsonArray.getDouble(i));
                }
            }
        } else if (realmList.clazz == String.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                } else {
                    osList.addString(jsonArray.getString(i));
                }
            }
        } else if (realmList.clazz == byte[].class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                } else {
                    osList.addBinary(JsonUtils.stringToBytes(jsonArray.getString(i)));
                }
            }
        } else if (realmList.clazz == Date.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                    continue;
                }

                Object timestamp = jsonArray.get(i);
                if (timestamp instanceof String) {
                    osList.addDate(JsonUtils.stringToDate((String) timestamp));
                } else {
                    osList.addDate(new Date(jsonArray.getLong(i)));
                }
            }
        } else if (realmList.clazz == ObjectId.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                    continue;
                }

                Object id = jsonArray.get(i);
                if (id instanceof String) {
                    osList.addObjectId(new ObjectId((String) id));
                } else {
                    osList.addObjectId((ObjectId) id);
                }
            }
        } else if (realmList.clazz == Decimal128.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                    continue;
                }

                Object decimal = jsonArray.get(i);

                if (decimal instanceof org.bson.types.Decimal128) {
                    osList.addDecimal128((org.bson.types.Decimal128) decimal);
                } else if (decimal instanceof String) {
                    osList.addDecimal128(org.bson.types.Decimal128.parse((String) decimal));
                } else if (decimal instanceof Integer) {
                    osList.addDecimal128(new org.bson.types.Decimal128((Integer) (decimal)));
                } else if (decimal instanceof Long) {
                    osList.addDecimal128(new org.bson.types.Decimal128((Long) (decimal)));
                } else if (decimal instanceof Double) {
                    osList.addDecimal128(new org.bson.types.Decimal128(new java.math.BigDecimal((Double) (decimal))));
                } else {
                    osList.addDecimal128((Decimal128) decimal);
                }
            }
        } else if (realmList.clazz == UUID.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                    continue;
                }

                Object uuid = jsonArray.get(i);
                if (uuid instanceof java.util.UUID) {
                    osList.addUUID((java.util.UUID) uuid);
                } else {
                    osList.addUUID(java.util.UUID.fromString((String)uuid));
                }
            }
        } else if (realmList.clazz == Mixed.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                    continue;
                }

                Object value = jsonArray.get(i);
                Mixed mixed;
                if (value instanceof String) {
                    mixed = Mixed.valueOf((String) value);
                } else if (value instanceof Integer) {
                    mixed = Mixed.valueOf((Integer) value);
                } else if (value instanceof Long) {
                    mixed = Mixed.valueOf((Long) value);
                } else if (value instanceof Double) {
                    mixed = Mixed.valueOf((Double) value);
                } else if (value instanceof Boolean) {
                    mixed = Mixed.valueOf((Boolean) value);
                } else if (value instanceof Mixed) {
                    mixed = (io.realm.Mixed) value;
                    mixed = ProxyUtils.copyOrUpdate(mixed, realm, update, new HashMap<>(), new HashSet<>());
                } else {
                    throw new IllegalArgumentException(String.format("Unsupported JSON type: %s", value.getClass().getSimpleName()));
                }
                osList.addMixed(mixed.getNativePtr());
            }
        } else if (realmList.clazz == Long.class || realmList.clazz == Integer.class ||
                realmList.clazz == Short.class || realmList.clazz == Byte.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                } else {
                    osList.addLong(jsonArray.getLong(i));
                }
            }
        } else {
            throwWrongElementType(realmList.clazz);
        }
    }

    /**
     * Called by proxy to create an unmanaged {@link RealmList} according to the given {@link JsonReader}.
     *
     * @param elementClass the type of the {@link RealmList}.
     * @param jsonReader   the JSON stream to be parsed which may contain the data of the list to be set.
     * @param <E>          type of the {@link RealmList}.
     * @throws IOException if it fails to parse JSON stream.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static <E> RealmList<E> createRealmListWithJsonStream(Class<E> elementClass, JsonReader jsonReader) throws IOException {

        if (jsonReader.peek() == null) {
            jsonReader.skipValue();
            return null;
        }

        jsonReader.beginArray();
        RealmList realmList = new RealmList<E>();

        if (elementClass == Boolean.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add(jsonReader.nextBoolean());
                }
            }
        } else if (elementClass == Float.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add((float) jsonReader.nextDouble());
                }
            }
        } else if (elementClass == Double.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add(jsonReader.nextDouble());
                }
            }
        } else if (elementClass == String.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add(jsonReader.nextString());
                }
            }
        } else if (elementClass == byte[].class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add(JsonUtils.stringToBytes(jsonReader.nextString()));
                }
            }
        } else if (elementClass == Date.class) {
            while (jsonReader.hasNext()) {
                JsonToken token = jsonReader.peek();
                if (token == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else if (token == JsonToken.NUMBER) {
                    realmList.add(new Date(jsonReader.nextLong()));
                } else {
                    realmList.add(JsonUtils.stringToDate(jsonReader.nextString()));
                }
            }
        } else if (elementClass == Long.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add(jsonReader.nextLong());
                }
            }
        } else if (elementClass == Integer.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add((int) jsonReader.nextLong());
                }
            }
        } else if (elementClass == Short.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add((short) jsonReader.nextLong());
                }
            }
        } else if (elementClass == Byte.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add((byte) jsonReader.nextLong());
                }
            }
        } else if (elementClass == ObjectId.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add(new ObjectId(jsonReader.nextString()));
                }
            }
        } else if (elementClass == Decimal128.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add(org.bson.types.Decimal128.parse(jsonReader.nextString()));
                }
            }
        } else if (elementClass == UUID.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add(java.util.UUID.fromString(jsonReader.nextString()));
                }
            }
        } else if (elementClass == Mixed.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(Mixed.nullValue());
                } else if (jsonReader.peek() == JsonToken.STRING) {
                    realmList.add(Mixed.valueOf(jsonReader.nextString()));
                } else if (jsonReader.peek() == JsonToken.NUMBER) {
                    String value = jsonReader.nextString();
                    if (value.contains(".")) {
                        realmList.add(Mixed.valueOf(Double.parseDouble(value)));
                    } else {
                        realmList.add(Mixed.valueOf(Long.parseLong(value)));
                    }
                } else if (jsonReader.peek() == JsonToken.BOOLEAN) {
                    realmList.add(Mixed.valueOf(jsonReader.nextBoolean()));
                }
            }
        } else {
            throwWrongElementType(elementClass);
        }

        jsonReader.endArray();

        return realmList;
    }

    private static void throwWrongElementType(@Nullable Class clazz) {
        throw new IllegalArgumentException(String.format(Locale.ENGLISH, "Element type '%s' is not handled.",
                clazz));
    }

    @Nullable
    static <T extends RealmModel> Mixed copyToRealmIfNeeded(ProxyState<T> proxyState, @Nullable Mixed value) {
        final Realm realm = (Realm) proxyState.getRealm$realm();

        if ((value != null) && (value.getType() == MixedType.OBJECT)) {
            RealmModel mixedRealmModel = value.asRealmModel(RealmModel.class);

            if (realm.getSchema().getSchemaForClass(mixedRealmModel.getClass()).isEmbedded()) {
                throw new IllegalArgumentException("Embedded objects are not supported by Mixed.");
            }

            if (!RealmObject.isManaged(mixedRealmModel)) {
                if (realm.hasPrimaryKey(mixedRealmModel.getClass())) {
                    value = Mixed.valueOf(realm.copyToRealmOrUpdate(mixedRealmModel));
                } else {
                    value = Mixed.valueOf(realm.copyToRealm(mixedRealmModel));
                }
            } else {
                proxyState.checkValidObject(mixedRealmModel);
            }
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    static Mixed copyOrUpdate(Mixed mixed, @Nonnull Realm realm, boolean update, @Nonnull Map<RealmModel, RealmObjectProxy> cache, @Nonnull Set<ImportFlag> flags) {
        if (mixed == null) {
            return Mixed.nullValue();
        }

        if (mixed.getType() == MixedType.OBJECT) {
            Class<? extends RealmModel> mixedValueClass = (Class<? extends RealmModel>) mixed.getValueClass();
            RealmModel mixedRealmObject = mixed.asRealmModel(mixedValueClass);

            RealmObjectProxy cacheRealmObject = cache.get(mixedRealmObject);
            if (cacheRealmObject != null) {
                mixed = Mixed.valueOf(cacheRealmObject);
            } else {
                RealmModel managedMixedRealmObject = realm
                        .getConfiguration()
                        .getSchemaMediator()
                        .copyOrUpdate(realm, mixedRealmObject, update, cache, flags);

                mixed = Mixed.valueOf(managedMixedRealmObject);
            }
        }

        return mixed;
    }

    @SuppressWarnings("unchecked")
    static Mixed insert(Mixed mixed, @Nonnull Realm realm, @Nonnull Map<RealmModel, Long> cache) {
        if (mixed == null) {
            return Mixed.nullValue();
        }

        if (mixed.getType() == MixedType.OBJECT) {
            Class<? extends RealmModel> mixedValueClass = (Class<? extends RealmModel>) mixed.getValueClass();
            RealmModel mixedRealmObject = mixed.asRealmModel(mixedValueClass);

            Long cacheRealmObject = cache.get(mixedRealmObject);
            if (cacheRealmObject != null) {
                mixed = Mixed.valueOf(cacheRealmObject);
            } else {
                long index = realm.getConfiguration()
                        .getSchemaMediator()
                        .insert(realm, mixedRealmObject, cache);

                RealmModel realmModel = realm.get(mixedValueClass, null, index);

                mixed = Mixed.valueOf(realmModel);
            }
        }

        return mixed;
    }

    @SuppressWarnings("unchecked")
    static Mixed insertOrUpdate(Mixed mixed, @Nonnull Realm realm, @Nonnull Map<RealmModel, Long> cache) {
        if (mixed == null) {
            return Mixed.nullValue();
        }

        if (mixed.getType() == MixedType.OBJECT) {
            Class<? extends RealmModel> mixedValueClass = (Class<? extends RealmModel>) mixed.getValueClass();
            RealmModel mixedRealmObject = mixed.asRealmModel(mixedValueClass);

            Long cacheRealmObject = cache.get(mixedRealmObject);
            if (cacheRealmObject != null) {
                mixed = Mixed.valueOf(cacheRealmObject);
            } else {
                long index = realm.getConfiguration()
                        .getSchemaMediator()
                        .insertOrUpdate(realm, mixedRealmObject, cache);

                mixed = Mixed.valueOf(realm.get(mixedValueClass, null, index));
            }
        }

        return mixed;
    }

    @SuppressWarnings("unchecked")
    static Mixed createDetachedCopy(Mixed mixed, @Nonnull Realm realm, int currentDepth, int maxDepth, Map<RealmModel, RealmObjectProxy.CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || mixed == null) {
            return Mixed.nullValue();
        }

        if (mixed.getType() == MixedType.OBJECT) {
            Class<? extends RealmModel> mixedValueClass = (Class<? extends RealmModel>) mixed.getValueClass();
            RealmModel mixedRealmObject = mixed.asRealmModel(mixedValueClass);

            RealmModel detachedCopy = realm.getConfiguration()
                    .getSchemaMediator()
                    .createDetachedCopy(mixedRealmObject, maxDepth - 1, cache);

            mixed = Mixed.valueOf(detachedCopy);
        }

        return mixed;
    }

    @SuppressWarnings("unchecked")
    static Mixed createOrUpdateUsingJsonObject(Mixed mixed, @Nonnull Realm realm, int currentDepth, int maxDepth, Map<RealmModel, RealmObjectProxy.CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || mixed == null) {
            return Mixed.nullValue();
        }

        if (mixed.getType() == MixedType.OBJECT) {
            Class<? extends RealmModel> mixedValueClass = (Class<? extends RealmModel>) mixed.getValueClass();
            RealmModel mixedRealmObject = mixed.asRealmModel(mixedValueClass);

            RealmModel detachedCopy = realm.getConfiguration()
                    .getSchemaMediator()
                    .createDetachedCopy(mixedRealmObject, maxDepth - 1, cache);

            mixed = Mixed.valueOf(detachedCopy);
        }

        return mixed;
    }
}
