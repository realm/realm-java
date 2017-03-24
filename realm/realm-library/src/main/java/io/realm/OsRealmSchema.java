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

package io.realm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * Class for interacting with the Realm schema using a dynamic API. This makes it possible
 * to add, delete and change the classes in the Realm.
 * <p>
 * All changes must happen inside a write transaction for the particular Realm.
 *
 * @see RealmMigration
 */
public class OsRealmSchema extends RealmSchema {
    private static final String EMPTY_STRING_MSG = "Null or empty class names are not allowed";

    // Caches Class Strings to their Schema object
    private final Map<String, RealmObjectSchema> dynamicClassToSchema = new HashMap<>();

    private final long nativePtr;

    OsRealmSchema(ArrayList<RealmObjectSchema> realmObjectSchemas) {
        long list[] = new long[realmObjectSchemas.size()];
        for (int i = 0; i < realmObjectSchemas.size(); i++) {
            list[i] = realmObjectSchemas.get(i).getNativePtr();
        }
        this.nativePtr = nativeCreateFromList(list);
    }

    public long getNativePtr() {
        return this.nativePtr;
    }

    @Override
    public void close() {
        Set<RealmObjectSchema> schemas = getAll();
        for (RealmObjectSchema schema : schemas) {
            schema.close();
        }
        nativeClose(nativePtr);
    }

    /**
     * Returns the Realm schema for a given class.
     *
     * @param className name of the class
     * @return schema object for that class or {@code null} if the class doesn't exists.
     */
    @Override
    public RealmObjectSchema get(String className) {
        checkEmpty(className, EMPTY_STRING_MSG);
        return (!contains(className)) ? null : dynamicClassToSchema.get(className);
    }

    /**
     * Returns the {@link RealmObjectSchema} for all RealmObject classes that can be saved in this Realm.
     *
     * @return the set of all classes in this Realm or no RealmObject classes can be saved in the Realm.
     */
    @Override
    public Set<RealmObjectSchema> getAll() {
        long[] ptrs = nativeGetAll(nativePtr);
        Set<RealmObjectSchema> schemas = new LinkedHashSet<>(ptrs.length);
        for (int i = 0; i < ptrs.length; i++) {
            schemas.add(new RealmObjectSchema(ptrs[i]));
        }
        return schemas;
    }

    /**
     * Adds a new class to the Realm.
     *
     * @param className name of the class.
     * @return a Realm schema object for that class.
     */
    @Override
    public RealmObjectSchema create(String className) {
        // Adding a class is always permitted.
        checkEmpty(className, EMPTY_STRING_MSG);
        RealmObjectSchema realmObjectSchema = new RealmObjectSchema(className);
        dynamicClassToSchema.put(className, realmObjectSchema);
        return realmObjectSchema;
    }

    /**
     * Checks if a given class already exists in the schema.
     *
     * @param className class name to check.
     * @return {@code true} if the class already exists. {@code false} otherwise.
     */
    @Override
    public boolean contains(String className) {
        return dynamicClassToSchema.containsKey(className);
    }

    private void checkEmpty(String str, String error) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(error);
        }
    }

    static native long nativeCreateFromList(long[] objectSchemaPtrs);

    static native void nativeClose(long nativePtr);

    static native long[] nativeGetAll(long nativePtr);
}
