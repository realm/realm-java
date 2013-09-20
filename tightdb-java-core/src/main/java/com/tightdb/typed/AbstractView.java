package com.tightdb.typed;

import com.tightdb.ColumnType;
import com.tightdb.TableView;

/**
 * Super-type of the generated XyzView classes for the Xyz entity, having
 * common view operations for all entities.
 */
public abstract class AbstractView<Cursor, View, Query> extends AbstractTableOrView<Cursor, View, Query> {

    protected final TableView viewBase;

    public AbstractView(EntityTypes<?, View, Cursor, Query> types, TableView viewBase) {
        super(types, viewBase);
        this.viewBase = viewBase;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return the number of columns.
     */
    public long getColumnCount() {
        return viewBase.getColumnCount();
    }

    /**
     * Returns the name of a column identified by columnIndex. Notice that the
     * index is zero based.
     *
     * @param columnIndex
     *            the column index
     * @return the name of the column
     */
    public String getColumnName(long columnIndex) {
        return viewBase.getColumnName(columnIndex);
    }

    /**
     * Returns the 0-based index of a column based on the name.
     *
     * @param columnName
     *            the column name
     * @return the index, -1 if not found
     */
    public long getColumnIndex(String columnName) {
        return viewBase.getColumnIndex(columnName);
    }

    /**
     * Get the type of a column identified by the columnIdex.
     *
     * @param columnIndex
     *            index of the column.
     * @return Type of the particular column.
     */
    public ColumnType getColumnType(long columnIndex) {
        return viewBase.getColumnType(columnIndex);
    }

    protected static <V> V createView(Class<V> viewClass, TableView viewBase) {
        try {
            return viewClass.getConstructor(TableView.class).newInstance(viewBase);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create a view!", e);
        }
    }
}
