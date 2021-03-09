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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.TestHelper;
import io.realm.entities.AllTypes;
import io.realm.entities.NonLatinFieldNames;
import io.realm.log.LogLevel;
import io.realm.log.RealmLog;
import io.realm.TestRealmConfigurationFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * Class used to stress test multiple actions across different threads.
 * This doesn't attempt to test correctness beyond "Don't Crash".
 *
 * Some error level logging is done during the run of this. This is mostly to make
 * it clearer what has happened in the case a run actually did crash, and doesn't indicate
 * problems with the test as such.
 */
@Ignore("Bug in Core: https://github.com/realm/realm-core/issues/4465")
@RunWith(Parameterized.class)
public class ThreadStressTests {

    @Parameterized.Parameters(name = "Encryption: {0}, ReuseThreads: {1}")
    public static List<Boolean[]> parameters() {
        ArrayList<Boolean[]> list = new ArrayList<>();
        list.add(new Boolean[] { Boolean.TRUE, Boolean.TRUE });
        list.add(new Boolean[] { Boolean.TRUE, Boolean.FALSE });
        list.add(new Boolean[] { Boolean.FALSE, Boolean.TRUE });
        list.add(new Boolean[] { Boolean.FALSE, Boolean.FALSE });
        return list;
    }

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @Parameterized.Parameter
    public boolean reuseThreads;
    @Parameterized.Parameter(1)
    public boolean useEncryption;

    private int originalLogLevel;
    private final static int MAX_THREADS = 100;
    private final static int MAX_CREATE = 1000;
    private ExecutorService executor;
    private RealmConfiguration realmConfig;
    private Random random;
    private List<Future> threads = new CopyOnWriteArrayList<>();
    private AtomicInteger workerThreadId = new AtomicInteger(0);

    enum CRUDAction {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }

    public interface AsyncTaskRunner {
        void run(Realm realm, CountDownLatch success);
    }

    public interface TaskRunner {
        void run(Realm realm);
    }

    @Before
    public void setUp() {
        originalLogLevel = RealmLog.getLevel();
        RealmLog.setLevel(LogLevel.INFO);
        long seed = System.currentTimeMillis();
        RealmLog.info("Starting stress test with seed: " + seed);
        random = new Random(seed);
        RealmConfiguration.Builder builder = configFactory.createConfigurationBuilder();
        if (useEncryption) {
            builder.encryptionKey(TestHelper.getRandomKey(seed));
        }
        realmConfig = configFactory.createConfiguration();
        Realm.deleteRealm(realmConfig);
        executor = Executors.newFixedThreadPool(reuseThreads ? Math.max(random.nextInt(MAX_THREADS), 1) : MAX_THREADS);
    }

    @After
    public void tearDown() {
        RealmLog.setLevel(originalLogLevel);
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
    public void threadStressTest() throws ExecutionException, InterruptedException {
        Realm realm = Realm.getInstance(realmConfig);
        populateTestRealm(realm);
        for (int i = 0; i < MAX_THREADS; i++) {
            CRUDAction action = CRUDAction.values()[random.nextInt(4)];
            Runnable task = null;
            switch(action) {
                case CREATE:
                    task = createObjects(random.nextInt(MAX_CREATE), random.nextBoolean());
                    break;
                case READ:
                    task = readObjects(random.nextBoolean());
                    break;
                case UPDATE:
                    task = updateObjects(random.nextBoolean(), random.nextBoolean());
                    break;
                case DELETE:
                    task = deleteObjects(random.nextBoolean(), random.nextBoolean());
                    break;
            }
            threads.add(executor.submit(task));
        }
        for (Future task : threads) {
            assertNull(task.get());
        }
        realm.close();
    }

    private Runnable createObjects(int objectsCount, boolean asyncTransaction) {
        if (asyncTransaction) {
            return createTaskInHandlerThread((realm, success) -> {
                RealmLog.info("Creating objects (async): " + Thread.currentThread().getName());
                realm.executeTransactionAsync(bgRealm -> populateTestRealm(bgRealm, objectsCount), success::countDown);
            });
        } else {
            return createTaskInThread((realm) -> {
                RealmLog.info("Creating objects: " + Thread.currentThread().getName());
                populateTestRealm(realm, objectsCount);
            });
        }
    }

    private Runnable deleteObjects(boolean filterObjects, boolean asyncTransaction) {
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
                RealmLog.info("Deleting objects (async): " + Thread.currentThread().getName());
                realm.executeTransactionAsync(delete::run, success::countDown);
            }));
        } else {
            return createTaskInThread((realm) -> {
                RealmLog.info("Deleting objects: " + Thread.currentThread().getName());
                realm.executeTransaction(delete::run);
            });
        }
    }


    private Runnable updateObjects(boolean filterObjects, boolean asyncTransaction) {
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
                RealmLog.info("Updating objects (async): " + Thread.currentThread().getName());
                realm.executeTransactionAsync(update::run, success::countDown);
            }));
        } else {
            return createTaskInThread((realm) -> {
                RealmLog.info("Updating objects: " + Thread.currentThread().getName());
                realm.executeTransaction(update::run);
            });
        }
    }

    private Runnable readObjects(boolean asyncQuery) {
        if (asyncQuery) {
            return createTaskInHandlerThread(new AsyncTaskRunner() {
                private RealmResults<AllTypes> liveResults;
                @Override
                public void run(Realm realm, CountDownLatch success) {
                    RealmLog.info("Reading objects (async): " + Thread.currentThread().getName());
                    liveResults = realm.where(AllTypes.class)
                            .lessThan(AllTypes.FIELD_LONG, random.nextInt((int) realm.where(AllTypes.class).count() + 1))
                            .equalTo(AllTypes.FIELD_BOOLEAN, random.nextBoolean())
                            .findAllAsync();
                    liveResults.addChangeListener((updatedResults, changeSet) -> {
                        for (AllTypes result : updatedResults) {
                            assertFalse(TextUtils.isEmpty(result.getColumnString()));
                        }
                        if (updatedResults.isLoaded()) {
                            RealmLog.info("Query finished on: " + Thread.currentThread().getName());
                            success.countDown();
                        }
                    });
                }
            });
        } else {
            return createTaskInThread((realm) -> {
                RealmLog.info("Reading objects: " + Thread.currentThread().getName());
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

    private Runnable createTaskInThread(TaskRunner runnable) {
        return () -> {
            Realm realm = Realm.getInstance(realmConfig);
            runnable.run(realm);
            realm.close();
        };
    }

    private Runnable createTaskInHandlerThread(AsyncTaskRunner wrapper) {
        return new Runnable() {
            CountDownLatch successLatch = new CountDownLatch(1);
            CountDownLatch closeLatch = new CountDownLatch(1);
            volatile Handler handler;
            volatile HandlerThread handlerThread;
            AtomicReference<Realm> realm = new AtomicReference<>(null);
            AsyncTaskRunner wrapperStrongRef = wrapper;

            @Override
            public void run() {
                handlerThread = new HandlerThread("HandlerWorker: " + workerThreadId.incrementAndGet());
                handlerThread.start();
                Looper looper = handlerThread.getLooper();
                handler = new Handler(looper);
                handler.post(() -> {
                    realm.set(Realm.getInstance(realmConfig));
                    wrapperStrongRef.run(realm.get(), successLatch);
                });

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
