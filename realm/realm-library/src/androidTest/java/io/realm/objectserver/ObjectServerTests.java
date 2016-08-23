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

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.rule.RunInLooperThread;
import io.realm.rule.RunTestInLooperThread;
import io.realm.rule.TestRealmConfigurationFactory;

@RunWith(AndroidJUnit4.class)
public class ObjectServerTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @Rule
    public final RunInLooperThread looperThread = new RunInLooperThread();

    private Realm realm;
    private Context context;

    @Before
    public void setUp() throws MalformedURLException {
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
    @RunTestInLooperThread
    public void exploration() throws MalformedURLException {

        Credentials creds = Credentials.fromUsernamePassword("foo", "bar");
        User.authenticate(creds, new URL("http://127.0.0.1:8080/auth"), new User.Callback() {
            @Override
            public void onSuccess(User user) {
                SyncConfiguration config = new SyncConfiguration.Builder(context)
                        .user(user)
                        .serverUrl("realm://127.0.0.1/~/default")
                        .build();
                Realm.deleteRealm(config);

                Realm realm = Realm.getInstance(config);
            }

            @Override
            public void onError(int i, String s) {
                throw new RuntimeException("Error: " + i + " -> " + s);
            }
        });

//        RealmConfiguration config = new RealmConfiguration.Builder(InstrumentationRegistry.getContext()).build();
//
//        // Realm Object Server requires an "User"
//        // Local User might be a combination of many remote ones
//        // Each remote credentials is validated using a Credential
//        // - How is remote credentials identified?
//
//        // First get credentials
//        // ... then create user
//        // ... then put into Realm configuration
//        // ... now you can open the Realm :(
//
//        // Global settings, all optional
//
//        Session.ErrorHandler sessionErrors = new Session.ErrorHandler() {
//            @Override
//            public void onError(int errorCode, int error) {
//                // List of error codes? Where does HTTP errors come in?
//                // Cannot handle unexpected errors?
//                // Maybe all these should be considered events instead?
//            }
//
//            @Override
//            public void onError(Throwable error) {
//                if (error instanceof ProtocolException) {
//                    // Annoying to do this to get to errorCode/errorMessage
//                }
//            }
//        };
//
//        // Soo many events ... hmmm
//        Session.EventHandler sessionEvents = new Session.EventHandler() {
//            @Override public void sessionStarted(Session session) {};
//            @Override public void realmUnbound(Session session) {};
//            @Override public void bindingRealm(Session session) {};
//            @Override public void realmBound(Session session) {};
//            @Override public void sessionStopped(Session session) {};
//            @Override public void authorizationMissing() {};
//            @Override public void authorizationExpired() {};
//            @Override public void localChangesAvailable() {};
//            @Override public void remoteChangesAvailable() {};
//            @Override public void realmSynchronized() {};
//            @Override public void allRemoteChangesDownloaded() {};
//        };
//
//        User.ErrorHandler userErrors = new User.ErrorHandler() {
//            @Override
//            public void onError(int errorCode, int error) {
//                // List of error codes? Where does HTTP errors come in?
//                // Cannot handle unexpected errors?
//                // Maybe all these should be considered events instead?
//            }
//
//            @Override
//            public void onError(Throwable error) {
//                if (error instanceof ProtocolException) {
//                    // Annoying to do this to get to errorCode/errorMessage
//                }
//            }
//        };
//
//        User.EventHandler userEvents = new User.EventHandler() {
//            @Override public void loggedIn() {};
//            @Override public void loggedOut() {}
//            @Override public void refreshingLogin() {}
//            @Override public void loginRefreshed() {};
//            @Override public void loginRefreshFailed(int errorCode, String errorMsg) {};
//            @Override public void loginExpired() {};
//        };

//
//        ObjectServer.setGlobalAuthentificationServer(new URL("realm://sync.realm.io/auth"));
//        ObjectServer.setGlobalSessionErrorHandler(sessionErrors);
//        ObjectServer.setGlobalSessionEventHandler(sessionEvents);
//        ObjectServer.setGlobalUserErrorHandler(userErrors);
//        ObjectServer.setGlobalUserEventHandler(userEvents);
//
//
//        // Async errors
//        // 1) Get Credentials (out of scope, developer responsibility)
//        // 2) Login ( invalid credentials , protocol error, I/O errors)
//        // 3) Session ( invalid permissions, access expired, protocol errors, I/O errors)
//
//        // Events
//        // User:
//            // logged in, logged out, refreshed
//
//        // Session
//            // started, unbound, binding, authenticating, bound, stopped
//
//        // Credentials
//        // A class for each type. Don't want to expose the underlying JSON
//        User user = User.fromFacebook(getFacebookToken);
//        user.createOnServer(true);
//
//        // Anonymous user syncs changes for statistics/metrics + potential for upgrading. Not possible to merge two anonymous users.
//        // Showing all sync properties
//        ObjectServerConfiguration syncConfig = new ObjectServerConfiguration.Builder(context)
//                // Standard Realm properties
//                .schemaVersion(42) // Future proof? Not really needed right now? Used as developer acknowledgement?
//                .name("realm://realm.io/~/testrealm") // Name used
//                .syncPolicy(new AutomaticSyncPolicy()) // Default value. Controls bind/unbind e.g WifiSyncPolicy()
//                .errorHandler(sessionErrors) // Override global handler
//                .eventHandler(sessionEvents) // Override global handler
//                .build();
//
//
//
//
//
//        ObjectServer.downloadRealm(syncConfig, new ObjectServer.ResultCallback() {
//            @Override
//            public void onSuccess(ObjectServerConfiguration config) {
//                // Why not just part of Session.Eventlistener#realmSynchronized or something like that
//            }
//
//            @Override
//            public void onError(ObjectServerConfiguration config, Exception e) {
//
//            }
//        });
//
//        // Requirements
//            // Easy to find file on disk
//            // Can open a Realm without having the user yet
//            // File should not be moved when a user is attached
//
//        // Arguments
//            // Offline first, means we shouldn't abstract the filesystem away
//            // RealmConfiguration must still be used to open Realms
//
//        // Simple setup
//        // At this stage the developer
//        ObjectServerConfiguration simpleConfig = new ObjectServerConfiguration.Builder(context)
//                .name("local.realm"); // Unavoidable if access is possible without a user
//                .user(user); // Impossible if wanting to generate the config before the concept of a user exists.
//                .user(callback); // If user is required, means Realm.getInstance() also have to be async.
//                .remoteRealm("realm://sync.realm.io/~/default.realm") // url / serverUrl / remoteName / syncUrl ...
//                .build();
//
//        boolean createUserOnServer = true; // Not sure I am fan of having this
//        User user = User.fromFacebook(getFacebookToken(), new URL("realm://sync.realm.io/auth", createUserOnServer);
//
//        // Concerns
//        // Use of /~/ make the URL problematic. It isn't unique.
//        // The RealmConfig might need to be available before the User is.
//
//        // Future use cases (File system strategy?)
//        // - Merge users
//        // - Upgrade anonymous user to "authenticated" user
//
//        User user = User.appSingleton();
//        User.newLocal();
//        User user = User.fromJson(loadUserJson());
//        userToken = user.toJson();
//
//        // Persist which users(realms)
//
//        User.currentUser(); // basically lastUser
//
//        // Requirement
//        // RealmConfiguration should uniquely identify a Realm!!!
//
//
//        // Use cases
//        // App with 1 user
//        //  - (default.realm) -> Fine pattern to create config first, attach user later
//
//                User user = User.local();
//                user.login(new User.Callback() {
//                    @Override
//                    public int onSuccess(User user) {
//                        ObjectServerConfiguration config = new ObjectServerConfiguration.Builder(context)
//                                .name("realm://sync.realm.io/~/default.realm")
//                                .user(user) // Default value
//                                .build();
//                        // Continue with the app
//                    }
//
//                    onError() {
//                        showNoAccess();
//                    }
//                });
//
//                User user = User.local();
//
//                // NO NETWORK!!!
//                ObjectServerConfiguration config = new ObjectServerConfiguration.Builder(context)
//                .name("realm://sync.realm.io/~/default.realm")
//                .user(user) // Default value
//                .build();
//
//                Realm realm = Realm.getInstance(config); // will do login if not already logged in.
//
//
//        user.open(config);
//
//
//        SyncConfiguration newConfig = config.copyWithNewUser(loadUser());
//
//        Realm.setDefaultInstance(config);
//        User.setDefault(loadUser());
//
//        Realm realm = Realm.getInstance(config); // Throw if no user is present
//
//        User user = User.local(new URL("realm://sync.realm.io/auth"), true));
//        user.open(User);
//
//        // App with multiple users
//        // - Configurations are not created up front. A "user" is selected from somewhere, which determines name of local realm
//
//        void switchUser(String id) {
//            User user = User.fromJson(persistence.load(id));
//            ObjectServerConfiguration config = new ObjectServerConfiguration.Builder(context)
//                    .user(loadUser(id))
//                    .remoteRealm("realm://sync.realm.io/~/default.realm")
//                    .build();
//
//            user.open(config);
//        }
//
//
//        Session session = user.connectTo(simpleConfig);
//        Session session = ObjectServer.bind(user, simpleConfig);
//        Session session = SyncManager.connect(user, simpleConfig);
//
//
//        SyncManager.bind(config, user, Syn)
//
//
//
//        // This will trigger all the flow
//        Realm realm = Realm.getInstance(simpleConfig);
//
//
//        // For optional session control
//        Session session = ObjectServer.getSession(simpleConfig);
//        session.start();
//        session.bind();
//        session.unbind();
//        session.stop();
//
//        // Extra. Manually start synchronizing.
//        // Will trigger a request for credentials if they are not provided by the configuration
//
//        // As normal. `bind` can be done before or after, doesn't matter.
//
//        // If needed
////        session.disconnect();
//
//
//
//        // ClientHistory? Why do we need a different one for Sync?
    }

    private String getFacebookToken() {
        return null;
    }


    private User getUser() {
        // Get the user object somehow
        return null;
    }


}
