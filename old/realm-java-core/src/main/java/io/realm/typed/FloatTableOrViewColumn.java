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
import io.realm.TableQuery;

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

    public Cursor findFirst(float value) {
        return cursor(tableOrView.findFirstFloat(columnIndex, value));
    }

    public View findAll(float value) {
        return view(tableOrView.findAllFloat(columnIndex, value));
    }

}
