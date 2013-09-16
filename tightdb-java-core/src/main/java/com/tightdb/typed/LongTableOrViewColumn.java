package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

/**
 * Super-type of the fields that represent a long column in the generated
 * XyzView and XyzTable classes for the Xyz entity.
 */
public class LongTableOrViewColumn<Cursor, View, Query> extends
        LongQueryColumn<Cursor, View, Query> implements TableOrViewColumn<Long> {

    public LongTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types,
            TableOrView tableOrView, int index, String name) {
        this(types, tableOrView, null, index, name);
    }

    public LongTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types,
            TableOrView tableOrView, TableQuery query, int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    @Override
    public long sum() {
        return tableOrView.sum(columnIndex);
    }

    @Override
    public long maximum() {
        return tableOrView.maximum(columnIndex);
    }

    @Override
    public long minimum() {
        return tableOrView.minimum(columnIndex);
    }

    @Override
    public double average() {
        return tableOrView.average(columnIndex);
    }

    /*
        public void setIndex() {
            tableOrView.setIndex(columnIndex);
        }

        public boolean hasIndex() {
            return tableOrView.hasIndex(columnIndex);
        }
    */

    @Override
    public Long[] getAll() {
        long count = tableOrView.size();
        Long[] values = new Long[(int) count];
        for (int i = 0; i < count; i++) {
            values[i] = tableOrView.getLong(columnIndex, i);
        }
        return values;
    }

    @Override
    public void setAll(Long value) {
        long count = tableOrView.size();
        for (int i = 0; i < count; i++) {
            tableOrView.setLong(columnIndex, i, value);
        }
    }

    public void setAll(long value) {
        setAll(new Long(value));
    }

    public void adjust(long value) {
        tableOrView.adjust(columnIndex, value);
    }

    public Cursor findFirst(long value) {
        return cursor(tableOrView.findFirstLong(columnIndex, value));
    }

    public View findAll(long value) {
        return view(tableOrView.findAllLong(columnIndex, value));
    }

    public long lowerBound(long value) {
        return tableOrView.lowerBoundLong(columnIndex, value);
    }

    public long upperBound(long value) {
        return tableOrView.upperBoundLong(columnIndex, value);
    }
}
