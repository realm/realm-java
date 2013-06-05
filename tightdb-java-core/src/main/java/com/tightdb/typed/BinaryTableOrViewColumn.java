package com.tightdb.typed;

import java.nio.ByteBuffer;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

/**
 * Super-type of the fields that represent a binary column in the generated
 * XyzView and XyzTable classes for the Xyz entity.
 */
public class BinaryTableOrViewColumn<Cursor, View, Query> extends BinaryQueryColumn<Cursor, View, Query> implements TableOrViewColumn<ByteBuffer> {

    public BinaryTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, int index, String name) {
        this(types, tableOrView, null, index, name);
    }

    public BinaryTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    @Override
    public ByteBuffer[] getAll() {
        long count = tableOrView.size();
        ByteBuffer[] values = new ByteBuffer[(int) count];
        for (int i = 0; i < count; i++) {
            values[i] = tableOrView.getBinaryByteBuffer(columnIndex, i);
        }
        return values;
    }

    @Override
    public void setAll(ByteBuffer value) {
        long count = tableOrView.size();
        for (int i = 0; i < count; i++) {
            tableOrView.setBinaryByteBuffer(columnIndex, i, value);
        }
    }

}
