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

import io.realm.TableOrView;

/**
 * Super-type of the generated XyzRow classes for the Xyz entity, having
 * common row operations for all entities.
 */
public abstract class AbstractCursor<Cursor> {

    protected final long position;
    protected final EntityTypes<?, ?, Cursor, ?> types;
    protected final TableOrView tableOrView;

    public AbstractCursor(EntityTypes<?, ?, Cursor, ?> types,
            TableOrView tableOrView, long position) {
        this.types = types;
        this.tableOrView = tableOrView;
        this.position = position;
    }

    public Cursor next() {
        return after(1);
    }

    public Cursor previous() {
        return before(1);
    }

    public Cursor before(long delta) {
        long pos = position - delta;
        if (isValidIndex(pos)) {
            return createCursor(types.getCursorClass(), tableOrView, pos);
        } else {
            return null;
        }
    }

    public Cursor after(long delta) {
        long pos = position + delta;
        if (isValidIndex(pos)) {
            return createCursor(types.getCursorClass(), tableOrView, pos);
        } else {
            return null;
        }
    }

    private boolean isValidIndex(long position) {
        return 0 <= position && position < tableOrView.size();
    }

    public long getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return tableOrView.rowToString(position);
    }
/*
     public String toString() {
        StringBuffer sb = new StringBuffer();
        AbstractColumn<?, ?, ?, ?>[] columns = columns();

        for (int i = 0; i < columns.length; i++) {
            AbstractColumn<?, ?, ?, ?> column = columns[i];
            sb.append(String.format("%s=%s", column.getName(),
                    column.getReadableValue()));
            if (i < columns.length - 1) {
                sb.append(", ");
            }
        }

        return types.getCursorClass().getSimpleName() + " {" + sb + "}";
    }
*/

    public AbstractColumn<?, ?, ?, ?>[] columns() {
        return null;
    }

    protected AbstractColumn<?, ?, ?, ?>[] getColumnsArray(
            AbstractColumn<?, ?, ?, ?>... columns) {
        return columns;
    }

    protected static <C> C createCursor(Class<C> cursorClass,
            TableOrView targetTableOrView, long position) {
        try {
            return cursorClass.getDeclaredConstructor(TableOrView.class,
                    long.class).newInstance(targetTableOrView, position);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate a cursor!", e);
        }
    }

}
