package io.realm.internal.android;

import android.util.Log;

import io.realm.internal.log.Logger;
import io.realm.internal.log.RealmLog;

public class AndroidLogger implements Logger {

    private static final int LOG_ENTRY_MAX_LENGTH = 4000;
    private int minimumLogLevel = RealmLog.VERBOSE;
    private String logTag = "REALM";

    /**
     * Manually set a logging tag.
     * @param tag Logging tag to use for all subsequent logging calls.
     */
    public void setTag(String tag) {
        logTag = tag;
    }

    /**
     * Override the provided logger behavior and only log if log entry has a level equal or higher.
     * @param logLevel Minimum log level to report.
     */
    public void setMinimumLogLevel(int logLevel) {
        minimumLogLevel = logLevel;
    }

    // Inspired by https://github.com/JakeWharton/timber/blob/master/timber/src/main/java/timber/log/Timber.java
    private void log(int logLevel, String message, Throwable t) {
        if (logLevel < minimumLogLevel) {
            return;
        }
        if (message == null || message.length() == 0) {
            if (t != null) {
                message = Log.getStackTraceString(t);
            } else {
                return; // Don't log if message is null and there is no throwable
            }
        } else if (t != null) {
            message += "\n" + Log.getStackTraceString(t);
        }

       if (message.length() < 4000) {
            Log.println(logLevel, logTag, message);
        } else {
           logMessageIgnoringLimit(logLevel, logTag, message);
        }
    }

    /**
     * Inspired by:
     * http://stackoverflow.com/questions/8888654/android-set-max-length-of-logcat-messages
     * https://github.com/jakubkrolewski/timber/blob/feature/logging_long_messages/timber/src/main/java/timber/log/Timber.java
     */
    private void logMessageIgnoringLimit(int logLevel, String tag, String message) {
        while (message.length() != 0) {
            int nextNewLineIndex = message.indexOf('\n');
            int chunkLength = nextNewLineIndex != -1 ? nextNewLineIndex : message.length();
            chunkLength = Math.min(chunkLength, LOG_ENTRY_MAX_LENGTH);
            String messageChunk = message.substring(0, chunkLength);
            Log.println(logLevel, tag, messageChunk);

            if (nextNewLineIndex != -1 && nextNewLineIndex == chunkLength) {
                // Don't print out the \n twice.
                message = message.substring(chunkLength + 1);
            } else {
                message = message.substring(chunkLength);
            }
        }
    }

    @Override
    public void v(String message) {
        log(RealmLog.VERBOSE, message, null);
    }

    @Override
    public void v(String message, Throwable t) {
        log(RealmLog.VERBOSE, message, t);
    }

    @Override
    public void d(String message) {
        log(RealmLog.DEBUG, message, null);
    }

    @Override
    public void d(String message, Throwable t) {
        log(RealmLog.DEBUG, message, t);
    }

    @Override
    public void i(String message) {
        log(RealmLog.INFO, message, null);
    }

    @Override
    public void i(String message, Throwable t) {
        log(RealmLog.INFO, message, t);
    }

    @Override
    public void w(String message) {
        log(RealmLog.WARN, message, null);
    }

    @Override
    public void w(String message, Throwable t) {
        log(RealmLog.WARN, message, t);
    }

    @Override
    public void e(String message) {
        log(RealmLog.ERROR, message, null);
    }

    @Override
    public void e(String message, Throwable t) {
        log(RealmLog.ERROR, message, t);
    }
}
