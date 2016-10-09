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

import java.util.ArrayList;
import java.util.List;

import io.realm.internal.Keep;
import io.realm.internal.Util;

/**
 * Global logger used by all Realm components.
 * Custom loggers can be added by registering classes implementing {@link Logger}.
 */
@Keep
public final class RealmLog {

    private static final Logger[] NO_LOGGERS = new Logger[0];

    // All of the below should be modified together under under a lock on LOGGERS.
    private static final List<Logger> LOGGERS = new ArrayList<>();
    private static volatile Logger[] loggersAsArray = NO_LOGGERS;
    private static int minimumNativeLogLevel = Integer.MAX_VALUE;

    /**
     * Adds a logger implementation that will be notified on log events.
     *
     * @param logger the reference to a {@link Logger} implementation.
     */
    public static void add(Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("A non-null logger has to be provided");
        }
        synchronized (LOGGERS) {
            LOGGERS.add(logger);
            int minimumLogLevel = logger.getMinimumNativeDebugLevel();
            if (minimumLogLevel < minimumNativeLogLevel) {
                setMinimumNativeDebugLevel(minimumLogLevel);
            }
            loggersAsArray = LOGGERS.toArray(new Logger[LOGGERS.size()]);
        }
    }

    private static void setMinimumNativeDebugLevel(int nativeDebugLevel) {
        minimumNativeLogLevel = nativeDebugLevel;
        Util.setDebugLevel(nativeDebugLevel); // Log level for Realm Core
    }

    /**
     * Removes the given logger if it is currently added.
     *
     * @return {@code true} if the logger was removed, {@code false} otherwise.
     */
    public static boolean remove(Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("A non-null logger has to be provided");
        }
        synchronized (LOGGERS) {
            LOGGERS.remove(logger);
            int newMinLevel = Integer.MAX_VALUE;
            for (int i = 0; i < LOGGERS.size(); i++) {
                int logMin = LOGGERS.get(i).getMinimumNativeDebugLevel();
                if (logMin < newMinLevel) {
                    newMinLevel = logMin;
                }
            }
            setMinimumNativeDebugLevel(newMinLevel);
            loggersAsArray = LOGGERS.toArray(new Logger[LOGGERS.size()]);
        }
        return true;
    }

    /**
     * Remove all loggers.
     */
    public static void clear() {
        synchronized (LOGGERS) {
            LOGGERS.clear();
            setMinimumNativeDebugLevel(Integer.MAX_VALUE);
            loggersAsArray = NO_LOGGERS;
        }
    }

    /**
     * Logs a {@link LogLevel#TRACE} exception.
     *
     * @param throwable exception to log.
     */
    public static void trace(Throwable throwable) {
        trace(throwable, null);
    }

    /**
     * Logs a {@link LogLevel#TRACE} event.
     *
     * @param message message to log.
     * @param args optional args used to format the message using {@link String#format(String, Object...)}.
     */
    public static void trace(String message, Object... args) {
        trace(null, message, args);
    }

    /**
     * Logs a {@link LogLevel#TRACE} event.
     *
     * @param throwable optional exception to log.
     * @param message optional message.
     * @param args optional args used to format the message using {@link String#format(String, Object...)}.
     */
    public static void trace(Throwable throwable, String message, Object... args) {
        Logger[] loggers = loggersAsArray;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < loggers.length; i++) {
            loggers[i].trace(throwable, message, args);
        }
    }

    /**
     * Logs a {@link LogLevel#DEBUG} exception.
     *
     * @param throwable exception to log.
     */
    public static void debug(Throwable throwable) {
        debug(throwable, null);
    }

    /**
     * Logs a {@link LogLevel#DEBUG} event.
     *
     * @param message message to log.
     * @param args optional args used to format the message using {@link String#format(String, Object...)}.
     */
    public static void debug(String message, Object... args) {
        debug(null, message, args);
    }

    /**
     * Logs a {@link LogLevel#DEBUG} event.
     *
     * @param throwable optional exception to log.
     * @param message optional message.
     * @param args optional args used to format the message using {@link String#format(String, Object...)}.
     */
    public static void debug(Throwable throwable, String message, Object... args) {
        Logger[] loggers = loggersAsArray;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < loggers.length; i++) {
            loggers[i].debug(throwable, message, args);
        }
    }

    /**
     * Logs an {@link LogLevel#INFO} exception.
     *
     * @param throwable exception to log.
     */
    public static void info(Throwable throwable) {
        info(throwable, null);
    }

    /**
     * Logs an {@link LogLevel#INFO} event.
     *
     * @param message message to log.
     * @param args optional args used to format the message using {@link String#format(String, Object...)}.
     */
    public static void info(String message, Object... args) {
        info(null, message, args);
    }

    /**
     * Logs an {@link LogLevel#INFO} event.
     *
     * @param throwable optional exception to log.
     * @param message optional message.
     * @param args optional args used to format the message using {@link String#format(String, Object...)}.
     */
    public static void info(Throwable throwable, String message, Object... args) {
        Logger[] loggers = loggersAsArray;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < loggers.length; i++) {
            loggers[i].info(throwable, message, args);
        }
    }

    /**
     * Logs a {@link LogLevel#WARN} exception.
     *
     * @param throwable exception to log.
     */
    public static void warn(Throwable throwable) {
        warn(throwable, null);
    }

    /**
     * Logs a {@link LogLevel#WARN} event.
     *
     * @param message message to log.
     * @param args optional args used to format the message using {@link String#format(String, Object...)}.
     */
    public static void warn(String message, Object... args) {
        warn(null, message, args);
    }

    /**
     * Logs a {@link LogLevel#WARN} event.
     *
     * @param throwable optional exception to log.
     * @param message optional message.
     * @param args optional args used to format the message using {@link String#format(String, Object...)}.
     */
    public static void warn(Throwable throwable, String message, Object... args) {
        Logger[] loggers = loggersAsArray;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < loggers.length; i++) {
            loggers[i].warn(throwable, message, args);
        }
    }

    /**
     * Logs an {@link LogLevel#ERROR} exception.
     *
     * @param throwable exception to log.
     */
    public static void error(Throwable throwable) {
        error(throwable, null);
    }

    /**
     * Logs an {@link LogLevel#ERROR} event.
     *
     * @param message message to log.
     * @param args optional args used to format the message using {@link String#format(String, Object...)}.
     */
    public static void error(String message, Object... args) {
        error(null, message, args);
    }

    /**
     * Logs an {@link LogLevel#ERROR} event.
     *
     * @param throwable optional exception to log.
     * @param message optional message.
     * @param args optional args used to format the message using {@link String#format(String, Object...)}.
     */
    public static void error(Throwable throwable, String message, Object... args) {
        Logger[] loggers = loggersAsArray;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < loggers.length; i++) {
            loggers[i].error(throwable, message, args);
        }
    }

    /**
     * Logs a {@link LogLevel#FATAL} exception.
     *
     * @param throwable exception to log.
     */
    public static void fatal(Throwable throwable) {
        fatal(throwable, null);
    }

    /**
     * Logs an {@link LogLevel#FATAL} event.
     *
     * @param message message to log.
     * @param args optional args used to format the message using {@link String#format(String, Object...)}.
     */
    public static void fatal(String message, Object... args) {
        fatal(null, message, args);
    }

    /**
     * Logs a {@link LogLevel#FATAL} event.
     *
     * @param throwable optional exception to log.
     * @param message optional message.
     * @param args optional args used to format the message using {@link String#format(String, Object...)}.
     */
    public static void fatal(Throwable throwable, String message, Object... args) {
        Logger[] loggers = loggersAsArray;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < loggers.length; i++) {
            loggers[i].fatal(throwable, message, args);
        }
    }
}
