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

package io.realm;

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
class OsRealmSchema extends RealmSchema {
    static final class Creator extends RealmSchema {
        private final Map<String, OsRealmObjectSchema> schema = new HashMap<>();

        @Override
        public void close() { }

        @Override
        public RealmObjectSchema get(String className) {
            checkEmpty(className);
            return (!contains(className)) ? null : schema.get(className);
        }

        @Override
        public Set<OsRealmObjectSchema> getAll() {
            return new LinkedHashSet<>(schema.values());
        }

        @Override
        public RealmObjectSchema create(String className) {
            checkEmpty(className);
            OsRealmObjectSchema realmObjectSchema = new OsRealmObjectSchema(className);
            schema.put(className, realmObjectSchema);
            return realmObjectSchema;
        }

        @Override
        public boolean contains(String className) {
            return schema.containsKey(className);
        }

        @Override
        public void remove(String className) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RealmObjectSchema rename(String oldClassName, String newClassName) {
            throw new UnsupportedOperationException();
        }
    }

    private final Map<String, RealmObjectSchema> dynamicClassToSchema = new HashMap<>();

    private final long nativePtr;

    OsRealmSchema(Creator creator) {
        Set<OsRealmObjectSchema> realmObjectSchemas = creator.getAll();
        long[] schemaNativePointers = new long[realmObjectSchemas.size()];
        int i = 0;
        for (OsRealmObjectSchema schema : realmObjectSchemas) {
            schemaNativePointers[i++] = schema.getNativePtr();
        }
        this.nativePtr = nativeCreateFromList(schemaNativePointers);
    }

    public long getNativePtr() {
        return this.nativePtr;
    }

    // THIS IS NEVER CALLED!
    // See BaseRealm uses a StandardRealmSchema, not a OsRealmSchema.
    @Override
    public void close() {
        Set<OsRealmObjectSchema> schemas = getAll();
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
        checkEmpty(className);
        return (!contains(className)) ? null : dynamicClassToSchema.get(className);
    }

    /**
     * Returns the {@link RealmObjectSchema} for all RealmObject classes that can be saved in this Realm.
     *
     * @return the set of all classes in this Realm or no RealmObject classes can be saved in the Realm.
     */
    @Override
    public Set<OsRealmObjectSchema> getAll() {
        long[] ptrs = nativeGetAll(nativePtr);
        Set<OsRealmObjectSchema> schemas = new LinkedHashSet<>(ptrs.length);
        for (int i = 0; i < ptrs.length; i++) {
            schemas.add(new OsRealmObjectSchema(ptrs[i]));
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
        checkEmpty(className);
        OsRealmObjectSchema realmObjectSchema = new OsRealmObjectSchema(className);
        dynamicClassToSchema.put(className, realmObjectSchema);
        return realmObjectSchema;
    }

    @Override
    public void remove(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RealmObjectSchema rename(String oldClassName, String newClassName) {
        throw new UnsupportedOperationException();
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

    static void checkEmpty(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("Null or empty class names are not allowed");
        }
    }

    static native long nativeCreateFromList(long[] objectSchemaPtrs);

    static native void nativeClose(long nativePtr);

    static native long[] nativeGetAll(long nativePtr);
}
