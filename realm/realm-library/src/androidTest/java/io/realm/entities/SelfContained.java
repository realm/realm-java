/*
 * Copyright 2016 Realm Inc.
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

public class SelfContained extends RealmObject {

    public static final String CLASS_NAME = "SelfContained";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ID = "id";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_OBJECT = "object";
    public static final String FIELD_OBJECTS = "objects";

    public long id;
    public String name;
    public Date date;
    public SelfContained object;
    public RealmList<SelfContained> objects;
}
