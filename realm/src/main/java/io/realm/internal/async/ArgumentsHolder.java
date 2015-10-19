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


package io.realm.internal.async;
/**
 * Value holder class to encapsulate the arguments of a RealmQuery
 * (in case we want to re-query)
 */
public class ArgumentsHolder {
    public final static int TYPE_FIND_ALL = 0;
    public final static int TYPE_FIND_ALL_SORTED = 1;
    public final static int TYPE_FIND_ALL_MULTI_SORTED = 2;
    public final static int TYPE_FIND_FIRST = 3;

    public final int type;
    public long columnIndex;
    public boolean ascending;
    public long[] columnIndices;
    public boolean[] ascendings;

    public ArgumentsHolder(int type) {
        this.type = type;
    }
}
