package io.realm;


import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import io.realm.internal.network.AuthenticateRequest;
import io.realm.internal.network.AuthenticationServer;
import io.realm.internal.objectserver.Token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class AuthenticateRequestTests {

    @Before
    public void setUp() {
        Realm.init(InstrumentationRegistry.getTargetContext());
    }

    // Tests based on the schemas described here: https://github.com/realm/realm-sync-services/blob/master/doc/index.apib

    @Test
    public void realmLogin() throws URISyntaxException, JSONException {
        Token t = SyncTestUtils.createTestUser().getRefreshToken();
        AuthenticateRequest request = AuthenticateRequest.realmLogin(t, new URI("realm://objectserver/" + t.identity() + "/default").getPath());

        JSONObject obj = new JSONObject(request.toJson());
        assertEquals("/" + t.identity() + "/default", obj.get("path"));
        assertEquals(t.value(), obj.get("data"));
        assertEquals("realm", obj.get("provider"));
    }

    @Test
    public void userLogin() throws URISyntaxException, JSONException {
        AuthenticateRequest request = AuthenticateRequest.userLogin(SyncCredentials.facebook("foo"));

        JSONObject obj = new JSONObject(request.toJson());
        assertFalse(obj.has("path"));
        assertEquals("foo", obj.get("data"));
        assertEquals("facebook", obj.get("provider"));
    }

    @Test
    public void userRefresh() throws URISyntaxException, JSONException {
        Token t = SyncTestUtils.createTestUser().getRefreshToken();
        AuthenticateRequest request = AuthenticateRequest.userRefresh(t, new URI("realm://objectserver/" + t.identity() + "/default").getPath());

        JSONObject obj = new JSONObject(request.toJson());
        assertTrue(obj.has("path"));
        assertEquals(t.value(), obj.get("data"));
        assertEquals("realm", obj.get("provider"));
    }


    @Test
    public void errorsNotWrapped() {
        AuthenticationServer originalAuthServer = SyncManager.getAuthServer();
        AuthenticationServer authServer = Mockito.mock(AuthenticationServer.class);
        when(authServer.loginUser(any(SyncCredentials.class), any(URL.class))).thenReturn(SyncTestUtils.createErrorResponse(ErrorCode.ACCESS_DENIED));
        SyncManager.setAuthServerImpl(authServer);

        try {
            SyncUser.logIn(SyncCredentials.facebook("foo"), "http://foo.bar/auth");
            fail();
        } catch (ObjectServerError e) {
            assertEquals(ErrorCode.ACCESS_DENIED, e.getErrorCode());
        } finally {
            // Reset the auth server implementation for other tests.
            SyncManager.setAuthServerImpl(originalAuthServer);
        }
    }
}
