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
        } else if (realmList.clazz == RealmAny.class) {
            for (int i = 0; i < arraySize; i++) {
                if (jsonArray.isNull(i)) {
                    osList.addNull();
                    continue;
                }

                Object value = jsonArray.get(i);
                RealmAny realmAny;
                if (value instanceof String) {
                    realmAny = RealmAny.valueOf((String) value);
                } else if (value instanceof Integer) {
                    realmAny = RealmAny.valueOf((Integer) value);
                } else if (value instanceof Long) {
                    realmAny = RealmAny.valueOf((Long) value);
                } else if (value instanceof Double) {
                    realmAny = RealmAny.valueOf((Double) value);
                } else if (value instanceof Boolean) {
                    realmAny = RealmAny.valueOf((Boolean) value);
                } else if (value instanceof RealmAny) {
                    realmAny = (io.realm.RealmAny) value;
                    realmAny = ProxyUtils.copyOrUpdate(realmAny, realm, update, new HashMap<>(), new HashSet<>());
                } else {
                    throw new IllegalArgumentException(String.format("Unsupported JSON type: %s", value.getClass().getSimpleName()));
                }
                osList.addRealmAny(realmAny.getNativePtr());
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
        } else if (elementClass == RealmAny.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(RealmAny.nullValue());
                } else if (jsonReader.peek() == JsonToken.STRING) {
                    realmList.add(RealmAny.valueOf(jsonReader.nextString()));
                } else if (jsonReader.peek() == JsonToken.NUMBER) {
                    String value = jsonReader.nextString();
                    if (value.contains(".")) {
                        realmList.add(RealmAny.valueOf(Double.parseDouble(value)));
                    } else {
                        realmList.add(RealmAny.valueOf(Long.parseLong(value)));
                    }
                } else if (jsonReader.peek() == JsonToken.BOOLEAN) {
                    realmList.add(RealmAny.valueOf(jsonReader.nextBoolean()));
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
    static <T extends RealmModel> RealmAny copyToRealmIfNeeded(ProxyState<T> proxyState, @Nullable RealmAny value) {
        final Realm realm = (Realm) proxyState.getRealm$realm();

        if ((value != null) && (value.getType() == RealmAnyType.OBJECT)) {
            RealmModel realmAnyRealmModel = value.asRealmModel(RealmModel.class);

            if (realm.getSchema().getSchemaForClass(realmAnyRealmModel.getClass()).isEmbedded()) {
                throw new IllegalArgumentException("Embedded objects are not supported by RealmAny.");
            }

            if (!RealmObject.isManaged(realmAnyRealmModel)) {
                if (realm.hasPrimaryKey(realmAnyRealmModel.getClass())) {
                    value = RealmAny.valueOf(realm.copyToRealmOrUpdate(realmAnyRealmModel));
                } else {
                    value = RealmAny.valueOf(realm.copyToRealm(realmAnyRealmModel));
                }
            } else {
                proxyState.checkValidObject(realmAnyRealmModel);
            }
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    static RealmAny copyOrUpdate(RealmAny realmAny, @Nonnull Realm realm, boolean update, @Nonnull Map<RealmModel, RealmObjectProxy> cache, @Nonnull Set<ImportFlag> flags) {
        if (realmAny == null) {
            return RealmAny.nullValue();
        }

        if (realmAny.getType() == RealmAnyType.OBJECT) {
            Class<? extends RealmModel> realmAnyValueClass = (Class<? extends RealmModel>) realmAny.getValueClass();
            RealmModel realmAnyRealmObject = realmAny.asRealmModel(realmAnyValueClass);

            RealmObjectProxy cacheRealmObject = cache.get(realmAnyRealmObject);
            if (cacheRealmObject != null) {
                realmAny = RealmAny.valueOf(cacheRealmObject);
            } else {
                RealmModel managedRealmAnyRealmObject = realm
                        .getConfiguration()
                        .getSchemaMediator()
                        .copyOrUpdate(realm, realmAnyRealmObject, update, cache, flags);

                realmAny = RealmAny.valueOf(managedRealmAnyRealmObject);
            }
        }

        return realmAny;
    }

    @SuppressWarnings("unchecked")
    static RealmAny insert(RealmAny realmAny, @Nonnull Realm realm, @Nonnull Map<RealmModel, Long> cache) {
        if (realmAny == null) {
            return RealmAny.nullValue();
        }

        if (realmAny.getType() == RealmAnyType.OBJECT) {
            Class<? extends RealmModel> realmAnyValueClass = (Class<? extends RealmModel>) realmAny.getValueClass();
            RealmModel realmAnyRealmObject = realmAny.asRealmModel(realmAnyValueClass);

            Long cacheRealmObject = cache.get(realmAnyRealmObject);
            if (cacheRealmObject != null) {
                realmAny = RealmAny.valueOf(cacheRealmObject);
            } else {
                long index = realm.getConfiguration()
                        .getSchemaMediator()
                        .insert(realm, realmAnyRealmObject, cache);

                RealmModel realmModel = realm.get(realmAnyValueClass, null, index);

                realmAny = RealmAny.valueOf(realmModel);
            }
        }

        return realmAny;
    }

    @SuppressWarnings("unchecked")
    static RealmAny insertOrUpdate(RealmAny realmAny, @Nonnull Realm realm, @Nonnull Map<RealmModel, Long> cache) {
        if (realmAny == null) {
            return RealmAny.nullValue();
        }

        if (realmAny.getType() == RealmAnyType.OBJECT) {
            Class<? extends RealmModel> realmAnyValueClass = (Class<? extends RealmModel>) realmAny.getValueClass();
            RealmModel realmAnyRealmObject = realmAny.asRealmModel(realmAnyValueClass);

            Long cacheRealmObject = cache.get(realmAnyRealmObject);
            if (cacheRealmObject != null) {
                realmAny = RealmAny.valueOf(cacheRealmObject);
            } else {
                long index = realm.getConfiguration()
                        .getSchemaMediator()
                        .insertOrUpdate(realm, realmAnyRealmObject, cache);

                realmAny = RealmAny.valueOf(realm.get(realmAnyValueClass, null, index));
            }
        }

        return realmAny;
    }

    @SuppressWarnings("unchecked")
    static RealmAny createDetachedCopy(RealmAny realmAny, @Nonnull Realm realm, int currentDepth, int maxDepth, Map<RealmModel, RealmObjectProxy.CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmAny == null) {
            return RealmAny.nullValue();
        }

        if (realmAny.getType() == RealmAnyType.OBJECT) {
            Class<? extends RealmModel> realmAnyValueClass = (Class<? extends RealmModel>) realmAny.getValueClass();
            RealmModel realmAnyRealmObject = realmAny.asRealmModel(realmAnyValueClass);

            RealmModel detachedCopy = realm.getConfiguration()
                    .getSchemaMediator()
                    .createDetachedCopy(realmAnyRealmObject, maxDepth - 1, cache);

            realmAny = RealmAny.valueOf(detachedCopy);
        }

        return realmAny;
    }

    @SuppressWarnings("unchecked")
    static RealmAny createOrUpdateUsingJsonObject(RealmAny realmAny, @Nonnull Realm realm, int currentDepth, int maxDepth, Map<RealmModel, RealmObjectProxy.CacheData<RealmModel>> cache) {
        if (currentDepth > maxDepth || realmAny == null) {
            return RealmAny.nullValue();
        }

        if (realmAny.getType() == RealmAnyType.OBJECT) {
            Class<? extends RealmModel> realmAnyValueClass = (Class<? extends RealmModel>) realmAny.getValueClass();
            RealmModel realmAnyRealmObject = realmAny.asRealmModel(realmAnyValueClass);

            RealmModel detachedCopy = realm.getConfiguration()
                    .getSchemaMediator()
                    .createDetachedCopy(realmAnyRealmObject, maxDepth - 1, cache);

            realmAny = RealmAny.valueOf(detachedCopy);
        }

        return realmAny;
    }
}
