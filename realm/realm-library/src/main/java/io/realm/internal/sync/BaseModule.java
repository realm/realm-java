/*
 * Copyright 2018 Realm Inc.
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

package io.realm.internal.sync;

import io.realm.annotations.RealmModule;

// Workaround preventing `io.realm.DefaultRealmModuleMediator` being generated in the
// Realm JAR. Related to `https://github.com/realm/realm-java/issues/5799
@RealmModule(library = true, allClasses = true)
public class BaseModule {
}
