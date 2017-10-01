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

package some.test;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

public class ValueList extends RealmObject {
    public RealmList<String> stringList;
    public RealmList<byte[]> binaryList;
    public RealmList<Boolean> booleanList;
    public RealmList<Long> longList;
    public RealmList<Integer> integerList;
    public RealmList<Short> shortList;
    public RealmList<Byte> byteList;
    public RealmList<Double> doubleList;
    public RealmList<Float> floatList;
    public RealmList<Date> integerList;
}
