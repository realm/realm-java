/*
 * Copyright 2016 Realm Inc.
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

package io.realm.log;

import android.util.Log;

import static android.util.Log.getStackTraceString;

/**
 * Logger implementation outputting to Android LogCat.
 * Androids {@link Log}levels are mapped to Realm {@link LogLevel}s using the following table:
 *
 * <table summary="Comparison of Realm and Android log levels">
 * <tr>
 *     <td>{@link LogLevel#ALL}</td><td>{@link Log#VERBOSE}</td>
 *     <td>{@link LogLevel#TRACE}</td><td>{@link Log#VERBOSE}</td>
 *     <td>{@link LogLevel#DEBUG}</td><td>{@link Log#DEBUG}</td>
 *     <td>{@link LogLevel#INFO}</td><td>{@link Log#INFO}</td>
 *     <td>{@link LogLevel#WARN}</td><td>{@link Log#WARN}</td>
 *     <td>{@link LogLevel#ERROR}</td><td>{@link Log#ERROR}</td>
 *     <td>{@link LogLevel#FATAL}</td><td>{@link Log#ERROR}</td>
 *     <td>{@link LogLevel#OFF}</td><td>Not supported. Remove the logger instead.</td>
 * </tr>
 * </table>
 *
 * @deprecated The new {@link RealmLogger} for Android is implemented in native code. This class will be removed in a
 * future release.
 */
public class AndroidLogger implements Logger {

    private static final int LOG_ENTRY_MAX_LENGTH = 4000;
    private final int minimumLogLevel;
    private volatile String logTag = "REALM";

    /**
     * Creates an logger that outputs to logcat.
     *
     * @param androidLogLevel Android log level
     */
    public AndroidLogger(int androidLogLevel) {
        if (androidLogLevel < Log.VERBOSE || androidLogLevel > Log.ASSERT) {
            throw new IllegalArgumentException("Unknown android log level: " + androidLogLevel);
        }
        minimumLogLevel = androidLogLevel;
    }

    /**
     * Sets the logging tag used when outputting to LogCat. The default value is "REALM".
     *
     * @param tag Logging tag to use for all subsequent logging calls.
     */
    public void setTag(String tag) {
        logTag = tag;
    }

    @Override
    public int getMinimumNativeDebugLevel() {
        // Map Android log level to Realms log levels
        switch (minimumLogLevel) {
            case Log.VERBOSE:   return LogLevel.TRACE;
            case Log.DEBUG:     return LogLevel.DEBUG;
            case Log.INFO:      return LogLevel.INFO;
            case Log.WARN:      return LogLevel.WARN;
            case Log.ERROR:     return LogLevel.ERROR;
            case Log.ASSERT:    return LogLevel.FATAL;
            default:
                throw new IllegalStateException("Unknown log level: " + minimumLogLevel);
        }
    }

    // Inspired by https://github.com/JakeWharton/timber/blob/master/timber/src/main/java/timber/log/Timber.java
    private void log(int androidLogLevel, Throwable t, String message, Object... args) {
        if (androidLogLevel < minimumLogLevel) {
            return;
        }
        if (message == null) {
            if (t == null) {
                return; // Ignore event if message is null and there's no throwable.
            }
            message = getStackTraceString(t);
        } else {
            if (args != null && args.length > 0) {
                message = String.format(message, args);
            }
            if (t != null) {
                message += "\n" + getStackTraceString(t);
            }
        }

        // Message fit one line. Just print and exit
        if (message.length() < LOG_ENTRY_MAX_LENGTH) {
            Log.println(androidLogLevel, logTag, message);
            return;
        }

        // Message does not fit one line.
        // Split by line, then ensure each line can fit into Log's maximum length.
        for (int i = 0, length = message.length(); i < length; i++) {
            int newline = message.indexOf('\n', i);
            newline = newline != -1 ? newline : length;
            do {
                int end = Math.min(newline, i + LOG_ENTRY_MAX_LENGTH);
                String part = message.substring(i, end);
                Log.println(androidLogLevel, logTag, part);
                i = end;
            } while (i < newline);
        }
    }

    @Override
    public void trace(Throwable throwable, String message, Object... args) {
        log(Log.VERBOSE, throwable, message, args);
    }

    @Override
    public void debug(Throwable throwable, String message, Object... args) {
        log(Log.DEBUG, throwable, message, args);
    }

    @Override
    public void info(Throwable throwable, String message, Object... args) {
        log(Log.INFO, throwable, message, args);
    }

    @Override
    public void warn(Throwable throwable, String message, Object... args) {
        log(Log.WARN, throwable, message, args);
    }

    @Override
    public void error(Throwable throwable, String message, Object... args) {
        log(Log.ERROR, throwable, message, args);
    }

    @Override
    public void fatal(Throwable throwable, String message, Object... args) {
        log(Log.ASSERT, throwable, message, args);
    }
}
