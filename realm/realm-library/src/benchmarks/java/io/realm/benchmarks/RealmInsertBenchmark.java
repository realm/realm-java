/*
 * Copyright 2017 Realm Inc.
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

import java.util.ArrayList;
import java.util.List;

import dk.ilios.spanner.AfterExperiment;
import dk.ilios.spanner.BeforeExperiment;
import dk.ilios.spanner.Benchmark;
import dk.ilios.spanner.BenchmarkConfiguration;
import dk.ilios.spanner.SpannerConfig;
import dk.ilios.spanner.junit.SpannerRunner;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.benchmarks.config.BenchmarkConfig;
import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;

@RunWith(SpannerRunner.class)
public class RealmInsertBenchmark {

    @BenchmarkConfiguration
    public SpannerConfig configuration = BenchmarkConfig.getConfiguration(this.getClass().getCanonicalName());

    private Realm realm;
    private static final int COLLECTION_SIZE = 100;
    private List<AllTypes> noPkObjects = new ArrayList<AllTypes>(COLLECTION_SIZE);
    private List<AllTypesPrimaryKey> pkObjects = new ArrayList<AllTypesPrimaryKey>(COLLECTION_SIZE);

    @BeforeExperiment
    public void before() {
        Realm.init(InstrumentationRegistry.getTargetContext());
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);

        for (int i = 0; i < COLLECTION_SIZE; i++) {
            noPkObjects.add(new AllTypes());
        }

        for (int i = 0; i < COLLECTION_SIZE; i++) {
            AllTypesPrimaryKey allTypesPrimaryKey = new AllTypesPrimaryKey();
            allTypesPrimaryKey.setColumnLong(i);
            pkObjects.add(allTypesPrimaryKey);
        }
    }

    @AfterExperiment
    public void after() {
        realm.close();
    }

    @Benchmark
    public void insertNoPrimaryKey(long reps) {
        AllTypes allTypes = new AllTypes();
        realm.beginTransaction();
        for (long i = 0; i < reps; i++) {
            realm.insert(allTypes);
        }
        realm.commitTransaction();
    }

    @Benchmark
    public void insertNoPrimaryKeyList(long reps) {
        realm.beginTransaction();
        for (long i = 0; i < reps; i++) {
            realm.insert(noPkObjects);
        }
        realm.commitTransaction();
    }

    @Benchmark
    public void insertWithPrimaryKey(long reps) {
        AllTypesPrimaryKey allTypesPrimaryKey = new AllTypesPrimaryKey();
        realm.beginTransaction();
        for (long i = 0; i < reps; i++) {
            allTypesPrimaryKey.setColumnLong(i);
            realm.insertOrUpdate(allTypesPrimaryKey);
        }
        realm.commitTransaction();
    }

    @Benchmark
    public void insertOrUpdateWithPrimaryKeyList(long reps) {
        realm.beginTransaction();
        for (long i = 0; i < reps; i++) {
            realm.insertOrUpdate(pkObjects);
        }
        realm.commitTransaction();
    }
}
