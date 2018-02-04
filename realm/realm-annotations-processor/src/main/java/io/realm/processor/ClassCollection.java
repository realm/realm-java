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
package io.realm.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper around all Realm model classes metadata found during processing. It also
 * allows easy lookup for specific class data.
 */
public class ClassCollection {

    // These three collections should always stay in sync
    private Map<String, ClassMetaData> qualifiedNameClassMap = new LinkedHashMap<>();
    private Set<ClassMetaData> classSet = new LinkedHashSet<>();

    public void addClass(ClassMetaData metadata) {
        classSet.add(metadata);
        qualifiedNameClassMap.put(metadata.getFullyQualifiedClassName(), metadata);
    }

    public Set<ClassMetaData> getClasses() {
        return Collections.unmodifiableSet(classSet);
    }

    public ClassMetaData getClassFromQualifiedName(String qualifiedJavaClassName) {
        ClassMetaData data = qualifiedNameClassMap.get(qualifiedJavaClassName);
        if (data == null) {
            throw new IllegalArgumentException("Class " + qualifiedJavaClassName + " was not found");
        }
        return data;
    }

    public int size() {
        return classSet.size();
    }

    public boolean containsQualifiedClass(String qualifiedClassName) {
        return qualifiedNameClassMap.containsKey(qualifiedClassName);
    }
}
