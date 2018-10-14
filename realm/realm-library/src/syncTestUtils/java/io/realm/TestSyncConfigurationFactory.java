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

package io.realm;

import io.realm.internal.OsRealmConfig;
import io.realm.internal.sync.permissions.ObjectPermissionsModule;
import io.realm.rule.TestRealmConfigurationFactory;

/**
 * Test rule used for creating SyncConfigurations. Will ensure that any Realm files are deleted when the
 * test ends.
 */
public class TestSyncConfigurationFactory extends TestRealmConfigurationFactory {

    public SyncConfiguration.Builder createSyncConfigurationBuilder(SyncUser user, String url) {
        return user.createConfiguration(url)
                .sessionStopPolicy(OsRealmConfig.SyncSessionStopPolicy.IMMEDIATELY)
                .addModule(new ObjectPermissionsModule())
                .directory(getRoot());
    }
}
