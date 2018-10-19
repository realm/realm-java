/*
 * Copyright 2018 Realm Inc.
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

import org.junit.Rule;
import org.junit.Test;

import io.realm.entities.AllTypes;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test sync specific functionality on {@link RealmResults}.
 */
public class SyncRealmResultsTests {

    @Rule
    public final TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    /**
     * Tests https://github.com/realm/realm-java/issues/6235
     * This checks that the INITIAL callback is called for query-based notifications even when
     * the device is offline.
     */
    @Test
    @RunTestInLooperThread
    public void listenersTriggerWhenOffline() {
        SyncUser user = SyncTestUtils.createTestUser();
        String url = "http://foo.com/partialSync";
        SyncConfiguration config = configFactory.createSyncConfigurationBuilder(user, url)
                .build();
        Realm realm = Realm.getInstance(config);
        looperThread.closeAfterTest(realm);

        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllAsync();

        looperThread.keepStrongReference(results);
        results.addChangeListener((objects, changeSet) -> {
            if(changeSet.getState() == OrderedCollectionChangeSet.State.INITIAL) {
                assertTrue(results.isLoaded());
                assertFalse(changeSet.isCompleteResult());
                looperThread.testComplete();
            }
        });
    }
}
