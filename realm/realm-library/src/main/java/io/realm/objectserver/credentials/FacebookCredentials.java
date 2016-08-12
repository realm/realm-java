package io.realm.objectserver.credentials;

import java.net.URL;

import io.realm.objectserver.ObjectServer;

public class FacebookCredentials extends Credentials {

    private final String token;

    /**
     * Creates a Facebook credentials token that will use the global configured Authentication Server.
     *
     * @param token The oAuth2 token received from Facebook.
     * @param createUser creates a new credentials on the Authentication Server if it doesn't exists already.
     *
     * @see <a href="LINK_HERE">Tutorial showing how to get an token using Facebooks SDK</a>
     * @see ObjectServer#setGlobalAuthentificationServer(URL);
     */
    public FacebookCredentials(String token, boolean createUser) {
        this(token, createUser, ObjectServer.getGlobalAuthentificationServer());
    }

    /**
     * Creates a Facebook credentials token.
     *
     * @param token The oAuth2 token received from Facebook.
     * @param createUser creates a new credentials on the Realm Object Server if it doesn't exists already.
     * @param authenticationServer the Server that can be used to validate the credentials.
     *
     * @see <a href="LINK_HERE">Tutorial showing how to get an token using Facebooks SDK</a>
     * @see ObjectServer#setGlobalAuthentificationServer(URL);
     */
    public FacebookCredentials(String token, boolean createUser, URL authenticationServer) {
        super(authenticationServer, createUser);
        this.token = token;
    }

    @Override
    public LoginType getLoginType() {
        return LoginType.FACEBOOK;
    }

    @Override
    public String getToken() {
        return token;
    }
}
