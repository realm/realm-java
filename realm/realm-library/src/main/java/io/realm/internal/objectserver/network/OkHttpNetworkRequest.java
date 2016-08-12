package io.realm.internal.objectserver.network;

import okhttp3.Call;

public class OkHttpNetworkRequest implements NetworkRequest {

    private final Call request;

    public OkHttpNetworkRequest(Call request) {
        this.request = request;
    }

    @Override
    public void cancel() {
        request.cancel();
    }
}
