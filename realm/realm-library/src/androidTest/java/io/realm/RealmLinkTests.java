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

package io.realm;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import io.realm.entities.AllTypes;
import io.realm.entities.Cat;
import io.realm.entities.Dog;
import io.realm.entities.Owner;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RealmLinkTests {

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    private Realm testRealm;
    private RealmConfiguration realmConfig;

    @Before
    public void setUp() {
        realmConfig = configFactory.createConfiguration();
        testRealm = Realm.getInstance(realmConfig);

        populate();
    }

    private void populate() {
        testRealm.beginTransaction();
        testRealm.delete(Dog.class);
        testRealm.delete(Cat.class);
        testRealm.delete(Owner.class);

        Dog dog1 = testRealm.createObject(Dog.class);
        dog1.setName("Pluto");
        dog1.setAge(5);
        dog1.setHeight(1.2f);
        dog1.setWeight(9.9);
        dog1.setHasTail(true);
        dog1.setBirthday(new Date(2000));

        Dog dog2 = testRealm.createObject(Dog.class);
        dog2.setName("Fido");
        dog2.setAge(10);
        dog2.setHeight(0.7f);
        dog2.setWeight(11.3);
        dog2.setHasTail(true);
        dog2.setBirthday(new Date(4000));

        Cat cat = testRealm.createObject(Cat.class);
        cat.setName("Blackie");
        cat.setAge(12);
        cat.setHeight(0.3f);
        cat.setWeight(1.1);
        cat.setHasTail(true);
        cat.setBirthday(new Date(6000));

        Owner owner = testRealm.createObject(Owner.class);
        owner.setName("Tim");
        owner.getDogs().add(dog1);
        owner.getDogs().add(dog2);
        owner.setCat(cat);

        cat.setOwner(owner);
        dog1.setOwner(owner);
        dog2.setOwner(owner);

        testRealm.commitTransaction();
    }

    @After
    public void tearDown() {
        if (testRealm != null) {
            testRealm.close();
        }
    }

    @Test
    public void objects() {
        RealmResults<Owner> owners = testRealm.where(Owner.class).findAll();
        assertEquals(1, owners.size());
        assertEquals(2, owners.first().getDogs().size());
        assertEquals("Pluto", owners.first().getDogs().first().getName());
        assertEquals("Fido", owners.first().getDogs().last().getName());
        assertEquals("Blackie", owners.first().getCat().getName());
        assertEquals(12, owners.first().getCat().getAge());

        RealmResults<Dog> dogs = testRealm.where(Dog.class).findAll();
        assertEquals(2, dogs.size());
        for (Dog dog : dogs) {
            assertEquals("Tim", dog.getOwner().getName());
        }

        RealmResults<Cat> cats = testRealm.where(Cat.class).findAll();
        assertEquals(1, cats.size());
        assertEquals("Tim", cats.first().getOwner().getName());
    }


    @Test
    public void reamListQuery() {
        RealmResults<Owner> owners = testRealm.where(Owner.class).findAll();
        RealmResults<Dog> dogs = owners.get(0).getDogs().where().contains("name", "o").findAll();
        assertEquals(2, dogs.size());
        assertEquals("Pluto", dogs.get(0).getName());
        assertEquals("Fido", dogs.get(1).getName());
    }

    @Test
    public void querySingleRelationBoolean() {
        RealmResults<Owner> owners = testRealm.where(Owner.class).equalTo("cat.hasTail", true).findAll();
        assertEquals(1, owners.size());
        assertEquals(12, owners.first().getCat().getAge());

        RealmResults<Owner> none = testRealm.where(Owner.class).equalTo("cat.hasTail", false).findAll();
        assertEquals(0, none.size());
    }

    @Test
    public void querySingleRelationInteger() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).equalTo("cat.age", 12).findAll();
        assertEquals(1, owners1.size());
        assertEquals(12, owners1.first().getCat().getAge());

        RealmResults<Owner> none1 = testRealm.where(Owner.class).equalTo("cat.age", 13).findAll();
        assertEquals(0, none1.size());

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).notEqualTo("cat.age", 13).findAll();
        assertEquals(1, owners2.size());
        assertEquals(12, owners2.first().getCat().getAge());

        RealmResults<Owner> none2 = testRealm.where(Owner.class).notEqualTo("cat.age", 12).findAll();
        assertEquals(0, none2.size());

        RealmResults<Owner> owners3 = testRealm.where(Owner.class).greaterThan("cat.age", 5).findAll();
        assertEquals(1, owners3.size());
        assertEquals(12, owners3.first().getCat().getAge());

        RealmResults<Owner> owners4 = testRealm.where(Owner.class).greaterThanOrEqualTo("cat.age", 5).findAll();
        assertEquals(1, owners4.size());
        assertEquals(12, owners4.first().getCat().getAge());

        RealmResults<Owner> owners5 = testRealm.where(Owner.class).lessThan("cat.age", 20).findAll();
        assertEquals(1, owners5.size());
        assertEquals(12, owners5.first().getCat().getAge());

        RealmResults<Owner> owners6 = testRealm.where(Owner.class).lessThanOrEqualTo("cat.age", 20).findAll();
        assertEquals(1, owners6.size());
        assertEquals(12, owners6.first().getCat().getAge());

        try {
            RealmResults<Owner> owners7 = testRealm.where(Owner.class).between("cat.age", 1, 20).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void querySingleRelationDate() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).equalTo("cat.birthday", new Date(6000)).findAll();
        assertEquals(1, owners1.size());
        assertEquals(12, owners1.first().getCat().getAge());

        RealmResults<Owner> none1 = testRealm.where(Owner.class).equalTo("cat.birthday", new Date(1000)).findAll();
        assertEquals(0, none1.size());

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).notEqualTo("cat.birthday", new Date(1000)).findAll();
        assertEquals(1, owners2.size());
        assertEquals(12, owners2.first().getCat().getAge());

        RealmResults<Owner> none2 = testRealm.where(Owner.class).notEqualTo("cat.birthday", new Date(6000)).findAll();
        assertEquals(0, none2.size());

        RealmResults<Owner> owners3 = testRealm.where(Owner.class).greaterThan("cat.birthday", new Date(5)).findAll();
        assertEquals(1, owners3.size());
        assertEquals(12, owners3.first().getCat().getAge());

        RealmResults<Owner> owners4 = testRealm.where(Owner.class).greaterThanOrEqualTo("cat.birthday", new Date(5)).findAll();
        assertEquals(1, owners4.size());
        assertEquals(12, owners4.first().getCat().getAge());

        RealmResults<Owner> owners5 = testRealm.where(Owner.class).lessThan("cat.birthday", new Date(10000)).findAll();
        assertEquals(1, owners5.size());
        assertEquals(12, owners5.first().getCat().getAge());

        RealmResults<Owner> owners6 = testRealm.where(Owner.class).lessThanOrEqualTo("cat.birthday", new Date(10000)).findAll();
        assertEquals(1, owners6.size());
        assertEquals(12, owners6.first().getCat().getAge());

        try {
            testRealm.where(Owner.class).between("cat.birthday", new Date(1), new Date(10000)).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
            // Between does not support link queries.
        }
    }

    @Test
    public void querySingleRelationFloat() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).greaterThanOrEqualTo("cat.height", 0.2f).findAll();
        assertEquals(1, owners1.size());
        assertEquals(12, owners1.first().getCat().getAge());

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).greaterThan("cat.height", 0.2f).findAll();
        assertEquals(1, owners2.size());
        assertEquals(12, owners2.first().getCat().getAge());

        RealmResults<Owner> owners3 = testRealm.where(Owner.class).lessThan("cat.height", 2.2f).findAll();
        assertEquals(1, owners3.size());
        assertEquals(12, owners3.first().getCat().getAge());

        RealmResults<Owner> owners4 = testRealm.where(Owner.class).lessThanOrEqualTo("cat.height", 2.2f).findAll();
        assertEquals(1, owners4.size());
        assertEquals(12, owners4.first().getCat().getAge());

        RealmResults<Owner> owners5 = testRealm.where(Owner.class).notEqualTo("cat.height", 0.2f).findAll();
        assertEquals(1, owners5.size());
        assertEquals(12, owners5.first().getCat().getAge());

        RealmResults<Owner> owners6 = testRealm.where(Owner.class).greaterThanOrEqualTo("cat.height", 0.3f).findAll();
        assertEquals(1, owners6.size());
        assertEquals(12, owners6.first().getCat().getAge());

        try {
            RealmResults<Owner> owners7 = testRealm.where(Owner.class).between("cat.height", 0.2f, 2.2f).findAll();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void querySingleRelationDouble() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).greaterThanOrEqualTo("cat.weight", 0.2).findAll();
        assertEquals(1, owners1.size());
        assertEquals(12, owners1.first().getCat().getAge());

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).greaterThan("cat.weight", 0.2).findAll();
        assertEquals(1, owners2.size());
        assertEquals(12, owners2.first().getCat().getAge());

        RealmResults<Owner> owners3 = testRealm.where(Owner.class).lessThan("cat.weight", 2.2).findAll();
        assertEquals(1, owners3.size());
        assertEquals(12, owners3.first().getCat().getAge());

        RealmResults<Owner> owners4 = testRealm.where(Owner.class).lessThanOrEqualTo("cat.weight", 2.2).findAll();
        assertEquals(1, owners4.size());
        assertEquals(12, owners4.first().getCat().getAge());

        RealmResults<Owner> owners5 = testRealm.where(Owner.class).notEqualTo("cat.weight", 0.2).findAll();
        assertEquals(1, owners5.size());
        assertEquals(12, owners5.first().getCat().getAge());

        RealmResults<Owner> owners6 = testRealm.where(Owner.class).greaterThanOrEqualTo("cat.weight", 0.3).findAll();
        assertEquals(1, owners6.size());
        assertEquals(12, owners6.first().getCat().getAge());

        try {
            RealmResults<Owner> owners7 = testRealm.where(Owner.class).between("cat.weight", 0.2, 2.2).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void querySingleRelationString() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).equalTo("cat.name", "Blackie").findAll();
        assertEquals(1, owners1.size());

        RealmResults<Owner> none1 = testRealm.where(Owner.class).equalTo("cat.name", "Max").findAll();
        assertEquals(0, none1.size());

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).notEqualTo("cat.name", "Max").findAll();
        assertEquals(1, owners2.size());

        RealmResults<Owner> none2 = testRealm.where(Owner.class).notEqualTo("cat.name", "Blackie").findAll();
        assertEquals(0, none2.size());

        RealmResults<Owner> owners3 = testRealm.where(Owner.class).beginsWith("cat.name", "Blackie").findAll();
        assertEquals(1, owners3.size());

        RealmResults<Owner> owners4 = testRealm.where(Owner.class).endsWith("cat.name", "Blackie").findAll();
        assertEquals(1, owners4.size());

        RealmResults<Owner> owners5 = testRealm.where(Owner.class).contains("cat.name", "Blackie").findAll();
        assertEquals(1, owners5.size());

        RealmResults<Owner> owners6 = testRealm.where(Owner.class).equalTo("cat.name", "blackIE", Case.INSENSITIVE).findAll();
        assertEquals(1, owners6.size());
    }

    @Test
    public void queryMultipleRelationsBoolean() {
        RealmResults<Owner> owners = testRealm.where(Owner.class).equalTo("dogs.hasTail", true).findAll();
        assertEquals(1, owners.size());

        RealmResults<Owner> none = testRealm.where(Owner.class).notEqualTo("dogs.hasTail", true).findAll();
        assertEquals(0, none.size());
    }

    @Test
    public void queryMultipleRelationsInteger() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).equalTo("dogs.age", 10).findAll();
        assertEquals(1, owners1.size());

        RealmResults<Owner> none1 = testRealm.where(Owner.class).equalTo("dogs.age", 7).findAll();
        assertEquals(0, none1.size());

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).notEqualTo("dogs.age", 10).findAll();
        assertEquals(1, owners2.size());

        RealmResults<Owner> all1 = testRealm.where(Owner.class).notEqualTo("dogs.age", 7).findAll();
        assertEquals(1, all1.size());

        RealmResults<Owner> owners3 = testRealm.where(Owner.class).greaterThan("dogs.age", 9).findAll();
        assertEquals(1, owners3.size());

        RealmResults<Owner> owners4 = testRealm.where(Owner.class).greaterThanOrEqualTo("dogs.age", 9).findAll();
        assertEquals(1, owners4.size());

        RealmResults<Owner> owners5 = testRealm.where(Owner.class).lessThan("dogs.age", 9).findAll();
        assertEquals(1, owners5.size());

        RealmResults<Owner> owners6 = testRealm.where(Owner.class).lessThanOrEqualTo("dogs.age", 9).findAll();
        assertEquals(1, owners6.size());

        try {
            RealmResults<Owner> owners7 = testRealm.where(Owner.class).between("dogs.age", 9, 11).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void queryMultipleRelationsDate() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).equalTo("dogs.birthday", new Date(2000)).findAll();
        assertEquals(1, owners1.size());

        RealmResults<Owner> none1 = testRealm.where(Owner.class).equalTo("dogs.birthday", new Date(7)).findAll();
        assertEquals(0, none1.size());

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).notEqualTo("dogs.birthday", new Date(10)).findAll();
        assertEquals(1, owners2.size());

        RealmResults<Owner> all1 = testRealm.where(Owner.class).notEqualTo("dogs.birthday", new Date(7)).findAll();
        assertEquals(1, all1.size());

        RealmResults<Owner> owners3 = testRealm.where(Owner.class).greaterThan("dogs.birthday", new Date(9)).findAll();
        assertEquals(1, owners3.size());

        RealmResults<Owner> owners4 = testRealm.where(Owner.class).greaterThanOrEqualTo("dogs.birthday", new Date(9)).findAll();
        assertEquals(1, owners4.size());

        RealmResults<Owner> owners5 = testRealm.where(Owner.class).lessThan("dogs.birthday", new Date(10000)).findAll();
        assertEquals(1, owners5.size());

        RealmResults<Owner> owners6 = testRealm.where(Owner.class).lessThanOrEqualTo("dogs.birthday", new Date(10000)).findAll();
        assertEquals(1, owners6.size());

        try {
            RealmResults<Owner> owners7 = testRealm.where(Owner.class).between("dogs.birthday", new Date(1000), new Date(3000)).findAll();
            fail();
        } catch (IllegalArgumentException ignore) {
            // Between does not support link queries.
        }
    }

    @Test
    public void queryMultipleRelationsFloat() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).greaterThanOrEqualTo("dogs.height", 0.2f).findAll();
        assertEquals(1, owners1.size());
        assertEquals(12, owners1.first().getCat().getAge());

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).greaterThan("dogs.height", 0.2f).findAll();
        assertEquals(1, owners2.size());
        assertEquals(12, owners2.first().getCat().getAge());

        RealmResults<Owner> owners3 = testRealm.where(Owner.class).lessThan("dogs.height", 2.2f).findAll();
        assertEquals(1, owners3.size());
        assertEquals(12, owners3.first().getCat().getAge());

        RealmResults<Owner> owners4 = testRealm.where(Owner.class).lessThanOrEqualTo("dogs.height", 2.2f).findAll();
        assertEquals(1, owners4.size());
        assertEquals(12, owners4.first().getCat().getAge());

        RealmResults<Owner> owners5 = testRealm.where(Owner.class).notEqualTo("dogs.height", 0.2f).findAll();
        assertEquals(1, owners5.size());
        assertEquals(12, owners5.first().getCat().getAge());

        RealmResults<Owner> owners6 = testRealm.where(Owner.class).greaterThanOrEqualTo("dogs.height", 0.3f).findAll();
        assertEquals(1, owners6.size());
        assertEquals(12, owners6.first().getCat().getAge());

        try {
            RealmResults<Owner> owners7 = testRealm.where(Owner.class).between("dogs.height", 0.2f, 2.2f).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void queryMultipleRelationsDouble() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).greaterThanOrEqualTo("dogs.weight", 0.2).findAll();
        assertEquals(1, owners1.size());
        assertEquals(12, owners1.first().getCat().getAge());

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).greaterThan("dogs.weight", 0.2).findAll();
        assertEquals(1, owners2.size());
        assertEquals(12, owners2.first().getCat().getAge());

        RealmResults<Owner> owners3 = testRealm.where(Owner.class).lessThan("dogs.weight", 12.2).findAll();
        assertEquals(1, owners3.size());
        assertEquals(12, owners3.first().getCat().getAge());

        RealmResults<Owner> owners4 = testRealm.where(Owner.class).lessThanOrEqualTo("dogs.weight", 12.2).findAll();
        assertEquals(1, owners4.size());
        assertEquals(12, owners4.first().getCat().getAge());

        RealmResults<Owner> owners5 = testRealm.where(Owner.class).notEqualTo("dogs.weight", 0.2).findAll();
        assertEquals(1, owners5.size());
        assertEquals(12, owners5.first().getCat().getAge());

        RealmResults<Owner> owners6 = testRealm.where(Owner.class).greaterThanOrEqualTo("dogs.weight", 0.3).findAll();
        assertEquals(1, owners6.size());
        assertEquals(12, owners6.first().getCat().getAge());

        try {
            RealmResults<Owner> owners7 = testRealm.where(Owner.class).between("dogs.weight", 0.2, 12.2).findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void queryMultipleRelationsString() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).equalTo("dogs.name", "Pluto").findAll();
        assertEquals(1, owners1.size());

        RealmResults<Owner> none1 = testRealm.where(Owner.class).equalTo("dogs.name", "King").findAll();
        assertEquals(0, none1.size());

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).notEqualTo("dogs.name", "King").findAll();
        assertEquals(1, owners2.size());

        RealmResults<Owner> none2 = testRealm.where(Owner.class).notEqualTo("dogs.name", "Pluto").findAll();
        assertEquals(0, none1.size());

        RealmResults<Owner> owners3 = testRealm.where(Owner.class).beginsWith("dogs.name", "Blackie").findAll();
        assertEquals(0, owners3.size());

        RealmResults<Owner> owners4 = testRealm.where(Owner.class).endsWith("dogs.name", "Blackie").findAll();
        assertEquals(0, owners4.size());

        RealmResults<Owner> owners5 = testRealm.where(Owner.class).contains("dogs.name", "Blackie").findAll();
        assertEquals(0, owners5.size());
    }

    @Test
    public void queryShouldFail() {
        try {
            RealmResults<Owner> owners = testRealm.where(Owner.class).equalTo("cat..hasTail", true).findAll();
            fail("Should throw Exception (double dot)");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            RealmResults<Owner> owners = testRealm.where(Owner.class).equalTo(".cat.hasTail", true).findAll();
            fail("Should throw Exception (initial dot)");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            RealmResults<Owner> owners = testRealm.where(Owner.class).equalTo("cat.hasTail.", true).findAll();
            fail("Should throw Exception (final dot)");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            RealmResults<Owner> owners = testRealm.where(Owner.class).equalTo("not.there", true).findAll();
            fail("Should throw Exception (non-existent column)");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void where() throws Exception {
        RealmResults<Owner> owners = testRealm.where(Owner.class).findAll();
        RealmResults<Dog> dogs = owners.first().getDogs().where().equalTo("name", "Pluto").findAll();
        assertEquals(1, dogs.size());
        assertEquals("Pluto", dogs.first().getName());
        assertEquals(5, dogs.first().getAge());

        RealmResults<Dog> none = owners.first().getDogs().where().equalTo("name", "Mars").findAll();
        assertEquals(0, none.size());
    }

    @Test
    public void subquery() {
        RealmResults<Owner> owners = testRealm.where(Owner.class).equalTo("dogs.name", "Pluto").findAll();
        RealmResults<Owner> subOwners = owners.where().equalTo("cat.name", "Blackie").findAll();
        assertEquals(1, subOwners.size());
    }

    @Test
    public void linkIsNull() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).isNull("cat").findAll();
        assertEquals(0, owners1.size());

        testRealm.beginTransaction();
        testRealm.delete(Cat.class);
        testRealm.commitTransaction();

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).isNull("cat").findAll();
        assertEquals(1, owners2.size());

        RealmResults<Owner> owners3 = testRealm.where(Owner.class).isNull("dogs.birthday").findAll();
        assertEquals(0, owners3.size());
    }

    @Test
    public void linkIsNotNull() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).isNotNull("cat").findAll();
        assertEquals(1, owners1.size());

        testRealm.beginTransaction();
        testRealm.delete(Cat.class);
        testRealm.commitTransaction();

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).isNotNull("cat").findAll();
        assertEquals(0, owners2.size());

        RealmResults<Owner> owners3 = testRealm.where(Owner.class).isNotNull("dogs.birthday").findAll();
        assertEquals(1, owners3.size());
    }

    @Test
    public void isEmpty() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).isEmpty("cat.name").findAll();
        assertEquals(0, owners1.size());

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).isEmpty("dogs.name").findAll();
        assertEquals(0, owners2.size());
    }

    @Test
    public void isNotEmpty() {
        RealmResults<Owner> owners1 = testRealm.where(Owner.class).isNotEmpty("cat.name").findAll();
        assertEquals(1, owners1.size());

        RealmResults<Owner> owners2 = testRealm.where(Owner.class).isNotEmpty("dogs.name").findAll();
        assertEquals(1, owners2.size());
    }

    @Test
    public void isNullWrongType() {
        try {
            // AllTypes.columnFloat is not nullable.
            testRealm.where(AllTypes.class).isNull("columnFloat").findAll();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }
}
