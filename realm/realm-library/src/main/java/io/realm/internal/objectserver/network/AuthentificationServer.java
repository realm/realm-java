package io.realm.internal.objectserver.network;

import io.realm.objectserver.credentials.Credentials;

/**
 * Interface for handling communication with the Realm Mobile Platform Authentication Server.
 *
 * Note, any implementation of this class is not responsible for handling retries or error handling, it is
 * only responsible for executing a given network request.
 *
 * All {@link NetworkRequest} are asynchronous and must be manually triggered by calling
 * {@link NetworkRequest#run(OkHttpNetworkRequest.Callback)}.
 */
public interface AuthentificationServer {
    AuthenticateResponse authenticate(Credentials credentials);
}
