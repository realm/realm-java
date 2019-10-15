package com.example.testapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.log.RealmLog;

public class MainActivity extends AppCompatActivity {

    private Realm realm;
    private RealmResults<StringOnly> results;
    private Thread t;
    private Thread writerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        encryption_stressTest();
    }

    public void encryption_stressTest() {
        final int TRANSACTIONS = 50;
        final int TEST_OBJECTS = 100_000;
        final int MAX_STRING_LENGTH = 100;
        final AtomicInteger id = new AtomicInteger(0);
        final CountDownLatch writersDone = new CountDownLatch(TRANSACTIONS);
        final CountDownLatch mainReaderDone = new CountDownLatch(1);
        long seed = System.nanoTime();
        RealmLog.error("Starting test with seed: " + seed);
        Random random = new Random(seed);

        final RealmConfiguration config = new RealmConfiguration.Builder() //.configFactory.createConfigurationBuilder()
                .name("stress-test.realm")
                .encryptionKey(getRandomKey(seed))
                .build();
        Realm.deleteRealm(config);
        Realm.getInstance(config).close();

        writerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Realm realm = Realm.getInstance(config);
                for (int i = 0; i < TRANSACTIONS; i++) {
                    realm.executeTransaction(r -> {
                        for (int j = 0; j < (TEST_OBJECTS / TRANSACTIONS); j++) {
                            StringOnly obj = new StringOnly(getRandomString(random.nextInt(MAX_STRING_LENGTH)));
                            r.insert(obj);
                        }
                    });
                }
                realm.close();
                writersDone.countDown();
            }
        });
        writerThread.start();

        realm = Realm.getInstance(config);
        results = realm.where(StringOnly.class).findAllAsync();
        results.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<StringOnly>>() {
            @Override
            public void onChange(RealmResults<StringOnly> results, OrderedCollectionChangeSet changeSet) {
                for (StringOnly obj : results) {
                    String s = obj.getChars();
                }

                RealmLog.info("Progress: " + results.size() + " of " + TEST_OBJECTS);

                if (results.size() == TEST_OBJECTS) {
                    realm.close();
                    mainReaderDone.countDown();
                }
            }
        });

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    writersDone.await();
                    mainReaderDone.await();
                } catch (InterruptedException e) {
                    RealmLog.error("TEST FAILURE: " + e.toString());
                }
                RealmLog.info("TEST SUCCESS");
            }
        });
        t.start();
    }

    // Generate a random string with only capital letters which is always a valid class/field name.
    public static String getRandomString(int length) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) (r.nextInt(26) + 'A')); // Restrict to capital letters
        }
        return sb.toString();
    }

    // Returns a random key used by encrypted Realms.
    public static byte[] getRandomKey(long seed) {
        byte[] key = new byte[64];
        new Random(seed).nextBytes(key);
        return key;
    }

}
