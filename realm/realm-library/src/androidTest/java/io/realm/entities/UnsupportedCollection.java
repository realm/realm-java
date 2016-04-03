package io.realm.entities;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import io.realm.OrderedRealmCollection;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class UnsupportedCollection<E extends RealmObject> implements OrderedRealmCollection<E> {

    @Override
    public E first() {
        return null;
    }

    @Override
    public E last() {
        return null;
    }

    @Override
    public RealmResults<E> sort(final String fieldName) {
        return null;
    }

    @Override
    public RealmResults<E> sort(final String fieldName, final Sort sortOrder) {
        return null;
    }

    @Override
    public RealmResults<E> sort(final String fieldName1, final Sort sortOrder1, final String fieldName2, final Sort sortOrder2) {
        return null;
    }

    @Override
    public RealmResults<E> sort(final String[] fieldNames, final Sort[] sortOrders) {
        return null;
    }

    @Override
    public void deleteFromRealm(final int location) {

    }

    @Override
    public boolean deleteFirstFromRealm() {
        return false;
    }

    @Override
    public boolean deleteLastFromRealm() {
        return false;
    }

    @Override
    public void add(final int location, final E object) {

    }

    @Override
    public boolean add(final E object) {
        return false;
    }

    @Override
    public boolean addAll(final int location, final Collection<? extends E> collection) {
        return false;
    }

    @Override
    public boolean addAll(final Collection<? extends E> collection) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean contains(final Object object) {
        return false;
    }

    @Override
    public boolean containsAll(final Collection<?> collection) {
        return false;
    }

    @Override
    public E get(final int location) {
        return null;
    }

    @Override
    public int indexOf(final Object object) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public int lastIndexOf(final Object object) {
        return 0;
    }

    @Override
    public ListIterator<E> listIterator() {
        return null;
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator(final int location) {
        return null;
    }

    @Override
    public E remove(final int location) {
        return null;
    }

    @Override
    public boolean remove(final Object object) {
        return false;
    }

    @Override
    public boolean removeAll(final Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(final Collection<?> collection) {
        return false;
    }

    @Override
    public E set(final int location, final E object) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @NonNull
    @Override
    public List<E> subList(final int start, final int end) {
        return null;
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NonNull
    @Override
    public <T> T[] toArray(final T[] array) {
        return null;
    }

    @Override
    public RealmQuery<E> where() {
        return null;
    }

    @Override
    public Number min(final String fieldName) {
        return null;
    }

    @Override
    public Number max(final String fieldName) {
        return null;
    }

    @Override
    public Number sum(final String fieldName) {
        return null;
    }

    @Override
    public double average(final String fieldName) {
        return 0;
    }

    @Override
    public Date maxDate(final String fieldName) {
        return null;
    }

    @Override
    public Date minDate(final String fieldName) {
        return null;
    }

    @Override
    public boolean deleteAllFromRealm() {
        return false;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public boolean load() {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }
}
