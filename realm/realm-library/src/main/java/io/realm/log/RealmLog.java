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

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Global logger used by all Realm components.
 * Custom loggers can be added by registering classes implementing {@link RealmLogger}.
 */
public final class RealmLog {

    @SuppressWarnings("FieldCanBeLocal")
    private static String REALM_JAVA_TAG = "REALM_JAVA";

    /**
     * To convert the old {@link Logger} to the new {@link RealmLogger}.
     */
    private static class LoggerAdapter implements RealmLogger {
        private Logger logger;
        private static final Map<Logger, LoggerAdapter> loggerMap = new IdentityHashMap<Logger, LoggerAdapter>();

        LoggerAdapter(Logger logger) {
            this.logger = logger;
            if (loggerMap.containsKey(logger)) {
                throw new IllegalStateException(String.format("Logger %s exists in the map!", logger.toString()));
            }
            loggerMap.put(logger, this);
        }

        static RealmLogger removeLogger(Logger logger) {
            return loggerMap.remove(logger);
        }

        static void clear() {
            loggerMap.clear();
        }

        @Override
        public void log(int level, String tag, Throwable throwable, String message) {
            switch (level) {
                case LogLevel.TRACE:
                    logger.trace(throwable, message);
                    break;
                case LogLevel.INFO:
                    logger.info(throwable, message);
                    break;
                case LogLevel.DEBUG:
                    logger.debug(throwable, message);
                    break;
                case LogLevel.WARN:
                    logger.warn(throwable, message);
                    break;
                case LogLevel.ERROR:
                    logger.error(throwable, message);
                    break;
                case LogLevel.FATAL:
                    logger.fatal(throwable, message);
                    break;
                default:
                    throw new IllegalArgumentException("Level: " + level + " cannot be logged.");
            }
        }
    }

    /**
     * Adds a logger implementation that will be notified on log events.
     *
     * @param logger the reference to a {@link RealmLogger} implementation.
     */
    public static void add(RealmLogger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("A non-null logger has to be provided");
        }
        nativeAddLogger(logger);
    }

    /**
     * Adds a logger implementation that will be notified on log events.
     *
     * @param logger the reference to a {@link Logger} implementation.
     * @deprecated use {@link #add(RealmLogger)} instead.
     */
    public static void add(Logger logger) {
        synchronized (LoggerAdapter.class) {
            add(new LoggerAdapter(logger));
        }
    }

    /**
     * Sets the current {@link LogLevel}. Setting this will affect all registered loggers.
     *
     * @param level see {@link LogLevel}.
     */
    public static void setLevel(int level) {
        nativeSetLogLevel(level);
    }

    /**
     * Get the current {@link LogLevel}.
     *
     * @return the current {@link LogLevel}.
     */
    public static int getLevel() {
        return nativeGetLogLevel();
    }

    /**
     * Removes the given logger if it is currently added.
     *
     * @return {@code true} if the logger was removed, {@code false} otherwise.
     */
    public static boolean remove(RealmLogger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("A non-null logger has to be provided");
        }
        nativeRemoveLogger(logger);
        return true;
    }

    /**
     * Removes the given logger if it is currently added.
     *
     * @return {@code true} if the logger was removed, {@code false} otherwise.
     * @deprecated use {@link #remove(RealmLogger)} instead.
     */
    public static boolean remove(Logger logger) {
        synchronized (LoggerAdapter.class) {
            if (logger == null) {
                throw new IllegalArgumentException("A non-null logger has to be provided");
            }
            RealmLogger adaptor = LoggerAdapter.removeLogger(logger);
            if (adaptor != null) {
                nativeRemoveLogger(adaptor);
            }
        }
        return true;
    }

    /**
     * Removes all loggers. The default native logger will be removed as well. Use {@link #registerDefaultLogger()} to
     * add it back.
     */
    public static void clear() {
        synchronized (LoggerAdapter.class) {
            nativeClearLoggers();
            LoggerAdapter.clear();
        }
    }

    /**
     * Adds default native logger if it has been removed before. If the default logger has been registered already,
     * it won't be added again. The default logger on Android will log to logcat.
     */
    public static void registerDefaultLogger() {
        nativeRegisterDefaultLogger();
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
        log(LogLevel.TRACE, throwable, message, args);
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
        log(LogLevel.DEBUG, throwable, message, args);
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
        log(LogLevel.INFO, throwable, message, args);
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
        log(LogLevel.WARN, throwable, message, args);
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
        log(LogLevel.ERROR, throwable, message, args);
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
        log(LogLevel.FATAL, throwable, message, args);
    }

    // Format the message, parse the stacktrace of given throwable and pass them to nativeLog.
    private static void log(int level, Throwable throwable, String message, Object... args) {
        StringBuilder stringBuilder = new StringBuilder();
        if (args != null && args.length > 0) {
            message = String.format(message, args);
        }
        if (throwable != null) {
            stringBuilder.append(Log.getStackTraceString(throwable));
        }
        if (message != null) {
            if (throwable != null) {
                stringBuilder.append("\n");
            }
            stringBuilder.append(message);
        }
        nativeLog(level,REALM_JAVA_TAG, throwable, stringBuilder.toString());
    }

    private static native void nativeAddLogger(RealmLogger logger);
    private static native void nativeRemoveLogger(RealmLogger logger);
    private static native void nativeClearLoggers();
    private static native void nativeRegisterDefaultLogger();
    private static native void nativeLog(int level, String tag, Throwable throwable, String message);
    private static native void nativeSetLogLevel(int level);
    private static native int nativeGetLogLevel();
}
