/*
 * Copyright 2021 Realm Inc.
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

import javax.annotation.Nullable;

import io.realm.internal.MixedNativeFunctions;
import io.realm.internal.TableQuery;
import io.realm.internal.objectstore.OsKeyPathMapping;
import io.realm.internal.objectstore.OsObjectBuilder;


public class MixedNativeFunctionsImpl implements MixedNativeFunctions {

    @Override
    public void handleItem(long listPtr, Mixed mixed) {
        OsObjectBuilder.nativeAddMixedListItem(listPtr, mixed.getNativePtr());
    }

    @Override
    public void callRawPredicate(TableQuery query, @Nullable OsKeyPathMapping mapping, String predicate, Mixed... arguments) {
        long[] args = new long[arguments.length];

        try {
            for (int i = 0; i < arguments.length; i++) {
                args[i] = arguments[i].getNativePtr();
            }
        } catch (IllegalStateException cause) {
            // This might happen if a query is constructed with a Mixed value containing an unmanaged Realm object.
            throw new IllegalArgumentException("Unmanaged Realm objects are not valid query arguments", cause);
        }

        query.rawPredicateWithPointers(mapping, predicate, args);
    }
}
