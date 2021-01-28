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

import java.util.UUID;

import io.realm.Mixed;
import io.realm.RealmDictionary;
import io.realm.RealmObject;

public class RealmDictionaryModel extends RealmObject {

    private final RealmDictionary<Boolean> immutableRealmDictionaryField = new RealmDictionary();

    private RealmDictionary<Mixed> myMixedRealmDictionary;
    private RealmDictionary<Boolean> myBooleanRealmDictionary;
    private RealmDictionary<String> myStringRealmDictionary;
    private RealmDictionary<Integer> myIntegerRealmDictionary;
    private RealmDictionary<Float> myFloatRealmDictionary;
    private RealmDictionary<Long> myLongRealmDictionary;
    private RealmDictionary<Short> myShortRealmDictionary;
    private RealmDictionary<UUID> myUUIDRealmDictionary;

    // TODO: add RealmDictionary<RealmModel>

    public RealmDictionary<Boolean> getImmutableRealmDictionaryField() {
        return immutableRealmDictionaryField;
    }

    public RealmDictionary<Mixed> getMyMixedRealmDictionary() {
        return myMixedRealmDictionary;
    }

    public void setMyMixedRealmDictionary(RealmDictionary<Mixed> myMixedRealmDictionary) {
        this.myMixedRealmDictionary = myMixedRealmDictionary;
    }

    public RealmDictionary<Boolean> getMyBooleanRealmDictionary() {
        return myBooleanRealmDictionary;
    }

    public void setMyBooleanRealmDictionary(RealmDictionary<Boolean> myBooleanRealmDictionary) {
        this.myBooleanRealmDictionary = myBooleanRealmDictionary;
    }

    public RealmDictionary<String> getMyStringRealmDictionary() {
        return myStringRealmDictionary;
    }

    public void setMyStringRealmDictionary(RealmDictionary<String> myStringRealmDictionary) {
        this.myStringRealmDictionary = myStringRealmDictionary;
    }

    public RealmDictionary<Integer> getMyIntegerRealmDictionary() {
        return myIntegerRealmDictionary;
    }

    public void setMyIntegerRealmDictionary(RealmDictionary<Integer> myIntegerRealmDictionary) {
        this.myIntegerRealmDictionary = myIntegerRealmDictionary;
    }

    public RealmDictionary<Float> getMyFloatRealmDictionary() {
        return myFloatRealmDictionary;
    }

    public void setMyFloatRealmDictionary(RealmDictionary<Float> myFloatRealmDictionary) {
        this.myFloatRealmDictionary = myFloatRealmDictionary;
    }

    public RealmDictionary<Long> getMyLongRealmDictionary() {
        return myLongRealmDictionary;
    }

    public void setMyLongRealmDictionary(RealmDictionary<Long> myLongRealmDictionary) {
        this.myLongRealmDictionary = myLongRealmDictionary;
    }

    public RealmDictionary<Short> getMyShortRealmDictionary() {
        return myShortRealmDictionary;
    }

    public void setMyShortRealmDictionary(RealmDictionary<Short> myShortRealmDictionary) {
        this.myShortRealmDictionary = myShortRealmDictionary;
    }

    public RealmDictionary<UUID> getMyUUIDRealmDictionary() {
        return myUUIDRealmDictionary;
    }

    public void setMyUUIDRealmDictionary(RealmDictionary<UUID> myUUIDRealmDictionary) {
        this.myUUIDRealmDictionary = myUUIDRealmDictionary;
    }
}
