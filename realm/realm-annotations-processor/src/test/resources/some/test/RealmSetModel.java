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

package some.test;

import org.bson.types.ObjectId;

import io.realm.RealmSet;
import io.realm.RealmObject;

public class RealmSetModel extends RealmObject {

    private RealmSet<String> stringSet;
    private RealmSet<Integer> integerSet;
    private RealmSet<Long> longSet;
    private RealmSet<Short> shortSet;
    private RealmSet<Byte> byteSet;
    private RealmSet<byte[]> binarySet;
    private RealmSet<ObjectId> objectIdSet;

    public RealmSet<String> getStringSet() {
        return stringSet;
    }

    public void setStringSet(RealmSet<String> stringSet) {
        this.stringSet = stringSet;
    }

    public RealmSet<Integer> getIntegerSet() {
        return integerSet;
    }

    public void setIntegerSet(RealmSet<Integer> integerSet) {
        this.integerSet = integerSet;
    }

    public RealmSet<Long> getLongSet() {
        return longSet;
    }

    public void setLongSet(RealmSet<Long> longSet) {
        this.longSet = longSet;
    }

    public RealmSet<Short> getShortSet() {
        return shortSet;
    }

    public void setShortSet(RealmSet<Short> shortSet) {
        this.shortSet = shortSet;
    }

    public RealmSet<Byte> getByteSet() {
        return byteSet;
    }

    public void setByteSet(RealmSet<Byte> byteSet) {
        this.byteSet = byteSet;
    }

    public RealmSet<byte[]> getBinarySet() {
        return binarySet;
    }

    public void setBinarySet(RealmSet<byte[]> binarySet) {
        this.binarySet = binarySet;
    }

    public RealmSet<ObjectId> getObjectIdSet() {
        return objectIdSet;
    }

    public void setObjectIdSet(RealmSet<ObjectId> objectIdSet) {
        this.objectIdSet = objectIdSet;
    }
}
