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

package io.realm.internal;

import javax.annotation.Nullable;

/**
 * Used to avoid passing a {@link Class} and a {@link String} via parameters to the value operators.
 */
public class ClassContainer {

    @Nullable
    private final Class<?> clazz;
    @Nullable
    private final String className;

    public ClassContainer(@Nullable Class<?> clazz, @Nullable String className) {
        this.clazz = clazz;
        this.className = className;
    }

    @Nullable
    public Class<?> getClazz() {
        return clazz;
    }

    @Nullable
    public String getClassName() {
        return className;
    }
}
