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

public class StringAndInt extends RealmObject {
    private String str;
    private int number;

    public String getStr() {
        return realmGetter$str();
    }

    public void setStr(String str) {
        realmSetter$str(str);
    }

    public String realmGetter$str() {
        return str;
    }

    public void realmSetter$str(String str) {
        this.str = str;
    }

    public int getNumber() {
        return realmGetter$number();
    }

    public void setNumber(int number) {
        realmSetter$number(number);
    }

    public int realmGetter$number() {
        return number;
    }

    public void realmSetter$number(int number) {
        this.number = number;
    }
}
