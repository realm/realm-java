package io.realm.typed;

import io.realm.Table;
import io.realm.TableOrView;
import io.realm.TableQuery;
import io.realm.TableView;

/**
 * Super-type of the generated XyzQuery classes for the Xyz entity, having
 * common query operations for all entities.
 */
public abstract class AbstractQuery<Query, Cursor, View extends AbstractView<Cursor, View, ?>> {

    private Long currPos = null;

    private final TableQuery query;
    private final Table table; // TODO ??? needed?
    private final EntityTypes<?, View, Cursor, Query> types;

    public AbstractQuery(EntityTypes<?, View, Cursor, Query> types, Table table, TableQuery query) {
        this.types = types;
        this.table = table;
        this.query = query;
    }

    public long count() {
        return query.count();
    }

    public long count(long start, long end, long limit) {
        return query.count(start, end, limit);
    }

    public long remove(long start, long end) {
        return query.remove(start, end);
    }

    public long remove() {
        return query.remove();
    }

    public View findAll() {
        return view(query.findAll());
    }

    public Cursor findFirst() {
        currPos = null;
        return findNext();
    }

    public Cursor findFrom(long start) {
        long resIndex = query.find(start);
        if (resIndex == -1) {
            return null; // no match
        }
        else {
            return cursor(table, resIndex);
        }
    }

    public Cursor findNext() {
        if (currPos == null) {
            // first time
            currPos = query.find();
        } else {
            // next times
            if (currPos < Long.MAX_VALUE) {
                currPos = query.find(currPos+1);
            } else {
                return null;
            }
        }

        // if there is a result - return it, or return null otherwise
        if (currPos >= 0 && currPos < table.size()) {
            return cursor(table, currPos);
        } else {
            currPos = Long.MAX_VALUE;
            return null;
        }
    }

    public Cursor findLast() {
        // TODO: find more efficient way to search
        TableView viewBase = query.findAll();
        long count = viewBase.size();
        if (count > 0) {
            return cursor(viewBase, count - 1);
        } else {
            return null;
        }
    }

    /**
     * public Cursor findUnique() { TableViewBase viewBase =
     * query.findAll(table, 0, table.size(), 2); switch (viewBase.size()) { case
     * 0: throw new
     * IllegalStateException("Expected exactly one result, but found none!");
     * case 1: return cursor(viewBase, 0); default: throw new
     * IllegalStateException("Expected exactly one result, but found more!"); }
     * }
     */

    public Query or() {
        query.or();
        return newQuery(query);
    }

    public Query group() {
        query.group();
        return newQuery(query);
    }

    public Query endGroup() {
        query.endGroup();
        return newQuery(query);
    }

    // FIXME: columnIndex is low-level
    public Query subtable(long columnIndex) {
        query.subtable(columnIndex);
        return newQuery(query);
    }

    public Query endSubtable() {
        query.endSubtable();
        return newQuery(query);
    }

    private Query newQuery(TableQuery q) {
        return createQuery(types.getQueryClass(), table, q);
    }

    public long clear() {
        View results = findAll();       // FIXME: Too expensive clear.
        long count = results.size();
        results.clear();
        return count;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    protected View view(TableView viewBase) {
        return AbstractView.createView(types.getViewClass(), viewBase);
    }

    protected Cursor cursor(TableOrView tableOrView, long position) {
        return AbstractCursor.createCursor(types.getCursorClass(), tableOrView, position);
    }

    protected static <Q> Q createQuery(Class<Q> queryClass, Table tableBase, TableQuery tableQuery) {
        try {
            return queryClass.getConstructor(Table.class, TableQuery.class).newInstance(tableBase, tableQuery);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create a query!", e);
        }
    }
}
