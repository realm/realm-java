package io.realm.objectserver.credentials;

import java.net.URL;

import io.realm.objectserver.ObjectServer;

/**
 * An access token is normally acquired from another set of credentials that are sent to the configured Realm
 * Authentication Server. However in some cases, the required access token was either saved or provided by some
 * other means.
 *
 * In that case the access token can just be used directly as credentials by using this class.
 */
public class AccessTokenCredentials extends Credentials {

    /**
     * Creates a Facebook credentials token that will use the global configured Authentication Server.
     *
     * @param token The oAuth2 token received from Facebook.
     * @param createUser creates a new credentials on the Authentication Server if it doesn't exists already.
     *
     * @see <a href="LINK_HERE">Tutorial showing how to get an token using Facebooks SDK</a>
     * @see ObjectServer#setGlobalAuthentificationServer(URL);
     */
    public AccessTokenCredentials(String accessToken, String refreshToken) {
        super(ObjectServer.getGlobalAuthentificationServer(), false);
    }

    @Override
    public LoginType getLoginType() {
        return LoginType.ACCESS_TOKEN;
    }

    @Override
    public String getToken() {
        return "";
    }
}
