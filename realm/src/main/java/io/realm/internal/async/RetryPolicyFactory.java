package io.realm.internal.async;

/**
 * Created by Nabil on 17/06/15.
 */
public class RetryPolicyFactory {
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
