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

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

public class RealmVersionChecker {
    public static final String REALM_ANDROID_DOWNLOAD_URL = "http://static.realm.io/downloads/java/latest";

    private static RealmVersionChecker instance = null;
    private static boolean isFirstRound = true;

    private static final String VERSION_URL = "http://static.realm.io/update/java?";
    private static final String REALM_VERSION = Version.VERSION;
    private static final int READ_TIMEOUT = 2000;
    private static final int CONNECT_TIMEOUT = 4000;

    private ProcessingEnvironment processingEnvironment;

    private RealmVersionChecker(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    public static RealmVersionChecker getInstance(ProcessingEnvironment processingEnvironment) {
        if (instance == null) {
            instance = new RealmVersionChecker(processingEnvironment);
        }
        return instance;
    }

    private void launchRealmCheck() {
        //Check Realm version server
        String latestVersionStr = checkLatestVersion();
        if (!latestVersionStr.equals(REALM_VERSION)) {
            printMessage("Version " + latestVersionStr + " of Realm is now available: " + REALM_ANDROID_DOWNLOAD_URL);
        }
    }

    public void executeRealmVersionUpdate() {
        if (isFirstRound) {
            isFirstRound = false;
            Thread backgroundThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    launchRealmCheck();
                }
            });
            backgroundThread.start();
            try {
                backgroundThread.join(CONNECT_TIMEOUT + READ_TIMEOUT);
            } catch (InterruptedException e) {
                // We ignore this exception on purpose not to break the build system if this class fails
            }
        }
    }

    private String checkLatestVersion() {
        String result = REALM_VERSION;
        try {
            URL url = new URL(VERSION_URL + REALM_VERSION);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            result = rd.readLine();
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
