package io.realm.entities;
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

import io.realm.MutableRealmInteger;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.Required;


public class MutableRealmIntegerTypes extends RealmObject {
    public static final String CLASS_NAME = "MutableRealmIntegerTypes";
    public static final String FIELD_NULLABLE_MUTABLEEALMINTEGER = "columnNullableMutableRealmInteger";
    public static final String FIELD_NONNULLABLE_MUTABLEREALMINTEGER = "columnNonNullableMutableRealmInteger";
    public static final String FIELD_INDEXED_MUTABLEREALMINTEGER = "columnIndexedMutableRealmInteger";

    public final MutableRealmInteger columnNullableMutableRealmInteger = MutableRealmInteger.ofNull();

    @Required
    public final MutableRealmInteger columnNonNullableMutableRealmInteger = MutableRealmInteger.valueOf(0L);

    @Index
    public final MutableRealmInteger columnIndexedMutableRealmInteger = MutableRealmInteger.ofNull();

    public MutableRealmInteger getColumnNullableMutableRealmInteger() {
        return columnNullableMutableRealmInteger;
    }

    public MutableRealmInteger getColumnNonNullableMutableRealmInteger() {
        return columnNonNullableMutableRealmInteger;
    }

    public MutableRealmInteger getColumnIndexedMutableRealmInteger() {
        return columnIndexedMutableRealmInteger;
    }
}
