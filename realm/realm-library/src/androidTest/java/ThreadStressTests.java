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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.TestHelper;
import io.realm.entities.AllTypes;
import io.realm.entities.NonLatinFieldNames;
import io.realm.log.RealmLog;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertFalse;

/**
 * Class used to stress test multiple actions across different threads.
 * This doesn't attempt to test correctness beyond "Don't Crash".
 *
 * Some error level logging is done during the run of this. This is mostly to make
 * it clearer what has happened in the case a run actually did crash, and doesn't indicate
 * problems with the test as such.
 */
@RunWith(Parameterized.class)
public class ThreadStressTests {

    @Parameterized.Parameters(name = "Encryption: {0}")
    public static List<Boolean> data() {
        return Arrays.asList(Boolean.TRUE, Boolean.FALSE);
    }

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private final boolean useEncryption;
    private final static int MAX_THREADS = 100;
    private final static int MAX_CREATE = 1000;
    private RealmConfiguration realmConfig;
    private Random random;
    private List<ThreadWrapper> threads = new CopyOnWriteArrayList<>();
    private AtomicInteger workerThreadId = new AtomicInteger(0);

    enum CRUDAction {
        CREATE(0),
        READ(1),
        UPDATE(2),
        DELETE(3);

        private final int val;

        CRUDAction(int val) {
            this.val = val;
        }

        public static CRUDAction fromValue(int value) {
            for (CRUDAction action : values()) {
                if (action.val == value) {
                    return action;
                }
            }
            throw new IllegalArgumentException("Unknown value: " + value);
        }
    }

    public interface ThreadWrapper {
        void start();
        void join();
    }

    public interface AsyncTaskRunner {
        void run(Realm realm, CountDownLatch success);
    }

    public interface TaskRunner {
        void run(Realm realm);
    }

    public ThreadStressTests(boolean useEncryption) {
        this.useEncryption = useEncryption;
    }

    @Before
    public void setUp() {
        long seed = System.currentTimeMillis();
        RealmLog.error("Starting stress test with seed: " + seed);
        random = new Random(seed);
        RealmConfiguration.Builder builder = configFactory.createConfigurationBuilder();
        if (useEncryption) {
            builder.encryptionKey(TestHelper.getRandomKey(seed));
        }
        realmConfig = configFactory.createConfiguration();
        Realm.deleteRealm(realmConfig);
    }

