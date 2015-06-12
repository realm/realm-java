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

package io.realm.examples.librarymodules.modules;

import io.realm.annotations.RealmModule;
import io.realm.examples.librarymodules.model.Elephant;
import io.realm.examples.librarymodules.model.Lion;
import io.realm.examples.librarymodules.model.Zebra;

@RealmModule(library = true, classes = {Elephant.class, Lion.class, Zebra.class})
public class ZooAnimalsModule {
}
