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
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.benchmarks.config.BenchmarkConfig;
import io.realm.benchmarks.entities.AllTypes;


@RunWith(SpannerRunner.class)
public class RealmQueryBenchmarks {

    private static final int DATA_SIZE = 1000;

    @BenchmarkConfiguration
    public SpannerConfig configuration = BenchmarkConfig.getConfiguration(this.getClass().getCanonicalName());

    private Realm realm;

    @BeforeExperiment
    public void before() {
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
        realm.beginTransaction();
        for (int i = 0; i < DATA_SIZE; i++) {
            AllTypes obj = realm.createObject(AllTypes.class);
            obj.setColumnLong(i);
            obj.setColumnBoolean(i % 2 == 0);
            obj.setColumnString("Foo " + i);
            obj.setColumnDouble(i + 1.234D);
        }
        realm.commitTransaction();
    }

    @AfterExperiment
    public void after() {
        realm.close();
    }

    @Benchmark
    public void containsQuery(long reps) {
        for (long i = 0; i < reps; i++) {
            RealmResults<AllTypes> realmResults = realm.where(AllTypes.class).contains(AllTypes.FIELD_STRING, "Foo 1").findAll();
        }
    }

    @Benchmark
    public void count(long reps) {
        for (long i = 0; i < reps; i++) {
            long size = realm.where(AllTypes.class).count();
        }
    }

    @Benchmark
    public void findAll(long reps) {
        for (long i = 0; i < reps; i++) {
            RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        }
    }

    @Benchmark
    public void findAllSortedOneField(long reps) {
        for (long i = 0; i < reps; i++) {
            RealmResults<AllTypes> results = realm.where(AllTypes.class).findAllSorted(AllTypes.FIELD_STRING, Sort.ASCENDING);
        }
    }
}
