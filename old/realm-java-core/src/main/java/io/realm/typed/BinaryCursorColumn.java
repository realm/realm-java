package io.realm.typed;


/**
 * Type of the fields that represent a binary column in the generated XyzRow class
 * for the Xyz entity.
 */
public class BinaryCursorColumn<Cursor, View, Query> extends AbstractColumn<byte[], Cursor, View, Query> {

    public BinaryCursorColumn(EntityTypes<?, View, Cursor, Query> types, AbstractCursor<Cursor> cursor, int index, String name) {
        super(types, cursor, index, name);
    }

    @Override
    public byte[] get() {
        return cursor.tableOrView.getBinaryByteArray(columnIndex, cursor.getPosition());
    }

    @Override
    public void set(byte[] value) {
        cursor.tableOrView.setBinaryByteArray(columnIndex, cursor.getPosition(), value);
    }

    @Override
    public String getReadableValue() {
        return "{binary}";
    }

}
