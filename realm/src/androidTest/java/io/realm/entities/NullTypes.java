/*
 * Copyright 2015 Realm Inc.
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

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


public class NullTypes extends RealmObject {

    public static String FIELD_STRING_NOT_NULL = "fieldStringNotNull";
    public static String FIELD_STRING_NULL = "fieldStringNull";
    public static String FIELD_BYTES_NOT_NULL = "fieldBytesNotNull";
    public static String FIELD_BYTES_NULL = "fieldBytesNull";

    @PrimaryKey
    private int id;

    @Required
    private String fieldStringNotNull;
    private String fieldStringNull;

    @Required
    private byte[] fieldBytesNotNull;
    private byte[] fieldBytesNull;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFieldStringNotNull() {
        return fieldStringNotNull;
    }

    public void setFieldStringNotNull(String fieldStringNotNull) {
        this.fieldStringNotNull = fieldStringNotNull;
    }

    public String getFieldStringNull() {
        return fieldStringNull;
    }

    public void setFieldStringNull(String fieldStringNull) {
        this.fieldStringNull = fieldStringNull;
    }

    public byte[] getFieldBytesNull() {
        return fieldBytesNull;
    }

    public void setFieldBytesNull(byte[] fieldBytesNull) {
        this.fieldBytesNull = fieldBytesNull;
    }

    public byte[] getFieldBytesNotNull() {
        return fieldBytesNotNull;
    }

    public void setFieldBytesNotNull(byte[] fieldBytesNotNull) {
        this.fieldBytesNotNull = fieldBytesNotNull;
    }
}
