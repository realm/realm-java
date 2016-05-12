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

package io.realm.entities;

import io.realm.RealmList;
import io.realm.RealmObject;

public class NonLatinFieldNames extends RealmObject {

    public final static String FIELD_LONG_KOREAN_CHAR = "델타";
    public final static String FIELD_LONG_GREEK_CHAR = "Δέλτα";
    public final static String FIELD_FLOAT_KOREAN_CHAR = "베타";
    public final static String FIELD_FLOAT_GREEK_CHAR = "βήτα";
    public final static String FIELD_CHILDREN = "children";

    private long 델타;
    private long Δέλτα;

    private float 베타;
    private float βήτα;

    private RealmList<NonLatinFieldNames> children;

    public float get베타() { return 베타; }

    public void set베타(float 베타) { this.베타 = 베타; }

    public float getΒήτα() { return βήτα; }

    public void setΒήτα(float βήτα) { this.βήτα = βήτα; }

    public long get델타() { return 델타; }

    public void set델타(long 델타) { this.델타 = 델타; }

    public long getΔέλτα() { return Δέλτα; }

    public void setΔέλτα(long δέλτα) { this.Δέλτα = δέλτα; }

    public RealmList<NonLatinFieldNames> getChildren() {
        return children;
    }

    public void setChildren(RealmList<NonLatinFieldNames> children) {
        this.children = children;
    }
}
