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
import io.realm.RealmSet;

/**
 * Sets must specify a generic type. With this class we check that the annotation processor
 * detects this class has a RealmSet field missing the required type and therefore should
 * fail in compile time.
 */
public class RealmSetMissingGenerics extends RealmObject {

    private RealmSet set; // this is an error!
}
