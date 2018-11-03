/*
 * Copyright 2018 Realm Inc.
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

import io.realm.annotations.Beta;

/**
 * This class describe how data is saved to Realm when saving whole objects.
 *
 * @see Realm#copyToRealm(RealmModel, ImportFlag...)
 */
@Beta
public enum ImportFlag {

    /**
     * With this flag enabled, fields will not be written to the Realm file if they contain the same
     * value as the value already present in the Realm.
     * <p>
     * For local Realms this only has an impact on change listeners which will not report changes to
     * those fields that was not written.
     * <p>
     * For synchronized Realms this also impacts the server, which will see improved performance as
     * there is less changes to upload and merge into the server Realm.
     * <p>
     * It also impact how the server merges changes from different devices. Realm uses a
     * last-write-wins approach when merging individual fields in an object, so if a field is not
     * written it will be considered "older" than other fields modified.
     * <p>
     * E.g:
     * <ol>
     *     <li>
     *         Server starts out with (Field A = 1, Field B = 1)
     *     </li>
     *     <li>
     *         Device 1 writes (Field A = 2, Field B = 2).
     *     </li>
     *     <li>
     *         Device 2 writes (Field A = 3, Field B = 1) but ignores (Field B = 1), because that is
     *         the value in the Realm file at this point.
     *     </li>
     *     <li>
     *         Device 1 uploads its changes to the server making the server (Field A = 2, Field B = 2).
     *         Then Device 2 uploads its changes. Due to last-write-wins, the server version now
     *         becomes (Field A = 3, Field B = 2).
     *     </li>
     * </ol>
     * This is normally the desired behaviour as the final object is the merged result of the latest
     * changes from both devices, however if all the fields in an object are considered an atomic
     * unit, then this flag should not be set as it will ensure that all fields are set and thus have
     * the same "age" when data are sent to the server.
     *
     * @see <a href="https://docs.realm.io/platform/self-hosted/customize/conflict-resolution">Docs on conflict resolution</a>
     */
    CHECK_SAME_VALUES_BEFORE_SET,

}
