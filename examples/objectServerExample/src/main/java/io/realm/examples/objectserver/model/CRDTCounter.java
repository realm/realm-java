package io.realm.examples.objectserver.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Counter class that is eventually consistent. Two devices can simultaneous increment this and eventually reach
 * the same value.
 *
 * @see <href ref="https://en.wikipedia.org/wiki/Conflict-free_replicated_data_type">Conflict Free Replicated Data Structures</href>
 */
public class CRDTCounter extends RealmObject {

    @PrimaryKey
    private long id;
    private RealmList<CounterOperation> operations;

    public CRDTCounter() {
        // Required by Realm
    }

    public CRDTCounter(long id) {
        this.id = id;
    }

    public long getCount() {
        return operations.where().sum("adjustment").longValue();
    }

    public void add(long val) {
        operations.add(new CounterOperation(val));
    }
}
