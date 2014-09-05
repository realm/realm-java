/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.typed;

import java.util.Iterator;

import io.realm.TableOrView;

/**
 * Super-type of the generated XyzTable and XyzView classes for the Xyz entity,
 * having common operations for both table and view.
 */
public abstract class AbstractTableOrView<Cursor, View, Query> implements Iterable<Cursor> {

    protected final EntityTypes<?, View, Cursor, Query> types;
    protected final TableOrView tableOrView;

    public AbstractTableOrView(EntityTypes<?, View, Cursor, Query> types, TableOrView tableOrView) {
        this.types = types;
        this.tableOrView = tableOrView;
    }

    public long size() {
        return tableOrView.size();
    }

    public boolean isEmpty() {
        return tableOrView.isEmpty();
    }

    public void clear() {
        tableOrView.clear();
    }

    public void remove(long rowIndex) {
        tableOrView.remove(rowIndex);
    }

    public void removeLast() {
        tableOrView.removeLast();
    }

    /*
     * TODO: public View range(long from, long to) { throw new
     * UnsupportedOperationException(); }
     */

    /**
     * This method is deprecated, use {@link #get(long)} instead.
     */
    @Deprecated
    public Cursor at(long position) {
        return cursor(position);
    }

    public Cursor get(long position) {
        return cursor(position);
    }

    public Cursor first() {
        return cursor(0);
    }

    public Cursor last() {
        return cursor(size() - 1);
    }

    protected Cursor cursor(long position) {
        return AbstractCursor.createCursor(types.getCursorClass(), tableOrView, position);
    }

    @Override
    public Iterator<Cursor> iterator() {
        return new TableOrViewIterator<Cursor>(this);
    }

    public abstract String getName();

    public String toJson() {
        return tableOrView.toJson();
    }

    public String toString() {
        return toString(500);
    }

    public String toString(long maxRows) {
        return getName() + ":\n" + tableOrView.toString(maxRows);
    }
}
