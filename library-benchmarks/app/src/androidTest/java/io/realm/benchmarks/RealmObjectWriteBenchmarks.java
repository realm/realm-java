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
public class RealmObjectWriteBenchmarks {

    @BenchmarkConfiguration
    public SpannerConfig configuration = BenchmarkConfig.getConfiguration(this.getClass().getCanonicalName());

    private Realm realm;
    private AllTypes writeObject;

    @BeforeExperiment
    public void before() {
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
        realm.beginTransaction();
        writeObject = realm.createObject(AllTypes.class);
    }

    @AfterExperiment
    public void after() {
        realm.cancelTransaction();
        realm.close();
    }

    @Benchmark
    public void writeShortString(long reps) {
        for (long i = 0; i < reps; i++) {
            writeObject.setColumnString("Foo");
        }
    }

    @Benchmark
    public void writeMediumString(long reps) {
        for (long i = 0; i < reps; i++) {
            writeObject.setColumnString("ABCDEFHIJKLMNOPQ");
        }
    }

    @Benchmark
    public void writeLongString(long reps) {
        for (long i = 0; i < reps; i++) {
            writeObject.setColumnString("ABCDEFHIJKLMNOPQABCDEFHIJKLMNOPQABCDEFHIJKLMNOPQABCDEFHIJKLMNOPQ");
        }
    }

    @Benchmark
    public void writeLong(long reps) {
        for (long i = 0; i < reps; i++) {
            writeObject.setColumnLong(42);
        }
    }

    @Benchmark
    public void writeDouble(long reps) {
        for (long i = 0; i < reps; i++) {
            writeObject.setColumnDouble(1.234D);
        }
    }
}
