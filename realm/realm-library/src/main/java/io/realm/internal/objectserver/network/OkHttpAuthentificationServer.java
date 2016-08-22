package io.realm.internal.objectserver.network;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import io.realm.internal.Util;
import io.realm.internal.objectserver.Error;
import io.realm.internal.objectserver.Token;
import io.realm.objectserver.Credentials;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpAuthentificationServer implements AuthentificationServer {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * Authenticate the given credentials on the specified Realm Authentication Server.
     */
    @Override
    public AuthenticateResponse authenticateUser(Credentials credentials, URL authentificationUrl) {
        try {

            String requestBody = AuthenticateRequest.fromCredentials(credentials).toJson();
            Request request = new Request.Builder()
                    .url(authentificationUrl)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("Connection","close") //  See https://github.com/square/okhttp/issues/2363
                    .post(RequestBody.create(JSON, requestBody))
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            return AuthenticateResponse.createFrom(response);
        } catch (Exception e) {
            return new AuthenticateResponse(Error.OTHER_ERROR, Util.getStackTrace(e));
        }
    }

    @Override
    public AuthenticateResponse authenticateRealm(Token refreshToken, String path, URL authentificationUrl) {
        return null;
    }

    @Override
    public RefreshResponse refresh(String token, URL authentificationUrl) {
        return null;
    }
}
