package com.tightdb.typed;

import com.tightdb.TableOrView;
import com.tightdb.TableQuery;

/**
 * Super-type of the fields that represent a float column in the generated
 * XyzView and XyzTable classes for the Xyz entity.
 */
public class FloatTableOrViewColumn<Cursor, View, Query> extends FloatQueryColumn<Cursor, View, Query> implements
        TableOrViewColumn<Float> {

    public FloatTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, int index,
            String name) {
        this(types, tableOrView, null, index, name);
    }

    public FloatTableOrViewColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query,
            int index, String name) {
        super(types, tableOrView, query, index, name);
    }

    @Override
    public double sum() {
        return tableOrView.sumFloat(columnIndex);
    }

    @Override
    public float maximum() {
        return tableOrView.maximumFloat(columnIndex);
    }

    @Override
    public float minimum() {
        return tableOrView.minimumFloat(columnIndex);
    }

    @Override
    public double average() {
        return tableOrView.averageFloat(columnIndex);
    }

    /*
     * public void setIndex() { tableOrView.setIndex(columnIndex); }
     * 
     * public boolean hasIndex() { return tableOrView.hasIndex(columnIndex); }
     */

    @Override
    public Float[] getAll() {
        long count = tableOrView.size();
        Float[] values = new Float[(int) count];
        for (int i = 0; i < count; i++) {
            values[i] = tableOrView.getFloat(columnIndex, i);
        }
        return values;
    }

    @Override
    public void setAll(Float value) {
        long count = tableOrView.size();
        for (int i = 0; i < count; i++) {
            tableOrView.setFloat(columnIndex, i, value);
        }
    }

    public void setAll(float value) {
        setAll(new Float(value));
    }

    // public void addFloat(float value) {
    // tableOrView.addFloat(columnIndex, value);
    // }

    public Cursor findFirst(long value) {
        return cursor(tableOrView.findFirstFloat(columnIndex, value));
    }

    public View findAll(long value) {
        return view(tableOrView.findAllFloat(columnIndex, value));
    }

}
