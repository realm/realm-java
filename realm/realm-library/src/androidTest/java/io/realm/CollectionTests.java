/*
 * Copyright 2016 Realm Inc.
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

package io.realm;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.CyclicType;
import io.realm.entities.NonLatinFieldNames;
import io.realm.entities.NullTypes;

/**
 * Super class for all RealmCollection related tests.
 * This class only contains configuration and helper methods.
 */
public abstract class CollectionTests {

    protected final static long YEAR_MILLIS = TimeUnit.DAYS.toMillis(365);

    // Enumerates all known collection classes from the Realm API.
    protected enum CollectionClass {
        MANAGED_REALMLIST, UNMANAGED_REALMLIST, REALMRESULTS,
        REALMRESULTS_SNAPSHOT_RESULTS_BASE, REALMRESULTS_SNAPSHOT_LIST_BASE

    }

    // Enumerates all current supported collections that can be in unmanaged mode.
    protected enum UnManagedCollection {
        UNMANAGED_REALMLIST
    }

    // Enumerates all current supported collections that can be managed by Realm.
    protected enum ManagedCollection {
        MANAGED_REALMLIST, REALMRESULTS, REALMRESULTS_SNAPSHOT_RESULTS_BASE, REALMRESULTS_SNAPSHOT_LIST_BASE
    }

    // Enumerates all methods from the RealmCollection interface that depend on Realm API's.
    protected enum RealmCollectionMethod { WHERE, MIN, MAX, SUM, AVERAGE, MIN_DATE, MAX_DATE, DELETE_ALL_FROM_REALM, IS_VALID, IS_MANAGED
    }

    // Enumerates all methods from the Collection interface
    protected enum CollectionMethod {
        ADD_OBJECT, ADD_ALL_OBJECTS, CLEAR, CONTAINS, CONTAINS_ALL, EQUALS, HASHCODE, IS_EMPTY, ITERATOR, REMOVE_OBJECT,
        REMOVE_ALL, RETAIN_ALL, SIZE, TO_ARRAY, TO_ARRAY_INPUT
    }

    // Enumerates all methods on the List interface and OrderedRealmCollection interface that doesn't depend on Realm
    // API's.
    protected enum ListMethod {
        FIRST, LAST, ADD_INDEX, ADD_ALL_INDEX, GET_INDEX, INDEX_OF, LAST_INDEX_OF, LIST_ITERATOR, LIST_ITERATOR_INDEX, REMOVE_INDEX,
        SET, SUBLIST
    }

    // Enumerates all methods from the OrderedRealmCollection interface that depend on Realm API's.
    protected enum OrderedRealmCollectionMethod {
        DELETE_INDEX, DELETE_FIRST, DELETE_LAST, SORT, SORT_FIELD, SORT_2FIELDS, SORT_MULTI, CREATE_SNAPSHOT
    }

    // Enumerates all methods that can mutate a RealmCollection.
    protected enum CollectionMutatorMethod {
        DELETE_ALL, ADD_OBJECT, ADD_ALL_OBJECTS, CLEAR, REMOVE_OBJECT, REMOVE_ALL, RETAIN_ALL
    }

    // Enumerates all methods that can mutate a RealmOrderedCollection.
    protected enum OrderedCollectionMutatorMethod {
        DELETE_INDEX, DELETE_FIRST, DELETE_LAST, ADD_INDEX, ADD_ALL_INDEX, SET, REMOVE_INDEX
    }

