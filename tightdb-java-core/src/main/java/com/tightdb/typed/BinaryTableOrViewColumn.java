package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

/**
 * Super-type of the fields that represent a binary column in the generated
 * XyzView and XyzTable classes for the Xyz entity.
 */
public class BinaryTableOrViewColumn<Cursor, View, Query> extends BinaryQueryColumn<Cursor, View, Query> implements TableOrViewColumn<byte[]> {

    public BinaryTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, int index, String name) {
        this(types, tableOrView, null, index, name);
    }

    public BinaryTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    @Override
    public byte[][] getAll() {
        long count = tableOrView.size();

        byte[][] values = new byte[(int)count][];

        for (int i = 0; i < count; i++) {
            values[i] = tableOrView.getBinaryByteArray(columnIndex, i);
        }

        return values;
    }

    @Override
    public void setAll(byte[] value) {
        long count = tableOrView.size();
        for (int i = 0; i < count; i++) {
            tableOrView.setBinaryByteArray(columnIndex, i, value);
        }
    }

}
