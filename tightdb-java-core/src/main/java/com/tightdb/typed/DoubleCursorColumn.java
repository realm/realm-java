package com.tightdb.typed;

/**
 * Type of the fields that represent a double column in the generated XyzRow class
 * for the Xyz entity.
 */
public class DoubleCursorColumn<Cursor, View, Query> extends AbstractColumn<Double, Cursor, View, Query> {

    public DoubleCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index,
            String name) {
        super(types, cursor, index, name);
    }

    @Override
    public Double get() {
        return cursor.tableOrView.getDouble(columnIndex, cursor.getPosition());
    }

    @Override
    public void set(Double value) {
        cursor.tableOrView.setDouble(columnIndex, cursor.getPosition(), value);
    }

    public void set(double value) {
        set(Double.valueOf(value));
    }

}
