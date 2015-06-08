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

package io.realm.examples.junkyard;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.realm.RealmObject;
import io.realm.examples.junkyard.RealmLibraryModule;


public class AppModule implements RealmLibraryModule {

    public Set<Class<? extends RealmObject>> getClasses() {
        return new HashSet<Class<? extends RealmObject>>(Arrays.asList(
                RealmObject.class
        ));
    }
}


