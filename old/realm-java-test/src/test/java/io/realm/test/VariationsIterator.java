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

package io.realm.test;

import java.util.Iterator;
import java.util.List;

public class VariationsIterator<T> implements Iterator<Object[]> {

    private final List<List<?>> lists;

    private int[] sizes;
    private int[] indexes;

    private boolean hasNext = true;

    public VariationsIterator(List<List<?>> lists) {
        this.lists = lists;

        sizes = new int[lists.size()];
        indexes = new int[lists.size()];

        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).isEmpty()) {
                throw new RuntimeException("Each list must contain at least 1 item!");
            }

            sizes[i] = lists.get(i).size();
            indexes[i] = 0;
        }
    }

    public Object[] next() {
        Object[] result = new Object[lists.size()];

        for (int i = 0; i < lists.size(); i++) {
            result[i] = lists.get(i).get(indexes[i]);
        }

        increase();
        return result;
    }

    public boolean hasNext() {
        return hasNext;
    }

    private void increase() {
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = (indexes[i] + 1) % sizes[i];
            if (indexes[i] > 0) {
                hasNext = true;
                return;
            }
        }
        hasNext = false;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
