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

package io.realm;

import javax.annotation.Nullable;

import io.realm.internal.Util;

/**
 * This class is a wrapper for all errors happening when communicating with the Realm Object Server.
 * This include both exceptions and protocol errors.
 *
 * Only {@link #getErrorCode()} is guaranteed to contain a value. If the error was caused by an underlying exception
 * {@link #getErrorMessage()} is {@code null} and {@link #getException()} is set, while if the error was a protocol error
 * {@link #getErrorMessage()} is set and {@link #getException()} is null.
 *
 * @see ErrorCode for a list of possible errors.
 */
public class ObjectServerError extends RuntimeException {

    private final ErrorCode error;
    private final String errorMessage;
    private final Throwable exception;

    /**
     * Create an error caused by an error in the protocol when communicating with the Object Server.
     *
     * @param errorCode error code for this type of error.
     * @param errorMessage detailed error message.
     */
    public ObjectServerError(ErrorCode errorCode, String errorMessage) {
        this(errorCode, errorMessage, (Throwable) null);
    }

    /**
     * Create an error caused by an an exception when communicating with the Object Server.
     *
     * @param errorCode error code for this type of error.
     * @param exception underlying exception causing this error.
     */
    public ObjectServerError(ErrorCode errorCode, Throwable exception) {
        this(errorCode, null, exception);
    }

    /**
     * Generic error happening that could happen anywhere.
     *
     * @param errorCode error code for this type of error.
     * @param errorMessage detailed error message.
     * @param exception underlying exception if the error was caused by this.
     */
    public ObjectServerError(ErrorCode errorCode, @Nullable String errorMessage, @Nullable Throwable exception) {
        this.error = errorCode;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }

    /**
     * Errors happening while trying to authenticate a user.
     *
     * @param errorCode error code for this type of error.
     * @param title Title for this type of error.
     * @param hint a hint for resolving the error.
     */
    public ObjectServerError(ErrorCode errorCode, String title, @Nullable String hint) {
        this(errorCode, (hint != null) ? title + " : " + hint : title, (Throwable) null);
    }

    /**
     * Returns the error code uniquely identifying this type of error.
     *
     * @return the error code identifying the type of error.
     * @see ErrorCode
     */
    public ErrorCode getErrorCode() {
        return error;
    }

    /**
     * Returns a more detailed error message about the cause of this error.
     *
     * @return a detailed error message or {@code null} if one was not available.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns the underlying exception causing this error, if any.
     *
     * @return the underlying exception causing this error, or {@code null} if not caused by an exception.
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Returns the {@link ErrorCode.Category} category for this error.
     * Errors that are {@link ErrorCode.Category#RECOVERABLE} mean that it is still possible for a
     * given {@link SyncSession} to resume synchronization. {@link ErrorCode.Category#FATAL} errors
     * means that session has stopped and cannot be recovered.
     *
     * @return the error category.
     */
    public ErrorCode.Category getCategory() {
        return error.getCategory();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getErrorCode().toString());
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
