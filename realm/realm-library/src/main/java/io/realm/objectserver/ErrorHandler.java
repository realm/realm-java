package io.realm.objectserver;

import io.realm.objectserver.Error;

/**
 * Interface used by both the Object Server network client and sessions to report back errors.
 *
 * @see SyncManager#setGlobalErrorHandler(ErrorHandler)
 * @see SyncManager#setDefaultSessionErrorHandler(ErrorHandler)
 * @see io.realm.objectserver.SyncConfiguration.Builder#errorHandler(ErrorHandler)
 */
public interface ErrorHandler {
    void onError(Error errorCode, String errorMessage);
}

