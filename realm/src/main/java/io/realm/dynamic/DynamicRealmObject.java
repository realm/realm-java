/*
 * Copyright 2014 Realm Inc.
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

package io.realm.dynamic;

import java.util.Date;

/**
 * Object for interacting with a RealmObject using dynamic names. This is used when migrating between
 * different versions of a RealmObject.
 *
 * @see io.realm.RealmMigration
 */
public class DynamicRealmObject {

    public boolean getBoolean(String fieldName) {
        return false;
    }

    public int getInt(String fieldName) {
        return 0;
    }

    public short getShort(String fieldName) {
        return 0;
    }

    public long getLong(String fieldName) {
        return 0;
    }

    public float getFloat(String fieldName) {
        return 0;
    }

    public double getDouble(String fieldName) {
        return 0;
    }

    public byte[] getBytes(String fieldName) {
        return new byte[0];
    }

    public String getString(String fieldName) {
        return null;
    }

    public Date getDate(String fieldName) {
        return null;
    }

    public DynamicRealmObject getRealmObject(String fieldName) {
        return null;
    }

    public DynamicRealmList getRealmList(String fieldName) {
        return null;
    }
}