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
package io.realm.internal.android;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class wraps the Android Context class by extracting all relevant information from the context.
 * This means we only need the Context when {@link io.realm.Realm#init(Context)} is called, but we don't
 * need to hold onto it.
 *
 * This should also make Realm play nicer with Instant Run.
 */
public class ContextWrapper {

    private final AssetManager assetManager;
    private File filesDir;

    public ContextWrapper(Context applicationContext) {
        assetManager = applicationContext.getAssets();
        filesDir = applicationContext.getFilesDir();
    }

    /**
     * Returns the default location for Realm files.
     */
    public File getDefaultRealmFileDirectory() {
        return filesDir;
    }

    /**
     * Returns an asset specified by a given path.
     * On Android this path is assumed to be a path under the {@code assets} folder.
     */
    public InputStream getAsset(String assetFilePath) throws IOException {
        return assetManager.open(assetFilePath);
    }
}
