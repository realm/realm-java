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

/**
 * Interface for custom loggers that can be registered at {@link RealmLog#add(Logger)}.
 * The different log levels are described in {@link LogLevel}.
 * @deprecated Use {@link RealmLogger} instead.
 */
public interface Logger {

    /**
     * Defines which {@link LogLevel} events this logger cares about from the native components.
     * <p>
     * If multiple loggers are registered, the minimum value among all loggers is used.
     * <p>
     * Note that sending log events from the native layer is relatively expensive, so only set this value to events
     * that are truly useful.
     *
     * @return the minimum {@link LogLevel} native events this logger cares about.
     */
    int getMinimumNativeDebugLevel();

    /**
     * Handles a {@link LogLevel#TRACE} event.
     *
     * @param throwable optional exception to log.
     * @param message optional additional message.
     * @param args optional arguments used to format the message using {@link String#format(String, Object...)}.
     */
    void trace(Throwable throwable, String message, Object... args);

    /**
     * Handles a {@link LogLevel#DEBUG} event.
     *
     * @param throwable optional exception to log.
     * @param message optional additional message.
     * @param args optional arguments used to format the message using {@link String#format(String, Object...)}.
     */
    void debug(Throwable throwable, String message, Object... args);

    /**
     * Handles an {@link LogLevel#INFO} event.
     *
     * @param throwable optional exception to log.
     * @param message optional additional message.
     * @param args optional arguments used to format the message using {@link String#format(String, Object...)}.
     */
    void info(Throwable throwable, String message, Object... args);

    /**
     * Handles a {@link LogLevel#WARN} event.
     *
     * @param throwable optional exception to log.
     * @param message optional additional message.
     * @param args optional arguments used to format the message using {@link String#format(String, Object...)}.
     */
    void warn(Throwable throwable, String message, Object... args);

    /**
     * Handles an {@link LogLevel#ERROR} event.
     *
     * @param throwable optional exception to log.
     * @param message optional additional message.
     * @param args optional arguments used to format the message using {@link String#format(String, Object...)}.
     */
    void error(Throwable throwable, String message, Object... args);

    /**
     * Handles a {@link LogLevel#FATAL} event.
     *
     * @param throwable optional exception to log.
     * @param message optional additional message.
     * @param args optional arguments used to format the message using {@link String#format(String, Object...)}.
     */
    void fatal(Throwable throwable, String message, Object... args);
}
