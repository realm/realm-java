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
 * Build a {@link RetryPolicy}
 */
public class RetryPolicyFactory {
    /**
     * Return the appropriate {@link RetryPolicy}
     * @param mode one of the supported mode in {@link RetryPolicy}
     * @param maxNumberOfRetries how many times we should retry before giving up
     *                           (not applicable for {@link RetryPolicy#MODE_INDEFINITELY})
     * @return new instance of {@code RetryPolicy}
     */
    public static RetryPolicy get(int mode, int maxNumberOfRetries) {
        switch (mode) {
            case RetryPolicy.MODE_NO_RETRY: {
                return new NoRetryPolicy();
            }
            case RetryPolicy.MODE_MAX_RETRY: {
                return new MaxRetryPolicy(maxNumberOfRetries);
            }
            case RetryPolicy.MODE_INDEFINITELY: {
                return new IndefinitelyRetryPolicy();
            }
            default:
                throw new IllegalArgumentException("Unsupported retry policy " + mode);
        }
    }
}
