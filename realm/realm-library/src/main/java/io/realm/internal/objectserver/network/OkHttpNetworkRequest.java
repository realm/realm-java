package io.realm.internal.objectserver.network;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public abstract class OkHttpNetworkRequest<T> implements NetworkRequest<T> {

    private final Call request;

    public OkHttpNetworkRequest(Call request) {
        this.request = request;
    }

    public void run(final Callback<T> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Non-null 'callback' required.");
        }
        request.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    T result = convert(response);
                    callback.onSucces(result);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }

    protected abstract T convert(Response response);

    @Override
    public void cancel() {
        request.cancel();
    }
}
