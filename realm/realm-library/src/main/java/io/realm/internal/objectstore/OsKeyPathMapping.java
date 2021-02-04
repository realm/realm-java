package io.realm.internal.objectstore;

import io.realm.RealmSchema;
import io.realm.exceptions.RealmError;
import io.realm.internal.NativeObject;

/**
 * Wrapper for ObjectStore's KeyPathMapping class.
 * <p>
 * The primary use case for this  class is to make sure that we release native memory for any old
 * mappings when the schema is updated.
 * <p>
 * It will use the already constructed ObjectStore schema to create the mappings required by
 * the Query Parser.
 *
 * @see RealmSchema#refresh()
 */
public class OsKeyPathMapping implements NativeObject {

    private static final long nativeFinalizerPtr = nativeGetFinalizerMethodPtr();
    public long mappingPointer = -1;

    public OsKeyPathMapping(long sharedRealmNativePointer) {
        try {
            // The KeyPathMapping is created before we validated the schema, in some
            // cases this can cause problems if the keypath mapping point to non-existing
            // classes or properties. Instead of crashing here, we let the schema validation
            // throw a better error
            mappingPointer = nativeCreateMapping(sharedRealmNativePointer);
        } catch (Exception ignore) {
        } catch (RealmError ignore) {
        }
    }

    @Override
    public long getNativePtr() {
        return mappingPointer;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    private static native long nativeGetFinalizerMethodPtr();
    private static native long nativeCreateMapping(long sharedRealmNativePointer);
}
