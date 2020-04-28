/*
 * Copyright 2020 Realm Inc.
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

package io.realm.internal.common;

public final class OperationResult<SuccessTypeT, FailureTypeT> {
    private final SuccessTypeT result;
    private final FailureTypeT failureResult;
    private final boolean isSuccessful;

    private OperationResult(
            final SuccessTypeT result, final FailureTypeT failureResult, final boolean isSuccessful) {
        this.result = result;
        this.failureResult = failureResult;
        this.isSuccessful = isSuccessful;
    }

    public static <T, U> OperationResult<T, U> successfulResultOf(final T value) {
        return new OperationResult<>(value, null, true);
    }

    public static <T, U> OperationResult<T, U> failedResultOf(final U value) {
        return new OperationResult<>(null, value, false);
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    /**
     * Gets the result of the operation, if successful.
     *
     * @return The result of the operation.
     */
    public SuccessTypeT geResult() {
        if (!isSuccessful) {
            throw new IllegalStateException("operation was failed, not successful");
        }
        return result;
    }

    /**
     * Gets the failure reason for the operation, if it failed.
     *
     * @return The failure reason of the operation.
     */
    public FailureTypeT getFailure() {
        if (isSuccessful) {
            throw new IllegalStateException("operation was successful, not failed");
        }
        return failureResult;
    }
}
