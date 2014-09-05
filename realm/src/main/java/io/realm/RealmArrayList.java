package io.realm;

import java.util.ArrayList;

public class RealmArrayList<E extends RealmObject> extends ArrayList<E> implements RealmList<E> {


    @Override
    public void move(int oldPos, int newPos) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E first() {
        return get(0);
    }

    @Override
    public E last() {
        return get(size()-1);
    }

    @Override
    public RealmQuery<E> where() {
        return null;
    }

}
