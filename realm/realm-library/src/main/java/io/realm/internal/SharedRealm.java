/*
 * Copyright 2016 Realm Inc.
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

import java.io.Closeable;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.RealmConfiguration;
import io.realm.RealmSchema;
import io.realm.internal.android.AndroidCapabilities;
import io.realm.internal.android.AndroidRealmNotifier;

public final class SharedRealm implements Closeable, NativeObject {

    // Const value for RealmFileException conversion
    public static final byte FILE_EXCEPTION_KIND_ACCESS_ERROR = 0;
    public static final byte FILE_EXCEPTION_KIND_BAD_HISTORY = 1;
    public static final byte FILE_EXCEPTION_KIND_PERMISSION_DENIED = 2;
    public static final byte FILE_EXCEPTION_KIND_EXISTS = 3;
    public static final byte FILE_EXCEPTION_KIND_NOT_FOUND = 4;
    public static final byte FILE_EXCEPTION_KIND_INCOMPATIBLE_LOCK_FILE = 5;
    public static final byte FILE_EXCEPTION_KIND_FORMAT_UPGRADE_REQUIRED = 6;
    private static final long nativeFinalizerPtr = nativeGetFinalizerPtr();

    public static void initialize(File tempDirectory) {
        if (SharedRealm.temporaryDirectory != null) {
            // already initialized
            return;
        }
        if (tempDirectory == null) {
            throw new IllegalArgumentException("'tempDirectory' must not be null.");
        }

        String temporaryDirectoryPath = tempDirectory.getAbsolutePath();
        if (!tempDirectory.isDirectory() && !tempDirectory.mkdirs() && !tempDirectory.isDirectory()) {
            throw new IOException("failed to create temporary directory: " + temporaryDirectoryPath);
        }

        if (!temporaryDirectoryPath.endsWith("/")) {
            temporaryDirectoryPath += "/";
        }
        nativeInit(temporaryDirectoryPath);
        SharedRealm.temporaryDirectory = tempDirectory;
    }

    public static File getTemporaryDirectory() {
        return temporaryDirectory;
    }

    private volatile static File temporaryDirectory;

    public enum Durability {
        FULL(0),
        MEM_ONLY(1);

        final int value;

        Durability(int value) {
            this.value = value;
        }
    }

    // Public for static checking in JNI
    @SuppressWarnings("WeakerAccess")
    public static final byte SCHEMA_MODE_VALUE_AUTOMATIC = 0;
    @SuppressWarnings("WeakerAccess")
    public static final byte SCHEMA_MODE_VALUE_READONLY = 1;
    @SuppressWarnings("WeakerAccess")
    public static final byte SCHEMA_MODE_VALUE_RESET_FILE = 2;
    @SuppressWarnings("WeakerAccess")
    public static final byte SCHEMA_MODE_VALUE_ADDITIVE = 3;
    @SuppressWarnings("WeakerAccess")
    public static final byte SCHEMA_MODE_VALUE_MANUAL = 4;
    @SuppressWarnings("WeakerAccess")
    public enum SchemaMode {
        SCHEMA_MODE_AUTOMATIC(SCHEMA_MODE_VALUE_AUTOMATIC),
        SCHEMA_MODE_READONLY(SCHEMA_MODE_VALUE_READONLY),
        SCHEMA_MODE_RESET_FILE(SCHEMA_MODE_VALUE_RESET_FILE),
        SCHEMA_MODE_ADDITIVE(SCHEMA_MODE_VALUE_ADDITIVE),
        SCHEMA_MODE_MANUAL(SCHEMA_MODE_VALUE_MANUAL);

        final byte value;
        SchemaMode(byte value) {
            this .value = value;
        }

        public byte getNativeValue() {
            return value;
        }
    }

    // JNI will only hold a weak global ref to this.
    public final RealmNotifier realmNotifier;
    public final ObjectServerFacade objectServerFacade;
    public final List<WeakReference<Collection>> collections = new CopyOnWriteArrayList<WeakReference<Collection>>();
    public final Capabilities capabilities;

    // To prevent overflow the message queue.
    public boolean reattachCollectionsPosted = false;

    public static class VersionID implements Comparable<VersionID> {
        public final long version;
        public final long index;

        VersionID(long version, long index) {
            this.version = version;
            this.index = index;
        }

        @Override
        public int compareTo(@SuppressWarnings("NullableProblems") VersionID another) {
            if (another == null) {
                throw new IllegalArgumentException("Version cannot be compared to a null value.");
            }
            if (version > another.version) {
                return 1;
            } else if (version < another.version) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return "VersionID{" +
                    "version=" + version +
                    ", index=" + index +
                    '}';
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            VersionID versionID = (VersionID) object;
            return (version == versionID.version && index == versionID.index);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (int) (version ^ (version >>> 32));
            result = 31 * result + (int) (index ^ (index >>> 32));
            return result;
        }
    }

    public interface SchemaVersionListener {
        void onSchemaVersionChanged(long currentVersion);
    }

    private long nativePtr;
    private RealmConfiguration configuration;
    final Context context;
    private long lastSchemaVersion;
    private final SchemaVersionListener schemaChangeListener;

    private SharedRealm(long nativeConfigPtr,
                        RealmConfiguration configuration,
                        SchemaVersionListener schemaVersionListener) {
        Capabilities capabilities = new AndroidCapabilities();
        RealmNotifier realmNotifier = new AndroidRealmNotifier(this, capabilities);

        this.nativePtr = nativeGetSharedRealm(nativeConfigPtr, realmNotifier);
        this.configuration = configuration;

        this.capabilities = capabilities;
        this.realmNotifier = realmNotifier;
        this.schemaChangeListener = schemaVersionListener;
        context = new Context();
        context.addReference(this);
        this.lastSchemaVersion = schemaVersionListener == null ? -1L : getSchemaVersion();
        objectServerFacade = null;
        nativeSetAutoRefresh(nativePtr, capabilities.canDeliverNotification());
    }

    // This will create a SharedRealm where autoChangeNotifications is false,
    // If autoChangeNotifications is true, an additional SharedGroup might be created in the OS's external commit helper.
    // That is not needed for some cases: eg.: An extra opened SharedGroup will cause a compact failure.
    public static SharedRealm getInstance(RealmConfiguration config) {
        return getInstance(config, null, false);
    }


    public static SharedRealm getInstance(RealmConfiguration config, SchemaVersionListener schemaVersionListener,
                                          boolean autoChangeNotifications) {
        String[] userAndServer = ObjectServerFacade.getSyncFacadeIfPossible().getUserAndServerUrl(config);
        String rosServerUrl = userAndServer[0];
        String rosUserToken = userAndServer[1];
        boolean enable_caching = false; // Handled in Java currently
        boolean disableFormatUpgrade = false; // TODO Double negatives :/

        long nativeConfigPtr = nativeCreateConfig(
                config.getPath(),
                config.getEncryptionKey(),
                rosServerUrl != null ? SchemaMode.SCHEMA_MODE_ADDITIVE.getNativeValue() : SchemaMode.SCHEMA_MODE_MANUAL.getNativeValue(),
                config.getDurability() == Durability.MEM_ONLY,
                enable_caching,
                config.getSchemaVersion(),
                disableFormatUpgrade,
                autoChangeNotifications,
                rosServerUrl,
                rosUserToken);

        try {
            return new SharedRealm(nativeConfigPtr, config, schemaVersionListener);
        } finally {
            nativeCloseConfig(nativeConfigPtr);
        }
    }

    public void beginTransaction() {
        detachCollections();
        nativeBeginTransaction(nativePtr);
        invokeSchemaChangeListenerIfSchemaChanged();
    }

    public void commitTransaction() {
        nativeCommitTransaction(nativePtr);
        postToReattachCollections();
    }

    public void cancelTransaction() {
        nativeCancelTransaction(nativePtr);
        postToReattachCollections();
    }

    public boolean isInTransaction() {
        return nativeIsInTransaction(nativePtr);
    }

    public void setSchemaVersion(long schemaVersion) {
        nativeSetVersion(nativePtr, schemaVersion);
    }

    public long getSchemaVersion() {
        return nativeGetVersion(nativePtr);
    }

    // FIXME: This should be removed, migratePrimaryKeyTableIfNeeded is using it which should be in Object Store instead?
    long getGroupNative() {
        return nativeReadGroup(nativePtr);
    }

    public boolean hasTable(String name) {
        return nativeHasTable(nativePtr, name);
    }

    public Table getTable(String name) {
        return new Table(this, nativeGetTable(nativePtr, name));
    }

    public void renameTable(String oldName, String newName) {
        nativeRenameTable(nativePtr, oldName, newName);
    }

    public void removeTable(String name) {
        nativeRemoveTable(nativePtr, name);
    }

    public String getTableName(int index) {
        return nativeGetTableName(nativePtr, index);
    }

    public long size() {
        return nativeSize(nativePtr);
    }

    public String getPath() {
        return configuration.getPath();
    }

    public boolean isEmpty() {
        return nativeIsEmpty(nativePtr);
    }

    public void refresh() {
        nativeRefresh(nativePtr);
        invokeSchemaChangeListenerIfSchemaChanged();
    }

    public SharedRealm.VersionID getVersionID() {
        long[] versionId = nativeGetVersionID (nativePtr);
        return new SharedRealm.VersionID(versionId[0], versionId[1]);
    }

    public long getLastSnapshotVersion() {
        return nativeGetSnapshotVersion(nativePtr);
    }

    public boolean isClosed() {
        return nativePtr == 0 || nativeIsClosed(nativePtr);
    }

    public void writeCopy(File file, byte[] key) {
        if (file.isFile() && file.exists()) {
            throw new IllegalArgumentException("The destination file must not exist");
        }
        nativeWriteCopy(nativePtr, file.getAbsolutePath(), key);
    }

    public boolean waitForChange() {
        return nativeWaitForChange(nativePtr);
    }

    public void stopWaitForChange() {
        nativeStopWaitForChange(nativePtr);
    }

    public boolean compact() {
        return nativeCompact(nativePtr);
    }

    /**
     * Updates the underlying schema based on the schema description.
     * Calling this method must be done from inside a write transaction.
     */
    public void updateSchema(RealmSchema schema, long version) {
        nativeUpdateSchema(nativePtr, schema.getNativePtr(), version);
    }

    public void setAutoRefresh(boolean enabled) {
        capabilities.checkCanDeliverNotification(null);
        nativeSetAutoRefresh(nativePtr, enabled);
    }

    public boolean isAutoRefresh() {
        return nativeIsAutoRefresh(nativePtr);
    }

    public boolean requiresMigration(RealmSchema schema) {
        return nativeRequiresMigration(nativePtr, schema.getNativePtr());
    }

    @Override
    public void close() {
        if (realmNotifier != null) {
            realmNotifier.close();
        }
        synchronized (context) {
            if (nativePtr != 0) {
                nativeCloseSharedRealm(nativePtr);
                // It is OK to clear the nativePtr. It has been saved to the NativeObjectReference when adding to the
                // context.
                nativePtr = 0;
            }
        }
    }

    @Override
    public long getNativePtr() {
        return nativePtr;
    }

    @Override
    public long getNativeFinalizerPtr() {
        return nativeFinalizerPtr;
    }

    public void invokeSchemaChangeListenerIfSchemaChanged() {
        if (schemaChangeListener == null) {
            return;
        }

        final long before = lastSchemaVersion;
        final long current = getSchemaVersion();
        if (current != before) {
            lastSchemaVersion = current;
            schemaChangeListener.onSchemaVersionChanged(current);
        }
    }

    // addCollection(), detachCollections(), reattachCollections() and postToReattachCollections() are used to make
    // RealmResults stable iterators work. When a Collection is detached from a living OS Results, it won't receive
    // notifications and its elements won't be changed.
    // See https://github.com/realm/realm-java/issues/3883 for more information.
    // Should only be called by Collection's constructor.
    void addCollection(Collection collection) {
        if (realmNotifier != null) {
            collections.add(new WeakReference<Collection>(collection));
        }
    }

    // The detaching should happen before transaction begins.
    private void detachCollections() {
        for (WeakReference<Collection> collectionRef : collections) {
            Collection collection = collectionRef.get();
            if (collection == null) {
                collections.remove(collectionRef);
            } else {
                collection.detach();
            }
        }
    }

    // Ideally the reattaching should happen at the very end of the event loop, but it is impossible for most event
    // framework. We need to ensure:
    // 1) It happens before any other coming events get handled (eg: UI redraw event).
    // 2) It happens before Object Store async callbacks since the Object Store event_loop_signal might use a different
    //    event queue. This is guaranteed by call this function in the binding_context::before_notify callback.
    void reattachCollections() {
        if (isClosed()) {
            return;
        }
        if (isInTransaction()) {
            // This should never happen.
            throw new IllegalStateException( "Collection cannot be reattached if the Realm is in transaction." +
                    " Please remember to commit or cancel transaction before finishing the current event loop.");
        }
        for (WeakReference<Collection> collectionRef : collections) {
            Collection collection = collectionRef.get();
            if (collection == null) {
                collections.remove(collectionRef);
            } else {
                collection.reattach();
            }
        }
    }

    // To handle the point 1) in the reattachCollections comments.
    private void postToReattachCollections() {
        if (realmNotifier != null && !collections.isEmpty() && !reattachCollectionsPosted) {
            reattachCollectionsPosted = true;
            realmNotifier.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    reattachCollectionsPosted = false;
                    reattachCollections();
                }
            });
        }
    }

    private static native void nativeInit(String temporaryDirectoryPath);
    private static native long nativeCreateConfig(String realmPath, byte[] key, byte schemaMode, boolean inMemory,
                                                  boolean cache, long schemaVersion, boolean disableFormatUpgrade,
                                                  boolean autoChangeNotification,
                                                  String syncServerURL, String syncUserToken);
    private static native void nativeCloseConfig(long nativeConfigPtr);
    private static native long nativeGetSharedRealm(long nativeConfigPtr, RealmNotifier notifier);
    private static native void nativeCloseSharedRealm(long nativeSharedRealmPtr);
    private static native boolean nativeIsClosed(long nativeSharedRealmPtr);
    private static native void nativeBeginTransaction(long nativeSharedRealmPtr);
    private static native void nativeCommitTransaction(long nativeSharedRealmPtr);
    private static native void nativeCancelTransaction(long nativeSharedRealmPtr);
    private static native boolean nativeIsInTransaction(long nativeSharedRealmPtr);
    private static native long nativeGetVersion(long nativeSharedRealmPtr);
    private static native long nativeGetSnapshotVersion(long nativeSharedRealmPtr);
    private static native void nativeSetVersion(long nativeSharedRealmPtr, long version);
    private static native long nativeReadGroup(long nativeSharedRealmPtr);
    private static native boolean nativeIsEmpty(long nativeSharedRealmPtr);
    private static native void nativeRefresh(long nativeSharedRealmPtr);
    private static native long[]  nativeGetVersionID(long nativeSharedRealmPtr);
    private static native long nativeGetTable(long nativeSharedRealmPtr, String tableName);
    private static native String nativeGetTableName(long nativeSharedRealmPtr, int index);
    private static native boolean nativeHasTable(long nativeSharedRealmPtr, String tableName);
    private static native void nativeRenameTable(long nativeSharedRealmPtr, String oldTableName, String newTableName);
    private static native void nativeRemoveTable(long nativeSharedRealmPtr, String tableName);
    private static native long nativeSize(long nativeSharedRealmPtr);
    private static native void nativeWriteCopy(long nativeSharedRealmPtr, String path, byte[] key);
    private static native boolean nativeWaitForChange(long nativeSharedRealmPtr);
    private static native void nativeStopWaitForChange(long nativeSharedRealmPtr);
    private static native boolean nativeCompact(long nativeSharedRealmPtr);
    private static native void nativeUpdateSchema(long nativePtr, long nativeSchemaPtr, long version);
    private static native void nativeSetAutoRefresh(long nativePtr, boolean enabled);
    private static native boolean nativeIsAutoRefresh(long nativePtr);
    private static native boolean nativeRequiresMigration(long nativePtr, long nativeSchemaPtr);
    private static native long nativeGetFinalizerPtr();
}
