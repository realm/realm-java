package io.realm.examples.cursor.models;

import java.util.concurrent.atomic.AtomicLong;
import io.realm.Realm;

/**
 * This factory class implements auto-incremented keys for the Score class.
 */
public class PrimaryKeyFactory {

    private static AtomicLong maxScoreId;

    public static void initialize(Realm realm) {
        maxScoreId = new AtomicLong(realm.where(Score.class).maximumInt(Score.FIELD_ID));
    }

    /**
     * Creates the next primary key value. This should only be called from inside a write transaction to avoid
     * subtle bugs where objects are created outside a write transaction and then inserted in a different order.
     */
    public long nextScoreId() {
        return maxScoreId.incrementAndGet();
    }
}
