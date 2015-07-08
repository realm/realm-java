package io.realm.internal.log;

/**
 * Interface for Realm logger implementations.
 */
public interface Logger {
    void v(String message);

    void v(String message, Throwable t);

    void d(String message);

    void d(String message, Throwable t);

    void i(String message);

    void i(String message, Throwable t);

    void w(String message);

    void w(String message, Throwable t);

    void e(String message);

    void e(String message, Throwable t);
}