    protected void populateRealm(Realm realm, int objects) {
        realm.beginTransaction();
        realm.delete(AllJavaTypes.class);
         realm.delete(NonLatinFieldNames.class);
        if (objects > 0) {
            for (int i = 0; i < objects; i++) {
                AllJavaTypes obj = realm.createObject(AllJavaTypes.class, i);
                fillObject(i, objects, obj);
                NonLatinFieldNames nonLatinFieldNames = realm.createObject(NonLatinFieldNames.class);
                nonLatinFieldNames.set델타(i);
                nonLatinFieldNames.setΔέλτα(i);
                // Sets the linked object to itself.
                obj.setFieldObject(obj);
            }

            // Adds all items to the RealmList on the first object.
            AllJavaTypes firstObj = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_ID, 0).findFirst();
            RealmResults<AllJavaTypes> listData = realm.where(AllJavaTypes.class).sort(AllJavaTypes.FIELD_ID, Sort.ASCENDING).findAll();
            RealmList<AllJavaTypes> list = firstObj.getFieldList();
            for (int i = 0; i < listData.size(); i++) {
                list.add(listData.get(i));
            }
        }
        realm.commitTransaction();
    }

    protected RealmList<AllJavaTypes> populateInMemoryList(int objects) {
        RealmList<AllJavaTypes> list = new RealmList<AllJavaTypes>();
        for (int i = 0; i < objects; i++) {
            AllJavaTypes obj = new AllJavaTypes();
            fillObject(i, objects, obj);
            list.add(obj);
        }
        return list;
    }

    private void fillObject(int index, int totalObjects, AllJavaTypes obj) {
        obj.setFieldLong(index);
        obj.setFieldInt(index);
        obj.setFieldBoolean(((index % 2) == 0));
        obj.setFieldBinary(new byte[]{1, 2, 3});
        obj.setFieldDate(new Date(YEAR_MILLIS * 20 * (index - totalObjects / 2)));
        obj.setFieldDouble(Math.PI + index);
        obj.setFieldFloat(1.234567f + index);
        obj.setFieldString("test data " + index);
    }

    // Creates a collection that is based on a RealmList that was deleted.
    protected OrderedRealmCollection<CyclicType> populateCollectionOnDeletedLinkView(Realm realm, ManagedCollection collectionClass) {
        realm.beginTransaction();
        CyclicType parent = realm.createObject(CyclicType.class);
        for (int i = 0; i < 10; i++) {
            CyclicType child = new CyclicType();
            child.setName("name_" + i);
            child.setObject(parent);
            parent.getObjects().add(child);
        }
        realm.commitTransaction();

        OrderedRealmCollection<CyclicType> result;
        switch (collectionClass) {
            case MANAGED_REALMLIST:
                result = parent.getObjects();
                break;
            case REALMRESULTS:
                result = parent.getObjects().where().equalTo(CyclicType.FIELD_NAME, "name_0").findAll();
                break;
            default:
                throw new AssertionError("Unknown collection: " + collectionClass);
        }

        realm.beginTransaction();
        parent.deleteFromRealm();
        realm.commitTransaction();
        return result;
    }

    // Creates a number of objects that mix null and real values for number type fields.
    protected void populatePartialNullRowsForNumericTesting(Realm realm) {
        NullTypes nullTypes1 = new NullTypes();
        nullTypes1.setId(1);
        nullTypes1.setFieldIntegerNull(1);
        nullTypes1.setFieldFloatNull(2F);
        nullTypes1.setFieldDoubleNull(3D);
        nullTypes1.setFieldBooleanNull(true);
        nullTypes1.setFieldStringNull("4");
        nullTypes1.setFieldDateNull(new Date(12345));

        NullTypes nullTypes2 = new NullTypes();
        nullTypes2.setId(2);

        NullTypes nullTypes3 = new NullTypes();
        nullTypes3.setId(3);
        nullTypes3.setFieldIntegerNull(0);
        nullTypes3.setFieldFloatNull(0F);
        nullTypes3.setFieldDoubleNull(0D);
        nullTypes3.setFieldBooleanNull(false);
        nullTypes3.setFieldStringNull("0");
        nullTypes3.setFieldDateNull(new Date(0));

        realm.beginTransaction();
        realm.copyToRealm(nullTypes1);
        realm.copyToRealm(nullTypes2);
        realm.copyToRealm(nullTypes3);
        realm.commitTransaction();
    }

    // Creates a list of AllJavaTypes with its `fieldString` field set to a given value.
    protected OrderedRealmCollection<AllJavaTypes> createStringCollection(Realm realm, ManagedCollection collectionClass, String... args) {
        realm.beginTransaction();
        realm.deleteAll();
        OrderedRealmCollection<AllJavaTypes> orderedCollection;
        switch (collectionClass) {
            case REALMRESULTS_SNAPSHOT_RESULTS_BASE:
            case REALMRESULTS:
                int id = 0;
                for (String arg : args) {
                    AllJavaTypes obj = realm.createObject(AllJavaTypes.class, id++);
                    obj.setFieldString(arg);
                }
                realm.commitTransaction();
                    orderedCollection = realm.where(AllJavaTypes.class).sort(AllJavaTypes.FIELD_STRING).findAll();
                break;

            case REALMRESULTS_SNAPSHOT_LIST_BASE:
            case MANAGED_REALMLIST:
                AllJavaTypes first = realm.createObject(AllJavaTypes.class, 0);
                first.setFieldString(args[0]);
                first.getFieldList().add(first);
                for (int i = 1; i < args.length; i++) {
                    AllJavaTypes obj = realm.createObject(AllJavaTypes.class, i);
                    obj.setFieldString(args[i]);
                    first.getFieldList().add(obj);
                }
                realm.commitTransaction();
                orderedCollection = first.getFieldList();
                break;

            default:
                throw new AssertionError("Unknown collection: " + collectionClass);
        }

        if (isSnapshot(collectionClass)) {
            orderedCollection = orderedCollection.createSnapshot();
        }

        return orderedCollection;
    }

    boolean isSnapshot(ManagedCollection collectionClass) {
        return collectionClass == ManagedCollection.REALMRESULTS_SNAPSHOT_LIST_BASE ||
                collectionClass == ManagedCollection.REALMRESULTS_SNAPSHOT_RESULTS_BASE;
    }

    boolean isSnapshot(CollectionClass collectionClass) {
        return collectionClass == CollectionClass.REALMRESULTS_SNAPSHOT_LIST_BASE ||
                collectionClass == CollectionClass.REALMRESULTS_SNAPSHOT_RESULTS_BASE;
    }

    boolean isRealmList(ManagedCollection collectionClass) {
        return collectionClass == ManagedCollection.MANAGED_REALMLIST;
    }
}
