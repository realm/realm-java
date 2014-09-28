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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

public class RealmVersionChecker {

    public static final String UPDATE_FILE_NAME = "realm_version_check.timestmp";
    public static final String REALM_ANDROID_DOWNLOAD_URL = "http://static.realm.io/downloads/android/latest";

    private static final String versionUrlStr = "http://static.realm.io/update/java?"; 
    //The version value would ideally be pulled from a build file
    private static final String REALM_VERSION = "0.50.0";

    private static final int READ_TIMEOUT = 2000;
    private static final int CONNECT_TIMEOUT = 4000;

    private void launchRealmCheck() {
        long lastRealmUpdate = readRealmStat();

        if ((lastRealmUpdate + (24 * 60 * 60 * 1000)) < System.currentTimeMillis()) {
            updateLastRealmStat();
            //Check Realm version server
            String latestVersionStr = checkLatestVersion();

            if (!latestVersionStr.equals(REALM_VERSION)) {
                System.out.println("Version " + latestVersionStr + " of Realm is now available: " + REALM_ANDROID_DOWNLOAD_URL);
            }
        }
    }

    public void executeRealmVersionUpdate() {
        Thread bgT = new Thread(new Runnable() {
            @Override
            public void run() {
                launchRealmCheck();
            }
        });
        bgT.start();
        try {
            bgT.join(CONNECT_TIMEOUT + READ_TIMEOUT);
        } catch(InterruptedException e) {
            //e.printStackTrace();
        }
    }

    private Long readRealmStat() {
        InputStream in = null;
        BufferedReader reader = null;

        String lastVersionStr = null;
        try {
            in     = new FileInputStream(UPDATE_FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
            lastVersionStr = reader.readLine();
            in.close();
        } catch (FileNotFoundException f) {
            //f.printStackTrace();
            return 0L;
        } catch (IOException e) {
            //e.printStackTrace();
            return 0L;
        }

        try {
            long retL = new Long(lastVersionStr);
            //System.out.println("Stored version found: " + retL);
            return retL;
        } catch (NumberFormatException ne) {
            return 0L;
        }
    }

    private void updateLastRealmStat() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(UPDATE_FILE_NAME, "UTF-8");
            writer.println(System.currentTimeMillis());
            writer.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private String checkLatestVersion() {
        String result = REALM_VERSION;
        try {
            URL url = new URL(versionUrlStr+REALM_VERSION);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            result = rd.readLine();
            rd.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        //System.out.println("Latest version found: " + result);
        return result;
    }
}
