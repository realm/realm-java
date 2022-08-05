/*
 * Copyright 2022 Realm Inc.
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
package io.realm.mongodb.sync;

import io.realm.Realm;

/**
 * Interface that defines an automatic sync client reset strategy, it could be either
 * {@link DiscardUnsyncedChangesStrategy}, {@link RecoverOrDiscardUnsyncedChangesStrategy} or
 * {@link RecoverUnsyncedChangesStrategy}.
 */
public interface AutomaticClientResetStrategy extends SyncClientResetStrategy {
    /**
     * Callback that indicates a Client Reset is about to happen. It provides a handle to the local realm
     * before the reset.
     *
     * @param realm frozen {@link Realm} in its state before the reset.
     *
     */
    void onBeforeReset(Realm realm);

    /**
     * Callback that indicates the seamless Client reset failed to complete. It should be handled
     * as {@link ManuallyRecoverUnsyncedChangesStrategy#onClientReset(SyncSession, ClientResetRequiredError)}.
     *
     * @param session {@link SyncSession} this error happened on.
     * @param error   {@link ClientResetRequiredError} the specific Client Reset error.
     */
    void onError(SyncSession session, ClientResetRequiredError error);
}
