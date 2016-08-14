package io.realm.internal.objectserver.network;

import java.util.concurrent.TimeUnit;

import io.realm.BuildConfig;
import io.realm.objectserver.credentials.Credentials;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OkHttpAuthentificationServer implements AuthentificationServer {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final String appId;

    public OkHttpAuthentificationServer() {
        appId = BuildConfig.APPLICATION_ID;
    }

    /**
     * Authenticate the given credentials on the specified Realm Authentication Server.
     * This
     * @param credentials
     * @return
     * @throws Exception
     */
    @Override
    public AuthenticateResponse authenticate(Credentials credentials) {

        Request request = new Request.Builder()
                .url("http://publicobject.com/helloworld.txt")
                .build();

        Call call = client.newCall(request);
//
//        return new OkHttpNetworkRequest<AuthenticateResponse>(call) {
//            @Override
//            protected AuthenticateResponse convert(Response response) {
//                // TODO Convert
//                return null;
//            }
//        };
        return null;
    }
}
