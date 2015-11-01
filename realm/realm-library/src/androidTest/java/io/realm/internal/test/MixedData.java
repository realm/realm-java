package io.realm.internal.test;

import io.realm.internal.ColumnType;

public class MixedData {

    public ColumnType type;

    public Object value;

    public MixedData(ColumnType type, Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "MixedData [type=" + type + ", value=" + value + "]";
    }

}
