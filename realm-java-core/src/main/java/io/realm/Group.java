package io.realm;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.realm.typed.TightDB;

/**
 * This class is used to serialize tables to either disk or memory. It consists
 * of a collection of tables.
 */
public class Group implements Closeable {
    
    protected long nativePtr;
    protected boolean immutable;
    private final Context context;

    static {
        TightDB.loadLibrary();
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
    };

    public Group(String filepath, OpenMode mode) {
        if (mode.equals(OpenMode.READ_ONLY))
            this.immutable = true;
        else
            this.immutable = false;
        
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
        if (nativePtr == 0)
            throw new IllegalStateException("Illegal to call methods on a closed Group.");
    }


    public long size() {
        verifyGroupIsValid();
        return nativeSize(nativePtr);
    }

    protected native long nativeSize(long nativeGroupPtr);


    public boolean isEmpty(){
        return size() == 0;
    }


    /**
     * Checks whether table exists in the Group.
     *
     * @param name
     *            The name of the table.
     * @return true if the table exists, otherwise false.
     */
    public boolean hasTable(String name) {
        verifyGroupIsValid();
        if (name == null)
            return false;
        return nativeHasTable(nativePtr, name);
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
     * @param name
     *            The name of the table.
     * @return The table if it exists, otherwise create it.
     */
    public Table getTable(String name) {
        verifyGroupIsValid();
        if (name == null || name.equals(""))
            throw new IllegalArgumentException("Invalid name. Name must be a non-empty String.");
        if (immutable)
            if (!hasTable(name))
                throwImmutable();
        
        // Execute the disposal of abandoned realm objects each time a new realm object is created
        context.executeDelayedDisposal();
        long nativeTablePointer = nativeGetTableNativePtr(nativePtr, name);
        try {
            // Copy context reference from parent
            return new Table(context, this, nativeTablePointer);
        }
        catch (RuntimeException e) {
            Table.nativeClose(nativeTablePointer);
            throw e;
        }
    }

    protected native long nativeGetTableNativePtr(long nativeGroupPtr, String name);

    /**
     * Writes the group to the specific file on the disk.
     *
     * @param fileName
     *            The file of the file.
     * @throws IOException
     */
    public void writeToFile(String fileName) throws IOException {
        verifyGroupIsValid();
        if (fileName == null)
            throw new IllegalArgumentException("fileName is null");
        File file = new File(fileName);
        writeToFile(file);
    }

    protected native void nativeWriteToFile(long nativeGroupPtr, String fileName)
            throws IOException;

    /**
     * Serialize the group to the specific file on the disk.
     *
     * @param file
     *            A File object representing the file.
     * @throws IOException
     */
    public void writeToFile(File file) throws IOException {
        verifyGroupIsValid();
        nativeWriteToFile(nativePtr, file.getAbsolutePath());
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

    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (!(other instanceof Group))
            return false;

        Group otherGroup = (Group) other;
        return nativeEquals(nativePtr, otherGroup.nativePtr);
    }

    protected native boolean nativeEquals(long nativeGroupPtr, long nativeGroupToComparePtr);

    private void throwImmutable() {
        throw new IllegalStateException("Mutable method call during read transaction.");
    }
}
