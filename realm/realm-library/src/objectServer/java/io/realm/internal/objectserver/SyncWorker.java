/*
 * Copyright 2019 Realm Inc.
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

package io.realm.internal.objectserver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class represents a value describing a sync worker on the Realm Cloud.
 */
public class SyncWorker {

    private static final String KEY_PATH = "path";

    private final String path;

    public static SyncWorker from(JSONObject syncWorker) throws JSONException {
        String path = syncWorker.getString(KEY_PATH);

        return new SyncWorker(path);
    }

    public SyncWorker(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
