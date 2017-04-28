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

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObject;
import io.realm.TestHelper;
import io.realm.annotations.RealmModule;

import static org.junit.Assert.assertTrue;

/**
 * Rule that creates the {@link RealmConfiguration } in a temporary directory and deletes the Realm created with that
 * configuration once the test finishes. Be sure to close all Realm instances before finishing the test. Otherwise
 * {@link Realm#deleteRealm(RealmConfiguration)} will throw an exception in the {@link #after()} method.
 * The temp directory will be deleted regardless if the {@link Realm#deleteRealm(RealmConfiguration)} fails or not.
 */
public class TestRealmConfigurationFactory extends TemporaryFolder {
    private final Map<RealmConfiguration, Boolean> map = new ConcurrentHashMap<RealmConfiguration, Boolean>();
    private final Set<RealmConfiguration> configurations = Collections.newSetFromMap(map);

    private boolean unitTestFailed = false;

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                } catch (Throwable throwable) {
                    setUnitTestFailed();
                    throw throwable;
                } finally {
                    after();
                }
            }
        };
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        Realm.init(InstrumentationRegistry.getTargetContext());
    }

    @Override
    protected void after() {
        // Waits all async tasks done to ensure successful deleteRealm call.
        // This will throw when timeout. And the reason of timeout needs to be solved properly.
        TestHelper.waitRealmThreadExecutorFinish();

        try {
            for (RealmConfiguration configuration : configurations) {
                Realm.deleteRealm(configuration);
            }
        } catch (IllegalStateException e) {
            // Only throws the exception caused by deleting the opened Realm if the test case itself doesn't throw.
            if (!isUnitTestFailed()) {
                throw e;
            }
        } finally {
            // This will delete the temp directory.
            super.after();
        }
    }

    public synchronized void setUnitTestFailed() {
        this.unitTestFailed = true;
    }

    private synchronized boolean isUnitTestFailed() {
        return this.unitTestFailed;
    }

    // This builder creates a configuration that is *NOT* managed.
    // You have to delete it yourself.
    public RealmConfiguration.Builder createConfigurationBuilder() {
        return new RealmConfiguration.Builder().directory(getRoot());
    }

    public RealmConfiguration createConfiguration() {
        return createConfiguration(null);
    }

    public RealmConfiguration createConfiguration(String name) {
        return createConfiguration(null, name);
    }

    public RealmConfiguration createConfiguration(String subDir, String name) {
        return createConfiguration(subDir, name, null, null);
    }

    public RealmConfiguration createConfiguration(String name, byte[] key) {
        return createConfiguration(null, name, null, key);
    }

    public RealmConfiguration createConfiguration(String name, Object module) {
        return createConfiguration(null, name, module, null);
    }

    public RealmConfiguration createConfiguration(String subDir, String name, Object module, byte[] key) {
        RealmConfiguration.Builder builder = createConfigurationBuilder();

        File folder = getRoot();
        if (subDir != null) {
            folder = new File(folder, subDir);
            assertTrue(folder.mkdirs());
        }
        builder.directory(folder);

        if (name != null) {
            builder.name(name);
        }

        if (module != null) {
            builder.modules(module);
        }

        if (key != null) {
            builder.encryptionKey(key);
        }

        RealmConfiguration configuration = builder.build();
        configurations.add(configuration);

        return configuration;
    }

    // Copies a Realm file from assets to temp dir
    public void copyRealmFromAssets(Context context, String realmPath, String newName) throws IOException {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .directory(getRoot())
                .name(newName)
                .build();

        copyRealmFromAssets(context, realmPath, config);
    }

    public void copyRealmFromAssets(Context context, String realmPath, RealmConfiguration config) throws IOException {
        // Deletes the existing file before copy
        Realm.deleteRealm(config);

        File outFile = new File(config.getRealmDirectory(), config.getRealmFileName());

        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = context.getAssets().open(realmPath);
            os = new FileOutputStream(outFile);

            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buf)) > -1) {
                os.write(buf, 0, bytesRead);
            }
        } finally {
            if (is != null) {
                try { is.close(); } catch (IOException ignore) {}
            }
            if (os != null) {
                try { os.close(); } catch (IOException ignore) {}
            }
        }
    }
}
