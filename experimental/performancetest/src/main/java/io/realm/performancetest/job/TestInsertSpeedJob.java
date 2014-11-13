package io.realm.performancetest.job;

import android.content.Context;

import java.util.Date;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.entities.AllTypes;

public class TestInsertSpeedJob extends AbstractMeasurementJob {

    @Inject Context context;

    private final int noToInsert;

    private Realm realm;

    public TestInsertSpeedJob(String description, int noToInsert) {
        super(description);
        this.noToInsert = noToInsert;
    }

    @Override
    void startTest() {
        realm = Realm.getInstance(context);
    }

    @Override
    void runTest() {
        realm.beginTransaction();
        for (int i = 0; i < noToInsert; i++) {
            AllTypes allTypes = realm.createObject(AllTypes.class);
            allTypes.setColumnBoolean((i % 2) == 0);
            allTypes.setColumnBinary(new byte[]{1, 2, 3});
            allTypes.setColumnDate(new Date((long) i));
            allTypes.setColumnDouble(3.1415 + i);
            allTypes.setColumnFloat(1.234567f + i);
            allTypes.setColumnString("test data " + i);
            allTypes.setColumnLong(i);
        }
        realm.commitTransaction();
    }

    @Override
    void stopTest() {
        Realm.deleteRealmFile(context);
    }
}
