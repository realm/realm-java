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

package io.realm.internal.network;

import java.util.concurrent.TimeUnit;

import io.realm.ErrorCode;
import io.realm.log.RealmLog;

/**
 * Abstracts the concept of running an network task with incremental backoff. It will run forever until interrupted.
 */
public abstract class ExponentialBackoffTask<T extends AuthServerResponse> implements Runnable {
    private final int maxRetries;

    public ExponentialBackoffTask(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public ExponentialBackoffTask() {
        this(Integer.MAX_VALUE - 1);
    }

    // Task to perform
    protected abstract T execute();

    // Check if the task was successful
    protected boolean isSuccess(T result) {
        return result != null && result.isValid();
    }

    // Return true if based on the task result that this task will never complete
    protected boolean shouldAbortTask(T response) {
        // Only retry in case of IO exceptions, since that might be network timeouts etc.
        // All other errors indicate a bigger problem, so just stop the task.
        if (Thread.interrupted()) {
            return true;
        } else if (!response.isValid()) {
            return response.getError().getErrorCode() != ErrorCode.IO_EXCEPTION;
        } else {
            return false;
        }
    }

    // Callback when task have succeeded
    protected abstract void onSuccess(T response);

    // Callback when task has failed
    protected abstract void onError(T response);

    @Override
    public void run() {
        int attempt = 0;
        while (!Thread.interrupted()) {
            attempt++;
            long sleep = calculateExponentialDelay(attempt - 1, TimeUnit.MINUTES.toMillis(5));
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    RealmLog.debug("Incremental backoff was interrupted.");
                    return; // Abort if interrupted
                }
            }
            T response = execute();

            if (isSuccess(response)) {
                onSuccess(response);
                break;
            } else {
                if (shouldAbortTask(response) || attempt == maxRetries + 1) {
                    onError(response);
                    break;
                }
            }
        }
    }

    private static long calculateExponentialDelay(int failedAttempts, long maxDelayInMs) {
        // https://en.wikipedia.org/wiki/Exponential_backoff
        //Attempt = FailedAttempts + 1
        //Attempt 1     0s     0s
        //Attempt 2     2s     2s
        //Attempt 3     4s     4s
        //Attempt 4     8s     8s
        //Attempt 5     16s    16s
        //Attempt 6     32s    32s
        //Attempt 7     64s    1m 4s
        //Attempt 8     128s   2m 8s
        //Attempt 9     256s   4m 16s
        //Attempt 10    512    8m 32s
        //Attempt 11    1024   17m 4s
        //Attempt 12    2048   34m 8s
        //Attempt 13    4096   1h 8m 16s
        //Attempt 14    8192   2h 16m 32s
        //Attempt 15    16384  4h 33m 4s
        double SCALE = 1.0D; // Scale the exponential backoff
        double delayInMs = ((Math.pow(2.0D, failedAttempts) - 1d) / 2.0D) * 1000 * SCALE;

        // Just use maximum back-off value. We are not afraid of many threads using this value
        // to trigger at once.
        return maxDelayInMs < delayInMs ? maxDelayInMs : (long) delayInMs;
    }
}
