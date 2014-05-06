package com.realm;


public interface TableSchema {

    TableSchema getSubtableSchema(long columnIndex);

    long addColumn(ColumnType type, String name);

    void removeColumn(long columnIndex);

    void renameColumn(long columnIndex, String newName);

    /*
    // FIXME the column information classes should be here as well.
    // There is currently no path based implementation in core, so we should consider adding them with Spec, or wait for a core implementation.

    long getColumnCount();

    String getColumnName(long columnIndex);

    long getColumnIndex(String name);

    ColumnType getColumnType(long columnIndex);
    */
}
