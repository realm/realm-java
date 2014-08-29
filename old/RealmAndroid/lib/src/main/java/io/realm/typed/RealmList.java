package io.realm.typed;

import java.util.List;

public interface RealmList<E extends RealmObject> extends List<E> {


    void move(int oldPos, int newPos);

    /**
     *
     * @param rowIndex      The objects index in the list
     * @return              An object of type E, which is backed by Realm
     */
    @Override
    E get(int rowIndex);

    /**
     * Gets the first object in this list
     * @return              An object of type E, which is backed by Realm
     */
    E first();

    /**
     * Gets the last object in this list
     * @return              An object of type E, which is backed by Realm
     */
    E last();

    /**
     *
     * @return              The number of elements in this RealmList
     */
    @Override
    int size();

    /**
     * Returns a RealmQuery, used to filter this RealmList
     *
     * @return              A RealmQuery to filter the list
     */
    RealmQuery<E> where();

}
