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

package io.realm.objectserver.utils;

import io.realm.SyncManager;
import io.realm.services.RemoteTestService;

// Remote test service base class which contains some initialization for sync.
public class RemoteIntegrationTestService extends RemoteTestService {
    public RemoteIntegrationTestService() {
        super();
        SyncManager.Debug.skipOnlineChecking = true;
        SyncManager.Debug.separatedDirForSyncManager = true;
    }
}
