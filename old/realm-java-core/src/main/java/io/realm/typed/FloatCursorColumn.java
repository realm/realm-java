package io.realm.typed;

/**
 * Type of the fields that represent a float column in the generated XyzRow class
 * for the Xyz entity.
 */
public class FloatCursorColumn<Cursor, View, Query> extends AbstractColumn<Float, Cursor, View, Query> {

    public FloatCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index,
            String name) {
        super(types, cursor, index, name);
    }

    @Override
    public Float get() {
        return cursor.tableOrView.getFloat(columnIndex, cursor.getPosition());
    }

    @Override
    public void set(Float value) {
        cursor.tableOrView.setFloat(columnIndex, cursor.getPosition(), value);
    }

    public void set(float value) {
        set(Float.valueOf(value));
    }

}
