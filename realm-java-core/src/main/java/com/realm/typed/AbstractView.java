package com.realm.typed;

import com.realm.TableView;

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

    protected static <V> V createView(Class<V> viewClass, TableView viewBase) {
        try {
            return viewClass.getConstructor(TableView.class).newInstance(viewBase);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create a view!", e);
        }
    }
}
