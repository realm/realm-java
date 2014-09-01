package com.tightdb.typed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import com.tightdb.internal.util;

/**
 * Utility methods for TightDB.
 */
public class TightDB {

    private static final String PATH_SEP = System.getProperty("path.separator");
    private static final String JAVA_LIBRARY_PATH = "java.library.path";
    private static final String BINARIES_PATH = "lib" + PATH_SEP + "../lib";

    private static boolean loadedLibrary;

    private static String getJniFileName()
    {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0)
            return "tightdb_jni32.dll or tightdb_jni64.dll";
        if (os.indexOf("mac") >= 0)
            return "libtightdb-jni.jnilib";
        if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("sunos") >= 0)
            return "libtightdb-jni.so";
        return "tightdb-jni";
    }

    public static boolean osIsWindows()
    {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
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

    public static void print(String caption, AbstractTableOrView<? extends AbstractCursor<?>, ?, ?> tableOrView) {
        String format = "%-15s| ";
        System.out.println(String.format("================== %s ====================", caption));
        if (!tableOrView.isEmpty()) {
            for (AbstractColumn<?, ?, ?, ?> column : tableOrView.get(0).columns()) {
                System.out.print(String.format(format, column.getName()));
            }
            System.out.println();

            for (int i = 0; i < tableOrView.size(); i++) {
                AbstractCursor<?> p = tableOrView.get(i);
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

    public static void print(AbstractTableOrView<? extends AbstractCursor<?>, ?, ?> tableOrView) {
        print(tableOrView.getName(), tableOrView);
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
                System.err.println("Searched JAVA_LIBRARY_PATH=" + System.getProperty(JAVA_LIBRARY_PATH));
                throw new RuntimeException("Couldn't load the TightDB JNI library '" + getJniFileName() +
                        "'. Please include the directory to the library in java.library.path.");
            }
        }
        if (!util.versionCompatible()) {
            throw new RuntimeException("tightdb-jni jar and tightdb-jni lib are incompatible. Please check your installation.");
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
            String libraryPath = System.getProperty(JAVA_LIBRARY_PATH) + PATH_SEP + path + PATH_SEP;
            System.setProperty(JAVA_LIBRARY_PATH, libraryPath);
        } catch (Exception e) {
            throw new RuntimeException("Cannot set the library path!", e);
        }
    }

    // Hack for having a cross platform location for the lib:
    // The Classloader has a static field (sys_paths) that contains the paths.
    // If that field is set to null, it is initialized automatically.
    // Therefore forcing that field to null will result into the reevaluation of the library path
    // as soon as loadLibrary() is called

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
