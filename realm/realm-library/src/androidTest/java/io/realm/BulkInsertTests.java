/*
 * Copyright 2014-2016 Realm Inc.
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

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.realm.entities.AllJavaTypes;
import io.realm.entities.AllTypes;
import io.realm.entities.AllTypesPrimaryKey;
import io.realm.entities.AnimalModule;
import io.realm.entities.Cat;
import io.realm.entities.CatOwner;
import io.realm.entities.CyclicType;
import io.realm.entities.CyclicTypePrimaryKey;
import io.realm.entities.Dog;
import io.realm.entities.DogPrimaryKey;
import io.realm.entities.HumanModule;
import io.realm.entities.NoPrimaryKeyWithPrimaryKeyObjectRelation;
import io.realm.entities.NullTypes;
import io.realm.entities.Owner;
import io.realm.entities.PrimaryKeyAsBoxedShort;
import io.realm.entities.PrimaryKeyAsLong;
import io.realm.entities.PrimaryKeyAsString;
import io.realm.entities.PrimaryKeyWithNoPrimaryKeyObjectRelation;
import io.realm.entities.pojo.AllTypesRealmModel;
import io.realm.entities.pojo.InvalidRealmModel;
import io.realm.exceptions.RealmException;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import io.realm.internal.modules.CompositeMediator;
import io.realm.internal.modules.FilterableMediator;
import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class BulkInsertTests {
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm realm;

    @Before
    public void setUp() {
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        realm = Realm.getInstance(realmConfig);
    }

    @After
    public void tearDown() {
        if (realm != null) {
            realm.close();
        }
    }

    @Test
    public void insert() {
        AllJavaTypes obj = new AllJavaTypes();
        obj.setFieldIgnored("cookie");
        obj.setFieldId(42);
        obj.setFieldLong(42);
        obj.setFieldString("obj1");

        RealmList<AllJavaTypes> list = new RealmList<AllJavaTypes>();
        list.add(obj);

        Date date = new Date();

        AllJavaTypes allTypes = new AllJavaTypes();
        allTypes.setFieldString("String");
        allTypes.setFieldId(1L);
        allTypes.setFieldLong(1L);
        allTypes.setFieldFloat(1F);
        allTypes.setFieldDouble(1D);
        allTypes.setFieldBoolean(true);
        allTypes.setFieldDate(date);
        allTypes.setFieldBinary(new byte[]{1, 2, 3});
        allTypes.setFieldObject(obj);
        allTypes.setFieldList(list);

        realm.beginTransaction();
        realm.insert(allTypes);
        realm.commitTransaction();

        AllJavaTypes realmTypes = realm.where(AllJavaTypes.class).findFirst();

        assertNotNull(realmTypes);
        assertNotSame(allTypes, realmTypes); // Objects should not be considered equal
        assertEquals(allTypes.getFieldString(), realmTypes.getFieldString()); // But they contain the same data
        assertEquals(allTypes.getFieldLong(), realmTypes.getFieldLong());
        assertEquals(allTypes.getFieldFloat(), realmTypes.getFieldFloat(), 0);
        assertEquals(allTypes.getFieldDouble(), realmTypes.getFieldDouble(), 0);
        assertEquals(allTypes.isFieldBoolean(), realmTypes.isFieldBoolean());
        assertEquals(allTypes.getFieldDate(), realmTypes.getFieldDate());
        assertArrayEquals(allTypes.getFieldBinary(), realmTypes.getFieldBinary());
        assertEquals(allTypes.getFieldObject().getFieldString(), obj.getFieldString());
        assertEquals(list.size(), realmTypes.getFieldList().size());
        assertEquals(list.get(0).getFieldString(), realmTypes.getFieldList().get(0).getFieldString());
        assertEquals(list.get(0).getFieldLong(), realmTypes.getFieldList().get(0).getFieldLong());
        assertNull(realmTypes.getFieldList().get(0).getFieldIgnored());


        // Makes sure Dog was not inserted twice in the recursive process.
        assertEquals(2, realm.where(AllJavaTypes.class).findAll().size());
    }

    @Test
    public void insert_realmModel() {
        AllTypesRealmModel allTypes = new AllTypesRealmModel();
        allTypes.columnLong = 10;
        allTypes.columnBoolean = false;
        allTypes.columnBinary = new byte[]{1, 2, 3};
        allTypes.columnDate = new Date();
        allTypes.columnDouble = Math.PI;
        allTypes.columnFloat = 1.234567f;
        allTypes.columnString = "test data";
        allTypes.columnByte = 0x2A;

        realm.beginTransaction();
        realm.insert(allTypes);
        realm.commitTransaction();

        AllTypesRealmModel first = realm.where(AllTypesRealmModel.class).findFirst();
        assertNotNull(first);
        assertEquals(allTypes.columnString, first.columnString);
        assertEquals(allTypes.columnLong, first.columnLong);
        assertEquals(allTypes.columnBoolean, first.columnBoolean);
        assertArrayEquals(allTypes.columnBinary, first.columnBinary);
        assertEquals(allTypes.columnDate, first.columnDate);
        assertEquals(allTypes.columnDouble, first.columnDouble, 0.0000001);
        assertEquals(allTypes.columnFloat, first.columnFloat, 0.0000001);
        assertEquals(allTypes.columnByte, first.columnByte);
    }

    @Test
    public void insert_invalidRealmModel() {
        InvalidRealmModel invalidRealmModel = new InvalidRealmModel();

        realm.beginTransaction();
        try {
            realm.insert(invalidRealmModel);
            fail("Expected Missing Proxy Class Exception");
        } catch (RealmException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void insertOrUpdate_nullTypes() {
        NullTypes nullTypes1 = new NullTypes();
        nullTypes1.setId(1);
        nullTypes1.setFieldIntegerNull(1);
        nullTypes1.setFieldFloatNull(2F);
        nullTypes1.setFieldDoubleNull(3D);
        nullTypes1.setFieldBooleanNull(true);
        nullTypes1.setFieldStringNull("4");
        nullTypes1.setFieldDateNull(new Date(12345));

        realm.beginTransaction();
        realm.insert(nullTypes1);
        realm.commitTransaction();

        NullTypes first = realm.where(NullTypes.class).findFirst();

        assertNotNull(first);
        assertEquals(nullTypes1.getId(), first.getId());
        assertEquals(nullTypes1.getFieldIntegerNull(), first.getFieldIntegerNull());
        assertEquals(nullTypes1.getFieldFloatNull(), first.getFieldFloatNull());
        assertEquals(nullTypes1.getFieldDoubleNull(), first.getFieldDoubleNull());
        assertEquals(nullTypes1.getFieldBooleanNull(), first.getFieldBooleanNull());
        assertEquals(nullTypes1.getFieldStringNull(), first.getFieldStringNull());
        assertEquals(nullTypes1.getFieldDateNull(), first.getFieldDateNull());

        NullTypes nullTypes2 = new NullTypes();
        nullTypes2.setId(2);

        NullTypes nullTypes3 = new NullTypes();
        nullTypes3.setId(3);

        nullTypes1 = new NullTypes();
        nullTypes1.setId(1);
        nullTypes1.setFieldIntegerNull(null);
        nullTypes1.setFieldFloatNull(null);
        nullTypes1.setFieldDoubleNull(null);
        nullTypes1.setFieldBooleanNull(null);
        nullTypes1.setFieldStringNull(null);
        nullTypes1.setFieldDateNull(null);
        nullTypes1.setFieldListNull(new RealmList<NullTypes>());
        nullTypes1.getFieldListNull().add(nullTypes2);
        nullTypes1.getFieldListNull().add(nullTypes3);

        OrderedRealmCollection<NullTypes> collection = new RealmList<NullTypes>();
        collection.add(nullTypes2);
        collection.add(nullTypes1);
        collection.add(nullTypes3);

        realm.beginTransaction();
        realm.insertOrUpdate(collection);
        realm.commitTransaction();

        first = realm.where(NullTypes.class).equalTo("id", 1).findFirst();

        assertNotNull(first);
        assertEquals(nullTypes1.getId(), first.getId());
        assertNull(first.getFieldIntegerNull());
        assertNull(first.getFieldFloatNull());
        assertNull(first.getFieldDoubleNull());
        assertNull(first.getFieldBooleanNull());
        assertNull(first.getFieldStringNull());
        assertNull(first.getFieldDateNull());
        assertEquals(2, first.getFieldListNull().size());
        assertEquals(2, first.getFieldListNull().get(0).getId());
        assertEquals(3, first.getFieldListNull().get(1).getId());
    }

    @Test
    public void insert_cyclicType() {
        CyclicType oneCyclicType = new CyclicType();
        oneCyclicType.setName("One");
        CyclicType anotherCyclicType = new CyclicType();
        anotherCyclicType.setName("Two");
        oneCyclicType.setObject(anotherCyclicType);
        anotherCyclicType.setObject(oneCyclicType);

        realm.beginTransaction();
        realm.insert(Arrays.asList(oneCyclicType, anotherCyclicType));
        realm.commitTransaction();

        RealmResults<CyclicType> realmObjects = realm.where(CyclicType.class).findAllSorted(CyclicType.FIELD_NAME);
        assertNotNull(realmObjects);
        assertEquals(2, realmObjects.size());
        assertEquals("One", realmObjects.get(0).getName());
        assertEquals("Two", realmObjects.get(0).getObject().getName());
    }

    @Test
    public void insertOrUpdate_cyclicType() {
        CyclicTypePrimaryKey oneCyclicType = new CyclicTypePrimaryKey(1, "One");
        CyclicTypePrimaryKey anotherCyclicType = new CyclicTypePrimaryKey(2, "Two");
        oneCyclicType.setObject(anotherCyclicType);
        anotherCyclicType.setObject(oneCyclicType);

        realm.beginTransaction();
        realm.insertOrUpdate(Arrays.asList(oneCyclicType, anotherCyclicType));
        realm.commitTransaction();

        RealmResults<CyclicTypePrimaryKey> realmObjects = realm.where(CyclicTypePrimaryKey.class).findAllSorted("name");
        assertNotNull(realmObjects);
        assertEquals(2, realmObjects.size());
        assertEquals("One", realmObjects.get(0).getName());
        assertEquals("Two", realmObjects.get(0).getObject().getName());

        CyclicTypePrimaryKey updatedCyclicType = new CyclicTypePrimaryKey(2, "updated");

        realm.beginTransaction();
        realm.insertOrUpdate(updatedCyclicType);
        realm.commitTransaction();

        assertEquals("One", realmObjects.get(0).getName());
        assertEquals("updated", realmObjects.get(0).getObject().getName());
        assertEquals(2, realm.where(CyclicTypePrimaryKey.class).count());
    }

    @Test
    public void insertOrUpdate_cyclicDependenciesFromOtherRealm() {
        RealmConfiguration config1 = configFactory.createConfiguration("realm1");
        RealmConfiguration config2 = configFactory.createConfiguration("realm2");

        Realm realm1 = Realm.getInstance(config1);
        Realm realm2 = Realm.getInstance(config2);

        realm1.beginTransaction();
        Owner owner = realm1.createObject(Owner.class);
        owner.setName("Kiba");
        Dog dog = realm1.createObject(Dog.class);
        dog.setName("Akamaru");
        owner.getDogs().add(dog);
        dog.setOwner(owner);
        realm1.commitTransaction();

        // Copies object with relations from realm1 to realm2.
        realm2.beginTransaction();
        realm2.insertOrUpdate(owner);
        realm2.commitTransaction();

        assertEquals(1, realm1.where(Owner.class).count());
        assertEquals(1, realm1.where(Owner.class).findFirst().getDogs().size());
        assertEquals(1, realm1.where(Dog.class).count());

        assertEquals(realm1.where(Owner.class).count(), realm2.where(Owner.class).count());
        assertEquals(realm1.where(Dog.class).count(), realm2.where(Dog.class).count());

        assertEquals(1, realm2.where(Owner.class).findFirst().getDogs().size());

        assertEquals(realm1.where(Owner.class).findFirst().getName(), realm2.where(Owner.class).findFirst().getName());

        assertEquals(realm1.where(Owner.class).findFirst().getDogs().first().getName()
                , realm2.where(Owner.class).findFirst().getDogs().first().getName());

        realm1.close();
        realm2.close();
    }

    @Test
    public void insert_nullPrimaryKey() {
        PrimaryKeyAsString primaryKeyAsString = new PrimaryKeyAsString();
        primaryKeyAsString.setId(19);

        realm.beginTransaction();
        realm.insertOrUpdate(primaryKeyAsString);
        realm.commitTransaction();

        primaryKeyAsString = realm.where(PrimaryKeyAsString.class).isNull("name").findFirst();
        assertNotNull(primaryKeyAsString);
        assertNull(primaryKeyAsString.getName());
        assertEquals(19, primaryKeyAsString.getId());

        PrimaryKeyAsBoxedShort primaryKeyAsShort = new PrimaryKeyAsBoxedShort();
        primaryKeyAsShort.setName("42");

        realm.beginTransaction();
        realm.insertOrUpdate(primaryKeyAsShort);
        realm.commitTransaction();

        primaryKeyAsShort = realm.where(PrimaryKeyAsBoxedShort.class).isNull("id").findFirst();
        assertNotNull(primaryKeyAsShort);
        assertNull(primaryKeyAsShort.getId());
        assertEquals("42", primaryKeyAsShort.getName());
    }

    @Test
    public void insert_duplicatedPrimaryKeyFails() {

        // Single object with 2 references to two objects with the same ID
        AllJavaTypes obj = new AllJavaTypes(2);
        obj.setFieldList(new RealmList<AllJavaTypes>(new AllJavaTypes(1), new AllJavaTypes(1)));
        realm.beginTransaction();
        try {
            realm.insert(obj);
            fail();
        } catch (RealmPrimaryKeyConstraintException ignored) {
        } finally {
            realm.cancelTransaction();
        }

        // Two objects with the same ID in a list
        realm.beginTransaction();
        try {
            realm.insert(Arrays.asList(new AllJavaTypes(1), new AllJavaTypes(1)));
            fail();
        } catch (RealmPrimaryKeyConstraintException ignored) {
        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void insertOrUpdate() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AllTypesPrimaryKey obj = new AllTypesPrimaryKey();
                obj.setColumnString("Foo");
                obj.setColumnLong(1);
                obj.setColumnFloat(1.23F);
                obj.setColumnDouble(1.234D);
                obj.setColumnBoolean(false);
                obj.setColumnBinary(new byte[]{1, 2, 3});
                obj.setColumnDate(new Date(1000));
                obj.setColumnRealmObject(new DogPrimaryKey(1, "Dog1"));
                obj.setColumnRealmList(new RealmList<DogPrimaryKey>(new DogPrimaryKey(2, "Dog2")));
                obj.setColumnBoxedBoolean(true);
                realm.insert(obj);

                AllTypesPrimaryKey obj2 = new AllTypesPrimaryKey();
                obj2.setColumnString("Bar");
                obj2.setColumnLong(1);
                obj2.setColumnFloat(2.23F);
                obj2.setColumnDouble(2.234D);
                obj2.setColumnBoolean(true);
                obj2.setColumnBinary(new byte[]{2, 3, 4});
                obj2.setColumnDate(new Date(2000));
                obj2.setColumnRealmObject(new DogPrimaryKey(3, "Dog3"));
                obj2.setColumnRealmList(new RealmList<DogPrimaryKey>(new DogPrimaryKey(4, "Dog4")));
                obj2.setColumnBoxedBoolean(false);
                realm.insertOrUpdate(obj2);
            }
        });

        assertEquals(1, realm.where(AllTypesPrimaryKey.class).count());
        AllTypesPrimaryKey obj = realm.where(AllTypesPrimaryKey.class).findFirst();

        // Checks that the only element has all its properties updated.
        assertNotNull(obj);
        assertEquals("Bar", obj.getColumnString());
        assertEquals(1, obj.getColumnLong());
        assertEquals(2.23F, obj.getColumnFloat(), 0);
        assertEquals(2.234D, obj.getColumnDouble(), 0);
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{2, 3, 4}, obj.getColumnBinary());
        assertEquals(new Date(2000), obj.getColumnDate());
        assertEquals("Dog3", obj.getColumnRealmObject().getName());
        assertEquals(1, obj.getColumnRealmList().size());
        assertEquals("Dog4", obj.getColumnRealmList().get(0).getName());
        assertEquals(4, realm.where(DogPrimaryKey.class).findAll().size());
        assertFalse(obj.getColumnBoxedBoolean());
    }

    @Test
    public void insert_list() {
        Dog dog1 = new Dog();
        dog1.setName("Dog 1");
        Dog dog2 = new Dog();
        dog2.setName("Dog 2");
        RealmList<Dog> list = new RealmList<Dog>();
        list.addAll(Arrays.asList(dog1, dog2));

        realm.beginTransaction();
        realm.insert(list);
        realm.commitTransaction();


        RealmResults<Dog> copiedList = realm.where(Dog.class).findAll();

        assertEquals(2, copiedList.size());
        assertEquals(dog1.getName(), copiedList.get(0).getName());
        assertEquals(dog2.getName(), copiedList.get(1).getName());
    }

    @Test
    public void insert_emptyList() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insert(Collections.<PrimaryKeyAsLong>emptyList());
            }
        });
        assertEquals(0, realm.where(PrimaryKeyAsLong.class).count());
    }

    /**
     * Added to reproduce https://github.com/realm/realm-java/issues/3103
     */
    @Test
    public void insert_emptyListWithCompositeMediator() {
        final RealmConfiguration config = configFactory.createConfigurationBuilder()
                .modules(new HumanModule(), new AnimalModule())
                .name("composite.realm")
                .build();
        Realm.deleteRealm(config);

        assertEquals(config.getSchemaMediator().getClass(), CompositeMediator.class);

        final Realm realm = Realm.getInstance(config);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insert(Collections.<Cat>emptyList());
                }
            });
            assertEquals(0, realm.where(Cat.class).count());
        } finally {
            realm.close();
        }
    }

    /**
     * Added to reproduce https://github.com/realm/realm-java/issues/3103
     */
    @Test
    public void insert_emptyListWithFilterableMediator() {
        //noinspection unchecked
        final RealmConfiguration config = configFactory.createConfigurationBuilder()
                .schema(CatOwner.class, Cat.class, Owner.class, DogPrimaryKey.class, Dog.class)
                .name("filterable.realm")
                .build();
        Realm.deleteRealm(config);

        assertEquals(config.getSchemaMediator().getClass(), FilterableMediator.class);

        final Realm realm = Realm.getInstance(config);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insert(Collections.<Cat>emptyList());
                }
            });
            assertEquals(0, realm.where(Cat.class).count());
        } finally {
            realm.close();
        }
    }

    @Test
    public void insertOrUpdate_list() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                PrimaryKeyAsLong obj = new PrimaryKeyAsLong();
                obj.setId(1);
                obj.setName("Foo");
                realm.copyToRealm(obj);

                PrimaryKeyAsLong obj2 = new PrimaryKeyAsLong();
                obj2.setId(1);
                obj2.setName("Bar");

                PrimaryKeyAsLong obj3 = new PrimaryKeyAsLong();
                obj3.setId(1);
                obj3.setName("Baz");

                realm.insertOrUpdate(Arrays.asList(obj2, obj3));
            }
        });

        assertEquals(1, realm.where(PrimaryKeyAsLong.class).count());
        PrimaryKeyAsLong first = realm.where(PrimaryKeyAsLong.class).findFirst();
        assertNotNull(first);
        assertEquals("Baz", first.getName());
    }

    @Test
    public void insertOrUpdate_emptyList() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(Collections.<PrimaryKeyAsLong>emptyList());
            }
        });
        assertEquals(0, realm.where(PrimaryKeyAsLong.class).count());
    }

    /**
     * Added to reproduce https://github.com/realm/realm-java/issues/3103
     */
    @Test
    public void insertOrUpdate_emptyListWithCompositeMediator() {
        final RealmConfiguration config = configFactory.createConfigurationBuilder()
                .modules(new HumanModule(), new AnimalModule())
                .name("composite.realm")
                .build();
        Realm.deleteRealm(config);

        assertEquals(config.getSchemaMediator().getClass(), CompositeMediator.class);

        final Realm realm = Realm.getInstance(config);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(Collections.<Cat>emptyList());
                }
            });
            assertEquals(0, realm.where(Cat.class).count());
        } finally {
            realm.close();
        }
    }

    /**
     * Added to reproduce https://github.com/realm/realm-java/issues/3103
     */
    @Test
    public void insertOrUpdate_emptyListWithFilterableMediator() {
        //noinspection unchecked
        final RealmConfiguration config = configFactory.createConfigurationBuilder()
                .schema(CatOwner.class, Cat.class, Owner.class, DogPrimaryKey.class, Dog.class)
                .name("filterable.realm")
                .build();
        Realm.deleteRealm(config);

        assertEquals(config.getSchemaMediator().getClass(), FilterableMediator.class);

        final Realm realm = Realm.getInstance(config);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(Collections.<Cat>emptyList());
                }
            });
            assertEquals(0, realm.where(Cat.class).count());
        } finally {
            realm.close();
        }
    }

    @Test
    public void insertOrUpdate_mixingPrimaryKeyAndNoPrimaryKeyModels() {
        AllTypes objB_no_pk = new AllTypes();
        objB_no_pk.setColumnString("B");

        PrimaryKeyWithNoPrimaryKeyObjectRelation objA_pk = new PrimaryKeyWithNoPrimaryKeyObjectRelation();
        objA_pk.setColumnString("A");
        objA_pk.setColumnRealmObjectNoPK(objB_no_pk);

        realm.beginTransaction();
        realm.insert(objA_pk);
        realm.commitTransaction();

        RealmResults<PrimaryKeyWithNoPrimaryKeyObjectRelation> all = realm.where(PrimaryKeyWithNoPrimaryKeyObjectRelation.class).findAll();
        assertEquals(1, all.size());
        assertEquals("A", all.get(0).getColumnString());
        assertEquals(8, all.get(0).getColumnInt());
        assertNotNull(all.get(0).getColumnRealmObjectNoPK());
        assertEquals("B", all.get(0).getColumnRealmObjectNoPK().getColumnString());
        assertEquals(1, realm.where(AllTypes.class).findAll().size());

        objA_pk.setColumnInt(42);
        objB_no_pk.setColumnString("updated B");

        realm.beginTransaction();
        realm.insertOrUpdate(objA_pk);
        realm.commitTransaction();

        all = realm.where(PrimaryKeyWithNoPrimaryKeyObjectRelation.class).findAll();
        assertEquals(1, all.size());
        assertEquals("A", all.get(0).getColumnString());
        assertEquals(42, all.get(0).getColumnInt());
        assertNotNull(all.get(0).getColumnRealmObjectNoPK());
        assertEquals("updated B", all.get(0).getColumnRealmObjectNoPK().getColumnString());
        // Since AllTypes doesn't have a PK we now have two instances.
        assertEquals(2, realm.where(AllTypes.class).findAll().size());
    }


    @Test
    public void insertOrUpdate_mixingNoPrimaryKeyAndPrimaryKeyModels() {
        AllTypesPrimaryKey objB_pk = new AllTypesPrimaryKey();
        objB_pk.setColumnLong(7);
        objB_pk.setColumnString("B");

        NoPrimaryKeyWithPrimaryKeyObjectRelation objA_no_pk = new NoPrimaryKeyWithPrimaryKeyObjectRelation();
        objA_no_pk.setColumnRealmObjectPK(objB_pk);
        objA_no_pk.setColumnString("A");

        realm.beginTransaction();
        realm.insert(objA_no_pk);
        realm.commitTransaction();

        RealmResults<NoPrimaryKeyWithPrimaryKeyObjectRelation> all = realm.where(NoPrimaryKeyWithPrimaryKeyObjectRelation.class).findAll();
        assertEquals(1, all.size());
        assertEquals("A", all.get(0).getColumnString());
        assertEquals(8, all.get(0).getColumnInt());
        assertNotNull(all.get(0).getColumnRealmObjectPK());
        assertEquals(7, all.get(0).getColumnRealmObjectPK().getColumnLong());
        assertEquals("B", all.get(0).getColumnRealmObjectPK().getColumnString());
        assertEquals(1, realm.where(AllTypesPrimaryKey.class).findAll().size());

        objA_no_pk.setColumnString("different A");
        objA_no_pk.setColumnInt(42); // Should insert a new instance
        // Updates (since it has a PK) now both AllTypesPrimaryKey points to the same objB_pk instance.
        objB_pk.setColumnString("updated B");

        realm.beginTransaction();
        realm.insertOrUpdate(objA_no_pk);
        realm.commitTransaction();

        all = realm.where(NoPrimaryKeyWithPrimaryKeyObjectRelation.class).findAllSorted("columnString");
        assertEquals(2, all.size());
        assertEquals("A", all.get(0).getColumnString());
        assertEquals(8, all.get(0).getColumnInt());
        assertEquals("different A", all.get(1).getColumnString());
        assertEquals(42, all.get(1).getColumnInt());

        assertNotNull(all.get(0).getColumnRealmObjectPK());
        assertNotNull(all.get(1).getColumnRealmObjectPK());

        assertEquals(7, all.get(0).getColumnRealmObjectPK().getColumnLong());
        assertEquals(7, all.get(1).getColumnRealmObjectPK().getColumnLong());
        assertEquals("updated B", all.get(0).getColumnRealmObjectPK().getColumnString());
        assertEquals("updated B", all.get(1).getColumnRealmObjectPK().getColumnString());
        assertEquals(1, realm.where(AllTypesPrimaryKey.class).findAll().size());
    }

    @Test
    public void insertOrUpdate_mixingPrimaryAndNoPrimaryKeyList() {
        NoPrimaryKeyWithPrimaryKeyObjectRelation objA_no_pk = new NoPrimaryKeyWithPrimaryKeyObjectRelation();
        objA_no_pk.setColumnString("A");
        NoPrimaryKeyWithPrimaryKeyObjectRelation objB_no_pk = new NoPrimaryKeyWithPrimaryKeyObjectRelation();
        objB_no_pk.setColumnString("B");
        AllTypesPrimaryKey objC_pk = new AllTypesPrimaryKey();
        objC_pk.setColumnLong(7);
        objC_pk.setColumnString("C");
        AllTypesPrimaryKey objD_pk = new AllTypesPrimaryKey();
        objD_pk.setColumnLong(7);
        objD_pk.setColumnString("D");

        objA_no_pk.setColumnRealmObjectPK(objC_pk);
        objB_no_pk.setColumnRealmObjectPK(objD_pk);

        ArrayList<NoPrimaryKeyWithPrimaryKeyObjectRelation> objects = new ArrayList<NoPrimaryKeyWithPrimaryKeyObjectRelation>(2);
        objects.add(objA_no_pk);
        objects.add(objB_no_pk);

        realm.beginTransaction();
        realm.insertOrUpdate(objects);
        realm.commitTransaction();

        RealmResults<NoPrimaryKeyWithPrimaryKeyObjectRelation> all = realm.where(NoPrimaryKeyWithPrimaryKeyObjectRelation.class).findAllSorted("columnString", Sort.DESCENDING);
        assertEquals(2, all.size());
        assertEquals("B", all.get(0).getColumnString());
        assertEquals("A", all.get(1).getColumnString());

        assertNotNull(all.get(0).getColumnRealmObjectPK());
        assertNotNull(all.get(1).getColumnRealmObjectPK());

        assertEquals("D", all.get(0).getColumnRealmObjectPK().getColumnString());
        assertEquals("D", all.get(1).getColumnRealmObjectPK().getColumnString());

        assertEquals(1, realm.where(AllTypesPrimaryKey.class).findAll().size());
    }

    // Any omitted argument should not end in a SIGSEGV but an exception.

    @Test
    public void insert_nullObject() {
        AllTypes nullObject = null;

        realm.beginTransaction();
        try {
            //noinspection ConstantConditions
            realm.insert(nullObject);
            fail("Should trigger NullPointerException");
        } catch (IllegalArgumentException ignore) {

        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void inset_nullList() {
        List<AllTypes> nullObjects = null;

        realm.beginTransaction();
        try {
            //noinspection ConstantConditions
            realm.insert(nullObjects);
            fail("Should trigger IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {

        } finally {
            realm.cancelTransaction();
        }
    }

    @Test
    public void insert_listWithNullElement() {
        Dog dog1 = new Dog();
        dog1.setName("Dog 1");
        Dog dog2 = new Dog();
        dog2.setName("Dog 2");
        ArrayList<Dog> list = new ArrayList<Dog>();
        list.addAll(Arrays.asList(dog1, null, dog2));

        realm.beginTransaction();
        try {
            realm.insert(list);
            fail("Should trigger IllegalArgumentException");
        } catch (NullPointerException ignore) {

        } finally {
            realm.cancelTransaction();
        }
    }

    // Inserting a managed object will result in it being copied or updated again.
    @Test
    public void insertOrUpdate_managedObject() {
        AllJavaTypes obj = new AllJavaTypes();
        obj.setFieldId(42);
        obj.setFieldIgnored("cookie");
        obj.setFieldLong(42);
        obj.setFieldString("obj1");

        realm.beginTransaction();
        AllJavaTypes managedAllJavaTypes = realm.copyToRealm(obj);
        realm.commitTransaction();

        realm.beginTransaction();

        AllJavaTypes filedObject = new AllJavaTypes();
        filedObject.setFieldLong(8);
        filedObject = realm.copyToRealm(filedObject);
        managedAllJavaTypes.setFieldObject(filedObject);
        managedAllJavaTypes.setFieldString("updated");

        realm.insertOrUpdate(managedAllJavaTypes);
        realm.commitTransaction();

        AllJavaTypes first = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_LONG, 42).findFirst();
        assertNotNull(first);
        assertEquals(42, first.getFieldLong(), 0);
        assertEquals("updated", first.getFieldString());
        assertNull(first.getFieldIgnored());
        assertNotNull(first.getFieldObject());
        assertEquals(8, first.getFieldObject().getFieldLong());

        assertEquals(2, realm.where(AllJavaTypes.class).findAll().size());
    }

    @Test
    public void insertOrUpdate_linkingManagedToUnmanagedObject() {
        realm.beginTransaction();
        AllJavaTypes managedAllJavaTypes = realm.createObject(AllJavaTypes.class, 42);
        realm.commitTransaction();

        AllJavaTypes unmanagedObject = new AllJavaTypes(8);
        unmanagedObject.setFieldObject(managedAllJavaTypes);//Linking managed object to unmanaged object

        realm.beginTransaction();
        realm.insertOrUpdate(unmanagedObject);
        realm.commitTransaction();

        AllJavaTypes first = realm.where(AllJavaTypes.class).equalTo(AllJavaTypes.FIELD_ID, 8).findFirst();
        assertNotNull(first);
        assertEquals(8, first.getFieldId(), 0);
        assertNotNull(first.getFieldObject());
        assertEquals(42, first.getFieldObject().getFieldId());
        assertEquals(2, realm.where(AllJavaTypes.class).count());
    }

    @Test
    public void insertManagedObjectWillNotDuplicate() {
        realm.beginTransaction();
        Dog managedRealmObject = realm.createObject(Dog.class);
        managedRealmObject.setName("dog1");
        realm.commitTransaction();

        realm.beginTransaction();
        realm.insert(managedRealmObject);
        realm.commitTransaction();

        assertEquals(1, realm.where(Dog.class).count());
    }

    @Test
    public void insertOrUpdate_collectionOfManagedObjects() {
        realm.beginTransaction();
        AllTypesPrimaryKey allTypes = realm.createObject(AllTypesPrimaryKey.class, 0);
        allTypes.getColumnRealmList().add(realm.createObject(DogPrimaryKey.class, 0));
        realm.commitTransaction();
        assertEquals(1, allTypes.getColumnRealmList().size());

        List<AllTypesPrimaryKey>  list = new ArrayList<AllTypesPrimaryKey>();
        // Although there are two same objects in the list, none of them should be saved to the db since they are managed
        // already.
        list.add(allTypes);
        list.add(allTypes);

        realm.beginTransaction();
        realm.insertOrUpdate(list);
        realm.commitTransaction();
        allTypes = realm.where(AllTypesPrimaryKey.class).findFirst();
        assertNotNull(allTypes);
        assertEquals(1, allTypes.getColumnRealmList().size());
    }

    // To reproduce https://github.com/realm/realm-java/issues/3105.
    @Test
    public void insertOrUpdate_shouldNotClearRealmList() {
        realm.beginTransaction();
        AllTypesPrimaryKey allTypes = realm.createObject(AllTypesPrimaryKey.class, 0);
        allTypes.getColumnRealmList().add(realm.createObject(DogPrimaryKey.class, 0));
        realm.commitTransaction();
        assertEquals(1, allTypes.getColumnRealmList().size());

        realm.beginTransaction();
        realm.insertOrUpdate(allTypes);
        realm.commitTransaction();
        allTypes = realm.where(AllTypesPrimaryKey.class).findFirst();
        assertNotNull(allTypes);
        assertEquals(1, allTypes.getColumnRealmList().size());
    }

    @Test
    public void insert_collectionOfManagedObjects() {
        realm.beginTransaction();
        AllTypes allTypes = realm.createObject(AllTypes.class);
        allTypes.getColumnRealmList().add(realm.createObject(Dog.class));
        realm.commitTransaction();
        assertEquals(1, allTypes.getColumnRealmList().size());

        List<AllTypes>  list = new ArrayList<AllTypes>();
        // Although there are two same objects in the list, none of them should be saved to the db since they are managed
        // already.
        list.add(allTypes);
        list.add(allTypes);

        realm.beginTransaction();
        realm.insert(list);
        realm.commitTransaction();
        assertEquals(1, realm.where(AllTypes.class).count());
        allTypes = realm.where(AllTypes.class).findFirst();
        assertNotNull(allTypes);
        assertEquals(1, allTypes.getColumnRealmList().size());
    }

    @Test(expected = IllegalStateException.class)
    public void insert_collection_notInTransaction() {
        realm.insert(Arrays.asList(new AllTypes(), new AllTypes()));
    }

    @Test(expected = IllegalStateException.class)
    public void insert_object_notInTransaction() {
        realm.insert(new AllTypes());
    }

    @Test(expected = IllegalStateException.class)
    public void insertOrUpdate_collection_notInTransaction() {
        realm.insert(Arrays.asList(new AllTypesPrimaryKey(), new AllTypesPrimaryKey()));
    }

    @Test(expected = IllegalStateException.class)
    public void insertOrUpdate_object_notInTransaction() {
        realm.insert(new AllTypes());
    }
}
