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

import android.content.Context;

import com.getkeepsafe.relinker.ReLinker;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Locale;

import io.realm.BuildConfig;


/**
 * Utility methods for Realm Core.
 */
public class RealmCore {

    private static final String FILE_SEP = File.separator;
    private static final String PATH_SEP = File.pathSeparator;          // On Windows ";"
    private static final String BINARIES_PATH = "lib" + PATH_SEP + ".." + FILE_SEP + "lib";
    private static final String JAVA_LIBRARY_PATH = "java.library.path";

    private static boolean libraryIsLoaded = false;

    public static boolean osIsWindows() {
        String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        return (os.contains("win"));
    }

    /**
     * Loads the .so file. Typically, the .so file is installed and can be found by System.loadLibrary() but
     * can be damaged or missing. This happens for the Android installer, especially when apps are installed
     * through other means than the official Play store. In this case, the .so file can be found in the .apk.
     * In other to access the .apk, an {@link android.content.Context} must be provided.
     * <p>
     * Although loadLibrary is synchronized internally from AOSP 4.3, for compatibility reasons,
     * KEEP synchronized here for old devices!
     */
    public static synchronized void loadLibrary(Context context) {
        if (libraryIsLoaded) {
            return;
        }
        ReLinker.loadLibrary(context, "realm-jni", BuildConfig.VERSION_NAME);
        libraryIsLoaded = true;
    }

    private static String loadLibraryWindows() {
///*
        try {
            addNativeLibraryPath(BINARIES_PATH);
            resetLibraryPath();
        } catch (Throwable e) {
            // Above can't be used on Android.
        }
//*/
        // Loads debug library first - if available.
        String jnilib;
        jnilib = loadCorrectLibrary("realm_jni32d", "realm_jni64d");
        if (jnilib != null) {
            System.out.println("!!! Realm debug version loaded. !!!\n");
        } else {
            jnilib = loadCorrectLibrary("realm_jni32", "realm_jni64");
            if (jnilib == null) {
                System.err.println("Searched java.library.path=" + System.getProperty("java.library.path"));
                throw new RuntimeException("Couldn't load the Realm JNI library 'realm_jni32.dll or realm_jni64.dll" +
                        "'. Please include the directory to the library in java.library.path.");
            }
        }
        return jnilib;
    }

    private static String loadCorrectLibrary(String... libraryCandidateNames) {
        for (String libraryCandidateName : libraryCandidateNames) {
            try {
                System.loadLibrary(libraryCandidateName);
                return libraryCandidateName;
            } catch (Throwable ignored) {
            }
        }
        return null;
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
    // The ClassLoader has a static field (sys_paths) that contains the paths.
    // If that field is set to null, it is initialized automatically.
    // Therefore forcing that field to null will result into the reevaluation of the library path
    // as soon as loadLibrary() is called.
    private static void resetLibraryPath() {
        try {
            // Resets the library path (a hack).
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Cannot reset the library path!", e);
        }
    }
}
