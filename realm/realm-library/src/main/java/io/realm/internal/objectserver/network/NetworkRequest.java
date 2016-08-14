package io.realm.internal.objectserver.network;

/**
 * Wrapper interface for an existing network request.
 */
public interface NetworkRequest<T> {
    void run(Callback<T> callback);
    void cancel();

    interface Callback<T> {
        void onError(Exception e);
        void onSucces(T response);
    }
}
