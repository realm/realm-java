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

package io.realm.rule;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static org.junit.Assert.assertTrue;

/**
 * Rule that creates the {@link RealmConfiguration } in a temporary directory and deletes the Realm created with that
 * configuration once the test finishes. Be sure to close all Realm instances before finishing the test. Otherwise
 * {@link Realm#deleteRealm(RealmConfiguration)} will throw an exception in the {@link #after()} method.
 * The temp directory will be deleted regardless if the {@link Realm#deleteRealm(RealmConfiguration)} fails or not.
 */
public class TestRealmConfigurationFactory extends BaseConfigurationFactory {

    public RealmConfiguration createConfiguration() {
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .directory(getRoot())
                .build();

        registerConfig(configuration);
        return configuration;
    }

    public RealmConfiguration createConfiguration(String subDir, String name) {
        final File folder = new File(getRoot(), subDir);
        assertTrue(folder.mkdirs());
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .directory(folder)
                .name(name)
                .build();

        registerConfig(configuration);
        return configuration;
    }

    public RealmConfiguration createConfiguration(String name) {
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .directory(getRoot())
                .name(name)
                .build();

        registerConfig(configuration);
        return configuration;
    }

    public RealmConfiguration createConfiguration(String name, byte[] key) {
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .directory(getRoot())
                .name(name)
                .encryptionKey(key)
                .build();

        registerConfig(configuration);
        return configuration;
    }

    public RealmConfiguration.Builder createConfigurationBuilder() {
        return new RealmConfiguration.Builder().directory(getRoot());
    }
}
