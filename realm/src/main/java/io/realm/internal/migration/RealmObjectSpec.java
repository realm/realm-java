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

package io.realm.internal.migration;

import java.lang.annotation.Annotation;
import java.util.Set;

import io.realm.annotations.Index;
import io.realm.examples.MigrationAPIExamples;

/**
 * This class maps between a Java specification and the underlying implementation in the core database.
 * All fields are accessed by their "realm" name.
 *
 * Should this class replace TableSpec?
 */
public class RealmObjectSpec {

    public RealmObjectSpec(String className) {

    }

    public static RealmObjectSpec fromClass(Class<MigrationAPIExamples.New> newClass) {
        return null;
    }

    public RealmObjectSpec addField(String fieldName, Class<?> typeClass, Set<Annotation> annotations) {
        return null;
    }

    public RealmObjectSpec addField(String w, Class<?> stringClass) {
        return null;
    }

    public RealmObjectSpec deleteField(String x) {
        return null;
    }

    public RealmObjectSpec renameField(String x, String y) {
        return null;
    }

    public RealmObjectSpec merge(String x, String y, Class<String> stringClass) {
        return null;
    }

    public RealmObjectSpec setType(String x, Class<Integer> integerClass) {
        return null;
    }

    public RealmObjectSpec moveFieldToClass(String x, RealmObjectSpec otherClass) {
        return null;
    }

    public RealmObjectSpec moveFieldToClass(String x, String other) {
        return null;
    }

    public static class Builder {
        public Builder(String className) {

        }

        public Builder addField(String fieldName, Class<?> typeClass, Class<Index> indexClass) {
            return this;
        }

        public RealmObjectSpec build() {
            return null;
        }
    }
}
