package io.realm.typed;

import io.realm.TableOrView;
import io.realm.TableQuery;

/**
 * Type of the fields that represent a nested table column in the generated XyzQuery
 * class for the Xyz entity.
 */
public class TableQueryColumn<Cursor, View, Query, Subtable> extends AbstractColumn<Subtable, Cursor, View, Query> {

    protected Subtable subtable;
    protected final Class<Subtable> subtableClass;

    public TableQueryColumn(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView, TableQuery query, int index, String name,
            Class<Subtable> subtableClass) {
        super(types, tableOrView, query, index, name);
        this.subtableClass = subtableClass;
    }

    @Override
    public String getReadableValue() {
        return "subtable";
    }

/* Not supported yet. The query returned must be specific to the subtable (to provide
 * access to the subtable members.
 * 
    public Query subtable() {
        return query(getQuery().subtable(columnIndex));
    }
*/
}
