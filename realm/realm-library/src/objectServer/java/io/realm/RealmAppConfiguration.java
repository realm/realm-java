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

import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.realm.log.LogLevel;

/**
 * FIXME
 */
public class RealmAppConfiguration {

    private final String appId;
    private final String appName;
    private final String appVersion;
    private final String baseUrl;
    private final Context context;
    private final SyncSession.ErrorHandler defaultErrorHandler;
    private final byte[] encryptionKey;
    private final long logLevel;
    private final long requestTimeoutMs;

    public RealmAppConfiguration(String appId,
                                 String appName,
                                 String appVersion,
                                 String baseUrl,
                                 Context context,
                                 SyncSession.ErrorHandler defaultErrorHandler,
                                 byte[] encryptionKey,
                                 long logLevel,
                                 long requestTimeoutMs) {

        this.appId = appId;
        this.appName = appName;
        this.appVersion = appVersion;
        this.baseUrl = baseUrl;
        this.context = context;
        this.defaultErrorHandler = defaultErrorHandler;
        this.encryptionKey = encryptionKey;
        this.logLevel = logLevel;
        this.requestTimeoutMs = requestTimeoutMs;
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
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * FIXME
     * @return
     */
    public Context getContext() {
        return context;
    }

    /**
     * FIXME
     * @return
     */
    public SyncSession.ErrorHandler getDefaultErrorHandler() {
        return defaultErrorHandler;
    }

    /**
     * FIXME
     * @return
     */
    public byte[] getEncryptionKey() {
        return encryptionKey;
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
     */
    public static class Builder {
        private String appId;
        private String appName;
        private String appVersion;
        private String baseUrl;
        private Context context;
        private SyncSession.ErrorHandler defaultErrorHandler;
        private byte[] encryptionKey;
        private long logLevel = LogLevel.WARN;
        private long requestTimeoutMs = 60000;

        /**
         * FIXME
         *
         * @param context
         * @param appId
         */
        public Builder(Context context, String appId) {
            // FIXME: Null checks
            this.context = context;
            this.appId = appId;
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
            this.encryptionKey = key;
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
         * @param errorHandler
         * @return
         */
        public Builder defaultSessionErrorHandler(@Nullable SyncSession.ErrorHandler errorHandler) {
            // FIXME checks
            this.defaultErrorHandler = errorHandler;
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
            // FIXME checks
            this.requestTimeoutMs = TimeUnit.MICROSECONDS.convert(time, unit);
            return this;
        }

        public RealmAppConfiguration build() {
            return new RealmAppConfiguration(appId,
                    appName,
                    appVersion,
                    baseUrl,
                    context,
                    defaultErrorHandler,
                    encryptionKey,
                    logLevel,
                    requestTimeoutMs);
        }
    }


}
