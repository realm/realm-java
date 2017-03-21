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
 * The Log levels defined and used by Realm when logging events in the API.
 * <p>
 * Realm uses the log levels defined by Log4J:
 * https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/Level.html
 *
 * @see RealmLog#add(RealmLogger)
 */
public class LogLevel {

    /**
     * The ALL has the lowest possible rank and is intended to turn on all logging.
     */
    public static final int ALL = 1;

    /**
     * The TRACE level designates finer-grained informational events than DEBUG.
     */
    public static final int TRACE = 2;

    /**
     * The DEBUG level designates fine-grained informational events that are mostly useful to debug an application.
     */
    public static final int DEBUG = 3;

    /**
     * The INFO level designates informational messages that highlight the progress of the application at
     * coarse-grained level.
     */
    public static final int INFO = 4;

    /**
     * The WARN level designates potentially harmful situations.
     */
    public static final int WARN = 5;

    /**
     * The ERROR level designates error events that might still allow the application to continue running.
     */
    public static final int ERROR = 6;

    /**
     * The FATAL level designates very severe error events that will presumably lead the application to abort.
     */
    public static final int FATAL = 7;

    /**
     * The OFF has the highest possible rank and is intended to turn off logging.
     */
    public static final int OFF = 8;
}


