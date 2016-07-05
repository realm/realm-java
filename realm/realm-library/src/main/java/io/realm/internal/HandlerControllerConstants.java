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

package io.realm.internal;

/**
 * This class is to share some Android handler related constants between package {@link io.realm} and
 * {@link io.realm.internal.async}.
 */
public final class HandlerControllerConstants {
    public static final int REALM_CHANGED = 14930352; // Hopefully it won't clash with other message IDs.
    public static final int COMPLETED_UPDATE_ASYNC_QUERIES = 24157817;
    public static final int COMPLETED_ASYNC_REALM_RESULTS = 39088169;
    public static final int COMPLETED_ASYNC_REALM_OBJECT = 63245986;
    public static final int REALM_ASYNC_BACKGROUND_EXCEPTION = 102334155;
    public static final int LOCAL_COMMIT = 165580141;
}
