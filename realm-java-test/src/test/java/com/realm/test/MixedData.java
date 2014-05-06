package com.realm.test;

import com.realm.ColumnType;

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
