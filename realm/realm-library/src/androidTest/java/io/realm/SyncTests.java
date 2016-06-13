package io.realm;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;

import io.realm.internal.SharedGroup;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;
import io.realm.sync.SyncConfiguration;
import io.realm.sync.SyncManager;
import io.realm.sync.SyncSession;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SyncTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    @Test
    public void validSessionPointer() {
        io.realm.internal.Util.setDebugLevel(5);
        RealmConfiguration config = configFactory.createConfiguration();
        // you can download a pre-compiled sync server here: https://github.com/realm/realm-sync-beta/releases/tag/v0.23.1
        // to start the server locally:  ./realm-server-noinst -r ./tmp -L 192.168.1.65  -l 2
        SyncConfiguration syncConfig = new SyncConfiguration(config, "realm://192.168.1.64/" + config.getRealmFileName());

        SharedGroup realm = new SharedGroup(syncConfig);

        long nativeSessionPointer = realm.startSession(syncConfig.getServer());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }

        //FIXME give a chance to the client thread to start the event loop
        // to process the session bind
        assertNotEquals(0, nativeSessionPointer);
    }

//    @Test
    public void startSync() throws MalformedURLException {
        io.realm.internal.Util.setDebugLevel(5);
        RealmConfiguration config = configFactory.createConfiguration();
//        SyncConfiguration syncConfig = new SyncConfiguration(config, "realm://fr.demo.realmusercontent.com/unittest");
        SyncConfiguration syncConfig = new SyncConfiguration(config, "realm://192.168.1.65/unittest");

        SyncSession session = SyncManager.apply(syncConfig);
        session.start();
    }
}
