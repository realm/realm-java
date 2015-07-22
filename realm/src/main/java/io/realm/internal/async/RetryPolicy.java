/*
 * Copyright 2015 Realm Inc.
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

package io.realm.internal.async;

/**
 * Define a retry policy in case an {@link io.realm.RealmQuery} fails to
 * import the result to the caller Realm. (usually because in the meantime, the caller
 * Realm has advanced the transaction) failing to retry will throw an exception
 */
public interface RetryPolicy {
    /**
     * No retry this will probably propagate the exception thrown by Core 'Handover failed due to version mismatch'
     */
    int MODE_NO_RETRY = 0;
    /**
     * Retry the query a number of times
     */
    int MODE_MAX_RETRY = 1;
    /**
     * Retry indefinitely
     */
    int MODE_INDEFINITELY = 2;

    /**
     * Given the selected mode, this return {@code true} or {@code false} indicating
     * whether the {@link io.realm.RealmQuery} should keep retrying or not
     * @return {@code true} if query should be retried, {@code false} otherwise.
     */
    boolean shouldRetry ();
}
