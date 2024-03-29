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

package io.realm.gradle;

import org.gradle.api.tasks.Input;

import java.util.LinkedHashMap;
import java.util.Map;

public class RealmPluginExtension {
    private boolean syncEnabled;
    private boolean kotlinExtensionsEnabled;

    @Input
    public boolean isSyncEnabled() {
        return syncEnabled;
    }

    public void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }

    @Input
    public boolean isKotlinExtensionsEnabled() {
        return kotlinExtensionsEnabled;
    }

    public void setKotlinExtensionsEnabled(boolean kotlinExtensionsEnabled) {
        this.kotlinExtensionsEnabled = kotlinExtensionsEnabled;
    }
}
