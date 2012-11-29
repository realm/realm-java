package com.tightdb;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.tightdb.internal.CloseMutex;
import com.tightdb.lib.TightDB;

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
		this.nativePtr = createNative(data);
		checkNativePtr();
	}

	protected native long createNative(byte[] data);

	public Group(ByteBuffer buffer) {
		this.nativePtr = createNative(buffer);
		checkNativePtr();
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
				if (isValid()) {
					// System.err.println("CLOSE GROUP -------------- this " +
					// this + "   native " + nativePtr);
					nativeClose(nativePtr);
				} else {
					// System.err.println("CLOSE GROUP - INVALID!!!!");
				}
				nativePtr = 0;
			}
		}
	}

	protected native void nativeClose(long nativeGroupPtr);

	public boolean isValid() {
		return nativeIsValid(nativePtr);
	}

	protected native boolean nativeIsValid(long nativeGroupPtr);

	public long getTableCount() {
		return nativeGetTableCount(nativePtr);
	}

	protected native long nativeGetTableCount(long nativeGroupPtr);

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
		long cnt = getTableCount();
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
	public TableBase getTable(String name) {
		if (immutable)
			if (!hasTable(name))
				throwImmutable();
		return new TableBase(this, nativeGetTableNativePtr(nativePtr, name),
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
	 * Serialize the group to a memory buffer.
	 * 
	 * @return Binary array of the serialized group.
	 */
	public byte[] writeToMem() {
		return nativeWriteToMem(nativePtr);
	}

	protected native byte[] nativeWriteToMem(long nativeGroupPtr);

	public ByteBuffer writeToByteBuffer() {
		return nativeWriteToByteBuffer(nativePtr);
	}

	protected native ByteBuffer nativeWriteToByteBuffer(long nativeGroupPtr);

	private void throwImmutable() {
		throw new IllegalStateException(
				"Mutable method call during read transaction.");
	}

	protected long nativePtr;
	protected boolean immutable = false;
}
