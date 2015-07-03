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
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class is used to serialize tables to either disk or memory. It consists of a collection of tables.
 */
public class Group implements Closeable {

    protected long nativePtr;
    protected boolean immutable;
    private final Context context;

    static {
        RealmCore.loadLibrary();
    }

    //
    // Group construction and destruction
    //

    private void checkNativePtrNotZero() {
        if (this.nativePtr == 0)
            // FIXME: It is wrong to assume that a null pointer means 'out
            // of memory'. An out of memory condition in
            // createNative() must be handled by having createNative()
            // throw OutOfMemoryError.
            throw new OutOfMemoryError("Out of native memory.");
    }

    public Group() {
        this.immutable = false;
        this.context = new Context();
        this.nativePtr = createNative();
        checkNativePtrNotZero();
    }

    protected native long createNative();

    public enum OpenMode {
        // Below values must match the values in realm::group::OpenMode in C++
        READ_ONLY(0),
        READ_WRITE(1),
        READ_WRITE_NO_CREATE(2);
        private int value;

        private OpenMode(int value) {
            this.value = value;
        }
    }

    ;

    public Group(String filepath, OpenMode mode) {
        this.immutable = mode.equals(OpenMode.READ_ONLY);

        this.context = new Context();
        this.nativePtr = createNative(filepath, mode.value);
        checkNativePtrNotZero();
    }

    protected native long createNative(String filepath, int value);

    public Group(String filepath) {
        this(filepath, OpenMode.READ_ONLY);
    }

    public Group(File file) {
        this(file.getAbsolutePath(), file.canWrite() ? OpenMode.READ_WRITE : OpenMode.READ_ONLY);
    }

    public Group(byte[] data) {
        this.immutable = false;
        this.context = new Context();
        if (data != null) {
            this.nativePtr = createNative(data);
            checkNativePtrNotZero();
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected native long createNative(byte[] data);

    public Group(ByteBuffer buffer) {
        this.immutable = false;
        this.context = new Context();
        if (buffer != null) {
            this.nativePtr = createNative(buffer);
            checkNativePtrNotZero();
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected native long createNative(ByteBuffer buffer);

    Group(Context context, long nativePointer, boolean immutable) {
        this.context = context;
        this.nativePtr = nativePointer;
        this.immutable = immutable;
    }

    // If close() is called, no penalty is paid for delayed disposal
    // via the context
    public void close() {
        synchronized (context) {
            if (nativePtr != 0) {
                nativeClose(nativePtr);
                nativePtr = 0;
            }
        }
    }

    protected static native void nativeClose(long nativeGroupPtr);

    /**
     * Checks if a group has been closed and can no longer be used.
     *
     * @return True if closed, false otherwise.
     */
    boolean isClosed() {
        return nativePtr == 0;
    }

    protected void finalize() {
        synchronized (context) {
            if (nativePtr != 0) {
                context.asyncDisposeGroup(nativePtr);
                nativePtr = 0; // Set to 0 if finalize is called before close() for some reason
            }
        }
    }

    //
    // Group methods
    //

    private void verifyGroupIsValid() {
        if (nativePtr == 0) {
            throw new IllegalStateException("Illegal to call methods on a closed Group.");
        }
    }

    public long size() {
        verifyGroupIsValid();
        return nativeSize(nativePtr);
    }

    protected native long nativeSize(long nativeGroupPtr);

    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Checks whether table exists in the Group.
     *
     * @param name The name of the table.
     * @return true if the table exists, otherwise false.
     */
    public boolean hasTable(String name) {
        verifyGroupIsValid();
        return name != null && nativeHasTable(nativePtr, name);
    }

    protected native boolean nativeHasTable(long nativeGroupPtr, String name);

    public String getTableName(int index) {
        verifyGroupIsValid();
        long cnt = size();
        if (index < 0 || index >= cnt) {
            throw new IndexOutOfBoundsException(
                    "Table index argument is out of range. possible range is [0, "
                            + (cnt - 1) + "]");
        }
        return nativeGetTableName(nativePtr, index);
    }

    protected native String nativeGetTableName(long nativeGroupPtr, int index);

    /**
     * Returns a table with the specified name.
     *
     * @param name The name of the table.
     * @return The table if it exists, otherwise create it.
     */
    public Table getTable(String name) {
        verifyGroupIsValid();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Invalid name. Name must be a non-empty String.");
        }
        if (immutable) {
            if (!hasTable(name)) {
                throw new IllegalStateException("Requested table is not in this Realm. " +
                        "Creating it requires a transaction: " + name);
            }
        }

        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeTablePointer = nativeGetTableNativePtr(nativePtr, name);
        try {
            // Copy context reference from parent
            return new Table(context, this, nativeTablePointer);
        } catch (RuntimeException e) {
            Table.nativeClose(nativeTablePointer);
            throw e;
        }
    }

    protected native long nativeGetTableNativePtr(long nativeGroupPtr, String name);

    protected native void nativeWriteToFile(long nativeGroupPtr, String fileName, byte[] keyArray)
            throws IOException;

    /**
     * Serialize the group to the specific file on the disk using encryption.
     *
     * @param file A File object representing the file.
     * @param key A 64 bytes long byte array containing the key to the encrypted Realm file. Can be null if encryption
     * is not required.
     */
    public void writeToFile(File file, byte[] key) throws IOException {
        verifyGroupIsValid();
        if (file.isFile() && file.exists()) {
            throw new IllegalArgumentException("The destination file must not exist");
        }
        if (key != null && key.length != 64) {
            throw new IllegalArgumentException("Realm AES keys must be 64 bytes long");
        }

        nativeWriteToFile(nativePtr, file.getAbsolutePath(), key);
    }

    protected static native long nativeLoadFromMem(byte[] buffer);

    /**
     * Serialize the group to a memory buffer. The byte[] is owned by the JVM.
     *
     * @return Binary array of the serialized group.
     */
    public byte[] writeToMem() {
        verifyGroupIsValid();
        return nativeWriteToMem(nativePtr);
    }

    protected native byte[] nativeWriteToMem(long nativeGroupPtr);
/*
 * TODO: Find a way to release the malloc'ed native memory automatically

    public ByteBuffer writeToByteBuffer() {
        verifyGroupIsValid();
        return nativeWriteToByteBuffer(nativePtr);
    }

    protected native ByteBuffer nativeWriteToByteBuffer(long nativeGroupPtr);
*/

    public void commit() {
        verifyGroupIsValid();
        nativeCommit(nativePtr);
    }

    public String toJson() {
        return nativeToJson(nativePtr);
    }

    protected native String nativeToJson(long nativeGroupPtr);

    public String toString() {
        return nativeToString(nativePtr);
    }

    protected native void nativeCommit(long nativeGroupPtr);

    protected native String nativeToString(long nativeGroupPtr);

    private void throwImmutable() {
        throw new IllegalStateException("Objects cannot be changed outside a transaction; see beginTransaction() for " +
                "details.");
    }
}
