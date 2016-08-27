/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
