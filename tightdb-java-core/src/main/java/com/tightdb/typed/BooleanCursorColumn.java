package com.tightdb.typed;

/**
 * Type of the fields that represent a boolean column in the generated XyzRow class
 * for the Xyz entity.
 */
public class BooleanCursorColumn<Cursor, View, Query> extends AbstractColumn<Boolean, Cursor, View, Query> {

    public BooleanCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
        super(types, cursor, index, name);
    }

    @Override
    public Boolean get() {
        return cursor.tableOrView.getBoolean(columnIndex, cursor.getPosition());
    }

    @Override
    public void set(Boolean value) {
        cursor.tableOrView.setBoolean(columnIndex, cursor.getPosition(), value);
    }

}
