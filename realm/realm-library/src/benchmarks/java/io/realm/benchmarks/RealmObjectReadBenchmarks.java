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
public class RealmObjectReadBenchmarks {

    @BenchmarkConfiguration
    public SpannerConfig configuration = BenchmarkConfig.getConfiguration(this.getClass().getCanonicalName());

    private Realm realm;
    private AllTypes readObject;

    @BeforeExperiment
    public void before() {
        RealmConfiguration config = new RealmConfiguration.Builder(InstrumentationRegistry.getTargetContext()).build();
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
