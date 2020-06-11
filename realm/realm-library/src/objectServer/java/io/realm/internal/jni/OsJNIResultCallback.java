/*
 * Copyright 2020 Realm Inc.
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

package io.realm.internal.jni;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.mongodb.ErrorCode;
import io.realm.mongodb.AppException;
import io.realm.internal.Keep;
import io.realm.internal.objectstore.OsJavaNetworkTransport;

// Common callback for handling results from the ObjectStore layer.
// NOTE: This class is called from JNI. If renamed, adjust callbacks in App.cpp
@Keep
public abstract class OsJNIResultCallback<T> extends OsJavaNetworkTransport.NetworkTransportJNIResultCallback {

    private final AtomicReference<T> success;
    private final AtomicReference<AppException> error;

    public OsJNIResultCallback(@Nullable AtomicReference<T> success, AtomicReference<AppException> error) {
        this.success = success;
        this.error = error;
    }

    @Override
    public void onSuccess(Object result) {
        T mappedResult = mapSuccess(result);
        if (success != null) {
            success.set(mappedResult);
        }
    }

    // Must map the underlying success Object to the appropriate type in Java
    protected abstract T mapSuccess(Object result);

    @Override
    public void onError(String nativeErrorCategory, int nativeErrorCode, String errorMessage) {
        ErrorCode code = ErrorCode.fromNativeError(nativeErrorCategory, nativeErrorCode);
        if (code == ErrorCode.UNKNOWN) {
            // In case of UNKNOWN errors parse as much error information on as possible.
            String detailedErrorMessage = String.format("{%s::%s} %s", nativeErrorCategory, nativeErrorCode, errorMessage);
            error.set(new AppException(code, detailedErrorMessage));
        } else {
            error.set(new AppException(code, errorMessage));
        }
    }
}
