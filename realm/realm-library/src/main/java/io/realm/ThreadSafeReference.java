/*
 * Copyright 2017 Realm Inc.
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

import java.io.Closeable;
import java.io.IOException;

import io.realm.internal.RealmObjectProxy;
import io.realm.internal.SharedRealm;
import io.realm.internal.objectstore.OsThreadSafeReference;
import io.realm.internal.objectstore.OsType;

public class ThreadSafeReference<E extends ThreadConfined> implements Closeable {

    private static final String ERROR_MSG_UNMANAGED = "Object is unmanaged. Threadsafe references can only be created " +
            "from managed objects.";
    private static final String ERROR_MSG_INVALID = "Object is not valid. It has either been deleted or the Realm is " +
            "closed. A threadsafe reference can only be created from valid Realm objects.";

    // Reference to Object Store wrapper
    private final OsThreadSafeReference osRef;

    /**
     * Creates a thread safe reference for thread confined object. This makes it possible to transfer the object
     * to another thread and access it there.
     *
     * @param object object to create a thread safe reference for.
     * @param <E>
     * @return
     * @throws IllegalArgumentException if {@code null} is used or if a threadsafe reference cannot be created for the
     * class.
     */
    public static <E extends ThreadConfined> ThreadSafeReference<E> create(E object) {
        //noinspection ConstantConditions
        if (object == null) {
            throw new IllegalArgumentException("Non-null object required.");
        }

        long nativeObjectPtr;
        int nativeObjectType;
        SharedRealm realm;

        if (object instanceof RealmModel) {
            RealmModel obj = (RealmModel) object;
            if (!RealmObject.isManaged(obj)) {
                throw new IllegalArgumentException(ERROR_MSG_UNMANAGED);
            }
            if (RealmObject.isValid(obj)) {
                throw new IllegalArgumentException(ERROR_MSG_INVALID);
            }
            RealmObjectProxy proxy = (RealmObjectProxy) obj;
            nativeObjectPtr = proxy.realmGet$proxyState().getRow$realm().getNativePtr();
            nativeObjectType = OsType.OBJECT;
            realm = proxy.realmGet$proxyState().getRealm$realm().sharedRealm;
            return new Object(realm, nativeObjectPtr, object.getClass());

        } else if (object instanceof OrderedRealmCollection) {
            OrderedRealmCollection collection = (OrderedRealmCollection) object;
            if (!collection.isManaged()) {
                throw new IllegalArgumentException(ERROR_MSG_UNMANAGED);
            }
            if (!collection.isValid()) {
                throw new IllegalArgumentException(ERROR_MSG_INVALID);
            }

            OrderedRealmCollectionImpl results = ((OrderedRealmCollectionImpl) collection);
            nativeObjectPtr = results.collection.getNativePtr();
            nativeObjectType = OsType.RESULT;
            realm = results.realm.sharedRealm;

        } else if (object instanceof RealmQuery) {
            RealmQuery query = (RealmQuery) object;
            if (!query.isValid()) {
                throw new IllegalArgumentException(ERROR_MSG_INVALID);
            }

            nativeObjectPtr = query.query.getNativePtr();
            nativeObjectType = OsType.QUERY;
            realm = query.realm.sharedRealm;

        } else {
            throw new IllegalArgumentException(String.format("Thread safe references not supported for : " + object.getClass().getName()));
        }

        return new ThreadSafeReference(realm.createThreadSafeReference(nativeObjectPtr, nativeObjectType));
    }

    private ThreadSafeReference(OsThreadSafeReference osRef) {
        this.osRef = osRef;
    }

    /**
     * Returns weather this thread safe reference is still valid and can be resolved on a thread.
     */
    public boolean isValid() {
        return true; // FIXME
    }

    @Override
    public void close() throws IOException {
        // FIXME Release resources
    }

    private abstract class Base<E> {
        abstract E resolve();
    }

    // Wrapper class for Result types
    private static class RealmResults {

    }

    private class RealmList {

    }

    // Wrapper class for RealmModel objects
    private static class Object<E> extends Base<E> {
        public Object(SharedRealm realm, long nativeObjectPtr, Class<E> modelClass) {

        }

        @Override
        E resolve() {
            return null;
        }
    }

    private class Realm {

    }

    private class Query {

    }
}
