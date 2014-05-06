package com.realm.typed;

import com.realm.Table;

/**
 * Super-type of the generated XyzTable classes for the Xyz nested table.
 */
public abstract class AbstractSubtable<Cursor, View, Query> extends AbstractTable<Cursor, View, Query> {

    public AbstractSubtable(EntityTypes<?, View, Cursor, Query> types, Table subtable) {
        super(types, subtable);
    }

    protected static <S> S createSubtable(Class<S> subtableClass, Table subtableBase) {
        try {
            S subtable = subtableClass.getConstructor(Table.class).newInstance(subtableBase);
            return subtable;
        } catch (Exception e) {
            throw new RuntimeException("Cannot create subtable instance!", e);
        }
    }

}
