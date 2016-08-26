package io.realm.examples.objectserver.model;

import io.realm.RealmObject;

public class CounterOperation extends RealmObject {
    public long adjustment;
    public CounterOperation() {};
    public CounterOperation(long adjustment) {
        this.adjustment = adjustment;
    }
}
