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
import io.realm.entities.AllTypes;

@RunWith(SpannerRunner.class)
public class RealmObjectWriteBenchmarks {

    @BenchmarkConfiguration
    public SpannerConfig configuration = BenchmarkConfig.getConfiguration(this.getClass().getCanonicalName());

    private Realm realm;
    private AllTypes writeObject;

    @BeforeExperiment
    public void before() {
        RealmConfiguration config = new RealmConfiguration.Builder(InstrumentationRegistry.getTargetContext()).build();
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
    public void writeString(long reps) {
        for (long i = 0; i < reps; i++) {
            writeObject.setColumnString("Foo");
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
