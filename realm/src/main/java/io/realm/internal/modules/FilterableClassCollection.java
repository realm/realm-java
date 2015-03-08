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

package io.realm.internal.modules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.RealmObject;
import io.realm.internal.RealmProxyMediator;

/**
 * Specialized version of a class collection that can further filter the available classes based on provided filter
 */
public class FilterableClassCollection extends RealmClassCollection {

    private RealmClassCollection originalCollection;
    private Set<Class<? extends RealmObject>> allowedClasses = new HashSet<Class<? extends RealmObject>>();

    /**
     * Creates a filterable class collection.
     *
     * @param originalCollection    Original auto generated collection.
     * @param filter                Classes from original collection to allow.
     */
    public FilterableClassCollection(RealmClassCollection originalCollection, List<Class<? extends RealmObject>> filter) {
        this.originalCollection = originalCollection;
        Set<Class<? extends RealmObject>> originalClasses = originalCollection.getModuleClasses();
        for (Class<? extends RealmObject> clazz : filter) {
            if (originalClasses.contains(clazz)) {
                allowedClasses.add(clazz);
            }
        };
    }

    @Override
    public Set<Class<? extends RealmObject>> getModuleClasses() {
        return allowedClasses;
    }

    @Override
    public RealmProxyMediator getProxyMediator() {
        return originalCollection.getProxyMediator();
    }

    public RealmClassCollection getOriginalCollection() {
        return originalCollection;
    }
}
