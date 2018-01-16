package io.realm.objectserver;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.StandardIntegrationTest;
import io.realm.SyncConfiguration;
import io.realm.SyncManager;
import io.realm.SyncUser;
import io.realm.TestHelper;
import io.realm.TestSyncConfigurationFactory;
import io.realm.exceptions.RealmException;
import io.realm.objectserver.model.PartialSyncModule;
import io.realm.objectserver.model.PartialSyncObjectA;
import io.realm.objectserver.model.PartialSyncObjectB;
import io.realm.objectserver.utils.Constants;
import io.realm.objectserver.utils.UserFactory;

import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class PartialSyncTests extends StandardIntegrationTest {

    @Rule
    public TestSyncConfigurationFactory configFactory = new TestSyncConfigurationFactory();

}