    private void populateTestRealm(Realm realm, int objects) {
        boolean inTransaction = realm.isInTransaction();
        if (!inTransaction) {
            realm.beginTransaction();
        }
        realm.deleteAll();
        for (int i = 0; i < objects; ++i) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnLong(i);
            allTypes.setColumnBoolean((i % 3) == 0);
            allTypes.setColumnBinary(new byte[] {1, 2, 3});
            allTypes.setColumnDate(new Date());
            allTypes.setColumnDouble(Math.PI);
            allTypes.setColumnFloat(1.234567F + i);

            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
            NonLatinFieldNames nonLatinFieldNames = realm.createObject(NonLatinFieldNames.class);
            nonLatinFieldNames.set델타(i);
            nonLatinFieldNames.setΔέλτα(i);
            nonLatinFieldNames.set베타(1.234567F + i);
            nonLatinFieldNames.setΒήτα(1.234567F + i);
        }
        if (!inTransaction) {
            realm.commitTransaction();
        }
    }

    private void populateTestRealm(Realm realm) {
        populateTestRealm(realm, 1000);
    }

    @Test
    public void threadStressTest() {
        Realm realm = Realm.getInstance(realmConfig);
        populateTestRealm(realm);
        for (int i = 0; i < MAX_THREADS; i++) {
            CRUDAction action = CRUDAction.fromValue(random.nextInt(4));
            ThreadWrapper thread = null;
            switch(action) {
                case CREATE:
                    thread = createObjects(random.nextInt(MAX_CREATE), random.nextBoolean());
                    break;
                case READ:
                    thread = readObjects(random.nextBoolean());
                    break;
                case UPDATE:
                    thread = updateObjects(random.nextBoolean(), random.nextBoolean());
                    break;
                case DELETE:
                    thread = deleteObjects(random.nextBoolean(), random.nextBoolean());
                    break;
            }
            threads.add(thread);
            thread.start();
        }
        for (ThreadWrapper thread : threads) {
            thread.join();
        }
        realm.close();
    }

    private ThreadWrapper createObjects(int objectsCount, boolean asyncTransaction) {
        if (asyncTransaction) {
            return createTaskInHandlerThread((realm, success) -> {
                RealmLog.error("Creating objects (async): " + Thread.currentThread().getName());
                realm.executeTransactionAsync(bgRealm -> populateTestRealm(bgRealm, objectsCount), success::countDown);
            });
        } else {
            return createTaskInThread((realm) -> {
                RealmLog.error("Creating objects: " + Thread.currentThread().getName());
                populateTestRealm(realm, objectsCount);
            });
        }
    }

    private ThreadWrapper deleteObjects(boolean filterObjects, boolean asyncTransaction) {
        TaskRunner delete = realm -> {
            if (filterObjects) {
                realm.where(AllTypes.class)
                        .lessThan(AllTypes.FIELD_LONG, realm.where(AllTypes.class).count()/2)
                        .equalTo(AllTypes.FIELD_BOOLEAN, true)
                        .findAll()
                        .deleteAllFromRealm();
            } else {
                realm.delete(AllTypes.class);
            }
        };

        if (asyncTransaction) {
            return createTaskInHandlerThread(((realm, success) -> {
                RealmLog.error("Deleting objects (async): " + Thread.currentThread().getName());
                realm.executeTransactionAsync(delete::run, success::countDown);
            }));
        } else {
            return createTaskInThread((realm) -> {
                RealmLog.error("Deleting objects: " + Thread.currentThread().getName());
                realm.executeTransaction(delete::run);
            });
        }
    }


    private ThreadWrapper updateObjects(boolean filterObjects, boolean asyncTransaction) {
        TaskRunner update = realm -> {
            RealmResults<AllTypes> results;
            if (filterObjects) {
                results = realm.where(AllTypes.class)
                        .lessThan(AllTypes.FIELD_LONG, random.nextInt((int) realm.where(AllTypes.class).count() + 1))
                        .equalTo(AllTypes.FIELD_BOOLEAN, random.nextBoolean())
                        .findAll();
            } else {
                results = realm.where(AllTypes.class).findAll();
            }

            results.setString(AllTypes.FIELD_STRING, "Updated: " + Thread.currentThread().getName());
            results.setBoolean(AllTypes.FIELD_BOOLEAN, random.nextBoolean());
        };

        if (asyncTransaction) {
            return createTaskInHandlerThread(((realm, success) -> {
                RealmLog.error("Updating objects (async): " + Thread.currentThread().getName());
                realm.executeTransactionAsync(update::run, success::countDown);
            }));
        } else {
            return createTaskInThread((realm) -> {
                RealmLog.error("Updating objects: " + Thread.currentThread().getName());
                realm.executeTransaction(update::run);
            });
        }
    }

    private ThreadWrapper readObjects(boolean asyncQuery) {
        if (asyncQuery) {
            return createTaskInHandlerThread(new AsyncTaskRunner() {
                private RealmResults<AllTypes> liveResults;
                @Override
                public void run(Realm realm, CountDownLatch success) {
                    RealmLog.error("Reading objects (async): " + Thread.currentThread().getName());
                    liveResults = realm.where(AllTypes.class)
                            .lessThan(AllTypes.FIELD_LONG, random.nextInt((int) realm.where(AllTypes.class).count() + 1))
                            .equalTo(AllTypes.FIELD_BOOLEAN, random.nextBoolean())
                            .findAllAsync();
                    liveResults.addChangeListener((updatedResults, changeSet) -> {
                        for (AllTypes result : updatedResults) {
                            assertFalse(TextUtils.isEmpty(result.getColumnString()));
                        }
                        if (updatedResults.isLoaded()) {
                            RealmLog.error("Query finished on: " + Thread.currentThread().getName());
                            success.countDown();
                        }
                    });
                }
            });
        } else {
            return createTaskInThread((realm) -> {
                RealmLog.error("Reading objects: " + Thread.currentThread().getName());
                RealmResults<AllTypes> results = realm.where(AllTypes.class)
                        .lessThan(AllTypes.FIELD_LONG, random.nextInt((int) realm.where(AllTypes.class).count() + 1))
                        .equalTo(AllTypes.FIELD_BOOLEAN, random.nextBoolean())
                        .findAll();
                for (AllTypes result : results) {
                    assertFalse(TextUtils.isEmpty(result.getColumnString()));
                }
            });
        }
    }

    private ThreadWrapper createTaskInThread(TaskRunner runnable) {
        return new ThreadWrapper() {
            private Thread thread;

            @Override
            public void start() {
                thread = new Thread(() -> {
                    Realm realm = Realm.getInstance(realmConfig);
                    runnable.run(realm);
                    realm.close();
                }, "Worker: " + workerThreadId.incrementAndGet());
                thread.start();
            }

            @Override
            public void join() {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private ThreadWrapper createTaskInHandlerThread(AsyncTaskRunner wrapper) {
        return new ThreadWrapper() {
            CountDownLatch successLatch = new CountDownLatch(1);
            CountDownLatch closeLatch = new CountDownLatch(1);
            volatile Handler handler;
            volatile HandlerThread handlerThread;
            AtomicReference<Realm> realm = new AtomicReference<>(null);
            AsyncTaskRunner wrapperStrongRef = wrapper;

            @Override
            public void start() {
                handlerThread = new HandlerThread("HandlerWorker: " + workerThreadId.incrementAndGet());
                handlerThread.start();
                Looper looper = handlerThread.getLooper();
                handler = new Handler(looper);
                handler.post(() -> {
                    realm.set(Realm.getInstance(realmConfig));
                    wrapperStrongRef.run(realm.get(), successLatch);
                });
            }

            @Override
            public void join() {
                TestHelper.awaitOrFail(successLatch);
                handler.post(() -> {
                    realm.get().close();
                    closeLatch.countDown();
                });
                TestHelper.awaitOrFail(closeLatch);
                handlerThread.quitSafely();
            }
        };
    }
}
