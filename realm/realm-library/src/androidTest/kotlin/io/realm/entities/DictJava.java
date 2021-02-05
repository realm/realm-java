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

package io.realm.entities;

import io.realm.RealmList;
import io.realm.RealmObject;

public class DictJava extends RealmObject {

    private RealmList<Integer> myIntegerList;
    private RealmList<MyRealmModel> myModelList;

    public RealmList<Integer> getMyIntegerList() {
        return myIntegerList;
    }

    public void setMyIntegerList(RealmList<Integer> myIntegerList) {
        this.myIntegerList = myIntegerList;
    }

    public RealmList<MyRealmModel> getMyModelList() {
        return myModelList;
    }

    public void setMyModelList(RealmList<MyRealmModel> myModelList) {
        this.myModelList = myModelList;
    }

    //    private RealmDictionary<byte[]> myByteArrayDictionary;
//    private RealmDictionary<Byte[]> myNonPrimitiveByteArrayDictionary;
//
//    public RealmDictionary<byte[]> getMyByteArrayDictionary() {
//        return myByteArrayDictionary;
//    }
//
//    public void setMyByteArrayDictionary(RealmDictionary<byte[]> myByteArrayDictionary) {
//        this.myByteArrayDictionary = myByteArrayDictionary;
//    }
//
//    public RealmDictionary<Byte[]> getMyNonPrimitiveByteArrayDictionary() {
//        return myNonPrimitiveByteArrayDictionary;
//    }
//
//    public void setMyNonPrimitiveByteArrayDictionary(RealmDictionary<Byte[]> myNonPrimitiveByteArrayDictionary) {
//        this.myNonPrimitiveByteArrayDictionary = myNonPrimitiveByteArrayDictionary;
//    }
}
