package io.realm;

import android.support.test.InstrumentationRegistry;

import org.junit.runner.RunWith;

import dk.ilios.spanner.AfterExperiment;
import dk.ilios.spanner.BeforeExperiment;
import dk.ilios.spanner.Benchmark;
import dk.ilios.spanner.junit.SpannerRunner;
import io.realm.entities.AllTypes;

@RunWith(SpannerRunner.class)
public class ReadBenchmarks {

    private Realm realm;
    private AllTypes realmObject;
    private AllTypes javaObject;

    @BeforeExperiment
    public void before() {
        RealmConfiguration config = TestHelper.createConfiguration(InstrumentationRegistry.getTargetContext(), "bench");
        Realm.deleteRealm(config);
        realm = Realm.getInstance(config);
        realm.beginTransaction();
        realm.clear(io.realm.entities.AllTypes.class);
        realmObject = realm.createObject(io.realm.entities.AllTypes.class);
        realmObject.setColumnLong(1);
        realmObject.setColumnString("foo");
        realmObject.setColumnBoolean(true);

        javaObject = new AllTypes();
        javaObject.setColumnLong(1);
        javaObject.setColumnString("foo");
        javaObject.setColumnBoolean(true);
    }

    @AfterExperiment
    public void after() {
        realm.close();
    }


    @Benchmark
    public void readRealmString(int reps) {
        String str;
        for (int i = 0; i < reps; i++) {
            str = realmObject.getColumnString();
        }
    }

    @Benchmark
    public void readRealmInt(int reps) {
        long value;
        for (int i = 0; i < reps; i++) {
            value = realmObject.getColumnLong();
        }
    }

    @Benchmark
    public void readRealmBoolean(int reps) {
        boolean bool;
        for (int i = 0; i < reps; i++) {
            bool = realmObject.isColumnBoolean();
        }
    }
}
