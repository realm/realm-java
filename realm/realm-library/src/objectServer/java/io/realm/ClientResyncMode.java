/*
 * Copyright 2019 Realm Inc.
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

import io.realm.internal.OsRealmConfig;

/**
 * Enum describing what should happen in case of a Client Resync.
 * <p>
 * A Client Resync is triggered if the device and server cannot agree on a common shared history
 * for the Realm file, thus making it impossible for the device to upload or receive any changes.
 * This can happen if the server is rolled back or restored from backup.
 * <p>
 * <b>IMPORTANT:</b> Just having the device offline will not trigger a Client Resync.
 */
public enum ClientResyncMode {

    /**
     * Realm will compare the local Realm with the Realm on the server and automatically transfer
     * any changes from the local Realm that makes sense to the Realm provided by the server.
     * <p>
     * This is the default mode for fully synchronized Realms. It is not yet supported by
     * Query-based Realms.
     */
    RECOVER_LOCAL_REALM(OsRealmConfig.CLIENT_RESYNC_MODE_RECOVER),

    /**
     * The local Realm will be discarded and replaced with the server side Realm.
     * All local changes will be lost.
     * <p>
     * This mode is not yet supported by Query-based Realms.
     */
    DISCARD_LOCAL_REALM(OsRealmConfig.CLIENT_RESYNC_MODE_DISCARD),

    /**
     * A manual Client Resync is also known as a Client Reset.
     * <p>
     * A {@link io.realm.ClientResetRequiredError} will be sent to
     * {@link io.realm.SyncSession.ErrorHandler#onError(SyncSession, ObjectServerError)}, triggering
     * a Client Reset. Doing this provides a handle to both the old and new Realm file, enabling
     * full control of which changes to move, if any.
     * <p>
     * This is the only supported mode for Query-based Realms.
     *
     * @see io.realm.SyncSession.ErrorHandler#onError(SyncSession, ObjectServerError) for more
     * information about when and why Client Reset occurs and how to deal with it.
     */
    MANUAL(OsRealmConfig.CLIENT_RESYNC_MODE_MANUAL);

    final byte value;

    ClientResyncMode(byte value) {
        this.value = value;
    }

    public byte getNativeValue() {
        return value;
    }

    }
