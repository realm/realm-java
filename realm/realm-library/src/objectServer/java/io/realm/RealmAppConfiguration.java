/*
 * Copyright 2020 Realm Inc.
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
package io.realm;

import android.content.Context;

import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.IterableCodecProvider;
import org.bson.codecs.MapCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.realm.internal.Util;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;

/**
 * FIXME
 */
public class RealmAppConfiguration {

    private final String appId;
    private final String appName;
    private final String appVersion;
    private final URL baseUrl;
    private final SyncSession.ErrorHandler defaultErrorHandler;
    @Nullable private final byte[] encryptionKey;
    private final long logLevel;
    private final long requestTimeoutMs;
    private final String authorizationHeaderName;
    private final Map<String, String> customHeaders;
    private final File syncRootDir; // Root directory for storing Sync related files
    private final CodecRegistry codecRegistry;

    private RealmAppConfiguration(String appId,
                                 String appName,
                                 String appVersion,
                                 String baseUrl,
                                 SyncSession.ErrorHandler defaultErrorHandler,
                                 @Nullable byte[] encryptionKey,
                                 long logLevel,
                                 long requestTimeoutMs,
                                 String authorizationHeaderName,
                                 Map<String, String> customHeaders,
                                 File syncRootdir,
                                 CodecRegistry codecRegistry) {

        this.appId = appId;
        this.appName = appName;
        this.appVersion = appVersion;
        this.baseUrl = createUrl(baseUrl);
        this.defaultErrorHandler = defaultErrorHandler;
        this.encryptionKey = (encryptionKey == null) ? null : Arrays.copyOf(encryptionKey, encryptionKey.length);
        this.logLevel = logLevel;
        this.requestTimeoutMs = requestTimeoutMs;
        this.authorizationHeaderName = (!Util.isEmptyString(authorizationHeaderName)) ? authorizationHeaderName : "Authorization";
        this.customHeaders = Collections.unmodifiableMap(customHeaders);
        this.syncRootDir = syncRootdir;
        this.codecRegistry = codecRegistry;
    }

