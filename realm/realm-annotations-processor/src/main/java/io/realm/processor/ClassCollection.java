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

package io.realm.processor;

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper around all {@link ClassMetaData} that also enables easy lookup of class data.
 */
public class ClassCollection {

    // These two collections should always stay in sync
    Map<String, ClassMetaData> classes = new HashMap<String, ClassMetaData>();
    Set<ClassMetaData> classSet = new HashSet<ClassMetaData>();

    public void addClass(ClassMetaData metadata) {
        classSet.add(metadata);
        classes.put(metadata.getSimpleJavaClassName(), metadata);
    }

    public Set<ClassMetaData> getClasses() {
        return Collections.unmodifiableSet(classSet);
    }

    public ClassMetaData getClass(String simpleJavaClassName) {
        return classes.get(simpleJavaClassName);
    }

    public int size() {
        return classSet.size();
    }
}
