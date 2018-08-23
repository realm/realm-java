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
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.benchmarks.config.BenchmarkConfig;
import io.realm.benchmarks.entities.AllTypes;


@RunWith(SpannerRunner.class)
public class RealmAllocBenchmarks {
    @BenchmarkConfiguration
    public SpannerConfig configuration = BenchmarkConfig.getConfiguration(this.getClass().getCanonicalName());

    private Realm realm;

    @BeforeExperiment
    public void before() {
        Realm.init(InstrumentationRegistry.getTargetContext());
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
        realm.beginTransaction();
        realm.createObject(AllTypes.class).getColumnRealmList().add(realm.createObject(AllTypes.class));
        realm.commitTransaction();
    }

    @AfterExperiment
    public void after() {
        realm.close();
    }

    @Benchmark
    public void createObjects(long reps) {
        RealmResults<AllTypes> results = realm.where(AllTypes.class).findAll();
        for (long i = 0; i < reps; i++) {
            results.first();
        }
    }

    @Benchmark
    public void createQueries(long reps) {
        for (long i = 0; i < reps; i++) {
            realm.where(AllTypes.class);
        }
    }
    @Benchmark
    public void createRealmResults(long reps) {
        RealmQuery<AllTypes> query = realm.where(AllTypes.class);
        for (long i = 0; i < reps; i++) {
            query.findAll();
        }
    }

    @Benchmark
    public void createRealmLists(long reps) {
        AllTypes allTypes = realm.where(AllTypes.class).findFirst();
        for (long i = 0; i < reps; i++) {
            //noinspection ConstantConditions
            allTypes.getColumnRealmList();
        }
    }
}
