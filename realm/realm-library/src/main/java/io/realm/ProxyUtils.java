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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;

import io.realm.internal.OsList;
import io.realm.internal.android.JsonUtils;

class ProxyUtils {

    /**
     * Called by proxy to set the managed {@link RealmList} according to the given {@link JSONObject}.
     *
     * @param realmList the managed {@link RealmList}.
     * @param jsonObject the {@link JSONObject} which may contain the data of the list to be set.
     * @param fieldName the field name of the {@link RealmList}.
     * @param <E> type of the {@link RealmList}.
     * @throws JSONException if it fails to parse JSON.
     */
    static <E> void setRealmListWithJsonObject(
            RealmList<E> realmList, JSONObject jsonObject, String fieldName) throws JSONException {
        if (!jsonObject.has(fieldName))  {
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
        } else if (realmList.clazz == Date.class ) {
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
     * Called by proxy to create a unmanaged {@link RealmList} according to the given {@link JsonReader}.
     *
     * @param elementClass the type of the {@link RealmList}.
     * @param jsonReader the JSON stream to be parsed which may contain the data of the list to be set.
     * @param <E> type of the {@link RealmList}.
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
                    realmList.add((int)jsonReader.nextLong());
                }
            }
        } else if (elementClass == Short.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add((short)jsonReader.nextLong());
                }
            }
        } else if (elementClass == Byte.class) {
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    realmList.add(null);
                } else {
                    realmList.add((byte)jsonReader.nextLong());
                }
            }
        } else {
            throwWrongElementType(elementClass);
        }

        jsonReader.endArray();

        return realmList;
    }

    private static void throwWrongElementType(@Nullable  Class clazz) {
        throw new IllegalArgumentException(String.format(Locale.ENGLISH, "Element type '%s' is not handled.",
                clazz));
    }

}
