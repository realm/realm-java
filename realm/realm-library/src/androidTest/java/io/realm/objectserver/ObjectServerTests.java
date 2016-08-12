package io.realm.objectserver;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.objectserver.credentials.AccessTokenCredentials;
import io.realm.objectserver.credentials.CredentialsHandler;
import io.realm.objectserver.session.Session;
import io.realm.rule.TestRealmConfigurationFactory;

@RunWith(AndroidJUnit4.class)
public class ObjectServerTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm realm;
    private Context context;

    @Before
    public void setUp() throws MalformedURLException {
        ObjectServer.setGlobalAuthentificationServer(new URL("realm://sync.realm.io/auth"));
        context = InstrumentationRegistry.getTargetContext();
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
        ObjectServerConfiguration syncConfig = new ObjectServerConfiguration.Builder(context)
                // Standard Realm properties
                .name("testrealm.realm") // Local name

//                // ROS specific
                .remoteRealm("realm://realm.io/testrealm")
                .credentials(new CredentialsHandler() {
                    @Override
                    public void getCredentials(Session session) {
                        // Get the credentials somehow
                        session.setCredentials(new AccessTokenCredentials("accessToken", "refreshToken"));
                    }
                })
                .build();


        // Extra. Manually start synchronizing.
        // Will trigger a request for credentials if they are not provided by the configuration

        // As normal. `bind` can be done before or after, doesn't matter.
        Realm realm = Realm.getInstance(syncConfig);

        // If needed
//        session.disconnect();



        // ClientHistory? Why do we need a different one for Sync?
    }



}
