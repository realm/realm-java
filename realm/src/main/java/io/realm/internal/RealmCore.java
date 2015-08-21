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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * Utility methods for Realm Core.
 */
public class RealmCore {

///*
    private static final String FILE_SEP = File.separator;
    private static final String PATH_SEP = File.pathSeparator;          // On Windows ";"
    private static final String BINARIES_PATH = "lib" + PATH_SEP + ".." + FILE_SEP + "lib";
    private static final String JAVA_LIBRARY_PATH = "java.library.path";
    private static final String JNILIB_NAME= "earthworm";
//*/

    private static volatile boolean libraryIsLoaded = false;


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

    // Although loadLibrary is synchronized internally from AOSP 4.3, for the compatibility reason,
    // KEEP synchronized here for the old devices!
    public static synchronized void loadLibrary() {
        if (libraryIsLoaded) {
            // The java native should ensure only load the lib once, but we met some problems before.
            // So keep the flag.
            return;
        }

        else {
            System.loadLibrary(JNILIB_NAME);
        }

        libraryIsLoaded = true;

        Version.coreLibVersionCompatible(true);
    }
}
