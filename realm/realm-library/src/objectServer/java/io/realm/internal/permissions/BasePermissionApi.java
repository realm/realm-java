/*
 * Copyright 2017 Realm Inc.
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

package io.realm.internal.permissions;

import java.util.Date;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.realm.RealmModel;


/**
 * Common methods shared between most Realm model classes used in the Permission Realm API.
 */
public interface BasePermissionApi extends RealmModel {

    /**
     * Returns the unique id for this object.
     *
     * @return the unique id for this object.
     */
    String getId();

    /**
     * Returns the timestamp on the Client that created this object.
     *
     * @return {@link Date} this object was created. The timestamp will use the device clock it was created on.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    Date getCreatedAt();

    /**
     * Returns the timestamp this object was last updated. The timstamp can be both a server timestamp and a device
     * timestamp.
     *
     * @return {@link Date} this object was last modified.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    Date getUpdatedAt();

    /**
     * Returns the status code for this change.
     *
     * @return {@code null} if not yet processed. {@code 0} if successful, {@code >0} if an error happened. See {@link #getStatusMessage()}.
     */
    Integer getStatusCode();

    /**
     * Returns the servers status message, if an error occurred. Otherwise it will return {@code null}.
     *
     * @return The servers status message in case of an error, {@code null} otherwise.
     */
    String getStatusMessage();
}
