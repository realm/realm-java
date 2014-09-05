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
import java.util.NoSuchElementException;

/**
 * Iterator for the generated XyzView and XyzTable classes for the Xyz entity.
 */
public class TableOrViewIterator<T> implements Iterator<T> {

    private final AbstractTableOrView<T, ?, ?> tableOrView;
    private long endIndex = 0;
    private long index = 0;

    public TableOrViewIterator(final AbstractTableOrView<T, ?, ?> tableOrView) {
        this.tableOrView = tableOrView;
        this.endIndex = tableOrView.size();
        this.index = 0;
    }

    public boolean hasNext() {
        return (index < endIndex);
    }

    public T next() {
        if (hasNext() == false) {
            throw new NoSuchElementException();
        }
    return tableOrView.get(index++);
    }

    public void remove() {
        throw new UnsupportedOperationException("The method remove() is currently not supported!");
    }

}
