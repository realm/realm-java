/*
 * Copyright 2015 Realm Inc.
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Set;

// Asynchronously submits build information to Realm when the annotation
// processor is running
//
// To be clear: this does *not* run when your app is in production or on
// your end-user's devices; it will only run when you build your app from source.
//
// Why are we doing this? Because it helps us build a better product for you.
// None of the data personally identifies you, your employer or your app, but it
// *will* help us understand what Realm version you use, what host OS you use,
// etc. Having this info will help with prioritizing our time, adding new
// features and deprecating old features. Collecting an anonymized bundle &
// anonymized MAC is the only way for us to count actual usage of the other
// metrics accurately. If we don't have a way to deduplicate the info reported,
// it will be useless, as a single developer building their app on Windows ten
// times would report 10 times more than a single developer that only builds
// once from Mac OS X, making the data all but useless. No one likes sharing
// data unless it’s necessary, we get it, and we’ve debated adding this for a
// long long time. Since Realm is a free product without an email signup, we
// feel this is a necessary step so we can collect relevant data to build a
// better product for you.
//
// Currently the following information is reported:
// - What version of Realm is being used
// - What OS you are running on
// - An anonymized MAC address and bundle ID to aggregate the other information on.
public class RealmAnalytics {
    private static RealmAnalytics instance;
    private static final int READ_TIMEOUT = 2000;
    private static final int CONNECT_TIMEOUT = 4000;
    private static final String ADDRESS_PREFIX = "http://api.mixpanel.com/track/?data=";
    private static final String ADDRESS_SUFFIX = "&ip=1";
    private static final String TOKEN = "ce0fac19508f6c8f20066d345d360fd0";
    private static final String EVENT_NAME = "Run";
    private static final String JSON_TEMPLATE
            = "{\n"
            + "   \"event\": \"%EVENT%\",\n"
            + "   \"properties\": {\n"
            + "      \"token\": \"%TOKEN%\",\n"
            + "      \"distinct_id\": \"%USER_ID%\",\n"
            + "      \"Anonymized MAC Address\": \"%USER_ID%\",\n"
            + "      \"Anonymized Bundle ID\": \"%APP_ID%\",\n"
            + "      \"Binding\": \"java\",\n"
            + "      \"Realm Version\": \"%REALM_VERSION%\",\n"
            + "      \"Host OS Type\": \"%OS_TYPE%\",\n"
            + "      \"Host OS Version\": \"%OS_VERSION%\"\n"
            + "      \"Target OS Type\": \"android\"\n"
            + "   }\n"
            + "}";

    // The list of packages the model classes reside in
    private Set<String> packages;

    private RealmAnalytics(Set<String> packages) {
        this.packages = packages;
    }

    public static RealmAnalytics getInstance(Set<String> packages) {
        if (instance == null) {
            instance = new RealmAnalytics(packages);
        }
        return instance;
    }

    private void send() {
        try {
            URL url = getUrl();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            connection.getResponseCode();
        } catch (IOException ignored) {
        } catch (NoSuchAlgorithmException ignored) {
        }
    }

    public void execute() {
        Thread backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                send();
            }
        });
        backgroundThread.start();
        try {
            backgroundThread.join(CONNECT_TIMEOUT + READ_TIMEOUT);
        } catch (InterruptedException e) {
            // We ignore this exception on purpose not to break the build system if this class fails
        }
    }

    public URL getUrl() throws
            MalformedURLException,
            SocketException,
            NoSuchAlgorithmException,
            UnsupportedEncodingException {
        return new URL(ADDRESS_PREFIX + Utils.base64Encode(generateJson()) + ADDRESS_SUFFIX);
    }

    public String generateJson() throws SocketException, NoSuchAlgorithmException {
        return JSON_TEMPLATE
                .replaceAll("%EVENT%", EVENT_NAME)
                .replaceAll("%TOKEN%", TOKEN)
                .replaceAll("%USER_ID%", getAnonymousUserId())
                .replaceAll("%APP_ID%", getAnonymousAppId())
                .replaceAll("%REALM_VERSION%", Version.VERSION)
                .replaceAll("%OS_TYPE%", System.getProperty("os.name"))
                .replaceAll("%OS_VERSION%", System.getProperty("os.version"));
    }

    /**
     * Compute an anonymous user id from the hashed MAC address of the first network interface
     * @return the anonymous user id
     * @throws NoSuchAlgorithmException
     * @throws SocketException
     */
    public static String getAnonymousUserId() throws NoSuchAlgorithmException, SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        if (!networkInterfaces.hasMoreElements()) {
            throw new IllegalStateException("No network interfaces detected");
        }

        NetworkInterface networkInterface = networkInterfaces.nextElement();
        byte[] hardwareAddress = networkInterface.getHardwareAddress(); // Normally this is the MAC address

        return Utils.hexStringify(Utils.sha256Hash(hardwareAddress));
    }

    /**
     * Compute an anonymous app/library id from the packages containing Realm model classes
     * @return the anonymous app/library id
     * @throws NoSuchAlgorithmException
     */
    public String getAnonymousAppId() throws NoSuchAlgorithmException {
        StringBuilder stringBuilder = new StringBuilder();
        for (String modelPackage : packages) {
            stringBuilder.append(modelPackage).append(":");
        }
        byte[] packagesBytes = stringBuilder.toString().getBytes();

        return Utils.hexStringify(Utils.sha256Hash(packagesBytes));
    }
}