    private URL createUrl(String baseUrl) {
        try {
            return new URL(baseUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(baseUrl);
        }
    }

    /**
     * FIXME
     * @return
     */
    public String getAppId() {
        return appId;
    }

    /**
     * FIXME
     * @return
     */
    public String getAppName() {
        return appName;
    }

    /**
     * FIXME
     * @return
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * FIXME
     * @return
     */
    public URL getBaseUrl() {
        return baseUrl;
    }

    /**
     * FIXME
     * @return
     */
    public byte[] getEncryptionKey() {
        return encryptionKey == null ? null : Arrays.copyOf(encryptionKey, encryptionKey.length);
    }

    /**
     * FIXME
     * @return
     */
    public long getLogLevel() {
        return logLevel;
    }

    /**
     * FIXME
     * @return
     */
    public long getRequestTimeoutMs() {
        return requestTimeoutMs;
    }


    /**
     * FIXME
     *
     * @return
     */
    public String getAuthorizationHeaderName() {
        return authorizationHeaderName;
    }

    /**
     * FIXME
     *
     * @return
     */
    public Map<String, String> getCustomRequestHeaders() {
        return customHeaders;
    }

    /**
     * FIXME
     *
     * @return
     */
    public SyncSession.ErrorHandler getDefaultErrorHandler() {
        return defaultErrorHandler;
    }

    /**
     * Returns the root folder containing all files and Realms used when synchronizing data
     * between the device and MongoDB Realm.
     */
    public File getSyncRootDirectory() {
        return syncRootDir;
    }

    // FIXME Doc
    public CodecRegistry getDefaultCodecRegistry() { return codecRegistry; }

    /**
     * FIXME
     */
    public static class Builder {
        // Default BSON codec for passing BSON to/from JNI
        static CodecRegistry DEFAULT_BSON_CODEC_REGISTRY = CodecRegistries.fromRegistries(
                CodecRegistries.fromProviders(
                        // For primitive support
                        new ValueCodecProvider(),
                        // For BSONValue support
                        new BsonValueCodecProvider(),
                        new DocumentCodecProvider(),
                        // For list support
                        new IterableCodecProvider(),
                        new MapCodecProvider()
                )
        );

        private String appId;
        private String appName;
        private String appVersion;
        private String baseUrl = "https://stitch.mongodb.com"; // FIXME Find the correct base url for release
        private SyncSession.ErrorHandler defaultErrorHandler = new SyncSession.ErrorHandler() {
            @Override
            public void onError(SyncSession session, ObjectServerError error) {
                if (error.getErrorCode() == ErrorCode.CLIENT_RESET) {
                    RealmLog.error("Client Reset required for: " + session.getConfiguration().getServerUrl());
                    return;
                }

                String errorMsg = String.format(Locale.US, "Session Error[%s]: %s",
                        session.getConfiguration().getServerUrl(),
                        error.toString());
                switch (error.getErrorCode().getCategory()) {
                    case FATAL:
                        RealmLog.error(errorMsg);
                        break;
                    case RECOVERABLE:
                        RealmLog.info(errorMsg);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported error category: " + error.getErrorCode().getCategory());
                }
            }
        };
        private byte[] encryptionKey;
        private long logLevel = LogLevel.WARN; // FIXME: Consider what this should be set at
        private long requestTimeoutMs = 60000;
        private String autorizationHeaderName;
        private Map<String, String> customHeaders = new HashMap<>();
        private File syncRootDir;
        private CodecRegistry codecRegistry = DEFAULT_BSON_CODEC_REGISTRY;

        /**
         * FIXME
         *
         * @param appId
         */
        public Builder(String appId) {
            Util.checkEmpty(appId, "appId");
            this.appId = appId;
            Context context = BaseRealm.applicationContext;
            if (context == null) {
                throw new IllegalStateException("Call `Realm.init(Context)` before calling this method.");
            }
            File rootDir = new File(context.getFilesDir(), "mongodb-realm");
            if (!rootDir.exists() && !rootDir.mkdir()) {
                throw new IllegalStateException("Could not create Sync root dir: " + rootDir.getAbsolutePath());
            }
            syncRootDir = rootDir;
        }

        /**
         * FIXME
         *
         * @param level
         * @return
         */
        public Builder logLevel(int level) {
            // FIXME: Boundary checks
            this.logLevel = level;
            return this;
        }

        /**
         * FIXME
         *
         * @param key
         * @return
         */
        public Builder encryptionKey(byte[] key) {
            this.encryptionKey = Arrays.copyOf(key, key.length);
            return this;
        }

        /**
         * FIXME
         *
         * @param baseUrl
         * @return
         */
        public Builder baseUrl(String baseUrl) {
            // FIXME Check input
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * FIXME
         *
         * @param appName
         * @return
         */
        public Builder appName(String appName) {
            // FIXME CHecks
            this.appName = appName;
            return this;
        }

        /**
         * FIXME
         *
         * @param appVersion
         * @return
         */
        public Builder appVersion(String appVersion) {
            // FIXME checks
            this.appVersion = appVersion;
            return this;
        }

        /**
         * FIXME
         *
         * @param time
         * @param unit
         * @return
         */
        public Builder requestTimeout(long time, TimeUnit unit) {
            if (time < 1) {
                throw new IllegalStateException("A timeout above 0 is required: " + time);
            }
            Util.checkNull(unit, "unit");
            this.requestTimeoutMs = TimeUnit.MICROSECONDS.convert(time, unit);
            return this;
        }

        /**
         * Sets the name of the HTTP header used to send authorization data in when making requests to
         * MongoDB Realm. The MongoDB server or firewall must have been configured to expect a
         * custom authorization header.
         * <p>
         * The default authorization header is named "Authorization".
         *
         * @param headerName name of the header.
         * @throws IllegalArgumentException if a null or empty header is provided.
         * @see <a href="https://docs.realm.io/platform/guides/learn-realm-sync-and-integrate-with-a-proxy#adding-a-custom-proxy">Adding a custom proxy</a>
         */
        public Builder authorizationHeaderName(String headerName) {
            Util.checkEmpty(headerName, "headerName");
            this.autorizationHeaderName = headerName;
            return this;
        }

        /**
         * Adds an extra HTTP header to append to every request to a Realm Object Server.
         *
         * @param headerName the name of the header.
         * @param headerValue the value of header.
         * @throws IllegalArgumentException if a non-empty {@code headerName} is provided or a null {@code headerValue}.
         */
        public Builder addCustomRequestHeader(String headerName, String headerValue) {
            Util.checkEmpty(headerName, "headerName");
            Util.checkNull(headerValue, "headerValue");
            customHeaders.put(headerName, headerValue);
            return this;
        }

        /**
         * Adds extra HTTP headers to append to every request to a Realm Object Server.
         *
         * @param headers map of (headerName, headerValue) pairs.
         * @throws IllegalArgumentException If any of the headers provided are illegal.
         */
        public Builder addCustomRequestHeaders(@Nullable Map<String, String> headers) {
            if (headers != null) {
                customHeaders.putAll(headers);
            }
            return this;
        }

        /**
         *
         * @param errorHandler
         * @return
         */
        public Builder defaultSyncErrorHandler(SyncSession.ErrorHandler errorHandler) {
            Util.checkNull(errorHandler, "errorHandler");
            defaultErrorHandler = errorHandler;
            return this;
        }

        /**
         * Configures the root folder containing all files and Realms used when synchronizing data
         * between the device and MongoDB Realm.
         * <p>
         * The default root dir is {@code Context.getFilesDir()/mongodb-realm}.
         * </p>
         * @param rootDir where to store sync related files.
         */
        public Builder syncRootDirectory(File rootDir) {
            Util.checkNull(rootDir, "rootDir");
            if (rootDir.isFile()) {
                throw new IllegalArgumentException("'rootDir' is a file, not a directory: " +
                        rootDir.getAbsolutePath() + ".");
            }
            if (!rootDir.exists() && !rootDir.mkdirs()) {
                throw new IllegalArgumentException("Could not create the specified directory: " +
                        rootDir.getAbsolutePath() + ".");
            }
            if (!rootDir.canWrite()) {
                throw new IllegalArgumentException("Realm directory is not writable: " +
                        rootDir.getAbsolutePath() + ".");
            }
            syncRootDir = rootDir;
            return this;
        }

        // FIXME Doc
        public Builder codecRegistry(CodecRegistry codecRegistry) {
            Util.checkNull(codecRegistry, "codecRegistry");
            this.codecRegistry = codecRegistry;
            return this;
        }

        public RealmAppConfiguration build() {
            return new RealmAppConfiguration(appId,
                    appName,
                    appVersion,
                    baseUrl,
                    defaultErrorHandler,
                    encryptionKey,
                    logLevel,
                    requestTimeoutMs,
                    autorizationHeaderName,
                    customHeaders,
                    syncRootDir,
                    codecRegistry);
        }
    }
}
