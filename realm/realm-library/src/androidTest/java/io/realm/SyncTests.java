package io.realm;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;

import io.realm.rule.RunInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;
import io.realm.sync.SyncConfiguration;
import io.realm.sync.SyncManager;
import io.realm.sync.SyncSession;

@RunWith(AndroidJUnit4.class)
public class SyncTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Test
    public void startSync() throws MalformedURLException {
        io.realm.internal.Util.setDebugLevel(5);
        RealmConfiguration config = configFactory.createConfiguration();
        SyncConfiguration syncConfig = new SyncConfiguration(config, "realm://fr.demo.realmusercontent.com");

        SyncSession session = SyncManager.apply(syncConfig);
        session.start();
    }
}
