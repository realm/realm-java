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

package io.realm.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;


public class RealmVersionChecker {
    public static final String REALM_ANDROID_DOWNLOAD_URL = "http://static.realm.io/downloads/java/latest";

    private static final String VERSION_URL = "http://static.realm.io/update/java?";
    private static final String REALM_VERSION = Version.VERSION;
    private static final String REALM_VERSION_PATTERN = "\\d+\\.\\d+\\.\\d+";
    private static final int READ_TIMEOUT = 2000;
    private static final int CONNECT_TIMEOUT = 4000;

    private static RealmVersionChecker instance = null;

    private ProcessingEnvironment processingEnvironment;

    public static RealmVersionChecker getInstance(ProcessingEnvironment processingEnvironment) {
        if (instance == null) {
            instance = new RealmVersionChecker(processingEnvironment);
        }
        return instance;
    }

    private RealmVersionChecker(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    public void executeRealmVersionUpdate() {
        Thread backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                launchRealmCheck();
            }
        });

        backgroundThread.start();

        try {
            backgroundThread.join(CONNECT_TIMEOUT + READ_TIMEOUT);
        } catch (InterruptedException ignore) {
            // We ignore this exception on purpose not to break the build system if this class fails
        }
    }

    private void launchRealmCheck() {
        //Check Realm version server
        String latestVersionStr = checkLatestVersion();
        if (reportNewVersion(latestVersionStr, REALM_VERSION)) {
            printMessage("Version " + latestVersionStr + " of Realm is now available: " + REALM_ANDROID_DOWNLOAD_URL);
        }
    }

    /**
     * Checks if the latest release should be reported to the user.
     *
     * Note: Public for testing.
     *
     * @param newVersion new version string.
     * @param currentVersion current version string.
     * @return returns {@code true} if the new version detected should be reported to the user, {@code false} if not.
     */

    public static boolean reportNewVersion(String newVersion, String currentVersion) {
        if (newVersion == null || newVersion.equals("")) {
            // Invalid version strings should be ignored
            return false;
        }
        if (!newVersion.matches(REALM_VERSION_PATTERN)) {
            // Releases not following a standard major.minor.patch format should not be
            // considered a standard release, so we do not want to notify people about
            // these.
            return false;
        }

        // In all other cases assume that any change is cause for an upgrade.
        return !newVersion.equals(currentVersion);
    }

    private String checkLatestVersion() {
        String result = REALM_VERSION;
        try {
            URL url = new URL(VERSION_URL + REALM_VERSION);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String latestVersion = rd.readLine();
            // if the obtained string does not match the pattern, we are in a separate network.
            if (latestVersion.matches(REALM_VERSION_PATTERN)) {
                result = latestVersion;
            }
            rd.close();
        } catch (IOException e) {
            // We ignore this exception on purpose not to break the build system if this class fails
        }
        return result;
    }

    private void printMessage(String message) {
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.OTHER, message);
    }
}
