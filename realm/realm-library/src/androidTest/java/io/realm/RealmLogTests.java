package io.realm;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.log.LogLevel;
import io.realm.log.Logger;
import io.realm.log.RealmLog;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RealmLogTests {

    @Before
    public void setUp() {
        Realm.init(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void add_remove() {
        TestHelper.TestLogger testLogger = new TestHelper.TestLogger();
        RealmLog.add(testLogger);
        RealmLog.fatal("TEST");
        assertEquals("TEST", testLogger.message);
        RealmLog.remove(testLogger);
        RealmLog.fatal("TEST_AGAIN");
        assertEquals("TEST", testLogger.message);
    }

    @Test
    public void set_get_logLevel() {
        TestHelper.TestLogger testLogger = new TestHelper.TestLogger();
        RealmLog.add(testLogger);

        RealmLog.setLevel(LogLevel.FATAL);
        assertEquals(LogLevel.FATAL, RealmLog.getLevel());
        RealmLog.debug("TEST_DEBUG");
        assertNull(testLogger.message);

        RealmLog.setLevel(LogLevel.DEBUG);
        RealmLog.debug("TEST_DEBUG");
        assertEquals("TEST_DEBUG", testLogger.message);
        RealmLog.fatal("TEST_FATAL");
        assertEquals("TEST_FATAL", testLogger.message);

        RealmLog.remove(testLogger);
    }

    @Test
    public void clear() {
        TestHelper.TestLogger testLogger1 = new TestHelper.TestLogger();
        TestHelper.TestLogger testLogger2 = new TestHelper.TestLogger();
        RealmLog.add(testLogger1);
        RealmLog.add(testLogger2);
        RealmLog.fatal("TEST");

        assertEquals("TEST", testLogger1.message);
        assertEquals("TEST", testLogger2.message);

        RealmLog.clear();

        RealmLog.fatal("TEST_AGAIN");
        assertEquals("TEST", testLogger1.message);
        assertEquals("TEST", testLogger2.message);

        RealmLog.registerDefaultLogger();
    }

    @Test
    public void throwable_passedToTheJavaLogger() {
        TestHelper.TestLogger testLogger = new TestHelper.TestLogger();
        RealmLog.add(testLogger);
        Throwable throwable;

        try {
            throw new RuntimeException("Test exception.");
        } catch (RuntimeException e) {
            throwable = e;
            RealmLog.fatal(e);
        }

        // Throwable has been passed.
        assertEquals(throwable, testLogger.throwable);
        // Message is the stacktrace.
        assertTrue(testLogger.message.contains("RealmLogTests.java"));
        RealmLog.remove(testLogger);
    }

    static class TestOldLogger implements Logger {
        String message;
        Throwable throwable;

        @Override
        public int getMinimumNativeDebugLevel() {
            return 0;
        }

        @Override
        public void trace(Throwable throwable, String message, Object... args) {
        }

        @Override
        public void debug(Throwable throwable, String message, Object... args) {
        }

        @Override
        public void info(Throwable throwable, String message, Object... args) {
        }

        @Override
        public void warn(Throwable throwable, String message, Object... args) {
        }

        @Override
        public void error(Throwable throwable, String message, Object... args) {
        }

        @Override
        public void fatal(Throwable throwable, String message, Object... args) {
            this.throwable = throwable;
            this.message = message;
        }
    }

    @Test
    public void loggerAdaptor() {
        TestOldLogger testLogger = new TestOldLogger();
        RealmLog.add(testLogger);
        Throwable throwable;

        try {
            throw new RuntimeException("Test exception.");
        } catch (RuntimeException e) {
            throwable = e;
            RealmLog.fatal(e);
        }

        // Throwable has been passed.
        assertEquals(throwable, testLogger.throwable);
        assertTrue(testLogger.message.contains("RealmLogTests.java"));

        RealmLog.remove(testLogger);
        RealmLog.fatal("new string");

        // Logger has been removed, nothing should be changed.
        assertEquals(throwable, testLogger.throwable);
        assertTrue(testLogger.message.contains("RealmLogTests.java"));
    }
}
