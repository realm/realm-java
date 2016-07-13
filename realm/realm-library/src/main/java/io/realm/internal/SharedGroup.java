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

package io.realm.internal;

import java.io.Closeable;
import java.io.IOError;
import java.util.concurrent.TimeUnit;

import io.realm.exceptions.IncompatibleLockFileException;
import io.realm.exceptions.RealmError;
import io.realm.exceptions.RealmIOException;
import io.realm.internal.async.BadVersionException;
import io.realm.internal.log.RealmLog;

public class SharedGroup implements Closeable {

    // Keep these public so we can ask users to experiment with these values if needed.
    // Should be locked down as soon as possible.
    public static long[] INCREMENTAL_BACKOFF_MS = new long[] {1, 10, 20, 50, 100, 200, 400}; // Will keep re-using last value until LIMIT is hit
    public static long INCREMENTAL_BACKOFF_LIMIT_MS = 3000;

    public static final boolean IMPLICIT_TRANSACTION = true;
    public static final boolean EXPLICIT_TRANSACTION = false;

    private static final boolean CREATE_FILE_YES = false;
    private static final boolean CREATE_FILE_NO = true;
    private static final boolean ENABLE_REPLICATION = true;
    private static final boolean DISABLE_REPLICATION = false;

    private final String path;
    private long nativePtr;
    private long nativeReplicationPtr;
    private boolean implicitTransactionsEnabled = false;
    private boolean activeTransaction;
    private final Context context;

    public enum Durability {
        FULL(0),
        MEM_ONLY(1);
        //ASYNC(2); // TODO: re-enable when possible

        final int value;

        Durability(int value) {
            this.value = value;
        }
    }

    // TODO Only used by Unit tests. Remove?
    public SharedGroup(String databaseFile) {
        context = new Context();
        path = databaseFile;
        nativePtr = nativeCreate(databaseFile, Durability.FULL.value, CREATE_FILE_YES, DISABLE_REPLICATION, null);
        checkNativePtrNotZero();
    }

    public SharedGroup(String canonicalPath, boolean enableImplicitTransactions, Durability durability, byte[] key) {
        if (enableImplicitTransactions) {
            nativeReplicationPtr = nativeCreateReplication(canonicalPath, key);
            nativePtr = openSharedGroupOrFail(durability, key);
            implicitTransactionsEnabled = true;
        } else {
            nativePtr = nativeCreate(canonicalPath, Durability.FULL.value, CREATE_FILE_YES, DISABLE_REPLICATION, key);
        }
        context = new Context();
        path = canonicalPath;
        checkNativePtrNotZero();
    }

    private long openSharedGroupOrFail(Durability durability, byte[] key) {
        // We have anecdotal evidence that on some versions of Android it is possible for two versions of an app
        // to exist in two processes during an app upgrade. This is problematic since the lock file might not be
        // compatible across two versions of Android. See https://github.com/realm/realm-java/issues/2459. If this
        // happens we assume the overlap is really small so instead of failing outright we retry using incremental
        // backoff.
        int i = 0;
        final long start = System.nanoTime();
        RuntimeException lastError = null;
        while (TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS) < INCREMENTAL_BACKOFF_LIMIT_MS) {
            try {
                long nativePtr = createNativeWithImplicitTransactions(nativeReplicationPtr, durability.value, key);
                if (i > 0) {
                    RealmLog.w("IncompatibleLockFile was detected. Error was resolved after " + i + " retries");
                }
                return nativePtr;
            } catch (IncompatibleLockFileException e) {
                i++;
                lastError = e;
                try {
                    Thread.sleep(getSleepTime(i));
                    RealmLog.d("Waiting for another process to release the Realm file: " + path);
                } catch (InterruptedException ignored) {
                    RealmLog.d("Waiting for Realm to open interrupted: " + path);
                }
            }
        }

