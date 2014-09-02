package io.realm;

import java.lang.reflect.Array;
import java.util.AbstractList;

import io.realm.internal.LinkView;

public class RealmLinkList<E extends RealmObject> extends AbstractList<E> implements RealmList<E> {

    private Class<E> clazz;
    private LinkView view;
    private Realm realm;

    public RealmLinkList(Class<E> clazz, LinkView view, Realm realm) {
        this.clazz = clazz;
        this.view = view;
        this.realm = realm;
    }




    @Override
    public void add(int location, E object) {
        if(object.realmGetRow() == null) {
            realm.add(object);
            view.add(object.realmAddedAtRowIndex);
        } else {
            view.add(object.realmGetRow().getIndex());
        }
    }

    @Override
    public E set(int location, E object) {
        if(object.realmGetRow() == null) {
            realm.add(object);
            view.set(location, object.realmAddedAtRowIndex);
            return realm.get((Class<E>)object.getClass(), object.realmAddedAtRowIndex);
        } else {
            view.set(location, object.realmGetRow().getIndex());
            return object;
        }
    }

    @Override
    public void move(int oldPos, int newPos) {
        view.move(oldPos, newPos);
    }

    @Override
    public void clear() {
        view.clear();
    }

    @Override
    public E remove(int location) {
        view.remove(location);
        return null;
    }

    @Override
    public E get(int i) {
        return realm.get(clazz, view.getTargetRowIndex(i));
    }

    @Override
    public E first() {
        if(!view.isEmpty()) {
            return get(0);
        }
        return null;
    }

    @Override
    public E last() {
        if(!view.isEmpty()) {
            return get(size()-1);
        }
        return null;
    }

    @Override
    public int size() {
        return ((Long)view.size()).intValue();
    }

    @Override
    public RealmQuery<E> where() {
        return null;
    }

    @Override
    public void addObject(E element) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public void addObjectFromArray(Array array) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public String JSONString() throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public void removeLastObject() throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public void replaceObjectAtIndexWithObject(int index, E newElement) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public void insertObjectAtIndex(E element, int index) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public int indexOfObjectWhere() throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public int indexOfObject(E element) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }
    @Override
    public double averageOfProperty(String propertyName) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public double sumOfProperty(String propertyName) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public int maxOfProperty(String propertyName) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public int minOfProperty(String propertyName) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

    @Override
    public RealmList<E> arraySortForProperty(String propertyName) throws NoSuchMethodException {
        throw new NoSuchMethodException();
    }

}
