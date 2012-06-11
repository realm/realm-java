package com.tightdb.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Date;


import com.sun.xml.internal.bind.v2.runtime.RuntimeUtil;
import com.tightdb.Mixed;

public class TightDB {

	private static final String JAVA_LIBRARY_PATH = "java.library.path";
	private static final String BINARIES_PATH = "./lib";

	private static boolean loadedLibrary;

	public static byte[] serialize(Serializable value) {
		try {
			ByteArrayOutputStream mem = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(mem);
			output.writeObject(value);
			output.close();
			return mem.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Cannot serialize the object!", e);
		}
	}

	public static Serializable deserialize(ByteBuffer buf) {
		return deserialize(buf.array());
	}

	public static Serializable deserialize(byte[] value) {
		try {
			ByteArrayInputStream mem = new ByteArrayInputStream(value);
			ObjectInputStream output = new ObjectInputStream(mem);
			Object obj = output.readObject();
			output.close();
			return (Serializable) obj;
		} catch (Exception e) {
			throw new RuntimeException("Cannot deserialize the object!", e);
		}
	}

	public static void print(String caption, AbstractRowset<? extends AbstractCursor<?>, ?, ?> rowset) {
		String format = "%-15s| ";
		System.out.println(String.format("================== %s ====================", caption));
		if (!rowset.isEmpty()) {
			for (AbstractColumn<?, ?, ?, ?> column : rowset.at(0).columns()) {
				System.out.print(String.format(format, column.getName()));
			}
			System.out.println();

			for (int i = 0; i < rowset.size(); i++) {
				AbstractCursor<?> p = rowset.at(i);
				for (AbstractColumn<?, ?, ?, ?> column : p.columns()) {
					System.out.print(String.format(format, column.getReadableValue()));
				}
				System.out.println();
			}
			System.out.println();
		} else {
			System.out.println(" - No records to show!");
		}
	}

	public static void print(AbstractRowset<? extends AbstractCursor<?>, ?, ?> rowset) {
		print(rowset.getName(), rowset);
	}

	public static void print(String caption, AbstractCursor<?> cursor) {
		System.out.println(caption + ": " + cursor);
	}

	/**
	 * Guarantee gc is done.
	 */
	public static void gcGuaranteed(){
        Object obj = new Object();
        WeakReference<Object> ref = new WeakReference<Object>(obj);
        obj = null;
        while(ref.get()!=null)
            System.gc();
    }

	/**
	 * Guarantee gc is done after JVM shutdown.
	 */
	public static void gcOnExit(){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
            	gcGuaranteed();
            }
        });
    }

	private static void initTightDB() {
		gcOnExit();
	}
	
	public static void loadLibrary() {
		if (!loadedLibrary) {
			initTightDB();
			addNativeLibraryPath(BINARIES_PATH);
			resetLibraryPath();
			loadedLibrary = loadCorrectLibrary("tightdb_jni32d", "tightdb_jni64d", "tightdb_jni32", "tightdb_jni64");
			if (!loadedLibrary) {
				throw new RuntimeException("Couldn't load the TightDB library. Please add 'lib/tightdb_jni??' as external jar.");
			}
		}
	}

	private static boolean loadCorrectLibrary(String... libraryCandidateNames) {
		for (String libraryCandidateName : libraryCandidateNames) {
			try {
				System.loadLibrary(libraryCandidateName);
				return true;
			} catch (Throwable e) {
			}
		}

		return false;
	}

	public static void addNativeLibraryPath(String path) {
		try {
			//System.out.println("JAVA_LIBRARY_PATH=" + System.getProperty(JAVA_LIBRARY_PATH));
			System.setProperty(JAVA_LIBRARY_PATH, System.getProperty(JAVA_LIBRARY_PATH) + ";" + path + ";");
		} catch (Exception e) {
			throw new RuntimeException("Cannot set the library path!", e);
		}
	}

	private static void resetLibraryPath() {
		try {
			// reset the library path (a hack)
			Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (Exception e) {
			throw new RuntimeException("Cannot reset the library path!", e);
		}
	}

	public static Mixed mixedValue(Object value) {
		Mixed mixed;
		if (value instanceof String) {
			mixed = new Mixed((String) value);
		} else if (value instanceof Long) {
			mixed = new Mixed((Long) value);
		} else if (value instanceof Integer) {
			mixed = new Mixed(new Long(((Integer) value).intValue()));
		} else if (value instanceof Boolean) {
			mixed = new Mixed((Boolean) value);
		} else if (value instanceof Date) {
			mixed = new Mixed((Date) value);
		} else if (value instanceof ByteBuffer) {
			mixed = new Mixed((ByteBuffer) value);
		} else if (value instanceof byte[]) {
			throw new IllegalArgumentException("Not implemented yet!"); // FIXME:
																		// implement
																		// this
		} else {
			throw new IllegalArgumentException("The value is of unsupported type: " + value.getClass());
		}
		return mixed;
	}
}
