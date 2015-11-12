package io.realm.internal.log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Logger implementation for Realm. This can be used to transparently change logging behavior between Android and Java.
 *
 * This class supports adding multiple logger implementations.
 */
public final class RealmLog {

    // Log levels
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;
    public static final int NONE = 8;

    private static final List<Logger> LOGGERS = new CopyOnWriteArrayList<Logger>();

    /**
     * Adds a logger implementation.
     *
     * @param logger the reference to a {@link Logger} implementation.
     */
    public static void add(Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("A non-null logger has to be provided");
        }
        LOGGERS.add(logger);
    }

    /**
     * Removes a current logger implementation.
     *
     * @param logger.
     */
    public static void remove(Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("A non-null logger has to be provided");
        }
        LOGGERS.remove(logger);
    }

    public static void v(String message) {
        for (int i = 0; i < LOGGERS.size(); i++) {
            LOGGERS.get(i).v(message);
        }
    }

    public static void v(String message, Throwable t) {
        for (int i = 0; i < LOGGERS.size(); i++) {
            LOGGERS.get(i).v(message, t);
        }
    }

    public static void d(String message) {
        for (int i = 0; i < LOGGERS.size(); i++) {
            LOGGERS.get(i).d(message);
        }
    }

    public static void d(String message, Throwable t) {
        for (int i = 0; i < LOGGERS.size(); i++) {
            LOGGERS.get(i).d(message, t);
        }
    }

    public static void i(String message) {
        for (int i = 0; i < LOGGERS.size(); i++) {
            LOGGERS.get(i).i(message);
        }
    }

    public static void i(String message, Throwable t) {
        for (int i = 0; i < LOGGERS.size(); i++) {
            LOGGERS.get(i).i(message, t);
        }
    }

    public static void w(String message) {
        for (int i = 0; i < LOGGERS.size(); i++) {
            LOGGERS.get(i).w(message);
        }
    }

    public static void w(String message, Throwable t) {
        for (int i = 0; i < LOGGERS.size(); i++) {
            LOGGERS.get(i).w(message, null);
        }
    }

    public static void e(String message)  {
        for (int i = 0; i < LOGGERS.size(); i++) {
            LOGGERS.get(i).e(message);
        }
    }

    public static void e(String message, Throwable t) {
        for (int i = 0; i < LOGGERS.size(); i++) {
            LOGGERS.get(i).v(message, t);
        }
    }
}
