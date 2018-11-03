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

package io.realm.benchmarks;

import android.support.test.InstrumentationRegistry;

import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.ilios.spanner.AfterExperiment;
import dk.ilios.spanner.BeforeExperiment;
import dk.ilios.spanner.Benchmark;
import dk.ilios.spanner.BenchmarkConfiguration;
import dk.ilios.spanner.SpannerConfig;
import dk.ilios.spanner.junit.SpannerRunner;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.benchmarks.config.BenchmarkConfig;
import io.realm.benchmarks.entities.AllTypes;
import io.realm.benchmarks.entities.AllTypesPrimaryKey;


@RunWith(SpannerRunner.class)
public class CopyToRealmBenchmarks {

    @BenchmarkConfiguration
    public SpannerConfig configuration = BenchmarkConfig.getConfiguration(this.getClass().getCanonicalName());

    private Realm realm;
    private static final int COLLECTION_SIZE = 100;
    private List<AllTypes> noPkObjects = new ArrayList<>(COLLECTION_SIZE);
    private List<AllTypesPrimaryKey> pkObjects = new ArrayList<>(COLLECTION_SIZE);
    private ArrayList<AllTypesPrimaryKey> complextTestObjects;
    private ArrayList<AllTypes> simpleTestObjects;

    @BeforeExperiment
    public void before() {
        Realm.init(InstrumentationRegistry.getTargetContext());
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(config);

        // Create test data
        complextTestObjects = new ArrayList<>();
        for (int i = 0; i < COLLECTION_SIZE; i++) {
            AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
            obj.setColumnString("obj" + i);
            obj.setColumnLong(i);
            obj.setColumnFloat(1.23F);
            obj.setColumnDouble(1.234);
            obj.setColumnBoolean(true);
            obj.setColumnDate(new Date(1000));
            obj.setColumnBinary(new byte[] {1,2,3});
            obj.setColumnRealmObject(obj);
            obj.setColumnRealmList(new RealmList<>(obj, obj, obj));
            obj.setColumnBooleanList(new RealmList<>(true, false, true));
            obj.setColumnStringList(new RealmList<>("foo", "bar", "baz"));
            obj.setColumnBinaryList(new RealmList<>(new byte[]{0,1,2},new byte[]{2,3,4},new byte[]{4,5,6}));
            obj.setColumnByteList(new RealmList<>((byte)1,(byte)2,(byte)3));
            obj.setColumnShortList(new RealmList<>((short)1,(short)2,(short)3));
            obj.setColumnIntegerList(new RealmList<>(1,2,3));
            obj.setColumnLongList(new RealmList<>(1L,2L,3L));
            obj.setColumnFloatList(new RealmList<>(1.1F, 1.2F, 1.3F));
            obj.setColumnDoubleList(new RealmList<>(1.111, 1.222, 1.333));
            obj.setColumnDateList(new RealmList<>(new Date(1000), new Date(2000), new Date(3000)));
            complextTestObjects.add(obj);
        }

        simpleTestObjects = new ArrayList<>();
        for (int i = 0; i < COLLECTION_SIZE; i++) {
            AllTypes obj = new AllTypes();
            obj.setColumnString("obj" + i);
            obj.setColumnLong(i);
            obj.setColumnFloat(1.23F);
            obj.setColumnDouble(1.234);
            obj.setColumnBoolean(true);
            obj.setColumnDate(new Date(1000));
            obj.setColumnBinary(new byte[] {1,2,3});
            simpleTestObjects.add(obj);
        }

        // Setup Realm before test
        realm = Realm.getInstance(config);
        realm.beginTransaction();
    }

    @AfterExperiment
    public void after() {
        realm.cancelTransaction();
        realm.close();
    }

    @Benchmark
    public void copyToRealm_complexObjects(long reps) {
        for (long i = 0; i < reps; i++) {
            realm.copyToRealmOrUpdate(complextTestObjects);
        }
    }

    @Benchmark
    public void copyToRealm_simpleObjects(long reps) {
        for (long i = 0; i < reps; i++) {
            realm.copyToRealm(simpleTestObjects);
        }
    }

}
