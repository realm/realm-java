package com.tightdb.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import com.tightdb.util;

public class TightDB {
	
	private static final String PATH_SEP = System.getProperty("path.separator");
	private static final String JAVA_LIBRARY_PATH = "java.library.path";
	private static final String BINARIES_PATH = "lib" + PATH_SEP + "../lib";
			
	private static boolean loadedLibrary;

	private static String getJniFileName()
	{
		String os = System.getProperty("os.name").toLowerCase(); 
		if (os.indexOf("win") >= 0)
			return "tightdb_jni??.dll";	
		if (os.indexOf("mac") >= 0)
			return "libtightdb-jni.jnilib";
		if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("sunos") >= 0)
			return "libtightdb-jni.so";
		return "tightdb-jni";
	}
	
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
			try {
				addNativeLibraryPath(BINARIES_PATH);
				resetLibraryPath();
			}
			catch (Throwable e) {
				// Above can't be used on Android.
			}
			// Load debug library first - if available
			loadedLibrary = loadCorrectLibrary("tightdb-jnid", "tightdb_jni32d", "tightdb_jni64d");
			if (loadedLibrary) {
				System.out.println("!!! TightDB debug version loaded. !!!\n");
			} else {
				loadedLibrary = loadCorrectLibrary("tightdb-jni", "tightdb_jni32", "tightdb_jni64");
			}
			if (!loadedLibrary) {
				throw new RuntimeException("Couldn't load the TightDB library. Please add '" + getJniFileName() + 
						"' as external jar in your project.");
			}
		}
		if (!util.versionCompatible()) {
			throw new RuntimeException("Tightdb jar and Tightdb lib are incompatible. Please check your installation.");
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
			String libraryPath = System.getProperty(JAVA_LIBRARY_PATH) + PATH_SEP + path + PATH_SEP;
			System.setProperty(JAVA_LIBRARY_PATH, libraryPath);
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
}
