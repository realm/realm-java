package com.tightdb;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.tightdb.internal.CloseMutex;
import com.tightdb.typed.TightDB;

/**
 * This class is used to serialize tables to either disk or memory. It consists
 * of a collection of tables.
 */
public class Group {

	static {
		TightDB.loadLibrary();
	}

    private void checkNativePtr() {
        if (this.nativePtr == 0)
            // FIXME: It is wrong to assume that a null pointer means 'out
            // of memory'. An out of memory condition in
            // createNative() must be handled by having createNative()
            // throw OutOfMemoryError.
            throw new OutOfMemoryError("Out of native memory.");
    }

	public Group() {
		this.nativePtr = createNative();
		checkNativePtr();
	}

	protected native long createNative();

	public Group(File file) {
		this(file.getAbsolutePath(), !file.canWrite());
		checkNativePtr();
	}

	public Group(String fileName, boolean readOnly) {
		this.nativePtr = createNative(fileName, readOnly);
		checkNativePtr();
	}

	public Group(String fileName) {
		this(fileName, true);
		checkNativePtr();
	}

	protected native long createNative(String filename, boolean readOnly);

    public Group(byte[] data) {
        if (data != null) {
            this.nativePtr = createNative(data);
            checkNativePtr();
        } else {
            throw new IllegalArgumentException();
        }
    }

	protected native long createNative(byte[] data);

	public Group(ByteBuffer buffer) {
		if (buffer != null) {
			this.nativePtr = createNative(buffer);
			checkNativePtr();
		} else {
			throw new IllegalArgumentException();
		}
	}

	protected native long createNative(ByteBuffer buffer);

	protected Group(long nativePtr, boolean immutable) {
		this.immutable = immutable;
		this.nativePtr = nativePtr;
		checkNativePtr();
	}

	protected void finalize() {
		// System.err.println("FINALIZE GROUP -------------- this " + this +
		// "   native " + nativePtr);
		close();
	}

	public void close() {
		synchronized (CloseMutex.getInstance()) {
			if (nativePtr != 0) {
				nativeClose(nativePtr);
				nativePtr = 0;
			}
		}
	}

	protected native void nativeClose(long nativeGroupPtr);

	public long size() {
		return nativeSize(nativePtr);
	}

	protected native long nativeSize(long nativeGroupPtr);

	/**
	 * Checks whether table exists in the Group.
	 * 
	 * @param name
	 *            The name of the table.
	 * @return true if the table exists, otherwise false.
	 */
	public boolean hasTable(String name) {
		if (name == null)
			return false;
		return nativeHasTable(nativePtr, name);
	}

	protected native boolean nativeHasTable(long nativeGroupPtr, String name);

	public String getTableName(int index) {
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
		if (immutable)
			if (!hasTable(name))
				throwImmutable();
		return new Table(this, nativeGetTableNativePtr(nativePtr, name),
				immutable);
	}

	protected native long nativeGetTableNativePtr(long nativeGroupPtr,
			String name);

	/**
	 * Writes the group to the specific file on the disk.
	 * 
	 * @param fileName
	 *            The file of the file.
	 * @throws IOException
	 */
	public void writeToFile(String fileName) throws IOException,
			NullPointerException {
		if (fileName == null)
			throw new NullPointerException("file name is null");
		File file = new File(fileName);
		writeToFile(file);
	}

	protected native void nativeWriteToFile(long nativeGroupPtr, String fileName)
			throws Exception;

	/**
	 * Serialize the group to the specific file on the disk.
	 * 
	 * @param file
	 *            A File object representing the file.
	 * @throws IOException
	 */
	public void writeToFile(File file) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		try {
			nativeWriteToFile(nativePtr, file.getAbsolutePath());
		} catch (Exception ex) {
			throw new IOException(ex.getMessage());
		}
	}

	protected static native long nativeLoadFromMem(byte[] buffer);

	/**
	 * Serialize the group to a memory buffer. The byte[] is owned by the JVM.
	 * 
	 * @return Binary array of the serialized group.
	 */
	public byte[] writeToMem() {
		return nativeWriteToMem(nativePtr);
	}

	protected native byte[] nativeWriteToMem(long nativeGroupPtr);
/*
 * TODO: Find a way to release the malloc'ed native memory automatically

	public ByteBuffer writeToByteBuffer() {
		return nativeWriteToByteBuffer(nativePtr);
	}

	protected native ByteBuffer nativeWriteToByteBuffer(long nativeGroupPtr);
*/
	
	private void throwImmutable() {
		throw new IllegalStateException(
				"Mutable method call during read transaction.");
	}

	protected long nativePtr;
	protected boolean immutable = false;
}
