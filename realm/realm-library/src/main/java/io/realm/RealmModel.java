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

package io.realm;

import io.realm.annotations.RealmClass;


/**
 * Interface for marking classes as RealmObjects, it can be used instead of extending {@link RealmObject}.
 * <p>
 * All helper methods available to classes that extend RealmObject are instead available as static methods:
 * <p>
 * <pre>
 * {@code
 *   Person p = realm.createObject(Person.class);
 *
 *   // With the RealmModel interface
 *   RealmObject.isValid(p);
 *
 *   // With the RealmObject base class
 *   p.isValid();
 * }
 * </pre>
 * <p>
 * Note: Object implementing this interface needs also to be annotated with {@link RealmClass}, so the annotation
 * processor can generate the underlining proxy class.
 *
 * @see RealmObject
 */

public interface RealmModel {
}
