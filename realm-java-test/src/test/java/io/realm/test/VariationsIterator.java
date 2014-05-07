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
