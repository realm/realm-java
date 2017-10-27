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

import javax.annotation.Nullable;

import io.realm.internal.Keep;


/**
 * Interface for custom loggers that can be registered at {@link RealmLog#add(RealmLogger)}.
 * The different log levels are described in {@link LogLevel}.
 */
@Keep // This interface is used as a parameter type of a native method in OsSharedRealm.java
public interface RealmLogger {

    /**
     * Handles a log event.
     *
     * @param level for this log event. It can only be a value between {@link LogLevel#TRACE} and
     * {@link LogLevel#FATAL}
     * @param tag for this log event.
     * @param throwable optional exception to log.
     * @param message optional additional message.
     */
    void log(int level, String tag, @Nullable Throwable throwable, @Nullable String message);
}
