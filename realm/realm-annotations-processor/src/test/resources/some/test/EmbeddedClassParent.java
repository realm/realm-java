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

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.RealmClass;

// This class is only for creating the correct type hiearchy when testing Embedded Objects
// This class can work as a parent for all legal embedded object classes
public class EmbeddedClassParent extends RealmObject {
    public String name;
    public int age;

    // Valid single children references
    public EmbeddedClass child1;
    public EmbeddedClassRequiredParent child2;
    public EmbeddedClassOptionalParents child3;
    public EmbeddedClassOptionalParents child4;
}
