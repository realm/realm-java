/*
 * Copyright 2015 Realm Inc.
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

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.TestHelper;

public class BaseConfigurationFactory extends TemporaryFolder {
    private final Map<RealmConfiguration, Boolean> map = new ConcurrentHashMap<RealmConfiguration, Boolean>();
    private final Set<RealmConfiguration> configurations = Collections.newSetFromMap(map);
    protected volatile boolean unitTestFailed = false;

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    base.evaluate();
                } catch (Throwable throwable) {
                    unitTestFailed = true;
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
        // Wait all async tasks done to ensure successful deleteRealm call.
        // This will throw when timeout. And the reason of timeout needs to be solved properly.
        TestHelper.waitRealmThreadExecutorFinish();

        try {
            for (RealmConfiguration configuration : configurations) {
                Realm.deleteRealm(configuration);
            }
        } catch (IllegalStateException e) {
            // Only throw the exception caused by deleting the opened Realm if the test case itself doesn't throw.
            if (!unitTestFailed) {
                throw e;
            }
        } finally {
            // This will delete the temp directory.
            super.after();
        }
    }

    protected void registerConfig(RealmConfiguration config) {
        configurations.add(config);
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
        // Delete the existing file before copy
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
