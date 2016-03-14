/**
 * Copyright 2015 KeepSafe Software, Inc.
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
package io.realm.internal.android;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * ReLinker is a small library to help alleviate {@link UnsatisfiedLinkError} exceptions thrown due
 * to Android's inability to properly install / load native libraries for Android versions before
 * API 21
 */
@SuppressWarnings("deprecation")
@SuppressWarnings("PMD")
public class ReLinker {
    private static final String LIB_DIR = "lib";
    private static final int MAX_TRIES = 5;
    private static final int COPY_BUFFER_SIZE = 4096;

    private ReLinker() {
        // No instances
    }

    /**
     * Utilizes the regular system call to attempt to load a native library. If a failure occurs,
     * then the function extracts native .so library out of the app's APK and attempts to load it.
     * <p>
     *     <strong>Note: This is a synchronous operation</strong>
     */
    public static void loadLibrary(final Context context, final String library) {
        if (context == null) {
            throw new IllegalArgumentException("Given context is null");
        }

        if (TextUtils.isEmpty(library)) {
            throw new IllegalArgumentException("Given library is either null or empty");
        }

        try {
            System.loadLibrary(library);
            return;
        } catch (final UnsatisfiedLinkError ignored) {
            // :-(
        }

        final File workaroundFile = getWorkaroundLibFile(context, library);
        if (!workaroundFile.exists()) {
            unpackLibrary(context, library);
        }

        System.load(workaroundFile.getAbsolutePath());
    }

    /**
     * @param context {@link Context} to describe the location of it's private directories
     * @return A {@link File} locating the directory that can store extracted libraries
     * for later use
     */
    private static File getWorkaroundLibDir(final Context context) {
        return context.getDir(LIB_DIR, Context.MODE_PRIVATE);
    }

    /**
     * @param context {@link Context} to retrieve the workaround directory from
     * @param library The name of the library to load
     * @return A {@link File} locating the workaround library file to load
     */
    private static File getWorkaroundLibFile(final Context context, final String library) {
        final String libName = System.mapLibraryName(library);
        return new File(getWorkaroundLibDir(context), libName);
    }

    /**
     * Attempts to unpack the given library to the workaround directory. Implements retry logic for
     * IO operations to ensure they succeed.
     *
     * @param context {@link Context} to describe the location of the installed APK file
     * @param library The name of the library to load
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void unpackLibrary(final Context context, final String library) {
        ZipFile zipFile = null;
        try {
            final ApplicationInfo appInfo = context.getApplicationInfo();
            int tries = 0;
            while (tries++ < MAX_TRIES) {
                try {
                    zipFile = new ZipFile(new File(appInfo.sourceDir), ZipFile.OPEN_READ);
                    break;
                } catch (IOException ignored) {}
            }

            if (zipFile == null) {
                return;
            }

            tries = 0;
            while (tries++ < MAX_TRIES) {
                String jniNameInApk = null;
                ZipEntry libraryEntry = null;

                if (Build.VERSION.SDK_INT >= 21 && Build.SUPPORTED_ABIS.length > 0) {
                    for (final String ABI : Build.SUPPORTED_ABIS) {
                        jniNameInApk = "lib/" + ABI + "/" + System.mapLibraryName(library);
                        libraryEntry = zipFile.getEntry(jniNameInApk);

                        if (libraryEntry != null) {
                            break;
                        }
                    }
                } else {
                    //noinspection deprecation
                    jniNameInApk = "lib/" + Build.CPU_ABI + "/" + System.mapLibraryName(library);
                    libraryEntry = zipFile.getEntry(jniNameInApk);
                }

                if (libraryEntry == null) {
                    // Does not exist in the APK
                    if (jniNameInApk != null) {
                        throw new MissingLibraryException(jniNameInApk);
                    } else {
                        throw new MissingLibraryException(library);
                    }
                }

                final File outputFile = getWorkaroundLibFile(context, library);
                outputFile.delete(); // Remove any old file that might exist

                try {
                    if (!outputFile.createNewFile()) {
                        continue;
                    }
                } catch (IOException ignored) {
                    // Try again
                    continue;
                }

                InputStream inputStream = null;
                FileOutputStream fileOut = null;
                try {
                    inputStream = zipFile.getInputStream(libraryEntry);
                    fileOut = new FileOutputStream(outputFile);
                    copy(inputStream, fileOut);
                } catch (FileNotFoundException e) {
                    // Try again
                    continue;
                } catch (IOException e) {
                    // Try again
                    continue;
                } finally {
                    closeSilently(inputStream);
                    closeSilently(fileOut);
                }

                // Change permission to rwxr-xr-x
                outputFile.setReadable(true, false);
                outputFile.setExecutable(true, false);
                outputFile.setWritable(true);
                break;
            }
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException ignored) {}
        }
    }

    /**
     * Copies all data from an {@link InputStream} to an {@link OutputStream}.
     *
     * @param in The stream to read from.
     * @param out The stream to write to.
     * @throws IOException when a stream operation fails.
     */
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[COPY_BUFFER_SIZE];
        while (true) {
            int read = in.read(buf);
            if (read == -1) {
                break;
            }
            out.write(buf, 0, read);
        }
    }

    /**
     * Closes a {@link Closeable} silently (without throwing or handling any exceptions)
     * @param closeable {@link Closeable} to close
     */
    private static void closeSilently(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ignored) {}
    }

    private static class MissingLibraryException extends RuntimeException {
        public MissingLibraryException(final String library) {
            super(library);
        }
    }
}