        throw new RealmError("Could not open the Realm file: " + lastError.getMessage());
    }

    // Returns the time to sleep before retrying opening the SharedGroup.
    private static long getSleepTime(int tries) {
        if (INCREMENTAL_BACKOFF_MS == null) {
            return 0;
        } else {
            if (tries > INCREMENTAL_BACKOFF_MS.length) {
                return INCREMENTAL_BACKOFF_MS[INCREMENTAL_BACKOFF_MS.length - 1];
            } else {
                return INCREMENTAL_BACKOFF_MS[tries - 1];
            }
        }
    }

    // TODO Only used by Unit tests. Remove?
    public SharedGroup(String canonicalPath, Durability durability, byte[] key) {
        path = canonicalPath;
        context = new Context();
        nativePtr = nativeCreate(canonicalPath, durability.value, false, false, key);
        checkNativePtrNotZero();
    }

    void advanceRead() {
        nativeAdvanceRead(nativePtr);
    }

    void advanceRead(VersionID versionID) throws BadVersionException {
        nativeAdvanceReadToVersion(nativePtr, versionID.version, versionID.index);
    }

    void promoteToWrite() {
        nativePromoteToWrite(nativePtr);
    }

    void commitAndContinueAsRead() {
        nativeCommitAndContinueAsRead(nativePtr);
    }

    void rollbackAndContinueAsRead() {
        nativeRollbackAndContinueAsRead(nativePtr);
    }

    public ImplicitTransaction beginImplicitTransaction() {
        if (activeTransaction) {
            throw new IllegalStateException(
                    "Can't beginImplicitTransaction() during another active transaction");
        }
        long nativeGroupPtr = nativeBeginImplicit(nativePtr);
        ImplicitTransaction transaction = new ImplicitTransaction(context, this, nativeGroupPtr);
        activeTransaction = true;
        return transaction;
    }

    public WriteTransaction beginWrite() {
        if (activeTransaction)
            throw new IllegalStateException(
                    "Can't beginWrite() during another active transaction");
        // FIXME: throw from nativeMethod in case of error

        long nativeWritePtr = nativeBeginWrite(nativePtr);
        try {
            // Copy context reference from parent
            WriteTransaction t = new WriteTransaction(context, this, nativeWritePtr);
            activeTransaction = true;
            return t;
        } catch (RuntimeException e) {
            Group.nativeClose(nativeWritePtr);
            throw e;
        }
    }

    public ReadTransaction beginRead() {
        if (activeTransaction)
            throw new IllegalStateException("Can't beginRead() during another active transaction");
        // FIXME: throw from nativeMethod in case of error

        long nativeReadPtr = nativeBeginRead(nativePtr);
        try {
            // Copy context reference from parent
            ReadTransaction t = new ReadTransaction(context, this, nativeReadPtr);
            activeTransaction = true;
            return t;
        } catch (RuntimeException e) {
            Group.nativeClose(nativeReadPtr);
            throw e;
        }
    }

    void endRead() {
        if (isClosed())
            throw new IllegalStateException("Can't endRead() on closed group. " +
                    "ReadTransaction is invalid.");
        nativeEndRead(nativePtr);
        activeTransaction = false;
    }

    public void close() {
        synchronized (context) {
            if (nativePtr != 0) {
                nativeClose(nativePtr);
                nativePtr = 0;
                if (implicitTransactionsEnabled && nativeReplicationPtr != 0) {
                    nativeCloseReplication(nativeReplicationPtr);
                    nativeReplicationPtr = 0;
                }
            }
        }
    }

    void commit() {
        if (isClosed())
            throw new IllegalStateException(
                    "Can't commit() on closed group. WriteTransaction is invalid.");
        nativeCommit(nativePtr);
        activeTransaction = false;
    }

    void rollback() {
        if (isClosed())
            throw new IllegalStateException(
                    "Can't rollback() on closed group. WriteTransaction is invalid.");
        nativeRollback(nativePtr);
        activeTransaction = false;
    }

    public boolean isClosed() {
        return nativePtr == 0;
    }

    public boolean hasChanged() {
        return nativeHasChanged(nativePtr);
    }

    public void reserve(long bytes) {
        nativeReserve(nativePtr, bytes);
    }

    /**
     * Compacts a shared group. This will block access to the shared group until done.
     *
     * @return {@code true} if compaction succeeded, {@code false} otherwise.
     * @throws RuntimeException if using this within either a read or or write transaction.
     */
    public boolean compact() {
        return nativeCompact(nativePtr);
    }

    /**
     * Returns the absolute path to the file backing this SharedGroup.
     *
     * @return the canonical path to the Realm file.
     */
    public String getPath() {
        return path;
    }

    private void checkNativePtrNotZero() {
        if (this.nativePtr == 0) {
            throw new IOError(new RealmIOException("Realm could not be opened"));
        }
    }

    public long getNativePointer () {
        return nativePtr;
    }

    public long getNativeReplicationPointer () {
        return nativeReplicationPtr;
    }

    public VersionID getVersion () {
        long[] versionId = nativeGetVersionID (nativePtr);
        return new VersionID (versionId[0], versionId[1]);

    }

    public static class VersionID implements Comparable<VersionID> {
        final long version;
        final long index;

        VersionID(long version, long index) {
            this.version = version;
            this.index = index;
        }

        @Override
        public int compareTo(VersionID another) {
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
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            if (!super.equals(object)) return false;

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

    /**
     * Waits for change committed by {@link SharedGroup} in other Thread.
     *
     * @return {@code true} if successfully detects change, {@code false} no change has been detected otherwise.
     */
    public boolean waitForChange() {
        return nativeWaitForChange(nativePtr);
    }

    /**
     * Stops waiting for change.
     */
    public void stopWaitForChange() {
        nativeStopWaitForChange(nativePtr);
    }

    private native long createNativeWithImplicitTransactions(long nativeReplicationPtr,
                                                             int durability, byte[] key);
    private native long nativeCreateReplication(String databaseFile, byte[] key);
    private native void nativeCommitAndContinueAsRead(long nativePtr);
    private native long nativeBeginImplicit(long nativePtr);

    private native void nativeReserve(long nativePtr, long bytes);
    private native boolean nativeHasChanged(long nativePtr);
    private native long nativeBeginRead(long nativePtr);
    private native void nativeEndRead(long nativePtr);
    private native long nativeBeginWrite(long nativePtr);
    private native void nativeCommit(long nativePtr);
    private native void nativeRollback(long nativePtr);
    private native long nativeCreate(String databaseFile,
                                     int durabilityValue,
                                     boolean dontCreateFile,
                                     boolean enableReplication,
                                     byte[] key);
    private native boolean nativeCompact(long nativePtr);
    protected static native void nativeClose(long nativePtr);
    private native void nativeCloseReplication(long nativeReplicationPtr);
    private native void nativeRollbackAndContinueAsRead(long nativePtr);
    private native long[] nativeGetVersionID (long nativePtr);
    private native boolean nativeWaitForChange(long nativePtr);
    private native void nativeStopWaitForChange(long nativePtr);
    private native void nativeAdvanceRead(long nativePtr);
    private native void nativeAdvanceReadToVersion(long nativePtr, long version, long index) throws BadVersionException;
    private native void nativePromoteToWrite(long nativePtr);
}
