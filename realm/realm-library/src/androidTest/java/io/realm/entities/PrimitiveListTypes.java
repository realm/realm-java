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

package io.realm.entities;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

public class PrimitiveListTypes extends RealmObject {
    public static final String FIELD_STRING_LIST = "stringList";
    public static final String FIELD_BINARY_LIST = "binaryList";
    public static final String FIELD_BOOLEAN_LIST = "booleanList";
    public static final String FIELD_DOUBLE_LIST = "doubleList";
    public static final String FIELD_FLOAT_LIST = "floatList";
    public static final String FIELD_DATE_LIST = "dateList";
    public static final String FIELD_BYTE_LIST = "byteList";
    public static final String FIELD_SHORT_LIST = "shortList";
    public static final String FIELD_INT_LIST = "intList";
    public static final String FIELD_LONG_LIST = "longList";

    @SuppressWarnings("unused")
    private RealmList<String> stringList;
    @SuppressWarnings("unused")
    private RealmList<byte[]> binaryList;
    @SuppressWarnings("unused")
    private RealmList<Boolean> booleanList;
    @SuppressWarnings("unused")
    private RealmList<Double> doubleList;
    @SuppressWarnings("unused")
    private RealmList<Float> floatList;
    @SuppressWarnings("unused")
    private RealmList<Date> dateList;
    @SuppressWarnings("unused")
    private RealmList<Byte> byteList;
    @SuppressWarnings("unused")
    private RealmList<Short> shortList;
    @SuppressWarnings("unused")
    private RealmList<Integer> intList;
    @SuppressWarnings("unused")
    private RealmList<Long> longList;

    public RealmList getList(String fieldName) {
        switch (fieldName) {
            case FIELD_STRING_LIST:
                return stringList;
            case FIELD_BINARY_LIST:
                return binaryList;
            case FIELD_BOOLEAN_LIST:
                return booleanList;
            case FIELD_DOUBLE_LIST:
                return doubleList;
            case FIELD_FLOAT_LIST:
                return floatList;
            case FIELD_DATE_LIST:
                return dateList;
            case FIELD_BYTE_LIST:
                return byteList;
            case FIELD_SHORT_LIST:
                return shortList;
            case FIELD_INT_LIST:
                return intList;
            case FIELD_LONG_LIST:
                return longList;
            default:
                throw new IllegalArgumentException("Unknown field name: '" + fieldName + "'.");
        }
    }
}
