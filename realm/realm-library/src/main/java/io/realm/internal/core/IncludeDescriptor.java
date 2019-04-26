/*
 * Copyright 2019 Realm Inc.
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
package io.realm.internal.core;

import java.util.EnumSet;

import io.realm.RealmFieldType;
import io.realm.internal.NativeObject;
import io.realm.internal.Table;
import io.realm.internal.fields.FieldDescriptor;

/**
 * Creates the Java wrapper for a `realm::IncludeDescriptor`.
 */
public class IncludeDescriptor implements NativeObject {

    private static final long nativeFinalizerMethodPtr = nativeGetFinalizerMethodPtr();
    private final long nativePtr;

    public static IncludeDescriptor createInstance(FieldDescriptor.SchemaProxy schemaConnector, Table table, String includePath) {
            EnumSet<RealmFieldType> supportedIntermediateColumnTypes = EnumSet.of(RealmFieldType.OBJECT, RealmFieldType.LIST, RealmFieldType.LINKING_OBJECTS);
            EnumSet<RealmFieldType> supportedFinalColumnType = EnumSet.of(RealmFieldType.LINKING_OBJECTS);
            FieldDescriptor fieldDescriptor = FieldDescriptor.createFieldDescriptor(
                    schemaConnector,
                    table,
                    includePath,
                    supportedIntermediateColumnTypes,
                    supportedFinalColumnType);
            return new IncludeDescriptor(table, fieldDescriptor.getColumnIndices(), fieldDescriptor.getNativeTablePointers());
    }

    private IncludeDescriptor(Table table, long[] columnIndices, long[] nativeTablePointers) {
        nativePtr = nativeCreate(table.getNativePtr(), columnIndices, nativeTablePointers);
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerMethodPtr;
    }

    private static native long nativeGetFinalizerMethodPtr();
    private static native long nativeCreate(long tablePtr, long[] columnIndices, long[] tablePtrIndices);
}

