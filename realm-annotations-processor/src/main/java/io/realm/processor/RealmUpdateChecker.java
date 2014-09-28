package io.realm.processor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class RealmUpdateChecker {

    public static final String UPDATE_FILE = "last_realm_version";

    private static final String versionUrlStr = "http://static.realm.io/update/java?"; 
    //The version value would ideally be pulled from a build file
    private static final String REALM_VERSION = "0.70.0";

    private void launchRealmCheck() {
        long lastRealmUpdate = readRealmStat();

        if ((lastRealmUpdate + (24 * 60 * 60 * 1000)) < System.currentTimeMillis()) {
            updateLastRealmStat();
            //Check Realm version server
            String latestVersionStr = checkLatestVersion();

            if (!latestVersionStr.equals(REALM_VERSION)) {
                System.out.println("Version " + latestVersionStr + " of Realm is now available: http://static.realm.io/downloads/android/latest");
            }
        }
    }

    public void executeRealmVersionUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                launchRealmCheck();
            }
        }).start();
    }

    private Long readRealmStat() {
        InputStream in = null;
        BufferedReader reader = null;

        String lastVersionStr = null;
        try {
            in     = new FileInputStream(UPDATE_FILE);
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
            return retL;
        } catch (NumberFormatException ne) {
            return 0L;
        }
    }

    private void updateLastRealmStat() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(UPDATE_FILE, "UTF-8");
            writer.println(System.currentTimeMillis());
            writer.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private String checkLatestVersion() {
        String result = REALM_VERSION;
        try {
            URL url = new URL(versionUrlStr);
            URLConnection conn = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            result = rd.readLine();
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Latest version: " + result);
        return result;
    }
}
