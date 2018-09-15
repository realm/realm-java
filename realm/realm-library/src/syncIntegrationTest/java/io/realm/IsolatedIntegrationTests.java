package io.realm;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;


/**
 * Base class for integration tests where each test is run on a separate ROS instance.
 * This adds quite a lot of overhead between each test, so only use this base class if absolutely needed.
 * Otherwise use {@link IsolatedIntegrationTests}.
 */
public class IsolatedIntegrationTests extends BaseIntegrationTest {

    @Before
    public void setupTest() throws IOException {
        startSyncServer();
        SyncTestUtils.prepareEnvironmentForTest();
    }

    @After
    public void teardownTest() {
        if (!looperThread.isRuleUsed() || looperThread.isTestComplete()) {
            // Non-looper tests can reset here
            SyncTestUtils.restoreEnvironmentAfterTest();
            stopSyncServer();
        } else {
            // Otherwise we need to wait for the test to complete
            looperThread.runAfterTest(new Runnable() {
                @Override
                public void run() {
                    SyncTestUtils.restoreEnvironmentAfterTest();
                    stopSyncServer();
                }
            });
        }
    }

}
