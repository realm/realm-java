package io.realm.objectserver;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.rule.TestRealmConfigurationFactory;

@RunWith(AndroidJUnit4.class)
public class ObjectServerTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm realm;

    @Before
    public void setUp() {
        RealmObjectServer.setGlobalAuthentificationServer();
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    // TODO Add tests for invalid configuration combinations
    // TODO Add test for opening an old Realm with new Sync (should crash)

    @Test
    public void exploration() {
        RealmConfiguration config = new RealmConfiguration.Builder(InstrumentationRegistry.getContext()).build();

        // Realm Object Server requires an "User"
        // Local User might be a combination of many remote ones
        // Each remote credentials is validated using a Credential
        // - How is remote credentials identified?


//        User rosUser = new User(); // anonymous credentials
//        rosUser.addCredentials(getFacebookToken()); // convert to
//

        RealmObjectServerConfiguration syncConfig = RealmObjectServerConfiguration.from(config)
                .remoteRealm("realm://realm.io/testrealm")
                .credentials(new CredentialsHandler() {
                    void credentialsNeeded(SessionInfo session) {
                        // Get the credentials somehow
                        session.setCredentials(getFacebookCredentials())
                    }
                })
                .replicationPolicy(new SyncPolicy() {
                    @Override
                    public void apply(SessionInfo session) {
                        session.start();

                    }
                })
                .autoConnect() // Will automatically handle connections as part of the normal Realm lifecycle.
                .build();

        // Extra. Manually start synchronizing.
        // Will trigger a request for credentials if they are not provided by the configuration

        // As normal. `connect` can be done before or after, doesn't matter.
        Realm realm = Realm.getDefaultInstance(syncConfig);


        // If needed
        session.disconnect();



        // ClientHistory? Why do we need a different one for Sync?
    }



}
