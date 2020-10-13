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

package io.realm.internal.network;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.annotation.Nullable;

import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.mongodb.log.obfuscator.HttpLogObfuscator;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

/**
 * The LoggingInterceptor prints information on the HTTP requests produced by a Realm app.
 */
public class LoggingInterceptor implements Interceptor {

    public static final String LOGIN_FEATURE = "providers";

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Nullable
    private HttpLogObfuscator httpLogObfuscator;

    LoggingInterceptor(@Nullable HttpLogObfuscator httpLogObfuscator) {
        this.httpLogObfuscator = httpLogObfuscator;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (RealmLog.getLevel() <= LogLevel.DEBUG) {
            StringBuilder sb = new StringBuilder(request.method());
            sb.append(' ');
            sb.append(request.url());
            sb.append('\n');
            sb.append(request.headers());
            if (request.body() != null) {
                // Stripped down version of https://github.com/square/okhttp/blob/master/okhttp-logging-interceptor/src/main/java/okhttp3/logging/HttpLoggingInterceptor.java
                // We only expect request context to be JSON.
                Buffer buffer = new Buffer();
                request.body().writeTo(buffer);

                // Obfuscate sensitive information if applicable
                String input = buffer.readString(UTF8);
                if (httpLogObfuscator != null) {
                    input = httpLogObfuscator.obfuscate(request.url().pathSegments(), input);
                }
                sb.append(input);
            }
            RealmLog.debug("HTTP Request = \n%s", sb);
        }
        return chain.proceed(request);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoggingInterceptor)) return false;
        LoggingInterceptor that = (LoggingInterceptor) o;
        if (httpLogObfuscator == null) {
            return that.httpLogObfuscator == null;
        }
        return httpLogObfuscator.equals(that.httpLogObfuscator);
    }

    @Override
    public int hashCode() {
        if (httpLogObfuscator == null) {
            return super.hashCode();
        }
        return httpLogObfuscator.hashCode() + 27;
    }
}
