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

package io.realm.objectserver;

import io.realm.internal.Util;

/**
 * This class is a wrapper for all errors happening when communicating with the Realm Object Server.
 * This include both exceptions and protocol errors.
 *
 * Only {@link #errorCode()} is guaranteed to be set. If the error was caused by an underlying exception
 * {@link #errorMessage()} is {@code null} and {@link #exception()} is set, while if the error was a protocol error
 * {@link #errorMessage()} is set and {@link #exception()} is null.
 *
 * @see io.realm.objectserver.ErrorCode for a list of possible errors.
 */
public class ObjectServerError extends RuntimeException {

    private final ErrorCode error;
    private final String errorMessage;
    private final Throwable exception;

    public ObjectServerError(ErrorCode errorCode, String errorMessage) {
        this(errorCode, errorMessage, null);
    }

    public ObjectServerError(ErrorCode errorCode, Throwable exception) {
        this(errorCode, null, exception);
    }

    public ObjectServerError(ErrorCode errorCode, String errorMessage, Throwable exception) {
        this.error = errorCode;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }

    public ErrorCode errorCode() {
        return error;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public Throwable exception() {
        return exception;
    }

    public ErrorCode.Category category() {
        return error.getCategory();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(errorCode().toString());
        if (errorMessage != null) {
            sb.append('\n');
            sb.append(errorMessage);
        }
        if (exception != null) {
            sb.append('\n');
            sb.append(Util.getStackTrace(exception));
        }
        return sb.toString();
    }
}
