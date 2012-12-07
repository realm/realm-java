package com.tightdb;

// Make sure numbers match with <tightdb/column_type.hpp>
// FIXME: Add a unit test that verifies the correct correspondance.

public enum ColumnType {
    ColumnTypeBool(1),
    ColumnTypeInt(0),
    ColumnTypeString(2),
    ColumnTypeBinary(4),
    ColumnTypeDate(7),
    ColumnTypeTable(5),
    ColumnTypeMixed(6);

    private ColumnType(int nativeValue)
    {
        this.nativeValue = nativeValue;
    }

    private final int nativeValue;

    static ColumnType fromNativeValue(int value)
    {
        if (0 <= value && value < byNativeValue.length) {
            ColumnType e = byNativeValue[value];
            if (e != null) return e;
        }
        throw new IllegalArgumentException("Bad native column type");
    }

    // Note that if this array is too small, an
    // IndexOutOfBoundsException will be thrown during class loading.
    private static ColumnType[] byNativeValue = new ColumnType[10];

    static {
        ColumnType[] columnTypes = values();
        for(int i=0; i<columnTypes.length; ++i) {
            int v = columnTypes[i].nativeValue;
            byNativeValue[v] = columnTypes[i];
        }
    }
}

