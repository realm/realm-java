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
package io.realm.processor.nameconverter;

/**
 * Interface for converters that can implement a given naming policy.
 *
 * @see io.realm.annotations.RealmNamingPolicy
 */
public interface NameConverter {
    /**
     * Converts the {@code name} so it matches the {@link io.realm.annotations.RealmNamingPolicy}.
     *
     * @param name string to convert.
     * @return the converted string.
     */
    String convert(String name);
}
