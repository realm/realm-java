package io.realm.internal.network;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.internal.Keep;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.log.RealmLog;
import io.realm.mongodb.AppException;
import io.realm.mongodb.ErrorCode;

/**
 * Specialized case of {@link NetworkRequest} where we are not interested in the response value,
 * just wether or not the request succeeded.
 */
public abstract class VoidNetworkRequest extends NetworkRequest<Void> {

    @Override
    protected Void mapSuccess(Object result) {
        return null;
    }

    /**
     * Run the network request and wait for confirmation that it succeded.
     * This method will block until this confirmation arrives after which it will return.
     *
     * If an error occurred, an {@link AppException} is thrown instead.
     */
    public void run() {
        super.resultOrThrow();
    }
}
