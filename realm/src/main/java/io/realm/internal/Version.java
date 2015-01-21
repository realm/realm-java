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

public class Version {

    // Set by tools/set-version.sh
    static final int REALM_JAVA_MAJOR = 0;
    static final int REALM_JAVA_MINOR = 77;
    static final int REALM_JAVA_PATCH = 0;

    static final int CORE_MIN_MAJOR = 0;
    static final int CORE_MIN_MINOR = 1;
    static final int CORE_MIN_PATCH = 6;
    static final int REQUIRED_JNI_VERSION = 23;

    public enum Feature {
        Feature_Debug(0),
        Feature_Replication(1);

        private Feature(int nativeValue)
        {
            this.nativeFeature = nativeValue;
        }

        @SuppressWarnings("unused")
        private final int nativeFeature;
    }

    public static String getCoreVersion() {
        return nativeGetVersion();
    }

    public static String getVersion() {
        return String.format("%d.%d.%d", REALM_JAVA_MAJOR, REALM_JAVA_MINOR, REALM_JAVA_PATCH);
    }

    public static boolean hasFeature(Feature feature) {
        return nativeHasFeature(feature.ordinal());
    }

    public static boolean coreLibVersionCompatible(boolean throwIfNot) {
        String errTxt = "";
        boolean compatible = nativeIsAtLeast(CORE_MIN_MAJOR, CORE_MIN_MINOR, CORE_MIN_PATCH);
        if (!compatible) {
            errTxt = "Version mismatch between realm.jar ("
                    + CORE_MIN_MAJOR + "." + CORE_MIN_MINOR + "." + CORE_MIN_PATCH
                    + ") and native core library (" + getCoreVersion() + ")";
            if (throwIfNot)
                throw new RuntimeException(errTxt);
            System.err.println(errTxt);
            return false;
        }

        compatible = (nativeGetAPIVersion() == REQUIRED_JNI_VERSION);
        if (!compatible) {
            errTxt = "Native lib API is version " + nativeGetAPIVersion()
                     + " != " +  REQUIRED_JNI_VERSION + " which is expected by the jar.";
            if (throwIfNot)
                throw new RuntimeException(errTxt);
            System.err.println(errTxt);                    
        }
        return compatible;
    }

    static native String nativeGetVersion();
    static native boolean nativeHasFeature(int feature);    
    static native boolean nativeIsAtLeast(int major, int minor, int patch);    
    static native int nativeGetAPIVersion();
}
