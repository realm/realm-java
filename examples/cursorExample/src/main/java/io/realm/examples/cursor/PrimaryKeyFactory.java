package io.realm.examples.cursor;

import java.util.concurrent.atomic.AtomicLong;

import io.realm.Realm;
import io.realm.examples.cursor.models.Score;

/**
 * This factory class implements a method for getting auto-incremented key values for the Score class.
 */
public class PrimaryKeyFactory {

    private static AtomicLong maxScoreId;

    public static void initialize(Realm realm) {
        maxScoreId = new AtomicLong(Math.max(0, realm.where(Score.class).maximumInt(Score.FIELD_ID)));
    }

    /**
     * Creates the next primary key value. This should only be called from inside a write transaction to avoid subtle
     * bugs where two objects are created outside a write transaction and then inserted in a different order.
     */
    public static long nextScoreId() {
        return maxScoreId.incrementAndGet();
    }
}
