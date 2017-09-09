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

package io.realm.benchmarks;

import android.support.test.InstrumentationRegistry;

import org.junit.runner.RunWith;

import dk.ilios.spanner.AfterExperiment;
import dk.ilios.spanner.BeforeExperiment;
import dk.ilios.spanner.Benchmark;
import dk.ilios.spanner.BenchmarkConfiguration;
import dk.ilios.spanner.SpannerConfig;
import dk.ilios.spanner.junit.SpannerRunner;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.benchmarks.config.BenchmarkConfig;
import io.realm.benchmarks.entities.AllTypes;


@RunWith(SpannerRunner.class)
public class RealmObjectReadBenchmarks {

    @BenchmarkConfiguration
    public SpannerConfig configuration = BenchmarkConfig.getConfiguration(this.getClass().getCanonicalName());

    private Realm realm;
    private AllTypes readObject;

    @BeforeExperiment
    public void before() {
        Realm.init(InstrumentationRegistry.getTargetContext());
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                readObject = realm.createObject(AllTypes.class);
                readObject.setColumnString("Foo");
                readObject.setColumnLong(42);
                readObject.setColumnDouble(1.234D);
            }
        });
    }

    @AfterExperiment
    public void after() {
        realm.close();
    }

    @Benchmark
    public void readString(long reps) {
        for (long i = 0; i < reps; i++) {
            String value = readObject.getColumnString();
        }
    }

    @Benchmark
    public void readLong(long reps) {
        for (long i = 0; i < reps; i++) {
            long value = readObject.getColumnLong();
        }
    }

    @Benchmark
    public void readDouble(long reps) {
        for (long i = 0; i < reps; i++) {
            double value = readObject.getColumnDouble();
        }
    }
}
