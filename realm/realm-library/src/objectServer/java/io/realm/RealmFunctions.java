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
package io.realm;

import org.bson.BsonDocument;
import org.bson.BsonElement;
import org.bson.BsonValue;

import java.util.ArrayList;
import java.util.List;

import io.realm.internal.Util;
import io.realm.internal.util.BsonConverter;

/**
 * A <i>Realm functions<i> manager to call MongoDB functions.
 */
// TODO Timeout is currently handled uniformly through OkHttpNetworkTransport configured through RealmAppConfig
public class RealmFunctions {

    /**
     * Call a Stitch function synchronously.
     *
     * @param name Name of the Stitch function to call.
     * @param args Arguments to the Stitch function.
     * @return Result of the Stitch function.
     *
     * // FIXME Document possible exceptions
     * @throws
     */
    public BsonValue callFunction(String name, BsonValue... args) {
        List<BsonElement> elements = new ArrayList<>(args.length);
        int i = 0;
        for (BsonValue arg : args) {
            // FIXME Bson argument naming
            elements.add(new BsonElement("arg" + i, arg));
            ++i;
        }
        BsonDocument document = new BsonDocument(elements);

        String resultString = invoke(name, document.toJson());

        BsonDocument resultDocument = BsonDocument.parse(resultString);
        // FIXME Guard if no values, etc. when conventions are clarified
        BsonValue result = resultDocument.values().iterator().next();
        return result;
    }

    <T extends BsonValue> T callFunctionTyped(String name, Class<T> clz, BsonValue... args) {
        BsonValue value = callFunction(name, args);
        if (clz.isInstance(value)) {
            return (T) value;
        } else {
            throw new RuntimeException("Cannot convert " + value + " to " + clz.getSimpleName());
        }
    }

    <T extends BsonValue> T callFunctionTyped(String name, Class<T> clz, Object... args) {
        List<BsonValue> bsonArgs = BsonConverter.to(args);
        return callFunctionTyped(name, clz, bsonArgs.get(0));
    }

    <T> T callFunctionNativeTyped(String name, Class<T> clz, Object... args) {
        Class<? extends BsonValue> t = BsonConverter.bsontype(clz);
        BsonValue response = callFunctionTyped(name, t, args);
        return BsonConverter.from(clz, response);
    }

    /**
     * Call a Stitch function asynchronously.
     *
     * This is the asynchronous equivalent of {@link #callFunction(String, BsonValue...)}.
     *
     * @param name Name of the Stitch function to call.
     * @param callback The callback to invoke on success
     * @param args Arguments to the Stitch function.
     * @return Result of the Stitch function.
     *
     * // FIXME Any ObjectServer issues
     * @throws IllegalStateException if not called on a looper thread.
     *
     * @see #callFunction(String, BsonValue...)
     */
    // FIXME Evaluate original asynchronous Stitch API relying on Google Play Tasks. For now just
    //  use a RealmAsyncTask
    //  https://docs.mongodb.com/stitch-sdks/java/4/com/mongodb/stitch/android/core/services/StitchServiceClient.html
    //  <ResultT> Task<ResultT> callFunction​(String name, List<?> args, Long requestTimeout, Class<ResultT> resultClass, CodecRegistry codecRegistry);
    RealmAsyncTask callFunctionAsync(String name, RealmApp.Callback<BsonValue> callback, BsonValue... args) {
        Util.checkLooperThread("Asynchronous functions is only possible from looper threads.");
        return new RealmApp.Request<BsonValue>(RealmApp.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public BsonValue run() throws ObjectServerError {
                return callFunction(name, args);
            }
        }.start();
    }

    // FIXME Basically just wrapping to allow mocking it through mockito
    private String invoke(String name, String args) {
        // Native calling scheme is actually synchronous
        // FIXME For now just return args directly until actual native call is in place
        return args;
   }

//    private static native String nativeCallFunction(String name, @Nullable String argsJson, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);

}

