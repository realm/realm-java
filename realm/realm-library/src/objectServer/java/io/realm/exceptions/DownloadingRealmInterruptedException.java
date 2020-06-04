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

package io.realm.exceptions;

import io.realm.annotations.Beta;
import io.realm.mongodb.sync.SyncConfiguration;


/**
 * Exception class used when a Realm was interrupted while downloading the initial data set.
 * This can only happen if {@link SyncConfiguration.Builder#waitForInitialRemoteData()} is set.
 */
@Beta
public class DownloadingRealmInterruptedException extends RuntimeException {
    public DownloadingRealmInterruptedException(SyncConfiguration syncConfig, Throwable exception) {
        super("Realm was interrupted while downloading the latest changes from the server: " + syncConfig.getPath(),
                exception);
    }
    public DownloadingRealmInterruptedException(SyncConfiguration syncConfig, String message) {
        super("Realm was interrupted while downloading the latest changes from the server: " + syncConfig.getPath() + "\n" + message);
    }
}
