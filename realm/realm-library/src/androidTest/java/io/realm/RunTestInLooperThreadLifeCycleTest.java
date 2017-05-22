package io.realm;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Meta test. Checking the lifecycle of `@RunTestInLooperThreadTest does the right thing.
 *
 * Current order is:
 * - @RunTestInLooperThread(before = <classRef>)
 * - @Before()
 * - @RunTestInLooperThread/@Test
 * - @After : This is called when exiting the test method
 * - looperThread.runAfterTest(Runnable) : This is called when the LooperTest either succeed for fails.
 */

public class RunTestInLooperThreadLifeCycleTest {

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private static AtomicBoolean beforeCalled = new AtomicBoolean(false);
    private static AtomicBoolean afterCalled = new AtomicBoolean(false);
    private static AtomicBoolean testExited = new AtomicBoolean(false);
    private static AtomicBoolean beforeRunnableCalled = new AtomicBoolean(false);
    private static AtomicBoolean afterRunnableCalled = new AtomicBoolean(false);

    @Before
    public void before() {
        assertTrue(beforeCalled.compareAndSet(false, true));
        assertTrue(beforeRunnableCalled.get());

        looperThread.runAfterTest(new Runnable() {
            @Override
            public void run() {
                assertTrue(testExited.get());
                assertTrue(afterRunnableCalled.compareAndSet(false, true));
                assertTrue(looperThread.isTestComplete());
            }
        });
;    }

    @After
    public void after() {
        assertTrue(afterCalled.compareAndSet(false, true));
        assertTrue(testExited.get());
        assertFalse(looperThread.isTestComplete()); // Beware of this. Use `runAfterTest` for destroying resources used.
    }

    @Test
    @RunTestInLooperThread(before = PrepareLooperTest.class)
    public void looperTest() {
        looperThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                assertTrue(afterCalled.get());
                assertFalse(looperThread.isTestComplete());
                looperThread.testComplete();
            }
        });
        assertTrue(testExited.compareAndSet(false, true));
    }

    public static class PrepareLooperTest implements RunInLooperThread.RunnableBefore {
        @Override
        public void run(RealmConfiguration realmConfig) {
            assertTrue(beforeRunnableCalled.compareAndSet(false, true));
            assertFalse(beforeCalled.get());
        }
    }
}
