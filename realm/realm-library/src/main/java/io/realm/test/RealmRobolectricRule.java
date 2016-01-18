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

package io.realm.test;

import android.text.TextUtils;

import org.junit.rules.ExternalResource;

import java.io.File;
import java.lang.StringBuilder;

/**
 * JUnit Rule to initialize support for Robolectric.
 */
public class RealmRobolectricRule extends ExternalResource {
    @Override
    protected void before() throws Throwable {
        final String libraryPathKey = "java.library.path";
        final String previousLibraryPath = System.getProperty(libraryPathKey);
        final String robolectricLibPath = new StringBuilder(".").append(File.separator)
                .append("src").append(File.separator)
                .append("test").append(File.separator)
                .append("libs").toString();

        if (previousLibraryPath.contains(robolectricLibPath)) {
            return;
        }

        final String libraryPath;
        if (TextUtils.isEmpty(previousLibraryPath)) {
            libraryPath = robolectricLibPath;
        } else {
            libraryPath = new StringBuilder(robolectricLibPath).append(File.pathSeparator).append(previousLibraryPath).toString();
        }
        System.setProperty(libraryPathKey, libraryPath);
    }
}

